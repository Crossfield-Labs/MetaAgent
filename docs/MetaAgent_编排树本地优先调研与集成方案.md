# MetaAgent 编排树本地优先调研与集成方案

## 1. 先说结论

你的设想是对的：

1. 先打通 **Android 本地编排树调度**，再接入 **PC 节点 / PC Orchestrator**，这是当前仓库和任务书最稳的开发路径。
2. 现有 `chat` 确实已经能调大量本地能力，但当前编排树只接通了其中一小部分。
3. 当前“编排树”在执行层更像“**带依赖的任务列表 / DAG**”，还不是完整的图执行引擎；它可以借 `workflow` 的执行原理，但不建议直接抄成手工工作流编辑器。
4. 编排树后续整合到 `chat` 时，最合理的定位是：**chat 是入口和陪伴界面，plan tree 是 chat 内嵌的任务调度层，workflow 是底层可复用执行资产**。
5. 按任务书，编排树最少要适配的不是“所有工具”，而是能支撑 Demo 闭环的那几类：`CHAT / ANDROID_UI / INTENT / WEB / CLI / WORKFLOW / 后续 PC`。

---

## 2. 为什么“本地优先，再接 PC”是正确路线

### 2.1 产品书本身就是这么设计的

产品书明确强调了三件事：

- 复赛阶段优先做 **稳定可演示的核心闭环**，先用成熟方案把链路打通，而不是一开始就上所有重型能力。见 `docs/MetaAgent_V3_产品书.md:24`
- 编排树核心体验是：
  - 先生成全局计划
  - 用户可审批 / 修改 / 砍分支 / 调优先级
  - 过程实时同步
  见 `docs/MetaAgent_V3_产品书.md:165-183`
- 跨端执行是第二阶段重点，手机与 PC 间以 WebSocket 长连接同步。见 `docs/MetaAgent_V3_产品书.md:311-315`、`docs/MetaAgent_V3_产品书.md:824-925`

### 2.2 三天冲刺计划也明确分阶段

三天冲刺计划和你的思路完全一致：

- Day 1 / Phase 1：Android 本地生成编排树，聊天界面展示编排树和进度流。见 `docs/MetaAgent_三天冲刺_安卓开发计划.md:10-13`
- Day 2 / Phase 2：WebSocket + PC Orchestrator + Claude 执行。见 `docs/MetaAgent_三天冲刺_安卓开发计划.md:15-18`
- Day 3 / Phase 3：异步双线程和 Demo 打磨。见 `docs/MetaAgent_三天冲刺_安卓开发计划.md:20`

所以从路线正确性上，这不是“妥协方案”，而是**任务书和计划书都支持的正解**。

---

## 3. 现有 chat 到底已经有多少本地能力

### 3.1 结论

现有 `chat` 不是一个单纯问答界面，而是一个已经挂了大量本地工具的“通用执行入口”。

`AIToolHandler.registerDefaultTools()` 会统一注册默认工具，工具定义集中在：

- `app/src/main/java/com/ai/assistance/metaagent/core/tools/AIToolHandler.kt:175`
- `app/src/main/java/com/ai/assistance/metaagent/core/tools/ToolRegistration.kt`

当前 `ToolRegistration.kt` 里注册的工具数量是 **119 个**。

### 3.2 现有可调本地能力总表

下面是对编排树最相关的本地能力分类。

