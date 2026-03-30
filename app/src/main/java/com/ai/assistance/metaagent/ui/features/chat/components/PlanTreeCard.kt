package com.ai.assistance.metaagent.ui.features.chat.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ai.assistance.metaagent.core.plan.model.PlanNode
import com.ai.assistance.metaagent.core.plan.model.PlanNodeAdapter
import com.ai.assistance.metaagent.core.plan.model.PlanNodeStatus
import com.ai.assistance.metaagent.core.plan.model.TaskSession
import com.ai.assistance.metaagent.core.plan.model.TaskSessionStatus

@Composable
fun PlanTreeCard(
    taskSession: TaskSession,
    onApprove: () -> Unit = {},
    onReject: (String) -> Unit = {},
    onPause: () -> Unit = {},
    onResume: () -> Unit = {},
    onCancel: () -> Unit = {},
    onConfirmBlocked: () -> Unit = {},
    onReplyBlocked: (String) -> Unit = {},
    onNodeTap: (PlanNode) -> Unit = {},
    onOpenDetails: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(true) }
    var showBlockedReplyDialog by remember { mutableStateOf(false) }
    var blockedReplyText by remember { mutableStateOf("") }
    val blockedNode = remember(taskSession) {
        taskSession.planNodes.firstOrNull { it.status == PlanNodeStatus.BLOCKED }
    }
    val summaryMode = remember(taskSession.status) {
        taskSession.status !in setOf(
            TaskSessionStatus.DRAFT,
            TaskSessionStatus.PLANNED,
            TaskSessionStatus.AWAITING_APPROVAL
        )
    }
    val displayedNodes = remember(taskSession, summaryMode) {
        if (summaryMode) taskSession.recentExecutionChain(maxNodes = 4) else taskSession.planNodes
    }
    val latestSummary = remember(taskSession) {
        taskSession.pcLatestSummary.ifBlank {
            taskSession.pcLatestArtifactSummary.ifBlank {
                taskSession.events.lastOrNull { it.message.isNotBlank() }?.message.orEmpty()
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                TaskStatusIcon(taskSession.status)
                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "任务编排",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = taskSession.goal,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (taskSession.planNodes.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "${taskSession.completedNodeCount}/${taskSession.planNodes.size}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                }

                TextButton(
                    onClick = onOpenDetails,
                    enabled = taskSession.planNodes.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountTree,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(2.dp))
                    Text("详情")
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "折叠" else "展开",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (taskSession.planNodes.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                SimpleLinearProgressIndicator(
                    progress = taskSession.overallProgress,
                    modifier = Modifier.fillMaxWidth(),
                    color = when (taskSession.status) {
                        TaskSessionStatus.COMPLETED -> MaterialTheme.colorScheme.primary
                        TaskSessionStatus.FAILED -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    if (summaryMode && taskSession.planNodes.size > displayedNodes.size) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f),
                            modifier = Modifier.padding(bottom = 10.dp)
                        ) {
                            Text(
                                text = "运行中仅展示最近 ${displayedNodes.size} 个关键节点，完整树与分支请进入详情页查看。",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                            )
                        }
                    }

                    displayedNodes.forEachIndexed { visibleIndex, node ->
                        val originalIndex = taskSession.planNodes.indexOfFirst { it.id == node.id }
                            .takeIf { it >= 0 } ?: visibleIndex
                        PlanNodeRow(
                            node = node,
                            index = originalIndex,
                            onTap = { onNodeTap(node) }
                        )
                        if (visibleIndex < displayedNodes.lastIndex) {
                            Box(
                                modifier = Modifier
                                    .padding(start = 15.dp)
                                    .width(2.dp)
                                    .height(4.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant)
                            )
                        }
                    }

                    if (latestSummary.isNotBlank()) {
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = if (taskSession.hasActivePcProjection) "PC 侧摘要" else "最新进展",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = latestSummary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (taskSession.pcActiveWorker.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SummaryBadge("Worker ${taskSession.pcActiveWorker}")
                                taskSession.pcActiveWorkerProfile.takeIf { it.isNotBlank() }?.let {
                                    SummaryBadge("Profile $it")
                                }
                                if (taskSession.pcActiveWorkerCanInterrupt) {
                                    SummaryBadge("可中断")
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            PlanActionButtons(
                taskSession = taskSession,
                status = taskSession.status,
                onApprove = onApprove,
                onReject = onReject,
                onPause = onPause,
                onResume = onResume,
                onCancel = onCancel,
                onConfirmBlocked = onConfirmBlocked,
                onEditBlocked = {
                    blockedReplyText = blockedNode?.detail ?: ""
                    showBlockedReplyDialog = true
                }
            )
        }
    }

    if (showBlockedReplyDialog) {
        AlertDialog(
            onDismissRequest = { showBlockedReplyDialog = false },
            title = { Text("修改后继续") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    blockedNode?.let { node ->
                        Text(
                            text = "节点：${node.title}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedTextField(
                        value = blockedReplyText,
                        onValueChange = { blockedReplyText = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 6,
                        placeholder = { Text("输入补充说明或修改后的发送内容") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onReplyBlocked(blockedReplyText.trim())
                        showBlockedReplyDialog = false
                    },
                    enabled = blockedReplyText.isNotBlank()
                ) {
                    Text("继续执行")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBlockedReplyDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

private fun TaskSession.recentExecutionChain(maxNodes: Int): List<PlanNode> {
    if (planNodes.size <= maxNodes) return planNodes

    val anchorIndex = activeNodeId
        ?.let { activeId -> planNodes.indexOfFirst { it.id == activeId } }
        ?.takeIf { it >= 0 }
        ?: planNodes.indexOfLast {
            it.status == PlanNodeStatus.RUNNING ||
                it.status == PlanNodeStatus.BLOCKED ||
                it.status == PlanNodeStatus.FAILED ||
                it.status == PlanNodeStatus.DONE
        }.takeIf { it >= 0 }
        ?: 0

    val before = 1
    val after = maxNodes - before - 1
    var start = (anchorIndex - before).coerceAtLeast(0)
    var end = (anchorIndex + after).coerceAtMost(planNodes.lastIndex)

    if (end - start + 1 < maxNodes) {
        start = (start - (maxNodes - (end - start + 1))).coerceAtLeast(0)
    }
    if (end - start + 1 < maxNodes) {
        end = (end + (maxNodes - (end - start + 1))).coerceAtMost(planNodes.lastIndex)
    }

    return planNodes.subList(start, end + 1)
}

@Composable
private fun SummaryBadge(text: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun PlanNodeRow(
    node: PlanNode,
    index: Int,
    onTap: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onTap)
            .padding(vertical = 6.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NodeStatusDot(status = node.status)
        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${index + 1}. ${node.title}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (node.status == PlanNodeStatus.RUNNING) FontWeight.SemiBold else FontWeight.Normal,
                color = when (node.status) {
                    PlanNodeStatus.CANCELLED,
                    PlanNodeStatus.SKIPPED -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )

            val detailText = when {
                node.status == PlanNodeStatus.DONE && node.resultSummary.isNotBlank() -> node.resultSummary
                node.workerSummary.isNotBlank() -> node.workerSummary
                node.detail.isNotBlank() -> node.detail
                else -> node.explainToUser
            }
            if (detailText.isNotBlank()) {
                Text(
                    text = detailText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (node.status == PlanNodeStatus.RUNNING && node.progress > 0f) {
                Spacer(Modifier.height(4.dp))
                SimpleLinearProgressIndicator(
                    progress = node.progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                )
            }
        }

        AdapterBadge(adapter = node.adapter)
    }
}

@Composable
private fun NodeStatusDot(status: PlanNodeStatus) {
    val color by animateColorAsState(
        targetValue = when (status) {
            PlanNodeStatus.PENDING -> MaterialTheme.colorScheme.outlineVariant
            PlanNodeStatus.RUNNING -> MaterialTheme.colorScheme.primary
            PlanNodeStatus.DONE -> MaterialTheme.colorScheme.primary
            PlanNodeStatus.FAILED -> MaterialTheme.colorScheme.error
            PlanNodeStatus.BLOCKED -> MaterialTheme.colorScheme.tertiary
            PlanNodeStatus.SKIPPED,
            PlanNodeStatus.CANCELLED -> MaterialTheme.colorScheme.outline
        },
        label = "nodeStatusColor"
    )

    val pulseAnim = rememberInfiniteTransition(label = "nodePulse")
    val pulseScale by pulseAnim.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "nodePulseScale"
    )

    Box(
        modifier = Modifier
            .size(12.dp)
            .then(if (status == PlanNodeStatus.RUNNING) Modifier.scale(pulseScale) else Modifier)
            .background(color = color, shape = CircleShape)
    ) {
        if (status == PlanNodeStatus.DONE) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .size(10.dp)
                    .align(Alignment.Center)
            )
        }
    }
}

@Composable
private fun AdapterBadge(adapter: PlanNodeAdapter) {
    val (icon, label) = when (adapter) {
        PlanNodeAdapter.TOOL -> Icons.Outlined.Settings to "Tool"
        PlanNodeAdapter.CLAUDE -> Icons.Outlined.Computer to "PC"
        PlanNodeAdapter.CLI -> Icons.Outlined.Terminal to "CLI"
        PlanNodeAdapter.ANDROID -> Icons.Outlined.PhoneAndroid to "UI 自动化"
        PlanNodeAdapter.LOCAL_RUNNER -> Icons.Outlined.Settings to "Runner"
        PlanNodeAdapter.PC -> Icons.Outlined.Computer to "PC"
        PlanNodeAdapter.CHAT -> Icons.Outlined.Chat to "Chat"
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.padding(start = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(12.dp)
            )
            Spacer(Modifier.width(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun TaskStatusIcon(status: TaskSessionStatus) {
    val (icon, tint) = when (status) {
        TaskSessionStatus.DRAFT -> Icons.Default.Edit to MaterialTheme.colorScheme.onSurfaceVariant
        TaskSessionStatus.PLANNED,
        TaskSessionStatus.AWAITING_APPROVAL -> Icons.Outlined.Checklist to MaterialTheme.colorScheme.tertiary
        TaskSessionStatus.RUNNING -> Icons.Default.PlayArrow to MaterialTheme.colorScheme.primary
        TaskSessionStatus.PAUSED -> Icons.Default.Pause to MaterialTheme.colorScheme.tertiary
        TaskSessionStatus.WAITING_USER -> Icons.Outlined.HelpOutline to MaterialTheme.colorScheme.tertiary
        TaskSessionStatus.COMPLETED -> Icons.Default.CheckCircle to MaterialTheme.colorScheme.primary
        TaskSessionStatus.FAILED -> Icons.Default.Error to MaterialTheme.colorScheme.error
        TaskSessionStatus.CANCELLED -> Icons.Default.Cancel to MaterialTheme.colorScheme.outline
    }

    Icon(
        imageVector = icon,
        contentDescription = status.name,
        tint = tint,
        modifier = Modifier.size(24.dp)
    )
}

@Composable
private fun PlanActionButtons(
    taskSession: TaskSession,
    status: TaskSessionStatus,
    onApprove: () -> Unit,
    onReject: (String) -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    onConfirmBlocked: () -> Unit,
    onEditBlocked: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (status) {
            TaskSessionStatus.AWAITING_APPROVAL -> {
                FilledTonalButton(
                    onClick = onApprove,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("批准执行", fontSize = 13.sp)
                }
                OutlinedButton(
                    onClick = { onReject("") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("修改计划", fontSize = 13.sp)
                }
            }

            TaskSessionStatus.WAITING_USER -> {
                FilledTonalButton(
                    onClick = onConfirmBlocked,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("确认", fontSize = 13.sp)
                }
                OutlinedButton(
                    onClick = onEditBlocked,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("修改", fontSize = 13.sp)
                }
                OutlinedButton(
                    onClick = onCancel,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("取消", fontSize = 13.sp)
                }
            }

            TaskSessionStatus.RUNNING -> {
                OutlinedButton(
                    onClick = onPause,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("暂停", fontSize = 13.sp)
                }
                OutlinedButton(
                    onClick = onCancel,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("取消", fontSize = 13.sp)
                }
            }

            TaskSessionStatus.PAUSED -> {
                FilledTonalButton(
                    onClick = onResume,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("继续", fontSize = 13.sp)
                }
                OutlinedButton(
                    onClick = onCancel,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("取消", fontSize = 13.sp)
                }
            }

            else -> Unit
        }
    }
}
