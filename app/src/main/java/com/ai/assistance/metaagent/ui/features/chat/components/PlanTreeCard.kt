package com.ai.assistance.metaagent.ui.features.chat.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import com.ai.assistance.metaagent.core.plan.model.PlanNode
import com.ai.assistance.metaagent.core.plan.model.PlanNodeStatus
import com.ai.assistance.metaagent.core.plan.model.TaskSession
import com.ai.assistance.metaagent.core.plan.model.TaskSessionStatus

/**
 * 编排树卡片 — Material You 风格
 *
 * 在聊天界面中展示编排树的完整状态，包括：
 * - 任务标题 + 总体进度
 * - 节点列表（带状态图标、进度条）
 * - 审批按钮（等待审批状态时）
 * - 操作按钮（暂停/恢复/取消）
 */
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
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(true) }
    var showBlockedReplyDialog by remember { mutableStateOf(false) }
    var blockedReplyText by remember { mutableStateOf("") }
    val blockedNode = remember(taskSession) {
        taskSession.planNodes.firstOrNull { it.status == PlanNodeStatus.BLOCKED }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // ---- 标题行 ----
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 状态图标
                TaskStatusIcon(taskSession.status)
                Spacer(Modifier.width(12.dp))

                // 标题
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

                // 进度标签
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

                // 展开/折叠
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "折叠" else "展开",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ---- 总体进度条 ----
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

            // ---- 节点列表 ----
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    taskSession.planNodes.forEachIndexed { index, node ->
                        PlanNodeRow(
                            node = node,
                            index = index,
                            onTap = { onNodeTap(node) }
                        )
                        if (index < taskSession.planNodes.lastIndex) {
                            // 节点之间的连线
                            Box(
                                modifier = Modifier
                                    .padding(start = 15.dp)
                                    .width(2.dp)
                                    .height(4.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant)
                            )
                        }
                    }

                    if (taskSession.events.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "执行日志",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(6.dp))
                        taskSession.events.takeLast(if (expanded) 8 else 3).forEach { event ->
                            Text(
                                text = "• ${event.message.ifBlank { event.type.name }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // ---- 操作按钮 ----
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
            title = {
                Text("修改后继续")
            },
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
                        placeholder = {
                            Text("输入补充说明或修改后的发送内容")
                        }
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
                TextButton(
                    onClick = { showBlockedReplyDialog = false }
                ) {
                    Text("取消")
                }
            }
        )
    }
}

/**
 * 单个节点行
 */
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
        // 状态圆点
        NodeStatusDot(status = node.status)
        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            // 节点标题
            Text(
                text = "${index + 1}. ${node.title}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (node.status == PlanNodeStatus.RUNNING) FontWeight.SemiBold else FontWeight.Normal,
                color = when (node.status) {
                    PlanNodeStatus.CANCELLED, PlanNodeStatus.SKIPPED ->
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )

            // 详情/说明
            val detailText = when {
                node.status == PlanNodeStatus.DONE && node.resultSummary.isNotBlank() -> node.resultSummary
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

            // 进度条（执行中时）
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

        // 适配器标签
        AdapterBadge(adapter = node.adapter)
    }
}

/**
 * 节点状态小圆点（带脉冲动画）
 */
@Composable
private fun NodeStatusDot(status: PlanNodeStatus) {
    val color by animateColorAsState(
        targetValue = when (status) {
            PlanNodeStatus.PENDING -> MaterialTheme.colorScheme.outlineVariant
            PlanNodeStatus.RUNNING -> MaterialTheme.colorScheme.primary
            PlanNodeStatus.DONE -> MaterialTheme.colorScheme.primary
            PlanNodeStatus.FAILED -> MaterialTheme.colorScheme.error
            PlanNodeStatus.BLOCKED -> MaterialTheme.colorScheme.tertiary
            PlanNodeStatus.SKIPPED, PlanNodeStatus.CANCELLED -> MaterialTheme.colorScheme.outline
        },
        label = "nodeStatusColor"
    )

    // RUNNING 状态脉冲动画
    val pulseAnim = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulseAnim.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = Modifier
            .size(12.dp)
            .then(
                if (status == PlanNodeStatus.RUNNING) Modifier.scale(pulseScale) else Modifier
            )
            .background(color = color, shape = CircleShape)
    ) {
        // DONE 状态显示对号
        if (status == PlanNodeStatus.DONE) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(10.dp).align(Alignment.Center)
            )
        }
    }
}

/**
 * 适配器小标签 — Material You
 */
@Composable
private fun AdapterBadge(adapter: com.ai.assistance.metaagent.core.plan.model.PlanNodeAdapter) {
    val (icon, label) = when (adapter) {
        com.ai.assistance.metaagent.core.plan.model.PlanNodeAdapter.TOOL ->
            Icons.Outlined.Settings to "Tool"
        com.ai.assistance.metaagent.core.plan.model.PlanNodeAdapter.CLAUDE ->
            Icons.Outlined.Computer to "PC"
        com.ai.assistance.metaagent.core.plan.model.PlanNodeAdapter.CLI ->
            Icons.Outlined.Terminal to "CLI"
        com.ai.assistance.metaagent.core.plan.model.PlanNodeAdapter.ANDROID ->
            Icons.Outlined.PhoneAndroid to "UI自动化"
        com.ai.assistance.metaagent.core.plan.model.PlanNodeAdapter.LOCAL_RUNNER ->
            Icons.Outlined.Settings to "Runner"
        com.ai.assistance.metaagent.core.plan.model.PlanNodeAdapter.PC ->
            Icons.Outlined.Computer to "PC"
        com.ai.assistance.metaagent.core.plan.model.PlanNodeAdapter.CHAT ->
            Icons.Outlined.Chat to "Chat"
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

/**
 * 任务状态图标 — Material Icons
 */
@Composable
private fun TaskStatusIcon(status: TaskSessionStatus) {
    val (icon, tint) = when (status) {
        TaskSessionStatus.DRAFT ->
            Icons.Outlined.Edit to MaterialTheme.colorScheme.onSurfaceVariant
        TaskSessionStatus.PLANNED, TaskSessionStatus.AWAITING_APPROVAL ->
            Icons.Outlined.Checklist to MaterialTheme.colorScheme.tertiary
        TaskSessionStatus.RUNNING ->
            Icons.Default.PlayArrow to MaterialTheme.colorScheme.primary
        TaskSessionStatus.PAUSED ->
            Icons.Default.Pause to MaterialTheme.colorScheme.tertiary
        TaskSessionStatus.WAITING_USER ->
            Icons.Outlined.HelpOutline to MaterialTheme.colorScheme.tertiary
        TaskSessionStatus.COMPLETED ->
            Icons.Default.CheckCircle to MaterialTheme.colorScheme.primary
        TaskSessionStatus.FAILED ->
            Icons.Default.Error to MaterialTheme.colorScheme.error
        TaskSessionStatus.CANCELLED ->
            Icons.Default.Cancel to MaterialTheme.colorScheme.outline
    }
    Icon(
        imageVector = icon,
        contentDescription = status.name,
        tint = tint,
        modifier = Modifier.size(24.dp)
    )
}

/**
 * 操作按钮区域 — Material You
 */
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
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
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
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
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
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("取消", fontSize = 13.sp)
                }
            }
            TaskSessionStatus.COMPLETED -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "任务已完成",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            TaskSessionStatus.FAILED -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "任务失败",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            else -> { /* DRAFT / PLANNED / CANCELLED — no buttons */ }
        }
    }
}
