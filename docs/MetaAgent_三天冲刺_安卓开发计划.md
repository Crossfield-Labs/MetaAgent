# MetaAgent 三天冲刺 · 安卓端核心开发计划

> **目标：3 天内打通核心 Demo 链路**——聊天界面改造 + 编排树 + 跨端通信 + 异步双线程 UI

---

## 〇、核心思路：先 Mock 后替换

```
Phase 1（今天）：Android 端编排树 + 聊天增强
  → 调度模型（Gemini/DeepSeek）在安卓本地生成编排树
  → 聊天界面展示编排树 + 进度流
  → PC 用 Mock 数据/事件模拟

Phase 2（明天）：WebSocket 通信 + PC Orchestrator MVP
  → 手机 WS Client ↔ PC WS Server 连通
  → PC 端 Claude Agent SDK 接入
  → 真链路跑通：手机发目标 → PC 执行 → 手机看进度

Phase 3（后天）：异步双线程 + Demo 打磨
  → 后台执行不阻塞前台聊天
  → Mock 页面（课程空间/画像/复习）美化
  → Demo 场景编排 + 录制
```

---

## 一、Claude Agent SDK 调研结论与架构决策

### 1.1 Claude Agent SDK 是什么

Claude Agent SDK（前身 Claude Code SDK）是 Anthropic 官方 Python SDK，提供：

- **内置工具**：`Read`（读文件）、`Write`（写文件）、`Edit`（编辑）、`Bash`（命令行）
- **Agent Loop**：自动循环——思考→调工具→观察结果→继续
- **Hooks 系统**：`PreToolUse`/`PostToolUse` 拦截，可以在工具执行前后注入逻辑
- **权限控制**：可限制命令/目录访问范围
- **上下文管理**：自动维护对话历史和 token 预算

安装：`pip install claude-agent-sdk`

### 1.2 架构决策：双脑分工

```
┌─ "项目经理脑"（调度模型 Gemini/DeepSeek）──────────────────┐
│  职责：                                                      │
│  · 理解用户目标                                              │
│  · 生成编排树（Plan Tree）                                   │
│  · 和用户对话（前台聊天回路）                                │
│  · 决定每个子任务交给谁执行                                  │
│  · 汇总结果、生成用户可读报告                                │
│  运行位置：Android 端 / PC Orchestrator 均可                 │
└──────────────────────────────────────────────────────────────┘
                              ↓ 分发子任务
┌─ "工程师脑"（Claude Agent SDK）────────────────────────────┐
│  职责：                                                      │
│  · 接收具体子任务描述                                        │
│  · 自主执行：读文件→写代码→跑命令→观察结果→迭代             │
│  · 返回执行结果和产物                                        │
│  运行位置：PC 端（Claude Agent SDK / Claude CLI）            │
└──────────────────────────────────────────────────────────────┘
```

### 1.3 PC Orchestrator 最简实现

**不需要从零写 LangGraph**。最快路径：

```python
# PC 端 main.py（FastAPI + WebSocket + Claude Agent SDK）

from fastapi import FastAPI, WebSocket
from claude_agent_sdk import ClaudeSDKClient, ClaudeAgentOptions
import asyncio, json

app = FastAPI()

# Claude Agent SDK 客户端
claude = ClaudeSDKClient(ClaudeAgentOptions(
    model="claude-sonnet-4-20250514",
    allowed_tools=["Read", "Write", "Edit", "Bash"],
    permission_mode="dangerously_skip_permissions"  # 比赛用
))

@app.websocket("/ws")
async def ws_endpoint(ws: WebSocket):
    await ws.accept()
    async for raw in ws.iter_text():
        frame = json.loads(raw)
        
        if frame.get("method") == "task.execute_node":
            node = frame["params"]["node"]
            # 把子任务交给 Claude
            asyncio.create_task(execute_with_claude(ws, node))
        
        elif frame.get("method") == "dialogue.message":
            # 对话回路：调调度模型回答
            reply = await dialogue_reply(frame["params"]["message"])
            await ws.send_json({"type":"event","event":"dialogue.reply",
                                "payload":{"message": reply}})

async def execute_with_claude(ws, node):
    """Claude 执行一个编排树节点"""
    await ws.send_json({"type":"event","event":"node.started",
                        "payload":{"nodeId": node["id"]}})
    
    result = await claude.query(
        prompt=f"完成任务：{node['title']}\n目标：{node['goal']}",
        hooks={
            "PostToolUse": lambda event: asyncio.create_task(
                ws.send_json({"type":"event","event":"node.progress",
                              "payload":{"nodeId": node["id"],
                                         "message": str(event)}})
            )
        }
    )
    
    await ws.send_json({"type":"event","event":"node.completed",
                        "payload":{"nodeId": node["id"],
                                   "result": result}})
```