| 类别 | 代表工具 | 仓库位置 | 适合的编排节点 |
| --- | --- | --- | --- |
| Shell / Terminal / Workspace | `execute_shell`、`create_terminal_session`、`execute_in_terminal_session` | `ToolRegistration.kt:75-167` | 本地命令、环境检查、脚本执行、工作区整理 |
| 文件系统 | `list_files`、`read_file`、`write_file`、`find_files`、`zip_files` | `ToolRegistration.kt:1231-1636` | 代码、资料、实验文件、报告产物 |
| Web / 浏览器 | `visit_web`、`start_web`、`web_navigate`、`web_click`、`web_fill`、`web_snapshot` | `ToolRegistration.kt:742-877` | 搜索、网页查找、网页操作 |
| HTTP / 接口 | `http_request`、`multipart_request`、`manage_cookies` | `ToolRegistration.kt:1453-1483` | 外部接口调用、联网检索、上传下载 |
| Android 系统 / App | `execute_intent`、`send_broadcast`、`start_app`、`stop_app`、`list_installed_apps` | `ToolRegistration.kt:919-973`、`ToolRegistration.kt:1799-1820` | 打开 App、跳转页面、系统联动 |
| UI 自动化 / AutoGLM 风格 | `click_element`、`tap`、`long_press`、`capture_screenshot`、`run_ui_subagent`、`set_input_text`、`swipe` | `ToolRegistration.kt:1363-1453`、`ToolRegistration.kt:1879-1946` | 手机上的复杂界面操作 |
| 记忆与画像 | `query_memory`、`update_user_preferences`、`create_memory` | `ToolRegistration.kt:379-565` | 课程空间、学习画像、上下文注入 |
| Workflow 本体 | `get_all_workflows`、`create_workflow`、`trigger_workflow` | `ToolRegistration.kt:1026-1093` | 把已有工作流当作编排树里的子能力 |
| Chat / Agent 服务 | `send_message_to_ai`、`send_message_to_ai_advanced`、`agent_status` | `ToolRegistration.kt:1186-1197`、`ToolRegistration.kt:1146` | 聊天型节点、解释型节点 |
| 系统反馈 | `toast`、`send_notification` | `ToolRegistration.kt:1730-1738` | 用户提示、主动回调 |
| 媒体处理 | `ffmpeg_execute`、`ffmpeg_info`、`ffmpeg_convert` | `ToolRegistration.kt:1963-1987` | 课程资料处理、视频音频转码 |

### 3.3 AutoGLM / PhoneAgent 这条线是实的

仓库里本来就有 AutoGLM 风格的 UI 自动化能力，不是概念稿：

- UI 自动化提示词：`core/config/FunctionalPrompts.kt:574`
- PhoneAgent：`core/tools/agent/PhoneAgent.kt:119`
- `run_ui_subagent` 入口：`core/tools/defaultTool/standard/StandardUITools.kt:409`

这意味着“安卓本地编排树 -> 调 UI 子代理去真实操作 App”这条链是有现实基础的。

---

## 4. 当前编排树已经接到了哪里，没接到哪里

### 4.1 已经接通的部分

当前本地 MVP 已经实现了：

- `ChatViewModel` 挂了编排树状态：
  - `showPlanTree`
  - `taskSession`
  - `createPlanFromGoal()`
  - `approvePlan() / rejectPlan() / pause / resume / cancel`
  见 `ui/features/chat/viewmodel/ChatViewModel.kt:416-422`、`ui/features/chat/viewmodel/ChatViewModel.kt:2286-2402`
- `AIChatScreen` 已经把编排树作为第三个按钮和独立面板接进聊天页：
  - 顶栏树按钮：`AIChatScreen.kt:759-772`
  - 编排树面板：`AIChatScreen.kt:1411-1419`
  - 顶部进度条：`AIChatScreen.kt:1431-1432`
- `PlanTreeExecutor` 已经把节点执行从纯 Mock 改成了本地 adapter 分发：
  - `CHAT / CLAUDE -> executeChatNode`
  - `CLI / LOCAL_RUNNER -> executeCliNode`
  - `ANDROID -> executeAndroidNode`
  见 `core/plan/PlanTreeExecutor.kt:340-457`

### 4.2 还没接通的部分

虽然 chat 有很多工具，但当前 plan tree 只直接调通了三类：

- `CHAT`
- `CLI`
- `ANDROID`

还**没有**通用接进 plan tree 的能力包括：

- `visit_web`
- `http_request`
- `execute_intent`
- `trigger_workflow`
- 文件系统细粒度工具
- 浏览器 Web 工具

也就是说：

- 仓库能力池很大
- 但当前编排树只接了“主干最小集”

这也是为什么你举的“网易云搜索热门歌 -> 打开 App -> 点赞播放 -> 回报结果”这个例子，**概念上能做，当前代码版还不能完整自动化做到**。

---

## 5. 当前编排树为什么“看起来像列表”

### 5.1 数据结构层已经不是纯列表

`PlanNode` 设计上已经有图结构的意图：

