package com.ai.assistance.metaagent.ui.features.chat.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ai.assistance.metaagent.core.plan.model.PlanNode
import com.ai.assistance.metaagent.core.plan.model.PlanNodeAdapter
import com.ai.assistance.metaagent.core.plan.model.PlanNodeStatus
import com.ai.assistance.metaagent.core.plan.model.TaskEvent
import com.ai.assistance.metaagent.core.plan.model.TaskEventType
import com.ai.assistance.metaagent.core.plan.model.TaskSession
import com.ai.assistance.metaagent.ui.features.chat.model.TaskPlanGraphMapper
import com.ai.assistance.metaagent.ui.features.workflow.components.GridWorkflowCanvas
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class TaskPlanSheetMode {
    NODE_DETAIL,
    LOGS,
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskPlanDetailScreen(
    taskSession: TaskSession,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val graph = remember(taskSession) { TaskPlanGraphMapper.map(taskSession) }
    var selectedNodeId by rememberSaveable { mutableStateOf<String?>(null) }
    var sheetMode by rememberSaveable { mutableStateOf<TaskPlanSheetMode?>(null) }

    val selectedNode = remember(taskSession, selectedNodeId) {
        selectedNodeId?.let(taskSession::findNode)
    }

    if (sheetMode != null) {
        ModalBottomSheet(
            onDismissRequest = {
                sheetMode = null
                selectedNodeId = null
            }
        ) {
            when (sheetMode) {
                TaskPlanSheetMode.NODE_DETAIL -> {
                    selectedNode?.let { node ->
                        NodeDetailSheet(node = node)
                    }
                }

                TaskPlanSheetMode.LOGS -> {
                    EventLogSheet(taskSession = taskSession)
                }

                null -> Unit
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "任务编排详情",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = taskSession.goal,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { sheetMode = TaskPlanSheetMode.LOGS }) {
                        Icon(
                            imageVector = Icons.Default.ListAlt,
                            contentDescription = "查看日志"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AssistChip(
                            onClick = {},
                            enabled = false,
                            label = { Text("状态 ${taskSession.status.name}") }
                        )
                        if (taskSession.pcActiveWorker.isNotBlank()) {
                            AssistChip(
                                onClick = {},
                                enabled = false,
                                label = { Text("PC ${taskSession.pcActiveWorker}") }
                            )
                        }
                        if (taskSession.pcActiveWorkerProfile.isNotBlank()) {
                            AssistChip(
                                onClick = {},
                                enabled = false,
                                label = { Text("Profile ${taskSession.pcActiveWorkerProfile}") }
                            )
                        }
                        if (taskSession.pcActiveWorkerCanInterrupt) {
                            AssistChip(
                                onClick = {},
                                enabled = false,
                                label = { Text("可中断") }
                            )
                        }
                    }

                    val summary = taskSession.pcLatestSummary.ifBlank {
                        taskSession.resultSummary.ifBlank {
                            taskSession.events.lastOrNull { it.message.isNotBlank() }?.message.orEmpty()
                        }
                    }
                    if (summary.isNotBlank()) {
                        Text(
                            text = summary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                GridWorkflowCanvas(
                    nodes = graph.nodes,
                    connections = graph.connections,
                    nodeExecutionStates = graph.executionStates,
                    onNodePositionChanged = { _, _, _ -> },
                    onNodeLongPress = { nodeId ->
                        selectedNodeId = nodeId.takeIf { it != "task-root-${taskSession.taskId}" }
                        if (selectedNodeId != null) {
                            sheetMode = TaskPlanSheetMode.NODE_DETAIL
                        }
                    },
                    onNodeClick = { nodeId ->
                        selectedNodeId = nodeId.takeIf { it != "task-root-${taskSession.taskId}" }
                        if (selectedNodeId != null) {
                            sheetMode = TaskPlanSheetMode.NODE_DETAIL
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NodeDetailSheet(node: PlanNode) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = node.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DetailChip("状态 ${node.status.name}")
            DetailChip("适配器 ${node.adapter.name}")
            if (node.workerName.isNotBlank()) DetailChip("Worker ${node.workerName}")
            if (node.workerProfile.isNotBlank()) DetailChip("Profile ${node.workerProfile}")
            if (node.pcPhase.isNotBlank()) DetailChip("Phase ${node.pcPhase}")
            if (node.workerCanInterrupt) DetailChip("可中断")
        }

        DetailSection("节点说明", preferredText(node))

        if (node.goal.isNotBlank() && node.goal != preferredText(node)) {
            DetailSection("节点目标", node.goal)
        }

        if (node.dependsOn.isNotEmpty()) {
            DetailSection("依赖节点", node.dependsOn.joinToString(" -> "))
        }

        if (node.artifacts.isNotEmpty()) {
            DetailListSection("产物") {
                node.artifacts.forEach { artifact ->
                    Text(
                        text = artifact,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        if (node.sessionInfoSummary.isNotBlank()) {
            DetailSection("会话摘要", node.sessionInfoSummary)
        }
        if (node.mcpStatusSummary.isNotBlank()) {
            DetailSection("MCP 状态", node.mcpStatusSummary)
        }
        if (node.permissionSummary.isNotBlank()) {
            DetailSection("权限摘要", node.permissionSummary)
        }
        if (node.recentHookEvents.isNotEmpty()) {
            DetailListSection("最近 Hook") {
                node.recentHookEvents.forEach { event ->
                    Text(
                        text = "• $event",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun EventLogSheet(taskSession: TaskSession) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            text = "执行日志",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "这里显示的是安卓侧保留后的结构化任务日志，不包含底层原始噪声输出。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(taskSession.events.reversed(), key = { it.id }) { event ->
                EventLogCard(event = event)
            }
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun EventLogCard(event: TaskEvent) {
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = eventTypeLabel(event.type),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = timeFormat.format(Date(event.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (event.nodeId != null) {
                Text(
                    text = "节点 ${event.nodeId}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = event.message.ifBlank { event.type.name },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun DetailSection(title: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun DetailListSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Column(verticalArrangement = Arrangement.spacedBy(6.dp), content = content)
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun DetailChip(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.medium
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

private fun preferredText(node: PlanNode): String {
    return when {
        node.resultSummary.isNotBlank() -> node.resultSummary
        node.detail.isNotBlank() -> node.detail
        node.explainToUser.isNotBlank() -> node.explainToUser
        else -> node.goal
    }
}

private fun eventTypeLabel(type: TaskEventType): String {
    return when (type) {
        TaskEventType.PLAN_GENERATED -> "计划生成"
        TaskEventType.PLAN_APPROVED -> "计划批准"
        TaskEventType.PLAN_MODIFIED -> "计划修改"
        TaskEventType.PLAN_REPLANNED -> "重新规划"
        TaskEventType.NODE_STARTED -> "节点开始"
        TaskEventType.NODE_PROGRESS -> "节点进展"
        TaskEventType.NODE_COMPLETED -> "节点完成"
        TaskEventType.NODE_FAILED -> "节点失败"
        TaskEventType.NODE_BLOCKED -> "节点阻塞"
        TaskEventType.TASK_COMPLETED -> "任务完成"
        TaskEventType.TASK_FAILED -> "任务失败"
        TaskEventType.TASK_PAUSED -> "任务暂停"
        TaskEventType.TASK_RESUMED -> "任务恢复"
        TaskEventType.USER_INTERVENTION -> "用户干预"
        TaskEventType.DIALOGUE_REPLY -> "对话回复"
        TaskEventType.PC_SESSION_PHASE -> "PC 阶段"
        TaskEventType.PC_SESSION_SUMMARY -> "PC 摘要"
        TaskEventType.PC_SESSION_WORKER -> "PC Worker"
        TaskEventType.PC_SESSION_RUNTIME -> "PC 运行态"
        TaskEventType.PC_SESSION_ARTIFACT -> "PC 产物"
        TaskEventType.PC_SESSION_PERMISSION -> "PC 权限"
        TaskEventType.PC_SESSION_MCP_STATUS -> "PC MCP"
        TaskEventType.PC_SESSION_FOLLOWUP -> "PC 跟进"
        TaskEventType.PC_SESSION_SNAPSHOT -> "PC 快照"
        TaskEventType.PC_SESSION_AWAIT_USER -> "PC 等待用户"
        TaskEventType.CONNECTED -> "连接建立"
        TaskEventType.DISCONNECTED -> "连接断开"
    }
}