**或者更简单——直接用 Claude CLI（subprocess）**：

```python
async def execute_with_claude_cli(ws, node):
    """最简方案：subprocess 调 claude CLI"""
    proc = await asyncio.create_subprocess_exec(
        "claude", "-p", node["goal"],
        "--dangerously-skip-permissions",
        cwd=node.get("workspace", "."),
        stdout=asyncio.subprocess.PIPE
    )
    async for line in proc.stdout:
        await ws.send_json({"type":"event","event":"node.progress",
                            "payload":{"nodeId": node["id"],
                                       "output": line.decode().strip()}})
    await proc.wait()
```

> **结论：PC 端用 Claude CLI subprocess 方案，一天内就能跑通。** SDK 方案更精细但可以后续升级。

---

## 二、现有仓库可复用的关键发现

调研发现几个**重大可复用点**：

### 2.1 `RemoteAgentServer.kt`（40KB）——已有 HTTP API 服务器！

手机端已经是一个**完整的远程 Agent 服务器**（NanoHTTPD），对外暴露：
- `/api/remote/agent/run` — 远程执行 Agent 任务
- `/api/remote/agent/tasks` — 任务列表
- `/api/remote/agent/{id}/state` — 任务状态
- `/api/remote/screenshot` — 截图
- `/api/remote/memory/*` — 记忆 CRUD
- `/api/remote/input/*` — UI 操作（tap/swipe/text）

**这意味着 PC 可以直接调安卓的 HTTP API 来执行安卓端任务！** 不需要重新实现。

### 2.2 `PhoneAgent.kt`（58KB）——已有安卓任务执行器！

完整的安卓端 Agent，可以：
- 操作 App UI（Accessibility）
- 截图 + 分析
- 启动 App
- 执行命令

### 2.3 Chat Plugin 系统——可插拔的消息处理

`AIMessageManager` 有 `MessageProcessingPluginRegistry`，支持插件接管消息处理。
**编排树逻辑可以实现为一个 Chat Plugin**，在检测到复杂任务时接管消息流。

### 2.4 `ToolProgressBus`——已有进度事件总线

可以复用做任务进度的 UI 更新通知。

---

## 三、三天详细开发计划

### Day 1（今天）：聊天增强 + 编排树本地生成

#### 开午上半：数据模型 + 协议定义（2h）

**目标：定死契约，后续所有端按契约开发**

##### 1. PlanNode 数据模型

```kotlin
// data/model/PlanNode.kt

@Serializable
data class PlanNode(
    val id: String,
    val title: String,
    val kind: PlanNodeKind = PlanNodeKind.EXEC,   // exec/clarify/checkpoint
    val goal: String = "",
    val status: PlanNodeStatus = PlanNodeStatus.PENDING,
    val adapter: String = "chat",                  // chat/claude/cli/android
    val dependsOn: List<String> = emptyList(),
    val progress: Float = 0f,
    val detail: String = "",                        // 进度详情文本
    val confidence: Float = 1.0f,
    val requiresApproval: Boolean = false,
    val children: List<PlanNode> = emptyList()      // 支持树形
)

@Serializable
enum class PlanNodeKind { EXEC, CLARIFY, CHECKPOINT, REPLAN }

@Serializable
enum class PlanNodeStatus { PENDING, RUNNING, BLOCKED, DONE, FAILED }
```

##### 2. TaskSession 数据模型

