package com.ai.assistance.metaagent.ui.features.home.screens

import android.net.Uri
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ai.assistance.metaagent.ui.features.home.components.SwipeableMessageItem
import com.ai.assistance.metaagent.ui.features.home.components.calculateGroupCorners
import com.ai.assistance.metaagent.ui.features.home.components.ChatBottomBar
import com.ai.assistance.metaagent.ui.features.home.components.MoreBottomSheet
import com.ai.assistance.metaagent.ui.features.home.data.MetaConversation
import com.ai.assistance.metaagent.ui.features.home.data.LearningDemoState
import com.ai.assistance.metaagent.ui.features.home.data.CourseDemoStage
import com.ai.assistance.metaagent.ui.features.home.data.CrossDeviceTaskStage
import com.ai.assistance.metaagent.ui.features.home.data.ReviewDemoStage
import com.ai.assistance.metaagent.ui.theme.SearchBarBackground
import kotlinx.coroutines.launch

/**
 * MetaAgent 主页 — 重新设计的聊天首页
 *
 * @param conversations 真实聊天列表（从 ChatHistoryManager 转换而来）
 * @param onConversationClick 点击会话 → 导航到对应聊天
 * @param onDeleteChat 删除会话回调
 */
@Composable
fun MetaAgentHomeScreen(
    conversations: List<MetaConversation>,
    onConversationClick: (String) -> Unit,
    onOpenFlashReview: () -> Unit = {},
    onBottomNavClick: (String) -> Unit = {},
    onAvatarClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onNewChatClick: () -> Unit = {},
    onDeleteChat: (String) -> Unit = {},
    avatarUri: Uri? = null,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("全部") }
    val filters = listOf("全部", "学习", "任务", "复习", "闲聊")
    var showMoreSheet by remember { mutableStateOf(false) }

    // 搜索状态
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

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

    // 基于搜索和筛选过滤对话列表
    val filteredConversations = remember(conversations, searchQuery, selectedFilter) {
        conversations.filter { conv ->
            // 搜索过滤
            val matchesSearch = searchQuery.isEmpty() ||
                conv.title.contains(searchQuery, ignoreCase = true) ||
                conv.lastMessage.contains(searchQuery, ignoreCase = true)

            // 标签过滤（目前 "全部" 不过滤，其它后续可扩展）
            val matchesFilter = selectedFilter == "全部" || conv.tag == selectedFilter

            matchesSearch && matchesFilter
        }
    }

    // 可变绘制列表（用于支持左滑标记读/未读等本地状态操作）
    val displayConversations = remember(filteredConversations) {
        mutableStateListOf(*filteredConversations.toTypedArray())
    }

    val dragProgressMap = remember { mutableStateMapOf<Int, Float>() }
    val snackbarHostState = remember { SnackbarHostState() }
    val snackScope = rememberCoroutineScope()

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

            // 搜索栏 — 可输入
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(28.dp))
                    .background(SearchBarBackground)
                    .clickable { isSearchActive = true }
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

                    Box(modifier = Modifier.weight(1f)) {
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                isSearchActive = true
                            },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = MaterialTheme.typography.bodyMedium.fontSize
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            decorationBox = { innerTextField ->
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "搜索对话...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }

                    // 清除按钮
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "清除搜索",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // 通知铃铛
            BadgedBox(
                badge = {
                    val unreadCount = conversations.count { it.isUnread }
                    if (unreadCount > 0) {
                        Badge(containerColor = MaterialTheme.colorScheme.error) {
                            Text("$unreadCount")
                        }
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

            // 闪卡复习入口
            IconButton(onClick = onOpenFlashReview) {
                Icon(
                    Icons.Default.School,
                    contentDescription = "闪卡复习",
                    tint = MaterialTheme.colorScheme.primary
                )
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

        Spacer(modifier = Modifier.height(12.dp))

        // ── 对话列表 ──
        Box(modifier = Modifier.fillMaxSize()) {
            if (displayConversations.isEmpty()) {
                // 空状态
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (searchQuery.isNotEmpty()) "🔍" else "💬",
                            style = MaterialTheme.typography.displaySmall
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "未找到匹配的对话"
                            else "还没有对话\n点击右下角 + 开始新对话",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    items(
                        count = displayConversations.size,
                        key = { displayConversations[it].id }
                    ) { index ->
                        val conversation = displayConversations[index]

                        val prevProgress = if (index > 0) dragProgressMap[index - 1] ?: 0f else 0f
                        val nextProgress = if (index < displayConversations.size - 1) dragProgressMap[index + 1] ?: 0f else 0f

                        val (topCorner, bottomCorner) = calculateGroupCorners(
                            index = index,
                            total = displayConversations.size,
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
                                displayConversations[index] = conversation.copy(isUnread = false)
                                snackScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "已标记为已读",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            },
                            onMarkUnread = {
                                displayConversations[index] = conversation.copy(isUnread = true)
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
                                val chatId = conversation.id
                                displayConversations.removeAt(index)
                                dragProgressMap.remove(index)
                                onDeleteChat(chatId)
                                snackScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "已删除「$title」",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        )
                    }
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
