package com.ai.assistance.metaagent.remote

import android.content.Context
import com.ai.assistance.metaagent.core.tools.AutomationExecutionResult
import com.ai.assistance.metaagent.core.tools.StringResultData
import com.ai.assistance.metaagent.core.tools.ToolResultData
import com.ai.assistance.metaagent.core.tools.agent.PhoneAgentJobRegistry
import com.ai.assistance.metaagent.core.tools.defaultTool.ToolGetter
import com.ai.assistance.metaagent.data.model.AITool
import com.ai.assistance.metaagent.data.model.ToolParameter
import com.ai.assistance.metaagent.util.AppLogger
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

object RemoteAgentTaskManager {
    private const val TAG = "RemoteAgentTaskManager"

    @Volatile
    private var scope = createScope()
    private val tasks = ConcurrentHashMap<String, RemoteAgentTaskSnapshot>()
    private val jobs = ConcurrentHashMap<String, Job>()

    fun startTask(context: Context, request: RemoteAgentRunRequest): RemoteAgentTaskSnapshot {
        val taskScope = ensureScope()
        val taskId = UUID.randomUUID().toString()
        val agentId = request.agentId?.takeIf { it.isNotBlank() } ?: "remote-${taskId.take(8)}"
        val now = System.currentTimeMillis()
        val initial = RemoteAgentTaskSnapshot(
            taskId = taskId,
            agentId = agentId,
            intent = request.intent,
            targetApp = request.targetApp,
            maxSteps = request.maxSteps.coerceAtLeast(1),
            status = "running",
            createdAtEpochMs = now,
            updatedAtEpochMs = now
        )
        tasks[taskId] = initial

        val job = taskScope.launch {
            val uiTools = ToolGetter.getUITools(context.applicationContext)
            try {
                val tool = AITool(
                    name = "run_ui_subagent",
                    parameters = buildList {
                        add(ToolParameter("intent", request.intent))
                        add(ToolParameter("max_steps", request.maxSteps.coerceAtLeast(1).toString()))
                        add(ToolParameter("agent_id", agentId))
                        request.targetApp?.takeIf { it.isNotBlank() }?.let {
                            add(ToolParameter("target_app", it))
                        }
                    }
                )
                val result = uiTools.runUiSubAgent(tool)
                updateFromToolResult(taskId, result.result, result.success, result.error)
            } catch (e: CancellationException) {
                AppLogger.w(TAG, "Agent task cancelled taskId=$taskId agentId=$agentId", e)
                updateTask(
                    taskId = taskId,
                    status = "cancelled",
                    success = false,
                    finalMessage = "Cancelled",
                    error = e.message ?: "Cancelled"
                )
                throw e
            } catch (e: Exception) {
                AppLogger.e(TAG, "Agent task failed taskId=$taskId agentId=$agentId", e)
                updateTask(
                    taskId = taskId,
                    status = "failed",
                    success = false,
                    error = e.message ?: "Unknown error"
                )
            } finally {
                jobs.remove(taskId)
            }
        }
        jobs[taskId] = job
        return initial
    }

    fun getTask(taskId: String): RemoteAgentTaskSnapshot? = tasks[taskId]

    fun listTasks(): List<RemoteAgentTaskSnapshot> =
        tasks.values.sortedByDescending { it.createdAtEpochMs }

    fun activeTaskCount(): Int =
        tasks.values.count { it.status == "running" }

    fun totalTaskCount(): Int = tasks.size

    fun cancelTask(taskId: String): Boolean {
        val snapshot = tasks[taskId] ?: return false
        PhoneAgentJobRegistry.cancelAgent(snapshot.agentId, "Cancelled by remote client")
        jobs.remove(taskId)?.cancel(CancellationException("Cancelled by remote client"))
        updateTask(
            taskId = taskId,
            status = "cancelled",
            success = false,
            finalMessage = "Cancelled",
            error = "Cancelled by remote client"
        )
        return true
    }

    fun shutdown() {
        jobs.values.forEach { job -> job.cancel(CancellationException("Remote service shutdown")) }
        jobs.clear()
        scope.cancel("Remote service shutdown")
        scope = createScope()
    }

    private fun ensureScope(): CoroutineScope {
        val existing = scope
        return if (existing.coroutineContext[Job]?.isCancelled == true) {
            createScope().also { scope = it }
        } else {
            existing
        }
    }

    private fun createScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private fun updateFromToolResult(
        taskId: String,
        resultData: ToolResultData,
        success: Boolean,
        error: String?
    ) {
        when (resultData) {
            is AutomationExecutionResult -> {
                updateTask(
                    taskId = taskId,
                    status = if (success && resultData.executionSuccess) "completed" else "failed",
                    success = success && resultData.executionSuccess,
                    executionSteps = resultData.executionSteps,
                    displayId = resultData.displayId,
                    finalMessage = resultData.executionMessage,
                    error = error ?: resultData.executionError
                )
            }
            is StringResultData -> {
                updateTask(
                    taskId = taskId,
                    status = if (success) "completed" else "failed",
                    success = success,
                    finalMessage = resultData.value,
                    error = error
                )
            }
            else -> {
                updateTask(
                    taskId = taskId,
                    status = if (success) "completed" else "failed",
                    success = success,
                    finalMessage = resultData.toString(),
                    error = error
                )
            }
        }
    }

    private fun updateTask(
        taskId: String,
        status: String,
        success: Boolean?,
        executionSteps: Int? = null,
        displayId: Int? = null,
        finalMessage: String? = null,
        error: String? = null
    ) {
        val current = tasks[taskId] ?: return
        tasks[taskId] = current.copy(
            status = status,
            updatedAtEpochMs = System.currentTimeMillis(),
            executionSteps = executionSteps ?: current.executionSteps,
            displayId = displayId ?: current.displayId,
            success = success,
            finalMessage = finalMessage ?: current.finalMessage,
            error = error
        )
    }
}
