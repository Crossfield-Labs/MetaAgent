package com.ai.assistance.metaagent.ui.features.settings.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ai.assistance.metaagent.remote.RemoteAgentServer
import com.ai.assistance.metaagent.remote.RemoteAgentTaskManager
import com.ai.assistance.metaagent.remote.RemoteAgentTaskSnapshot
import com.ai.assistance.metaagent.remote.RemoteCapabilitiesPayload
import com.ai.assistance.metaagent.remote.RemoteControlService
import com.ai.assistance.metaagent.remote.RemoteRuntimeInspector
import com.ai.assistance.metaagent.remote.RemoteSessionManager
import com.ai.assistance.metaagent.remote.RemoteSessionPublicSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RemoteControlUiState(
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
    private val _uiState = MutableStateFlow(RemoteControlUiState())
    val uiState: StateFlow<RemoteControlUiState> = _uiState.asStateFlow()

    init {
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

    private fun com.ai.assistance.metaagent.remote.RemoteSessionSnapshot.toPublicSnapshot():
        RemoteSessionPublicSnapshot =
        RemoteSessionPublicSnapshot(
            sessionId = sessionId,
            clientName = clientName,
            createdAtEpochMs = createdAtEpochMs,
            lastSeenAtEpochMs = lastSeenAtEpochMs
        )
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
