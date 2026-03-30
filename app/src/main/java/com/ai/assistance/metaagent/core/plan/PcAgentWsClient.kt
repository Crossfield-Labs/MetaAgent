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
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
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
    val inputMode: String? = null,
    val phase: String? = null,
    val worker: String? = null,
    val sessionMode: String? = null,
    val workerProfile: String? = null,
    val workerTaskId: String? = null,
    val canInterrupt: Boolean? = null,
    val snapshotVersion: Int? = null,
    val latestArtifactSummary: String? = null,
    val permissionSummary: String? = null,
    val sessionInfoSummary: String? = null,
    val mcpStatusSummary: String? = null,
    val intent: String? = null,
    val target: String? = null,
    val artifacts: List<String> = emptyList(),
    val recentHookEvents: List<String> = emptyList(),
    val snapshot: PcSessionSnapshot? = null,
    val rawPayload: JsonObject? = null
)

data class PcSessionSnapshot(
    val status: String = "",
    val phase: String = "",
    val activeWorker: String = "",
    val activeSessionMode: String = "",
    val activeWorkerTaskId: String = "",
    val activeWorkerProfile: String = "",
    val activeWorkerCanInterrupt: Boolean = false,
    val lastProgressMessage: String = "",
    val latestSummary: String = "",
    val latestArtifactSummary: String = "",
    val stopReason: String = "",
    val permissionSummary: String = "",
    val sessionInfoSummary: String = "",
    val mcpStatusSummary: String = "",
    val awaitingInput: Boolean = false,
    val pendingUserPrompt: String = "",
    val pendingInputMode: String = "",
    val artifacts: List<String> = emptyList(),
    val recentHookEvents: List<String> = emptyList(),
    val snapshotVersion: Int = 0
)

