package com.ai.assistance.metaagent.ui.features.home.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ai.assistance.metaagent.ui.features.home.data.StudyCourseMaterial
import com.ai.assistance.metaagent.ui.features.home.data.StudyModuleStore
import com.ai.assistance.metaagent.ui.features.home.data.StudyTask
import com.ai.assistance.metaagent.ui.features.home.data.StudyTaskStatus
import kotlinx.coroutines.launch

private enum class DeliverySummaryMode(val label: String) {
    SNAPSHOT("摘要"),
    HIGHLIGHTS("关键结论"),
    CONSTRAINTS("执行约束")
}

private data class DeliveryArtifactItem(
    val id: String,
    val title: String,
    val category: String,
    val summary: String,
    val preview: String
)

@Composable
fun ResultDeliveryScreen(
    taskId: String,
    onOpenCourse: () -> Unit = {},
    onOpenTaskCenter: () -> Unit = {},
    onOpenReview: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    StudyModuleStore.ensureInitialized(context)
    val task = StudyModuleStore.getTask(taskId)
    val siblingRunningCount = StudyModuleStore.tasks.count {
        it.id != taskId && it.status == StudyTaskStatus.RUNNING
    }
    val completedTaskCount = StudyModuleStore.tasks.count { it.status == StudyTaskStatus.COMPLETED }
    val unreadNotificationCount = StudyModuleStore.taskNotifications.count { !it.isRead }

    if (task == null) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("未找到结果，可能该任务已被删除。", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    val artifacts = remember(task) { buildDeliveryArtifacts(task) }
    val timelineStages = remember(task) {
        if (task.stages.isNotEmpty()) task.stages else listOf("等待生成交付阶段")
    }

    var summaryModeKey by rememberSaveable(taskId) { mutableStateOf(DeliverySummaryMode.SNAPSHOT.name) }
    val summaryMode = DeliverySummaryMode.valueOf(summaryModeKey)
    var selectedArtifactId by rememberSaveable(taskId) { mutableStateOf<String?>(null) }
    var expandedStageIndex by rememberSaveable(taskId) {
        mutableStateOf((task.completedStageCount - 1).coerceAtLeast(0).coerceAtMost(timelineStages.lastIndex))
    }
    var packageReady by rememberSaveable(taskId) { mutableStateOf(task.status == StudyTaskStatus.COMPLETED) }
    var reviewQueued by rememberSaveable(taskId) { mutableStateOf(false) }
    var courseSynced by rememberSaveable(taskId) { mutableStateOf(true) }
    var taskCenterPinned by rememberSaveable(taskId) { mutableStateOf(true) }

    val selectedArtifact = artifacts.firstOrNull { it.id == selectedArtifactId }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val showMessage: (String) -> Unit = { message ->
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    if (selectedArtifact != null) {
        AlertDialog(
            onDismissRequest = { selectedArtifactId = null },
            title = { Text(selectedArtifact.title, fontWeight = FontWeight.SemiBold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        selectedArtifact.category,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        selectedArtifact.preview,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showMessage("${selectedArtifact.title} 已加入本次交付包。")
                    selectedArtifactId = null
                }) {
                    Text("标记已查看")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedArtifactId = null }) {
                    Text("关闭")
                }
            }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                DeliveryHeroCard(
                    task = task,
                    packageReady = packageReady,
                    onCopySummary = { showMessage("交付摘要已复制到剪贴板。") },
                    onPreparePackage = {
                        packageReady = true
                        showMessage("交付包已生成，可直接发给老师或同学。")
                    },
                    onPreviewArtifacts = { selectedArtifactId = artifacts.firstOrNull()?.id },
                    onOpenCourse = {
                        courseSynced = true
                        onOpenCourse()
                    }
                )
            }
            item {
                DeliverySummaryCard(
                    task = task,
                    summaryMode = summaryMode,
                    onModeChange = { summaryModeKey = it.name },
                    onCopy = { showMessage("${summaryMode.label}已复制。") },
                    onOpenPreview = { selectedArtifactId = artifacts.firstOrNull()?.id }
                )
            }
            item {
                DeliveryImpactCard(
                    task = task,
                    siblingRunningCount = siblingRunningCount,
                    completedTaskCount = completedTaskCount,
                    unreadNotificationCount = unreadNotificationCount,
                    courseSynced = courseSynced,
                    reviewQueued = reviewQueued,
                    taskCenterPinned = taskCenterPinned,
                    onSyncCourse = {
                        courseSynced = true
                        showMessage("结果已回流到 ${task.courseName}。")
                    },
                    onQueueReview = {
                        reviewQueued = true
                        showMessage("今晚复习已加入 2 条跟进卡片。")
                    },
                    onPinTaskCenter = {
                        taskCenterPinned = true
                        showMessage("结果已固定在任务中心顶部。")
                    }
                )
            }
            item {
                DeliveryArtifactsCard(
                    artifacts = artifacts,
                    packageReady = packageReady,
                    onPreviewArtifact = { selectedArtifactId = it.id },
                    onPreparePackage = {
                        packageReady = true
                        showMessage("交付包已更新为最新版本。")
                    },
                    onSendToCourse = {
                        courseSynced = true
                        showMessage("交付物已同步到课程资料区。")
                    }
                )
            }
            item {
                DeliveryTimelineCard(
                    task = task,
                    stages = timelineStages,
                    expandedStageIndex = expandedStageIndex,
                    onSelectStage = { expandedStageIndex = it }
                )
            }
            item {
                DeliveryNextActionsCard(
                    task = task,
                    reviewQueued = reviewQueued,
                    packageReady = packageReady,
                    onOpenCourse = {
                        courseSynced = true
                        onOpenCourse()
                    },
                    onOpenTaskCenter = {
                        taskCenterPinned = true
                        onOpenTaskCenter()
                    },
                    onOpenReview = {
                        reviewQueued = true
                        onOpenReview()
                    },
                    onPreparePackage = {
                        packageReady = true
                        showMessage("交付包已经就绪。")
                    }
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
}

@Composable
private fun DeliveryHeroCard(
    task: StudyTask,
    packageReady: Boolean,
    onCopySummary: () -> Unit,
    onPreparePackage: () -> Unit,
    onPreviewArtifacts: () -> Unit,
    onOpenCourse: () -> Unit
) {
    val accent = when (task.status) {
        StudyTaskStatus.COMPLETED -> Color(0xFF0F9D7A)
        StudyTaskStatus.RUNNING -> Color(0xFFE87722)
        StudyTaskStatus.QUEUED -> Color(0xFF5C6BC0)
        StudyTaskStatus.FAILED -> MaterialTheme.colorScheme.error
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(accent.copy(alpha = 0.10f))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = accent.copy(alpha = 0.14f)) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.padding(10.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("结果交付", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        "摘要、交付物和下一步动作都已经整理好，不需要再翻回原任务过程。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(task.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                "课程：${task.courseName} · 更新时间：${task.updatedAt}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DeliveryMetricChip(text = task.status.label, accent = accent)
                DeliveryMetricChip(text = "${task.completedStageCount}/${task.stages.size.coerceAtLeast(1)} 个阶段", accent = accent)
                if (task.expectedMinutes > 0) {
                    DeliveryMetricChip(text = "${task.expectedMinutes} 分钟", accent = accent)
                }
                DeliveryMetricChip(
                    text = if (packageReady) "交付包已就绪" else "待生成交付包",
                    accent = accent
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionChipButton("复制摘要", Icons.Default.ContentCopy, onCopySummary)
                ActionChipButton(
                    if (packageReady) "更新交付包" else "生成交付包",
                    Icons.Default.Download,
                    onPreparePackage
                )
                ActionChipButton("查看交付物", Icons.Default.Description, onPreviewArtifacts)
                ActionChipButton("打开课程", Icons.Default.School, onOpenCourse)
            }
        }
    }
}

@Composable
private fun DeliverySummaryCard(
    task: StudyTask,
    summaryMode: DeliverySummaryMode,
    onModeChange: (DeliverySummaryMode) -> Unit,
    onCopy: () -> Unit,
    onOpenPreview: () -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Description, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("交付摘要", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DeliverySummaryMode.values().forEach { mode ->
                    FilterChip(
                        selected = mode == summaryMode,
                        onClick = { onModeChange(mode) },
                        label = { Text(mode.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                        )
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = buildSummaryContent(task, summaryMode),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ActionChipButton("复制这一版", Icons.Default.ContentCopy, onCopy)
                        ActionChipButton("查看交付物", Icons.Default.FolderOpen, onOpenPreview)
                    }
                }
            }
        }
    }
}

@Composable
private fun DeliveryImpactCard(
    task: StudyTask,
    siblingRunningCount: Int,
    completedTaskCount: Int,
    unreadNotificationCount: Int,
    courseSynced: Boolean,
    reviewQueued: Boolean,
    taskCenterPinned: Boolean,
    onSyncCourse: () -> Unit,
    onQueueReview: () -> Unit,
    onPinTaskCenter: () -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("交付改变了什么", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                text = buildDeliveryImpactHeadline(task, siblingRunningCount, unreadNotificationCount),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            DeliveryImpactRow(
                title = "课程上下文",
                subtitle = if (courseSynced) {
                    "摘要与交付物已经回流到《${task.courseName}》，后续可直接在课程里引用。"
                } else {
                    "还没有把这份结果沉淀回课程，建议先同步课程侧资料。"
                },
                status = if (courseSynced) "已同步" else "待同步",
                actionLabel = "同步到课程",
                onClick = onSyncCourse
            )
            DeliveryImpactRow(
                title = "后续复习",
                subtitle = if (reviewQueued) {
                    "已从本次结果里抽出重点，今晚会优先推送 2 条跟进卡片。"
                } else {
                    "还没安排复习跟进，可以把本次结果拆成后续错卡和提醒。"
                },
                status = if (reviewQueued) "已安排" else "待安排",
                actionLabel = "加入今晚复习",
                onClick = onQueueReview
            )
            DeliveryImpactRow(
                title = "任务中心",
                subtitle = if (taskCenterPinned) {
                    "这份结果已经固定在任务中心顶部，方便继续查看后续动作。"
                } else {
                    "建议把这份结果固定到任务中心，避免被后续任务淹没。"
                },
                status = if (taskCenterPinned) "已固定" else "待固定",
                actionLabel = "固定到任务中心",
                onClick = onPinTaskCenter
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DeliveryMetricChip(
                    text = "累计 $completedTaskCount 个完成任务",
                    accent = MaterialTheme.colorScheme.primary
                )
                if (siblingRunningCount > 0) {
                    DeliveryMetricChip(
                        text = "还有 $siblingRunningCount 个任务推进中",
                        accent = MaterialTheme.colorScheme.primary
                    )
                }
                if (unreadNotificationCount > 0) {
                    DeliveryMetricChip(
                        text = "$unreadNotificationCount 条提醒待处理",
                        accent = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun DeliveryArtifactsCard(
    artifacts: List<DeliveryArtifactItem>,
    packageReady: Boolean,
    onPreviewArtifact: (DeliveryArtifactItem) -> Unit,
    onPreparePackage: () -> Unit,
    onSendToCourse: () -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("交付内容", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            Text(
                "交付物已经按“摘要、结论、过程、附件”整理好，点开就能看，不需要再回原任务里翻记录。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            artifacts.forEach { artifact ->
                DeliveryArtifactRow(artifact = artifact, onClick = { onPreviewArtifact(artifact) })
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionChipButton(
                    if (packageReady) "更新交付包" else "生成交付包",
                    Icons.Default.Download,
                    onPreparePackage
                )
                ActionChipButton("发送到课程", Icons.Default.School, onSendToCourse)
            }
        }
    }
}

@Composable
private fun DeliveryTimelineCard(
    task: StudyTask,
    stages: List<String>,
    expandedStageIndex: Int,
    onSelectStage: (Int) -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.TaskAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("执行回放", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            stages.forEachIndexed { index, stage ->
                DeliveryTimelineRow(
                    index = index,
                    title = stage,
                    completed = index < task.completedStageCount,
                    expanded = index == expandedStageIndex,
                    detail = buildStageDetail(task, stage, index),
                    onClick = { onSelectStage(index) }
                )
            }
        }
    }
}

@Composable
private fun DeliveryNextActionsCard(
    task: StudyTask,
    reviewQueued: Boolean,
    packageReady: Boolean,
    onOpenCourse: () -> Unit,
    onOpenTaskCenter: () -> Unit,
    onOpenReview: () -> Unit,
    onPreparePackage: () -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("下一步", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "这份结果已经整理成可继续操作的页面，下面这些动作都能直接接上下一段流程。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            DeliveryActionRow(
                title = "回课程空间",
                subtitle = "把这次任务结果继续沉淀回《${task.courseName}》课程上下文。",
                icon = Icons.Default.School,
                onClick = onOpenCourse
            )
            DeliveryActionRow(
                title = if (reviewQueued) "继续这一轮复习" else "安排后续复习",
                subtitle = if (reviewQueued) {
                    "本次结果已经拆成跟进重点，直接回到复习入口继续。"
                } else {
                    "从这次交付里挑重点，加入下一轮复习和提醒。"
                },
                icon = Icons.Default.AutoAwesome,
                onClick = onOpenReview
            )
            DeliveryActionRow(
                title = "回任务中心",
                subtitle = "继续查看其他任务、提醒和跨端执行状态。",
                icon = Icons.Default.FolderOpen,
                onClick = onOpenTaskCenter
            )
            DeliveryActionRow(
                title = if (packageReady) "更新交付包" else "生成交付包",
                subtitle = "准备一份可直接转发和回看的交付包。",
                icon = Icons.Default.Download,
                onClick = onPreparePackage
            )
        }
    }
}

@Composable
private fun DeliveryArtifactRow(
    artifact: DeliveryArtifactItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
        ) {
            Icon(
                Icons.Default.Description,
                contentDescription = null,
                modifier = Modifier.padding(8.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(artifact.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(
                "${artifact.category} · ${artifact.summary}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text("预览", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun DeliveryImpactRow(
    title: String,
    subtitle: String,
    status: String,
    actionLabel: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(status, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            ActionChipButton(actionLabel, Icons.Default.PlayArrow, onClick)
        }
    }
}

@Composable
private fun DeliveryTimelineRow(
    index: Int,
    title: String,
    completed: Boolean,
    expanded: Boolean,
    detail: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.padding(top = 2.dp),
                    shape = CircleShape,
                    color = if (completed) Color(0xFF0F9D7A) else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        "${index + 1}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (completed) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Text(
                        if (completed) "已完成并写入交付结果" else "点击查看这一阶段将承接什么",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (expanded) {
                Text(
                    detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DeliveryActionRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)) {
            Icon(icon, contentDescription = null, modifier = Modifier.padding(8.dp), tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DeliveryMetricChip(text: String, accent: Color) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = accent.copy(alpha = 0.12f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = accent
        )
    }
}

@Composable
private fun ActionChipButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

private fun buildSummaryContent(task: StudyTask, mode: DeliverySummaryMode): String {
    return when (mode) {
        DeliverySummaryMode.SNAPSHOT -> buildSnapshotText(task)
        DeliverySummaryMode.HIGHLIGHTS -> buildHighlightsText(task)
        DeliverySummaryMode.CONSTRAINTS -> buildString {
            appendLine("执行约束：${task.promptHint.ifBlank { "未额外设置，按默认策略执行。" }}")
            append("任务说明：${task.detailContent.ifBlank { "本次交付以可回看、可继续用为优先。" }}")
        }
    }
}

private fun buildDeliveryArtifacts(task: StudyTask): List<DeliveryArtifactItem> {
    val artifacts = mutableListOf(
        DeliveryArtifactItem(
            id = "summary",
            title = "交付摘要.md",
            category = "摘要",
            summary = "适合直接粘到汇报或聊天里",
            preview = buildSnapshotText(task)
        ),
        DeliveryArtifactItem(
            id = "conclusion",
            title = "最终结论.txt",
            category = "结论",
            summary = "给老师或同学看的收束版",
            preview = buildConclusionText(task)
        ),
        DeliveryArtifactItem(
            id = "timeline",
            title = "执行回放.log",
            category = "过程",
            summary = "${task.completedStageCount}/${task.stages.size.coerceAtLeast(1)} 个阶段已记录",
            preview = buildTimelinePreview(task)
        ),
        DeliveryArtifactItem(
            id = "follow_up",
            title = "后续动作.md",
            category = "跟进",
            summary = "课程沉淀、复习安排、任务中心回流",
            preview = buildFollowUpText(task)
        )
    )

    task.materials.orEmpty().forEachIndexed { index, material ->
        artifacts += buildMaterialArtifact(index, material)
    }
    return artifacts
}

private fun buildMaterialArtifact(index: Int, material: StudyCourseMaterial): DeliveryArtifactItem {
    return DeliveryArtifactItem(
        id = "material_$index",
        title = material.displayName,
        category = "附件",
        summary = "${formatSizeLabel(material.sizeBytes)} · ${material.mimeType.ifBlank { "未知类型" }}",
        preview = buildString {
            appendLine("来源文件：${material.displayName}")
            appendLine("加入时间：${material.addedAt}")
            append("这份附件已经绑定到当前任务，交付包会一并带上它。")
        }
    )
}

private fun buildStageDetail(task: StudyTask, stage: String, index: Int): String {
    return when {
        index < task.completedStageCount && task.status == StudyTaskStatus.COMPLETED ->
            "阶段“$stage”已完成，相关输出已经沉淀进当前交付页，可直接在上面的交付物里查看。"
        index < task.completedStageCount ->
            "阶段“$stage”已经推进完成，后续结果会继续刷新到当前页面。"
        index == task.completedStageCount ->
            "这是当前承接中的下一步。完成它之后，交付摘要和交付物都会一起更新。"
        else ->
            "这是后续预留阶段，前面的步骤完成后才会进入。"
    }
}

private fun buildSnapshotText(task: StudyTask): String {
    if (task.result.isNotBlank() && task.result.contains('\n')) {
        return task.result
    }
    return buildString {
        appendLine("交付概览")
        appendLine("任务：${task.title}")
        appendLine("课程：${task.courseName}")
        appendLine("状态：${task.status.label} · 已完成 ${task.completedStageCount}/${task.stages.size.coerceAtLeast(1)} 个阶段")
        appendLine()
        appendLine("本次输出")
        appendLine("1. ${task.summary.ifBlank { "已整理出当前任务的核心内容和结论。" }}")
        appendLine("2. 已根据执行过程沉淀出可回看的摘要、阶段记录和后续动作。")
        append("3. 结果已准备好继续回流到课程、复习与任务中心。")
    }
}

private fun buildHighlightsText(task: StudyTask): String {
    return buildString {
        appendLine("关键结论")
        appendLine("1. 任务目标：${task.summary.ifBlank { task.title }}")
        appendLine("2. 本次重点：${task.detailContent.ifBlank { "先给出可回看版本，再补细节。" }}")
        appendLine("3. 当前可用：摘要、执行回放、交付包、后续动作。")
        append("4. 下一步建议：先看交付摘要，再按需要进入课程空间或安排复习。")
    }
}

private fun buildConclusionText(task: StudyTask): String {
    return buildString {
        appendLine("最终结论")
        appendLine("任务：${task.title}")
        appendLine("课程：${task.courseName}")
        appendLine()
        appendLine("建议保留的交付内容：")
        appendLine("• 一页可直接转发的摘要")
        appendLine("• 一份按阶段组织的执行回放")
        appendLine("• 一组可以继续落回课程和复习的后续动作")
        appendLine()
        append("如果需要进一步展开，可优先查看执行回放和原始附件。")
    }
}

private fun buildTimelinePreview(task: StudyTask): String {
    if (task.stages.isEmpty()) {
        return "当前还没有阶段记录。"
    }
    return buildString {
        appendLine("执行回放")
        task.stages.forEachIndexed { index, stage ->
            val state = if (index < task.completedStageCount) "已完成" else "待进入"
            appendLine("${index + 1}. $stage · $state")
        }
    }.trimEnd()
}

private fun buildFollowUpText(task: StudyTask): String {
    return buildString {
        appendLine("后续动作")
        appendLine("1. 回流到《${task.courseName}》课程空间，作为可继续引用的结果。")
        appendLine("2. 从本次任务里抽取 2-3 条后续复习重点。")
        appendLine("3. 在任务中心保留这份交付，方便继续查看后续动作。")
        append("4. 如需转发，优先使用“交付摘要.md”和“最终结论.txt”。")
    }
}

private fun buildDeliveryImpactHeadline(
    task: StudyTask,
    siblingRunningCount: Int,
    unreadNotificationCount: Int
): String {
    return when {
        siblingRunningCount > 0 && unreadNotificationCount > 0 ->
            "结果已经固定下来，同时还有并行任务和提醒在推进，适合继续往下接。"
        siblingRunningCount > 0 ->
            "这份交付已经可回看，但任务中心里还有别的任务在继续推进。"
        unreadNotificationCount > 0 ->
            "结果已整理完成，接下来可以回任务中心处理提醒，或者继续沉淀到课程和复习。"
        task.status == StudyTaskStatus.COMPLETED ->
            "这份结果已经具备可展示、可回看、可继续用的条件。"
        else ->
            "任务还在推进，结果页会持续承接最新阶段输出。"
    }
}

private fun formatSizeLabel(sizeBytes: Long): String {
    if (sizeBytes <= 0L) return "未知大小"
    val kb = sizeBytes / 1024.0
    return if (kb >= 1024) {
        String.format("%.1f MB", kb / 1024.0)
    } else {
        String.format("%.0f KB", kb)
    }
}
