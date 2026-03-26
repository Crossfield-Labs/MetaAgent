package com.ai.assistance.metaagent.ui.features.chat.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ai.assistance.metaagent.core.plan.model.TaskSession
import com.ai.assistance.metaagent.core.plan.model.TaskSessionStatus

/**
 * 任务进度条 — Material You 风格
 *
 * 悬浮在聊天界面顶部，显示当前任务的执行进度。
 * 点击可跳转到编排树详情。在有活跃任务时显示。
 */
@Composable
fun TaskProgressBar(
    taskSession: TaskSession?,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = taskSession != null && taskSession.isActive,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        taskSession ?: return@AnimatedVisibility

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 2.dp,
            shadowElevation = 1.dp
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    // 状态图标
                    val statusIcon = when (taskSession.status) {
                        TaskSessionStatus.RUNNING -> Icons.Default.Sync
                        TaskSessionStatus.PAUSED -> Icons.Default.Pause
                        TaskSessionStatus.WAITING_USER -> Icons.Outlined.HourglassTop
                        else -> Icons.Default.Task
                    }

                    // 脉冲圆点
                    val pulseTransition = rememberInfiniteTransition(label = "statusPulse")
                    val pulseScale by pulseTransition.animateFloat(
                        initialValue = 0.85f,
                        targetValue = 1.15f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, easing = EaseInOut),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "statusPulseScale"
                    )

                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier
                            .size(18.dp)
                            .then(
                                if (taskSession.status == TaskSessionStatus.RUNNING)
                                    Modifier.scale(pulseScale) else Modifier
                            )
                    )

                    Spacer(Modifier.width(8.dp))

                    // 任务信息
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (taskSession.status) {
                                TaskSessionStatus.RUNNING -> "后台执行中"
                                TaskSessionStatus.PAUSED -> "已暂停"
                                TaskSessionStatus.WAITING_USER -> "等待确认"
                                else -> "任务进行中"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        val currentStep = taskSession.activeNode
                        Text(
                            text = currentStep?.let {
                                "${it.title} · ${it.detail.ifBlank { "进行中…" }}"
                            } ?: taskSession.goal,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 11.sp
                        )
                    }

                    // 进度数字
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = "${taskSession.completedNodeCount}/${taskSession.planNodes.size}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(Modifier.width(4.dp))

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "查看详情",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // 底部进度条
                SimpleLinearProgressIndicator(
                    progress = taskSession.overallProgress,
                    modifier = Modifier.fillMaxWidth().height(2.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )
            }
        }
    }
}
