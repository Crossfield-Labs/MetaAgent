@file:OptIn(ExperimentalLayoutApi::class)

package com.ai.assistance.metaagent.ui.features.memory.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ai.assistance.metaagent.ui.components.CustomScaffold
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ai.assistance.metaagent.core.tools.AIToolHandler
import com.ai.assistance.metaagent.core.tools.StringResultData
import com.ai.assistance.metaagent.data.model.AITool
import com.ai.assistance.metaagent.data.model.ToolParameter
import com.ai.assistance.metaagent.data.preferences.preferencesManager
import com.ai.assistance.metaagent.ui.features.memory.screens.dialogs.BatchDeleteConfirmDialog
import com.ai.assistance.metaagent.ui.features.memory.screens.dialogs.DocumentViewDialog
import com.ai.assistance.metaagent.ui.features.memory.screens.dialogs.EditMemorySheet
import com.ai.assistance.metaagent.ui.features.memory.screens.dialogs.LinkMemoryDialog
import com.ai.assistance.metaagent.ui.features.memory.screens.dialogs.MemoryInfoDialog
import com.ai.assistance.metaagent.ui.features.memory.screens.dialogs.EdgeInfoDialog
import com.ai.assistance.metaagent.ui.features.memory.screens.dialogs.EditEdgeDialog
import com.ai.assistance.metaagent.ui.features.memory.viewmodel.MemoryViewModel
import com.ai.assistance.metaagent.ui.features.memory.viewmodel.MemoryViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import android.provider.OpenableColumns
import com.ai.assistance.metaagent.util.AppLogger
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import com.ai.assistance.metaagent.R
import com.ai.assistance.metaagent.data.model.Memory
import com.ai.assistance.metaagent.ui.features.memory.screens.dialogs.MemorySearchSettingsDialog
import com.ai.assistance.metaagent.ui.features.memory.screens.dialogs.MemorySearchSimulationDialog
import com.ai.assistance.metaagent.ui.features.memory.viewmodel.MemoryUiState

