package com.ai.assistance.metaagent.ui.features.home.screens

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ai.assistance.metaagent.ui.features.home.data.CourseMaterialDraft
import com.ai.assistance.metaagent.ui.features.home.data.StudyModuleStore
import com.ai.assistance.metaagent.ui.features.home.data.StudyTask
import com.ai.assistance.metaagent.ui.features.home.data.StudyTaskNotification
import com.ai.assistance.metaagent.ui.features.home.data.StudyTaskStatus
import com.ai.assistance.metaagent.ui.features.home.data.TaskItem
import com.ai.assistance.metaagent.ui.features.home.data.TaskStatus
import com.ai.assistance.metaagent.ui.theme.TaskApproval
import com.ai.assistance.metaagent.ui.theme.TaskCompleted
import com.ai.assistance.metaagent.ui.theme.TaskRunning
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt

/**
 * 任务中心 Dashboard - 保持原版式，只补充真实可操作的任务创建与动态状态。
 */
@Composable
fun TaskCenterScreen(
    onTaskClick: (String) -> Unit,
    onCrossDeviceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    StudyModuleStore.ensureInitialized(context)

    val rawTasks = StudyModuleStore.tasks
    val tasks = rawTasks.map { it.toTaskItem() }
    val courseNames = StudyModuleStore.courses.map { it.name }
    val taskNotifications = StudyModuleStore.taskNotifications
    val unreadNotificationCount = taskNotifications.count { !it.isRead }

    val runningCount = tasks.count { it.status == TaskStatus.RUNNING }
    val completedCount = tasks.count { it.status == TaskStatus.COMPLETED }
    val approvalCount = tasks.count { it.status == TaskStatus.AWAITING_APPROVAL }
    val totalCount = tasks.size
    val completionRate = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    val tabs = listOf("进行中", "新任务", "已归档")
    var selectedTab by remember { mutableStateOf("进行中") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    var showStatsDetailDialog by remember { mutableStateOf(false) }

    val filteredTasks = when (selectedTab) {
        "进行中" -> tasks.filter { it.status == TaskStatus.RUNNING }
        "新任务" -> tasks.filter { it.status == TaskStatus.AWAITING_APPROVAL || it.status == TaskStatus.PAUSED }
        "已归档" -> tasks.filter { it.status == TaskStatus.COMPLETED || it.status == TaskStatus.FAILED }
        else -> tasks
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hello,",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "@MetaAgent!",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box {
                        IconButton(
                            onClick = {
                                showNotificationDialog = true
                                StudyModuleStore.markAllTaskNotificationsRead()
                            }
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = "通知",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (unreadNotificationCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.error),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (unreadNotificationCount > 9) "9+" else unreadNotificationCount.toString(),
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onError,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "M",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    StatisticsCard(
                        totalTasks = totalCount,
                        running = runningCount,
                        completed = completedCount,
                        approval = approvalCount,
                        onSeeAll = { showStatsDetailDialog = true }
                    )
                }
                item {
                    QuickActionCard(
                        title = "新建任务",
                        subtitle = "Copilot 共做",
                        bgColor = MaterialTheme.colorScheme.primaryContainer,
                        onClick = { showCreateDialog = true }
                    )
                }
                item {
                    CrossDeviceCard(onClick = onCrossDeviceClick)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEach { tab ->
                    val selected = selectedTab == tab
                    val count = when (tab) {
                        "进行中" -> runningCount
                        "新任务" -> approvalCount
                        "已归档" -> completedCount + tasks.count { it.status == TaskStatus.FAILED }
                        else -> 0
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(40.dp))
                            .then(
                                if (selected) Modifier.border(
                                    1.5.dp,
                                    MaterialTheme.colorScheme.onSurface,
                                    RoundedCornerShape(40.dp)
                                )
                                else Modifier.border(
                                    1.dp,
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                    RoundedCornerShape(40.dp)
                                )
                            )
                            .clickable { selectedTab = tab }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = tab,
                                fontSize = 13.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (count > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$count",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ShowChart,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Your Progress",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "更多",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${(completionRate * 100).toInt()}.${((completionRate * 1000) % 10).toInt()}%",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${completedCount} 个任务已完成",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            lineHeight = 16.sp
                        )
                        Text(
                            text = "Keep it up!",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(24.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.onSurface)
                        )
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(10.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f))
                        )
                    }
                }
            }
        }

        items(filteredTasks) { task ->
            TaskCard(task = task, onClick = { onTaskClick(task.id) })
        }
    }

    if (showCreateDialog) {
        CreateTaskDialog(
            courseNames = courseNames,
            onDismiss = { showCreateDialog = false },
            onConfirm = { payload ->
                StudyModuleStore.createTask(
                    title = payload.title,
                    courseName = payload.courseName,
                    summary = payload.summary,
                    expectedMinutes = payload.expectedMinutes,
                    detailContent = payload.detailContent,
                    stages = payload.stages,
                    promptHint = payload.promptHint,
                    materials = payload.materials
                )
                showCreateDialog = false
                selectedTab = "新任务"
                Toast.makeText(context, "任务已加入新任务列表", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showNotificationDialog) {
        TaskNotificationDialog(
            notifications = taskNotifications,
            onDismiss = { showNotificationDialog = false },
            onNotificationClick = { notice ->
                StudyModuleStore.markTaskNotificationRead(notice.id)
                showNotificationDialog = false
                if (notice.taskId.isNotBlank()) {
                    onTaskClick(notice.taskId)
                }
            }
        )
    }

    if (showStatsDetailDialog) {
        TaskStatisticsDetailDialog(
            tasks = rawTasks,
            onDismiss = { showStatsDetailDialog = false }
        )
    }
}

@Composable
private fun StatisticsCard(
    totalTasks: Int,
    running: Int,
    completed: Int,
    approval: Int,
    onSeeAll: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(165.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Statistics", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            Row(
                modifier = Modifier.clickable(onClick = onSeeAll),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "See all", fontSize = 12.sp)
                Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(14.dp))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "$totalTasks", fontSize = 32.sp, fontWeight = FontWeight.Normal, color = MaterialTheme.colorScheme.onSurface)
        Text(text = "个任务", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            MiniBar(label = "进行", value = running, total = totalTasks, color = TaskRunning)
            MiniBar(label = "完成", value = completed, total = totalTasks, color = TaskCompleted)
            MiniBar(label = "审批", value = approval, total = totalTasks, color = TaskApproval)
        }
    }
}

