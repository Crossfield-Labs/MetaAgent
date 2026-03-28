package com.ai.assistance.metaagent.core.plan

import android.content.Context
import com.ai.assistance.metaagent.util.AppLogger
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

private const val TAG = "PcAgentWsClient"
private const val DEFAULT_PC_AGENT_PATH = "/ws/pc-agent"

@Serializable
data class PcSessionStartRequest(
    val runner: String,
    val workspace: String = "",
    val task: String,
    val goal: String = "",
    val command: String? = null
)

@Serializable
private data class PcSessionRequestFrame(
    val type: String = "req",
    val id: String = UUID.randomUUID().toString().take(8),
    val method: String,
    val taskId: String,
    val nodeId: String,
    val sessionId: String? = null,
    val params: JsonObject = buildJsonObject {}
)

data class PcSessionEvent(
    val event: String,
    val taskId: String?,
    val nodeId: String?,
    val sessionId: String?,
    val message: String = "",
    val progress: Float? = null,
    val runner: String? = null,
    val result: String = "",
    val error: String = "",
    val inputMode: String? = null
)

data class PcSessionExecutionResult(
    val success: Boolean,
    val sessionId: String?,
    val result: String = "",
    val error: String = "",
    val finalMessage: String = "",
    val awaitingUser: Boolean = false,
    val awaitingUserPrompt: String = "",
    val inputMode: String? = null
)

data class PcAgentConnectionConfig(
    val host: String,
    val port: String,
    val token: String
)