- `dependsOn`：依赖关系
- `children`：树形层级
- `requiresApproval`
- `confidence`

见：

- `core/plan/model/PlanNode.kt:62-99`

### 5.2 但运行时和 UI 目前主要按线性列表工作

当前“像列表”的原因不是错觉，而是事实：

1. 执行调度主要靠 `dependsOn`
   - `TaskSession.findNextExecutableNode()` 只是找“第一个所有依赖都完成、状态还是 PENDING 的节点”
   - 见 `core/plan/model/TaskSession.kt:142-146`

2. UI 渲染是线性列表
   - `PlanTreeCard` 直接 `taskSession.planNodes.forEachIndexed`
   - 节点之间只画简单竖线
   - 没有真正按图布局，也没有显式展示分支 / 合流 / 条件
   - 见 `ui/features/chat/components/PlanTreeCard.kt:139-146`

3. `children` 目前基本没参与执行器和 UI 的核心逻辑

所以现在它更准确的说法是：

> **用户可见的任务节点序列 + DAG 依赖执行雏形**

而不是一个完整图工作流编辑器。

---

## 6. workflow 是什么，它的原理是什么

### 6.1 workflow 的定位

仓库原本就有一套独立的 `workflow` 系统。它不是“让 AI 运行时生成计划”的，而是：

> **用户/系统预先定义好的静态自动化流程引擎**

核心模型在：

- `data/model/Workflow.kt`

### 6.2 workflow 支持哪些节点类型

`Workflow.kt` 里定义了这些核心节点：

| 节点类型 | 作用 | 位置 |
| --- | --- | --- |
| `TriggerNode` | 定义触发方式，支持 `manual / schedule / event` | `Workflow.kt:55` |
| `ExecuteNode` | 真正调用工具，`actionType` 对应工具名 | `Workflow.kt:70` |
| `ConditionNode` | 条件判断 | `Workflow.kt:96` |
| `LogicNode` | 逻辑聚合，支持 `AND / OR` | `Workflow.kt:114` |
| `ExtractNode` | 从文本或 JSON 里提取变量，也支持随机数/字符串等 | `Workflow.kt:134` |
| `WorkflowNodeConnection` | 图上的边，可带条件 | `Workflow.kt:183` |

此外，`ExecuteNode.actionConfig` 支持 `ParameterValue.StaticValue` 和 `ParameterValue.NodeReference`，意味着：

- 参数可以写死
- 也可以引用前序节点的输出

这是它非常关键的一点。

### 6.3 workflow 执行器是怎么跑起来的

执行器在：

- `core/workflow/WorkflowExecutor.kt:69`

其工作原理大致如下。

#### 第一步：找触发入口

`executeWorkflow()` 会先找到所有触发节点：

- 指定 `triggerNodeId` 时，只从指定触发点开始
- 不指定时，默认执行所有 `manual` 触发节点

见 `WorkflowExecutor.kt:486-603`

#### 第二步：构建依赖图

执行器会把整张 workflow 图转成依赖图：

- 显式连接：来自 `workflow.connections`
- 隐式依赖：来自参数引用 `NodeReference`

也就是说，哪怕图上没拉线，只要某个节点参数引用了前一个节点，也会自动形成依赖。

见：

- `WorkflowExecutor.kt:658-704`

#### 第三步：做环检测

执行器会对有向图做 cycle 检测，如果有环直接拒绝执行。

见：

- `WorkflowExecutor.kt:571-584`
- `WorkflowExecutor.kt:706-734`

#### 第四步：按拓扑顺序推进

执行器不是“随便遍历列表”，而是按依赖满足顺序推进。

见：

- `WorkflowExecutor.kt` 中的 `executeTopologicalOrder(...)`
- `executeWorkflow()` 里对它的调用：`WorkflowExecutor.kt:603-616`

#### 第五步：按节点类型分发

`executeNode(...)` 会根据节点类型分别处理：

- `TriggerNode`：直接标记成功，不需要真的执行
- `ConditionNode`：取左右值，做比较
- `LogicNode`：聚合多个布尔输入
- `ExtractNode`：正则 / JSONPath / substring / concat / random
- `ExecuteNode`：构造 `AITool`，交给 `toolHandler.executeTool(tool)`

见：

- `WorkflowExecutor.kt:957-1126`

