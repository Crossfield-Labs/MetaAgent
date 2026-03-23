package com.ai.assistance.metaagent.remote

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Call
import okhttp3.Response
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.net.URI
import java.util.concurrent.TimeUnit

@Serializable
data class DesktopRemoteConfig(
    val host: String = "",
    val port: String = "3210",
    val token: String = ""
)

@Serializable
data class DesktopPairingSettingsPayload(
    val autoApprove: Boolean,
    val passwordConfigured: Boolean
)

@Serializable
data class DesktopPairingRequestPayload(
    val pairingId: String,
    val deviceName: String,
    val status: String,
    val requestedAt: String,
    val approvedAt: String? = null,
    val rejectedAt: String? = null,
    val expiresAt: String
)

@Serializable
data class DesktopPairingEnvelopePayload(
    val request: DesktopPairingRequestPayload,
    val pairing: DesktopPairingSettingsPayload
)

@Serializable
data class DesktopPairingAuthPayload(
    val session: DesktopControlSessionPayload?,
    val sessionToken: String
)

@Serializable
data class DesktopHealthPayload(
    val service: String,
    val hasActiveSession: Boolean,
    val hasDesktopSession: Boolean,
    val apiVersion: Int = 1,
    val agent: DesktopAgentHealthPayload? = null,
    val video: DesktopVideoHealthPayload? = null
)

@Serializable
data class DesktopVideoHealthPayload(
    val session: DesktopVideoSessionPayload? = null,
    val transport: String,
    val capturePipeline: String
)

@Serializable
data class DesktopVideoSessionPayload(
    val id: String,
    val viewerName: String,
    val transport: String,
    val codec: String,
    val preferredWidth: Int,
    val preferredHeight: Int,
    val preferredFps: Int,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
    val lastError: String? = null,
    val viewerOfferSdp: String? = null,
    val hostAnswerSdp: String? = null,
    val candidateCount: Int = 0,
    val notes: List<String> = emptyList()
)

@Serializable
data class DesktopVideoSessionEnvelopePayload(
    val session: DesktopVideoSessionPayload? = null,
    val transport: String,
    val capturePipeline: String
)

@Serializable
data class DesktopAgentHealthPayload(
    val settings: DesktopAgentSettingsPayload,
    val state: DesktopAgentStatePayload
)

@Serializable
data class DesktopAgentSettingsPayload(
    val provider: String,
    val executable: String,
    val args: String,
    val cwd: String
)

@Serializable
data class DesktopAgentStatePayload(
    val status: String,
    val provider: String,
    val prompt: String? = null,
    val pid: Int? = null,
    val startedAt: String? = null,
    val finishedAt: String? = null,
    val exitCode: Int? = null,
    val cwd: String,
    val executable: String,
    val args: String,
    val lastError: String? = null,
    val lastOutput: String? = null
)

@Serializable
data class DesktopAgentLogsPayload(
    val logs: List<DesktopAgentLogEntryPayload>
)

@Serializable
data class DesktopAgentLogEntryPayload(
    val at: String,
    val stream: String,
    val line: String
)

@Serializable
data class DesktopAgentStateEnvelopePayload(
    val settings: DesktopAgentSettingsPayload,
    val state: DesktopAgentStatePayload
)

@Serializable
data class DesktopCapabilitiesPayload(
    val platform: String,
    val supported: Boolean,
    val supportsScreenshot: Boolean,
    val supportsMouse: Boolean,
    val supportsKeyboard: Boolean,
    val supportsAppLaunch: Boolean,
    val supportsClipboard: Boolean,
    val supportsWindowListing: Boolean,
    val supportsSystemInfo: Boolean,
    val reason: String? = null
)

@Serializable
data class DesktopControlSessionPayload(
    val id: String,
    val clientName: String,
    val openedAt: String,
    val lastSeenAt: String,
    val expiresAt: String
)

@Serializable
data class DesktopSessionPayload(
    val session: DesktopControlSessionPayload?
)

@Serializable
data class DesktopEventPayload(
    val id: String,
    val type: String,
    val createdAt: String,
    val data: Map<String, JsonElement> = emptyMap()
)

@Serializable
data class DesktopEventsPayload(
    val events: List<DesktopEventPayload>
)

@Serializable
data class DesktopScreenshotPayload(
    val mimeType: String,
    val base64: String,
    val width: Int,
    val height: Int
)

@Serializable
data class DesktopActionPayload(
    val ok: Boolean,
    val message: String
)

