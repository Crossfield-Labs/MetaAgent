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
    onOpenReviewDemo: () -> Unit = {},
    onOpenCourseDemo: () -> Unit = {},
    onOpenTaskDemo: () -> Unit = {},
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

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                DemoEntryCard(
                    title = "碎片复习",
                    subtitle = "答错卡片、画像变化、晚间提醒、续上进度",
                    icon = Icons.Default.School,
                    variant = HomeCardVariant.Review,
                    onClick = onOpenReviewDemo
                )
            }
            item {
                DemoEntryCard(
                    title = "课程整理",
                    subtitle = "导入资料、处理中、结构化笔记、课程追问",
                    icon = Icons.Default.Person,
                    variant = HomeCardVariant.Course,
                    onClick = onOpenCourseDemo
                )
            }
            item {
                DemoEntryCard(
                    title = "跨端任务",
                    subtitle = "看计划、批准、电脑执行、手机改方向",
                    icon = Icons.Default.Notifications,
                    variant = HomeCardVariant.Task,
                    onClick = onOpenTaskDemo
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        HomeOverviewBoard(
            onOpenReviewDemo = onOpenReviewDemo,
            onOpenCourseDemo = onOpenCourseDemo,
            onOpenTaskDemo = onOpenTaskDemo
        )

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

@Composable
private fun HomeOverviewBoard(
    onOpenReviewDemo: () -> Unit,
    onOpenCourseDemo: () -> Unit,
    onOpenTaskDemo: () -> Unit
) {
    val state = LearningDemoState
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("今天该做什么", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(todayOverviewText(), style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatusPill("最近任务", taskSummary(state.taskStage))
                StatusPill("课程入口", courseSummary(state.courseStage))
                StatusPill("复习入口", reviewSummary(state.reviewStage))
            }
            if (state.taskStage >= CrossDeviceTaskStage.RUNNING_ON_DESKTOP) {
                Text(
                    "进行中的任务状态：电脑端执行 ${(state.desktopProgress * 100).toInt()}%，手机端可继续聊天。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickJumpChip("去复习", onOpenReviewDemo)
                QuickJumpChip("去课程", onOpenCourseDemo)
                QuickJumpChip("去任务", onOpenTaskDemo)
            }
        }
    }
}

@Composable
private fun StatusPill(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun QuickJumpChip(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(text, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), style = MaterialTheme.typography.labelLarge)
    }
}

private fun todayOverviewText(): String {
    val state = LearningDemoState
    return when {
        state.reviewStage == ReviewDemoStage.START -> "先补 1 轮错卡，再决定是否进入课程整理。"
        state.courseStage < CourseDemoStage.GENERATED -> "课程资料还没整理成笔记，建议先完成课程整理。"
        state.taskStage < CrossDeviceTaskStage.RUNNING_ON_DESKTOP -> "跨端任务还没批准执行，先过计划。"
        else -> "电脑端任务正在跑，手机端优先复习并准备课程追问。"
    }
}

private fun taskSummary(stage: CrossDeviceTaskStage): String = when (stage) {
    CrossDeviceTaskStage.DRAFT -> "待发起"
    CrossDeviceTaskStage.PLAN_READY -> "待批准"
    CrossDeviceTaskStage.APPROVED -> "已批准"
    CrossDeviceTaskStage.RUNNING_ON_DESKTOP -> "执行中"
    CrossDeviceTaskStage.CHATTING_ON_PHONE -> "可聊天续上"
    CrossDeviceTaskStage.REDIRECTED -> "已改方向"
}

private fun courseSummary(stage: CourseDemoStage): String = when (stage) {
    CourseDemoStage.EMPTY -> "待导入"
    CourseDemoStage.IMPORTED -> "待整理"
    CourseDemoStage.PROCESSING -> "处理中"
    CourseDemoStage.GENERATED -> "已出笔记"
    CourseDemoStage.QA -> "可追问"
}

private fun reviewSummary(stage: ReviewDemoStage): String = when (stage) {
    ReviewDemoStage.START -> "待开始"
    ReviewDemoStage.ANSWERED_WRONG -> "已答错"
    ReviewDemoStage.PROFILE_UPDATED -> "画像已变"
    ReviewDemoStage.REMINDER_READY -> "待提醒"
    ReviewDemoStage.RESUMED -> "已续上"
}

private enum class HomeCardVariant {
    Review,
    Course,
    Task
}

@Composable
private fun DemoEntryCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    variant: HomeCardVariant,
    onClick: () -> Unit
) {
    val state = LearningDemoState
    val progressTarget = when (variant) {
        HomeCardVariant.Review -> when (state.reviewStage) {
            ReviewDemoStage.START -> 0.24f
            ReviewDemoStage.ANSWERED_WRONG -> 0.42f
            ReviewDemoStage.PROFILE_UPDATED -> 0.62f
            ReviewDemoStage.REMINDER_READY -> 0.82f
            ReviewDemoStage.RESUMED -> 0.94f
        }
        HomeCardVariant.Course -> when (state.courseStage) {
            CourseDemoStage.EMPTY -> 0.12f
            CourseDemoStage.IMPORTED -> 0.32f
            CourseDemoStage.PROCESSING -> 0.56f
            CourseDemoStage.GENERATED -> 0.82f
            CourseDemoStage.QA -> 0.94f
        }
        HomeCardVariant.Task -> when (state.taskStage) {
            CrossDeviceTaskStage.DRAFT -> 0.12f
            CrossDeviceTaskStage.PLAN_READY -> 0.28f
            CrossDeviceTaskStage.APPROVED -> 0.46f
            CrossDeviceTaskStage.RUNNING_ON_DESKTOP -> 0.68f
            CrossDeviceTaskStage.CHATTING_ON_PHONE -> 0.82f
            CrossDeviceTaskStage.REDIRECTED -> 0.94f
        }
    }
    val animatedProgress by animateFloatAsState(
        targetValue = progressTarget,
        animationSpec = tween(durationMillis = 700),
        label = "home_card_progress"
    )
    val infiniteTransition = rememberInfiniteTransition(label = "home_card_status")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "home_card_pulse_alpha"
    )
    val scanOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600),
            repeatMode = RepeatMode.Restart
        ),
        label = "home_card_scan"
    )
    val statusLabel = when (variant) {
        HomeCardVariant.Review -> when (state.reviewStage) {
            ReviewDemoStage.START -> "待开始"
            ReviewDemoStage.ANSWERED_WRONG -> "答错 4 张"
            ReviewDemoStage.PROFILE_UPDATED -> "画像已更新"
            ReviewDemoStage.REMINDER_READY -> "今晚提醒"
            ReviewDemoStage.RESUMED -> "已续上"
        }
        HomeCardVariant.Course -> when (state.courseStage) {
            CourseDemoStage.EMPTY -> "待导入"
            CourseDemoStage.IMPORTED -> "资料已入列"
            CourseDemoStage.PROCESSING -> "正在整理"
            CourseDemoStage.GENERATED -> "笔记已生成"
            CourseDemoStage.QA -> "可继续追问"
        }
        HomeCardVariant.Task -> when (state.taskStage) {
            CrossDeviceTaskStage.DRAFT -> "待发起"
            CrossDeviceTaskStage.PLAN_READY -> "计划已出"
            CrossDeviceTaskStage.APPROVED -> "已批准"
            CrossDeviceTaskStage.RUNNING_ON_DESKTOP -> "电脑执行中"
            CrossDeviceTaskStage.CHATTING_ON_PHONE -> "手机可续聊"
            CrossDeviceTaskStage.REDIRECTED -> "方向已调整"
        }
    }
    val detailText = when (variant) {
        HomeCardVariant.Review -> when (state.reviewStage) {
            ReviewDemoStage.START -> "今天还有 ${state.wrongCardsToday} 张错卡待过。"
            ReviewDemoStage.ANSWERED_WRONG -> "掌握度回落到 ${(state.mastery * 100).toInt()}%，准备更新画像。"
            ReviewDemoStage.PROFILE_UPDATED -> state.profileSummary
            ReviewDemoStage.REMINDER_READY -> state.nextRecommendation
            ReviewDemoStage.RESUMED -> "复习已续上，剩余 ${state.wrongCardsToday} 张错卡。"
        }
        HomeCardVariant.Course -> when (state.courseStage) {
            CourseDemoStage.EMPTY -> "导入本节资料后，会落到课程内笔记与卡片。"
            CourseDemoStage.IMPORTED -> "资料已接入，下一步进入结构化整理。"
            CourseDemoStage.PROCESSING -> "正在抽取章节结构、重点概念和可复习卡片。"
            CourseDemoStage.GENERATED -> "已产出 ${state.generatedCardCount} 张卡片和课程笔记。"
            CourseDemoStage.QA -> state.courseQaMessages.lastOrNull() ?: "课程内追问已就绪。"
        }
        HomeCardVariant.Task -> when (state.taskStage) {
            CrossDeviceTaskStage.DRAFT -> "先发起任务，再查看执行计划。"
            CrossDeviceTaskStage.PLAN_READY -> "计划已生成，批准后切到电脑继续跑。"
            CrossDeviceTaskStage.APPROVED -> "任务已批准，正在准备接管电脑端执行。"
            CrossDeviceTaskStage.RUNNING_ON_DESKTOP -> "电脑端已推进到 ${(state.desktopProgress * 100).toInt()}%，手机端可继续对话。"
            CrossDeviceTaskStage.CHATTING_ON_PHONE -> "执行不中断，你可以边聊边追改方向。"
            CrossDeviceTaskStage.REDIRECTED -> state.taskDirection
        }
    }
    val isActive = when (variant) {
        HomeCardVariant.Review -> state.reviewStage == ReviewDemoStage.REMINDER_READY
        HomeCardVariant.Course -> state.courseStage == CourseDemoStage.PROCESSING
        HomeCardVariant.Task -> state.taskStage == CrossDeviceTaskStage.RUNNING_ON_DESKTOP
    }
    Card(
        modifier = Modifier
            .width(220.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(158.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = if (isActive) pulseAlpha else 0.55f))
                        )
                        Text(statusLabel, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.75f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .height(8.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.88f))
                )
                if (isActive) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.18f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.35f))
                            .alpha(pulseAlpha)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            AnimatedContent(targetState = detailText, label = "home_card_detail") { currentDetail ->
                Text(
                    currentDetail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = if (isActive) "状态持续更新中 ${(scanOffset * 100).toInt()}%" else "点开继续",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
