package com.ai.assistance.metaagent.ui.features.home.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ai.assistance.metaagent.ui.features.home.components.SwipeableMessageItem
import com.ai.assistance.metaagent.ui.features.home.components.calculateGroupCorners
import com.ai.assistance.metaagent.ui.features.home.components.ChatBottomBar
import com.ai.assistance.metaagent.ui.features.home.components.MoreBottomSheet
import com.ai.assistance.metaagent.ui.features.home.data.MetaSampleData
import com.ai.assistance.metaagent.ui.theme.SearchBarBackground
import kotlinx.coroutines.launch

/**
 * MetaAgent 主页 — 重新设计的聊天首页
 */
@Composable
fun MetaAgentHomeScreen(
    onConversationClick: (String) -> Unit,
    onBottomNavClick: (String) -> Unit = {},
    onAvatarClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onNewChatClick: () -> Unit = {},
    avatarUri: Uri? = null,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("全部") }
    val filters = listOf("全部", "学习", "任务", "复习", "闲聊")
    var showMoreSheet by remember { mutableStateOf(false) }

    // "更多"上拉弹窗
    if (showMoreSheet) {
        MoreBottomSheet(
            onDismiss = { showMoreSheet = false },
            onNavigate = { route ->
                showMoreSheet = false
                onBottomNavClick(route)
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // 顶部：≡汉堡菜单 + 搜索栏 + 通知 + 头像
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左：汉堡菜单 → 打开左侧 drawer
            IconButton(onClick = onMenuClick) {
                Icon(
                    Icons.Default.Menu,
                    contentDescription = "菜单",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // 搜索栏
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(28.dp))
                    .background(SearchBarBackground)
                    .clickable { }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "搜索对话...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // 通知铃铛
            BadgedBox(
                badge = {
                    Badge(containerColor = MaterialTheme.colorScheme.error) {
                        Text("2")
                    }
                }
            ) {
                IconButton(onClick = { }) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "通知",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 右：用户头像
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onAvatarClick),
                contentAlignment = Alignment.Center
            ) {
                if (avatarUri != null) {
                    AsyncImage(
                        model = avatarUri,
                        contentDescription = "头像",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "M",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // 筛选标签
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filters) { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = {
                        Text(
                            text = filter,
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── 对话列表 — Android 16 分组堆叠 ──
        val conversations = remember {
            mutableStateListOf(*MetaSampleData.conversations.toTypedArray())
        }

        val dragProgressMap = remember { mutableStateMapOf<Int, Float>() }
        val snackbarHostState = remember { SnackbarHostState() }
        val snackScope = rememberCoroutineScope()

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                items(
                    count = conversations.size,
                    key = { conversations[it].id }
                ) { index ->
                    val conversation = conversations[index]

                    val prevProgress = if (index > 0) dragProgressMap[index - 1] ?: 0f else 0f
                    val nextProgress = if (index < conversations.size - 1) dragProgressMap[index + 1] ?: 0f else 0f

                    val (topCorner, bottomCorner) = calculateGroupCorners(
                        index = index,
                        total = conversations.size,
                        prevDragProgress = prevProgress,
                        nextDragProgress = nextProgress
                    )

                    SwipeableMessageItem(
                        conversation = conversation,
                        topCorner = topCorner,
                        bottomCorner = bottomCorner,
                        onClick = { onConversationClick(conversation.id) },
                        onDragProgressChanged = { progress ->
                            dragProgressMap[index] = progress
                        },
                        onMarkRead = {
                            conversations[index] = conversation.copy(isUnread = false)
                            snackScope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "已标记为已读",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        },
                        onMarkUnread = {
                            conversations[index] = conversation.copy(isUnread = true)
                            snackScope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "已标记为未读",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        },
                        onMute = { /* TODO */ },
                        onDelete = {
                            val title = conversation.title
                            conversations.removeAt(index)
                            dragProgressMap.remove(index)
                            snackScope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "已屏蔽「$title」",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    )
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 88.dp)
            )

            // 悬浮底部导航栏
            ChatBottomBar(
                currentRoute = "meta_home",
                onNavItemClick = { route ->
                    if (route == "more") {
                        showMoreSheet = true
                    } else {
                        onBottomNavClick(route)
                    }
                },
                onFabClick = onNewChatClick,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