```kotlin
// data/model/TaskSession.kt

@Serializable
data class TaskSession(
    val taskId: String,
    val goal: String,
    val status: TaskSessionStatus,
    val planNodes: List<PlanNode>,
    val createdAt: Long,
    val events: List<TaskEvent> = emptyList()
)

@Serializable
enum class TaskSessionStatus {
    DRAFT, PLANNED, AWAITING_APPROVAL, RUNNING, PAUSED, COMPLETED, FAILED
}

@Serializable
data class TaskEvent(
    val seq: Int,
    val event: String,       // node.started / node.progress / node.completed / ...
    val nodeId: String? = null,
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
```

##### 3. WebSocket 帧格式

```kotlin
// data/model/WSFrame.kt

@Serializable
data class WSFrame(
    val type: String,          // "req" / "resp" / "event"
    val id: String? = null,    // 请求 ID
    val method: String? = null, // task.create / plan.approve / dialogue.message
    val event: String? = null,  // plan.generated / node.progress / ...
    val taskId: String? = null,
    val seq: Int? = null,
    val params: JsonObject? = null,
    val payload: JsonObject? = null
)
```

#### 下午前半：编排树生成（2h）

**目标：用户输入目标 → 调度模型生成编排树 JSON → 解析为 PlanNode 列表**

##### 实现路径：复用现有 `AIMessageManager` + 新增 Plan 生成 Prompt

```kotlin
// core/plan/PlanTreeGenerator.kt

object PlanTreeGenerator {
    /**
     * 调用调度模型（Gemini/DeepSeek），输入用户目标，
     * 输出编排树 JSON，解析为 PlanNode 列表。
     */
    suspend fun generatePlan(
        goal: String,
        context: PlanContext    // 课程信息、已知条件等
    ): List<PlanNode> {
        val prompt = buildPlanPrompt(goal, context)
        // 复用现有 EnhancedAIService 调模型
        val response = aiService.singleQuery(prompt)
        // 从 response 中提取 JSON 并解析
        return parsePlanNodes(response)
    }
    
    private fun buildPlanPrompt(goal: String, ctx: PlanContext): String = """
        你是 MetaAgent 编排引擎。用户提出了以下目标：
        
        目标：$goal
        
        请将目标拆分为编排树，每个节点包含：
        - id: 唯一标识
        - title: 子任务标题
        - goal: 具体目标
        - adapter: 执行方式（chat=对话回答/claude=Claude执行/cli=命令行/android=安卓操作）
        - dependsOn: 依赖的前置节点id列表
        - requiresApproval: 是否需要用户审批（布尔值）
        
        输出 JSON 数组格式。
    """.trimIndent()
}
```

#### 下午后半：编排树 UI 组件（3h）

**目标：在聊天界面中嵌入可展开的编排树卡片**

##### 1. PlanTreeCard 组件

```kotlin
// ui/features/chat/components/PlanTreeCard.kt

@Composable
fun PlanTreeCard(
    nodes: List<PlanNode>,
    taskStatus: TaskSessionStatus,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onNodeTap: (PlanNode) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 标题行
            Text("📋 编排计划", style = MaterialTheme.typography.titleMedium)
            
            // 节点列表（简洁版——不用 Canvas，用列表 + 缩进 + 状态图标）
            nodes.forEach { node ->
                PlanNodeRow(node, onTap = { onNodeTap(node) })
            }
            
            // 审批按钮
            if (taskStatus == TaskSessionStatus.AWAITING_APPROVAL) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onApprove) { Text("✅ 批准执行") }
                    OutlinedButton(onClick = onReject) { Text("✏️ 修改") }
                }
            }
        }
    }
}

@Composable
fun PlanNodeRow(node: PlanNode, onTap: () -> Unit) {
    Row(
        modifier = Modifier.clickable(onClick = onTap).padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 状态图标
        Icon(
            imageVector = when (node.status) {
                PlanNodeStatus.PENDING -> Icons.Default.Circle
                PlanNodeStatus.RUNNING -> Icons.Default.PlayArrow  // 带动画
                PlanNodeStatus.DONE -> Icons.Default.CheckCircle
                PlanNodeStatus.FAILED -> Icons.Default.Error
                PlanNodeStatus.BLOCKED -> Icons.Default.Pause
            },
            tint = when (node.status) {
                PlanNodeStatus.RUNNING -> MaterialTheme.colorScheme.primary
                PlanNodeStatus.DONE -> Color(0xFF4CAF50)
                PlanNodeStatus.FAILED -> Color(0xFFF44336)
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
        Spacer(Modifier.width(8.dp))
        
        Column {
            Text(node.title, style = MaterialTheme.typography.bodyMedium)
            if (node.detail.isNotBlank()) {
                Text(node.detail, style = MaterialTheme.typography.bodySmall,
                     color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            // 进度条（执行中时显示）
            if (node.status == PlanNodeStatus.RUNNING && node.progress > 0) {
                LinearProgressIndicator(progress = node.progress)
            }
        }
    }
}
```

