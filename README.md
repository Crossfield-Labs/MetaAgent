# MetaAgent

`MetaAgent` 是一个面向 Android 的 AI 助手工程，当前处于从 `Operit` 迁移和收敛的阶段。

## 仓库定位

当前仓库已经具备这些主体能力：
- AI 对话与工具调用
- 悬浮助手与语音入口
- AutoGLM 与手机自动化
- 记忆库
- 工作流
- 包工具系统
- QuickJS 运行时

当前不保留本地模型主线，`terminal` 仍以核心模块形式保留，但后续还会继续收敛。

## 快速开始

本地构建：

```powershell
.\gradlew.bat assembleDebug --no-daemon --stacktrace --console plain
```

APK 位置：

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Agent 工作流入口

如果你是新接手这个仓库的人，不要直接从源码乱翻，先看：

- [agent/INDEX.md](./agent/INDEX.md)
- [agent/STATUS.md](./agent/STATUS.md)
- [agent/DECISIONS.md](./agent/DECISIONS.md)
- [agent/ARCHITECTURE.md](./agent/ARCHITECTURE.md)
- [agent/RULES.md](./agent/RULES.md)

建议提问格式：

```text
根据 agent/STATUS.md，接下来优先做什么？
根据 agent/DECISIONS.md，为什么 terminal 还保留着？
根据 agent/ARCHITECTURE.md，我要改 UI 自动化，应该看哪些文件？
根据 agent/RULES.md，现在发布 APK 应该怎么做？
根据 app/src/main/java/com/ai/assistance/metaagent/core/tools/agent/PhoneAgent.kt，接下来怎么修？
```

## 协作说明

协作与提交约定见：

- [CONTRIBUTING.md](./.github/CONTRIBUTING.md)

其中也会说明：
- 怎么进入 `agent` 工作流
- 提交前需要注意什么
- 发布时怎么处理本地 APK

## 当前说明

这个仓库已经能本地编译、安装和演示，但自动化链路仍有待继续稳定化。
当前最需要关注的是自动化行为正确性，而不是基础工程是否可编译。

如果只问“根据 `agent/INDEX.md`，接下来怎么做”，现在已经足够继续接手工作，但还不能完整替代全部历史对话。真正接手时，至少还要同时阅读 `agent/STATUS.md`、`agent/DECISIONS.md` 和 `agent/ARCHITECTURE.md`。
