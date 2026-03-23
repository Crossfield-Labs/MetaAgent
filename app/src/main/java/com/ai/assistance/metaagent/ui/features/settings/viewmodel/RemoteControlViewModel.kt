package com.ai.assistance.metaagent.ui.features.settings.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ai.assistance.metaagent.remote.DesktopCapabilitiesPayload
import com.ai.assistance.metaagent.remote.DesktopControlSessionPayload
import com.ai.assistance.metaagent.remote.DesktopEventPayload
import com.ai.assistance.metaagent.remote.DesktopHealthPayload
import com.ai.assistance.metaagent.remote.DesktopAgentLogEntryPayload
import com.ai.assistance.metaagent.remote.DesktopAgentSettingsPayload
import com.ai.assistance.metaagent.remote.DesktopAgentStatePayload
import com.ai.assistance.metaagent.remote.DesktopRemoteClient
import com.ai.assistance.metaagent.remote.DesktopRemoteConfig
import com.ai.assistance.metaagent.remote.DesktopScreenshotPayload
import com.ai.assistance.metaagent.remote.DesktopVideoSessionPayload
import com.ai.assistance.metaagent.remote.RemoteAgentServer
import com.ai.assistance.metaagent.remote.RemoteAgentTaskManager
import com.ai.assistance.metaagent.remote.RemoteAgentTaskSnapshot
import com.ai.assistance.metaagent.remote.RemoteCapabilitiesPayload
import com.ai.assistance.metaagent.remote.RemoteControlService
import com.ai.assistance.metaagent.remote.RemoteRuntimeInspector
import com.ai.assistance.metaagent.remote.RemoteSessionManager
import com.ai.assistance.metaagent.remote.RemoteSessionPublicSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RemoteControlUiState(
    val desktopHost: String = "",
    val desktopPort: String = "3210",
    val desktopToken: String = "",
    val desktopPairingId: String? = null,
    val desktopPairingStatus: String? = null,
    val desktopPassword: String = "",
    val desktopHealth: DesktopHealthPayload? = null,
    val desktopAgentSettings: DesktopAgentSettingsPayload? = null,
    val desktopAgentState: DesktopAgentStatePayload? = null,
    val desktopAgentLogs: List<DesktopAgentLogEntryPayload> = emptyList(),
    val desktopAgentProvider: String = "codex",
    val desktopAgentCwd: String = "",
    val desktopAgentPrompt: String = "请分析当前工程并给出下一步修改建议",
    val desktopVideoSession: DesktopVideoSessionPayload? = null,
    val desktopCapabilities: DesktopCapabilitiesPayload? = null,
    val desktopSession: DesktopControlSessionPayload? = null,
    val desktopEvents: List<DesktopEventPayload> = emptyList(),
    val desktopScreenshot: DesktopScreenshotPayload? = null,
    val desktopPreviewBytes: ByteArray? = null,
    val desktopActionMessage: String? = null,
    val desktopLastError: String? = null,
    val desktopIsLoading: Boolean = false,
    val desktopPreviewAutoRefresh: Boolean = false,
    val desktopTypeText: String = "MetaAgent-PC",
    val desktopKeyText: String = "ENTER",
    val isServiceRunning: Boolean = false,
    val localIpAddress: String? = null,
    val port: Int = RemoteAgentServer.DEFAULT_PORT,
    val capabilities: RemoteCapabilitiesPayload? = null,
    val session: RemoteSessionPublicSnapshot? = null,
    val debugToken: String? = null,
    val tasks: List<RemoteAgentTaskSnapshot> = emptyList(),
    val isRefreshing: Boolean = false
)

class RemoteControlViewModel(private val context: Context) : ViewModel() {
    private val preferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val desktopClient = DesktopRemoteClient()
    private var desktopHeartbeatJob: Job? = null
    private var touchpadFlushJob: Job? = null
    @Volatile
    private var pendingTouchpadDeltaX: Int = 0
    @Volatile
    private var pendingTouchpadDeltaY: Int = 0
    private val _uiState = MutableStateFlow(RemoteControlUiState())
    val uiState: StateFlow<RemoteControlUiState> = _uiState.asStateFlow()

