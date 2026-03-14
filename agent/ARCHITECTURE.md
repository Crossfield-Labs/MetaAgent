# 架构文档

## 总体结构

`MetaAgent` 当前是一个多模块 Android 工程，主体结构如下：

- `app`
  - 主应用
  - UI、服务、提示词、工具注册、自动化主链都在这里
- `terminal`
  - 终端核心模块
  - 当前仍承担部分执行环境、文件系统或自动化底层支撑
- `quickjs`
  - JavaScript 运行时
  - 用于包工具和脚本工具
- `dragonbones`
  - 动画/角色相关
- `mmd`
  - 模型展示相关
- `showerclient`
  - 虚拟显示与相关通信

## `app` 内主要分层

核心目录：
- `api/`
  - AI 服务接入、增强对话、功能模型调用
- `core/`
  - 配置、工具系统、自动化代理、系统能力
- `data/`
  - 偏好设置、数据库、模型、仓库
- `services/`
  - 前台服务、悬浮窗、默认助手、通知监听等
- `ui/`
  - Compose 页面和组件
- `util/`
  - 公共工具

## 自动化相关组成

这是目前最关键的一条链：

1. 系统提示层
- [SystemPromptConfig.kt](../app/src/main/java/com/ai/assistance/metaagent/core/config/SystemPromptConfig.kt)
- [FunctionalPrompts.kt](../app/src/main/java/com/ai/assistance/metaagent/core/config/FunctionalPrompts.kt)

职责：
- 规定模型何时必须走 `run_ui_subagent`
- 规定多步 App 内任务不能早停
- 规定群聊、联系人、分身选择、发送消息等具体策略

2. UI 子代理入口
- [StandardUITools.kt](../app/src/main/java/com/ai/assistance/metaagent/core/tools/defaultTool/standard/StandardUITools.kt)

职责：
- 注册 `run_ui_subagent`
- 构造 `PhoneAgent`
- 给 UI 控制模型提供系统提示词

3. UI 自动化代理
- [PhoneAgent.kt](../app/src/main/java/com/ai/assistance/metaagent/core/tools/agent/PhoneAgent.kt)

职责：
- 跑多步自动化循环
- 截图、拼接上下文、调用 UI 模型
- 解析 `do(...)` / `finish(...)`
- 拦截明显错误的早停与错误交互

4. 动作执行层
- 同文件内的 `ActionHandler`

职责：
- 执行 `Launch`、`Tap`、`Type`、`Swipe`、`Back`、`Home` 等动作
- 根据当前权限和虚拟屏条件决定走哪条执行路径

5. UI/系统工具实现
- [DebuggerUITools.kt](../app/src/main/java/com/ai/assistance/metaagent/core/tools/defaultTool/debugger/DebuggerUITools.kt)
- [StandardSystemOperationTools.kt](../app/src/main/java/com/ai/assistance/metaagent/core/tools/defaultTool/standard/StandardSystemOperationTools.kt)

职责：
- 真正跟 Android 侧动作、截图、输入、页面信息获取对接

## 权限与虚拟屏链路

关键文件：
- [AndroidPermissionPreferences.kt](../app/src/main/java/com/ai/assistance/metaagent/data/preferences/AndroidPermissionPreferences.kt)
- [DisplayPreferencesManager.kt](../app/src/main/java/com/ai/assistance/metaagent/data/preferences/DisplayPreferencesManager.kt)
- [ShizukuAuthorizer.kt](../app/src/main/java/com/ai/assistance/metaagent/core/tools/system/ShizukuAuthorizer.kt)
- [ShowerController.kt](../app/src/main/java/com/ai/assistance/metaagent/core/tools/agent/ShowerController.kt)

作用：
- 决定当前设备可用的权限层级
- 决定是否能创建和使用虚拟屏
- `Shizuku` 和 `ROOT` 权限都会影响 UI 自动化能力上限

## 包工具系统

关键位置：
- [PackageManager.kt](../app/src/main/java/com/ai/assistance/metaagent/core/tools/packTool/PackageManager.kt)
- [daily_life.js](../app/src/main/assets/packages/daily_life.js)

作用：
- 为 AI 提供包级工具
- 一些功能并不在 Kotlin 工具注册里，而是通过 JS 包暴露

当前注意点：
- 包工具适合补充功能
- 但对于多步 App 内自动化，不应该让它替代 `run_ui_subagent`

## 当前架构上的现实结论

- 这是一个“AI 主对话 + 工具系统 + UI 自动化代理 + Android 系统能力”的组合体
- 自动化问题大多不在页面层，而在：
  - 提示词
  - 代理决策
  - 动作验证
  - 权限/虚拟屏状态
- 所以改自动化时，优先看 `FunctionalPrompts`、`SystemPromptConfig`、`PhoneAgent`、`StandardUITools`

## 关于“能否只靠入口文档接手”

现在这套文档已经足够让新成员继续推进工作，但还不能 100% 复现全部历史对话。
原因不是文档没价值，而是历史对话里包含了很多临时判断、现场调试和过渡性决策。
因此实际接手时，入口文档负责建立方向，状态文档负责建立边界，架构文档负责建立定位。