class PcAgentWsClient(
    private val context: Context,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
) {
    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .writeTimeout(8, TimeUnit.SECONDS)
            .build()
    }

    suspend fun execute(
        taskId: String,
        nodeId: String,
        startRequest: PcSessionStartRequest,
        connectionOverride: PcAgentConnectionConfig? = null,
        endpointOverride: String? = null,
        onEvent: (PcSessionEvent) -> Unit = {}
    ): PcSessionExecutionResult {
        val params = buildJsonObject {
            put("runner", JsonPrimitive(startRequest.runner))
            put("workspace", JsonPrimitive(startRequest.workspace))
            put("task", JsonPrimitive(startRequest.task))
            put("goal", JsonPrimitive(startRequest.goal))
            startRequest.command?.takeIf { it.isNotBlank() }?.let { command ->
                put("command", JsonPrimitive(command))
            }
        }
        return runRequest(
            frame = PcSessionRequestFrame(
                method = "pc.session.start",
                taskId = taskId,
                nodeId = nodeId,
                params = params
            ),
            connectionOverride = connectionOverride,
            endpointOverride = endpointOverride,
            onEvent = onEvent
        )
    }

    suspend fun submitUserInput(
        taskId: String,
        nodeId: String,
        sessionId: String,
        userInput: String,
        connectionOverride: PcAgentConnectionConfig? = null,
        endpointOverride: String? = null,
        onEvent: (PcSessionEvent) -> Unit = {}
    ): PcSessionExecutionResult {
        val params = buildJsonObject {
            put("text", JsonPrimitive(userInput))
        }
        return runRequest(
            frame = PcSessionRequestFrame(
                method = "pc.session.input",
                taskId = taskId,
                nodeId = nodeId,
                sessionId = sessionId,
                params = params
            ),
            connectionOverride = connectionOverride,
            endpointOverride = endpointOverride,
            onEvent = onEvent
        )
    }

    suspend fun testConnection(
        connectionOverride: PcAgentConnectionConfig? = null
    ): PcSessionExecutionResult {
        return execute(
            taskId = "pc-agent-test",
            nodeId = "pc-agent-test",
            startRequest = PcSessionStartRequest(
                runner = "shell",
                task = "Verify PC agent websocket connectivity",
                goal = "Verify PC agent websocket connectivity",
                command = "Write-Output 'pc-agent-connection-ok'"
            ),
            connectionOverride = connectionOverride
        )
    }

    private suspend fun runRequest(
        frame: PcSessionRequestFrame,
        connectionOverride: PcAgentConnectionConfig? = null,
        endpointOverride: String? = null,
        onEvent: (PcSessionEvent) -> Unit = {}
    ): PcSessionExecutionResult = suspendCancellableCoroutine { continuation ->
        val config = connectionOverride ?: loadConnectionConfig()
        val url = endpointOverride?.trim().takeUnless { it.isNullOrBlank() }
            ?: buildWsUrl(config)

        if (url.isBlank()) {
            continuation.resume(
                PcSessionExecutionResult(
                    success = false,
                    sessionId = null,
                    error = "电脑端地址未配置，请先在设置中填写 PC Agent 编排连接。"
                )
            )
            return@suspendCancellableCoroutine
        }

        val payload = json.encodeToString(frame)
        var terminalSent = false
        lateinit var socketRef: WebSocket

        val request = Request.Builder()
            .url(url)
            .apply {
                if (config.token.isNotBlank()) {
                    addHeader("X-MetaAgent-Token", config.token)
                }
            }
            .build()

        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                webSocket.send(payload)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                runCatching { parseEvent(text) }
                    .onSuccess { event ->
                        onEvent(event)
                        when (event.event) {
                            "pc.session.await_user" -> {
                                if (!terminalSent && continuation.isActive) {
                                    terminalSent = true
                                    continuation.resume(
                                        PcSessionExecutionResult(
                                            success = true,
                                            sessionId = event.sessionId,
                                            awaitingUser = true,
                                            awaitingUserPrompt = event.message,
                                            inputMode = event.inputMode,
                                            finalMessage = event.message
                                        )
                                    )
                                }
                                webSocket.close(1000, "await-user")
                            }

                            "pc.session.completed" -> {
                                if (!terminalSent && continuation.isActive) {
                                    terminalSent = true
                                    continuation.resume(
                                        PcSessionExecutionResult(
                                            success = true,
                                            sessionId = event.sessionId,
                                            result = event.result.ifBlank { event.message },
                                            finalMessage = event.message
                                        )
                                    )
                                }
                                webSocket.close(1000, "completed")
                            }

                            "pc.session.failed" -> {
                                if (!terminalSent && continuation.isActive) {
                                    terminalSent = true
                                    continuation.resume(
                                        PcSessionExecutionResult(
                                            success = false,
                                            sessionId = event.sessionId,
                                            error = event.error.ifBlank { event.message },
                                            finalMessage = event.message
                                        )
                                    )
                                }
                                webSocket.close(1000, "failed")
                            }
                        }
                    }
                    .onFailure { error ->
                        AppLogger.e(TAG, "Failed to parse PC session event", error)
                        if (!terminalSent && continuation.isActive) {
                            terminalSent = true
                            continuation.resume(
                                PcSessionExecutionResult(
                                    success = false,
                                    sessionId = null,
                                    error = error.message ?: "PC session response parse failed."
                                )
                            )
                        }
                        webSocket.cancel()
                    }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                AppLogger.e(TAG, "PC session websocket failed", t)
                if (!terminalSent && continuation.isActive) {
                    terminalSent = true
                    continuation.resume(
                        PcSessionExecutionResult(
                            success = false,
                            sessionId = null,
                            error = t.message ?: "PC session connection failed."
                        )
                    )
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                if (!terminalSent && continuation.isActive) {
                    terminalSent = true
                    continuation.resume(
                        PcSessionExecutionResult(
                            success = false,
                            sessionId = null,
                            error = if (reason.isBlank()) "PC session connection closed." else reason
                        )
                    )
                }
            }
        }

        socketRef = httpClient.newWebSocket(request, listener)
        continuation.invokeOnCancellation {
            socketRef.cancel()
        }
    }

    private fun parseEvent(text: String): PcSessionEvent {
        val root = json.parseToJsonElement(text).jsonObject
        val payload = root["payload"] as? JsonObject
        return PcSessionEvent(
            event = root["event"]?.jsonPrimitive?.content.orEmpty(),
            taskId = root["taskId"]?.jsonPrimitive?.contentOrNull,
            nodeId = root["nodeId"]?.jsonPrimitive?.contentOrNull,
            sessionId = root["sessionId"]?.jsonPrimitive?.contentOrNull,
            message = payload?.get("message")?.jsonPrimitive?.contentOrNull.orEmpty(),
            progress = payload?.get("progress")
                ?.jsonPrimitive
                ?.contentOrNull
                ?.toFloatOrNull(),
            runner = payload?.get("runner")?.jsonPrimitive?.contentOrNull,
            result = payload?.get("result")?.jsonPrimitive?.contentOrNull.orEmpty(),
            error = payload?.get("error")?.jsonPrimitive?.contentOrNull.orEmpty(),
            inputMode = payload?.get("inputMode")?.jsonPrimitive?.contentOrNull
        )
    }

    private fun loadConnectionConfig(): PcAgentConnectionConfig {
        val prefs = context.getSharedPreferences(PcAgentConnectionPrefs.PREFS_NAME, Context.MODE_PRIVATE)
        val host = prefs.getString(PcAgentConnectionPrefs.KEY_HOST, "").orEmpty()
        val port = prefs.getString(PcAgentConnectionPrefs.KEY_PORT, "3210").orEmpty()
        val token = prefs.getString(PcAgentConnectionPrefs.KEY_TOKEN, "").orEmpty()

        if (host.isNotBlank() || token.isNotBlank()) {
            return PcAgentConnectionConfig(host = host, port = port, token = token)
        }

        val legacyPrefs = context.getSharedPreferences(PcAgentConnectionPrefs.LEGACY_PREFS_NAME, Context.MODE_PRIVATE)
        return PcAgentConnectionConfig(
            host = legacyPrefs.getString(PcAgentConnectionPrefs.LEGACY_KEY_HOST, "").orEmpty(),
            port = legacyPrefs.getString(PcAgentConnectionPrefs.LEGACY_KEY_PORT, "3210").orEmpty(),
            token = legacyPrefs.getString(PcAgentConnectionPrefs.LEGACY_KEY_TOKEN, "").orEmpty()
        )
    }

    private fun buildWsUrl(config: PcAgentConnectionConfig): String {
        val host = config.host.trim()
        if (host.isBlank()) return ""

        return when {
            host.startsWith("ws://") || host.startsWith("wss://") -> {
                if (host.contains(DEFAULT_PC_AGENT_PATH)) host else host.trimEnd('/') + DEFAULT_PC_AGENT_PATH
            }

            host.startsWith("http://") -> {
                host.replaceFirst("http://", "ws://").trimEnd('/') + DEFAULT_PC_AGENT_PATH
            }

            host.startsWith("https://") -> {
                host.replaceFirst("https://", "wss://").trimEnd('/') + DEFAULT_PC_AGENT_PATH
            }

            else -> {
                val port = config.port.trim().ifBlank { "3210" }
                "ws://$host:$port$DEFAULT_PC_AGENT_PATH"
            }
        }
    }
}
