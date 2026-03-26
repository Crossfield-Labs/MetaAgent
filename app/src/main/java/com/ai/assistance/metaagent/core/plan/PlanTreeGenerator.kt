package com.ai.assistance.metaagent.core.plan

import android.content.Context
import com.ai.assistance.metaagent.api.chat.EnhancedAIService
import com.ai.assistance.metaagent.core.plan.model.PlanNode
import com.ai.assistance.metaagent.core.plan.model.PlanNodeAdapter
import com.ai.assistance.metaagent.core.plan.model.PlanNodeKind
import com.ai.assistance.metaagent.core.plan.model.PlanNodeParameterValue
import com.ai.assistance.metaagent.data.model.FunctionType
import com.ai.assistance.metaagent.util.AppLogger
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.UUID

object PlanTreeGenerator {
    private const val TAG = "PlanTreeGenerator"

    private data class PlanningToolHint(
        val name: String,
        val params: String,
        val useCase: String
    )

    private val planningToolHints = listOf(
        PlanningToolHint("visit_web", "url or visit_key+link_number", "Open a page or visit a searched result"),
        PlanningToolHint("http_request", "url, method, body?", "Fetch structured web data or API responses"),
        PlanningToolHint("execute_intent", "action?, package?, component?, type?", "Open an Android app or jump to a screen"),
        PlanningToolHint("run_ui_subagent", "intent, max_steps, target_app?", "Drive complex Android UI actions"),
        PlanningToolHint("execute_shell", "command", "Run shell commands in the local workspace"),
        PlanningToolHint("trigger_workflow", "workflow_id", "Run an existing workflow"),
        PlanningToolHint("list_files", "path", "Inspect workspace files"),
        PlanningToolHint("read_file", "path", "Read a file"),
        PlanningToolHint("write_file", "path, content", "Write a file"),
        PlanningToolHint("send_message_to_ai", "message, chat_id?", "Send a message to another chat/agent")
    )

    suspend fun generatePlan(
        context: Context,
        goal: String,
        hint: String = ""
    ): List<PlanNode> {
        val prompt = buildPlanPrompt(goal, hint)

        return try {
            val aiService = EnhancedAIService.getInstance(context)
            val service = aiService.getAIServiceForFunction(FunctionType.CHAT)
            val fullResponse = StringBuilder()
            val responseStream = service.sendMessage(
                context = context,
                message = prompt,
                chatHistory = emptyList(),
                modelParameters = emptyList(),
                enableThinking = false,
                stream = true,
                availableTools = emptyList(),
                onTokensUpdated = { _, _, _ -> },
                onNonFatalError = { error ->
                    AppLogger.e(TAG, "Non-fatal plan generation error: $error")
                }
            )

            responseStream.collect { chunk ->
                fullResponse.append(chunk)
            }

            parsePlanNodes(fullResponse.toString())
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to generate plan", e)
            listOf(
                PlanNode(
                    title = "执行目标",
                    kind = PlanNodeKind.EXEC,
                    goal = goal,
                    adapter = PlanNodeAdapter.CHAT,
                    requiresApproval = true,
                    explainToUser = "自动规划失败，先由对话节点直接处理整个目标"
                )
            )
        }
    }

    suspend fun replan(
        context: Context,
        originalGoal: String,
        completedNodes: List<PlanNode>,
        failedNode: PlanNode,
        failureReason: String
    ): List<PlanNode> {
        val prompt = buildReplanPrompt(originalGoal, completedNodes, failedNode, failureReason)

        return try {
            val aiService = EnhancedAIService.getInstance(context)
            val service = aiService.getAIServiceForFunction(FunctionType.CHAT)
            val fullResponse = StringBuilder()
            val responseStream = service.sendMessage(
                context = context,
                message = prompt,
                chatHistory = emptyList(),
                modelParameters = emptyList(),
                enableThinking = false,
                stream = true,
                availableTools = emptyList(),
                onTokensUpdated = { _, _, _ -> },
                onNonFatalError = { error ->
                    AppLogger.e(TAG, "Non-fatal replan error: $error")
                }
            )

            responseStream.collect { chunk ->
                fullResponse.append(chunk)
            }

            parsePlanNodes(fullResponse.toString())
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to replan", e)
            emptyList()
        }
    }

