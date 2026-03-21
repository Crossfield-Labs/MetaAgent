@file:OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.ai.assistance.metaagent.ui.features.settings.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.format.DateFormat
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ai.assistance.metaagent.remote.RemoteAgentTaskSnapshot
import com.ai.assistance.metaagent.ui.features.settings.viewmodel.RemoteControlUiState
import com.ai.assistance.metaagent.ui.features.settings.viewmodel.RemoteControlViewModel
import com.ai.assistance.metaagent.ui.features.settings.viewmodel.RemoteControlViewModelFactory
import kotlinx.coroutines.delay
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteControlSettingsScreen(onBackPressed: () -> Unit) {
    val context = LocalContext.current
    val viewModel: RemoteControlViewModel = viewModel(
        factory = RemoteControlViewModelFactory(context)
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        while (true) {
            viewModel.refresh()
            delay(1500)
        }
    }

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                HeaderCard(
                    uiState = uiState,
                    onToggleService = {
                        if (uiState.isServiceRunning) viewModel.stopService() else viewModel.startService()
                    },
                    onRefresh = { viewModel.refresh() },
                    onCopyAddress = {
                        val host = uiState.localIpAddress ?: "127.0.0.1"
                        copyText(context, "remote_url", "http://$host:${uiState.port}")
                    }
                )
            }

            item {
                SessionCard(
                    uiState = uiState,
                    onCreateSession = { viewModel.createDebugSession() },
                    onCloseSession = { viewModel.closeSession() },
                    onCopyToken = {
                        uiState.debugToken?.let { token ->
                            copyText(context, "remote_token", token)
                        }
                    }
                )
            }

            item {
                CapabilityCard(uiState = uiState)
            }

            item {
                EndpointCard(port = uiState.port, localIpAddress = uiState.localIpAddress)
            }

            item {
                MemoryCard()
            }

            item {
                Text(
                    text = "远程任务",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (uiState.tasks.isEmpty()) {
                item {
                    EmptyStateCard("当前没有远程任务。桌面端调用 agent/run 之后会在这里显示状态。")
                }
            } else {
                items(uiState.tasks, key = { it.taskId }) { task ->
                    TaskCard(task = task, onCancel = { viewModel.cancelTask(task.taskId) })
                }
            }
        }
    }
}

@Composable
private fun HeaderCard(
    uiState: RemoteControlUiState,
    onToggleService: () -> Unit,
    onRefresh: () -> Unit,
    onCopyAddress: () -> Unit
) {
    val host = uiState.localIpAddress ?: "未获取到局域网地址"
    InfoCard(
        title = "远程控制",
        icon = Icons.Default.Devices,
        action = {
            IconButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, contentDescription = null)
            }
        }
    ) {
        Text(
            text = "把手机作为独立远程服务暴露给电脑端。当前协议是 HTTP JSON，默认端口 ${uiState.port}。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (uiState.isServiceRunning) "服务运行中" else "服务未启动",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$host:${uiState.port}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = uiState.isServiceRunning,
                onCheckedChange = { onToggleService() }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onToggleService) {
                Icon(
                    if (uiState.isServiceRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (uiState.isServiceRunning) "停止服务" else "启动服务")
            }
            OutlinedButton(onClick = onCopyAddress, enabled = uiState.localIpAddress != null) {
                Icon(Icons.Default.ContentCopy, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("复制地址")
            }
        }
    }
}

@Composable
private fun SessionCard(
    uiState: RemoteControlUiState,
    onCreateSession: () -> Unit,
    onCloseSession: () -> Unit,
    onCopyToken: () -> Unit
) {
    InfoCard(title = "会话与鉴权", icon = Icons.Default.Key) {
        val session = uiState.session
        if (session == null) {
            Text(
                text = "当前没有活跃桌面会话。你可以在手机上先创建一个调试 session，或让桌面端直接调用 `session/open`。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onCreateSession) {
                Icon(Icons.Default.Security, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("创建调试会话")
            }
        } else {
            MetaRow(label = "Session ID", value = session.sessionId)
            MetaRow(label = "Client", value = session.clientName)
            MetaRow(label = "Created", value = formatTime(session.createdAtEpochMs))
            MetaRow(label = "Last Seen", value = formatTime(session.lastSeenAtEpochMs))
            uiState.debugToken?.let { token ->
                MetaRow(label = "Token", value = token.take(12) + "...")
            }
            Spacer(modifier = Modifier.height(12.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onCopyToken, enabled = uiState.debugToken != null) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("复制 Token")
                }
                TextButton(onClick = onCloseSession) {
                    Icon(Icons.Default.Cancel, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("关闭会话")
                }
            }
        }
    }
}

@Composable
private fun CapabilityCard(uiState: RemoteControlUiState) {
    val caps = uiState.capabilities ?: return
    InfoCard(title = "执行能力", icon = Icons.Default.NetworkCheck) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatusChip("权限级别", caps.permissionLevel, true)
            StatusChip("无障碍", if (caps.accessibilityEnabled) "已启用" else "未启用", caps.accessibilityEnabled)
            StatusChip("Shizuku", if (caps.shizukuRunning) "运行中" else "未运行", caps.shizukuRunning)
            StatusChip("Shizuku授权", if (caps.shizukuGranted) "已授权" else "未授权", caps.shizukuGranted)
            StatusChip(
                "虚拟屏实验",
                if (caps.experimentalVirtualDisplayEnabled) "开启" else "关闭",
                caps.experimentalVirtualDisplayEnabled
            )
            StatusChip(
                "Display",
                caps.activeDisplayId?.toString() ?: "无",
                caps.activeDisplayId != null
            )
            StatusChip("Android", "API ${caps.sdkInt}", true)
        }
    }
}

