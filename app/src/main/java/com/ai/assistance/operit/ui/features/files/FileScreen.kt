package com.ai.assistance.operit.ui.features.files

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ai.assistance.operit.ui.main.LocalDemoController
import com.ai.assistance.operit.ui.main.FileItem
import com.ai.assistance.operit.ui.theme.*

@Composable
fun FileScreen() {
    val demoController = LocalDemoController.current
    var searchQuery by remember { mutableStateOf("") }
    val filters = listOf("全部", "文档", "图片", "代码", "音频")
    var selectedFilter by remember { mutableStateOf("全部") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // 使用系统背景色
            //.statusBarsPadding() // PhoneLayout 已处理，或者根据情况调整。用户说“留白太多”，可能要去掉这个
            // 如果 AppContent 没有 padding 顶部，那么我们需要。但用户说太多，可能我们 padding 了两次。
            // PhoneLayout 的 Scaffold 有 contentWindowInsets=0，AppContent 内部没有 padding。
            // 但是 SmallTopAppBar (in AppContent) 被隐藏了 (if !Home). 
            // Wait, AppContent logic: if !Home -> Show TopBar.
            // Oh! The condition I added was: `if (currentScreen !is Screen.Home) { SmallTopAppBar(...) }`.
            // So for Files and Skills, the TopBar IS SHOWING!
            // That's why there is so much whitespace! User sees TopBar title (likely empty or generic) AND my custom title.
            // I need to HIDE the AppContent TopBar for Files and Skills too.
    ) {
        // 搜索栏直接放在最上面? 或者留一点点 SystemBar 避让
        Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))


        // Search Bar with explicit Search Icon click
        SearchBar(searchQuery, onQueryChange = { searchQuery = it })

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

        // Staggered Grid Content
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalItemSpacing = 12.dp,
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 100.dp)
        ) {
            items(demoController.files.filter { 
                selectedFilter == "全部" || it.type == selectedFilter 
            }) { file ->
                FileCard(file)
            }
        }
    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("搜索本地文件...", style = MaterialTheme.typography.bodyMedium) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(50.dp)
            .clip(RoundedCornerShape(25.dp)),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f),
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        singleLine = true
    )
}

@Composable
fun FileCard(file: FileItem) {
    // 使用 Material 3 的 Card，自带 Ripple 和 Elevation
    Card(
        onClick = { /* TODO: Open File */ },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = file.color // 保持多彩配色，或使用 Surface 颜色
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Icon
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(file.icon, contentDescription = null, tint = Color.Black.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = file.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                color = Color(0xFF1A1A1A)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Tags
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                file.tags.forEach { tag ->
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
