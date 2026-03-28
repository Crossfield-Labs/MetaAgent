package com.ai.assistance.metaagent.core.plan

import android.content.Context
import android.content.Intent
import com.ai.assistance.metaagent.api.chat.EnhancedAIService
import com.ai.assistance.metaagent.core.plan.model.PlanNode
import com.ai.assistance.metaagent.core.plan.model.PlanNodeAdapter
import com.ai.assistance.metaagent.core.plan.model.PlanNodeParameterValue
import com.ai.assistance.metaagent.core.plan.model.PlanNodeStatus
import com.ai.assistance.metaagent.core.plan.model.TaskEventType
import com.ai.assistance.metaagent.core.plan.model.TaskSession
import com.ai.assistance.metaagent.core.plan.model.TaskSessionStatus
import com.ai.assistance.metaagent.core.tools.AIToolHandler
import com.ai.assistance.metaagent.core.tools.AutomationExecutionResult
import com.ai.assistance.metaagent.core.tools.MessageSendResultData
import com.ai.assistance.metaagent.core.tools.agent.StepResult
import com.ai.assistance.metaagent.core.tools.agent.UiAutomationStepCallbackRegistry
import com.ai.assistance.metaagent.data.model.AITool
import com.ai.assistance.metaagent.data.model.FunctionType
import com.ai.assistance.metaagent.data.model.ToolParameter
import com.ai.assistance.metaagent.util.AppLogger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.max

