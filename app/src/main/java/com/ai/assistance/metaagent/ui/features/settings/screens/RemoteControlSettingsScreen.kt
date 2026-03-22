@file:OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.ai.assistance.metaagent.ui.features.settings.screens

import android.graphics.BitmapFactory
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Base64
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Computer
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
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ai.assistance.metaagent.remote.RemoteAgentTaskSnapshot
import com.ai.assistance.metaagent.ui.features.settings.viewmodel.RemoteControlUiState
import com.ai.assistance.metaagent.ui.features.settings.viewmodel.RemoteControlViewModel
import com.ai.assistance.metaagent.ui.features.settings.viewmodel.RemoteControlViewModelFactory
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("UNUSED_PARAMETER")
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
                DesktopConnectionCard(
                    uiState = uiState,
                    onRefresh = { viewModel.checkDesktopHealth() },
                    onHostChanged = viewModel::updateDesktopHost,
                    onPortChanged = viewModel::updateDesktopPort,
                    onPasswordChanged = viewModel::updateDesktopPassword,
                    onRequestConnection = { viewModel.requestDesktopConnection() },
                    onRefreshPairing = { viewModel.refreshDesktopPairingStatus() },
                    onConfirmPassword = { viewModel.confirmDesktopPassword() }
                )
            }

            item {
                DesktopSessionCard(
                    uiState = uiState,
                    onHeartbeat = { viewModel.heartbeatDesktopSession() },
                    onCloseSession = { viewModel.closeDesktopSession() },
                    onQuerySession = { viewModel.queryDesktopSession() }
                )
            }

            item {
                DesktopActionCard(
                    uiState = uiState,
                    onFetchScreenshot = { viewModel.fetchDesktopScreenshot() },
                    onTogglePreviewAutoRefresh = viewModel::setDesktopPreviewAutoRefresh,
                    onMoveXChanged = viewModel::updateDesktopMoveX,
                    onMoveYChanged = viewModel::updateDesktopMoveY,
                    onTypeChanged = viewModel::updateDesktopTypeText,
                    onKeyChanged = viewModel::updateDesktopKeyText,
                    onMove = { viewModel.testDesktopMove() },
                    onClick = { viewModel.testDesktopClick() },
                    onType = { viewModel.testDesktopType() },
                    onKey = { viewModel.testDesktopKey() },
                    onTouchpadMove = viewModel::touchpadMove,
                    onTouchpadTap = { viewModel.touchpadTap() }
                )
            }

            item {
                DesktopEventsCard(
                    uiState = uiState,
                    onFetchEvents = { viewModel.fetchDesktopEvents() }
                )
            }

            item {
                LegacyPhoneServiceCard(
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
                    text = "兼容：手机远程任务",
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

    LaunchedEffect(
        uiState.desktopPreviewAutoRefresh,
        uiState.desktopSession?.id,
        uiState.desktopHost,
        uiState.desktopPort,
        uiState.desktopToken
    ) {
        if (uiState.desktopPreviewAutoRefresh && uiState.desktopSession != null) {
            viewModel.startDesktopStream()
        } else {
            viewModel.stopDesktopStream()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopDesktopStream()
        }
    }
}

@Composable
private fun DesktopConnectionCard(
    uiState: RemoteControlUiState,
    onRefresh: () -> Unit,
    onHostChanged: (String) -> Unit,
    onPortChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onRequestConnection: () -> Unit,
    onRefreshPairing: () -> Unit,
    onConfirmPassword: () -> Unit
) {
    InfoCard(
        title = "手机控制电脑",
        icon = Icons.Default.Devices,
        action = {
            IconButton(onClick = onRefresh, enabled = !uiState.desktopIsLoading) {
                Icon(Icons.Default.Refresh, contentDescription = null)
            }
        }
    ) {
        Text(
            text = "先填电脑地址并发起连接请求。电脑端确认后，再在手机上输入预设密码，系统会换取临时会话 token。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = uiState.desktopHost,
            onValueChange = onHostChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("电脑地址 / Host") },
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = uiState.desktopPort,
            onValueChange = onPortChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("端口") },
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        Spacer(modifier = Modifier.height(12.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onRequestConnection, enabled = !uiState.desktopIsLoading) {
                Text("请求连接")
            }
            OutlinedButton(onClick = onRefreshPairing, enabled = !uiState.desktopIsLoading && uiState.desktopPairingId != null) {
                Text("刷新连接状态")
            }
        }
        uiState.desktopPairingId?.let { pairingId ->
            Spacer(modifier = Modifier.height(8.dp))
            MetaRow(label = "Pairing ID", value = pairingId)
        }
        uiState.desktopPairingStatus?.let { pairingStatus ->
            MetaRow(label = "状态", value = pairingStatus)
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = uiState.desktopPassword,
            onValueChange = onPasswordChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("电脑端配对密码") },
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = onConfirmPassword,
            enabled = !uiState.desktopIsLoading && uiState.desktopPairingStatus == "approved"
        ) {
            Text("提交密码")
        }
        Spacer(modifier = Modifier.height(12.dp))
        uiState.desktopHealth?.let { health ->
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusChip("服务", health.service, true)
                StatusChip("桌面会话", if (health.hasDesktopSession) "已打开" else "未打开", health.hasDesktopSession)
                StatusChip("远控会话", if (health.hasActiveSession) "运行中" else "未运行", health.hasActiveSession)
                StatusChip("API", "v${health.apiVersion}", true)
                uiState.desktopCapabilities?.let { caps ->
                    StatusChip("平台", caps.platform, caps.supported)
                    StatusChip("截图", if (caps.supportsScreenshot) "支持" else "不支持", caps.supportsScreenshot)
                    StatusChip("鼠标", if (caps.supportsMouse) "支持" else "不支持", caps.supportsMouse)
                    StatusChip("键盘", if (caps.supportsKeyboard) "支持" else "不支持", caps.supportsKeyboard)
                }
            }
        }
        uiState.desktopActionMessage?.let {
            Spacer(modifier = Modifier.height(10.dp))
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
        uiState.desktopLastError?.let {
            Spacer(modifier = Modifier.height(10.dp))
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun DesktopSessionCard(
    uiState: RemoteControlUiState,
    onHeartbeat: () -> Unit,
    onCloseSession: () -> Unit,
    onQuerySession: () -> Unit
) {
    InfoCard(title = "电脑会话", icon = Icons.Default.Key) {
        val session = uiState.desktopSession
        if (session == null) {
            Text(
                text = "当前还没有连接到桌面会话。先测 health，再点打开会话。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            MetaRow(label = "Session ID", value = session.id)
            MetaRow(label = "Client", value = session.clientName)
            MetaRow(label = "Opened", value = session.openedAt)
            MetaRow(label = "Last Seen", value = session.lastSeenAt)
            MetaRow(label = "Expires", value = session.expiresAt)
        }
        Spacer(modifier = Modifier.height(12.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onHeartbeat, enabled = !uiState.desktopIsLoading) {
                Text("发送心跳")
            }
            OutlinedButton(onClick = onQuerySession, enabled = !uiState.desktopIsLoading) {
                Text("读取会话")
            }
            TextButton(onClick = onCloseSession, enabled = !uiState.desktopIsLoading) {
                Text("关闭会话")
            }
        }
    }
}

@Composable
private fun DesktopActionCard(
    uiState: RemoteControlUiState,
    onFetchScreenshot: () -> Unit,
    onTogglePreviewAutoRefresh: (Boolean) -> Unit,
    onMoveXChanged: (String) -> Unit,
    onMoveYChanged: (String) -> Unit,
    onTypeChanged: (String) -> Unit,
    onKeyChanged: (String) -> Unit,
    onMove: () -> Unit,
    onClick: () -> Unit,
    onType: () -> Unit,
    onKey: () -> Unit,
    onTouchpadMove: (Float, Float) -> Unit,
    onTouchpadTap: () -> Unit
) {
    InfoCard(title = "桌面预览与触控", icon = Icons.Default.Computer) {
        val imageBitmap = remember(uiState.desktopPreviewBytes, uiState.desktopScreenshot?.base64) {
            when {
                uiState.desktopPreviewBytes != null -> decodeDesktopScreenshot(uiState.desktopPreviewBytes)
                !uiState.desktopScreenshot?.base64.isNullOrEmpty() -> decodeDesktopScreenshot(
                    Base64.decode(uiState.desktopScreenshot!!.base64, Base64.DEFAULT)
                )
                else -> null
            }
        }
        val pointerOffset = remember { mutableStateOf(Offset.Zero) }
        uiState.desktopScreenshot?.let { screenshot ->
            MetaRow(label = "Screenshot", value = "${screenshot.width} x ${screenshot.height}")
            MetaRow(label = "Mime", value = screenshot.mimeType)
            MetaRow(label = "预览", value = if (uiState.desktopPreviewAutoRefresh) "自动刷新" else "手动刷新")
            Spacer(modifier = Modifier.height(8.dp))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "桌面实时预览",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    if (uiState.desktopStreaming) "流式预览中" else "自动刷新",
                    style = MaterialTheme.typography.bodySmall
                )
                Switch(
                    checked = uiState.desktopPreviewAutoRefresh,
                    onCheckedChange = onTogglePreviewAutoRefresh
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = "Desktop Preview",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            } else {
                Text(
                    text = "还没有桌面截图。先抓一次截图，或开启自动刷新后等待。",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "触控板",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f), RoundedCornerShape(18.dp))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            pointerOffset.value = it
                            onTouchpadTap()
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        pointerOffset.value = change.position
                        onTouchpadMove(dragAmount.x, dragAmount.y)
                    }
                }
        ) {
            Icon(
                imageVector = Icons.Default.Route,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "单击 = 左键，拖动 = 相对移动",
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (pointerOffset.value.x - 10f).toInt().coerceAtLeast(0),
                            (pointerOffset.value.y - 10f).toInt().coerceAtLeast(0)
                        )
                    }
                    .size(20.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.45f), RoundedCornerShape(999.dp))
                    .align(Alignment.TopStart)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onFetchScreenshot, enabled = !uiState.desktopIsLoading) {
                Text("抓桌面截图")
            }
            OutlinedButton(onClick = onClick, enabled = !uiState.desktopIsLoading) { Text("当前点左键") }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "兼容调试：绝对坐标",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = uiState.desktopMoveX,
                onValueChange = onMoveXChanged,
                modifier = Modifier.weight(1f),
                label = { Text("X") },
                singleLine = true
            )
            OutlinedTextField(
                value = uiState.desktopMoveY,
                onValueChange = onMoveYChanged,
                modifier = Modifier.weight(1f),
                label = { Text("Y") },
                singleLine = true
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onMove, enabled = !uiState.desktopIsLoading) { Text("移动鼠标") }
            OutlinedButton(onClick = onClick, enabled = !uiState.desktopIsLoading) { Text("绝对坐标点击") }
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = uiState.desktopTypeText,
            onValueChange = onTypeChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("发送文本") },
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = onType, enabled = !uiState.desktopIsLoading) {
            Text("发送文本")
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = uiState.desktopKeyText,
            onValueChange = onKeyChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("按键名，例如 ENTER / ESC / TAB") },
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = onKey, enabled = !uiState.desktopIsLoading) {
            Text("发送按键")
        }
    }
}