    init {
        _uiState.value = _uiState.value.copy(
            desktopHost = preferences.getString(KEY_DESKTOP_HOST, "").orEmpty(),
            desktopPort = preferences.getString(KEY_DESKTOP_PORT, "3210").orEmpty(),
            desktopToken = preferences.getString(KEY_DESKTOP_TOKEN, "").orEmpty()
        )
        refresh()
    }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isRefreshing = true) }
            val activeSession = RemoteSessionManager.getActiveSession()
            _uiState.update {
                it.copy(
                    isServiceRunning = RemoteControlService.isRunning,
                    localIpAddress = RemoteRuntimeInspector.localIpv4Address(),
                    capabilities = RemoteRuntimeInspector.collectCapabilities(context),
                    session = activeSession?.toPublicSnapshot(),
                    debugToken = activeSession?.token,
                    tasks = RemoteAgentTaskManager.listTasks().take(20),
                    isRefreshing = false
                )
            }
        }
    }

    fun startService() {
        context.startForegroundService(RemoteControlService.buildStartIntent(context))
        refresh()
    }

    fun stopService() {
        context.startService(RemoteControlService.buildStopIntent(context))
        refresh()
    }

    fun createDebugSession(clientName: String = "phone-ui") {
        RemoteSessionManager.openSession(clientName)
        refresh()
    }

    fun closeSession() {
        RemoteSessionManager.closeSession(null)
        refresh()
    }

    fun cancelTask(taskId: String) {
        RemoteAgentTaskManager.cancelTask(taskId)
        refresh()
    }

    fun updateDesktopHost(value: String) {
        updateDesktopConfig(host = value)
    }

    fun updateDesktopPort(value: String) {
        updateDesktopConfig(port = value)
    }

    fun updateDesktopTypeText(value: String) {
        _uiState.update { it.copy(desktopTypeText = value) }
    }

    fun updateDesktopKeyText(value: String) {
        _uiState.update { it.copy(desktopKeyText = value) }
    }

    fun updateDesktopAgentPrompt(value: String) {
        _uiState.update { it.copy(desktopAgentPrompt = value) }
    }

    fun updateDesktopAgentProvider(value: String) {
        _uiState.update { it.copy(desktopAgentProvider = value.trim().ifBlank { "codex" }) }
    }

    fun updateDesktopAgentCwd(value: String) {
        _uiState.update { it.copy(desktopAgentCwd = value) }
    }

    fun setDesktopPreviewAutoRefresh(enabled: Boolean) {
        _uiState.update { it.copy(desktopPreviewAutoRefresh = enabled) }
    }

    fun updateDesktopPassword(value: String) {
        _uiState.update { it.copy(desktopPassword = value) }
    }

    fun checkDesktopHealth() {
        runDesktopAction {
            val config = currentDesktopConfig()
            val health = desktopClient.health(config)
            val capabilities = desktopClient.capabilities(config)
            _uiState.update {
                it.copy(
                    desktopHealth = health,
                    desktopAgentSettings = health.agent?.settings,
                    desktopAgentState = health.agent?.state,
                    desktopAgentProvider = health.agent?.settings?.provider ?: it.desktopAgentProvider,
                    desktopAgentCwd = health.agent?.settings?.cwd ?: it.desktopAgentCwd,
                    desktopVideoSession = health.video?.session,
                    desktopCapabilities = capabilities,
                    desktopActionMessage = "已连接到 ${health.service}",
                    desktopLastError = null
                )
            }
        }
    }

    fun queryDesktopSession() {
        runDesktopAction {
            val session = desktopClient.getSession(currentDesktopConfig()).session
            _uiState.update {
                it.copy(
                    desktopSession = session,
                    desktopActionMessage = if (session != null) "已读取远端会话" else "远端当前没有活跃会话",
                    desktopLastError = null
                )
            }
        }
    }

    fun requestDesktopConnection(deviceName: String = "MetaAgent Android") {
        runDesktopAction {
            val payload = desktopClient.requestPairing(currentDesktopConfig(), deviceName)
            _uiState.update {
                it.copy(
                    desktopPairingId = payload.request.pairingId,
                    desktopPairingStatus = payload.request.status,
                    desktopToken = "",
                    desktopSession = null,
                    desktopActionMessage = if (payload.request.status == "approved") {
                        "电脑已同意连接，请输入密码"
                    } else {
                        "连接请求已发送，请在电脑端确认"
                    },
                    desktopLastError = null
                )
            }
            updateDesktopConfig(token = "")
        }
    }

    fun refreshDesktopPairingStatus() {
        runDesktopAction {
            val pairingId = _uiState.value.desktopPairingId ?: error("当前没有待处理的连接请求")
            val payload = desktopClient.pairingStatus(currentDesktopConfig(), pairingId)
            _uiState.update {
                it.copy(
                    desktopPairingStatus = payload.request.status,
                    desktopActionMessage = when (payload.request.status) {
                        "approved" -> "电脑已确认，请输入密码"
                        "rejected" -> "电脑已拒绝本次连接"
                        "expired" -> "连接请求已过期，请重新发起"
                        else -> "仍在等待电脑端确认"
                    },
                    desktopLastError = null
                )
            }
        }
    }

    fun confirmDesktopPassword() {
        runDesktopAction {
            val state = _uiState.value
            val pairingId = state.desktopPairingId ?: error("当前没有连接请求")
            val payload = desktopClient.authenticatePairing(
                config = currentDesktopConfig(),
                pairingId = pairingId,
                password = state.desktopPassword,
                clientName = "MetaAgent-Android"
            )
            _uiState.update {
                it.copy(
                    desktopToken = payload.sessionToken,
                    desktopSession = payload.session,
                    desktopPairingStatus = "connected",
                    desktopActionMessage = "电脑连接已建立",
                    desktopLastError = null
                )
            }
            updateDesktopConfig(token = payload.sessionToken)
        }
    }

    fun openDesktopSession(clientName: String = "MetaAgent-Android") {
        runDesktopAction {
            val payload = desktopClient.openSession(currentDesktopConfig(), clientName)
            _uiState.update {
                it.copy(
                    desktopSession = payload.session,
                    desktopActionMessage = "桌面会话已打开",
                    desktopLastError = null
                )
            }
        }
    }

    fun heartbeatDesktopSession() {
        runDesktopAction {
            val payload = desktopClient.heartbeat(
                currentDesktopConfig(),
                _uiState.value.desktopSession?.id
            )
            _uiState.update {
                it.copy(
                    desktopSession = payload.session,
                    desktopActionMessage = if (payload.session != null) "桌面会话已续期" else "远端没有活跃会话",
                    desktopLastError = null
                )
            }
        }
    }

    fun closeDesktopSession() {
        runDesktopAction {
            val payload = desktopClient.closeSession(
                currentDesktopConfig(),
                _uiState.value.desktopSession?.id
            )
            _uiState.update {
                it.copy(
                    desktopSession = payload.session,
                    desktopActionMessage = "桌面会话已关闭",
                    desktopLastError = null
                )
            }
        }
    }

    fun startDesktopHeartbeat() {
        if (desktopHeartbeatJob?.isActive == true) return
        desktopHeartbeatJob = viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                delay(15000)
                val sessionId = _uiState.value.desktopSession?.id ?: break
                runCatching {
                    val payload = desktopClient.heartbeat(currentDesktopConfig(), sessionId)
                    _uiState.update {
                        it.copy(
                            desktopSession = payload.session,
                            desktopLastError = null
                        )
                    }
                }.onFailure { error ->
                    _uiState.update {
                        it.copy(desktopLastError = error.message ?: error.javaClass.simpleName)
                    }
                }
            }
        }
    }

    fun stopDesktopHeartbeat() {
        desktopHeartbeatJob?.cancel()
        desktopHeartbeatJob = null
    }

    fun fetchDesktopEvents(limit: Int = 20) {
        runDesktopAction {
            val payload = desktopClient.events(currentDesktopConfig(), limit)
            _uiState.update {
                it.copy(
                    desktopEvents = payload.events.reversed(),
                    desktopActionMessage = "已拉取 ${payload.events.size} 条事件",
                    desktopLastError = null
                )
            }
        }
    }

    fun pollDesktopPairingStatus() {
        viewModelScope.launch(Dispatchers.IO) {
            val pairingId = _uiState.value.desktopPairingId ?: return@launch
            runCatching {
                val payload = desktopClient.pairingStatus(currentDesktopConfig(), pairingId)
                _uiState.update {
                    it.copy(
                        desktopPairingStatus = payload.request.status,
                        desktopLastError = null
                    )
                }
            }
        }
    }

    fun fetchDesktopAgentState() {
        runDesktopAction {
            val payload = desktopClient.agentState(currentDesktopConfig())
            _uiState.update {
                it.copy(
                    desktopAgentSettings = payload.settings,
                    desktopAgentState = payload.state,
                    desktopActionMessage = "已刷新桌面 Agent 状态",
                    desktopLastError = null
                )
            }
        }
    }

    fun pollDesktopRuntime() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val config = currentDesktopConfig()
                val statePayload = desktopClient.agentState(config)
                val logsPayload = desktopClient.agentLogs(config, 20)
                val eventsPayload = desktopClient.events(config, 12)
                val videoPayload = desktopClient.videoSession(config)
                _uiState.update {
                    it.copy(
                        desktopAgentSettings = statePayload.settings,
                        desktopAgentState = statePayload.state,
                        desktopAgentProvider = statePayload.settings.provider,
                        desktopAgentCwd = statePayload.settings.cwd,
                        desktopAgentLogs = logsPayload.logs.reversed(),
                        desktopVideoSession = videoPayload.session,
                        desktopEvents = eventsPayload.events.reversed(),
                        desktopLastError = null
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(desktopLastError = error.message ?: error.javaClass.simpleName)
                }
            }
        }
    }

    fun fetchDesktopAgentLogs(limit: Int = 80) {
        runDesktopAction {
            val payload = desktopClient.agentLogs(currentDesktopConfig(), limit)
            _uiState.update {
                it.copy(
                    desktopAgentLogs = payload.logs.reversed(),
                    desktopActionMessage = "已拉取 ${payload.logs.size} 条 Agent 日志",
                    desktopLastError = null
                )
            }
        }
    }

    fun runDesktopAgent() {
        runDesktopAction {
            val prompt = _uiState.value.desktopAgentPrompt.trim()
            require(prompt.isNotEmpty()) { "请先输入要发送到电脑的 Agent 任务" }
            val provider = _uiState.value.desktopAgentProvider.trim().ifBlank { "codex" }
            val cwd = _uiState.value.desktopAgentCwd.trim().ifBlank { null }
            val payload = desktopClient.runAgent(
                currentDesktopConfig(),
                prompt = prompt,
                provider = provider,
                cwd = cwd
            )
            _uiState.update {
                it.copy(
                    desktopAgentSettings = payload.settings,
                    desktopAgentState = payload.state,
                    desktopAgentProvider = payload.settings.provider,
                    desktopAgentCwd = payload.settings.cwd,
                    desktopActionMessage = "已向电脑端发送 ${payload.settings.provider} 任务",
                    desktopLastError = null
                )
            }
        }
    }

    fun openDesktopVideoSession() {
        runDesktopAction {
            val payload = desktopClient.openVideoSession(currentDesktopConfig())
            _uiState.update {
                it.copy(
                    desktopVideoSession = payload.session,
                    desktopActionMessage = "桌面视频流会话已创建",
                    desktopLastError = null
                )
            }
        }
    }

    fun closeDesktopVideoSession() {
        runDesktopAction {
            val payload = desktopClient.closeVideoSession(currentDesktopConfig())
            _uiState.update {
                it.copy(
                    desktopVideoSession = null,
                    desktopActionMessage = payload.message,
                    desktopLastError = null
                )
            }
        }
    }

    fun stopDesktopAgent() {
        runDesktopAction {
            val payload = desktopClient.stopAgent(currentDesktopConfig())
            _uiState.update {
                it.copy(
                    desktopActionMessage = payload.message,
                    desktopLastError = null
                )
            }
        }
    }

    fun fetchDesktopScreenshot() {
        runDesktopAction {
            val screenshot = desktopClient.screenshot(currentDesktopConfig())
            _uiState.update {
                it.copy(
                    desktopScreenshot = screenshot,
                    desktopPreviewBytes = android.util.Base64.decode(screenshot.base64, android.util.Base64.DEFAULT),
                    desktopActionMessage = "截图成功 ${screenshot.width}x${screenshot.height}",
                    desktopLastError = null
                )
            }
        }
    }

    fun touchpadMove(deltaX: Float, deltaY: Float) {
        val scaledX = (deltaX * 1.35f).toInt()
        val scaledY = (deltaY * 1.35f).toInt()
        if (scaledX == 0 && scaledY == 0) return

        pendingTouchpadDeltaX += scaledX
        pendingTouchpadDeltaY += scaledY

        if (touchpadFlushJob?.isActive == true) return
        touchpadFlushJob = viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                delay(16)
                val deltaXToSend = pendingTouchpadDeltaX
                val deltaYToSend = pendingTouchpadDeltaY
                pendingTouchpadDeltaX = 0
                pendingTouchpadDeltaY = 0
                if (deltaXToSend == 0 && deltaYToSend == 0) break

                val result = runCatching {
                    desktopClient.moveRelative(currentDesktopConfig(), deltaXToSend, deltaYToSend)
                }
                if (result.isFailure) {
                    val error = result.exceptionOrNull()
                    val message = error?.message ?: error?.javaClass?.simpleName ?: "Unknown error"
                    _uiState.update {
                        it.copy(desktopLastError = message)
                    }
                    break
                }
            }
        }
    }

    fun touchpadTap() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val result = desktopClient.click(currentDesktopConfig())
                _uiState.update {
                    it.copy(desktopActionMessage = result.message, desktopLastError = null)
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(desktopLastError = error.message ?: error.javaClass.simpleName)
                }
            }
        }
    }

    fun testDesktopType() {
        runDesktopAction {
            val text = _uiState.value.desktopTypeText.trim()
            require(text.isNotEmpty()) { "请先输入要发送的文本" }
            val result = desktopClient.type(currentDesktopConfig(), text)
            _uiState.update {
                it.copy(desktopActionMessage = result.message, desktopLastError = null)
            }
        }
    }

    fun testDesktopKey() {
        runDesktopAction {
            val key = _uiState.value.desktopKeyText.trim()
            require(key.isNotEmpty()) { "请先输入按键名" }
            val result = desktopClient.key(currentDesktopConfig(), key)
            _uiState.update {
                it.copy(desktopActionMessage = result.message, desktopLastError = null)
            }
        }
    }

    private fun com.ai.assistance.metaagent.remote.RemoteSessionSnapshot.toPublicSnapshot():
        RemoteSessionPublicSnapshot =
        RemoteSessionPublicSnapshot(
            sessionId = sessionId,
            clientName = clientName,
            createdAtEpochMs = createdAtEpochMs,
            lastSeenAtEpochMs = lastSeenAtEpochMs
        )

    private fun updateDesktopConfig(
        host: String? = null,
        port: String? = null,
        token: String? = null
    ) {
        _uiState.update {
            it.copy(
                desktopHost = host ?: it.desktopHost,
                desktopPort = port ?: it.desktopPort,
                desktopToken = token ?: it.desktopToken
            )
        }
        preferences.edit()
            .putString(KEY_DESKTOP_HOST, _uiState.value.desktopHost)
            .putString(KEY_DESKTOP_PORT, _uiState.value.desktopPort)
            .putString(KEY_DESKTOP_TOKEN, _uiState.value.desktopToken)
            .apply()
    }

    private fun currentDesktopConfig(): DesktopRemoteConfig {
        val state = _uiState.value
        return DesktopRemoteConfig(
            host = state.desktopHost,
            port = state.desktopPort,
            token = state.desktopToken
        )
    }

    private fun runDesktopAction(action: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(
                    desktopIsLoading = true,
                    desktopLastError = null
                )
            }
            runCatching { action() }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            desktopLastError = error.message ?: error.javaClass.simpleName,
                            desktopActionMessage = null
                        )
                    }
                }
            _uiState.update { it.copy(desktopIsLoading = false) }
        }
    }

    override fun onCleared() {
        stopDesktopHeartbeat()
        touchpadFlushJob?.cancel()
        super.onCleared()
    }

    companion object {
        private const val PREFS_NAME = "remote_control_desktop"
        private const val KEY_DESKTOP_HOST = "desktop_host"
        private const val KEY_DESKTOP_PORT = "desktop_port"
        private const val KEY_DESKTOP_TOKEN = "desktop_token"
    }
}

class RemoteControlViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RemoteControlViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RemoteControlViewModel(context.applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
