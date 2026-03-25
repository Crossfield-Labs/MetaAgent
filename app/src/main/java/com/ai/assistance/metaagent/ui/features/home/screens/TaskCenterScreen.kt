package com.ai.assistance.metaagent.ui.features.home.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ai.assistance.metaagent.ui.features.home.data.MetaSampleData
import com.ai.assistance.metaagent.ui.features.home.data.TaskItem
import com.ai.assistance.metaagent.ui.features.home.data.TaskStatus
import com.ai.assistance.metaagent.ui.theme.TaskApproval
import com.ai.assistance.metaagent.ui.theme.TaskCompleted
import com.ai.assistance.metaagent.ui.theme.TaskRunning

/**
 * 任务中心 Dashboard — 参考 Figma 设计稿 + 产品书"做"模块
 */
@Composable
fun TaskCenterScreen(
    onTaskClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tasks = MetaSampleData.tasks
    val runningCount = tasks.count { it.status == TaskStatus.RUNNING }
    val completedCount = tasks.count { it.status == TaskStatus.COMPLETED }
    val approvalCount = tasks.count { it.status == TaskStatus.AWAITING_APPROVAL }
    val totalCount = tasks.size
    val completionRate = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    // Tab 状态
    val tabs = listOf("进行中", "新任务", "已归档")
    var selectedTab by remember { mutableStateOf("进行中") }

    val filteredTasks = when (selectedTab) {
        "进行中" -> tasks.filter {
            it.status == TaskStatus.RUNNING || it.status == TaskStatus.AWAITING_APPROVAL
        }
        "新任务" -> tasks.filter {
            it.status == TaskStatus.PAUSED
        }
        "已归档" -> tasks.filter {
            it.status == TaskStatus.COMPLETED || it.status == TaskStatus.FAILED
        }
        else -> tasks
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // ══ 问候区 ══
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 问候
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
                // 右侧图标
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { }) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "通知",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // 头像
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

        // ══ 统计卡片横滑 ══
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
                        approval = approvalCount
                    )
                }
                item {
                    QuickActionCard(
                        title = "新建任务",
                        subtitle = "Copilot 共做",
                        bgColor = MaterialTheme.colorScheme.primaryContainer,
                        onClick = { }
                    )
                }
                item {
                    CrossDeviceCard()
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ══ Tab 栏 ══
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
                        "进行中" -> runningCount + approvalCount
                        "新任务" -> tasks.count { it.status == TaskStatus.PAUSED }
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

        // ══ Your Progress 面板 ══
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                    .padding(20.dp)
            ) {
                // 标题行
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

                // 大号百分比
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
                    // 竖条指示器
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

        // ══ 任务列表 ══
        items(filteredTasks) { task ->
            TaskCard(task = task, onClick = { onTaskClick(task.id) })
        }
    }
}

// ══════════════════════════════════════════
// 统计卡片
// ══════════════════════════════════════════

@Composable
private fun StatisticsCard(
    totalTasks: Int,
    running: Int,
    completed: Int,
    approval: Int
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
            Row(verticalAlignment = Alignment.CenterVertically) {
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

// ══════════════════════════════════════════
// 快捷操作卡片
// ══════════════════════════════════════════

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

// ══════════════════════════════════════════
// 跨端状态卡片
// ══════════════════════════════════════════

@Composable
private fun CrossDeviceCard() {
    Column(
        modifier = Modifier
            .width(170.dp)
            .height(165.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
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

// ══════════════════════════════════════════
// 任务卡片
// ══════════════════════════════════════════

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
        // 左侧彩条
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
            // 标题行
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

            // 进度条
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

            // 跨端标签
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

            // 交付物
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
