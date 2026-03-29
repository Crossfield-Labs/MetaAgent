package com.ai.assistance.metaagent.ui.features.home.screens

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ai.assistance.metaagent.ui.features.home.data.CourseMaterialDraft
import com.ai.assistance.metaagent.ui.features.home.data.StudyModuleStore
import com.ai.assistance.metaagent.ui.features.home.data.StudyTask
import com.ai.assistance.metaagent.ui.features.home.data.StudyTaskStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

@Composable
fun CrossDeviceExecutionScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    StudyModuleStore.ensureInitialized(context)
    val scope = rememberCoroutineScope()

    val tasks = StudyModuleStore.tasks
    var selectedTaskId by remember { mutableStateOf<String?>(null) }
    val selectedTask = tasks.firstOrNull { it.id == selectedTaskId }

    var inputText by remember { mutableStateOf("") }
    var showPlusPanel by remember { mutableStateOf(false) }
    var showTaskSelector by remember { mutableStateOf(false) }
    var uploadingHint by remember { mutableStateOf("") }
    var uploadedFiles by remember { mutableStateOf<List<CrossDeviceDraftFile>>(emptyList()) }
    var botReplyJob by remember { mutableStateOf<Job?>(null) }

    val messages = remember { mutableStateListOf<CrossDeviceMessage>() }

    fun appendMessage(fromUser: Boolean, content: String) {
        messages.add(
            CrossDeviceMessage(
                id = "msg_${System.currentTimeMillis()}_${Random.nextInt(100, 999)}",
                fromUser = fromUser,
                content = content,
                timeLabel = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            )
        )
    }

    LaunchedEffect(Unit) {
        if (messages.isEmpty()) {
            appendMessage(
                fromUser = false,
                content = "PC 执行机器人已连接。请通过左下角 + 上传文件并选择任务，再发送执行指令。"
            )
        }
    }

    LaunchedEffect(tasks.size, selectedTaskId) {
        if (selectedTaskId != null && selectedTask == null) {
            selectedTaskId = null
        }
    }

    val uploadLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isEmpty()) {
            return@rememberLauncherForActivityResult
        }
        val files = uris.mapNotNull { uri ->
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            context.resolveCrossDeviceDraftFile(uri)
        }
        if (files.isEmpty()) {
            uploadingHint = "文件读取失败，请重试。"
            return@rememberLauncherForActivityResult
        }

        uploadedFiles = (uploadedFiles + files).distinctBy { it.uri }
        val selectedId = selectedTaskId
        if (selectedId != null) {
            val addedCount = StudyModuleStore.addMaterialsToTask(
                taskId = selectedId,
                materials = files.map {
                    CourseMaterialDraft(
                        sourceUri = it.uri.toString(),
                        displayName = it.displayName,
                        mimeType = it.mimeType
                    )
                }
            )
            uploadingHint = if (addedCount > 0) {
                "已上传并绑定 $addedCount 个文件到当前任务。"
            } else {
                "文件已选择，但未成功绑定任务。"
            }
        } else {
            uploadingHint = "已上传 ${files.size} 个文件，请先选择要执行的任务。"
        }
        appendMessage(fromUser = false, content = uploadingHint)
    }

    fun submitPrompt() {
        val prompt = inputText.trim()
        if (prompt.isEmpty()) {
            return
        }
        appendMessage(fromUser = true, content = prompt)
        inputText = ""

        val task = selectedTask
        if (task == null) {
            appendMessage(fromUser = false, content = "还没有选择任务。请点击 + -> 选择任务。")
            return
        }

        val started = StudyModuleStore.startTaskExecution(task.id)
        if (!started) {
            appendMessage(fromUser = false, content = "任务启动失败，请稍后再试。")
            return
        }

        appendMessage(fromUser = false, content = "已接收任务「${task.title}」，PC 执行器已启动。")

        botReplyJob?.cancel()
        botReplyJob = scope.launch {
            delay(700)
            val attachInfo = if (uploadedFiles.isNotEmpty()) {
                "已挂载 ${uploadedFiles.size} 个文件附件，开始解析内容。"
            } else {
                "未检测到附件，将按任务描述直接执行。"
            }
            appendMessage(fromUser = false, content = attachInfo)

            delay(1000)
            appendMessage(
                fromUser = false,
                content = "正在执行阶段 1/${task.stages.size.coerceAtLeast(1)}：${task.stages.firstOrNull() ?: "准备阶段"}"
            )

            delay(1800)
            val latest = StudyModuleStore.getTask(task.id)
            val resultHint = if (latest?.status == StudyTaskStatus.COMPLETED) {
                "任务「${task.title}」已完成，结果已回传并通知移动端。"
            } else {
                "任务执行中，可在“一键整理”查看实时进度，完成后会自动通知你。"
            }
            appendMessage(fromUser = false, content = resultHint)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Devices,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "跨端执行会话",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = selectedTask?.let { "当前任务：${it.title}" } ?: "当前任务：未选择",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "附件：${uploadedFiles.size} 个",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                CrossDeviceMessageBubble(message = message)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            IconButton(onClick = { showPlusPanel = true }) {
                Icon(Icons.Default.Add, contentDescription = "更多操作")
            }
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("输入：请阅读文件并完成任务，完成后通知我") },
                modifier = Modifier.weight(1f),
                maxLines = 3
            )
            TextButton(
                enabled = inputText.trim().isNotEmpty(),
                onClick = { submitPrompt() }
            ) {
                Text("发送")
            }
        }
    }

    if (showPlusPanel) {
        AlertDialog(
            onDismissRequest = { showPlusPanel = false },
            confirmButton = { TextButton(onClick = { showPlusPanel = false }) { Text("关闭") } },
            title = { Text("跨端执行操作") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .clickable {
                                showPlusPanel = false
                                uploadLauncher.launch(arrayOf("*/*"))
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AttachFile, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("上传文件")
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .clickable {
                                showPlusPanel = false
                                showTaskSelector = true
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("选择任务")
                    }
                }
            }
        )
    }

    if (showTaskSelector) {
        val selectableTasks = tasks.sortedBy {
            when (it.status) {
                StudyTaskStatus.QUEUED -> 0
                StudyTaskStatus.RUNNING -> 1
                StudyTaskStatus.FAILED -> 2
                StudyTaskStatus.COMPLETED -> 3
            }
        }
        AlertDialog(
            onDismissRequest = { showTaskSelector = false },
            confirmButton = { TextButton(onClick = { showTaskSelector = false }) { Text("关闭") } },
            title = { Text("选择执行任务") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (selectableTasks.isEmpty()) {
                        Text(
                            text = "暂无任务，请先在“一键整理”创建任务。",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        selectableTasks.forEach { task ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                    .clickable {
                                        selectedTaskId = task.id
                                        showTaskSelector = false
                                        appendMessage(fromUser = false, content = "已选择任务：${task.title}")
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(task.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                    Text(
                                        "${task.status.label} · ${task.courseName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun CrossDeviceMessageBubble(message: CrossDeviceMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.fromUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (message.fromUser) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceContainerLow
                )
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Column {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (message.fromUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.timeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private data class CrossDeviceMessage(
    val id: String,
    val fromUser: Boolean,
    val content: String,
    val timeLabel: String
)

private data class CrossDeviceDraftFile(
    val uri: Uri,
    val displayName: String,
    val mimeType: String,
    val sizeBytes: Long
)

private fun android.content.Context.resolveCrossDeviceDraftFile(uri: Uri): CrossDeviceDraftFile? {
    val resolver = contentResolver
    val defaultName = "file_${System.currentTimeMillis()}"
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

    return CrossDeviceDraftFile(
        uri = uri,
        displayName = displayName,
        mimeType = mimeType,
        sizeBytes = sizeBytes
    )
}
