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
import com.ai.assistance.metaagent.ui.features.home.data.ReviewChoice
import com.ai.assistance.metaagent.ui.features.home.data.ReviewDemoStage
import com.ai.assistance.metaagent.ui.features.home.data.ReviewEvent
import com.ai.assistance.metaagent.ui.features.home.data.ReviewQuestion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewJourneyDemoScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state = LearningDemoState
    val canWorkOnQuestion = when (state.reviewStage) {
        ReviewDemoStage.START -> true
        ReviewDemoStage.PROFILE_UPDATED -> true
        ReviewDemoStage.RESUMED -> !state.reviewSessionCompleted
        ReviewDemoStage.ANSWERED_WRONG,
        ReviewDemoStage.REMINDER_READY -> false
    }
    val canSubmitCurrentQuestion = canWorkOnQuestion &&
        !state.reviewAnswered &&
        state.reviewSelectedChoiceId != null
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
                    title = "一题一推进度",
                    subtitle = "先做题，再给反馈和下一次推荐。中途停下，回来时也能从这组题接着走。",
                    accent = Color(0xFF3467EB),
                    icon = Icons.Default.School
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricChip("待过卡片", "${state.wrongCardsToday} 张")
                    MetricChip("掌握度", "${(state.mastery * 100).toInt()}%")
                    MetricChip("当前状态", reviewStageLabel(state.reviewStage))
                }
            }
            item {
                JourneyCard(
                    title = "本轮进度",
                    body = reviewStageDescription(state.reviewStage, state.nextRecommendation)
                )
            }
            if (canWorkOnQuestion) {
                item {
                    ReviewQuestionCard(
                        question = state.currentReviewQuestion,
                        questionNumber = state.reviewQuestionIndex + 1,
                        mastery = state.mastery,
                        selectedChoiceId = state.reviewSelectedChoiceId,
                        answered = state.reviewAnswered,
                        answerCorrect = state.reviewLastAnswerCorrect == true,
                        onSelectChoice = state::selectReviewChoice
                    )
                }
            }
            if (state.reviewAnswered || state.reviewSessionCompleted) {
                item {
                    ReviewFeedbackCard(
                        answerCorrect = state.reviewLastAnswerCorrect == true,
                        masteryDelta = state.reviewLastMasteryDelta,
                        explanation = state.currentReviewQuestion.explanation,
                        summary = state.profileSummary,
                        nextRecommendation = state.nextRecommendation,
                        sessionCompleted = state.reviewSessionCompleted
                    )
                }
            }
            item {
                ProfileCard(state.profileSummary)
            }
            item {
                ReviewNextStepCard(
                    title = if (state.reviewReminderVisible) "稍后继续" else "下次推荐",
                    body = if (state.reviewReminderVisible) state.reviewResumeLabel else state.nextRecommendation,
                    caption = if (state.reviewSessionCompleted) {
                        "这轮已经收尾，下次打开会按这条推荐继续。"
                    } else {
                        "系统会根据这轮表现调整下一题和下一次出现的时机。"
                    }
                )
            }
            item {
                AnimatedVisibility(
                    visible = state.reviewReminderVisible,
                    enter = fadeIn(tween(350)) + expandVertically(tween(350)),
                    exit = fadeOut(tween(220))
                ) {
                    ReminderCard(state.reviewResumeLabel)
                }
            }
            if (state.reviewEvents.isNotEmpty()) {
                item {
                    ReviewEventCard(state.reviewEvents)
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    when {
                        state.reviewStage == ReviewDemoStage.REMINDER_READY -> {
                            Button(
                                onClick = { state.resumeReviewFromReminder() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("从提醒继续")
                            }
                        }

                        state.reviewStage == ReviewDemoStage.ANSWERED_WRONG && state.reviewAnswered -> {
                            Button(
                                onClick = { state.continueReviewWithFollowUp() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text(if (state.reviewLastAnswerCorrect == true) "继续下一题" else "补 1 题同类题")
                            }
                        }

                        canSubmitCurrentQuestion -> {
                            Button(
                                onClick = { state.submitReviewAnswer() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text(if (state.reviewStage == ReviewDemoStage.RESUMED) "提交这题" else "提交答案")
                            }
                        }

                        state.reviewSessionCompleted -> {
                            Button(
                                onClick = { state.resetReviewDemo() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("再来一轮")
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (state.reviewStage == ReviewDemoStage.PROFILE_UPDATED && !state.reviewAnswered) {
                            OutlinedButton(onClick = { state.scheduleReviewReminder() }) {
                                Text("稍后继续")
                            }
                        }
                        OutlinedButton(onClick = { state.resetReviewDemo() }) {
                            Text("重新开始")
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
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
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricChip("课程", "计算机网络")
                    MetricChip("资料", "PDF + 截图")
                    MetricChip("状态", courseStageLabel(state.courseStage))
                }
            }
            item {
                Card(shape = RoundedCornerShape(22.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("已导入资料", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            when (state.courseStage) {
                                CourseDemoStage.EMPTY -> "点击下方按钮导入课程资料，支持 PDF、Word、图片等格式。"
                                else -> "已导入：${state.importedMaterialName}"
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (state.courseStage == CourseDemoStage.EMPTY) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable { state.advanceCourseDemo() }
                            ) {
                                Text(
                                    "导入资料",
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }
            item {
                AnimatedVisibility(
                    visible = state.courseStage >= CourseDemoStage.PROCESSING,
                    enter = fadeIn(tween(350)) + expandVertically(tween(350)),
                    exit = fadeOut(tween(220))
                ) {
                    Card(shape = RoundedCornerShape(22.dp)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("正在整理", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.width(8.dp))
                                if (state.courseStage == CourseDemoStage.PROCESSING) {
                                    val transition = rememberInfiniteTransition(label = "proc")
                                    val alpha = transition.animateFloat(0.35f, 1f, infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "a")
                                    AnimatedStatusDots(alpha = alpha.value)
                                } else {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF0F9D7A), modifier = Modifier.size(18.dp))
                                }
                            }
                            if (state.courseStage == CourseDemoStage.PROCESSING) {
                                val transition = rememberInfiniteTransition(label = "procProg")
                                val prog = transition.animateFloat(0.24f, 0.82f, infiniteRepeatable(tween(1200), RepeatMode.Reverse), label = "p")
                                Text(processingCaption(prog.value), style = MaterialTheme.typography.bodyMedium)
                                LinearProgressIndicator(progress = prog.value, modifier = Modifier.fillMaxWidth().height(8.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable { state.advanceCourseDemo() }
                                ) {
                                    Text(
                                        "生成笔记",
                                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            } else {
                                Text("整理完成，笔记已生成。", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
            item {
                AnimatedVisibility(
                    visible = state.courseStage >= CourseDemoStage.GENERATED,
                    enter = fadeIn(tween(350)) + expandVertically(tween(350)),
                    exit = fadeOut(tween(220))
                ) {
                    Card(shape = RoundedCornerShape(22.dp)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoStories, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("结构化笔记", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            }
                            Text(state.generatedNoteTitle, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                            Text("${state.generatedCardCount} 张复习卡片已生成，可用于课程内复习和追问。", style = MaterialTheme.typography.bodyMedium)
                            if (state.courseStage == CourseDemoStage.GENERATED) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable { state.advanceCourseDemo() }
                                ) {
                                    Text(
                                        "开始追问",
                                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }
                    }
                }
            }
            item {
                AnimatedVisibility(
                    visible = state.courseStage >= CourseDemoStage.QA,
                    enter = fadeIn(tween(350)) + expandVertically(tween(350)),
                    exit = fadeOut(tween(220))
                ) {
                    Card(shape = RoundedCornerShape(22.dp)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("课程内追问", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            LearningDemoState.courseQaMessages.forEach { msg ->
                                Text(msg, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
            item {
                if (state.courseStage == CourseDemoStage.QA) {
                    OutlinedButton(
                        onClick = { state.resetCourseDemo() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("重新整理")
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
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
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricChip("阶段", taskStageLabel(state.taskStage))
                    MetricChip("电脑进度", "${(state.desktopProgress * 100).toInt()}%")
                    MetricChip("方向", if (state.taskStage >= CrossDeviceTaskStage.REDIRECTED) "已调整" else "原计划")
                }
            }
            item {
                Card(shape = RoundedCornerShape(22.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("任务目标", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(state.taskGoal, style = MaterialTheme.typography.bodyMedium)
                        if (state.taskStage == CrossDeviceTaskStage.DRAFT) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable { state.advanceTaskDemo() }
                            ) {
                                Text("生成执行计划", modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }
            item {
                AnimatedVisibility(
                    visible = state.taskStage >= CrossDeviceTaskStage.PLAN_READY,
                    enter = fadeIn(tween(350)) + expandVertically(tween(350)),
                    exit = fadeOut(tween(220))
                ) {
                    Card(shape = RoundedCornerShape(22.dp)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("执行计划", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            listOf(
                                "1. 解析任务目标并生成三步计划",
                                "2. 用户在手机端批准后，电脑端开始执行",
                                "3. 执行中允许继续聊天并中途改方向"
                            ).forEach { Text(it, style = MaterialTheme.typography.bodyMedium) }
                            if (state.taskStage >= CrossDeviceTaskStage.REDIRECTED) {
                                Text("当前已改方向：${state.taskDirection}", color = MaterialTheme.colorScheme.primary)
                            }
                            if (state.taskStage == CrossDeviceTaskStage.PLAN_READY) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable { state.advanceTaskDemo() }
                                ) {
                                    Text("批准执行", modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }
                }
            }
            item {
                AnimatedVisibility(
                    visible = state.taskStage >= CrossDeviceTaskStage.RUNNING_ON_DESKTOP,
                    enter = fadeIn(tween(350)) + expandVertically(tween(350)),
                    exit = fadeOut(tween(220))
                ) {
                    Card(shape = RoundedCornerShape(22.dp)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Computer, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("电脑端执行中", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                if (state.taskStage == CrossDeviceTaskStage.RUNNING_ON_DESKTOP) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    val t = rememberInfiniteTransition(label = "dp")
                                    val a = t.animateFloat(0.35f, 1f, infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "da")
                                    AnimatedStatusDots(alpha = a.value)
                                }
                            }
                            val t2 = rememberInfiniteTransition(label = "dp2")
                            val drift = t2.animateFloat(0f, 0.06f, infiniteRepeatable(tween(1500), RepeatMode.Reverse), label = "dr")
                            val animProg = animateFloatAsState((state.desktopProgress + drift.value).coerceAtMost(0.98f), tween(350), label = "ap")
                            Text(desktopCaption(animProg.value), style = MaterialTheme.typography.bodyMedium)
                            LinearProgressIndicator(progress = animProg.value, modifier = Modifier.fillMaxWidth().height(8.dp))
                            if (state.taskStage == CrossDeviceTaskStage.RUNNING_ON_DESKTOP) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    modifier = Modifier.clickable { state.advanceTaskDemo() }
                                ) {
                                    Text("在手机上继续聊", modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }
                }
            }
            item {
                AnimatedVisibility(
                    visible = state.taskStage >= CrossDeviceTaskStage.CHATTING_ON_PHONE,
                    enter = fadeIn(tween(350)) + expandVertically(tween(350)),
                    exit = fadeOut(tween(220))
                ) {
                    Card(shape = RoundedCornerShape(22.dp)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("手机继续聊天", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            LearningDemoState.taskChatMessages.forEach { Text(it, style = MaterialTheme.typography.bodyMedium) }
                            if (state.taskStage == CrossDeviceTaskStage.CHATTING_ON_PHONE) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable { state.advanceTaskDemo() }
                                ) {
                                    Text("调整方向", modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }
                }
            }
            item {
                AnimatedVisibility(
                    visible = state.taskStage == CrossDeviceTaskStage.REDIRECTED,
                    enter = fadeIn(tween(350)) + expandVertically(tween(350)),
                    exit = fadeOut(tween(220))
                ) {
                    Card(shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF0F9D7A))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("方向已调整", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            }
                            Text("新方向：${state.taskDirection}", style = MaterialTheme.typography.bodyMedium)
                            Text("电脑端已收到新指令，继续执行中。", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            OutlinedButton(onClick = { state.resetTaskDemo() }, modifier = Modifier.fillMaxWidth()) {
                                Text("重新演示")
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
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

private fun reviewStageLabel(stage: ReviewDemoStage): String = when (stage) {
    ReviewDemoStage.START -> "开始"
    ReviewDemoStage.ANSWERED_WRONG -> "已答错"
    ReviewDemoStage.PROFILE_UPDATED -> "画像已更新"
    ReviewDemoStage.REMINDER_READY -> "提醒待触发"
    ReviewDemoStage.RESUMED -> "已续上"
}

private fun courseStageLabel(stage: CourseDemoStage): String = when (stage) {
    CourseDemoStage.EMPTY -> "待导入"
    CourseDemoStage.IMPORTED -> "已导入"
    CourseDemoStage.PROCESSING -> "处理中"
    CourseDemoStage.GENERATED -> "已生成"
    CourseDemoStage.QA -> "可追问"
}

private fun taskStageLabel(stage: CrossDeviceTaskStage): String = when (stage) {
    CrossDeviceTaskStage.DRAFT -> "发起任务"
    CrossDeviceTaskStage.PLAN_READY -> "待批准"
    CrossDeviceTaskStage.APPROVED -> "已批准"
    CrossDeviceTaskStage.RUNNING_ON_DESKTOP -> "电脑执行中"
    CrossDeviceTaskStage.CHATTING_ON_PHONE -> "手机继续聊天"
    CrossDeviceTaskStage.REDIRECTED -> "已改方向"
}

private fun reviewStageDescription(stage: ReviewDemoStage, nextRecommendation: String): String = when (stage) {
    ReviewDemoStage.START -> "开始新一轮复习，系统会根据你的答题情况调整后续推荐。"
    ReviewDemoStage.ANSWERED_WRONG -> "答错了几道题，掌握度有所下降，准备更新学习画像。"
    ReviewDemoStage.PROFILE_UPDATED -> "学习画像已更新，系统会根据新的画像调整复习策略。"
    ReviewDemoStage.REMINDER_READY -> "本轮复习已完成，系统会在合适的时间提醒你继续。"
    ReviewDemoStage.RESUMED -> "从提醒继续复习，接着上次的进度往下走。"
}

@Composable
private fun HeroCard(
    title: String,
    subtitle: String,
    accent: Color,
    icon: ImageVector
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = accent.copy(alpha = 0.12f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(accent.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun JourneyCard(title: String, body: String) {
    Card(shape = RoundedCornerShape(18.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReviewQuestionCard(
    question: ReviewQuestion,
    questionNumber: Int,
    mastery: Float,
    selectedChoiceId: String?,
    answered: Boolean,
    answerCorrect: Boolean,
    onSelectChoice: (String) -> Unit
) {
    Card(shape = RoundedCornerShape(22.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("第 $questionNumber 题", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text(question.prompt, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
            question.choices.forEach { choice ->
                val isSelected = choice.id == selectedChoiceId
                val bgColor = when {
                    !answered -> if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    choice.id == question.correctChoiceId -> Color(0xFF0F9D7A).copy(alpha = 0.2f)
                    isSelected && !answerCorrect -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = bgColor,
                    onClick = { if (!answered) onSelectChoice(choice.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(choice.text, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                        if (answered && choice.id == question.correctChoiceId) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF0F9D7A))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewFeedbackCard(
    answerCorrect: Boolean,
    masteryDelta: Float,
    explanation: String,
    summary: String,
    nextRecommendation: String,
    sessionCompleted: Boolean
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (answerCorrect) Color(0xFF0F9D7A).copy(alpha = 0.15f)
            else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (answerCorrect) Icons.Default.CheckCircle else Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = if (answerCorrect) Color(0xFF0F9D7A) else MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (answerCorrect) "回答正确" else "回答错误",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(explanation, style = MaterialTheme.typography.bodyMedium)
            if (!answerCorrect) {
                Text(
                    "掌握度变化：${if (masteryDelta >= 0) "+" else ""}${(masteryDelta * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (sessionCompleted) {
                Text("本轮复习已完成！", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun ProfileCard(summary: String) {
    Card(shape = RoundedCornerShape(18.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("学习画像", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(summary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ReviewNextStepCard(title: String, body: String, caption: String) {
    Card(shape = RoundedCornerShape(18.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(body, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(6.dp))
            Text(caption, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ReminderCard(label: String) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("提醒已设置", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(label, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun ReviewEventCard(events: List<ReviewEvent>) {
    Card(shape = RoundedCornerShape(18.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("复习记录", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            events.forEach { event ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(event.detail, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