@Composable
private fun MiniBar(label: String, value: Int, total: Int, color: Color) {
    val ratio = if (total > 0) value.toFloat() / total else 0f
    Column(horizontalAlignment = Alignment.Start) {
        Text(text = label, fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
        Spacer(modifier = Modifier.height(2.dp))
        Box(modifier = Modifier.width(35.dp).height((40 * ratio).coerceAtLeast(4f).dp).clip(RoundedCornerShape(1.dp)).background(MaterialTheme.colorScheme.surfaceContainerHighest))
        Box(modifier = Modifier.width(35.dp).height(5.dp).background(color.copy(alpha = 0.3f)))
        Spacer(modifier = Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(8.dp), tint = MaterialTheme.colorScheme.onSurface)
            Text(text = "${(ratio * 100).toInt()}%", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    bgColor: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(145.dp)
            .height(165.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(24.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurface)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "See all", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
        Column {
            Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text(text = subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
        }
    }
}

@Composable
private fun CrossDeviceCard(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(170.dp)
            .height(165.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(24.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Devices, contentDescription = null, modifier = Modifier.size(16.dp))
            }
            Text(text = "See all", fontSize = 12.sp)
        }
        Column {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "📱", fontSize = 20.sp)
                Text(text = "→", fontSize = 18.sp)
                Text(text = "💻", fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "跨端执行", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSecondaryContainer)
            Text(text = "手机审批 · 电脑执行", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f))
        }
    }
}

