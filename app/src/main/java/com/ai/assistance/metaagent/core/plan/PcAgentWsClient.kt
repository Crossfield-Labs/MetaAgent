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
    val method: String = "pc.session.start",
    val taskId: String,
    val nodeId: String,
    val params: JsonObject
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
    val error: String = ""
)

data class PcSessionExecutionResult(
    val success: Boolean,
    val sessionId: String?,
    val result: String = "",
    val error: String = "",
    val finalMessage: String = ""
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
    ): PcSessionExecutionResult = suspendCancellableCoroutine { continuation ->
        val config = connectionOverride ?: loadConnectionConfig()
        val url = endpointOverride?.trim().takeUnless { it.isNullOrBlank() }
            ?: buildWsUrl(config)

        if (url.isBlank()) {
            continuation.resume(
                PcSessionExecutionResult(
                    success = false,
                    sessionId = null,
                    error = "电脑端地址未配置。请先在设置 -> 远程控制 中填写桌面端 Host / Port。"
                )
            )
            return@suspendCancellableCoroutine
        }

        val params = buildJsonObject {
            put("runner", JsonPrimitive(startRequest.runner))
            put("workspace", JsonPrimitive(startRequest.workspace))
            put("task", JsonPrimitive(startRequest.task))
            put("goal", JsonPrimitive(startRequest.goal))
            startRequest.command?.takeIf { it.isNotBlank() }?.let { command ->
                put("command", JsonPrimitive(command))
            }
        }
        val payload = json.encodeToString(
            PcSessionRequestFrame(
                taskId = taskId,
                nodeId = nodeId,
                params = params
            )
        )

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
                                    error = error.message ?: "电脑端会话响应解析失败"
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
                            error = t.message ?: "电脑端连接失败"
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
                            error = if (reason.isBlank()) "电脑端连接已关闭" else reason
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
            error = payload?.get("error")?.jsonPrimitive?.contentOrNull.orEmpty()
        )
    }

    private fun loadConnectionConfig(): PcAgentConnectionConfig {
        val prefs = context.getSharedPreferences(PcAgentConnectionPrefs.PREFS_NAME, Context.MODE_PRIVATE)
        val host = prefs.getString(PcAgentConnectionPrefs.KEY_HOST, "").orEmpty()
        val port = prefs.getString(PcAgentConnectionPrefs.KEY_PORT, "3210").orEmpty()
        val token = prefs.getString(PcAgentConnectionPrefs.KEY_TOKEN, "").orEmpty()

        if (host.isNotBlank() || token.isNotBlank()) {
            return PcAgentConnectionConfig(
                host = host,
                port = port,
                token = token
            )
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
