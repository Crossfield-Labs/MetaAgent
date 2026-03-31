package com.ai.assistance.metaagent.ui.features.home.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ai.assistance.metaagent.ui.components.CustomScaffold
import com.ai.assistance.metaagent.ui.features.home.data.StudyModuleStore
import com.ai.assistance.metaagent.ui.features.home.data.StudyTask
import com.ai.assistance.metaagent.ui.features.home.data.StudyTaskStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanTreeScreen(
    onBack: () -> Unit,
    onTaskClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    StudyModuleStore.ensureInitialized(context)
    val tasks = StudyModuleStore.tasks
    val courses = StudyModuleStore.courses

    CustomScaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("编排树") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        if (tasks.isEmpty() && courses.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.AccountTree,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "还没有任务或课程",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "从课程空间或任务中心创建后会在这里显示",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 按课程分组展示任务树
                items(courses) { course ->
                    val courseTasks = tasks.filter { it.courseName == course.name }
                    CourseTreeNode(
                        courseName = course.name,
                        tasks = courseTasks,
                        onTaskClick = onTaskClick
                    )
                }
                // 没有关联课程的任务
                val orphanTasks = tasks.filter { task ->
                    courses.none { it.name == task.courseName }
                }
                if (orphanTasks.isNotEmpty()) {
                    item {
                        CourseTreeNode(
                            courseName = "独立任务",
                            tasks = orphanTasks,
                            onTaskClick = onTaskClick
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
private fun CourseTreeNode(
    courseName: String,
    tasks: List<StudyTask>,
    onTaskClick: (String) -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AccountTree,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    courseName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    "${tasks.size} 个任务",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (tasks.isEmpty()) {
                Text(
                    "暂无任务",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                tasks.forEachIndexed { index, task ->
                    TaskTreeNode(
                        task = task,
                        isLast = index == tasks.lastIndex,
                        onClick = { onTaskClick(task.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskTreeNode(task: StudyTask, isLast: Boolean, onClick: () -> Unit = {}) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        // 树形连接线
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(taskStatusColor(task.status))
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(32.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = when (task.status) {
                            StudyTaskStatus.COMPLETED -> Icons.Default.CheckCircle
                            StudyTaskStatus.RUNNING -> Icons.Default.PlayArrow
                            StudyTaskStatus.QUEUED -> Icons.Default.HourglassEmpty
                            StudyTaskStatus.FAILED -> Icons.Default.RadioButtonUnchecked
                        },
                        contentDescription = null,
                        tint = taskStatusColor(task.status),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        task.status.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = taskStatusColor(task.status)
                    )
                }
            }
            if (task.stages.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                task.stages.forEachIndexed { i, stage ->
                    Text(
                        "  ${i + 1}. $stage",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (!isLast) Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun taskStatusColor(status: StudyTaskStatus): Color = when (status) {
    StudyTaskStatus.COMPLETED -> Color(0xFF0F9D7A)
    StudyTaskStatus.RUNNING -> MaterialTheme.colorScheme.primary
    StudyTaskStatus.QUEUED -> MaterialTheme.colorScheme.tertiary
    StudyTaskStatus.FAILED -> MaterialTheme.colorScheme.error
}