@Composable
private fun TaskNotificationDialog(
    notifications: List<StudyTaskNotification>,
    onDismiss: () -> Unit,
    onNotificationClick: (StudyTaskNotification) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        },
        title = { Text("任务通知") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (notifications.isEmpty()) {
                    Text(
                        text = "暂无通知。任务完成后会自动推送到这里。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    notifications.forEach { notice ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (notice.isRead) MaterialTheme.colorScheme.surfaceContainerLow
                                    else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                                )
                                .clickable { onNotificationClick(notice) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = notice.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = notice.detail,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(horizontalAlignment = Alignment.End) {
                                if (!notice.isRead) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.error)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                                Text(
                                    text = notice.timeLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun TaskStatisticsDetailDialog(
    tasks: List<StudyTask>,
    onDismiss: () -> Unit
) {
    val total = tasks.size
    val safeTotal = total.coerceAtLeast(1)
    val queuedCount = tasks.count { it.status == StudyTaskStatus.QUEUED }
    val runningCount = tasks.count { it.status == StudyTaskStatus.RUNNING }
    val completedCount = tasks.count { it.status == StudyTaskStatus.COMPLETED }
    val failedCount = tasks.count { it.status == StudyTaskStatus.FAILED }
    val avgProgress = if (total > 0) tasks.map { it.progress }.average().roundToInt() else 0
    val avgExpectedMinutes = if (total > 0) tasks.map { it.expectedMinutes }.average().roundToInt() else 0
    val completionRate = if (total > 0) (completedCount * 100f / total).roundToInt() else 0

    val statusMetrics = listOf(
        TaskStatusMetric("待执行", queuedCount, TaskApproval),
        TaskStatusMetric("执行中", runningCount, TaskRunning),
        TaskStatusMetric("已完成", completedCount, TaskCompleted),
        TaskStatusMetric("失败", failedCount, Color(0xFFFF5252))
    )
    val courseDistribution = tasks
        .groupingBy { it.courseName.ifBlank { "未分类课程" } }
        .eachCount()
        .entries
        .sortedByDescending { it.value }
        .take(6)

    val durationBuckets = listOf(
        "≤ 30 分钟" to tasks.count { it.expectedMinutes <= 30 },
        "31 - 60 分钟" to tasks.count { it.expectedMinutes in 31..60 },
        "> 60 分钟" to tasks.count { it.expectedMinutes > 60 }
    )
    val progressBuckets = listOf(
        "0 - 30%" to tasks.count { it.progress <= 30 },
        "31 - 70%" to tasks.count { it.progress in 31..70 },
        "71 - 100%" to tasks.count { it.progress >= 71 }
    )
    val recentCompleted = tasks
        .filter { it.status == StudyTaskStatus.COMPLETED }
        .sortedByDescending { it.updatedAt }
        .take(5)

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        },
        title = { Text("详细统计") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 560.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatValueCard(
                        title = "任务总数",
                        value = total.toString(),
                        subtitle = "当前全部任务",
                        modifier = Modifier.weight(1f)
                    )
                    StatValueCard(
                        title = "完成率",
                        value = "$completionRate%",
                        subtitle = "完成 / 总任务",
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatValueCard(
                        title = "平均进度",
                        value = "$avgProgress%",
                        subtitle = "按任务进度均值",
                        modifier = Modifier.weight(1f)
                    )
                    StatValueCard(
                        title = "平均耗时",
                        value = "${avgExpectedMinutes}分钟",
                        subtitle = "期望耗时均值",
                        modifier = Modifier.weight(1f)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("任务状态分布", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusDonutChart(
                            metrics = statusMetrics,
                            total = safeTotal,
                            modifier = Modifier.size(120.dp)
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            statusMetrics.forEach { metric ->
                                StatusMetricRow(metric = metric, total = safeTotal)
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("课程任务占比（Top 6）", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    if (courseDistribution.isEmpty()) {
                        Text(
                            text = "暂无课程任务数据",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        val maxCourseCount = courseDistribution.maxOf { it.value }.coerceAtLeast(1)
                        courseDistribution.forEach { entry ->
                            val ratio = (entry.value.toFloat() / maxCourseCount).coerceIn(0f, 1f)
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(entry.key, style = MaterialTheme.typography.bodySmall)
                                    Text("${entry.value}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(ratio.coerceAtLeast(0.06f))
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                }
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("时长与进度分层", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    DistributionStackBar(
                        label = "任务时长分布",
                        buckets = durationBuckets,
                        colors = listOf(Color(0xFF66BB6A), Color(0xFFFFCA28), Color(0xFFFF7043))
                    )
                    DistributionStackBar(
                        label = "任务进度分布",
                        buckets = progressBuckets,
                        colors = listOf(Color(0xFF90A4AE), Color(0xFF42A5F5), Color(0xFF26A69A))
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("最近完成任务", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    if (recentCompleted.isEmpty()) {
                        Text(
                            text = "还没有完成任务。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        recentCompleted.forEach { task ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = task.updatedAt,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun StatValueCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun StatusDonutChart(
    metrics: List<TaskStatusMetric>,
    total: Int,
    modifier: Modifier = Modifier
) {
    val placeholderColor = MaterialTheme.colorScheme.surfaceVariant
    Canvas(modifier = modifier) {
        if (total <= 0) {
            drawArc(
                color = placeholderColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = size.minDimension * 0.18f, cap = StrokeCap.Round)
            )
            return@Canvas
        }
        val strokeWidth = size.minDimension * 0.18f
        var startAngle = -90f
        metrics.forEach { metric ->
            if (metric.count > 0) {
                val sweep = (metric.count.toFloat() / total.toFloat()) * 360f
                drawArc(
                    color = metric.color,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    size = Size(size.width, size.height)
                )
                startAngle += sweep
            }
        }
    }
}

@Composable
private fun StatusMetricRow(metric: TaskStatusMetric, total: Int) {
    val ratio = if (total > 0) metric.count.toFloat() / total else 0f
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(metric.color)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(metric.label, style = MaterialTheme.typography.bodySmall)
            }
            Text("${metric.count}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(ratio.coerceAtLeast(if (metric.count == 0) 0f else 0.06f))
                    .height(6.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(metric.color)
            )
        }
    }
}

@Composable
private fun DistributionStackBar(
    label: String,
    buckets: List<Pair<String, Int>>,
    colors: List<Color>
) {
    val total = buckets.sumOf { it.second }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
        if (total <= 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(10.dp))
            ) {
                buckets.forEachIndexed { index, bucket ->
                    val ratio = bucket.second.toFloat() / total.toFloat()
                    Box(
                        modifier = Modifier
                            .weight(ratio.coerceAtLeast(0.001f))
                            .fillMaxSize()
                            .background(colors.getOrElse(index) { MaterialTheme.colorScheme.primary })
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            buckets.forEachIndexed { index, bucket ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(colors.getOrElse(index) { MaterialTheme.colorScheme.primary })
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${bucket.first} ${bucket.second}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private data class TaskStatusMetric(
    val label: String,
    val count: Int,
    val color: Color
)

@Composable
private fun TaskCard(task: TaskItem, onClick: () -> Unit) {
    val borderColor = when (task.status) {
        TaskStatus.RUNNING -> TaskRunning
        TaskStatus.AWAITING_APPROVAL -> TaskApproval
        TaskStatus.COMPLETED -> TaskCompleted
        TaskStatus.PAUSED -> Color(0xFF9E9E9E)
        TaskStatus.FAILED -> Color(0xFFFF1744)
    }

    val statusIcon = when (task.status) {
        TaskStatus.RUNNING -> Icons.Default.PlayArrow
        TaskStatus.AWAITING_APPROVAL -> Icons.Default.Schedule
        TaskStatus.COMPLETED -> Icons.Default.Check
        TaskStatus.PAUSED -> Icons.Default.Warning
        TaskStatus.FAILED -> Icons.Default.Close
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (task.status == TaskStatus.COMPLETED)
                    MaterialTheme.colorScheme.surfaceContainerLow
                else MaterialTheme.colorScheme.surface
            )
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(if (task.status == TaskStatus.COMPLETED) 72.dp else 96.dp)
                .background(borderColor)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(statusIcon, contentDescription = null, modifier = Modifier.size(18.dp), tint = borderColor)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${task.status.label} · ${task.statusDetail}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (task.status == TaskStatus.RUNNING && task.progress > 0f) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { task.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = TaskRunning,
                    trackColor = TaskRunning.copy(alpha = 0.15f),
                    strokeCap = StrokeCap.Round
                )
            }

            if (task.isCrossDevice) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Devices, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "📱 → 💻 跨端执行",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (task.startTime.isNotEmpty()) {
                        Text(
                            text = " · ${task.startTime}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (task.deliverables.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "产物：${task.deliverables}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CreateTaskDialog(
    courseNames: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (TaskCreatePayload) -> Unit
) {
    val context = LocalContext.current
    val availableCourses = remember(courseNames) {
        if (courseNames.isEmpty()) listOf("通用任务") else courseNames.distinct()
    }
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var courseName by remember(availableCourses) { mutableStateOf(availableCourses.firstOrNull() ?: "通用任务") }
    var summary by remember { mutableStateOf("") }
    var expectedMinutesText by remember { mutableStateOf("30") }
    var detailContent by remember { mutableStateOf("") }
    var stagesText by remember { mutableStateOf(defaultTaskStagesText()) }
    var promptHint by remember { mutableStateOf("") }
    var materials by remember { mutableStateOf<List<DraftTaskMaterial>>(emptyList()) }
    var aiStatus by remember { mutableStateOf("") }
    var isAutoCompleting by remember { mutableStateOf(false) }
    var autoCompleteJob by remember { mutableStateOf<Job?>(null) }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isEmpty()) {
            return@rememberLauncherForActivityResult
        }
        val newMaterials = uris.mapNotNull { uri ->
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            context.resolveDraftTaskMaterial(uri)
        }
        if (newMaterials.isNotEmpty()) {
            materials = (materials + newMaterials).distinctBy { it.uri }
        }
    }

    fun startMockAutoComplete() {
        autoCompleteJob?.cancel()
        autoCompleteJob = scope.launch {
            isAutoCompleting = true
            aiStatus = "AI 正在分析课程内容..."

            val safeTitle = title.ifBlank { "课程整理任务" }
            val generatedSummary = "针对《$courseName》的「$safeTitle」，输出可直接演示的阶段化执行方案。"
            val generatedDetail = "目标：完成任务拆解、执行验证与结果沉淀。要求：每个阶段可追踪进度，并最终产出一份结构化结果。"
            val generatedStages = listOf(
                "阅读并理解任务目标",
                "准备实验/学习环境",
                "执行核心步骤并记录过程",
                "测试结果并整理关键结论",
                "生成最终报告与复盘建议"
            )
            val generatedPrompt = "输出风格：简洁、结构化、可复现；优先给结论，再给关键依据。"

            suspend fun streamFill(text: String, setter: (String) -> Unit) {
                setter("")
                var built = ""
                text.chunked(6).forEach { chunk ->
                    delay(80)
                    built += chunk
                    setter(built)
                }
            }

            streamFill(generatedSummary) { summary = it }
            aiStatus = "AI 正在补全任务详情..."
            streamFill(generatedDetail) { detailContent = it }

            aiStatus = "AI 正在规划执行阶段..."
            stagesText = ""
            generatedStages.forEachIndexed { index, stage ->
                delay(120)
                stagesText += "${index + 1}. $stage"
                if (index != generatedStages.lastIndex) {
                    stagesText += "\n"
                }
            }

            aiStatus = "AI 正在补全提示词..."
            streamFill(generatedPrompt) { promptHint = it }

            aiStatus = "AI 自动补全完成（Mock）"
            isAutoCompleting = false
        }
    }

    DisposableEffect(Unit) {
        onDispose { autoCompleteJob?.cancel() }
    }

    val parsedStages = parseTaskStagesInput(stagesText)
    val expectedMinutes = expectedMinutesText.toIntOrNull() ?: 0

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = title.trim().isNotEmpty() && parsedStages.isNotEmpty() && expectedMinutes > 0 && !isAutoCompleting,
                onClick = {
                    onConfirm(
                        TaskCreatePayload(
                            title = title.trim(),
                            courseName = courseName,
                            summary = summary.trim(),
                            expectedMinutes = expectedMinutes,
                            detailContent = detailContent.trim(),
                            stages = parsedStages,
                            promptHint = promptHint.trim(),
                            materials = materials.map {
                                CourseMaterialDraft(
                                    sourceUri = it.uri.toString(),
                                    displayName = it.displayName,
                                    mimeType = it.mimeType
                                )
                            }
                        )
                    )
                }
            ) {
                Text("创建")
            }
        },
        dismissButton = {
            TextButton(onClick = { autoCompleteJob?.cancel(); onDismiss() }) {
                Text("取消")
            }
        },
        title = { Text("新建任务") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 560.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    singleLine = true,
                    label = { Text("任务标题") },
                    modifier = Modifier.fillMaxWidth()
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "课程名称（从课程列表选择）",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(availableCourses) { item ->
                            val selected = item == courseName
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(
                                        if (selected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surface
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(18.dp)
                                    )
                                    .clickable { courseName = item }
                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = item,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    Text(
                        text = "当前选择：$courseName",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "任务附件",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        TextButton(onClick = { picker.launch(arrayOf("*/*")) }) {
                            Icon(Icons.Default.AttachFile, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("上传文件")
                        }
                    }
                    if (materials.isEmpty()) {
                        Text(
                            text = "可选上传任务相关文件，后续跨端执行会携带这些附件。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        materials.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.displayName, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                                    Text(
                                        formatAttachmentSize(item.sizeBytes),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { materials = materials.filterNot { it.uri == item.uri } }) {
                                    Icon(Icons.Default.Close, contentDescription = "移除附件")
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    label = { Text("任务简介") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = expectedMinutesText,
                    onValueChange = { expectedMinutesText = it.filter(Char::isDigit) },
                    singleLine = true,
                    label = { Text("期望耗时（分钟）") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = detailContent,
                    onValueChange = { detailContent = it },
                    label = { Text("详细内容") },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = stagesText,
                    onValueChange = { stagesText = it },
                    label = { Text("执行步骤/阶段（每行一个）") },
                    minLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = promptHint,
                    onValueChange = { promptHint = it },
                    label = { Text("可选提示词（效果/约束）") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        enabled = !isAutoCompleting,
                        onClick = { startMockAutoComplete() }
                    ) {
                        Text(if (isAutoCompleting) "AI 补全中..." else "AI 自动补全")
                    }
                    if (aiStatus.isNotBlank()) {
                        Text(
                            text = aiStatus,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    )
}

private data class TaskCreatePayload(
    val title: String,
    val courseName: String,
    val summary: String,
    val expectedMinutes: Int,
    val detailContent: String,
    val stages: List<String>,
    val promptHint: String,
    val materials: List<CourseMaterialDraft>
)

private data class DraftTaskMaterial(
    val uri: Uri,
    val displayName: String,
    val mimeType: String,
    val sizeBytes: Long
)

private fun android.content.Context.resolveDraftTaskMaterial(uri: Uri): DraftTaskMaterial? {
    val resolver = contentResolver
    val defaultName = "task_file_${System.currentTimeMillis()}"
    var displayName = defaultName
    var sizeBytes = 0L
    val mimeType = resolver.getType(uri) ?: "application/octet-stream"

    runCatching {
        resolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
            null,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (nameIndex >= 0) {
                    displayName = cursor.getString(nameIndex) ?: defaultName
                }
                if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) {
                    sizeBytes = cursor.getLong(sizeIndex)
                }
            }
        }
    }

    return DraftTaskMaterial(
        uri = uri,
        displayName = displayName,
        mimeType = mimeType,
        sizeBytes = sizeBytes
    )
}

private fun formatAttachmentSize(sizeBytes: Long): String {
    if (sizeBytes <= 0L) return "未知大小"
    val kb = sizeBytes / 1024.0
    if (kb < 1024) return String.format(Locale.US, "%.1f KB", kb)
    return String.format(Locale.US, "%.1f MB", kb / 1024.0)
}

private fun parseTaskStagesInput(raw: String): List<String> {
    val parsed = raw.lineSequence()
        .map { line ->
            line.trim()
                .replace(Regex("^[-•]+\\s*"), "")
                .replace(Regex("^\\d+[.)、]\\s*"), "")
        }
        .filter { it.isNotEmpty() }
        .toList()
    return if (parsed.isEmpty()) {
        listOf(
            "阅读并理解任务目标",
            "准备执行环境",
            "执行核心步骤",
            "测试并记录结果",
            "整理最终报告"
        )
    } else {
        parsed
    }
}

private fun defaultTaskStagesText(): String {
    return listOf(
        "1. 阅读并理解任务目标",
        "2. 准备执行环境",
        "3. 执行核心步骤",
        "4. 测试并记录结果",
        "5. 整理最终报告"
    ).joinToString("\n")
}

private fun StudyTask.toTaskItem(): TaskItem {
    val mappedStatus = when (status) {
        StudyTaskStatus.QUEUED -> TaskStatus.AWAITING_APPROVAL
        StudyTaskStatus.RUNNING -> TaskStatus.RUNNING
        StudyTaskStatus.COMPLETED -> TaskStatus.COMPLETED
        StudyTaskStatus.FAILED -> TaskStatus.FAILED
    }

    return TaskItem(
        id = id,
        title = title,
        status = mappedStatus,
        statusDetail = statusDetail,
        progress = (progress.coerceIn(0, 100) / 100f),
        isCrossDevice = status == StudyTaskStatus.RUNNING,
        startTime = createdAt,
        stepCount = stages.size,
        estimatedMinutes = expectedMinutes,
        deliverables = if (result.isNotBlank()) result else summary
    )
}