    private fun buildPlanPrompt(goal: String, hint: String): String = buildString {
        appendLine("你是 MetaAgent 的编排引擎。")
        appendLine("请把用户目标拆成一组可执行节点，输出 JSON 数组，不要输出任何额外解释。")
        appendLine()
        appendLine("规则：")
        appendLine("1. 节点数量控制在 3 到 8 个。")
        appendLine("2. 使用 dependsOn 描述依赖关系。")
        appendLine("3. 优先使用 TOOL 节点调用现成系统工具；只有在需要总结、解释、判断时才用 CHAT。")
        appendLine("4. 需要复杂安卓界面操作时使用 ANDROID。")
        appendLine("5. 复杂或高风险节点设置 requiresApproval=true。")
        appendLine("6. explainToUser 必须是给用户看的简洁中文说明。")
        appendLine("7. 如果后续节点要引用前序节点结果，在 toolParams 中使用引用对象：{\"type\":\"ref\",\"nodeId\":\"前序节点id\"}。")
        appendLine("8. toolParams 的静态值可以直接写字符串，也可以写 {\"type\":\"static\",\"value\":\"...\"}。")
        appendLine()
        appendLine("可用工具白名单：")
        planningToolHints.forEach { hintItem ->
            appendLine("- ${hintItem.name}: params=${hintItem.params}; use=${hintItem.useCase}")
        }
        appendLine()
        appendLine("输出格式示例：")
        appendLine(
            """
            [
              {
                "id": "n1",
                "title": "打开小红书",
                "goal": "启动小红书并进入搜索界面",
                "adapter": "TOOL",
                "toolName": "execute_intent",
                "toolParams": {
                  "package": "com.xingin.xhs",
                  "type": "activity"
                },
                "dependsOn": [],
                "requiresApproval": false,
                "explainToUser": "先把目标应用打开",
                "confidence": 0.9
              },
              {
                "id": "n2",
                "title": "搜索保研帖子",
                "goal": "在小红书搜索最新保研信息帖子并提取重点",
                "adapter": "ANDROID",
                "toolName": "run_ui_subagent",
                "toolParams": {
                  "intent": "打开小红书并搜索最新保研信息帖子，整理3条重点",
                  "max_steps": "14"
                },
                "dependsOn": ["n1"],
                "requiresApproval": false,
                "explainToUser": "让手机自动完成界面搜索和收集",
                "confidence": 0.78
              },
              {
                "id": "n3",
                "title": "整理结果",
                "goal": "根据搜索结果整理一段可直接发送给用户的摘要",
                "adapter": "CHAT",
                "dependsOn": ["n2"],
                "requiresApproval": false,
                "explainToUser": "把收集到的信息整理成可读结论",
                "confidence": 0.92
              }
            ]
            """.trimIndent()
        )
        appendLine()
        appendLine("用户目标：")
        appendLine(goal)
        if (hint.isNotBlank()) {
            appendLine()
            appendLine("额外信息：")
            appendLine(hint)
        }
    }

    private fun buildReplanPrompt(
        originalGoal: String,
        completedNodes: List<PlanNode>,
        failedNode: PlanNode,
        failureReason: String
    ): String = buildString {
        appendLine("你是 MetaAgent 的重新规划引擎。")
        appendLine("请基于已完成进度和失败原因，生成新的后续节点 JSON 数组。")
        appendLine("要求与正常规划一致：可以使用 TOOL / CHAT / ANDROID / CLI，支持 toolName 和 toolParams。")
        appendLine("不要重复已完成节点。")
        appendLine()
        appendLine("原始目标：")
        appendLine(originalGoal)
        appendLine()
        appendLine("已完成节点：")
        completedNodes.forEach { node ->
            appendLine("- ${node.id} ${node.title}: ${node.resultSummary.ifBlank { "完成" }}")
        }
        appendLine()
        appendLine("失败节点：")
        appendLine("- ${failedNode.id} ${failedNode.title}")
        appendLine("  goal=${failedNode.goal}")
        appendLine("  reason=$failureReason")
        appendLine()
        appendLine("注意：新节点的 dependsOn 可以引用已完成节点 id；如果需要消费前序节点结果，用 toolParams 中的 ref 对象。")
    }

