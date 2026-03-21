package com.ai.assistance.metaagent.remote

import android.content.Context
import android.os.Build
import android.util.Base64
import com.ai.assistance.metaagent.core.tools.AIToolHandler
import com.ai.assistance.metaagent.core.tools.agent.ShowerController
import com.ai.assistance.metaagent.core.tools.defaultTool.ToolGetter
import com.ai.assistance.metaagent.data.model.AITool
import com.ai.assistance.metaagent.data.model.ToolParameter
import com.ai.assistance.metaagent.data.preferences.MemorySearchSettingsPreferences
import com.ai.assistance.metaagent.data.preferences.preferencesManager
import com.ai.assistance.metaagent.data.repository.MemoryRepository
import com.ai.assistance.metaagent.ui.features.memory.screens.graph.model.Graph
import com.ai.assistance.metaagent.util.AppLogger
import fi.iki.elonen.NanoHTTPD
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.Locale
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.json.JSONObject

class RemoteAgentServer(
    context: Context,
    private val port: Int = DEFAULT_PORT
) : NanoHTTPD(port) {
    private val appContext = context.applicationContext
    private val json = Json { prettyPrint = true; encodeDefaults = true }
    private val toolHandler by lazy {
        AIToolHandler.getInstance(appContext).apply { registerDefaultTools() }
    }

    companion object {
        private const val TAG = "RemoteAgentServer"
        const val DEFAULT_PORT = 8095
    }

    override fun serve(session: IHTTPSession): Response {
        AppLogger.d(TAG, "Remote request: ${session.method} ${session.uri}")
        if (session.method == Method.OPTIONS) {
            return jsonResponse(Response.Status.OK, buildJsonObject { put("ok", true) })
        }

        val payload = parsePayload(session)

        return try {
            when {
                session.uri == "/api/remote/health" && session.method == Method.GET -> {
                    jsonResponse(
                        Response.Status.OK,
                        RemoteApiEnvelope(
                            ok = true,
                            data = RemoteServerInfo(
                                service = "remote-agent",
                                port = port,
                                hasActiveSession = RemoteSessionManager.getActiveSession() != null
                            )
                        )
                    )
                }
                session.uri == "/api/remote/session/open" && session.method == Method.POST -> {
                    handleOpenSession(payload)
                }
                session.uri == "/api/remote/session" && session.method == Method.GET -> {
                    requireAuth(session) ?: handleGetSession()
                }
                session.uri == "/api/remote/session/close" && session.method == Method.POST -> {
                    requireAuth(session) ?: handleCloseSession(payload)
                }
                session.uri == "/api/remote/heartbeat" && session.method == Method.GET -> {
                    requireAuth(session) ?: handleHeartbeat()
                }
                session.uri == "/api/remote/capabilities" && session.method == Method.GET -> {
                    requireAuth(session) ?: jsonResponse(
                        Response.Status.OK,
                        RemoteApiEnvelope(ok = true, data = collectCapabilities())
                    )
                }
                session.uri == "/api/remote/screenshot" && session.method == Method.GET -> {
                    requireAuth(session) ?: handleScreenshot(session)
                }
                session.uri == "/api/remote/input/tap" && session.method == Method.POST -> {
                    requireAuth(session) ?: handleTap(payload)
                }
                session.uri == "/api/remote/input/swipe" && session.method == Method.POST -> {
                    requireAuth(session) ?: handleSwipe(payload)
                }
                session.uri == "/api/remote/input/key" && session.method == Method.POST -> {
                    requireAuth(session) ?: handleKey(payload)
                }
                session.uri == "/api/remote/input/text" && session.method == Method.POST -> {
                    requireAuth(session) ?: handleText(payload)
                }
                session.uri == "/api/remote/app/launch" && session.method == Method.POST -> {
                    requireAuth(session) ?: handleLaunch(payload)
                }
                session.uri == "/api/remote/agent/run" && session.method == Method.POST -> {
                    requireAuth(session) ?: handleRunAgent(payload)
                }
                session.uri == "/api/remote/agent/tasks" && session.method == Method.GET -> {
                    requireAuth(session) ?: jsonResponse(
                        Response.Status.OK,
                        RemoteApiEnvelope(ok = true, data = RemoteAgentTaskManager.listTasks())
                    )
                }
                session.uri.startsWith("/api/remote/agent/") && session.uri.endsWith("/state") && session.method == Method.GET -> {
                    requireAuth(session) ?: handleAgentState(session.uri)
                }
                session.uri.startsWith("/api/remote/agent/") && session.uri.endsWith("/cancel") && session.method == Method.POST -> {
                    requireAuth(session) ?: handleAgentCancel(session.uri)
                }
                session.uri == "/api/remote/memory/query" && session.method == Method.GET -> {
                    requireAuth(session) ?: handleMemoryQuery(session)
                }
                session.uri == "/api/remote/memory/item" && session.method == Method.GET -> {
                    requireAuth(session) ?: handleMemoryItem(session)
                }
                session.uri == "/api/remote/memory/document" && session.method == Method.GET -> {
                    requireAuth(session) ?: handleMemoryDocument(session)
                }
                session.uri == "/api/remote/memory/create" && session.method == Method.POST -> {
                    requireAuth(session) ?: handleCreateMemory(payload)
                }
                session.uri == "/api/remote/memory/update" && session.method == Method.POST -> {
                    requireAuth(session) ?: handleUpdateMemory(payload)
                }
                session.uri == "/api/remote/memory/delete" && session.method == Method.POST -> {
                    requireAuth(session) ?: handleDeleteMemory(payload)
                }
                session.uri == "/api/remote/memory/link" && session.method == Method.POST -> {
                    requireAuth(session) ?: handleLinkMemory(payload)
                }
                session.uri == "/api/remote/memory/links" && session.method == Method.GET -> {
                    requireAuth(session) ?: handleMemoryLinks(session)
                }
                session.uri == "/api/remote/memory/graph" && session.method == Method.GET -> {
                    requireAuth(session) ?: handleMemoryGraph()
                }
                else -> notFound("Endpoint not found")
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Remote request failed: ${session.uri}", e)
            jsonResponse(
                Response.Status.INTERNAL_ERROR,
                RemoteApiEnvelope<RemoteServerInfo>(ok = false, error = e.message ?: "Internal server error")
            )
        }
    }

    private fun handleOpenSession(payload: JSONObject?): Response {
        val clientName = payload?.optString("clientName")
            ?.takeIf { it.isNotBlank() }
            ?: payload?.optString("client_name")?.takeIf { it.isNotBlank() }
            ?: "desktop-client"
        val snapshot = RemoteSessionManager.openSession(clientName)
        return jsonResponse(Response.Status.OK, RemoteApiEnvelope(ok = true, data = snapshot))
    }

    private fun handleGetSession(): Response {
        val snapshot = RemoteSessionManager.getActiveSession()
            ?: return notFound("No active remote session")
        return jsonResponse(
            Response.Status.OK,
            RemoteApiEnvelope(ok = true, data = snapshot.toPublicSnapshot())
        )
    }

    private fun handleCloseSession(payload: JSONObject?): Response {
        val sessionId = payload?.optString("sessionId")?.takeIf { it.isNotBlank() }
            ?: payload?.optString("session_id")?.takeIf { it.isNotBlank() }
        val closed = RemoteSessionManager.closeSession(sessionId)
        return if (closed) {
            jsonResponse(Response.Status.OK, RemoteApiEnvelope(ok = true, data = buildJsonObject { put("closed", true) }))
        } else {
            badRequest("Session id mismatch")
        }
    }

    private fun handleHeartbeat(): Response {
        val session = RemoteSessionManager.getActiveSession()?.toPublicSnapshot()
        val recentTasks = RemoteAgentTaskManager.listTasks().take(10)
        return jsonResponse(
            Response.Status.OK,
            RemoteApiEnvelope(
                ok = true,
                data = RemoteHeartbeatPayload(
                    server = RemoteServerInfo(
                        service = "remote-agent",
                        port = port,
                        hasActiveSession = session != null
                    ),
                    session = session,
                    capabilities = collectCapabilities(),
                    activeTaskCount = RemoteAgentTaskManager.activeTaskCount(),
                    totalTaskCount = RemoteAgentTaskManager.totalTaskCount(),
                    recentTasks = recentTasks
                )
            )
        )
    }

    private fun collectCapabilities(): RemoteCapabilitiesPayload {
        return RemoteRuntimeInspector.collectCapabilities(appContext)
    }

    private fun handleScreenshot(session: IHTTPSession): Response {
        val agentId = session.parameters["agent_id"]?.firstOrNull()?.takeIf { it.isNotBlank() } ?: "default"
        val showerDisplayId = ShowerController.getDisplayId(agentId)
        val screenshotPayload = runBlocking {
            if (showerDisplayId != null) {
                val bytes = ShowerController.requestScreenshot(agentId) ?: return@runBlocking null
                val dims = ShowerController.getVideoSize(agentId)
                RemoteScreenshotPayload(
                    agentId = agentId,
                    width = dims?.first,
                    height = dims?.second,
                    mimeType = "image/png",
                    base64 = Base64.encodeToString(bytes, Base64.NO_WRAP),
                    source = "shower"
                )
            } else {
                val uiTools = ToolGetter.getUITools(appContext)
                val (path, dims) = uiTools.captureScreenshot(AITool("capture_screenshot"))
                val filePath = path ?: return@runBlocking null
                val bytes = File(filePath).takeIf { it.exists() }?.readBytes() ?: return@runBlocking null
                RemoteScreenshotPayload(
                    agentId = agentId,
                    width = dims?.first,
                    height = dims?.second,
                    mimeType = inferMimeType(filePath),
                    base64 = Base64.encodeToString(bytes, Base64.NO_WRAP),
                    source = "ui_tools"
                )
            }
        } ?: return badRequest("Failed to capture screenshot")

        return jsonResponse(Response.Status.OK, RemoteApiEnvelope(ok = true, data = screenshotPayload))
    }

    private fun handleTap(payload: JSONObject?): Response {
        val x = payload?.optIntCompat("x") ?: return badRequest("Missing x")
        val y = payload.optIntCompat("y") ?: return badRequest("Missing y")
        val agentId = payload.optStringOrNull("agentId") ?: "default"
        val displayId = ShowerController.getDisplayId(agentId)

        val (success, message) = runBlocking {
            if (displayId != null) {
                val ok = ShowerController.tap(agentId, x, y)
                ok to if (ok) "Tapped via Shower" else "Shower tap failed"
            } else {
                val uiTools = ToolGetter.getUITools(appContext)
                val result = uiTools.tap(
                    AITool(
                        "tap",
                        listOf(ToolParameter("x", x.toString()), ToolParameter("y", y.toString()))
                    )
                )
                result.success to (result.error ?: result.result.toString())
            }
        }
        return opResponse(success, message, agentId, displayId)
    }

    private fun handleSwipe(payload: JSONObject?): Response {
        val startX = payload?.optIntCompat("startX") ?: payload?.optIntCompat("start_x")
            ?: return badRequest("Missing startX")
        val startY = payload.optIntCompat("startY") ?: payload.optIntCompat("start_y")
            ?: return badRequest("Missing startY")
        val endX = payload.optIntCompat("endX") ?: payload.optIntCompat("end_x")
            ?: return badRequest("Missing endX")
        val endY = payload.optIntCompat("endY") ?: payload.optIntCompat("end_y")
            ?: return badRequest("Missing endY")
        val durationMs = payload.optLongCompat("durationMs")
            ?: payload.optLongCompat("duration_ms")
            ?: 300L
        val agentId = payload.optStringOrNull("agentId") ?: "default"
        val displayId = ShowerController.getDisplayId(agentId)

        val (success, message) = runBlocking {
            if (displayId != null) {
                val ok = ShowerController.swipe(agentId, startX, startY, endX, endY, durationMs)
                ok to if (ok) "Swiped via Shower" else "Shower swipe failed"
            } else {
                val uiTools = ToolGetter.getUITools(appContext)
                val result = uiTools.swipe(
                    AITool(
                        "swipe",
                        listOf(
                            ToolParameter("start_x", startX.toString()),
                            ToolParameter("start_y", startY.toString()),
                            ToolParameter("end_x", endX.toString()),
                            ToolParameter("end_y", endY.toString()),
                            ToolParameter("duration", durationMs.toString())
                        )
                    )
                )
                result.success to (result.error ?: result.result.toString())
            }
        }
        return opResponse(success, message, agentId, displayId)
    }

    private fun handleKey(payload: JSONObject?): Response {
        val keyCode = payload?.optIntCompat("keyCode") ?: payload?.optIntCompat("key_code")
            ?: return badRequest("Missing keyCode")
        val metaState = payload.optIntCompat("metaState") ?: payload.optIntCompat("meta_state") ?: 0
        val agentId = payload.optStringOrNull("agentId") ?: "default"
        val displayId = ShowerController.getDisplayId(agentId)

        val (success, message) = runBlocking {
            if (displayId != null) {
                val ok = if (metaState != 0) {
                    ShowerController.keyWithMeta(agentId, keyCode, metaState)
                } else {
                    ShowerController.key(agentId, keyCode)
                }
                ok to if (ok) "Key injected via Shower" else "Shower key injection failed"
            } else {
                val uiTools = ToolGetter.getUITools(appContext)
                val result = uiTools.pressKey(
                    AITool("press_key", listOf(ToolParameter("key_code", keyCode.toString())))
                )
                result.success to (result.error ?: result.result.toString())
            }
        }
        return opResponse(success, message, agentId, displayId)
    }

    private fun handleText(payload: JSONObject?): Response {
        val text = payload?.optStringOrNull("text") ?: return badRequest("Missing text")
        val agentId = payload.optStringOrNull("agentId") ?: "default"
        val displayId = ShowerController.getDisplayId(agentId)
        val (success, message) = runBlocking {
            val uiTools = ToolGetter.getUITools(appContext)
            val result = uiTools.setInputText(
                AITool("set_input_text", listOf(ToolParameter("text", text)))
            )
            result.success to (result.error ?: result.result.toString())
        }
        return opResponse(success, message, agentId, displayId)
    }

    private fun handleLaunch(payload: JSONObject?): Response {
        val app = payload?.optStringOrNull("app") ?: payload?.optStringOrNull("packageName")
            ?: return badRequest("Missing app")
        val agentId = payload.optStringOrNull("agentId") ?: "default"
        val displayId = ShowerController.getDisplayId(agentId)

        val (success, message) = runBlocking {
            if (displayId != null) {
                val ok = ShowerController.launchApp(agentId, app)
                ok to if (ok) "App launched via Shower" else "Failed to launch app via Shower"
            } else {
                val result = toolHandler.executeTool(
                    AITool("start_app", listOf(ToolParameter("package_name", app)))
                )
                result.success to (result.error ?: result.result.toString())
            }
        }
        return opResponse(success, message, agentId, displayId)
    }

    private fun handleRunAgent(payload: JSONObject?): Response {
        val intent = payload?.optStringOrNull("intent") ?: return badRequest("Missing intent")
        val request = RemoteAgentRunRequest(
            intent = intent,
            targetApp = payload.optStringOrNull("targetApp") ?: payload.optStringOrNull("target_app"),
            maxSteps = payload.optIntCompat("maxSteps")
                ?: payload.optIntCompat("max_steps")
                ?: 20,
            agentId = payload.optStringOrNull("agentId") ?: payload.optStringOrNull("agent_id")
        )
        val snapshot = RemoteAgentTaskManager.startTask(appContext, request)
        return jsonResponse(Response.Status.OK, RemoteApiEnvelope(ok = true, data = snapshot))
    }

    private fun handleAgentState(uri: String): Response {
        val taskId = uri.removePrefix("/api/remote/agent/").removeSuffix("/state")
        val snapshot = RemoteAgentTaskManager.getTask(taskId) ?: return notFound("Agent task not found")
        return jsonResponse(Response.Status.OK, RemoteApiEnvelope(ok = true, data = snapshot))
    }

    private fun handleAgentCancel(uri: String): Response {
        val taskId = uri.removePrefix("/api/remote/agent/").removeSuffix("/cancel")
        val cancelled = RemoteAgentTaskManager.cancelTask(taskId)
        return if (cancelled) {
            jsonResponse(
                Response.Status.OK,
                RemoteApiEnvelope(ok = true, data = buildJsonObject { put("cancelled", true) })
            )
        } else {
            notFound("Agent task not found")
        }
    }

    private fun handleMemoryQuery(session: IHTTPSession): Response {
        val query = session.parameters["query"]?.firstOrNull()?.takeIf { it.isNotBlank() } ?: "*"
        val folderPath = session.parameters["folder_path"]?.firstOrNull()
        val limit = session.parameters["limit"]?.firstOrNull()?.toIntOrNull() ?: 20
        val repo = memoryRepository()
        val settings = memorySettings()
        val memories = runBlocking {
            repo.searchMemories(
                query = query,
                folderPath = folderPath,
                semanticThreshold = settings.semanticThreshold,
                scoreMode = settings.scoreMode,
                keywordWeight = settings.keywordWeight,
                semanticWeight = settings.vectorWeight,
                edgeWeight = settings.edgeWeight
            )
        }.take(limit.coerceIn(1, 500))
        return jsonResponse(
            Response.Status.OK,
            RemoteApiEnvelope(
                ok = true,
                data = RemoteMemoryQueryPayload(memories.size, memories.map { it.toRemotePayload() })
            )
        )
    }

    private fun handleMemoryItem(session: IHTTPSession): Response {
        val uuid = session.parameters["uuid"]?.firstOrNull()
        val title = session.parameters["title"]?.firstOrNull()
        val memory = runBlocking {
            when {
                !uuid.isNullOrBlank() -> memoryRepository().findMemoryByUuid(uuid)
                !title.isNullOrBlank() -> memoryRepository().findMemoryByTitle(title)
                else -> null
            }
        } ?: return notFound("Memory not found")
        return jsonResponse(Response.Status.OK, RemoteApiEnvelope(ok = true, data = memory.toRemotePayload()))
    }

    private fun handleMemoryDocument(session: IHTTPSession): Response {
        val uuid = session.parameters["uuid"]?.firstOrNull()
        val title = session.parameters["title"]?.firstOrNull()
        val repo = memoryRepository()
        val memory = runBlocking {
            when {
                !uuid.isNullOrBlank() -> repo.findMemoryByUuid(uuid)
                !title.isNullOrBlank() -> repo.findMemoryByTitle(title)
                else -> null
            }
        } ?: return notFound("Memory not found")
        if (!memory.isDocumentNode) {
            return badRequest("Target memory is not a document node")
        }

        val totalChunks = runBlocking { repo.getTotalChunkCount(memory.id) }
        val query = session.parameters["query"]?.firstOrNull()
        val chunkIndex = session.parameters["chunk_index"]?.firstOrNull()?.toIntOrNull()
        val chunkRange = session.parameters["chunk_range"]?.firstOrNull()
        val chunks = runBlocking {
            when {
                !query.isNullOrBlank() -> repo.searchChunksInDocument(memory.id, query)
                chunkIndex != null -> listOfNotNull(repo.getChunkByIndex(memory.id, (chunkIndex - 1).coerceAtLeast(0)))
                !chunkRange.isNullOrBlank() -> {
                    val parts = chunkRange.split('-')
                    if (parts.size != 2) emptyList()
                    else {
                        val start = (parts[0].trim().toIntOrNull() ?: 1) - 1
                        val end = (parts[1].trim().toIntOrNull() ?: totalChunks) - 1
                        repo.getChunksByRange(memory.id, start.coerceAtLeast(0), end.coerceAtLeast(start.coerceAtLeast(0)))
                    }
                }
                else -> repo.getChunksByRange(memory.id, 0, (totalChunks - 1).coerceAtLeast(0))
            }
        }
        return jsonResponse(
            Response.Status.OK,
            RemoteApiEnvelope(
                ok = true,
                data = RemoteDocumentPayload(
                    memory = memory.toRemotePayload(),
                    totalChunks = totalChunks,
                    chunks = chunks.map {
                        RemoteDocumentChunkPayload(
                            chunkIndex = it.chunkIndex + 1,
                            content = it.content
                        )
                    }
                )
            )
        )
    }

    private fun handleCreateMemory(payload: JSONObject?): Response {
        val title = payload?.optStringOrNull("title") ?: return badRequest("Missing title")
        val content = payload.optStringOrNull("content") ?: return badRequest("Missing content")
        val created = runBlocking {
            memoryRepository().createMemory(
                title = title,
                content = content,
                contentType = payload.optStringOrNull("contentType") ?: payload.optStringOrNull("content_type") ?: "text/plain",
                source = payload.optStringOrNull("source") ?: "remote_api",
                folderPath = payload.optStringOrNull("folderPath") ?: payload.optStringOrNull("folder_path") ?: "",
                tags = payload.optStringList("tags")
            )
        } ?: return badRequest("Failed to create memory")

        return jsonResponse(Response.Status.OK, RemoteApiEnvelope(ok = true, data = created.toRemotePayload()))
    }

    private fun handleUpdateMemory(payload: JSONObject?): Response {
        val repo = memoryRepository()
        val memory = runBlocking {
            when {
                !payload?.optStringOrNull("uuid").isNullOrBlank() -> repo.findMemoryByUuid(payload!!.optString("uuid"))
                !payload?.optStringOrNull("oldTitle").isNullOrBlank() -> repo.findMemoryByTitle(payload!!.optString("oldTitle"))
                !payload?.optStringOrNull("old_title").isNullOrBlank() -> repo.findMemoryByTitle(payload!!.optString("old_title"))
                !payload?.optStringOrNull("title").isNullOrBlank() -> repo.findMemoryByTitle(payload!!.optString("title"))
                else -> null
            }
        } ?: return notFound("Memory not found")

        val updated = runBlocking {
            repo.updateMemory(
                memory = memory,
                newTitle = payload.optStringOrNull("newTitle")
                    ?: payload.optStringOrNull("new_title")
                    ?: memory.title,
                newContent = payload.optStringOrNull("content") ?: memory.content,
                newContentType = payload.optStringOrNull("contentType")
                    ?: payload.optStringOrNull("content_type")
                    ?: memory.contentType,
                newSource = payload.optStringOrNull("source") ?: memory.source,
                newCredibility = payload.optFloatCompat("credibility") ?: memory.credibility,
                newImportance = payload.optFloatCompat("importance") ?: memory.importance,
                newFolderPath = payload.optStringOrNull("folderPath")
                    ?: payload.optStringOrNull("folder_path")
                    ?: memory.folderPath,
                newTags = payload.optStringList("tags")
            )
        } ?: return badRequest("Failed to update memory")

        return jsonResponse(Response.Status.OK, RemoteApiEnvelope(ok = true, data = updated.toRemotePayload()))
    }

    private fun handleDeleteMemory(payload: JSONObject?): Response {
        val repo = memoryRepository()
        val memory = runBlocking {
            when {
                !payload?.optStringOrNull("uuid").isNullOrBlank() -> repo.findMemoryByUuid(payload!!.optString("uuid"))
                !payload?.optStringOrNull("title").isNullOrBlank() -> repo.findMemoryByTitle(payload!!.optString("title"))
                else -> null
            }
        } ?: return notFound("Memory not found")

        val deleted = runBlocking { repo.deleteMemoryAndIndex(memory.id) }
        return if (deleted) {
            jsonResponse(
                Response.Status.OK,
                RemoteApiEnvelope(ok = true, data = buildJsonObject { put("deleted", true); put("uuid", memory.uuid) })
            )
        } else {
            badRequest("Failed to delete memory")
        }
    }

    private fun handleLinkMemory(payload: JSONObject?): Response {
        val sourceTitle = payload?.optStringOrNull("sourceTitle") ?: payload?.optStringOrNull("source_title")
            ?: return badRequest("Missing sourceTitle")
        val targetTitle = payload.optStringOrNull("targetTitle") ?: payload.optStringOrNull("target_title")
            ?: return badRequest("Missing targetTitle")
        val linkType = payload.optStringOrNull("linkType") ?: payload.optStringOrNull("link_type")
            ?: "RELATED"
        val weight = payload.optFloatCompat("weight") ?: 0.7f
        val description = payload.optStringOrNull("description") ?: ""
        val repo = memoryRepository()
        val source = runBlocking { repo.findMemoryByTitle(sourceTitle) } ?: return notFound("Source memory not found")
        val target = runBlocking { repo.findMemoryByTitle(targetTitle) } ?: return notFound("Target memory not found")

        runBlocking { repo.linkMemories(source, target, linkType, weight, description) }
        val links = runBlocking {
            repo.queryMemoryLinks(
                sourceMemoryId = source.id,
                targetMemoryId = target.id,
                linkType = linkType,
                limit = 1
            )
        }
        val link = links.firstOrNull()
            ?: return badRequest("Link creation did not return a persisted link")
        return jsonResponse(
            Response.Status.OK,
            RemoteApiEnvelope(ok = true, data = link.toRemotePayload())
        )
    }

    private fun handleMemoryLinks(session: IHTTPSession): Response {
        val repo = memoryRepository()
        val linkId = session.parameters["link_id"]?.firstOrNull()?.toLongOrNull()
        val sourceTitle = session.parameters["source_title"]?.firstOrNull()
        val targetTitle = session.parameters["target_title"]?.firstOrNull()
        val linkType = session.parameters["link_type"]?.firstOrNull()
        val limit = session.parameters["limit"]?.firstOrNull()?.toIntOrNull() ?: 20

        val sourceId = if (!sourceTitle.isNullOrBlank()) runBlocking { repo.findMemoryByTitle(sourceTitle) }?.id else null
        val targetId = if (!targetTitle.isNullOrBlank()) runBlocking { repo.findMemoryByTitle(targetTitle) }?.id else null

        val links = runBlocking {
            repo.queryMemoryLinks(
                linkId = linkId,
                sourceMemoryId = sourceId,
                targetMemoryId = targetId,
                linkType = linkType,
                limit = limit
            )
        }
        return jsonResponse(
            Response.Status.OK,
            RemoteApiEnvelope(
                ok = true,
                data = RemoteMemoryLinksPayload(
                    totalCount = links.size,
                    links = links.mapNotNull { it.toRemotePayloadOrNull() }
                )
            )
        )
    }

    private fun handleMemoryGraph(): Response {
        val graph = runBlocking { memoryRepository().getMemoryGraph() }
        return jsonResponse(
            Response.Status.OK,
            RemoteApiEnvelope(ok = true, data = graph.toRemotePayload())
        )
    }

    private fun memoryRepository(): MemoryRepository {
        val profileId = runBlocking { preferencesManager.activeProfileIdFlow.first() }
        return MemoryRepository(appContext, profileId)
    }

    private fun memorySettings() = runBlocking {
        val profileId = preferencesManager.activeProfileIdFlow.first()
        MemorySearchSettingsPreferences(appContext, profileId).load()
    }

    private fun parsePayload(session: IHTTPSession): JSONObject? {
        if (session.method == Method.GET || session.method == Method.HEAD) {
            return null
        }
        val contentType = session.headers["content-type"]?.lowercase(Locale.US).orEmpty()
        if (contentType.contains("application/json")) {
            return parseJsonPayload(session)
        }
        return try {
            val tempFiles = HashMap<String, String>()
            session.parseBody(tempFiles)
            val postDataPath = tempFiles["postData"] ?: return null
            val raw = File(postDataPath).takeIf { it.exists() }?.readText().orEmpty().trim()
            if (raw.isBlank()) null else JSONObject(raw)
        } catch (e: Exception) {
            AppLogger.w(TAG, "Failed to parse request body for ${session.uri}", e)
            null
        }
    }

    private fun parseJsonPayload(session: IHTTPSession): JSONObject? {
        return try {
            val declaredLength = session.headers["content-length"]?.trim()?.toIntOrNull()
            val rawBytes = readRequestBody(session, declaredLength)
            val raw = rawBytes.toString(StandardCharsets.UTF_8).trim()
            if (raw.isBlank()) {
                null
            } else {
                JSONObject(raw)
            }
        } catch (e: Exception) {
            AppLogger.w(TAG, "Failed to parse JSON request body for ${session.uri}", e)
            null
        }
    }

    private fun readRequestBody(session: IHTTPSession, declaredLength: Int?): ByteArray {
        val input = session.inputStream ?: return ByteArray(0)
        if (declaredLength != null && declaredLength > 0) {
            val body = ByteArray(declaredLength)
            var offset = 0
            while (offset < declaredLength) {
                val read = input.read(body, offset, declaredLength - offset)
                if (read <= 0) break
                offset += read
            }
            return if (offset == body.size) body else body.copyOf(offset)
        }

        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        val output = ByteArrayOutputStream()
        while (true) {
            val read = input.read(buffer)
            if (read <= 0) break
            output.write(buffer, 0, read)
            if (declaredLength == null && read < buffer.size) {
                break
            }
        }
        return output.toByteArray()
    }

    private fun requireAuth(session: IHTTPSession): Response? {
        val authHeader = session.headers["authorization"]?.trim().orEmpty()
        val bearerToken = if (authHeader.startsWith("Bearer ", ignoreCase = true)) {
            authHeader.substringAfter("Bearer ").trim()
        } else {
            null
        }
        return if (RemoteSessionManager.validateToken(bearerToken)) {
            null
        } else {
            jsonResponse(
                Response.Status.UNAUTHORIZED,
                RemoteApiEnvelope<RemoteServerInfo>(ok = false, error = "Unauthorized")
            )
        }
    }

    private fun opResponse(success: Boolean, message: String, agentId: String?, displayId: Int?): Response {
        val status = if (success) Response.Status.OK else Response.Status.BAD_REQUEST
        return jsonResponse(
            status,
            RemoteApiEnvelope(
                ok = success,
                data = RemoteOperationPayload(
                    success = success,
                    message = message,
                    agentId = agentId,
                    displayId = displayId
                ),
                error = if (success) null else message
            )
        )
    }

    private fun badRequest(message: String): Response =
        jsonResponse(
            Response.Status.BAD_REQUEST,
            RemoteApiEnvelope<RemoteServerInfo>(ok = false, error = message)
        )

    private fun notFound(message: String): Response =
        jsonResponse(
            Response.Status.NOT_FOUND,
            RemoteApiEnvelope<RemoteServerInfo>(ok = false, error = message)
        )

    private inline fun <reified T> jsonResponse(status: Response.Status, payload: T): Response {
        val body = json.encodeToString(payload)
        return newFixedLengthResponse(status, "application/json", body).apply {
            addHeader("Access-Control-Allow-Origin", "*")
            addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
            addHeader("Access-Control-Allow-Headers", "Authorization, Content-Type")
        }
    }

    private fun inferMimeType(path: String): String {
        return when (path.substringAfterLast('.', "").lowercase(Locale.US)) {
            "jpg", "jpeg" -> "image/jpeg"
            "webp" -> "image/webp"
            else -> "image/png"
        }
    }

    private fun RemoteSessionSnapshot.toPublicSnapshot(): RemoteSessionPublicSnapshot =
        RemoteSessionPublicSnapshot(
            sessionId = sessionId,
            clientName = clientName,
            createdAtEpochMs = createdAtEpochMs,
            lastSeenAtEpochMs = lastSeenAtEpochMs
        )

    private fun com.ai.assistance.metaagent.data.model.Memory.toRemotePayload(): RemoteMemoryPayload =
        RemoteMemoryPayload(
            id = id,
            uuid = uuid,
            title = title,
            content = content,
            contentType = contentType,
            source = source,
            credibility = credibility,
            importance = importance,
            folderPath = folderPath,
            tags = tags.map { it.name },
            isDocumentNode = isDocumentNode,
            documentPath = documentPath,
            createdAtEpochMs = createdAt.time,
            updatedAtEpochMs = updatedAt.time,
            lastAccessedAtEpochMs = lastAccessedAt.time
        )

    private fun com.ai.assistance.metaagent.data.model.MemoryLink.toRemotePayloadOrNull(): RemoteMemoryLinkPayload? {
        val source = source.target ?: return null
        val target = target.target ?: return null
        return RemoteMemoryLinkPayload(
            linkId = id,
            sourceTitle = source.title,
            targetTitle = target.title,
            linkType = type,
            weight = weight,
            description = description
        )
    }

    private fun com.ai.assistance.metaagent.data.model.MemoryLink.toRemotePayload(): RemoteMemoryLinkPayload =
        toRemotePayloadOrNull()
            ?: RemoteMemoryLinkPayload(
                linkId = id,
                sourceTitle = "",
                targetTitle = "",
                linkType = type,
                weight = weight,
                description = description
            )

    private fun Graph.toRemotePayload(): RemoteMemoryGraphPayload =
        RemoteMemoryGraphPayload(
            nodeCount = nodes.size,
            edgeCount = edges.size,
            nodes = nodes.map { RemoteMemoryGraphNodePayload(id = it.id, label = it.label) },
            edges = edges.map {
                RemoteMemoryGraphEdgePayload(
                    id = it.id,
                    sourceId = it.sourceId,
                    targetId = it.targetId,
                    label = it.label,
                    weight = it.weight,
                    isCrossFolderLink = it.isCrossFolderLink
                )
            }
        )

    private fun JSONObject?.optStringOrNull(key: String): String? =
        this?.optString(key)?.takeIf { !it.isNullOrBlank() }

    private fun JSONObject?.optIntCompat(key: String): Int? =
        this?.takeIf { it.has(key) && !it.isNull(key) }?.optInt(key)

    private fun JSONObject?.optLongCompat(key: String): Long? =
        this?.takeIf { it.has(key) && !it.isNull(key) }?.optLong(key)

    private fun JSONObject?.optFloatCompat(key: String): Float? =
        this?.takeIf { it.has(key) && !it.isNull(key) }?.optDouble(key)?.toFloat()

    private fun JSONObject?.optStringList(key: String): List<String>? {
        if (this == null || !has(key) || isNull(key)) return null
        val value = get(key)
        return when (value) {
            is org.json.JSONArray -> {
                buildList {
                    for (index in 0 until value.length()) {
                        val item = value.optString(index).trim()
                        if (item.isNotEmpty()) add(item)
                    }
                }.distinct()
            }
            is String -> value.split(',', '|', '\n').map { it.trim() }.filter { it.isNotEmpty() }.distinct()
            else -> null
        }
    }
}
