# 编排树开发计划（今晚 + 明天）

## 今晚目标：编排树能调度手机全量工具解决复杂问题

---

### Task 1：通用工具执行层（最高优先级）

**目标**：PlanTreeExecutor 不再硬编码 3 个 adapter，而是能调任意工具

**改动文件**：`PlanTreeExecutor.kt`

**做什么**：
- PlanNode 新增字段 `toolName` + `toolParams`
- 新增 `executeToolNode(node)` → 构造 `AITool` → `toolHandler.executeTool()`
- adapter 增加 `TOOL` 类型：visit_web / http_request / execute_intent / 任意工具都能调
- 保留 CHAT/ANDROID/CLI 三个特化路径（它们有定制 prompt 逻辑）

**验证**：
```
目标："帮我查一下今天天气"
→ Node 1 [TOOL] toolName=visit_web → 成功返回网页内容
→ Node 2 [CHAT] 整理天气信息 → 结果回到聊天
```

---

### Task 2：节点间数据传递

**目标**：后续节点能拿到前序节点的执行结果

**改动文件**：`PlanTreeExecutor.kt` 的 `buildChatExecutionPrompt()`

**做什么**：
- 把已完成节点的 `resultSummary` 拼入后续节点的执行 prompt
- ANDROID 节点的 intent 也注入前序上下文

**验证**：
```
目标："去小红书找保研信息，整理后告诉我"
→ Node 1 [ANDROID] 打开小红书搜索 → resultSummary 记录搜索结果
→ Node 2 [CHAT] 收到 Node 1 结果，整理输出
```

---

### Task 3：编排卡片嵌入 chat 消息流（Agent 模式入口）

**目标**：用户在 chat 里发送复杂目标 → 消息流中插入编排卡片 → 点击卡片进入详情面板

**交互流程**：
```
用户发消息 "帮我去小红书找保研信息整理好"
      ↓
ChatViewModel 识别为复杂任务（agent 模式）
      ↓
消息流中插入一条特殊消息：PlanTreeCardMessage
  - 初始状态："编排计划生成中…" (带 loading 动画)
  - 生成完成："已生成 4 个步骤，点击查看详情" (带迷你节点列表)
  - 执行中："执行中 2/4，正在整理信息…" (带进度条)
      ↓
用户点击卡片 → togglePlanTree() → 打开 PlanTreePanel 覆盖层
用户不点 → TaskProgressBar 在顶部浮动显示进度
```

**改动文件**：
- `ChatViewModel.kt` — 识别复杂任务 + 插入卡片消息
- 新建 `PlanTreeCardMessage.kt` — chat 消息流中的编排卡片 Composable
- `ChatScreenContent.kt` — 在消息列表中渲染 PlanTreeCardMessage

**验证**：
- 用户在 chat 输入复杂目标 → 消息流中出现编排卡片
- 卡片显示节点数量和当前状态
- 点击卡片 → 跳转到已有的 PlanTreePanel 详情
- 后台执行中，卡片实时更新进度

---

### Task 4：PlanTreeGenerator prompt 升级

**目标**：让 LLM 生成的节点能指定具体工具，不只是 adapter 大类

**改动文件**：`PlanTreeGenerator.kt`

**做什么**：
- prompt 中列出可用工具清单（从 toolHandler 读取 top 30 常用工具）
- 要求 LLM 输出 JSON 时包含 `toolName` 和 `toolParams`
- 解析时填入 PlanNode

**验证**：
```
目标："打开网易云音乐搜索一首歌然后播放"
→ 生成节点应包含: 
  Node 1: adapter=TOOL, toolName=execute_intent, toolParams={package: com.netease.cloudmusic}
  Node 2: adapter=ANDROID, toolName=run_ui_subagent, toolParams={intent: "搜索xxx并播放"}
  Node 3: adapter=CHAT
```

---

### Task 5：编译 + 真机联调

**验证清单**：
1. `./gradlew assembleDebug` 通过
2. 安装到真机
3. 点击编排树按钮 → 输入"检查当前工作区文件结构"→ 生成 2-3 个节点
4. 点批准 → CLI 节点执行 pwd/ls → CHAT 节点整理结果
5. 全部完成 → 显示已完成

---

## 明天目标：PC 调度 + 异步双线程

### Task 6：WebSocket 通信层

- Android 端 WebSocket 客户端
- PC 端 Claude Agent SDK 适配器
- PlanNode adapter=PC → 通过 WebSocket 发指令

### Task 7：异步执行反馈机制

**你提的问题很关键**：AutoGLM/终端执行时，输出是异步的。

**做法**：
- `run_ui_subagent` 本身是 suspend 函数，已经是异步的 — 它会等 PhoneAgent 执行完才返回结果
- 终端 `execute_shell` 也是 suspend，会等命令完成
- 所以"调度员"（PlanTreeExecutor）天然就是等每个节点完成后拿到结果再推进下一个
- 如果某个节点执行时间很长（比如跑实验），用户前台可以继续聊天（异步双线程已有雏形：执行在 `Dispatchers.IO`，UI 在主线程）

**需要补的**：
- 执行超时机制（某个 UI 子代理卡住了不能永远等）
- 执行日志实时回流（目前只有开始/完成两个事件，缺中间日志）

### Task 8：Demo 场景打磨

打通 2-3 个端到端场景：
1. "帮我检查工作区并整理实验报告"（CLI + CHAT）
2. "去小红书找保研信息整理给我"（ANDROID + CHAT）
3. "在 PC 上跑 CNN 实验，手机端看进度"（PC + CHAT，明天）
