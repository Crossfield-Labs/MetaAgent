package com.ai.assistance.metaagent.ui.features.settings.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ai.assistance.metaagent.core.plan.PcAgentConnectionConfig
import com.ai.assistance.metaagent.core.plan.PcAgentConnectionPrefs
import com.ai.assistance.metaagent.core.plan.PcAgentWsClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PcAgentConnectionUiState(
    val host: String = "",
    val port: String = "3210",
    val token: String = "",
    val isSaving: Boolean = false,
    val isTesting: Boolean = false,
    val endpointPreview: String = "",
    val statusMessage: String? = null,
    val lastError: String? = null,
    val lastTestOutput: String? = null
)

class PcAgentConnectionViewModel(
    private val context: Context
) : ViewModel() {
    private val preferences: SharedPreferences =
        context.getSharedPreferences(PcAgentConnectionPrefs.PREFS_NAME, Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(
        PcAgentConnectionUiState(
            host = preferences.getString(PcAgentConnectionPrefs.KEY_HOST, "").orEmpty(),
            port = preferences.getString(PcAgentConnectionPrefs.KEY_PORT, "3210").orEmpty(),
            token = preferences.getString(PcAgentConnectionPrefs.KEY_TOKEN, "").orEmpty()
        )
    )
    val uiState: StateFlow<PcAgentConnectionUiState> = _uiState.asStateFlow()

    init {
        refreshEndpointPreview()
    }

    fun updateHost(value: String) {
        _uiState.update { it.copy(host = value, statusMessage = null, lastError = null) }
        refreshEndpointPreview()
    }

    fun updatePort(value: String) {
        _uiState.update { it.copy(port = value, statusMessage = null, lastError = null) }
        refreshEndpointPreview()
    }

    fun updateToken(value: String) {
        _uiState.update { it.copy(token = value, statusMessage = null, lastError = null) }
    }

    fun saveConfig() {
        val snapshot = _uiState.value
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isSaving = true, statusMessage = null, lastError = null) }
            runCatching {
                require(snapshot.host.trim().isNotEmpty()) { "请先填写电脑 Host" }
                val port = snapshot.port.trim().ifBlank { "3210" }
                require(port.toIntOrNull() != null) { "端口格式不正确" }

                preferences.edit()
                    .putString(PcAgentConnectionPrefs.KEY_HOST, snapshot.host.trim())
                    .putString(PcAgentConnectionPrefs.KEY_PORT, port)
                    .putString(PcAgentConnectionPrefs.KEY_TOKEN, snapshot.token.trim())
                    .apply()
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        port = snapshot.port.trim().ifBlank { "3210" },
                        statusMessage = "编排连接配置已保存",
                        lastError = null
                    )
                }
                refreshEndpointPreview()
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        lastError = error.message ?: "保存失败"
                    )
                }
            }
        }
    }

    fun testConnection() {
        val snapshot = _uiState.value
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(
                    isTesting = true,
                    statusMessage = "正在测试 PC Agent 编排连接",
                    lastError = null,
                    lastTestOutput = null
                )
            }
            runCatching {
                require(snapshot.host.trim().isNotEmpty()) { "请先填写电脑 Host" }
                val port = snapshot.port.trim().ifBlank { "3210" }
                require(port.toIntOrNull() != null) { "端口格式不正确" }

                val client = PcAgentWsClient(context)
                client.testConnection(
                    connectionOverride = PcAgentConnectionConfig(
                        host = snapshot.host.trim(),
                        port = port,
                        token = snapshot.token.trim()
                    )
                )
            }.onSuccess { result ->
                if (result.success) {
                    val output = result.result.ifBlank { result.finalMessage }.ifBlank { "pc-agent-connection-ok" }
                    _uiState.update {
                        it.copy(
                            isTesting = false,
                            statusMessage = "PC Agent 编排连接测试通过",
                            lastError = null,
                            lastTestOutput = output
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isTesting = false,
                            statusMessage = null,
                            lastError = result.error.ifBlank { "连接测试失败" },
                            lastTestOutput = result.finalMessage.takeIf { message -> message.isNotBlank() }
                        )
                    }
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isTesting = false,
                        statusMessage = null,
                        lastError = error.message ?: "连接测试失败"
                    )
                }
            }
        }
    }

    private fun refreshEndpointPreview() {
        val state = _uiState.value
        val host = state.host.trim()
        val preview = when {
            host.isBlank() -> "ws://<host>:3210/ws/pc-agent"
            (host.startsWith("ws://") || host.startsWith("wss://")) && host.contains("/ws/pc-agent") -> host
            host.startsWith("ws://") || host.startsWith("wss://") -> host.trimEnd('/') + "/ws/pc-agent"
            host.startsWith("http://") && host.contains("/ws/pc-agent") -> host.replaceFirst("http://", "ws://")
            host.startsWith("http://") -> host.replaceFirst("http://", "ws://").trimEnd('/') + "/ws/pc-agent"
            host.startsWith("https://") && host.contains("/ws/pc-agent") -> host.replaceFirst("https://", "wss://")
            host.startsWith("https://") -> host.replaceFirst("https://", "wss://").trimEnd('/') + "/ws/pc-agent"
            else -> "ws://$host:${state.port.trim().ifBlank { "3210" }}/ws/pc-agent"
        }
        _uiState.update { it.copy(endpointPreview = preview) }
    }
}

class PcAgentConnectionViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PcAgentConnectionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PcAgentConnectionViewModel(context.applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
