package com.ai.assistance.metaagent.ui.features.chat.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ai.assistance.metaagent.core.plan.model.TaskEvent
import com.ai.assistance.metaagent.core.plan.model.TaskEventType
import com.ai.assistance.metaagent.core.plan.model.TaskSession
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PlanTreePanel(
    taskSession: TaskSession?,
    onCreatePlan: (String) -> Unit = {},
    onApprove: () -> Unit = {},
    onReject: (String) -> Unit = {},
    onPause: () -> Unit = {},
    onResume: () -> Unit = {},
    onCancel: () -> Unit = {},
    onClear: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (taskSession == null) {
        EmptyPlanState(
            onCreatePlan = onCreatePlan,
            modifier = modifier
        )
    } else {
        ActivePlanContent(
            taskSession = taskSession,
            onApprove = onApprove,
            onReject = onReject,
            onPause = onPause,
            onResume = onResume,
            onCancel = onCancel,
            onClear = onClear,
            modifier = modifier
        )
    }
}

@Composable
private fun EmptyPlanState(
    onCreatePlan: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var goalInput by rememberSaveable { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.AccountTree,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "创建一个可执行任务",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "输入一个需要多步骤处理的目标，系统会自动拆解为节点，并优先调用本地能力进行执行。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
                OutlinedTextField(
                    value = goalInput,
                    onValueChange = { goalInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("任务目标") },
                    placeholder = { Text("例如：检查当前工作区文件，并帮我梳理 CNN 实验步骤") },
                    minLines = 3,
                    maxLines = 5
                )
                Button(
                    onClick = { onCreatePlan(goalInput.trim()) },
                    enabled = goalInput.isNotBlank(),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("生成计划")
                }
            }
        }
    }
}

@Composable
private fun ActivePlanContent(
    taskSession: TaskSession,
    onApprove: () -> Unit,
    onReject: (String) -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(taskSession.events.size) {
        if (taskSession.events.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccountTree,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.size(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "任务编排",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = taskSession.goal,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (taskSession.isTerminal) {
                    TextButton(onClick = onClear) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.size(4.dp))
                        Text("清除", fontSize = 13.sp)
                    }
                }
            }
        }

        PlanTreeCard(
            taskSession = taskSession,
            onApprove = onApprove,
            onReject = onReject,
            onPause = onPause,
            onResume = onResume,
            onCancel = onCancel,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
        )

        if (taskSession.events.isNotEmpty()) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.ListAlt,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.size(4.dp))
                Text(
                    text = "执行日志",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                reverseLayout = true
            ) {
                items(
                    items = taskSession.events.reversed(),
                    key = { it.id }
                ) { event ->
                    EventLogItem(event = event)
                }
            }
        } else {
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun EventLogItem(event: TaskEvent) {
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        val (icon, tint) = when (event.type) {
            TaskEventType.PLAN_GENERATED -> Icons.Default.AutoAwesome to MaterialTheme.colorScheme.primary
            TaskEventType.PLAN_APPROVED -> Icons.Default.CheckCircle to MaterialTheme.colorScheme.primary
            TaskEventType.PLAN_MODIFIED -> Icons.Default.Edit to MaterialTheme.colorScheme.tertiary
            TaskEventType.PLAN_REPLANNED -> Icons.Default.Refresh to MaterialTheme.colorScheme.tertiary
            TaskEventType.NODE_STARTED -> Icons.Default.PlayArrow to MaterialTheme.colorScheme.primary
            TaskEventType.NODE_PROGRESS -> Icons.Default.TrendingUp to MaterialTheme.colorScheme.primary
            TaskEventType.NODE_COMPLETED -> Icons.Default.CheckCircle to MaterialTheme.colorScheme.primary
            TaskEventType.NODE_FAILED -> Icons.Default.Error to MaterialTheme.colorScheme.error
            TaskEventType.NODE_BLOCKED -> Icons.Outlined.HourglassTop to MaterialTheme.colorScheme.tertiary
            TaskEventType.TASK_COMPLETED -> Icons.Default.DoneAll to MaterialTheme.colorScheme.primary
            TaskEventType.TASK_FAILED -> Icons.Default.ErrorOutline to MaterialTheme.colorScheme.error
            TaskEventType.TASK_PAUSED -> Icons.Default.Pause to MaterialTheme.colorScheme.tertiary
            TaskEventType.TASK_RESUMED -> Icons.Default.PlayArrow to MaterialTheme.colorScheme.primary
            TaskEventType.USER_INTERVENTION -> Icons.Default.Person to MaterialTheme.colorScheme.secondary
            TaskEventType.DIALOGUE_REPLY -> Icons.Outlined.Chat to MaterialTheme.colorScheme.secondary
            TaskEventType.CONNECTED -> Icons.Default.Wifi to MaterialTheme.colorScheme.primary
            TaskEventType.DISCONNECTED -> Icons.Default.WifiOff to MaterialTheme.colorScheme.error
        }

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier
                .size(14.dp)
                .padding(top = 2.dp)
        )

        Spacer(Modifier.size(6.dp))

        Text(
            text = timeFormat.format(Date(event.timestamp)),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            fontSize = 10.sp
        )

        Spacer(Modifier.size(6.dp))

        Text(
            text = event.message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
            fontSize = 12.sp
        )
    }
}
