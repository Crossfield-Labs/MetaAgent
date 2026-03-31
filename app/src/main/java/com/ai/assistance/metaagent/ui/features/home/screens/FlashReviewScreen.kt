package com.ai.assistance.metaagent.ui.features.home.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ai.assistance.metaagent.ui.features.home.data.CourseDemoStage
import com.ai.assistance.metaagent.ui.features.home.data.CrossDeviceTaskStage
import com.ai.assistance.metaagent.ui.features.home.data.LearningDemoState
import com.ai.assistance.metaagent.ui.features.home.data.ReviewDemoStage

@Composable
fun FlashReviewScreen(
    onGoBack: () -> Unit,
    onOpenReviewDemo: () -> Unit,
    onOpenCourseDemo: () -> Unit,
    onOpenTaskDemo: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
    ) {
        // 自带顶部栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onGoBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "返回", tint = MaterialTheme.colorScheme.onSurface)
            }
            Text(
                "闪卡复习",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                FlashReviewEntryCard(
                    title = "碎片复习",
                    subtitle = "答错卡片、画像变化、晚间提醒、续上进度",
                    icon = Icons.Default.School,
                    variant = FlashReviewCardVariant.Review,
                    onClick = onOpenReviewDemo
                )
            }
            item {
                FlashReviewEntryCard(
                    title = "课程整理",
                    subtitle = "导入资料、处理中、结构化笔记、课程追问",
                    icon = Icons.Default.Person,
                    variant = FlashReviewCardVariant.Course,
                    onClick = onOpenCourseDemo
                )
            }
            item {
                FlashReviewEntryCard(
                    title = "跨端任务",
                    subtitle = "看计划、批准、电脑执行、手机改方向",
                    icon = Icons.Default.Notifications,
                    variant = FlashReviewCardVariant.Task,
                    onClick = onOpenTaskDemo
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        FlashReviewOverviewBoard(
            onOpenReviewDemo = onOpenReviewDemo,
            onOpenCourseDemo = onOpenCourseDemo,
            onOpenTaskDemo = onOpenTaskDemo
        )
        
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun FlashReviewOverviewBoard(
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
            Text(flashReviewTodayOverviewText(), style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FlashReviewStatusPill("最近任务", flashReviewTaskSummary(state.taskStage))
                FlashReviewStatusPill("课程入口", flashReviewCourseSummary(state.courseStage))
                FlashReviewStatusPill("复习入口", flashReviewReviewSummary(state.reviewStage))
            }
            if (state.taskStage >= CrossDeviceTaskStage.RUNNING_ON_DESKTOP) {
                Text(
                    "进行中的任务状态：电脑端执行 ${(state.desktopProgress * 100).toInt()}%，手机端可继续聊天。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FlashReviewQuickJumpChip("去复习", onOpenReviewDemo)
                FlashReviewQuickJumpChip("去课程", onOpenCourseDemo)
                FlashReviewQuickJumpChip("去任务", onOpenTaskDemo)
            }
        }
    }
}

@Composable
private fun FlashReviewStatusPill(label: String, value: String) {
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
private fun FlashReviewQuickJumpChip(
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

private fun flashReviewTodayOverviewText(): String {
    val state = LearningDemoState
    return when {
        state.reviewStage == ReviewDemoStage.START -> "先补 1 轮错卡，再决定是否进入课程整理。"
        state.courseStage < CourseDemoStage.GENERATED -> "课程资料还没整理成笔记，建议先完成课程整理。"
        state.taskStage < CrossDeviceTaskStage.RUNNING_ON_DESKTOP -> "跨端任务还没批准执行，先过计划。"
        else -> "电脑端任务正在跑，手机端优先复习并准备课程追问。"
    }
}

private fun flashReviewTaskSummary(stage: CrossDeviceTaskStage): String = when (stage) {
    CrossDeviceTaskStage.DRAFT -> "待发起"
    CrossDeviceTaskStage.PLAN_READY -> "待批准"
    CrossDeviceTaskStage.APPROVED -> "已批准"
    CrossDeviceTaskStage.RUNNING_ON_DESKTOP -> "执行中"
    CrossDeviceTaskStage.CHATTING_ON_PHONE -> "可聊天续上"
    CrossDeviceTaskStage.REDIRECTED -> "已改方向"
}

private fun flashReviewCourseSummary(stage: CourseDemoStage): String = when (stage) {
    CourseDemoStage.EMPTY -> "待导入"
    CourseDemoStage.IMPORTED -> "待整理"
    CourseDemoStage.PROCESSING -> "处理中"
    CourseDemoStage.GENERATED -> "已出笔记"
    CourseDemoStage.QA -> "可追问"
}

private fun flashReviewReviewSummary(stage: ReviewDemoStage): String = when (stage) {
    ReviewDemoStage.START -> "待开始"
    ReviewDemoStage.ANSWERED_WRONG -> "已答错"
    ReviewDemoStage.PROFILE_UPDATED -> "画像已变"
    ReviewDemoStage.REMINDER_READY -> "待提醒"
    ReviewDemoStage.RESUMED -> "已续上"
}

private enum class FlashReviewCardVariant {
    Review,
    Course,
    Task
}

@Composable
private fun FlashReviewEntryCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    variant: FlashReviewCardVariant,
    onClick: () -> Unit
) {
    val state = LearningDemoState
    val progressTarget = when (variant) {
        FlashReviewCardVariant.Review -> when (state.reviewStage) {
            ReviewDemoStage.START -> 0.24f
            ReviewDemoStage.ANSWERED_WRONG -> 0.42f
            ReviewDemoStage.PROFILE_UPDATED -> 0.62f
            ReviewDemoStage.REMINDER_READY -> 0.82f
            ReviewDemoStage.RESUMED -> 0.94f
        }
        FlashReviewCardVariant.Course -> when (state.courseStage) {
            CourseDemoStage.EMPTY -> 0.12f
            CourseDemoStage.IMPORTED -> 0.32f
            CourseDemoStage.PROCESSING -> 0.56f
            CourseDemoStage.GENERATED -> 0.82f
            CourseDemoStage.QA -> 0.94f
        }
        FlashReviewCardVariant.Task -> when (state.taskStage) {
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
        FlashReviewCardVariant.Review -> when (state.reviewStage) {
            ReviewDemoStage.START -> "待开始"
            ReviewDemoStage.ANSWERED_WRONG -> "答错 4 张"
            ReviewDemoStage.PROFILE_UPDATED -> "画像已更新"
            ReviewDemoStage.REMINDER_READY -> "今晚提醒"
            ReviewDemoStage.RESUMED -> "已续上"
        }
        FlashReviewCardVariant.Course -> when (state.courseStage) {
            CourseDemoStage.EMPTY -> "待导入"
            CourseDemoStage.IMPORTED -> "资料已入列"
            CourseDemoStage.PROCESSING -> "正在整理"
            CourseDemoStage.GENERATED -> "笔记已生成"
            CourseDemoStage.QA -> "可继续追问"
        }
        FlashReviewCardVariant.Task -> when (state.taskStage) {
            CrossDeviceTaskStage.DRAFT -> "待发起"
            CrossDeviceTaskStage.PLAN_READY -> "计划已出"
            CrossDeviceTaskStage.APPROVED -> "已批准"
            CrossDeviceTaskStage.RUNNING_ON_DESKTOP -> "电脑执行中"
            CrossDeviceTaskStage.CHATTING_ON_PHONE -> "手机可续聊"
            CrossDeviceTaskStage.REDIRECTED -> "方向已调整"
        }
    }
    val detailText = when (variant) {
        FlashReviewCardVariant.Review -> when (state.reviewStage) {
            ReviewDemoStage.START -> "今天还有 ${state.wrongCardsToday} 张错卡待过。"
            ReviewDemoStage.ANSWERED_WRONG -> "掌握度回落到 ${(state.mastery * 100).toInt()}%，准备更新画像。"
            ReviewDemoStage.PROFILE_UPDATED -> state.profileSummary
            ReviewDemoStage.REMINDER_READY -> state.nextRecommendation
            ReviewDemoStage.RESUMED -> "复习已续上，剩余 ${state.wrongCardsToday} 张错卡。"
        }
        FlashReviewCardVariant.Course -> when (state.courseStage) {
            CourseDemoStage.EMPTY -> "导入本节资料后，会落到课程内笔记与卡片。"
            CourseDemoStage.IMPORTED -> "资料已接入，下一步进入结构化整理。"
            CourseDemoStage.PROCESSING -> "正在抽取章节结构、重点概念和可复习卡片。"
            CourseDemoStage.GENERATED -> "已产出 ${state.generatedCardCount} 张卡片和课程笔记。"
            CourseDemoStage.QA -> state.courseQaMessages.lastOrNull() ?: "课程内追问已就绪。"
        }
        FlashReviewCardVariant.Task -> when (state.taskStage) {
            CrossDeviceTaskStage.DRAFT -> "先发起任务，再查看执行计划。"
            CrossDeviceTaskStage.PLAN_READY -> "计划已生成，批准后切到电脑继续跑。"
            CrossDeviceTaskStage.APPROVED -> "任务已批准，正在准备接管电脑端执行。"
            CrossDeviceTaskStage.RUNNING_ON_DESKTOP -> "电脑端已推进到 ${(state.desktopProgress * 100).toInt()}%，手机端可继续对话。"
            CrossDeviceTaskStage.CHATTING_ON_PHONE -> "执行不中断，你可以边聊边追改方向。"
            CrossDeviceTaskStage.REDIRECTED -> state.taskDirection
        }
    }
    val isActive = when (variant) {
        FlashReviewCardVariant.Review -> state.reviewStage == ReviewDemoStage.REMINDER_READY
        FlashReviewCardVariant.Course -> state.courseStage == CourseDemoStage.PROCESSING
        FlashReviewCardVariant.Task -> state.taskStage == CrossDeviceTaskStage.RUNNING_ON_DESKTOP
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