##### 2. 进度流组件

```kotlin
// ui/features/chat/components/TaskProgressBar.kt

@Composable
fun TaskProgressBar(taskSession: TaskSession?) {
    if (taskSession == null || taskSession.status == TaskSessionStatus.COMPLETED) return
    
    val currentNode = taskSession.planNodes.find { it.status == PlanNodeStatus.RUNNING }
    
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            // 脉冲动画圆点
            PulsingDot(color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("后台任务：${taskSession.goal}",
                     style = MaterialTheme.typography.labelSmall)
                Text(currentNode?.let { "${it.title} · ${it.detail}" } ?: "准备中…",
                     style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
```

##### 3. 集成到聊天界面

在现有 `ChatScreen` 中：
- 顶部添加 `TaskProgressBar`
- 消息流中嵌入 `PlanTreeCard`（作为特殊消息类型）
- 底部输入框**始终可用**（不因执行而禁用）

#### 晚上：本地联调（2h）

**目标：在 Android 本地跑通 "用户输入目标→模型生成编排树→展示编排树→用户审批→本地模拟执行→进度更新" 全流程**

- 编排树生成用 Gemini API
- 执行用 Mock（每个节点 `delay(2000)` 然后更新状态）
- 验证：
  1. 输入 "帮我完成CNN实验" → 看到编排树卡片
  2. 点击"批准" → 节点依次变绿
  3. 聊天框始终可用

---

### Day 2（明天）：WebSocket 通信 + PC 执行

#### 上午：WebSocket 客户端（Android）+ 服务端（PC）

##### Android 端 WebSocket Client

```kotlin
// remote/ws/MetaAgentWSClient.kt

class MetaAgentWSClient(
    private val serverUrl: String  // ws://192.168.x.x:8765
) {
    private val client = OkHttpClient.Builder()
        .pingInterval(30, TimeUnit.SECONDS)
        .build()
    private val _events = MutableSharedFlow<WSFrame>(extraBufferCapacity = 64)
    val events: SharedFlow<WSFrame> = _events
    
    fun connect() { /* OkHttp WebSocket */ }
    fun send(frame: WSFrame) { /* JSON 序列化 → ws.send */ }
    fun disconnect() { /* 关闭连接 */ }
}
```

##### PC 端 WebSocket Server

```python
# pc_orchestrator/main.py（FastAPI）
# 见上文 "PC Orchestrator 最简实现"
# 重点：收 task.create → 生编排树 → 推事件
#        收 plan.approve → Claude CLI 执行 → 推进度
#        收 dialogue.message → 调度模型回答
```

#### 下午：Claude CLI 适配器 + 真链路打通

```python
# pc_orchestrator/adapters/claude_adapter.py

async def execute_node_with_claude(ws, node, workspace):
    """用 Claude CLI 执行编排树节点"""
    cmd = ["claude", "-p", node["goal"], "--dangerously-skip-permissions"]
    proc = await asyncio.create_subprocess_exec(
        *cmd, cwd=workspace,
        stdout=asyncio.subprocess.PIPE,
        stderr=asyncio.subprocess.STDOUT
    )
    async for line in proc.stdout:
        text = line.decode().strip()
        if text:
            await ws.send_json({
                "type": "event", "event": "node.progress",
                "payload": {"nodeId": node["id"], "message": text}
            })
    
    code = await proc.wait()
    status = "completed" if code == 0 else "failed"
    await ws.send_json({
        "type": "event", "event": f"node.{status}",
        "payload": {"nodeId": node["id"]}
    })
```