class PlanTreeExecutor(
    private val context: Context
) {
    companion object {
        private const val TAG = "PlanTreeExecutor"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val toolHandler = AIToolHandler.getInstance(context)

    private val _taskSession = MutableStateFlow<TaskSession?>(null)
    val taskSession: StateFlow<TaskSession?> = _taskSession.asStateFlow()

    private var executionJob: Job? = null

    suspend fun createTask(goal: String, hint: String = "") {
        AppLogger.d(TAG, "Create plan task: $goal")
        val session = TaskSession(goal = goal, status = TaskSessionStatus.DRAFT)
        updateSession(session.appendEvent(TaskEventType.NODE_STARTED, message = "开始生成编排计划"))

        val nodes = PlanTreeGenerator.generatePlan(context, goal, hint)
        if (nodes.isEmpty()) {
            updateSession(
                _taskSession.value!!.copy(status = TaskSessionStatus.FAILED)
                    .appendEvent(TaskEventType.TASK_FAILED, message = "编排计划生成失败")
            )
            return
        }

        updateSession(
            _taskSession.value!!.copy(
                status = TaskSessionStatus.AWAITING_APPROVAL,
                planNodes = nodes
            ).appendEvent(
                TaskEventType.PLAN_GENERATED,
                message = "已生成 ${nodes.size} 个执行步骤，等待确认"
            )
        )
    }

    fun approvePlan() {
        val session = _taskSession.value ?: return
        if (session.status != TaskSessionStatus.AWAITING_APPROVAL) return

        updateSession(
            session.copy(
                status = TaskSessionStatus.RUNNING,
                executionStartedAt = System.currentTimeMillis()
            ).appendEvent(TaskEventType.PLAN_APPROVED, message = "计划已批准，开始执行")
        )

        executionJob = scope.launch {
            executeNodes()
        }
    }

    fun rejectPlan(feedback: String = "") {
        val session = _taskSession.value ?: return
        if (session.status != TaskSessionStatus.AWAITING_APPROVAL) return

        updateSession(
            session.copy(status = TaskSessionStatus.DRAFT)
                .appendEvent(
                    TaskEventType.PLAN_MODIFIED,
                    message = if (feedback.isBlank()) "用户要求修改计划" else "用户反馈：$feedback"
                )
        )

        scope.launch {
            val nodes = PlanTreeGenerator.generatePlan(context, session.goal, feedback)
            if (nodes.isNotEmpty()) {
                updateSession(
                    _taskSession.value!!.copy(
                        status = TaskSessionStatus.AWAITING_APPROVAL,
                        planNodes = nodes,
                        replanCount = session.replanCount + 1
                    ).appendEvent(
                        TaskEventType.PLAN_REPLANNED,
                        message = "已根据反馈重新生成计划（第 ${session.replanCount + 1} 次）"
                    )
                )
            }
        }
    }

    fun pauseExecution() {
        executionJob?.cancel()
        executionJob = null
        val session = _taskSession.value ?: return
        if (session.status == TaskSessionStatus.RUNNING) {
            updateSession(
                session.copy(status = TaskSessionStatus.PAUSED)
                    .appendEvent(TaskEventType.TASK_PAUSED, message = "任务已暂停")
            )
        }
    }

    fun resumeExecution() {
        val session = _taskSession.value ?: return
        if (session.status == TaskSessionStatus.PAUSED) {
            updateSession(
                session.copy(status = TaskSessionStatus.RUNNING)
                    .appendEvent(TaskEventType.TASK_RESUMED, message = "任务继续执行")
            )
            executionJob = scope.launch {
                executeNodes()
            }
        }
    }

    fun cancelTask() {
        executionJob?.cancel()
        executionJob = null
        val session = _taskSession.value ?: return
        updateSession(
            session.copy(status = TaskSessionStatus.CANCELLED, activeNodeId = null)
                .appendEvent(TaskEventType.TASK_FAILED, message = "任务已取消")
        )
    }

    fun clearTask() {
        executionJob?.cancel()
        executionJob = null
        _taskSession.value = null
    }

    fun dispose() {
        clearTask()
        scope.cancel()
    }

    fun respondToBlockedNode(nodeId: String, response: String) {
        val session = _taskSession.value ?: return
        val node = session.findNode(nodeId) ?: return
        if (node.status != PlanNodeStatus.BLOCKED) return

        updateSession(
            session.updateNode(nodeId) {
                it.copy(status = PlanNodeStatus.PENDING, detail = "用户补充：$response")
            }.copy(status = TaskSessionStatus.RUNNING)
                .appendEvent(
                    TaskEventType.USER_INTERVENTION,
                    nodeId = nodeId,
                    message = "用户回复：$response"
                )
        )

        executionJob?.cancel()
        executionJob = scope.launch {
            executeNodes()
        }
    }

    fun confirmAndContinueBlockedNode(nodeId: String, response: String) {
        val session = _taskSession.value ?: return
        val node = session.findNode(nodeId) ?: return
        if (node.status != PlanNodeStatus.BLOCKED) return

        updateSession(
            session.updateNode(nodeId) {
                it.copy(
                    status = PlanNodeStatus.PENDING,
                    requiresApproval = false,
                    detail = "User confirmed / updated: $response"
                )
            }.copy(
                status = TaskSessionStatus.RUNNING,
                activeNodeId = null
            ).appendEvent(
                TaskEventType.USER_INTERVENTION,
                nodeId = nodeId,
                message = "User confirmed continuation: $response"
            )
        )

        executionJob?.cancel()
        executionJob = scope.launch {
            executeNodes()
        }
    }

    private suspend fun executeNodes() {
        while (true) {
            val session = _taskSession.value ?: break
            if (session.status != TaskSessionStatus.RUNNING) break

            val nextNode = session.findNextExecutableNode() ?: break
            updateSession(
                session.updateNode(nextNode.id) { it.copy(status = PlanNodeStatus.RUNNING, detail = "准备执行") }
                    .copy(activeNodeId = nextNode.id)
                    .appendEvent(
                        TaskEventType.NODE_STARTED,
                        nodeId = nextNode.id,
                        message = nextNode.explainToUser.ifBlank { "正在执行：${nextNode.title}" }
                    )
            )

            if (nextNode.requiresApproval) {
                updateSession(
                    _taskSession.value!!.updateNode(nextNode.id) {
                        it.copy(status = PlanNodeStatus.BLOCKED, detail = "等待确认后执行")
                    }.copy(status = TaskSessionStatus.WAITING_USER)
                        .appendEvent(
                            TaskEventType.NODE_BLOCKED,
                            nodeId = nextNode.id,
                            message = "节点“${nextNode.title}”需要确认"
                        )
                )
                return
            }

            val success = executeNode(nextNode)
            if (success) {
                updateSession(
                    _taskSession.value!!.updateNode(nextNode.id) {
                        it.copy(
                            status = PlanNodeStatus.DONE,
                            progress = 1f,
                            resultSummary = it.resultSummary.ifBlank { "完成" },
                            detail = if (it.detail.isBlank()) "已完成" else it.detail
                        )
                    }.copy(activeNodeId = null)
                        .appendEvent(
                            TaskEventType.NODE_COMPLETED,
                            nodeId = nextNode.id,
                            message = _taskSession.value!!.findNode(nextNode.id)?.resultSummary?.ifBlank { "“${nextNode.title}”已完成" }
                                ?: "“${nextNode.title}”已完成"
                        )
                )
            } else {
                updateSession(
                    _taskSession.value!!.updateNode(nextNode.id) {
                        it.copy(
                            status = PlanNodeStatus.FAILED,
                            detail = if (it.detail.isBlank()) "执行失败" else it.detail
                        )
                    }.copy(activeNodeId = null)
                        .appendEvent(
                            TaskEventType.NODE_FAILED,
                            nodeId = nextNode.id,
                            message = "“${nextNode.title}”执行失败"
                        )
                )
                val failedNode = _taskSession.value!!.findNode(nextNode.id) ?: nextNode
                handleNodeFailure(failedNode)
                return
            }
        }

        val session = _taskSession.value ?: return
        if (session.status == TaskSessionStatus.RUNNING) {
            val allDone = session.planNodes.all {
                it.status == PlanNodeStatus.DONE || it.status == PlanNodeStatus.SKIPPED
            }
            if (allDone) {
                updateSession(
                    session.copy(
                        status = TaskSessionStatus.COMPLETED,
                        completedAt = System.currentTimeMillis(),
                        activeNodeId = null,
                        resultSummary = "全部 ${session.completedNodeCount} 个步骤已完成"
                    ).appendEvent(TaskEventType.TASK_COMPLETED, message = "任务已全部完成")
                )
            }
        }
    }

    private suspend fun executeNode(node: PlanNode): Boolean {
        return when (node.adapter) {
            PlanNodeAdapter.TOOL -> executeToolNode(node)
            PlanNodeAdapter.CHAT,
            PlanNodeAdapter.CLAUDE -> executeChatNode(node)
            PlanNodeAdapter.CLI,
            PlanNodeAdapter.LOCAL_RUNNER -> executeCliNode(node)
            PlanNodeAdapter.ANDROID -> executeAndroidNode(node)
        }
    }

    private suspend fun executeToolNode(node: PlanNode): Boolean {
        val session = _taskSession.value ?: return false
        val toolName = node.toolName.ifBlank {
            recordNodeResult(node.id, "", "缺少 toolName，无法执行工具节点")
            return false
        }

        return try {
            emitNodeProgress(node, 0.14f, "准备调用工具：$toolName")
            val parameters = resolveToolParameters(node, session)
            emitNodeProgress(node, 0.42f, "执行工具：$toolName")

            val result = toolHandler.executeTool(
                AITool(
                    name = toolName,
                    parameters = parameters,
                    description = node.title
                )
            )

            if (!result.success) {
                recordNodeResult(
                    nodeId = node.id,
                    summary = "",
                    detail = result.error ?: result.result.toString().trim().ifBlank { "工具执行失败" },
                    rawData = result.error ?: result.result.toString()
                )
                return false
            }

            val output = extractToolResultText(result.result)
            recordNodeResult(
                nodeId = node.id,
                summary = summarizeResult(output, "$toolName 执行完成"),
                detail = output.ifBlank { "$toolName 已执行完成，但没有额外输出" }.take(320),
                rawData = output
            )
            emitNodeProgress(node, 1f, "工具执行完成")
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLogger.e(TAG, "Tool node failed: ${node.id}", e)
            recordNodeResult(node.id, "", e.message ?: "工具执行失败", e.stackTraceToString())
            false
        }
    }

    private suspend fun executeChatNode(node: PlanNode): Boolean {
        val session = _taskSession.value ?: return false
        return try {
            emitNodeProgress(node, 0.1f, "正在分析任务")
            val aiService = EnhancedAIService.getInstance(context)
            val service = aiService.getAIServiceForFunction(FunctionType.CHAT)
            val fullResponse = StringBuilder()
            var hasMidProgress = false

            val responseStream = service.sendMessage(
                context = context,
                message = buildChatExecutionPrompt(node, session),
                chatHistory = emptyList(),
                modelParameters = emptyList(),
                enableThinking = false,
                stream = true,
                availableTools = emptyList(),
                onTokensUpdated = { _, _, _ -> },
                onNonFatalError = { error ->
                    AppLogger.w(TAG, "Chat execution non-fatal error: $error")
                }
            )

            responseStream.collect { chunk ->
                fullResponse.append(chunk)
                if (!hasMidProgress && fullResponse.length >= 80) {
                    hasMidProgress = true
                    emitNodeProgress(node, 0.72f, "正在整理分析结果")
                }
            }

            val output = fullResponse.toString().trim()
            if (output.isBlank()) {
                recordNodeResult(node.id, "", "对话节点没有返回内容")
                return false
            }

            recordNodeResult(
                nodeId = node.id,
                summary = summarizeResult(output, "已生成分析结果"),
                detail = output.take(320),
                rawData = output
            )
            emitNodeProgress(node, 1f, "分析完成")
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLogger.e(TAG, "Chat node failed: ${node.id}", e)
            recordNodeResult(node.id, "", e.message ?: "对话节点执行失败", e.stackTraceToString())
            false
        }
    }

    private suspend fun executeCliNode(node: PlanNode): Boolean {
        val session = _taskSession.value ?: return false
        return try {
            val command = resolveCliCommand(node, session)
            emitNodeProgress(node, 0.12f, "准备命令行任务")
            emitNodeProgress(node, 0.38f, "执行命令: $command")

            val result = toolHandler.executeTool(
                AITool(
                    name = "execute_shell",
                    parameters = listOf(ToolParameter(name = "command", value = command)),
                    description = node.title
                )
            )

            if (!result.success) {
                recordNodeResult(
                    nodeId = node.id,
                    summary = "",
                    detail = result.error ?: result.result.toString().trim().ifBlank { "命令执行失败" },
                    rawData = result.error ?: result.result.toString()
                )
                return false
            }

            val output = extractToolResultText(result.result)
            recordNodeResult(
                nodeId = node.id,
                summary = summarizeResult(output, "命令执行完成"),
                detail = output.ifBlank { "命令已执行，但没有额外输出" }.take(320),
                rawData = output
            )
            emitNodeProgress(node, 1f, "命令执行完成")
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLogger.e(TAG, "CLI node failed: ${node.id}", e)
            recordNodeResult(node.id, "", e.message ?: "命令执行失败", e.stackTraceToString())
            false
        }
    }

    private suspend fun executeAndroidNode(node: PlanNode): Boolean {
        val session = _taskSession.value ?: return false
        val callbackId = "plan-ui-${session.taskId}-${node.id}"
        var observedStepCount = 0
        return try {
            val originalIntent = buildAndroidIntentForUiStep(node, session)
            val maxSteps = resolveAndroidMaxSteps(node, session)
            val targetApp = node.toolParams["target_app"]?.let { resolvePlanParameterValue(it, session) }
            val explicitAgentId = node.toolParams["agent_id"]
                ?.let { resolvePlanParameterValue(it, session).trim() }
                ?.takeIf { it.isNotBlank() }

            var currentIntent = originalIntent
            var lastFailureText = ""
            val maxAttempts = 2

            UiAutomationStepCallbackRegistry.register(callbackId) { step ->
                observedStepCount += 1
                val latestMessage = summarizeUiAutomationStep(step)
                val pseudoProgress = max(
                    node.progress,
                    (0.12f + observedStepCount * 0.06f).coerceAtMost(0.92f)
                )
                emitNodeProgress(node, pseudoProgress, latestMessage)
            }

            for (attempt in 1..maxAttempts) {
                emitNodeProgress(
                    node,
                    if (attempt == 1) 0.12f else 0.18f,
                    "Starting UI automation attempt $attempt/$maxAttempts"
                )

                val params = mutableListOf(
                    ToolParameter(name = "intent", value = currentIntent),
                    ToolParameter(name = "max_steps", value = maxSteps.toString()),
                    ToolParameter(name = "step_callback_id", value = callbackId)
                )
                if (!explicitAgentId.isNullOrBlank()) {
                    params += ToolParameter(name = "agent_id", value = explicitAgentId)
                }
                if (!targetApp.isNullOrBlank()) {
                    params += ToolParameter(name = "target_app", value = targetApp)
                }

                val result = toolHandler.executeTool(
                    AITool(
                        name = node.toolName.ifBlank { "run_ui_subagent" },
                        parameters = params,
                        description = node.title
                    )
                )

                if (!result.success) {
                    val detail = result.error ?: result.result.toString().trim().ifBlank { "UI automation failed" }
                    lastFailureText = detail
                    recordNodeResult(
                        nodeId = node.id,
                        summary = "",
                        detail = detail,
                        rawData = result.error ?: result.result.toString()
                    )
                    return false
                }

                val automationResult = result.result as? AutomationExecutionResult
                val output = extractToolResultText(result.result)

                if (automationResult == null) {
                    val summary = summarizeResult(output, "UI automation task completed")
                    recordNodeResult(
                        nodeId = node.id,
                        summary = summary,
                        detail = output.ifBlank { "UI automation completed the task" }.take(320),
                        rawData = output
                    )
                    emitNodeProgress(node, 1f, summary)
                    return true
                }

                if (automationResult.executionSuccess) {
                    val summary = extractAutomationDisplaySummary(automationResult)
                    recordNodeResult(
                        nodeId = node.id,
                        summary = summary,
                        detail = output.ifBlank { "UI automation completed the task" }.take(320),
                        rawData = output
                    )
                    emitNodeProgress(node, 1f, summary)
                    return true
                }

                lastFailureText = output.ifBlank {
                    automationResult.executionError
                        ?: "UI automation attempt $attempt did not finish the task"
                }

                if (attempt >= maxAttempts) {
                    recordNodeResult(
                        nodeId = node.id,
                        summary = "",
                        detail = lastFailureText,
                        rawData = output
                    )
                    return false
                }

                updateSession(
                    (_taskSession.value ?: session).appendEvent(
                        TaskEventType.NODE_PROGRESS,
                        nodeId = node.id,
                        progress = 0.5f,
                        message = "UI automation attempt $attempt did not finish. Retrying with a follow-up instruction."
                    )
                )

                currentIntent = buildUiAutomationRetryIntent(
                    node = node,
                    originalIntent = originalIntent,
                    previousIntent = currentIntent,
                    previousResult = automationResult,
                    failureSummary = lastFailureText
                )
            }

            recordNodeResult(node.id, "", lastFailureText.ifBlank { "UI automation failed" }, lastFailureText)
            false
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLogger.e(TAG, "Android node failed: ${node.id}", e)
            recordNodeResult(node.id, "", e.message ?: "UI automation failed", e.stackTraceToString())
            false
        } finally {
            UiAutomationStepCallbackRegistry.unregister(callbackId)
            returnToMetaAgentIfNeeded(node)
        }
    }

    private fun resolveToolParameters(node: PlanNode, session: TaskSession): List<ToolParameter> {
        return node.toolParams.map { (key, value) ->
            ToolParameter(name = key, value = resolvePlanParameterValue(value, session))
        }
    }

    private fun resolvePlanParameterValue(
        value: PlanNodeParameterValue,
        session: TaskSession
    ): String {
        return when (value) {
            is PlanNodeParameterValue.StaticValue -> value.value
            is PlanNodeParameterValue.NodeReference -> {
                val refNode = session.findNode(value.nodeId)
                    ?: throw IllegalStateException("引用节点不存在: ${value.nodeId}")
                when (refNode.status) {
                    PlanNodeStatus.DONE -> refNode.resultData.ifBlank {
                        refNode.resultSummary.ifBlank { refNode.detail }
                    }
                    PlanNodeStatus.SKIPPED -> refNode.detail.ifBlank { refNode.resultSummary }
                    PlanNodeStatus.FAILED -> throw IllegalStateException("引用节点执行失败: ${value.nodeId}")
                    else -> throw IllegalStateException("引用节点尚未完成: ${value.nodeId}")
                }
            }
        }
    }

    private fun buildChatExecutionPrompt(node: PlanNode, session: TaskSession): String = buildString {
        appendLine("你是 MetaAgent 的本地任务执行助手。")
        appendLine("请完成下面这个子任务，并返回适合展示在任务卡片中的简洁中文结果。")
        appendLine()
        appendLine("任务标题：${node.title}")
        appendLine("任务目标：${node.goal.ifBlank { node.title }}")
        if (node.explainToUser.isNotBlank()) {
            appendLine("用户可见解释：${node.explainToUser}")
        }

        val dependencyContext = buildDependencyContext(node, session)
        if (dependencyContext.isNotBlank()) {
            appendLine()
            appendLine("已完成前序结果：")
            appendLine(dependencyContext)
        }

        appendLine()
        appendLine("输出要求：")
        appendLine("1. 第一行用一句中文总结本步骤结果")
        appendLine("2. 后续最多补充 3 条关键发现或建议")
        appendLine("3. 不要输出 Markdown 标题")
        appendLine("4. 如果缺少外部环境，就基于当前上下文给出最可执行的答案")
    }

    private fun resolveCliCommand(node: PlanNode, session: TaskSession): String {
        node.toolParams["command"]?.let {
            return resolvePlanParameterValue(it, session)
        }

        val text = "${node.title} ${node.goal}".lowercase()
        return when {
            containsAny(text, "时间", "date") -> "date"
            containsAny(text, "目录", "文件", "工作区", "workspace", "结构", "list", "ls") -> "pwd; ls"
            containsAny(text, "环境", "系统", "版本", "python", "依赖", "检查") -> "pwd; uname -a; ls"
            containsAny(text, "进程", "cpu", "内存") -> "ps"
            else -> "pwd; ls"
        }
    }

    private fun buildAndroidIntent(node: PlanNode, session: TaskSession): String {
        val explicitIntent = node.toolParams["intent"]?.let { resolvePlanParameterValue(it, session) }
        if (!explicitIntent.isNullOrBlank()) {
            return explicitIntent
        }

        val baseIntent = node.goal.ifBlank { node.title }
        val dependencyContext = buildDependencyContext(node, session, summaryOnly = true)
        return if (dependencyContext.isBlank()) {
            baseIntent
        } else {
            "$baseIntent\n\n已知前序结果：\n$dependencyContext"
        }
    }

    private fun buildAndroidIntentForUiStep(node: PlanNode, session: TaskSession): String {
        val targetApp = node.toolParams["target_app"]?.let { resolvePlanParameterValue(it, session) }
        val baseIntent = buildAndroidIntent(node, session).trim()
        if (targetApp.isNullOrBlank()) return baseIntent

        val lower = baseIntent.lowercase()
        val alreadyGuided = lower.contains("if the current screen is not inside the target app") ||
            lower.contains("first open that app")
        if (alreadyGuided) return baseIntent

        return buildString {
            appendLine("Start from the current phone state.")
            appendLine("If the current screen is not inside the target app ($targetApp), first open that app and then continue the task.")
            append(baseIntent)
        }
    }

    private fun resolveAndroidMaxSteps(node: PlanNode, session: TaskSession): Int {
        node.toolParams["max_steps"]?.let {
            return resolvePlanParameterValue(it, session).toIntOrNull()?.coerceIn(1, 40) ?: 16
        }

        val intent = node.goal.ifBlank { node.title }
        return when {
            intent.length > 80 -> 18
            intent.contains("打开") || intent.contains("进入") -> 10
            else -> 14
        }
    }

    private fun buildDependencyContext(
        node: PlanNode,
        session: TaskSession,
        summaryOnly: Boolean = false
    ): String {
        val dependencies = session.findDependencyNodes(node, onlyCompleted = true)
        if (dependencies.isEmpty()) return ""

        return dependencies.joinToString("\n\n") { dependency ->
            buildString {
                append("[${dependency.id}] ${dependency.title}")
                val summary = dependency.resultSummary.ifBlank { dependency.detail }
                if (summary.isNotBlank()) {
                    appendLine()
                    append("摘要：")
                    append(summary)
                }
                if (!summaryOnly) {
                    val detail = dependency.resultData.ifBlank { dependency.detail }
                    if (detail.isNotBlank() && detail != summary) {
                        appendLine()
                        append("详情：")
                        append(detail.take(600))
                    }
                }
            }
        }
    }

    private fun emitNodeProgress(node: PlanNode, progress: Float, message: String) {
        val session = _taskSession.value ?: return
        val safeProgress = progress.coerceIn(0f, 1f)
        updateSession(
            session.updateNode(node.id) {
                it.copy(progress = safeProgress, detail = message.take(240))
            }.appendEvent(
                TaskEventType.NODE_PROGRESS,
                nodeId = node.id,
                progress = safeProgress,
                message = message.take(120)
            )
        )
    }

    private fun recordNodeResult(nodeId: String, summary: String, detail: String, rawData: String = detail) {
        val session = _taskSession.value ?: return
        updateSession(
            session.updateNode(nodeId) {
                it.copy(
                    resultSummary = summary,
                    detail = detail,
                    resultData = rawData
                )
            }
        )
    }

    private fun extractToolResultText(resultData: Any?): String {
        return when (resultData) {
            is MessageSendResultData -> resultData.aiResponse ?: resultData.message
            is AutomationExecutionResult -> buildAutomationResultText(resultData)
            null -> ""
            else -> resultData.toString().trim()
        }
    }

    private fun summarizeUiAutomationStep(step: StepResult): String {
        val messageLine = step.message
            ?.lineSequence()
            ?.map { it.trim() }
            ?.firstOrNull { it.isNotBlank() }

        val actionLine = step.action?.let { action ->
            action.fields["text"]
                ?.takeIf { it.isNotBlank() }
                ?.let { text -> "${action.actionName ?: action.metadata}: $text" }
                ?: (action.actionName ?: action.metadata)
        }

        return when {
            step.finished && !messageLine.isNullOrBlank() -> messageLine.take(80)
            !messageLine.isNullOrBlank() -> messageLine.take(80)
            !actionLine.isNullOrBlank() -> "Running: ${actionLine.take(64)}"
            !step.thinking.isNullOrBlank() -> step.thinking.trim().lineSequence().first().take(72)
            else -> "UI automation is running"
        }
    }

    private fun extractAutomationDisplaySummary(resultData: AutomationExecutionResult): String {
        val finalBlock = resultData.executionMessage
            .substringAfter("Final message:", "")
            .substringBefore("\n\nFull conversation history:")
            .trim()

        val finalLines = finalBlock
            .lineSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toList()

        val preferredLine = when {
            finalLines.isEmpty() -> null
            finalLines.size > 1 && finalLines.first().length <= 10 -> finalLines[1]
            else -> finalLines.first()
        }

        if (!preferredLine.isNullOrBlank()) {
            return preferredLine.take(96)
        }

        return if (resultData.executionSuccess) {
            "UI automation completed the task"
        } else {
            resultData.executionError?.take(96) ?: "UI automation did not complete the task"
        }
    }

    private fun buildAutomationResultText(resultData: AutomationExecutionResult): String {
        val summary = buildString {
            append(
                if (resultData.executionSuccess) {
                    "UI automation finished after ${resultData.executionSteps} steps."
                } else {
                    "UI automation did not finish after ${resultData.executionSteps} steps."
                }
            )
            if (!resultData.executionError.isNullOrBlank()) {
                append("\nFailure reason: ${resultData.executionError}")
            }
            if (resultData.executionMessage.isNotBlank()) {
                append("\n${resultData.executionMessage}")
            }
        }
        return summary.trim()
    }

    private fun buildUiAutomationRetryIntent(
        node: PlanNode,
        originalIntent: String,
        previousIntent: String,
        previousResult: AutomationExecutionResult,
        failureSummary: String
    ): String = buildString {
        appendLine("The previous UI automation attempt did not fully complete.")
        appendLine("Continue from the current phone state instead of restarting unless restart is required.")
        appendLine()
        appendLine("Task goal:")
        appendLine(node.goal.ifBlank { node.title })
        appendLine()
        appendLine("Original intent:")
        appendLine(originalIntent)
        appendLine()
        appendLine("Previous attempt intent:")
        appendLine(previousIntent)
        appendLine()
        appendLine("Failure summary:")
        appendLine(failureSummary)
        if (!previousResult.executionError.isNullOrBlank()) {
            appendLine()
            appendLine("Execution error:")
            appendLine(previousResult.executionError)
        }
        appendLine()
        appendLine("Execution trace:")
        appendLine(previousResult.executionMessage.take(1600))
        appendLine()
        appendLine("Finish the task. If the app is already open, continue from the current screen.")
    }

    private fun returnToMetaAgentIfNeeded(node: PlanNode) {
        val session = _taskSession.value ?: return
        val shouldReturn = node.toolParams["return_to_metaagent"]
            ?.let { value ->
                when (resolvePlanParameterValue(value, session).trim().lowercase()) {
                    "false", "0", "no" -> false
                    else -> true
                }
            }
            ?: true

        if (!shouldReturn) return

        try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            if (launchIntent != null) {
                context.startActivity(launchIntent)
            }
        } catch (e: Exception) {
            AppLogger.w(TAG, "Failed to return MetaAgent to foreground", e)
        }
    }

    private fun containsAny(text: String, vararg keywords: String): Boolean {
        return keywords.any { text.contains(it) }
    }

    private fun summarizeResult(text: String, fallback: String): String {
        return text.lineSequence()
            .map { it.trim() }
            .firstOrNull { it.isNotBlank() }
            ?.take(80)
            ?: fallback
    }

    private suspend fun handleNodeFailure(failedNode: PlanNode) {
        val session = _taskSession.value ?: return
        val completedNodes = session.planNodes.filter { it.status == PlanNodeStatus.DONE }

        val newNodes = PlanTreeGenerator.replan(
            context = context,
            originalGoal = session.goal,
            completedNodes = completedNodes,
            failedNode = failedNode,
            failureReason = failedNode.detail
        )

        if (newNodes.isNotEmpty()) {
            val updatedOldNodes = session.planNodes.map { node ->
                if (node.status == PlanNodeStatus.PENDING) {
                    node.copy(status = PlanNodeStatus.CANCELLED)
                } else {
                    node
                }
            }

            updateSession(
                session.copy(
                    status = TaskSessionStatus.AWAITING_APPROVAL,
                    planNodes = updatedOldNodes + newNodes,
                    replanCount = session.replanCount + 1
                ).appendEvent(
                    TaskEventType.PLAN_REPLANNED,
                    message = "因执行失败已自动调整计划，请确认新方案"
                )
            )
        } else {
            updateSession(
                session.copy(status = TaskSessionStatus.FAILED)
                    .appendEvent(
                        TaskEventType.TASK_FAILED,
                        message = "执行失败且无法自动修复，请手动调整"
                    )
            )
        }
    }

    private fun updateSession(session: TaskSession) {
        _taskSession.value = session
    }
}
