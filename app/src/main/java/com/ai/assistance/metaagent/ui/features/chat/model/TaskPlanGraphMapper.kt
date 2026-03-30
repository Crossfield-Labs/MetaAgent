package com.ai.assistance.metaagent.ui.features.chat.model

import com.ai.assistance.metaagent.core.plan.model.PlanNode
import com.ai.assistance.metaagent.core.plan.model.PlanNodeAdapter
import com.ai.assistance.metaagent.core.plan.model.PlanNodeStatus
import com.ai.assistance.metaagent.core.plan.model.TaskSession
import com.ai.assistance.metaagent.core.workflow.NodeExecutionState
import com.ai.assistance.metaagent.data.model.ExecuteNode
import com.ai.assistance.metaagent.data.model.NodePosition
import com.ai.assistance.metaagent.data.model.TriggerNode
import com.ai.assistance.metaagent.data.model.WorkflowNode
import com.ai.assistance.metaagent.data.model.WorkflowNodeConnection
import kotlin.math.max

data class TaskPlanGraph(
    val nodes: List<WorkflowNode>,
    val connections: List<WorkflowNodeConnection>,
    val executionStates: Map<String, NodeExecutionState>,
)

object TaskPlanGraphMapper {
    private const val ROOT_X = 120f
    private const val ROOT_Y = 180f
    private const val LEVEL_GAP_X = 280f
    private const val LEVEL_GAP_Y = 180f

    fun map(taskSession: TaskSession): TaskPlanGraph {
        val planNodes = taskSession.planNodes
        val rootId = "task-root-${taskSession.taskId}"
        val nodeById = planNodes.associateBy { it.id }
        val levelCache = mutableMapOf<String, Int>()

        fun levelFor(node: PlanNode): Int {
            return levelCache.getOrPut(node.id) {
                if (node.dependsOn.isEmpty()) {
                    1
                } else {
                    node.dependsOn.mapNotNull(nodeById::get).maxOf { parent -> levelFor(parent) } + 1
                }
            }
        }

        val groupedByLevel = planNodes.groupBy(::levelFor).toSortedMap()
        val workflowNodes = mutableListOf<WorkflowNode>()
        val workflowConnections = mutableListOf<WorkflowNodeConnection>()
        val executionStates = mutableMapOf<String, NodeExecutionState>()

        workflowNodes += TriggerNode(
            id = rootId,
            name = "开始",
            description = taskSession.goal,
            position = NodePosition(ROOT_X, ROOT_Y),
            triggerType = "manual"
        )
        executionStates[rootId] = if (planNodes.isEmpty()) {
            NodeExecutionState.Pending
        } else {
            NodeExecutionState.Success("Task initialized")
        }

        groupedByLevel.forEach { (level, nodesAtLevel) ->
            nodesAtLevel.forEachIndexed { index, node ->
                val x = ROOT_X + LEVEL_GAP_X * level
                val y = ROOT_Y + LEVEL_GAP_Y * index
                workflowNodes += ExecuteNode(
                    id = node.id,
                    name = node.title,
                    description = buildNodeSubtitle(node),
                    position = NodePosition(x, y),
                    actionType = node.adapter.name.lowercase()
                )
                executionStates[node.id] = mapExecutionState(node)

                if (node.dependsOn.isEmpty()) {
                    workflowConnections += WorkflowNodeConnection(
                        sourceNodeId = rootId,
                        targetNodeId = node.id
                    )
                } else {
                    node.dependsOn.forEach { dependencyId ->
                        workflowConnections += WorkflowNodeConnection(
                            sourceNodeId = dependencyId,
                            targetNodeId = node.id
                        )
                    }
                }
            }
        }

        return TaskPlanGraph(
            nodes = workflowNodes,
            connections = workflowConnections,
            executionStates = executionStates
        )
    }

    private fun buildNodeSubtitle(node: PlanNode): String {
        val primary = when {
            node.resultSummary.isNotBlank() -> node.resultSummary
            node.detail.isNotBlank() -> node.detail
            node.explainToUser.isNotBlank() -> node.explainToUser
            else -> node.goal
        }.trim()

        val pcSummary = buildList {
            if (node.adapter == PlanNodeAdapter.PC && node.workerName.isNotBlank()) {
                add("worker=${node.workerName}")
            }
            if (node.workerProfile.isNotBlank()) {
                add("profile=${node.workerProfile}")
            }
            if (node.pcPhase.isNotBlank()) {
                add("phase=${node.pcPhase}")
            }
        }.joinToString(" · ")

        return listOf(primary, pcSummary)
            .filter { it.isNotBlank() }
            .joinToString("\n")
    }

    private fun mapExecutionState(node: PlanNode): NodeExecutionState {
        return when (node.status) {
            PlanNodeStatus.PENDING -> NodeExecutionState.Pending
            PlanNodeStatus.RUNNING -> NodeExecutionState.Running
            PlanNodeStatus.DONE -> NodeExecutionState.Success(
                node.resultSummary.ifBlank { node.detail.ifBlank { "Done" } }
            )
            PlanNodeStatus.BLOCKED -> NodeExecutionState.Running
            PlanNodeStatus.SKIPPED -> NodeExecutionState.Skipped(node.detail)
            PlanNodeStatus.CANCELLED -> NodeExecutionState.Skipped(node.detail.ifBlank { "Cancelled" })
            PlanNodeStatus.FAILED -> NodeExecutionState.Failed(
                node.detail.ifBlank { node.resultSummary.ifBlank { "Failed" } }
            )
        }
    }
}