#### 晚上：真链路联调

- 手机连 PC 的 WebSocket
- 手机发"帮我创建一个Python项目" → PC 收到 → Claude 执行 → 手机实时看到输出
- **这就是 Demo 的核心亮点时刻！**

---

### Day 3（后天）：异步双线程 + 全面打磨

#### 上午：异步双线程 UI

**核心：执行中聊天不阻塞**

```kotlin
// ViewModel 中执行状态和对话状态分离
class TaskChatViewModel : ViewModel() {
    // 执行回路
    val taskSession = MutableStateFlow<TaskSession?>(null)
    
    // 对话回路（独立于执行）
    val chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    
    // 用户发消息 → 走 WS dialogue.message，不影响执行
    fun sendChat(text: String) {
        chatMessages.value += ChatMessage(sender="user", content=text)
        wsClient.send(WSFrame(method="dialogue.message", 
                              params=jsonOf("message" to text)))
    }
    
    // 收到对话回复 → 追加到聊天（和执行进度互不干扰）
    fun onDialogueReply(reply: String) {
        chatMessages.value += ChatMessage(sender="ai", content=reply)
    }
}
```

#### 下午：Mock 页面 + Demo 打磨

- 课程空间：预埋假数据，保证 UI 好看
- 学习画像卡：硬编码一张静态展示
- 复习卡片：3 张静态卡 + 翻转动画
- 主动推送：App 启动 30 秒后推一条预置通知

#### 晚上：Demo 场景编排

按产品书 §4.1 "大学生的一天" 编排完整 Demo：
1. 打开 App → 看到学伴主页
2. 课程空间 → 资料预览
3. 对话发起任务 → 编排树展示 → 用户审批
4. 电脑端 Claude 执行 → 手机实时进度
5. **异步双线程高光**：执行的同时问问题、改方向
6. 任务完成 → 结果展示
7. 学习画像卡

---

## 四、文件清单汇总

### 需要新建的文件

```
app/src/main/java/com/ai/assistance/metaagent/
├── data/model/
│   ├── PlanNode.kt               # 编排树节点
│   ├── TaskSession.kt            # 任务会话
│   └── WSFrame.kt                # WebSocket 帧
├── core/plan/
│   └── PlanTreeGenerator.kt      # 编排树生成器
├── remote/ws/
│   └── MetaAgentWSClient.kt      # WebSocket 客户端
├── ui/features/chat/components/
│   ├── PlanTreeCard.kt           # 编排树 UI 组件
│   └── TaskProgressBar.kt        # 进度条组件
└── ui/features/chat/viewmodel/
    └── TaskChatViewModel.kt      # 任务+对话双回路 ViewModel
```

### 需要修改的文件

```
app/src/main/java/com/ai/assistance/metaagent/
├── ui/features/chat/screens/     # 聊天界面：嵌入编排树+进度条
├── ui/main/MetaAgentApp.kt       # 导航：添加任务相关路由
└── core/config/SystemPromptConfig.kt  # Prompt：添加"学伴"角色
```

### PC 端新建（Python）

```
pc_orchestrator/
├── main.py                       # FastAPI + WebSocket Server
├── adapters/
│   └── claude_adapter.py         # Claude CLI 适配器
└── requirements.txt              # fastapi, uvicorn, websockets
```

---

## 五、风险与应急

| 风险 | 应急方案 |
|------|---------|
| Claude API/CLI 收费或限速 | 用 Gemini API 替代执行（功能弱一些但能演示） |
| WebSocket 连不上（防火墙） | 改用 HTTP 轮询（降级方案） |
| 编排树生成质量不稳定 | 准备 3 套预置编排树 JSON，Demo 时走预置 |
| 时间不够做异步双线程 | 用 Mock 延迟 + 定时切换 UI 状态模拟并行感 |
| 聊天界面改造冲突 | 新建独立的 `TaskChatScreen`，不改原有 `ChatScreen` |
