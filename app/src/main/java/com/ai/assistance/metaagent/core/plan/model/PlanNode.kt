package com.ai.assistance.metaagent.core.plan.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
enum class PlanNodeKind {
    EXEC,
    CLARIFY,
    CHECKPOINT,
    REPLAN
}

@Serializable
enum class PlanNodeStatus {
    PENDING,
    RUNNING,
    DONE,
    FAILED,
    BLOCKED,
    SKIPPED,
    CANCELLED
}

@Serializable
enum class PlanNodeAdapter {
    CHAT,
    TOOL,
    CLAUDE,
    CLI,
    ANDROID,
    LOCAL_RUNNER,
    PC
}

/**
 * Workflow-style parameter values:
 * - StaticValue: direct literal string
 * - NodeReference: resolve from a previous node result at execution time
 */
@Serializable
sealed class PlanNodeParameterValue {
    @Serializable
    data class StaticValue(val value: String) : PlanNodeParameterValue()

    @Serializable
    data class NodeReference(val nodeId: String) : PlanNodeParameterValue()
}

@Serializable
data class PlanNode(
    val id: String = UUID.randomUUID().toString().take(8),
    val title: String,
    val kind: PlanNodeKind = PlanNodeKind.EXEC,
    val goal: String = "",
    val status: PlanNodeStatus = PlanNodeStatus.PENDING,
    val adapter: PlanNodeAdapter = PlanNodeAdapter.CHAT,
    val dependsOn: List<String> = emptyList(),
    val progress: Float = 0f,
    val detail: String = "",
    val confidence: Float = 1.0f,
    val requiresApproval: Boolean = false,
    val explainToUser: String = "",
    val resultSummary: String = "",
    val resultData: String = "",
    val artifacts: List<String> = emptyList(),
    val runtimeSessionId: String = "",
    val awaitingUserPrompt: String = "",
    val pendingUserInput: String = "",
    val pcPhase: String = "",
    val workerName: String = "",
    val workerSessionMode: String = "",
    val workerProfile: String = "",
    val workerCanInterrupt: Boolean = false,
    val workerTaskId: String = "",
    val workerSummary: String = "",
    val artifactSummary: String = "",
    val permissionSummary: String = "",
    val sessionInfoSummary: String = "",
    val mcpStatusSummary: String = "",
    val recentHookEvents: List<String> = emptyList(),
    val pcSnapshotVersion: Int = 0,
    val toolName: String = "",
    val toolParams: Map<String, PlanNodeParameterValue> = emptyMap(),
    val children: List<PlanNode> = emptyList(),
    val version: Int = 1
)