这意味着 `workflow` 的底层执行模型本质上是：

> **图结构 + 参数引用 + 条件 / 逻辑 / 提取 + AITool 执行**

### 6.4 workflow 还有调度能力

`WorkflowScheduler` 可以根据 `TriggerNode.triggerConfig` 做：

- interval
- one-time specific time
- cron 风格周期

底层通过 WorkManager 运行。

见：

- `core/workflow/WorkflowScheduler.kt:22`
- `core/workflow/WorkflowScheduler.kt:53-75`

### 6.5 workflow 还有模板与管理 UI

workflow 不只是引擎，仓库里还有完整的管理界面和模板体系：

- 列表页：`ui/features/workflow/screens/WorkflowListScreen.kt:48`
- 支持模板创建：
  - chat
  - condition
  - logic and / or
  - extract
  - error branch
  - speech trigger
  见 `WorkflowListScreen.kt:297-345`

所以 workflow 这套东西不是边角料，而是仓库里已经成熟的一整套自动化能力。

---

## 7. 编排树是不是要“抄 workflow”

### 7.1 不建议直接抄成手工 workflow 编辑器

原因很简单：

- workflow 的核心是“**人预先配好静态流程**”
- 编排树的核心是“**AI 针对当前用户目标运行时自动生成，并可审批 / 修改 / 动态重规划**”

如果直接把编排树做成 workflow 画布的另一个壳子，会丢掉产品书最重要的创新点：

- chat 内直接发起复杂任务
- AI 先给出全局计划
- 用户批准后执行
- 中途变向再重规划
- 过程可见

### 7.2 但强烈建议“借 workflow 的底层原理”

最值得借的是这些层：

1. **图执行思路**
   - 显式连接
   - 参数引用形成隐式依赖
   - 环检测
   - 拓扑推进

2. **节点语义**
   - 不要只有 `EXEC / CLARIFY / CHECKPOINT / REPLAN`
   - 后续可逐步加 `CONDITION / LOGIC / EXTRACT`

3. **工具调用方式**
   - `ExecuteNode` -> `AITool` -> `AIToolHandler`
   - 这是现有仓库里最强的复用点

4. **workflow 作为 plan node 的子能力**
   - 不是把 plan tree 改成 workflow
   - 而是允许 plan node 直接触发一个已有 workflow

### 7.3 我建议的方向

不是“抄 UI”，而是“借执行语义”：

| 层 | 是否建议复用 workflow | 原因 |
| --- | --- | --- |
| 图编辑器 UI | 不建议直接抄 | 编排树是 chat-native，不是画布-first |
| 节点执行语义 | 强烈建议复用 | 已有拓扑图执行经验 |
| 条件 / 逻辑 / 提取能力 | 建议逐步借 | 这些是编排树后续复杂化的关键 |
| 直接触发工具 | 强烈建议复用 | 现有工具生态已经很大 |
| 调度器 | 仅在固定自动化场景复用 | 编排树本身更偏即时任务 |

---

## 8. 编排树接下来应该怎样整合到 chat

### 8.1 chat 仍然是主入口

编排树不应该脱离 chat 独立成长为另一个系统。

更合理的结构是：

- 普通问题 -> 继续走原 chat
- 复杂目标 -> 进入 plan tree 模式
- 执行过程中 -> 前台 chat 继续可插话、催进度、改方向

也就是：

> **chat 是入口和陪伴层，plan tree 是 chat 内嵌任务编排层**

### 8.2 当前仓库已经有初步集成形态

现在已经具备以下集成方式：

- 顶栏第三个树按钮：`AIChatScreen.kt:759-772`
- 编排树独立面板：`AIChatScreen.kt:1411-1419`
- 顶部悬浮进度条：`AIChatScreen.kt:1431-1432`
- `ChatViewModel` 管理 `showPlanTree` / `taskSession`：`ChatViewModel.kt:416-422`

这条路是对的，后续不需要推翻。

### 8.3 后续整合建议

建议把接入方式演进成下面这样：

#### 第一层：显式进入

用户点树按钮，进入 plan 面板，输入目标生成计划。

这已经做到了。

#### 第二层：聊天自动识别复杂目标

当用户在普通 chat 里输入明显复杂目标时：