@Composable
fun MemorySearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onSettingsClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(onClick = onMenuClick) {
            Icon(
                Icons.Default.Folder, 
                contentDescription = "Toggle Folders",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text(stringResource(R.string.memory_search_hint)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                keyboardController?.hide()
                onSearch()
            })
        )
        IconButton(onClick = onSettingsClick) {
            Icon(
                Icons.Default.Settings,
                contentDescription = stringResource(R.string.memory_search_settings_title),
                tint = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

private enum class MemoryDisplayMode { GRAPH, LIST }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MemoryScreen() {
    val context = LocalContext.current
    val profileList by preferencesManager.profileListFlow.collectAsState(initial = emptyList())
    val activeProfileId by
    preferencesManager.activeProfileIdFlow.collectAsState(initial = "default")

    // 获取所有配置文件的名称映射(id -> name)
    val profileNameMap = remember { mutableStateMapOf<String, String>() }

    // 加载所有配置文件名称
    LaunchedEffect(profileList) {
        profileList.forEach { profileId ->
            val profile = preferencesManager.getUserPreferencesFlow(profileId).first()
            profileNameMap[profileId] = profile.name
        }
    }

    var selectedProfileId by remember { mutableStateOf(activeProfileId) }
    var showFolderNavigator by remember { mutableStateOf(false) }
    var displayMode by remember { mutableStateOf(MemoryDisplayMode.GRAPH) }

    LaunchedEffect(activeProfileId) { selectedProfileId = activeProfileId }

    val viewModel: MemoryViewModel =
        viewModel(
            key = selectedProfileId, // Recreate ViewModel when profile changes
            factory = MemoryViewModelFactory(context, selectedProfileId)
        )
    val uiState by viewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let { fileUri ->
                scope.launch {
                    var tempFile: File? = null
                    try {
                        // More robust file name extraction
                        val (fileName, mimeType) = withContext(Dispatchers.IO) {
                            // Execute ContentResolver operations on IO thread
                            var extractedFileName = "Untitled"
                            context.contentResolver.query(fileUri, null, null, null, null)
                                ?.use { cursor ->
                                    if (cursor.moveToFirst()) {
                                        val displayNameIndex =
                                            cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                                        if (displayNameIndex != -1) {
                                            extractedFileName = cursor.getString(displayNameIndex)
                                        }
                                    }
                                }

                            val extractedMimeType = context.contentResolver.getType(fileUri)
                            Pair(extractedFileName, extractedMimeType)
                        }

                        if (mimeType != null && mimeType.startsWith("text")) {
                            val content = withContext(Dispatchers.IO) {
                                val inputStream = context.contentResolver.openInputStream(fileUri)
                                val reader = BufferedReader(InputStreamReader(inputStream))
                                reader.readText()
                            }
                            viewModel.importDocument(fileName, fileUri.toString(), content)
                        } else {
                            // For binary files, use the tool
                            tempFile = File(context.cacheDir, fileName)
                            withContext(Dispatchers.IO) {
                                val inputStream = context.contentResolver.openInputStream(fileUri)
                                val outputStream = FileOutputStream(tempFile)
                                inputStream?.use { input ->
                                    outputStream.use { output ->
                                        input.copyTo(output)
                                    }
                                }
                            }

                            val result = withContext(Dispatchers.IO) {
                                val toolHandler = AIToolHandler.getInstance(context)
                                val tool = AITool(
                                    name = "read_file_full",
                                    parameters = listOf(ToolParameter("path", tempFile.absolutePath))
                                )
                                toolHandler.executeTool(tool)
                            }

                            if (result.success) {
                                // Assuming result.result can be cast to StringResultData
                                val resultData = result.result
                                val content = if (resultData is StringResultData) {
                                    resultData.value
                                } else {
                                    resultData.toString()
                                }
                                viewModel.importDocument(fileName, fileUri.toString(), content)
                            } else {
                                AppLogger.e("MemoryScreen", "Tool execution failed: ${result.error}")
                            }
                        }
                    } catch (e: Exception) {
                        AppLogger.e("MemoryScreen", "Error processing file: $fileUri", e)
                    } finally {
                        tempFile?.delete()
                    }
                }
            }
        }
    )

    CustomScaffold(
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 只有在框选模式下才显示"确认删除"按钮
                if (uiState.isBoxSelectionMode) {
                    FloatingActionButton(
                        onClick = { viewModel.showBatchDeleteConfirm() },
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Selected")
                    }
                }

                // 框选模式切换按钮
                FloatingActionButton(
                    onClick = {
                        com.ai.assistance.metaagent.util.AppLogger.d(
                            "MemoryScreen",
                            "Box selection button clicked. Current mode: ${uiState.isBoxSelectionMode}, toggling to ${!uiState.isBoxSelectionMode}"
                        )
                        viewModel.toggleBoxSelectionMode(!uiState.isBoxSelectionMode)
                    },
                    containerColor = if (uiState.isBoxSelectionMode) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.SelectAll, contentDescription = "Toggle Box Selection Mode")
                }

                FloatingActionButton(
                    onClick = {
                        com.ai.assistance.metaagent.util.AppLogger.d(
                            "MemoryScreen",
                            "Linking button clicked. Current mode: ${uiState.isLinkingMode}, toggling to ${!uiState.isLinkingMode}"
                        )
                        viewModel.toggleLinkingMode(!uiState.isLinkingMode)
                    },
                    containerColor = if (uiState.isLinkingMode) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.Link, contentDescription = "Toggle Linking Mode")
                }
                FloatingActionButton(
                    onClick = {
                        filePickerLauncher.launch(
                            arrayOf(
                                "text/*",
                                "application/pdf",
                                "application/msword",
                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                            )
                        )
                    },
                    modifier = Modifier.size(48.dp),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = "Import Document")
                }
                FloatingActionButton(
                    onClick = { viewModel.startEditing(null) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Memory")
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                MemorySearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = { viewModel.onSearchQueryChange(it) },
                    onSearch = {
                        keyboardController?.hide()
                        viewModel.searchMemories()
                    },
                    onSettingsClick = { viewModel.showSearchSettingsDialog(true) },
                    onMenuClick = { showFolderNavigator = !showFolderNavigator }
                )

                MemoryOverviewPanel(
                    uiState = uiState,
                    displayMode = displayMode,
                    onDisplayModeChange = { displayMode = it },
                    onEditSelected = { uiState.selectedMemory?.let(viewModel::startEditing) },
                    onDeleteSelected = { uiState.selectedMemory?.let { viewModel.deleteMemory(it.id) } }
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                ) {
                    when (displayMode) {
                        MemoryDisplayMode.GRAPH -> {
                            GraphVisualizer(
                                graph = uiState.graph,
                                modifier = Modifier.fillMaxSize(),
                                selectedNodeId = uiState.selectedNodeId,
                                boxSelectedNodeIds = uiState.boxSelectedNodeIds,
                                isBoxSelectionMode = uiState.isBoxSelectionMode,
                                linkingNodeIds = uiState.linkingNodeIds,
                                selectedEdgeId = uiState.selectedEdge?.id,
                                onNodeClick = { node -> viewModel.selectNode(node) },
                                onEdgeClick = { edge -> viewModel.selectEdge(edge) },
                                onNodesSelected = { nodeIds -> viewModel.addNodesToSelection(nodeIds) }
                            )
                        }

                        MemoryDisplayMode.LIST -> {
                            MemoryListPanel(
                                memories = uiState.memories,
                                selectedUuid = uiState.selectedMemory?.uuid,
                                onMemoryClick = { memory -> viewModel.selectMemory(memory) }
                            )
                        }
                    }

                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
            // 左侧文件夹导航 (Overlay)
            AnimatedVisibility(
                visible = showFolderNavigator,
                enter = slideInHorizontally(initialOffsetX = { -it }),
                exit = slideOutHorizontally(targetOffsetX = { -it })
            ) {
                FolderNavigator(
                    folderPaths = uiState.folderPaths,
                    selectedFolderPath = uiState.selectedFolderPath,
                    onFolderSelected = { folderPath -> viewModel.selectFolder(folderPath) },
                    onFolderRename = { oldPath, newPath ->
                        viewModel.renameFolder(
                            oldPath,
                            newPath
                        )
                    },
                    onFolderDelete = { folderPath -> viewModel.deleteFolder(folderPath) },
                    onFolderCreate = { folderPath -> viewModel.createFolder(folderPath) },
                    onRefresh = { viewModel.refreshFolderList() },
                    profileList = profileList,
                    profileNameMap = profileNameMap,
                    selectedProfileId = selectedProfileId,
                    onProfileSelected = { selectedProfileId = it },
                    onDismissRequest = { showFolderNavigator = false }
                )
            }

            // 对话框层
            if (uiState.isSearchSettingsDialogVisible) {
                MemorySearchSettingsDialog(
                    currentConfig = uiState.searchConfig,
                    cloudConfig = uiState.cloudEmbeddingConfig,
                    dimensionUsage = uiState.embeddingDimensionUsage,
                    rebuildProgress = uiState.embeddingRebuildProgress,
                    isRebuilding = uiState.isEmbeddingRebuildRunning,
                    onDismiss = { viewModel.showSearchSettingsDialog(false) },
                    onSave = { config, cloudConfig ->
                        viewModel.saveSearchSettings(config, cloudConfig)
                        viewModel.searchMemories()
                    },
                    onRebuild = { viewModel.rebuildVectorIndex() },
                    onSimulateSearch = { viewModel.openSearchSimulationDialog() }
                )
            }

            if (uiState.isSearchSimulationDialogVisible) {
                MemorySearchSimulationDialog(
                    query = uiState.searchSimulationQuery,
                    isRunning = uiState.isSearchSimulationRunning,
                    result = uiState.searchSimulationResult,
                    error = uiState.searchSimulationError,
                    onQueryChange = { viewModel.onSearchSimulationQueryChange(it) },
                    onRun = { viewModel.runSearchSimulation() },
                    onDismiss = { viewModel.showSearchSimulationDialog(false) }
                )
            }

            if (uiState.isDocumentViewOpen && uiState.selectedMemory != null) {
                var memoryTitle by remember { mutableStateOf(uiState.selectedMemory!!.title) }
                val chunkStates = remember {
                    mutableStateMapOf<Long, String>().apply {
                        uiState.selectedDocumentChunks.forEach { put(it.id, it.content) }
                    }
                }
                // 当chunks列表变化时，同步状态
                LaunchedEffect(uiState.selectedDocumentChunks) {
                    chunkStates.clear()
                    uiState.selectedDocumentChunks.forEach { chunk ->
                        chunkStates[chunk.id] = chunk.content
                    }
                }

                DocumentViewDialog(
                    memoryTitle = memoryTitle,
                    onTitleChange = { memoryTitle = it },
                    chunks = uiState.selectedDocumentChunks,
                    chunkStates = chunkStates,
                    onChunkChange = { id, content -> chunkStates[id] = content },
                    searchQuery = uiState.documentSearchQuery,
                    onSearchQueryChange = { viewModel.onDocumentSearchQueryChange(it) },
                    onPerformSearch = { viewModel.performSearchInDocument() },
                    onDismiss = { viewModel.closeDocumentView() },
                    onSave = {
                        // 保存标题
                        if (memoryTitle != uiState.selectedMemory!!.title) {
                            viewModel.updateMemory(
                                memory = uiState.selectedMemory!!,
                                newTitle = memoryTitle,
                                newContent = uiState.selectedMemory!!.content,
                                newContentType = uiState.selectedMemory!!.contentType,
                                newSource = uiState.selectedMemory!!.source,
                                newCredibility = uiState.selectedMemory!!.credibility,
                                newImportance = uiState.selectedMemory!!.importance,
                                newFolderPath = uiState.selectedMemory!!.folderPath ?: "",
                                newTags = uiState.selectedMemory!!.tags.map { it.name }
                            )
                        }
                        // 保存有变动的chunks
                        chunkStates.forEach { (id, content) ->
                            val originalContent =
                                uiState.selectedDocumentChunks.find { it.id == id }?.content
                            if (content != originalContent) {
                                viewModel.updateChunkContent(id, content)
                            }
                        }
                        viewModel.closeDocumentView()
                    },
                    onDelete = { viewModel.deleteMemory(uiState.selectedMemory!!.id) },
                    folderPath = uiState.selectedMemory?.folderPath ?: ""
                )
            } else if (uiState.selectedMemory != null) {
                MemoryInfoDialog(
                    memory = uiState.selectedMemory!!,
                    onDismiss = { viewModel.clearSelection() },
                    onEdit = {
                        viewModel.startEditing(uiState.selectedMemory)
                        viewModel.clearSelection() // 关闭当前对话框
                    },
                    onDelete = { viewModel.deleteMemory(uiState.selectedMemory!!.id) }
                )
            }

            val selectedEdge = uiState.selectedEdge
            if (selectedEdge != null) {
                EdgeInfoDialog(
                    edge = selectedEdge,
                    graph = uiState.graph,
                    onDismiss = { viewModel.clearSelection() },
                    onEdit = {
                        viewModel.startEditingEdge(selectedEdge)
                        viewModel.clearSelection() // 同样, 点击编辑后关闭
                    },
                    onDelete = { viewModel.deleteEdge(selectedEdge.id) }
                )
            }

            if (uiState.linkingNodeIds.size == 2) {
                val sourceNode = uiState.graph.nodes.find { it.id == uiState.linkingNodeIds[0] }
                val targetNode = uiState.graph.nodes.find { it.id == uiState.linkingNodeIds[1] }
                if (sourceNode != null && targetNode != null) {
                    LinkMemoryDialog(
                        sourceNodeLabel = sourceNode.label,
                        targetNodeLabel = targetNode.label,
                        onDismiss = { viewModel.toggleLinkingMode(false) },
                        onLink = { type, weight, description ->
                            viewModel.linkMemories(
                                sourceNode.id,
                                targetNode.id,
                                type,
                                weight,
                                description
                            )
                        }
                    )
                }
            }

            if (uiState.isEditing) {
                EditMemorySheet(
                    memory = uiState.editingMemory,
                    allFolderPaths = uiState.folderPaths,
                    onDismiss = { viewModel.cancelEditing() },
                    onSave = { memory, title, content, contentType, source, credibility, importance, folderPath, tags ->
                        if (memory == null) {
                            // 创建新记忆的逻辑（如果需要的话）
                             viewModel.createMemory(title, content, contentType)
                        } else {
                            viewModel.updateMemory(
                                memory = memory,
                                newTitle = title,
                                newContent = content,
                                newContentType = contentType,
                                newSource = source,
                                newCredibility = credibility,
                                newImportance = importance,
                                newFolderPath = folderPath,
                                newTags = tags
                            )
                        }
                    }
                )
            }

            val editingEdge = uiState.editingEdge
            if (uiState.isEditingEdge && editingEdge != null) {
                EditEdgeDialog(
                    edge = editingEdge,
                    onDismiss = { viewModel.cancelEditingEdge() },
                    onSave = { type, weight, description ->
                        viewModel.updateEdge(editingEdge, type, weight, description)
                    }
                )
            }

            if (uiState.showBatchDeleteConfirm) {
                BatchDeleteConfirmDialog(
                    selectedCount = uiState.boxSelectedNodeIds.size,
                    onDismiss = { viewModel.dismissBatchDeleteConfirm() },
                    onConfirm = { viewModel.deleteSelectedNodes() }
                )
            }
        }
    }
}