@Composable
private fun EndpointCard(port: Int, localIpAddress: String?) {
    val host = localIpAddress ?: "127.0.0.1"
    InfoCard(title = "电脑端怎么接", icon = Icons.Default.Route) {
        Text(
            text = "推荐顺序：启动服务 -> session/open -> heartbeat -> screenshot/input -> agent -> memory。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        CodeBlock("http://$host:$port/api/remote/health")
        Spacer(modifier = Modifier.height(8.dp))
        CodeBlock("POST /api/remote/session/open")
        CodeBlock("GET /api/remote/heartbeat")
        CodeBlock("GET /api/remote/screenshot")
        CodeBlock("POST /api/remote/input/tap")
        CodeBlock("POST /api/remote/agent/run")
        CodeBlock("GET /api/remote/memory/query")
    }
}

@Composable
private fun MemoryCard() {
    InfoCard(title = "记忆库接口", icon = Icons.Default.Memory) {
        Text(
            text = "远程记忆不是隐藏 prompt。桌面端直接读取和操作显式 Memory/Link/Graph 对象，人可以看、可以改、可以导出。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ElevatedAssistChip(onClick = {}, label = { Text("memory/query") }, leadingIcon = { Icon(Icons.Default.Hub, null) })
            ElevatedAssistChip(onClick = {}, label = { Text("memory/item") }, leadingIcon = { Icon(Icons.Default.Hub, null) })
            ElevatedAssistChip(onClick = {}, label = { Text("memory/document") }, leadingIcon = { Icon(Icons.Default.Hub, null) })
            ElevatedAssistChip(onClick = {}, label = { Text("memory/create") }, leadingIcon = { Icon(Icons.Default.Hub, null) })
            ElevatedAssistChip(onClick = {}, label = { Text("memory/update") }, leadingIcon = { Icon(Icons.Default.Hub, null) })
            ElevatedAssistChip(onClick = {}, label = { Text("memory/link") }, leadingIcon = { Icon(Icons.Default.Hub, null) })
            ElevatedAssistChip(onClick = {}, label = { Text("memory/graph") }, leadingIcon = { Icon(Icons.Default.Hub, null) })
        }
    }
}

@Composable
private fun TaskCard(task: RemoteAgentTaskSnapshot, onCancel: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.intent,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "agentId=${task.agentId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusChip(
                    label = "状态",
                    value = task.status,
                    positive = task.status == "completed" || task.status == "running"
                )
            }
            task.targetApp?.takeIf { it.isNotBlank() }?.let {
                MetaRow(label = "Target App", value = it)
            }
            MetaRow(label = "Steps", value = "${task.executionSteps}/${task.maxSteps}")
            task.displayId?.let { MetaRow(label = "Display", value = it.toString()) }
            task.finalMessage?.takeIf { it.isNotBlank() }?.let { MetaRow(label = "Message", value = it) }
            task.error?.takeIf { it.isNotBlank() }?.let { MetaRow(label = "Error", value = it) }
            if (task.status == "running") {
                TextButton(onClick = onCancel) {
                    Icon(Icons.Default.PowerSettingsNew, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("取消任务")
                }
            }
        }
    }
}

@Composable
private fun EmptyStateCard(message: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InfoCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    action: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                action?.invoke()
            }
            content()
        }
    }
}

@Composable
private fun StatusChip(label: String, value: String, positive: Boolean) {
    ElevatedAssistChip(
        onClick = {},
        label = { Text("$label: $value") },
        leadingIcon = {
            Icon(
                if (positive) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null
            )
        }
    )
}

@Composable
private fun MetaRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = label,
            modifier = Modifier.width(88.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun CodeBlock(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.bodySmall)
    }
}

private fun formatTime(timestamp: Long): String {
    return DateFormat.format("MM-dd HH:mm:ss", Date(timestamp)).toString()
}

private fun copyText(context: Context, label: String, value: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, value))
    Toast.makeText(context, "已复制", Toast.LENGTH_SHORT).show()
}
