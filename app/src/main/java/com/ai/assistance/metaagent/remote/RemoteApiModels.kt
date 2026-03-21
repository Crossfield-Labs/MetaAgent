package com.ai.assistance.metaagent.remote

import kotlinx.serialization.Serializable

@Serializable
data class RemoteApiEnvelope<T>(
    val ok: Boolean,
    val data: T? = null,
    val error: String? = null
)

@Serializable
data class RemoteServerInfo(
    val service: String,
    val port: Int,
    val hasActiveSession: Boolean,
    val apiVersion: Int = 1
)

@Serializable
data class RemoteSessionPublicSnapshot(
    val sessionId: String,
    val clientName: String,
    val createdAtEpochMs: Long,
    val lastSeenAtEpochMs: Long
)

@Serializable
data class RemoteCapabilitiesPayload(
    val permissionLevel: String,
    val accessibilityEnabled: Boolean,
    val shizukuRunning: Boolean,
    val shizukuGranted: Boolean,
    val experimentalVirtualDisplayEnabled: Boolean,
    val activeDisplayId: Int?,
    val sdkInt: Int
)

@Serializable
data class RemoteHeartbeatPayload(
    val server: RemoteServerInfo,
    val session: RemoteSessionPublicSnapshot?,
    val capabilities: RemoteCapabilitiesPayload,
    val activeTaskCount: Int,
    val totalTaskCount: Int,
    val recentTasks: List<RemoteAgentTaskSnapshot>
)

@Serializable
data class RemoteOperationPayload(
    val success: Boolean,
    val message: String,
    val agentId: String? = null,
    val displayId: Int? = null
)

@Serializable
data class RemoteScreenshotPayload(
    val agentId: String,
    val width: Int?,
    val height: Int?,
    val mimeType: String,
    val base64: String,
    val source: String
)

@Serializable
data class RemoteAgentRunRequest(
    val intent: String,
    val targetApp: String? = null,
    val maxSteps: Int = 20,
    val agentId: String? = null
)

@Serializable
data class RemoteAgentTaskSnapshot(
    val taskId: String,
    val agentId: String,
    val intent: String,
    val targetApp: String?,
    val maxSteps: Int,
    val status: String,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
    val executionSteps: Int = 0,
    val displayId: Int? = null,
    val success: Boolean? = null,
    val finalMessage: String? = null,
    val error: String? = null
)

@Serializable
data class RemoteMemoryPayload(
    val id: Long,
    val uuid: String,
    val title: String,
    val content: String,
    val contentType: String,
    val source: String,
    val credibility: Float,
    val importance: Float,
    val folderPath: String?,
    val tags: List<String>,
    val isDocumentNode: Boolean,
    val documentPath: String?,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
    val lastAccessedAtEpochMs: Long
)

@Serializable
data class RemoteMemoryQueryPayload(
    val totalCount: Int,
    val memories: List<RemoteMemoryPayload>
)

@Serializable
data class RemoteDocumentChunkPayload(
    val chunkIndex: Int,
    val content: String
)

@Serializable
data class RemoteDocumentPayload(
    val memory: RemoteMemoryPayload,
    val totalChunks: Int,
    val chunks: List<RemoteDocumentChunkPayload>
)

@Serializable
data class RemoteMemoryLinkPayload(
    val linkId: Long,
    val sourceTitle: String,
    val targetTitle: String,
    val linkType: String,
    val weight: Float,
    val description: String
)

@Serializable
data class RemoteMemoryLinksPayload(
    val totalCount: Int,
    val links: List<RemoteMemoryLinkPayload>
)

@Serializable
data class RemoteMemoryGraphNodePayload(
    val id: String,
    val label: String
)

@Serializable
data class RemoteMemoryGraphEdgePayload(
    val id: Long,
    val sourceId: String,
    val targetId: String,
    val label: String?,
    val weight: Float,
    val isCrossFolderLink: Boolean
)

@Serializable
data class RemoteMemoryGraphPayload(
    val nodeCount: Int,
    val edgeCount: Int,
    val nodes: List<RemoteMemoryGraphNodePayload>,
    val edges: List<RemoteMemoryGraphEdgePayload>
)
