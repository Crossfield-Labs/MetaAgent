@file:OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.ai.assistance.metaagent.ui.features.settings.screens

import android.graphics.BitmapFactory
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Base64
import android.widget.Toast
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.ui.viewinterop.AndroidView
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
                DesktopAgentCard(
                    uiState = uiState,
                    onProviderChanged = viewModel::updateDesktopAgentProvider,
                    onCwdChanged = viewModel::updateDesktopAgentCwd,
                    onPromptChanged = viewModel::updateDesktopAgentPrompt,
                    onRun = { viewModel.runDesktopAgent() },
                    onRefreshState = { viewModel.fetchDesktopAgentState() },
                    onFetchLogs = { viewModel.fetchDesktopAgentLogs() },
                    onStop = { viewModel.stopDesktopAgent() }
                )
            }

            item {
                DesktopVideoCard(
                    uiState = uiState,
                    onOpen = { viewModel.openDesktopVideoSession() },
                    onClose = { viewModel.closeDesktopVideoSession() },
                    onRefresh = { viewModel.pollDesktopRuntime() }
                )
            }

            item {
                DesktopActionCard(
                    uiState = uiState,
                    onFetchScreenshot = { viewModel.fetchDesktopScreenshot() },
                    onTypeChanged = viewModel::updateDesktopTypeText,
                    onKeyChanged = viewModel::updateDesktopKeyText,
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
        uiState.desktopPairingId,
        uiState.desktopPairingStatus,
        uiState.desktopHost,
        uiState.desktopPort
    ) {
        val pairingStatus = uiState.desktopPairingStatus
        if (uiState.desktopPairingId != null && pairingStatus != null && pairingStatus !in setOf("approved", "connected", "rejected", "expired")) {
            while (true) {
                delay(2000)
                viewModel.pollDesktopPairingStatus()
            }
        }
    }

    LaunchedEffect(
        uiState.desktopToken,
        uiState.desktopSession?.id,
        uiState.desktopHost,
        uiState.desktopPort
    ) {
        viewModel.stopDesktopHeartbeat()
        if (uiState.desktopToken.isNotBlank() && uiState.desktopSession != null) {
            viewModel.startDesktopHeartbeat()
            while (true) {
                viewModel.pollDesktopRuntime()
                delay(2500)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopDesktopHeartbeat()
        }
    }
}

@Composable
private fun DesktopVideoCard(
    uiState: RemoteControlUiState,
    onOpen: () -> Unit,
    onClose: () -> Unit,
    onRefresh: () -> Unit
) {
    InfoCard(title = "远程桌面视频流", icon = Icons.Default.Computer) {
        Text(
            text = "这里是独立的视频流链路，不再和截图轮询混在一起。当前这轮已经把视频会话、WebRTC 信令壳和状态跟踪拆出来，后面会直接替换成真正的桌面采集与编码管线。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        val video = uiState.desktopVideoSession
        if (video == null) {
            Text(
                text = "当前没有活跃的视频流会话。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            MetaRow(label = "Session", value = video.id)
            MetaRow(label = "Viewer", value = video.viewerName)
            MetaRow(label = "Transport", value = video.transport)
            MetaRow(label = "Codec", value = video.codec)
            MetaRow(label = "状态", value = video.status)
            MetaRow(label = "分辨率", value = "${video.preferredWidth} x ${video.preferredHeight}")
            MetaRow(label = "FPS", value = video.preferredFps.toString())
            MetaRow(label = "Candidates", value = video.candidateCount.toString())
            video.lastError?.takeIf { it.isNotBlank() }?.let {
                MetaRow(label = "Error", value = it)
            }
            if (video.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = video.notes.takeLast(3).joinToString("\n"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onOpen, enabled = !uiState.desktopIsLoading) {
                Text("创建视频会话")
            }
            OutlinedButton(onClick = onRefresh, enabled = !uiState.desktopIsLoading) {
                Text("刷新视频状态")
            }
            TextButton(onClick = onClose, enabled = !uiState.desktopIsLoading && uiState.desktopVideoSession != null) {
                Text("关闭视频会话")
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        DesktopVideoWebView(
            uiState = uiState,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(
                    run {
                        val width = video?.preferredWidth?.takeIf { it > 0 } ?: 16
                        val height = video?.preferredHeight?.takeIf { it > 0 } ?: 9
                        width.toFloat() / height.toFloat()
                    }
                )
        )
    }
}

@Composable
private fun DesktopVideoWebView(
    uiState: RemoteControlUiState,
    modifier: Modifier = Modifier
) {
    val baseUrl = remember(uiState.desktopHost, uiState.desktopPort) {
        val host = uiState.desktopHost.trim()
        val port = uiState.desktopPort.trim().ifBlank { "3210" }
        if (host.isBlank()) null else "http://$host:$port"
    }
    val token = uiState.desktopToken
    val videoSession = uiState.desktopVideoSession

    if (baseUrl == null || videoSession == null) {
        Box(
            modifier = modifier
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "创建视频会话后，这里会直接显示电脑桌面视频流。",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val configHash = remember(baseUrl, token, videoSession.id) {
        val json = org.json.JSONObject()
            .put("baseUrl", baseUrl)
            .put("token", token)
            .put("sessionId", videoSession.id)
            .toString()
        java.net.URLEncoder.encode(json, Charsets.UTF_8.name())
    }

    AndroidView(
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp)),
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                settings.domStorageEnabled = true
                settings.cacheMode = WebSettings.LOAD_NO_CACHE
                webChromeClient = WebChromeClient()
                tag = configHash
                loadUrl("file:///android_asset/desktop_viewer.html#$configHash")
            }
        },
        update = { webView ->
            if (webView.tag != configHash) {
                webView.tag = configHash
                webView.loadUrl("file:///android_asset/desktop_viewer.html#$configHash")
            }
        }
    )
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
    onTypeChanged: (String) -> Unit,
    onKeyChanged: (String) -> Unit,
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
            Spacer(modifier = Modifier.height(8.dp))
        }
        Text(
            text = "静态截图预览",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(
                    run {
                        val width = uiState.desktopScreenshot?.width?.takeIf { it > 0 } ?: 16
                        val height = uiState.desktopScreenshot?.height?.takeIf { it > 0 } ?: 9
                        width.toFloat() / height.toFloat()
                    }
                )
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
                    text = "这里只有手动抓取的静态截图。实时桌面请看上面的“远程桌面视频流”。",
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

@Composable
private fun DesktopAgentCard(
    uiState: RemoteControlUiState,
    onProviderChanged: (String) -> Unit,
    onCwdChanged: (String) -> Unit,
    onPromptChanged: (String) -> Unit,
    onRun: () -> Unit,
    onRefreshState: () -> Unit,
    onFetchLogs: () -> Unit,
    onStop: () -> Unit
) {
    InfoCard(title = "电脑端 Agent", icon = Icons.Default.Hub) {
        Text(
            text = "这条链是“手机给电脑发任务，电脑本地用 Codex/Claude CLI 执行”。当前优先走 Codex。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        uiState.desktopAgentSettings?.let { settings ->
            MetaRow(label = "Provider", value = settings.provider)
            MetaRow(label = "Executable", value = settings.executable.ifBlank { "(default)" })
            MetaRow(label = "Args", value = settings.args.ifBlank { "(default)" })
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = uiState.desktopAgentProvider,
            onValueChange = onProviderChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Provider，例如 codex / claude") },
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = uiState.desktopAgentCwd,
            onValueChange = onCwdChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("电脑端执行目录，可留空") },
            singleLine = true
        )
        uiState.desktopAgentState?.let { state ->
            Spacer(modifier = Modifier.height(8.dp))
            MetaRow(label = "状态", value = state.status)
            MetaRow(label = "PID", value = state.pid?.toString() ?: "无")
            MetaRow(label = "Started", value = state.startedAt ?: "未开始")
            state.lastOutput?.takeIf { it.isNotBlank() }?.let {
                MetaRow(label = "Last", value = it)
            }
            state.lastError?.takeIf { it.isNotBlank() }?.let {
                MetaRow(label = "Error", value = it)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = uiState.desktopAgentPrompt,
            onValueChange = onPromptChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("发给电脑端 Agent 的任务") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onRun, enabled = !uiState.desktopIsLoading) { Text("运行 Codex 任务") }
            OutlinedButton(onClick = onRefreshState, enabled = !uiState.desktopIsLoading) { Text("刷新状态") }
            OutlinedButton(onClick = onFetchLogs, enabled = !uiState.desktopIsLoading) { Text("查看日志") }
            TextButton(onClick = onStop, enabled = !uiState.desktopIsLoading) { Text("停止") }
        }
        if (uiState.desktopAgentLogs.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.desktopAgentLogs.take(10).forEach { log ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "${log.stream} · ${log.at}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = log.line, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
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
