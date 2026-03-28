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
        PlanningToolHint("pc.execute", "runner, workspace, task", "Run a PC sub-agent session for computer-side work"),
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
        appendLine("12. If one UI automation step collects information and a later step needs to use that information, insert a CHAT bridge node in between.")
        appendLine("13. For cross-app tasks, prefer UI_AUTOMATION -> CHAT -> UI_AUTOMATION -> CHAT.")
        appendLine("14. TOOL -> CHAT -> UI_AUTOMATION is preferred when a tool gathers options and a later UI step executes one option.")
        appendLine("15. Use PC nodes for computer-side work such as inspecting a project, running tests, or asking a PC runner to create files.")
        appendLine("16. End user-facing tasks with a CHAT node that explains the final outcome.")
        appendLine("17. If the user says 'ask me before continuing', 'confirm before the next step', or similar, create a CHAT gate node with requiresApproval=true before the continuation step.")
        appendLine("18. When a read-only PC inspection is followed by a user confirmation gate, do not require approval on the inspection node itself.")
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
        appendLine("Example for cross-app UI automation:")
        appendLine(
            """
            [
              {
                "id": "n1",
                "title": "Collect postgraduate recommendation posts in Xiaohongshu",
                "goal": "Open Xiaohongshu, search the newest postgraduate recommendation posts, and collect 3 key points",
                "adapter": "UI_AUTOMATION",
                "toolName": "run_ui_subagent",
                "toolParams": {
                  "intent": "Open Xiaohongshu, search the newest postgraduate recommendation posts, and collect 3 key points",
                  "target_app": "com.xingin.xhs",
                  "max_steps": "16"
                },
                "dependsOn": [],
                "requiresApproval": false,
                "explainToUser": "Use UI automation to collect the latest information."
              },
              {
                "id": "n2",
                "title": "Turn the findings into a short WeChat message",
                "goal": "Summarize the previous result into a short message that can be sent in WeChat",
                "adapter": "CHAT",
                "dependsOn": ["n1"],
                "requiresApproval": false,
                "explainToUser": "Compress the findings into a short sendable message."
              },
              {
                "id": "n3",
                "title": "Send the prepared message in WeChat",
                "goal": "Open WeChat and send the prepared message to the target contact",
                "adapter": "UI_AUTOMATION",
                "toolName": "run_ui_subagent",
                "toolParams": {
                  "intent": {
                    "type": "ref",
                    "nodeId": "n2"
                  },
                  "target_app": "com.tencent.mm",
                  "max_steps": "16"
                },
                "dependsOn": ["n2"],
                "requiresApproval": true,
                "explainToUser": "Let UI automation send the prepared content in WeChat."
              },
              {
                "id": "n4",
                "title": "Summarize the final outcome",
                "goal": "Tell the user whether the message was sent successfully",
                "adapter": "CHAT",
                "dependsOn": ["n3"],
                "requiresApproval": false,
                "explainToUser": "Explain the final result to the user."
              }
            ]
            """.trimIndent()
        )
        appendLine()
        appendLine("Example for tool plus UI automation:")
        appendLine(
            """
            [
              {
                "id": "n1",
                "title": "Search for a suitable study song",
                "goal": "Find a song that is suitable for studying",
                "adapter": "TOOL",
                "toolName": "visit_web",
                "toolParams": {
                  "url": "https://www.google.com/search?q=best+study+songs"
                },
                "dependsOn": [],
                "requiresApproval": false,
                "explainToUser": "Use a tool to collect candidate songs."
              },
              {
                "id": "n2",
                "title": "Pick one song to play",
                "goal": "Select one suitable song from the previous result and produce a short playback instruction",
                "adapter": "CHAT",
                "dependsOn": ["n1"],
                "requiresApproval": false,
                "explainToUser": "Choose one candidate and prepare a short instruction."
              },
              {
                "id": "n3",
                "title": "Play the selected song in NetEase Cloud Music",
                "goal": "Open NetEase Cloud Music and play the selected song",
                "adapter": "UI_AUTOMATION",
                "toolName": "run_ui_subagent",
                "toolParams": {
                  "intent": {
                    "type": "ref",
                    "nodeId": "n2"
                  },
                  "target_app": "com.netease.cloudmusic",
                  "max_steps": "14"
                },
                "dependsOn": ["n2"],
                "requiresApproval": false,
                "explainToUser": "Use UI automation to play the chosen song."
              },
              {
                "id": "n4",
                "title": "Report the playback result",
                "goal": "Tell the user what song is playing and whether playback succeeded",
                "adapter": "CHAT",
                "dependsOn": ["n3"],
                "requiresApproval": false,
                "explainToUser": "Report the final playback result."
              }
            ]
            """.trimIndent()
        )
        appendLine()
        appendLine("Example for a PC sub-agent task:")
        appendLine(
            """
            [
              {
                "id": "n1",
                "title": "Inspect the project structure on PC",
                "goal": "Check the current project directory structure on the PC and summarize the main folders",
                "adapter": "PC",
                "toolName": "pc.execute",
                "toolParams": {
                  "runner": "shell",
                  "workspace": "D:/workspace/my-project",
                  "task": "Check the current project directory structure and summarize the main folders"
                },
                "dependsOn": [],
                "requiresApproval": false,
                "explainToUser": "Run a PC-side directory inspection and report the result.",
                "confidence": 0.9
              },
              {
                "id": "n2",
                "title": "Summarize the PC inspection result",
                "goal": "Explain the inspection result to the user",
                "adapter": "CHAT",
                "dependsOn": ["n1"],
                "requiresApproval": false,
                "explainToUser": "Summarize the PC-side result for the user."
              }
            ]
            """.trimIndent()
        )
        appendLine()
        appendLine("Example for 'ask me before continuing' on PC:")
        appendLine(
            """
            [
              {
                "id": "n1",
                "title": "Inspect the current project directory on PC",
                "goal": "Check the current project directory structure on the PC and collect the main folders and notable files",
                "adapter": "PC",
                "toolName": "pc.execute",
                "toolParams": {
                  "runner": "shell",
                  "workspace": "D:/workspace/my-project",
                  "task": "Inspect the current project directory structure and collect the main folders and notable files"
                },
                "dependsOn": [],
                "requiresApproval": false,
                "explainToUser": "Inspect the project structure on the PC first."
              },
              {
                "id": "n2",
                "title": "Ask the user whether to continue",
                "goal": "Show the inspection result to the user and wait for confirmation before continuing the summary",
                "adapter": "CHAT",
                "dependsOn": ["n1"],
                "requiresApproval": true,
                "explainToUser": "Show the inspection result and wait for the user's decision."
              },
              {
                "id": "n3",
                "title": "Summarize the project structure",
                "goal": "Summarize the project structure after the user confirms to continue",
                "adapter": "CHAT",
                "dependsOn": ["n2"],
                "requiresApproval": false,
                "explainToUser": "Continue with the final project summary after confirmation."
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

        val cleaned = normalized
            .filterNot { launchNodesToRemove.contains(it.id) }
            .map { node ->
                if (launchNodesToRemove.isEmpty()) {
                    node
                } else {
                    node.copy(dependsOn = node.dependsOn.filterNot { launchNodesToRemove.contains(it) })
                }
            }

        val withBridgeNodes = insertBridgeChatNodes(cleaned)
        val withTerminalChat = ensureTerminalChatNode(withBridgeNodes)
        return normalizeApprovalGateNodes(withTerminalChat)
    }

    private fun insertBridgeChatNodes(nodes: List<PlanNode>): List<PlanNode> {
        if (nodes.size < 2) return nodes

        val result = nodes.toMutableList()
        var index = 0
        while (index < result.lastIndex) {
            val current = result[index]
            val next = result[index + 1]

            if (!shouldInsertBridgeNode(current, next)) {
                index += 1
                continue
            }

            val bridgeId = generateStableNodeId()
            val bridgeNode = PlanNode(
                id = bridgeId,
                title = createBridgeNodeTitle(current, next),
                kind = PlanNodeKind.EXEC,
                goal = createBridgeNodeGoal(current, next),
                adapter = PlanNodeAdapter.CHAT,
                dependsOn = listOf(current.id),
                requiresApproval = false,
                explainToUser = "Summarize the previous result and prepare the next step."
            )

            val updatedNextDependsOn = buildList {
                if (next.dependsOn.isEmpty()) {
                    add(bridgeId)
                } else {
                    var replaced = false
                    next.dependsOn.forEach { dependency ->
                        if (dependency == current.id) {
                            add(bridgeId)
                            replaced = true
                        } else {
                            add(dependency)
                        }
                    }
                    if (!replaced) add(bridgeId)
                }
            }.distinct()

            result[index + 1] = bridgeNode
            result.add(index + 2, next.copy(dependsOn = updatedNextDependsOn))
            result.removeAt(index + 3)
            index += 2
        }

        return result
    }

    private fun ensureTerminalChatNode(nodes: List<PlanNode>): List<PlanNode> {
        if (nodes.isEmpty()) return nodes
        val lastNode = nodes.last()
        if (lastNode.adapter == PlanNodeAdapter.CHAT) return nodes

        val summaryNode = PlanNode(
            id = generateStableNodeId(),
            title = "Summarize the final result",
            kind = PlanNodeKind.EXEC,
            goal = "Explain the final outcome of the previous steps to the user.",
            adapter = PlanNodeAdapter.CHAT,
            dependsOn = listOf(lastNode.id),
            requiresApproval = false,
            explainToUser = "Summarize the final task result for the user."
        )
        return nodes + summaryNode
    }

    private fun normalizeApprovalGateNodes(nodes: List<PlanNode>): List<PlanNode> {
        if (nodes.isEmpty()) return nodes

        val gateNodeIds = nodes
            .filter(::isUserDecisionGateNode)
            .map { it.id }
            .toSet()
        if (gateNodeIds.isEmpty()) return nodes

        return nodes.map { node ->
            when {
                node.id in gateNodeIds -> node.copy(
                    requiresApproval = true,
                    explainToUser = node.explainToUser.ifBlank {
                        "Show the interim result and wait for the user's confirmation."
                    }
                )

                node.adapter == PlanNodeAdapter.PC && gateNodeIds.any { gateId -> gateDependsOnNode(nodes, gateId, node.id) } -> {
                    if (isReadOnlyPcInspectionNode(node)) {
                        node.copy(requiresApproval = false)
                    } else {
                        node
                    }
                }

                else -> node
            }
        }
    }

    private fun shouldInsertBridgeNode(current: PlanNode, next: PlanNode): Boolean {
        if (next.adapter == PlanNodeAdapter.CHAT || current.adapter == PlanNodeAdapter.CHAT) return false

        val currentProducesContext = current.adapter in setOf(
            PlanNodeAdapter.ANDROID,
            PlanNodeAdapter.TOOL,
            PlanNodeAdapter.CLI,
            PlanNodeAdapter.LOCAL_RUNNER,
            PlanNodeAdapter.PC
        )
        if (!currentProducesContext) return false

        if (next.adapter == PlanNodeAdapter.ANDROID) {
            return true
        }

        return current.adapter == PlanNodeAdapter.ANDROID && next.adapter == PlanNodeAdapter.TOOL
    }

    private fun createBridgeNodeTitle(current: PlanNode, next: PlanNode): String {
        return when {
            next.adapter == PlanNodeAdapter.ANDROID ->
                "Prepare the next UI automation step"
            next.adapter == PlanNodeAdapter.PC ->
                "Prepare the next PC step"
            next.adapter == PlanNodeAdapter.TOOL ->
                "Prepare the next tool input"
            else ->
                "Summarize the previous result"
        }
    }

    private fun createBridgeNodeGoal(current: PlanNode, next: PlanNode): String {
        return when {
            current.adapter == PlanNodeAdapter.ANDROID && next.adapter == PlanNodeAdapter.ANDROID ->
                "Summarize the previous UI automation result into a concise instruction for the next UI automation step."
            current.adapter == PlanNodeAdapter.TOOL && next.adapter == PlanNodeAdapter.ANDROID ->
                "Turn the previous tool result into a concise instruction for the next UI automation step."
            current.adapter == PlanNodeAdapter.ANDROID && next.adapter == PlanNodeAdapter.TOOL ->
                "Summarize the previous UI automation result into a concise instruction for the next tool step."
            current.adapter == PlanNodeAdapter.PC && next.adapter == PlanNodeAdapter.ANDROID ->
                "Turn the previous PC result into a concise instruction for the next UI automation step."
            current.adapter == PlanNodeAdapter.PC && next.adapter == PlanNodeAdapter.TOOL ->
                "Summarize the previous PC result into a concise instruction for the next tool step."
            current.adapter == PlanNodeAdapter.TOOL && next.adapter == PlanNodeAdapter.PC ->
                "Turn the previous tool result into a concise instruction for the next PC step."
            else ->
                "Summarize the previous result and prepare the next step."
        }
    }

    private fun isUserDecisionGateNode(node: PlanNode): Boolean {
        if (node.adapter != PlanNodeAdapter.CHAT) return false
        val combinedText = listOf(node.title, node.goal, node.explainToUser)
            .joinToString(" ")
            .lowercase()
        return listOf(
            "ask the user whether to continue",
            "ask the user before continuing",
            "wait for confirmation before continuing",
            "wait for the user's confirmation",
            "whether to continue",
            "confirm before continuing",
            "先问我",
            "确认后继续",
            "是否继续",
            "继续之前先确认"
        ).any { combinedText.contains(it) }
    }

    private fun gateDependsOnNode(nodes: List<PlanNode>, gateNodeId: String, dependencyNodeId: String): Boolean {
        val gateNode = nodes.firstOrNull { it.id == gateNodeId } ?: return false
        return dependencyNodeId in gateNode.dependsOn
    }

    private fun isReadOnlyPcInspectionNode(node: PlanNode): Boolean {
        val combinedText = listOf(node.title, node.goal, node.explainToUser)
            .joinToString(" ")
            .lowercase()
        val readOnlyHints = listOf(
            "inspect",
            "check",
            "list",
            "read",
            "summarize",
            "analyze",
            "directory",
            "structure",
            "目录",
            "结构",
            "检查",
            "梳理",
            "读取"
        )
        val mutatingHints = listOf(
            "create",
            "write",
            "edit",
            "modify",
            "delete",
            "remove",
            "apply",
            "run tests",
            "build",
            "execute",
            "创建",
            "写入",
            "修改",
            "删除",
            "执行",
            "编译"
        )
        return readOnlyHints.any { combinedText.contains(it) } &&
            mutatingHints.none { combinedText.contains(it) }
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
            "PC" -> PlanNodeAdapter.PC
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