- Chat Plugin / Intent Classifier 识别为“共做任务”
- 自动触发 `createPlanFromGoal()`
- 在消息流里插入一条“任务编排卡片”
- 并允许用户切到完整 plan panel

这一步正好和三天冲刺计划里的“编排树逻辑可实现为 Chat Plugin”一致。见 `docs/MetaAgent_三天冲刺_安卓开发计划.md:171`

#### 第三层：异步双线程

执行状态和对话状态彻底分开：

- 执行线程：plan tree / node events / progress
- 对话线程：chat message / reply / 用户插话

这就是任务书里的“后台干活 + 前台陪你”。见 `MetaAgent_V3_产品书.md:319-335`

---

## 9. 基于任务书，编排树最少要适配哪些能力

这个问题不能按“理论上最好全部支持”来答，而应该按 **Demo 闭环必须具备** 来答。

### 9.1 P0 最小能力表

| 能力 | 为什么必须有 | 现状 | 建议优先级 |
| --- | --- | --- | --- |
| 计划生成 | 没有计划生成就没有编排树 | 已有 `PlanTreeGenerator` | P0 |
| 审批 / 重规划 | 是产品书核心差异点 | 已有 | P0 |
| 进度流 / 事件流 | 异步双线程展示核心 | 已有基础版 | P0 |
| Chat 分析节点 | 用来解释、汇总、决策 | 已接 | P0 |
| Android UI 节点 | 演示手机端真实操作 | 已接 | P0 |
| CLI / Workspace 节点 | 演示工作区检查、脚本执行 | 已接 | P0 |
| Intent / App 打开节点 | 很多手机操作先要打开 App | 仓库已有工具，plan tree 未接 | P0 |
| Web / HTTP 节点 | 需要联网搜索、查资料、查接口 | 仓库已有工具，plan tree 未接 | P0 |
| Workflow 节点 | 复用现成自动化资产 | 仓库已有工具，plan tree 未接 | P1 |
| PC 节点 | 跨端执行亮点 | 尚未接 | P1 |

### 9.2 为什么 `INTENT` 和 `WEB` 应该尽快补

你举的例子非常典型：

> 搜热门歌 -> 选歌 -> 打开网易云 -> 播放 / 点赞 -> 回复结果

这条链如果只靠现在的 `CHAT / ANDROID / CLI`，会有明显短板：

- `CHAT` 现在是纯模型分析，不会自动调用 `visit_web/http_request`
- `ANDROID` 可以做 UI 自动化，但不擅长先联网搜信息
- 没有 `execute_intent`，很多 App 打开动作只能靠 UI 猜路径，不够稳

所以只要想做更像“真的自己会选路”的编排树，至少要补上：

- `WEB`
- `INTENT`

### 9.3 为什么 `WORKFLOW` 值得做成 plan node

因为 workflow 已经能：

- 条件分支
- 逻辑聚合
- 提取变量
- 调工具
- 定时执行

如果编排树后续允许某个 plan node 直接写成：

- “触发一个已有 workflow”

那么大量原仓库已有能力就不用在 `PlanTreeExecutor` 里重新硬编码一遍。

---

## 10. 用“网易云点赞一首歌”这个例子看当前差距

### 10.1 目标拆解

如果从“模型自己决定走哪条路”的角度看，一个比较合理的计划大概会是：

1. 搜索当前热门歌曲
2. 选择一首合适的目标歌曲
3. 打开网易云音乐
4. 在 App 中搜索并进入歌曲页
5. 执行点赞 / 播放
6. 向用户回报结果

### 10.2 当前版本能做到什么

当前版本最稳的是：

- 生成一个类似的节点序列
- `ANDROID` 节点调用 `run_ui_subagent`
- 最后 `CHAT` 节点总结结果

### 10.3 当前版本还做不到什么

还做不到真正的“动态最优路径”：

- 不能在执行时自动权衡“是用 Web 搜索，还是直接 App 搜索”
- 不能显式比较多条候选路径并打分
- `CHAT` 节点现在没有开放工具调用能力，因此不能在执行节点内部真去调 `visit_web` / `http_request`

### 10.4 要做到这个例子，最少还要补哪些点

#### 节点执行层面