data class PcSessionExecutionResult(
    val success: Boolean,
    val sessionId: String?,
    val result: String = "",
    val error: String = "",
    val finalMessage: String = "",
    val awaitingUser: Boolean = false,
    val awaitingUserPrompt: String = "",
    val inputMode: String? = null,
    val artifacts: List<String> = emptyList(),
    val snapshot: PcSessionSnapshot? = null
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
        inputIntent: String = "reply",
        target: String? = null,
        connectionOverride: PcAgentConnectionConfig? = null,
        endpointOverride: String? = null,
        onEvent: (PcSessionEvent) -> Unit = {}
    ): PcSessionExecutionResult {
        val params = buildJsonObject {
            put("text", JsonPrimitive(userInput))
            put("inputIntent", JsonPrimitive(inputIntent))
            target?.takeIf { it.isNotBlank() }?.let { put("target", JsonPrimitive(it)) }
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
            onEvent = onEvent,
            terminalResolver = { event ->
                when {
                    inputIntent != "reply" && event.event == "pc.session.followup.accepted" -> {
                        PcSessionExecutionResult(
                            success = true,
                            sessionId = event.sessionId,
                            finalMessage = event.message,
                            result = event.result.ifBlank { event.message },
                            artifacts = event.artifacts,
                            snapshot = event.snapshot
                        )
                    }

                    else -> defaultTerminalResult(event)
                }
            }
        )
    }

    suspend fun interruptSession(
        taskId: String,
        nodeId: String,
        sessionId: String,
        note: String = "",
        connectionOverride: PcAgentConnectionConfig? = null,
        endpointOverride: String? = null,
        onEvent: (PcSessionEvent) -> Unit = {}
    ): PcSessionExecutionResult {
        val params = buildJsonObject {
            if (note.isNotBlank()) {
                put("text", JsonPrimitive(note))
            }
        }
        return runRequest(
            frame = PcSessionRequestFrame(
                method = "pc.session.interrupt",
                taskId = taskId,
                nodeId = nodeId,
                sessionId = sessionId,
                params = params
            ),
            connectionOverride = connectionOverride,
            endpointOverride = endpointOverride,
            onEvent = onEvent,
            terminalResolver = { event ->
                when (event.event) {
                    "pc.session.followup.accepted" -> {
                        PcSessionExecutionResult(
                            success = true,
                            sessionId = event.sessionId,
                            finalMessage = event.message,
                            result = event.result.ifBlank { event.message },
                            artifacts = event.artifacts,
                            snapshot = event.snapshot
                        )
                    }

                    else -> defaultTerminalResult(event)
                }
            }
        )
    }

    suspend fun requestSnapshot(
        taskId: String,
        nodeId: String,
        sessionId: String,
        connectionOverride: PcAgentConnectionConfig? = null,
        endpointOverride: String? = null,
        onEvent: (PcSessionEvent) -> Unit = {}
    ): PcSessionExecutionResult {
        return runRequest(
            frame = PcSessionRequestFrame(
                method = "pc.session.snapshot",
                taskId = taskId,
                nodeId = nodeId,
                sessionId = sessionId
            ),
            connectionOverride = connectionOverride,
            endpointOverride = endpointOverride,
            onEvent = onEvent,
            terminalResolver = { event ->
                when (event.event) {
                    "pc.session.snapshot" -> {
                        PcSessionExecutionResult(
                            success = true,
                            sessionId = event.sessionId,
                            finalMessage = event.message,
                            result = event.result.ifBlank { event.message },
                            artifacts = event.artifacts,
                            snapshot = event.snapshot
                        )
                    }

                    else -> defaultTerminalResult(event)
                }
            }
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
        onEvent: (PcSessionEvent) -> Unit = {},
        terminalResolver: (PcSessionEvent) -> PcSessionExecutionResult? = { event -> defaultTerminalResult(event) }
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
                        val terminalResult = terminalResolver(event)
                        if (terminalResult != null) {
                            if (!terminalSent && continuation.isActive) {
                                terminalSent = true
                                continuation.resume(terminalResult)
                            }
                            val reason = when (event.event) {
                                "pc.session.await_user" -> "await-user"
                                "pc.session.completed" -> "completed"
                                "pc.session.failed" -> "failed"
                                else -> event.event.ifBlank { "done" }
                            }
                            webSocket.close(1000, reason)
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
        val snapshotObject = payload?.getJsonObjectOrNull("snapshot")
        val snapshot = snapshotObject?.let(::parseSnapshot)
        val hookEvents = payload?.getHookEventSummaries("recentHookEvents")
            .takeUnless { it.isNullOrEmpty() }
            ?: snapshot?.recentHookEvents
            ?: emptyList()
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
            inputMode = payload?.get("inputMode")?.jsonPrimitive?.contentOrNull,
            phase = payload?.get("phase")?.jsonPrimitive?.contentOrNull ?: snapshot?.phase,
            worker = payload?.get("worker")?.jsonPrimitive?.contentOrNull ?: snapshot?.activeWorker,
            sessionMode = payload?.get("sessionMode")?.jsonPrimitive?.contentOrNull ?: snapshot?.activeSessionMode,
            workerProfile = payload?.get("workerProfile")?.jsonPrimitive?.contentOrNull ?: snapshot?.activeWorkerProfile,
            workerTaskId = payload?.get("taskId")?.jsonPrimitive?.contentOrNull ?: snapshot?.activeWorkerTaskId,
            canInterrupt = payload?.getBooleanOrNull("canInterrupt") ?: snapshot?.activeWorkerCanInterrupt,
            snapshotVersion = payload?.getIntOrNull("snapshotVersion") ?: snapshot?.snapshotVersion,
            latestArtifactSummary = payload?.get("latestArtifactSummary")?.jsonPrimitive?.contentOrNull
                ?: snapshot?.latestArtifactSummary,
            permissionSummary = payload?.get("permissionSummary")?.jsonPrimitive?.contentOrNull
                ?: snapshot?.permissionSummary,
            sessionInfoSummary = payload?.get("sessionInfoSummary")?.jsonPrimitive?.contentOrNull
                ?: snapshot?.sessionInfoSummary,
            mcpStatusSummary = payload?.get("mcpStatusSummary")?.jsonPrimitive?.contentOrNull
                ?: snapshot?.mcpStatusSummary,
            intent = payload?.get("intent")?.jsonPrimitive?.contentOrNull,
            target = payload?.get("target")?.jsonPrimitive?.contentOrNull,
            artifacts = payload?.getStringList("artifacts")
                ?.takeIf { it.isNotEmpty() }
                ?: snapshot?.artifacts
                ?: emptyList(),
            recentHookEvents = hookEvents,
            snapshot = snapshot,
            rawPayload = payload
        )
    }

    private fun defaultTerminalResult(event: PcSessionEvent): PcSessionExecutionResult? {
        return when (event.event) {
            "pc.session.await_user" -> {
                PcSessionExecutionResult(
                    success = true,
                    sessionId = event.sessionId,
                    awaitingUser = true,
                    awaitingUserPrompt = event.message,
                    inputMode = event.inputMode,
                    finalMessage = event.message,
                    artifacts = event.artifacts,
                    snapshot = event.snapshot
                )
            }

            "pc.session.completed" -> {
                PcSessionExecutionResult(
                    success = true,
                    sessionId = event.sessionId,
                    result = event.result.ifBlank { event.message },
                    finalMessage = event.message,
                    artifacts = event.artifacts,
                    snapshot = event.snapshot
                )
            }

            "pc.session.failed" -> {
                PcSessionExecutionResult(
                    success = false,
                    sessionId = event.sessionId,
                    error = event.error.ifBlank { event.message },
                    finalMessage = event.message,
                    artifacts = event.artifacts,
                    snapshot = event.snapshot
                )
            }

            else -> null
        }
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

    private fun parseSnapshot(snapshot: JsonObject): PcSessionSnapshot {
        return PcSessionSnapshot(
            status = snapshot["status"]?.jsonPrimitive?.contentOrNull.orEmpty(),
            phase = snapshot["phase"]?.jsonPrimitive?.contentOrNull.orEmpty(),
            activeWorker = snapshot["activeWorker"]?.jsonPrimitive?.contentOrNull.orEmpty(),
            activeSessionMode = snapshot["activeSessionMode"]?.jsonPrimitive?.contentOrNull.orEmpty(),
            activeWorkerTaskId = snapshot["activeWorkerTaskId"]?.jsonPrimitive?.contentOrNull.orEmpty(),
            activeWorkerProfile = snapshot["activeWorkerProfile"]?.jsonPrimitive?.contentOrNull.orEmpty(),
            activeWorkerCanInterrupt = snapshot.getBooleanOrNull("activeWorkerCanInterrupt") ?: false,
            lastProgressMessage = snapshot["lastProgressMessage"]?.jsonPrimitive?.contentOrNull.orEmpty(),
            latestSummary = snapshot["latestSummary"]?.jsonPrimitive?.contentOrNull.orEmpty(),
            latestArtifactSummary = snapshot["latestArtifactSummary"]?.jsonPrimitive?.contentOrNull.orEmpty(),
            stopReason = snapshot["stopReason"]?.jsonPrimitive?.contentOrNull.orEmpty(),
            permissionSummary = snapshot["permissionSummary"]?.jsonPrimitive?.contentOrNull.orEmpty(),
            sessionInfoSummary = snapshot["sessionInfoSummary"]?.jsonPrimitive?.contentOrNull.orEmpty(),
            mcpStatusSummary = snapshot["mcpStatusSummary"]?.jsonPrimitive?.contentOrNull.orEmpty(),
            awaitingInput = snapshot.getBooleanOrNull("awaitingInput") ?: false,
            pendingUserPrompt = snapshot["pendingUserPrompt"]?.jsonPrimitive?.contentOrNull.orEmpty(),
            pendingInputMode = snapshot["pendingInputMode"]?.jsonPrimitive?.contentOrNull.orEmpty(),
            artifacts = snapshot.getStringList("artifacts"),
            recentHookEvents = snapshot.getHookEventSummaries("recentHookEvents"),
            snapshotVersion = snapshot.getIntOrNull("snapshotVersion") ?: 0
        )
    }

    private fun JsonObject.getJsonObjectOrNull(key: String): JsonObject? {
        return runCatching { this[key]?.jsonObject }.getOrNull()
    }

    private fun JsonObject.getBooleanOrNull(key: String): Boolean? {
        return this[key]?.jsonPrimitive?.contentOrNull?.toBooleanStrictOrNull()
    }

    private fun JsonObject.getIntOrNull(key: String): Int? {
        return this[key]?.jsonPrimitive?.contentOrNull?.toIntOrNull()
    }

    private fun JsonObject.getStringList(key: String): List<String> {
        val element = this[key] ?: return emptyList()
        return runCatching {
            element.jsonArray.mapNotNull { it.jsonPrimitive.contentOrNull?.trim() }.filter { it.isNotBlank() }
        }.getOrDefault(emptyList())
    }

    private fun JsonObject.getHookEventSummaries(key: String): List<String> {
        val element = this[key] ?: return emptyList()
        return parseHookEvents(element)
    }

    private fun parseHookEvents(element: JsonElement): List<String> {
        return runCatching {
            element.jsonArray.mapNotNull { entry ->
                val item = runCatching { entry.jsonObject }.getOrNull() ?: return@mapNotNull null
                val name = item["name"]?.jsonPrimitive?.contentOrNull.orEmpty()
                val message = item["message"]?.jsonPrimitive?.contentOrNull.orEmpty()
                val tool = item["toolName"]?.jsonPrimitive?.contentOrNull.orEmpty()
                val status = item["status"]?.jsonPrimitive?.contentOrNull.orEmpty()
                listOf(name, status, tool)
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .joinToString(" · ")
                    .let { prefix ->
                        when {
                            prefix.isNotBlank() && message.isNotBlank() -> "$prefix: ${message.trim()}"
                            prefix.isNotBlank() -> prefix
                            message.isNotBlank() -> message.trim()
                            else -> null
                        }
                    }
            }
        }.getOrDefault(emptyList())
    }
}