    internal fun parsePlanNodes(response: String): List<PlanNode> {
        val jsonStr = extractJsonArray(response) ?: run {
            AppLogger.w(TAG, "Unable to extract JSON array from plan response")
            return emptyList()
        }

        return try {
            val jsonArray = Json.parseToJsonElement(jsonStr).jsonArray
            jsonArray.mapNotNull { element ->
                try {
                    parseNodeFromJson(element.jsonObject)
                } catch (e: Exception) {
                    AppLogger.w(TAG, "Failed to parse plan node: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to parse plan JSON", e)
            emptyList()
        }
    }

    private fun parseNodeFromJson(obj: JsonObject): PlanNode {
        return PlanNode(
            id = obj["id"]?.jsonPrimitive?.contentOrNull?.ifBlank { null }
                ?: generateStableNodeId(),
            title = obj["title"]?.jsonPrimitive?.contentOrNull ?: "未命名任务",
            kind = PlanNodeKind.EXEC,
            goal = obj["goal"]?.jsonPrimitive?.contentOrNull ?: "",
            adapter = parseAdapter(obj["adapter"]?.jsonPrimitive?.contentOrNull),
            dependsOn = obj["dependsOn"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: emptyList(),
            requiresApproval = obj["requiresApproval"]?.jsonPrimitive?.booleanOrNull ?: false,
            explainToUser = obj["explainToUser"]?.jsonPrimitive?.contentOrNull ?: "",
            confidence = obj["confidence"]?.jsonPrimitive?.floatOrNull ?: 0.9f,
            toolName = obj["toolName"]?.jsonPrimitive?.contentOrNull ?: "",
            toolParams = parseToolParams(obj["toolParams"])
        )
    }

    private fun parseToolParams(element: JsonElement?): Map<String, PlanNodeParameterValue> {
        val jsonObject = element as? JsonObject ?: return emptyMap()
        return jsonObject.mapValues { (_, value) -> parseParameterValue(value) }
    }

    private fun parseParameterValue(element: JsonElement): PlanNodeParameterValue {
        if (element is JsonObject) {
            val type = element["type"]?.jsonPrimitive?.contentOrNull?.lowercase()
            val nodeId = element["nodeId"]?.jsonPrimitive?.contentOrNull
                ?: element["ref"]?.jsonPrimitive?.contentOrNull
                ?: element["\$ref"]?.jsonPrimitive?.contentOrNull
            if ((type == "ref" || type == "reference" || type == "node_ref") && !nodeId.isNullOrBlank()) {
                return PlanNodeParameterValue.NodeReference(nodeId)
            }
            if (type == "static") {
                return PlanNodeParameterValue.StaticValue(
                    element["value"]?.jsonPrimitive?.contentOrNull ?: ""
                )
            }
            if (!nodeId.isNullOrBlank()) {
                return PlanNodeParameterValue.NodeReference(nodeId)
            }
        }

        return when (element) {
            is JsonObject, is JsonArray -> PlanNodeParameterValue.StaticValue(element.toString())
            else -> PlanNodeParameterValue.StaticValue(element.jsonPrimitive.contentOrNull ?: "")
        }
    }

    private fun parseAdapter(name: String?): PlanNodeAdapter {
        return when (name?.uppercase()) {
            "TOOL" -> PlanNodeAdapter.TOOL
            "CLAUDE" -> PlanNodeAdapter.CLAUDE
            "CLI" -> PlanNodeAdapter.CLI
            "ANDROID" -> PlanNodeAdapter.ANDROID
            "LOCAL_RUNNER" -> PlanNodeAdapter.LOCAL_RUNNER
            else -> PlanNodeAdapter.CHAT
        }
    }

    private fun extractJsonArray(text: String): String? {
        val trimmed = text.trim()
        if (trimmed.startsWith("[")) {
            return trimmed
        }

        val codeBlockPattern = Regex("""```(?:json)?\s*\n?([\s\S]*?)\n?```""")
        codeBlockPattern.find(trimmed)?.let { match ->
            val content = match.groupValues[1].trim()
            if (content.startsWith("[")) return content
        }

        val start = trimmed.indexOf('[')
        val end = trimmed.lastIndexOf(']')
        if (start >= 0 && end > start) {
            return trimmed.substring(start, end + 1)
        }

        return null
    }

    private fun generateStableNodeId(): String {
        return "n" + UUID.randomUUID().toString().take(6)
    }
}