data class DesktopStreamFrame(
    val mimeType: String,
    val bytes: ByteArray,
    val width: Int? = null,
    val height: Int? = null
)

@Serializable
private data class DesktopSessionOpenRequest(
    val clientName: String
)

@Serializable
private data class DesktopPairingRequestBody(
    val deviceName: String
)

@Serializable
private data class DesktopPairingAuthRequest(
    val pairingId: String,
    val password: String,
    val clientName: String
)

@Serializable
private data class DesktopHeartbeatRequest(
    val sessionId: String? = null
)

@Serializable
private data class DesktopCloseSessionRequest(
    val sessionId: String? = null
)

@Serializable
private data class DesktopMoveRequest(
    val x: Int,
    val y: Int
)

@Serializable
private data class DesktopRelativeMoveRequest(
    val deltaX: Int,
    val deltaY: Int
)

@Serializable
private data class DesktopClickRequest(
    val x: Int? = null,
    val y: Int? = null,
    val button: String = "left"
)

@Serializable
private data class DesktopTypeRequest(
    val text: String
)

@Serializable
private data class DesktopKeyRequest(
    val key: String
)

@Serializable
private data class DesktopAgentRunRequest(
    val prompt: String,
    val provider: String? = null,
    val cwd: String? = null
)

@Serializable
private data class DesktopVideoOpenRequest(
    val viewerName: String = "MetaAgent Android",
    val codec: String = "h264",
    val preferredWidth: Int = 1280,
    val preferredHeight: Int = 720,
    val preferredFps: Int = 30
)

@Serializable
private data class DesktopVideoSdpRequest(
    val sessionId: String,
    val sdp: String
)

@Serializable
private data class DesktopVideoCandidateRequest(
    val sessionId: String,
    val candidate: String
)

private val desktopJson = Json {
    ignoreUnknownKeys = true
}

