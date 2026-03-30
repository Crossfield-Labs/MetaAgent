package com.ai.assistance.metaagent.ui.features.home.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ai.assistance.metaagent.ui.components.CustomScaffold
import com.ai.assistance.metaagent.ui.features.home.data.CourseDemoStage
import com.ai.assistance.metaagent.ui.features.home.data.CrossDeviceTaskStage
import com.ai.assistance.metaagent.ui.features.home.data.LearningDemoState
import com.ai.assistance.metaagent.ui.features.home.data.ReviewDemoStage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewJourneyDemoScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state = LearningDemoState
    CustomScaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("碎片复习") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                HeroCard(
                    "今天答错 -> 画像变化 -> 晚些提醒 -> 再次打开续上",
                    "这条链会保留当前进度，再次进入时可以继续。",
                    Color(0xFF3467EB),
                    Icons.Default.School
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricChip("今日错卡", "${state.wrongCardsToday} 张")
                    MetricChip("掌握度", "${(state.mastery * 100).toInt()}%")
                    MetricChip("阶段", reviewStageLabel(state.reviewStage))
                }
            }
            item {
                JourneyCard(
                    title = "碎片复习流程",
                    body = reviewStageDescription(state.reviewStage, state.nextRecommendation)
                )
            }
            item {
                ReviewQuestionCard(
                    mastery = state.mastery,
                    stage = state.reviewStage
                )
            }
            item {
                ProfileCard(state.profileSummary)
            }
            item {
                AnimatedVisibility(
                    visible = state.reviewReminderVisible,
                    enter = fadeIn(tween(350)) + expandVertically(tween(350)),
                    exit = fadeOut(tween(220))
                ) {
                    ReminderCard(state.nextRecommendation)
                }
            }
            item {
                DemoActions(
                    primary = nextReviewActionLabel(state.reviewStage),
                    onPrimary = { state.advanceReviewDemo() },
                    secondary = "重新开始",
                    onSecondary = { state.resetReviewDemo() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseMaterialDemoScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state = LearningDemoState
    CustomScaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("课程整理") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                HeroCard(
                    "导入资料 -> 处理中 -> 笔记/卡片生成 -> 课程内追问",
                    "保留真实的导入、中间态、结果页和课程内追问结构。",
                    Color(0xFF0F9D7A),
                    Icons.Default.FolderZip
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricChip("当前课程", "计算机网络")
                    MetricChip("资料", "PDF + 截图")
                    MetricChip("阶段", courseStageLabel(state.courseStage))
                }
            }
            item {
                JourneyCard(
                    title = "导入动作",
                    body = when (state.courseStage) {
                        CourseDemoStage.EMPTY -> "点击“导入资料”后，课程空间出现本次导入条目。"
                        else -> "已导入：${state.importedMaterialName}"
                    }
                )
            }
            item {
                AnimatedVisibility(
                    visible = state.courseStage >= CourseDemoStage.PROCESSING,
                    enter = fadeIn(tween(350)) + expandVertically(tween(350)),
                    exit = fadeOut(tween(220))
                ) {
                    ProcessingCard()
                }
            }
            item {
                AnimatedVisibility(
                    visible = state.courseStage >= CourseDemoStage.GENERATED,
                    enter = fadeIn(tween(350)) + expandVertically(tween(350)),
                    exit = fadeOut(tween(220))
                ) {
                    ResultCard(
                        state.generatedNoteTitle,
                        "${state.generatedCardCount} 张卡片已生成，可继续用于课程内复习和追问。"
                    )
                }
            }
            item {
                AnimatedVisibility(
                    visible = state.courseStage >= CourseDemoStage.QA,
                    enter = fadeIn(tween(350)) + expandVertically(tween(350)),
                    exit = fadeOut(tween(220))
                ) {
                    InCourseQaCard()
                }
            }
            item {
                DemoActions(
                    primary = nextCourseActionLabel(state.courseStage),
                    onPrimary = { state.advanceCourseDemo() },
                    secondary = "重新开始",
                    onSecondary = { state.resetCourseDemo() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrossDeviceTaskDemoScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state = LearningDemoState
    CustomScaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("跨端任务") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                HeroCard(
                    "发起任务 -> 看计划 -> 批准 -> 电脑跑 -> 手机继续聊 -> 中途改方向",
                    "任务状态会连续推进，手机端和电脑端状态一起更新。",
                    Color(0xFFE87722),
                    Icons.Default.Computer
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricChip("任务阶段", taskStageLabel(state.taskStage))
                    MetricChip("电脑进度", "${(state.desktopProgress * 100).toInt()}%")
                    MetricChip("当前方向", if (state.taskStage >= CrossDeviceTaskStage.REDIRECTED) "已改方向" else "原计划")
                }
            }
            item {
                JourneyCard(
                    title = "任务目标",
                    body = state.taskGoal
                )
            }
            item {
                PlanCard(state.taskStage, state.taskDirection)
            }
            item {
                AnimatedVisibility(
                    visible = state.taskStage >= CrossDeviceTaskStage.RUNNING_ON_DESKTOP,
                    enter = fadeIn(tween(350)) + expandVertically(tween(350)),
                    exit = fadeOut(tween(220))
                ) {
                    DesktopRunningCard(state.desktopProgress)
                }
            }
            item {
                AnimatedVisibility(
                    visible = state.taskStage >= CrossDeviceTaskStage.CHATTING_ON_PHONE,
                    enter = fadeIn(tween(350)) + expandVertically(tween(350)),
                    exit = fadeOut(tween(220))
                ) {
                    PhoneChatCard()
                }
            }
            item {
                DemoActions(
                    primary = nextTaskActionLabel(state.taskStage),
                    onPrimary = { state.advanceTaskDemo() },
                    secondary = "重新开始",
                    onSecondary = { state.resetTaskDemo() }
                )
            }
        }
    }
}

@Composable
private fun HeroCard(
    title: String,
    subtitle: String,
    accent: Color,
    icon: ImageVector
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
                .padding(18.dp)
        ) {
            Box(
                modifier = Modifier.size(42.dp).background(accent.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun MetricChip(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun JourneyCard(title: String, body: String) {
    Card(shape = RoundedCornerShape(22.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ReviewQuestionCard(mastery: Float, stage: ReviewDemoStage) {
    val animatedMastery = animateFloatAsState(
        targetValue = mastery,
        animationSpec = tween(500),
        label = "reviewMastery"
    )
    Card(shape = RoundedCornerShape(22.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("今日复习题", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("TCP 拥塞控制的慢启动和拥塞避免分别在什么条件下切换？", style = MaterialTheme.typography.bodyLarge)
            AnimatedContent(targetState = reviewStageLabel(stage), label = "reviewStage") { label ->
                Text("当前表现：$label", color = MaterialTheme.colorScheme.primary)
            }
            LinearProgressIndicator(progress = animatedMastery.value, modifier = Modifier.fillMaxWidth().height(8.dp))
        }
    }
}

@Composable
private fun ProfileCard(summary: String) {
    Card(shape = RoundedCornerShape(22.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("学习画像变化", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            Text(summary, style = MaterialTheme.typography.bodyMedium)
            Text("证据来源：今日错题记录 + 课程进度 + 最近追问内容", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ReminderCard(text: String) {
    val transition = rememberInfiniteTransition(label = "reminderPulse")
    val iconAlpha = transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "reminderIconAlpha"
    )
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.NotificationsActive,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.alpha(iconAlpha.value)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text("晚些时候的复习提醒", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(text, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun ProcessingCard() {
    val transition = rememberInfiniteTransition(label = "processing")
    val animatedProgress = transition.animateFloat(
        initialValue = 0.24f,
        targetValue = 0.82f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "processingProgress"
    )
    val statusAlpha = transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "processingStatusAlpha"
    )
    Card(shape = RoundedCornerShape(22.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("处理中", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.width(8.dp))
                AnimatedStatusDots(alpha = statusAlpha.value)
            }
            AnimatedContent(targetState = processingCaption(animatedProgress.value), label = "processingCaption") { caption ->
                Text(caption, style = MaterialTheme.typography.bodyMedium)
            }
            LinearProgressIndicator(progress = animatedProgress.value, modifier = Modifier.fillMaxWidth().height(8.dp))
        }
    }
}

@Composable
private fun ResultCard(title: String, body: String) {
    Card(shape = RoundedCornerShape(22.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoStories, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("整理结果落地页", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun InCourseQaCard() {
    val messages = LearningDemoState.courseQaMessages
    Card(shape = RoundedCornerShape(22.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("课程内追问", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            messages.forEachIndexed { index, _ ->
                Text(messages[index], style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun PlanCard(stage: CrossDeviceTaskStage, direction: String) {
    val steps = remember {
        mutableStateListOf(
            "1. 解析任务目标并生成三步计划",
            "2. 需要用户在手机端批准后，电脑端开始执行",
            "3. 执行中允许继续聊天并中途改方向"
        )
    }
    Card(shape = RoundedCornerShape(22.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("执行计划", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            steps.forEach { Text(it, style = MaterialTheme.typography.bodyMedium) }
            if (stage >= CrossDeviceTaskStage.REDIRECTED) {
                Text("当前已改方向：$direction", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun DesktopRunningCard(progress: Float) {
    val transition = rememberInfiniteTransition(label = "desktopRunning")
    val drift = transition.animateFloat(
        initialValue = 0f,
        targetValue = 0.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "desktopProgressDrift"
    )
    val statusAlpha = transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "desktopStatusAlpha"
    )
    val animatedProgress = animateFloatAsState(
        targetValue = (progress + drift.value).coerceAtMost(0.98f),
        animationSpec = tween(350),
        label = "desktopProgress"
    )
    Card(shape = RoundedCornerShape(22.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Computer, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("电脑端正在执行", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.width(8.dp))
                AnimatedStatusDots(alpha = statusAlpha.value)
            }
            AnimatedContent(targetState = desktopCaption(animatedProgress.value), label = "desktopCaption") { caption ->
                Text(caption, style = MaterialTheme.typography.bodyMedium)
            }
            LinearProgressIndicator(progress = animatedProgress.value, modifier = Modifier.fillMaxWidth().height(8.dp))
        }
    }
}

@Composable
private fun AnimatedStatusDots(alpha: Float) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(3) { index ->
            val dotAlpha = (alpha - index * 0.18f).coerceIn(0.18f, 1f)
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .alpha(dotAlpha)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
        }
    }
}

private fun processingCaption(progress: Float): String = when {
    progress < 0.4f -> "正在抽取章节结构和关键词。"
    progress < 0.6f -> "正在整理笔记层级和重点卡片。"
    else -> "正在生成可追问的课程内容。"
}

private fun desktopCaption(progress: Float): String = when {
    progress < 0.35f -> "任务已发到电脑端，正在准备执行环境。"
    progress < 0.7f -> "电脑端正在持续执行，你可以在手机上继续聊天。"
    else -> "执行已进入后半段，随时可以改方向或追问下一步。"
}

@Composable
private fun PhoneChatCard() {
    val messages = LearningDemoState.taskChatMessages
    Card(shape = RoundedCornerShape(22.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("手机继续聊天", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            messages.forEach { Text(it, style = MaterialTheme.typography.bodyMedium) }
        }
    }
}

@Composable
private fun DemoActions(
    primary: String,
    onPrimary: () -> Unit,
    secondary: String,
    onSecondary: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(onClick = onPrimary, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
            Text(primary)
        }
        OutlinedButton(onClick = onSecondary) {
            Text(secondary)
        }
    }
}

private fun reviewStageLabel(stage: ReviewDemoStage): String = when (stage) {
    ReviewDemoStage.START -> "开始"
    ReviewDemoStage.ANSWERED_WRONG -> "已答错"
    ReviewDemoStage.PROFILE_UPDATED -> "画像已更新"
    ReviewDemoStage.REMINDER_READY -> "提醒待触发"
    ReviewDemoStage.RESUMED -> "已续上"
}

private fun reviewStageDescription(stage: ReviewDemoStage, next: String): String = when (stage) {
    ReviewDemoStage.START -> "先完成今天这轮做题。答错后会立刻看到错卡数量和掌握度变化。"
    ReviewDemoStage.ANSWERED_WRONG -> "已经记录错题，不停在这里，下一步要体现画像变化。"
    ReviewDemoStage.PROFILE_UPDATED -> "画像已根据错题更新，下一步要在晚些时候触发提醒。"
    ReviewDemoStage.REMINDER_READY -> "提醒规则已满足：$next"
    ReviewDemoStage.RESUMED -> "已经从提醒续上，并完成一轮回补。"
}

private fun nextReviewActionLabel(stage: ReviewDemoStage): String = when (stage) {
    ReviewDemoStage.START -> "提交本题"
    ReviewDemoStage.ANSWERED_WRONG -> "更新学习画像"
    ReviewDemoStage.PROFILE_UPDATED -> "进入提醒时段"
    ReviewDemoStage.REMINDER_READY -> "继续上次复习"
    ReviewDemoStage.RESUMED -> "已完成"
}

private fun courseStageLabel(stage: CourseDemoStage): String = when (stage) {
    CourseDemoStage.EMPTY -> "待导入"
    CourseDemoStage.IMPORTED -> "已导入"
    CourseDemoStage.PROCESSING -> "处理中"
    CourseDemoStage.GENERATED -> "已生成"
    CourseDemoStage.QA -> "可追问"
}

private fun nextCourseActionLabel(stage: CourseDemoStage): String = when (stage) {
    CourseDemoStage.EMPTY -> "导入资料"
    CourseDemoStage.IMPORTED -> "开始整理"
    CourseDemoStage.PROCESSING -> "生成结果"
    CourseDemoStage.GENERATED -> "课程内追问"
    CourseDemoStage.QA -> "已完成"
}

private fun taskStageLabel(stage: CrossDeviceTaskStage): String = when (stage) {
    CrossDeviceTaskStage.DRAFT -> "发起任务"
    CrossDeviceTaskStage.PLAN_READY -> "待批准"
    CrossDeviceTaskStage.APPROVED -> "已批准"
    CrossDeviceTaskStage.RUNNING_ON_DESKTOP -> "电脑执行中"
    CrossDeviceTaskStage.CHATTING_ON_PHONE -> "手机继续聊天"
    CrossDeviceTaskStage.REDIRECTED -> "已改方向"
}

private fun nextTaskActionLabel(stage: CrossDeviceTaskStage): String = when (stage) {
    CrossDeviceTaskStage.DRAFT -> "生成计划"
    CrossDeviceTaskStage.PLAN_READY -> "批准执行"
    CrossDeviceTaskStage.APPROVED -> "电脑开始跑"
    CrossDeviceTaskStage.RUNNING_ON_DESKTOP -> "手机继续聊天"
    CrossDeviceTaskStage.CHATTING_ON_PHONE -> "中途改方向"
    CrossDeviceTaskStage.REDIRECTED -> "已完成"
}