@Composable
private fun MemoryOverviewPanel(
    uiState: MemoryUiState,
    displayMode: MemoryDisplayMode,
    onDisplayModeChange: (MemoryDisplayMode) -> Unit,
    onEditSelected: () -> Unit,
    onDeleteSelected: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MemoryStatCard("节点", uiState.graph.nodes.size.toString(), Modifier.weight(1f))
                MemoryStatCard("连线", uiState.graph.edges.size.toString(), Modifier.weight(1f))
                MemoryStatCard("条目", uiState.memories.size.toString(), Modifier.weight(1f))
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = displayMode == MemoryDisplayMode.GRAPH,
                    onClick = { onDisplayModeChange(MemoryDisplayMode.GRAPH) },
                    label = { Text("图谱") }
                )
                FilterChip(
                    selected = displayMode == MemoryDisplayMode.LIST,
                    onClick = { onDisplayModeChange(MemoryDisplayMode.LIST) },
                    label = { Text("条目") }
                )
                FilterChip(
                    selected = uiState.selectedFolderPath.isNotBlank(),
                    onClick = {},
                    label = {
                        Text(
                            if (uiState.selectedFolderPath.isBlank()) "全部文件夹"
                            else "文件夹: ${uiState.selectedFolderPath}"
                        )
                    }
                )
                uiState.searchQuery.takeIf { it.isNotBlank() }?.let { query ->
                    FilterChip(
                        selected = true,
                        onClick = {},
                        label = { Text("搜索: $query") }
                    )
                }
            }

            uiState.selectedMemory?.let { memory ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = memory.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = buildString {
                                append(memory.contentType)
                                if (!memory.folderPath.isNullOrBlank()) append(" · ${memory.folderPath}")
                                if (memory.isDocumentNode) append(" · 文档")
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (memory.tags.isNotEmpty()) {
                            Text(
                                text = memory.tags.joinToString(" · ") { it.name },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = memory.content,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = onEditSelected) {
                                Icon(Icons.Default.Edit, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("编辑")
                            }
                            TextButton(onClick = onDeleteSelected) {
                                Icon(Icons.Default.Delete, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("删除")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MemoryStatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        )
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MemoryListPanel(
    memories: List<Memory>,
    selectedUuid: String?,
    onMemoryClick: (Memory) -> Unit
) {
    if (memories.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "当前没有可显示的记忆条目。",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(memories, key = { it.uuid }) { memory ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (memory.uuid == selectedUuid) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    }
                ),
                onClick = { onMemoryClick(memory) }
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = memory.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (memory.isDocumentNode) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Text(
                        text = buildString {
                            append(memory.contentType)
                            if (!memory.folderPath.isNullOrBlank()) append(" · ${memory.folderPath}")
                            if (memory.tags.isNotEmpty()) append(" · ${memory.tags.joinToString { it.name }}")
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = memory.content,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