class DesktopRemoteClient(
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .writeTimeout(12, TimeUnit.SECONDS)
        .build()
) {
    suspend fun health(config: DesktopRemoteConfig): DesktopHealthPayload {
        return getEnvelope("/api/desktop/health", config)
    }

    suspend fun capabilities(config: DesktopRemoteConfig): DesktopCapabilitiesPayload {
        return getEnvelope("/api/desktop/capabilities", config)
    }

    suspend fun requestPairing(
        config: DesktopRemoteConfig,
        deviceName: String = "MetaAgent Android"
    ): DesktopPairingEnvelopePayload {
        return postEnvelope(
            "/api/desktop/pair/request",
            config.copy(token = ""),
            DesktopPairingRequestBody(deviceName)
        )
    }

    suspend fun pairingStatus(
        config: DesktopRemoteConfig,
        pairingId: String
    ): DesktopPairingEnvelopePayload {
        return getEnvelope("/api/desktop/pair/status?pairingId=$pairingId", config.copy(token = ""))
    }

    suspend fun authenticatePairing(
        config: DesktopRemoteConfig,
        pairingId: String,
        password: String,
        clientName: String = "MetaAgent-Android"
    ): DesktopPairingAuthPayload {
        return postEnvelope(
            "/api/desktop/pair/authenticate",
            config.copy(token = ""),
            DesktopPairingAuthRequest(pairingId, password, clientName)
        )
    }

    suspend fun getSession(config: DesktopRemoteConfig): DesktopSessionPayload {
        return getEnvelope("/api/desktop/session", config)
    }

    suspend fun openSession(
        config: DesktopRemoteConfig,
        clientName: String = "MetaAgent-Android"
    ): DesktopSessionPayload {
        return postEnvelope(
            path = "/api/desktop/session/open",
            config = config,
            body = DesktopSessionOpenRequest(clientName)
        )
    }

    suspend fun heartbeat(
        config: DesktopRemoteConfig,
        sessionId: String? = null
    ): DesktopSessionPayload {
        return postEnvelope(
            path = "/api/desktop/session/heartbeat",
            config = config,
            body = DesktopHeartbeatRequest(sessionId)
        )
    }

    suspend fun closeSession(
        config: DesktopRemoteConfig,
        sessionId: String? = null
    ): DesktopSessionPayload {
        return postEnvelope(
            path = "/api/desktop/session/close",
            config = config,
            body = DesktopCloseSessionRequest(sessionId)
        )
    }

    suspend fun events(config: DesktopRemoteConfig, limit: Int = 20): DesktopEventsPayload {
        return getEnvelope("/api/desktop/events?limit=$limit", config)
    }

    suspend fun screenshot(config: DesktopRemoteConfig): DesktopScreenshotPayload {
        return getEnvelope("/api/desktop/screenshot", config)
    }

    suspend fun move(config: DesktopRemoteConfig, x: Int, y: Int): DesktopActionPayload {
        return postEnvelope("/api/desktop/input/move", config, DesktopMoveRequest(x, y))
    }

    suspend fun moveRelative(
        config: DesktopRemoteConfig,
        deltaX: Int,
        deltaY: Int
    ): DesktopActionPayload {
        return postEnvelope(
            "/api/desktop/input/move-relative",
            config,
            DesktopRelativeMoveRequest(deltaX, deltaY)
        )
    }

    suspend fun click(
        config: DesktopRemoteConfig,
        x: Int? = null,
        y: Int? = null,
        button: String = "left"
    ): DesktopActionPayload {
        return postEnvelope(
            "/api/desktop/input/click",
            config,
            DesktopClickRequest(x, y, button)
        )
    }

    suspend fun type(config: DesktopRemoteConfig, text: String): DesktopActionPayload {
        return postEnvelope("/api/desktop/input/type", config, DesktopTypeRequest(text))
    }

    suspend fun key(config: DesktopRemoteConfig, key: String): DesktopActionPayload {
        return postEnvelope("/api/desktop/input/key", config, DesktopKeyRequest(key))
    }

    suspend fun agentState(config: DesktopRemoteConfig): DesktopAgentStateEnvelopePayload {
        return getEnvelope("/api/desktop/agent/state", config)
    }

    suspend fun agentLogs(
        config: DesktopRemoteConfig,
        limit: Int = 80
    ): DesktopAgentLogsPayload {
        return getEnvelope("/api/desktop/agent/logs?limit=$limit", config)
    }

    suspend fun runAgent(
        config: DesktopRemoteConfig,
        prompt: String,
        provider: String? = null,
        cwd: String? = null
    ): DesktopAgentStateEnvelopePayload {
        return postEnvelope(
            "/api/desktop/agent/run",
            config,
            DesktopAgentRunRequest(prompt = prompt, provider = provider, cwd = cwd)
        )
    }

    suspend fun stopAgent(config: DesktopRemoteConfig): DesktopActionPayload {
        return postEnvelope("/api/desktop/agent/stop", config, emptyMap<String, String>())
    }

    suspend fun videoSession(config: DesktopRemoteConfig): DesktopVideoSessionEnvelopePayload {
        return getEnvelope("/api/desktop/video/session", config)
    }

    suspend fun openVideoSession(
        config: DesktopRemoteConfig,
        viewerName: String = "MetaAgent Android",
        codec: String = "h264",
        preferredWidth: Int = 1280,
        preferredHeight: Int = 720,
        preferredFps: Int = 30
    ): DesktopVideoSessionEnvelopePayload {
        return postEnvelope(
            "/api/desktop/video/session/open",
            config,
            DesktopVideoOpenRequest(
                viewerName = viewerName,
                codec = codec,
                preferredWidth = preferredWidth,
                preferredHeight = preferredHeight,
                preferredFps = preferredFps
            )
        )
    }

    suspend fun closeVideoSession(config: DesktopRemoteConfig): DesktopActionPayload {
        return postEnvelope("/api/desktop/video/session/close", config, emptyMap<String, String>())
    }

    suspend fun submitVideoOffer(
        config: DesktopRemoteConfig,
        sessionId: String,
        sdp: String
    ): DesktopVideoSessionEnvelopePayload {
        return postEnvelope(
            "/api/desktop/video/session/offer",
            config,
            DesktopVideoSdpRequest(sessionId, sdp)
        )
    }

    suspend fun submitVideoAnswer(
        config: DesktopRemoteConfig,
        sessionId: String,
        sdp: String
    ): DesktopVideoSessionEnvelopePayload {
        return postEnvelope(
            "/api/desktop/video/session/answer",
            config,
            DesktopVideoSdpRequest(sessionId, sdp)
        )
    }

    suspend fun submitVideoCandidate(
        config: DesktopRemoteConfig,
        sessionId: String,
        candidate: String
    ): DesktopVideoSessionEnvelopePayload {
        return postEnvelope(
            "/api/desktop/video/session/candidate",
            config,
            DesktopVideoCandidateRequest(sessionId, candidate)
        )
    }

    fun createDesktopStreamCall(config: DesktopRemoteConfig): Call {
        val request = requestBuilder(
            "/api/desktop/stream?format=jpeg&quality=68&scalePercent=60&fps=8",
            config
        ).get().build()
        return httpClient.newCall(request)
    }

    fun consumeDesktopStream(
        call: Call,
        onFrame: (DesktopStreamFrame) -> Unit
    ) {
        val response = call.execute()
        response.use { consumeDesktopStreamResponse(call, it, onFrame) }
    }

    private inline fun <reified T> getEnvelope(path: String, config: DesktopRemoteConfig): T {
        val request = requestBuilder(path, config).get().build()
        return executeEnvelope(request)
    }

    private inline fun <reified T, reified B> postEnvelope(
        path: String,
        config: DesktopRemoteConfig,
        body: B
    ): T {
        val requestBody = desktopJson.encodeToString(body)
            .toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = requestBuilder(path, config).post(requestBody).build()
        return executeEnvelope(request)
    }

    private inline fun <reified T> executeEnvelope(request: Request): T {
        httpClient.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("HTTP ${response.code}: ${raw.ifBlank { response.message }}")
            }
            val envelope = desktopJson.decodeFromString<RemoteApiEnvelope<T>>(raw)
            if (!envelope.ok || envelope.data == null) {
                throw IllegalStateException(envelope.error ?: "Desktop API returned empty payload")
            }
            return envelope.data
        }
    }

    private fun requestBuilder(path: String, config: DesktopRemoteConfig): Request.Builder {
        val url = buildUrl(config, path)
        return Request.Builder()
            .url(url)
            .apply {
                val token = config.token.trim()
                if (token.isNotEmpty()) {
                    header("Authorization", "Bearer $token")
                }
            }
    }

    private fun buildUrl(config: DesktopRemoteConfig, path: String): String {
        val host = config.host.trim().removeSuffix("/")
        require(host.isNotEmpty()) { "请先填写电脑地址" }
        val port = config.port.trim().ifBlank { "3210" }.toIntOrNull()
            ?: error("端口格式不正确")
        return if (host.startsWith("http://") || host.startsWith("https://")) {
            val uri = URI(host)
            val scheme = uri.scheme ?: "http"
            val resolvedPort = if (uri.port != -1) uri.port else port
            val authority = uri.host ?: error("电脑地址格式不正确")
            "$scheme://$authority:$resolvedPort$path"
        } else {
            "http://$host:$port$path"
        }
    }

    private fun consumeDesktopStreamResponse(
        call: Call,
        response: Response,
        onFrame: (DesktopStreamFrame) -> Unit
    ) {
        if (!response.isSuccessful) {
            val raw = response.body?.string().orEmpty()
            throw IllegalStateException("HTTP ${response.code}: ${raw.ifBlank { response.message }}")
        }
        val body = response.body ?: throw IllegalStateException("Desktop stream returned empty body")
        val input = BufferedInputStream(body.byteStream())
        while (!call.isCanceled()) {
            val boundary = readAsciiLine(input) ?: break
            if (boundary.isBlank()) continue
            if (!boundary.startsWith("--")) continue

            var mimeType = "image/png"
            var contentLength = -1
            var width: Int? = null
            var height: Int? = null

            while (true) {
                val headerLine = readAsciiLine(input) ?: return
                if (headerLine.isBlank()) break
                val split = headerLine.split(':', limit = 2)
                if (split.size != 2) continue
                val key = split[0].trim().lowercase()
                val value = split[1].trim()
                when (key) {
                    "content-type" -> mimeType = value
                    "content-length" -> contentLength = value.toIntOrNull() ?: -1
                    "x-width" -> width = value.toIntOrNull()
                    "x-height" -> height = value.toIntOrNull()
                }
            }

            if (contentLength <= 0) continue
            val bytes = input.readNBytes(contentLength)
            if (bytes.size == contentLength) {
                onFrame(DesktopStreamFrame(mimeType, bytes, width, height))
            }
        }
    }

    private fun readAsciiLine(input: BufferedInputStream): String? {
        val buffer = ByteArrayOutputStream()
        while (true) {
            val next = input.read()
            if (next == -1) {
                return if (buffer.size() == 0) null else buffer.toString(Charsets.UTF_8.name()).trimEnd('\r')
            }
            if (next == '\n'.code) {
                return buffer.toString(Charsets.UTF_8.name()).trimEnd('\r')
            }
            buffer.write(next)
        }
    }
}