- 新增 `WEB` 或 `TOOL` 执行层
- 新增 `INTENT` 或直接允许 `ANDROID` 节点指定更细工具

#### 节点 schema 层面

当前节点只有 `adapter`，不够细。

后续建议升级为：

```json
{
  "id": "n1",
  "title": "搜索热门歌曲",
  "kind": "EXEC",
  "adapter": "TOOL",
  "toolName": "visit_web",
  "toolParams": {
    "url": "..."
  },
  "dependsOn": [],
  "requiresApproval": false
}
```

以及：

```json
{
  "id": "n4",
  "title": "打开网易云并点赞",
  "kind": "EXEC",
  "adapter": "ANDROID",
  "toolName": "run_ui_subagent",
  "toolParams": {
    "intent": "打开网易云音乐并搜索某首歌后点赞"
  },
  "dependsOn": ["n2", "n3"]
}
```

#### 策略层面

再往后才是“动态加权路径选择”：

- 根据成功率、权限可用性、设备状态、是否联网，选择 `WEB` / `INTENT` / `ANDROID` / `WORKFLOW`

这一步是 P1/P2，不是现在本地 MVP 第一阶段必须完成的。

---

## 11. 我建议的编排树下一版结构

### 11.1 不要只保留“adapter 粗分类”

当前只用 `adapter`，会出现两个问题：

- 模型只能说“交给 CHAT 还是 ANDROID”
- 不能精确到具体工具、具体 workflow

建议升级为两层：

1. **谁来执行**
   - `CHAT`
   - `ANDROID`
   - `CLI`
   - `TOOL`
   - `WORKFLOW`
   - `PC`

2. **具体执行什么**
   - `toolName`
   - `toolParams`
   - `workflowId`

### 11.2 推荐的最小 schema 演进

建议未来从当前 `PlanNode` 演进到下面这种思路：

- `adapter`: 执行平面
- `toolName`: 具体工具
- `toolParams`: 工具参数
- `workflowId`: 当 adapter 为 `WORKFLOW` 时使用
- `priority`: 用户可调优先级
- `estimatedRisk`: 用户审批时有依据
- `parallelGroup`: 并行节点分组

这套 schema 更接近产品书里“全局计划 + 用户可控 + 后续跨端”的设计目标。

---

## 12. 接下来具体应该怎么做

### 12.1 第一阶段：把本地编排树从“3 类 adapter MVP”扩成“可演示的真实调度器”

优先顺序建议如下：

1. 新增 `WEB/TOOL` 执行层
2. 新增 `INTENT` 直达能力
3. 新增 `WORKFLOW` 节点执行能力
4. 普通 chat 输入自动识别复杂目标并转计划

### 12.2 第二阶段：把当前“列表感”升级为真正的图语义

不一定先改 UI，但执行语义上建议先补：

- `CONDITION`
- `LOGIC`
- `EXTRACT`
- `parallelGroup`

也就是说，先让它在内核上更像图，再决定 UI 要不要画成复杂图。

### 12.3 第三阶段：再接 PC

本地链路稳了以后再接：

- `PC_WS`
- `Claude Adapter`
- `Browser Adapter`
- `CLI Adapter`

此时 Android 侧尽量只保留：

- plan 生成
- 审批
- 进度展示
- chat 对话

PC 端则承担更重的执行任务。

---

## 13. 最终建议

### 13.1 对“是否先用本地能力做编排测试”

结论：**非常合理，而且应该这么做。**

### 13.2 对“是否要抄 workflow”

结论：**不要直接抄 workflow UI，但一定要借 workflow 的执行语义与工具复用能力。**

### 13.3 对“编排树最少要适配什么”

结论：先补这几类：

- `CHAT`
- `ANDROID_UI`
- `INTENT`
- `WEB`
- `CLI`
- `WORKFLOW`
- 然后再加 `PC`

### 13.4 对当前仓库的总体判断

最值得继续走的路线是：

> **Chat 作为主入口**
>  
> **Plan Tree 作为 chat 内嵌的动态任务编排层**
>  
> **Workflow 作为底层静态自动化资产库**
>  
> **PC 作为后续扩展出来的重执行平面**

这条路线和产品书、冲刺计划、现有仓库能力，是一致的。

