package com.ai.assistance.metaagent.core.plan.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
enum class TaskSessionStatus {
    DRAFT,
    PLANNED,
    AWAITING_APPROVAL,
    RUNNING,
    PAUSED,
    WAITING_USER,
    COMPLETED,
    FAILED,
    CANCELLED
}

@Serializable
data class TaskSession(
    val taskId: String = UUID.randomUUID().toString().take(12),
    val goal: String,
    val status: TaskSessionStatus = TaskSessionStatus.DRAFT,
    val planNodes: List<PlanNode> = emptyList(),
    val events: List<TaskEvent> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val replanCount: Int = 0,
    val executionStartedAt: Long? = null,
    val completedAt: Long? = null,
    val resultSummary: String = "",
    val activeNodeId: String? = null,
    val nextSeq: Int = 1
) {
    val activeNode: PlanNode?
        get() = planNodes.find { it.id == activeNodeId }

    val completedNodeCount: Int
        get() = planNodes.count { it.status == PlanNodeStatus.DONE }

    val overallProgress: Float
        get() = if (planNodes.isEmpty()) 0f else completedNodeCount.toFloat() / planNodes.size

    val isActive: Boolean
        get() = status in listOf(
            TaskSessionStatus.RUNNING,
            TaskSessionStatus.PAUSED,
            TaskSessionStatus.WAITING_USER
        )

    val isTerminal: Boolean
        get() = status in listOf(
            TaskSessionStatus.COMPLETED,
            TaskSessionStatus.FAILED,
            TaskSessionStatus.CANCELLED
        )

    fun appendEvent(
        type: TaskEventType,
        nodeId: String? = null,
        message: String = "",
        progress: Float? = null,
        data: String? = null
    ): TaskSession {
        val event = TaskEvent(
            seq = nextSeq,
            type = type,
            taskId = taskId,
            nodeId = nodeId,
            message = message,
            progress = progress,
            data = data
        )
        return copy(
            events = events + event,
            nextSeq = nextSeq + 1,
            updatedAt = System.currentTimeMillis()
        )
    }

    fun updateNode(nodeId: String, transform: (PlanNode) -> PlanNode): TaskSession {
        return copy(
            planNodes = planNodes.map { if (it.id == nodeId) transform(it) else it },
            updatedAt = System.currentTimeMillis()
        )
    }

    fun findNextExecutableNode(): PlanNode? {
        val completedIds = planNodes.filter { it.status == PlanNodeStatus.DONE }.map { it.id }.toSet()
        return planNodes.firstOrNull { node ->
            node.status == PlanNodeStatus.PENDING && node.dependsOn.all { it in completedIds }
        }
    }

    fun findNode(nodeId: String): PlanNode? = planNodes.find { it.id == nodeId }

    fun findDependencyNodes(node: PlanNode, onlyCompleted: Boolean = false): List<PlanNode> {
        val nodeIndex = planNodes.associateBy { it.id }
        val visited = mutableSetOf<String>()
        val ordered = mutableListOf<PlanNode>()

        fun visit(id: String) {
            if (!visited.add(id)) return
            val dependency = nodeIndex[id] ?: return
            dependency.dependsOn.forEach(::visit)
            if (!onlyCompleted || dependency.status == PlanNodeStatus.DONE) {
                ordered += dependency
            }
        }

        node.dependsOn.forEach(::visit)
        return ordered
    }
}
