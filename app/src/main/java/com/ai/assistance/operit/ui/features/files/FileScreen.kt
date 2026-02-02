package com.ai.assistance.operit.ui.features.files

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ai.assistance.operit.ui.main.FileItem
import com.ai.assistance.operit.ui.main.LocalDemoController

/**
 * 文件管理页面
 * 支持：文件夹选择、图片预览、多选操作、发送Agent
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileScreen(
    onNavigateToChat: (List<String>) -> Unit = {} // 导航到Chat并附带文件路径
) {
    val context = LocalContext.current
    val demoController = LocalDemoController.current
    var searchQuery by remember { mutableStateOf("") }
    val filters = listOf("全部", "文档", "图片", "代码", "音频", "视频")
    var selectedFilter by remember { mutableStateOf("全部") }
    
    // 图片预览状态
    var previewImageUri by remember { mutableStateOf<Uri?>(null) }
    
    // 文件夹选择器
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            // 持久化权限
            context.contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            demoController.loadFromFolder(context, it)
            Toast.makeText(context, "已加载文件夹", Toast.LENGTH_SHORT).show()
        }
    }
    
    // 过滤后的文件列表
    val filteredFiles = demoController.files.filter { file ->
        val matchesFilter = selectedFilter == "全部" || file.type == selectedFilter
        val matchesSearch = searchQuery.isEmpty() || 
            file.name.contains(searchQuery, ignoreCase = true) ||
            file.tags.any { it.contains(searchQuery, ignoreCase = true) }
        matchesFilter && matchesSearch
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

            // Search Bar
            FileSearchBar(searchQuery) { searchQuery = it }

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 24.dp)
            ) {
                items(filters) { filter ->
                    FilterChip(
                        label = { Text(filter) },
                        selected = filter == selectedFilter,
                        onClick = { selectedFilter = filter },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 文件网格
            if (filteredFiles.isEmpty()) {
                // 空状态
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.FolderOpen,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "点击右下角按钮选择文件夹",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalItemSpacing = 12.dp,
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 150.dp)
                ) {
                    items(filteredFiles, key = { it.id }) { file ->
                        FileCard(
                            file = file,
                            isSelectionMode = demoController.isSelectionMode.value,
                            onTap = {
                                if (demoController.isSelectionMode.value) {
                                    demoController.toggleSelection(file)
                                } else {
                                    // 点击打开文件
                                    openFile(context, file) { uri ->
                                        previewImageUri = uri
                                    }
                                }
                            },
                            onLongPress = {
                                demoController.toggleSelection(file)
                            }
                        )
                    }
                }
            }
        }

        // FAB - 选择文件夹
        if (!demoController.isSelectionMode.value) {
            FloatingActionButton(
                onClick = { folderPickerLauncher.launch(null) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 100.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.FolderOpen, contentDescription = "选择文件夹")
            }
        }

        // 多选操作栏 - Material Design 3 BottomAppBar 风格
        AnimatedVisibility(
            visible = demoController.isSelectionMode.value,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            SelectionActionBar(
                selectedCount = demoController.selectedFiles.size,
                onSendToAgent = {
                    val paths = demoController.getSelectedFilePaths()
                    if (paths.isNotEmpty()) {
                        onNavigateToChat(paths)
                        Toast.makeText(context, "正在发送 ${paths.size} 个文件到 Agent...", Toast.LENGTH_SHORT).show()
                    }
                    demoController.clearSelection()
                },
                onDelete = {
                    val count = demoController.selectedFiles.size
                    demoController.selectedFiles.toList().forEach { file ->
                        demoController.removeFile(file)
                    }
                    demoController.clearSelection()
                    Toast.makeText(context, "已删除 $count 个文件", Toast.LENGTH_SHORT).show()
                },
                onCancel = {
                    demoController.clearSelection()
                }
            )
        }
        
        // 图片预览 Dialog
        previewImageUri?.let { uri ->
            ImagePreviewDialog(
                uri = uri,
                onDismiss = { previewImageUri = null }
            )
        }
    }
}

@Composable
private fun FileSearchBar(query: String, onQueryChange: (String) -> Unit) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("搜索文件...", style = MaterialTheme.typography.bodyMedium) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "清除")
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(50.dp)
            .clip(RoundedCornerShape(25.dp)),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        singleLine = true
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileCard(
    file: FileItem,
    isSelectionMode: Boolean,
    onTap: () -> Unit,
    onLongPress: () -> Unit
) {
    val isImage = file.type == "图片" || file.mimeType?.startsWith("image/") == true
    
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = file.color),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onTap,
                onLongClick = onLongPress
            )
            .then(
                if (file.isSelected) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                } else Modifier
            )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // 顶部：图标或缩略图 + 选中框
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // 图标或图片缩略图
                if (isImage && file.uri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(file.uri)
                            .crossfade(true)
                            .build(),
                        contentDescription = file.name,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                file.icon, 
                                contentDescription = null, 
                                tint = Color.Black.copy(alpha = 0.7f), 
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
                
                // 选中框
                if (isSelectionMode) {
                    Checkbox(
                        checked = file.isSelected,
                        onCheckedChange = null,
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 文件名
            Text(
                text = file.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = Color(0xFF1A1A1A)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 标签
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                file.tags.take(2).forEach { tag ->
                    Surface(
                        color = Color.White.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = tag,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = Color(0xFF333333)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Material Design 3 风格的多选操作栏
 * 参考: BottomAppBar 设计规范
 */
@Composable
private fun SelectionActionBar(
    selectedCount: Int,
    onSendToAgent: () -> Unit,
    onDelete: () -> Unit,
    onCancel: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = 6.dp,
        shape = RoundedCornerShape(28.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧: 取消按钮 + 选中数量
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onCancel) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "取消",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    "已选 $selectedCount 项",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // 右侧: 操作按钮
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 删除按钮
                FilledTonalIconButton(
                    onClick = onDelete,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "删除")
                }
                
                // 发送 Agent 按钮
                FilledIconButton(
                    onClick = onSendToAgent,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(Icons.Default.Send, contentDescription = "发送 Agent")
                }
            }
        }
    }
}

@Composable
private fun ImagePreviewDialog(uri: Uri, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(uri)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Icon(
                    Icons.Default.Close, 
                    contentDescription = "关闭",
                    tint = Color.White
                )
            }
        }
    }
}

private fun openFile(
    context: android.content.Context, 
    file: FileItem,
    onPreviewImage: (Uri) -> Unit
) {
    val uri = file.uri ?: return
    val mimeType = file.mimeType ?: "*/*"
    
    // 图片：应用内预览
    if (mimeType.startsWith("image/")) {
        onPreviewImage(uri)
        return
    }
    
    // 其他文件：Intent 打开
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "无法打开此文件", Toast.LENGTH_SHORT).show()
    }
}