private fun decodeDesktopScreenshot(bytes: ByteArray) =
    runCatching {
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    }.getOrNull()

@Composable
private fun DesktopEventsCard(
    uiState: RemoteControlUiState,
    onFetchEvents: () -> Unit
) {
    InfoCard(
        title = "桌面事件",
        icon = Icons.Default.Route,
        action = {
            IconButton(onClick = onFetchEvents, enabled = !uiState.desktopIsLoading) {
                Icon(Icons.Default.Refresh, contentDescription = null)
            }
        }
    ) {
        Text(
            text = "这里直接看 MetaAgent-PC 最近事件，方便在手机上确认动作有没有真正打到电脑。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (uiState.desktopEvents.isEmpty()) {
            Text(
                text = "还没有事件。先打开会话或点一次桌面动作。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.desktopEvents.take(8).forEach { event ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = event.type,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = event.createdAt,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (event.data.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = event.data.entries.joinToString(" | ") { "${it.key}=${it.value}" },
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegacyPhoneServiceCard(
    uiState: RemoteControlUiState,
    onToggleService: () -> Unit,
    onRefresh: () -> Unit,
    onCopyAddress: () -> Unit
) {
    val host = uiState.localIpAddress ?: "未获取到局域网地址"
    InfoCard(
        title = "兼容模式：电脑控制手机",
        icon = Icons.Default.Devices,
        action = {
            IconButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, contentDescription = null)
            }
        }
    ) {
        Text(
            text = "保留原来的手机本地 HTTP 服务。只有你还需要电脑直接调手机 screenshot/input/agent/memory 时，才需要这一块。",
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

private fun copyText(context: Context, label: String, value: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, value))
    Toast.makeText(context, "已复制", Toast.LENGTH_SHORT).show()
}
