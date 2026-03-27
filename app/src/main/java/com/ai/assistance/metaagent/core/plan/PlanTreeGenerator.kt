package com.ai.assistance.metaagent.core.plan

import android.content.Context
import com.ai.assistance.metaagent.api.chat.EnhancedAIService
import com.ai.assistance.metaagent.core.plan.model.PlanNode
import com.ai.assistance.metaagent.core.plan.model.PlanNodeAdapter
import com.ai.assistance.metaagent.core.plan.model.PlanNodeKind
import com.ai.assistance.metaagent.core.plan.model.PlanNodeParameterValue
import com.ai.assistance.metaagent.data.model.FunctionType
import com.ai.assistance.metaagent.util.AppLogger
import java.util.UUID
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

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
        PlanningToolHint("run_ui_subagent", "intent, max_steps, target_app?", "Take over a full Android UI task inside one app"),
        PlanningToolHint("execute_shell", "command", "Run shell commands in the local workspace"),
        PlanningToolHint("trigger_workflow", "workflow_id", "Run an existing workflow"),
        PlanningToolHint("list_files", "path", "Inspect workspace files"),
        PlanningToolHint("read_file", "path", "Read a file"),
        PlanningToolHint("write_file", "path, content", "Write a file"),
        PlanningToolHint("send_message_to_ai", "message, chat_id?", "Send a message to another chat or agent")
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

            normalizePlanNodes(parsePlanNodes(fullResponse.toString()))
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to generate plan", e)
            listOf(
                PlanNode(
                    title = "Handle goal directly",
                    kind = PlanNodeKind.EXEC,
                    goal = goal,
                    adapter = PlanNodeAdapter.CHAT,
                    requiresApproval = true,
                    explainToUser = "Planning failed, so the goal falls back to a single chat node."
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

            normalizePlanNodes(parsePlanNodes(fullResponse.toString()))
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to replan", e)
            emptyList()
        }
    }

    private fun buildPlanPrompt(goal: String, hint: String): String = buildString {
        appendLine("You are MetaAgent's planning engine.")
        appendLine("Break the user goal into a JSON array of executable plan nodes. Return JSON only.")
        appendLine()
        appendLine("Rules:")
        appendLine("1. Keep the plan between 2 and 6 nodes.")
        appendLine("2. Use dependsOn to express dependencies.")
        appendLine("3. Prefer TOOL nodes for concrete system actions. Use CHAT only for analysis, summarization, or explanation.")
        appendLine("4. If the task requires multiple Android UI actions inside one app, prefer a single ANDROID main node.")
        appendLine("5. The default toolName for an ANDROID main node should be run_ui_subagent.")
        appendLine("6. Do not split 'open app' into execute_intent and then another ANDROID node unless the task is only to open the app.")
        appendLine("7. For Android UI automation nodes, include target_app whenever possible.")
        appendLine("8. Set requiresApproval=true for risky nodes.")
        appendLine("9. explainToUser must be a short user-facing sentence.")
        appendLine("10. If a later node depends on an earlier result, use a ref object inside toolParams: {\"type\":\"ref\",\"nodeId\":\"n1\"}.")
        appendLine("11. Static parameter values can be plain strings or {\"type\":\"static\",\"value\":\"...\"}.")
        appendLine()
        appendLine("Available planning tool hints:")
        planningToolHints.forEach { hintItem ->
            appendLine("- ${hintItem.name}: params=${hintItem.params}; use=${hintItem.useCase}")
        }
        appendLine()
        appendLine("Example output:")
        appendLine(
            """
            [
              {
                "id": "n1",
                "title": "Run UI automation in Xiaohongshu",
                "goal": "Open Xiaohongshu, search for the newest recommendation-postgraduate posts, and extract 3 key points",
                "adapter": "ANDROID",
                "toolName": "run_ui_subagent",
                "toolParams": {
                  "intent": "Open Xiaohongshu, search for the newest recommendation-postgraduate posts, and extract 3 key points",
                  "target_app": "com.xingin.xhs",
                  "max_steps": "14"
                },
                "dependsOn": [],
                "requiresApproval": false,
                "explainToUser": "Let the UI automation sub-agent take over the phone and collect the result.",
                "confidence": 0.82
              },
              {
                "id": "n2",
                "title": "Summarize the findings",
                "goal": "Turn the collected result into a short user-facing summary",
                "adapter": "CHAT",
                "dependsOn": ["n1"],
                "requiresApproval": false,
                "explainToUser": "Summarize the collected information into a readable answer.",
                "confidence": 0.92
              }
            ]
            """.trimIndent()
        )
        appendLine()
        appendLine("User goal:")
        appendLine(goal)
        if (hint.isNotBlank()) {
            appendLine()
            appendLine("Additional hint:")
            appendLine(hint)
        }
    }

    private fun buildReplanPrompt(
        originalGoal: String,
        completedNodes: List<PlanNode>,
        failedNode: PlanNode,
        failureReason: String
    ): String = buildString {
        appendLine("You are MetaAgent's replanning engine.")
        appendLine("Based on the completed progress and failure reason, output a new JSON array of follow-up plan nodes.")
        appendLine("You may use TOOL, CHAT, ANDROID, and CLI nodes with toolName and toolParams.")
        appendLine("If the failed step belongs to a complex in-app UI task, prefer regenerating one ANDROID main node instead of many tiny tool nodes.")
        appendLine("Do not repeat completed nodes.")
        appendLine()
        appendLine("Original goal:")
        appendLine(originalGoal)
        appendLine()
        appendLine("Completed nodes:")
        completedNodes.forEach { node ->
            appendLine("- ${node.id} ${node.title}: ${node.resultSummary.ifBlank { "done" }}")
        }
        appendLine()
        appendLine("Failed node:")
        appendLine("- ${failedNode.id} ${failedNode.title}")
        appendLine("  goal=${failedNode.goal}")
        appendLine("  reason=$failureReason")
        appendLine()
        appendLine("Note: new dependsOn values may point to completed node ids, and toolParams may use ref objects.")
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

    private fun normalizePlanNodes(nodes: List<PlanNode>): List<PlanNode> {
        if (nodes.isEmpty()) return nodes

        val nodesById = nodes.associateBy { it.id }
        val dependentCount = nodes
            .flatMap { node -> node.dependsOn.map { dependency -> dependency to node.id } }
            .groupingBy { it.first }
            .eachCount()

        val launchNodesToRemove = mutableSetOf<String>()

        val normalized = nodes.map { node ->
            if (node.adapter != PlanNodeAdapter.ANDROID) {
                return@map node
            }

            val launchNode = node.dependsOn
                .mapNotNull { nodesById[it] }
                .firstOrNull { dependency ->
                    dependency.adapter == PlanNodeAdapter.TOOL &&
                        dependency.toolName == "execute_intent" &&
                        (dependentCount[dependency.id] ?: 0) == 1
                } ?: return@map node

            launchNodesToRemove += launchNode.id

            val targetPackage = (launchNode.toolParams["package"] as? PlanNodeParameterValue.StaticValue)
                ?.value
                ?.takeIf { it.isNotBlank() }
            val currentIntent = (node.toolParams["intent"] as? PlanNodeParameterValue.StaticValue)
                ?.value
                ?.trim()
                .orEmpty()
            val launchInstruction = launchNode.goal.ifBlank { launchNode.title }.trim()
            val mergedIntent = when {
                currentIntent.isBlank() -> launchInstruction
                currentIntent.startsWith("Open ") || currentIntent.contains(launchInstruction) -> currentIntent
                else -> "$launchInstruction, then $currentIntent"
            }

            node.copy(
                toolName = node.toolName.ifBlank { "run_ui_subagent" },
                dependsOn = node.dependsOn.filterNot { it == launchNode.id },
                toolParams = buildMap {
                    putAll(node.toolParams)
                    put("intent", PlanNodeParameterValue.StaticValue(mergedIntent))
                    if (!targetPackage.isNullOrBlank() && !containsKey("target_app")) {
                        put("target_app", PlanNodeParameterValue.StaticValue(targetPackage))
                    }
                }
            )
        }

        return normalized
            .filterNot { launchNodesToRemove.contains(it.id) }
            .map { node ->
                if (launchNodesToRemove.isEmpty()) {
                    node
                } else {
                    node.copy(dependsOn = node.dependsOn.filterNot { launchNodesToRemove.contains(it) })
                }
            }
    }

    private fun parseNodeFromJson(obj: JsonObject): PlanNode {
        return PlanNode(
            id = obj["id"]?.jsonPrimitive?.contentOrNull?.ifBlank { null } ?: generateStableNodeId(),
            title = obj["title"]?.jsonPrimitive?.contentOrNull ?: "Untitled task",
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
            "ANDROID", "UI_AUTOMATION" -> PlanNodeAdapter.ANDROID
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
