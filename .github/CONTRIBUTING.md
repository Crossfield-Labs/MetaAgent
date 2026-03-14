# 协作说明

这份文档面向参与 `MetaAgent` 开发、调试、发布的成员。

## 先看什么

进入仓库后，优先顺序如下：

1. [agent/INDEX.md](../agent/INDEX.md)
2. [agent/STATUS.md](../agent/STATUS.md)
3. [agent/DECISIONS.md](../agent/DECISIONS.md)
4. [agent/ARCHITECTURE.md](../agent/ARCHITECTURE.md)
5. [agent/RULES.md](../agent/RULES.md)

不要先凭印象改代码。

## 建议提问格式

如果要让 AI 或其他成员快速接手，请这样提问：

```text
根据 agent/STATUS.md，接下来怎么做？
根据 agent/DECISIONS.md，为什么当前不彻底移除 terminal？
根据 agent/ARCHITECTURE.md，QQ 自动化问题应该改哪一层？
根据 agent/RULES.md，我现在提交到 main 前还要检查什么？
根据 app/src/main/java/com/ai/assistance/metaagent/core/config/FunctionalPrompts.kt，接下来怎么收紧提示词？
```

## 本地开发

构建命令：

```powershell
.\gradlew.bat assembleDebug --no-daemon --stacktrace --console plain
```

APK 位置：

```text
app/build/outputs/apk/debug/app-debug.apk
```

当前阶段允许直接上传本地构建出来的 APK 到 GitHub Release，不要求必须由 CI 产出。

## 提交前检查

提交前至少确认：

1. `git status` 干净或仅包含本次改动
2. 不要提交调试日志、临时导出文件
3. 如果改了自动化行为，优先说明改的是哪条链路

不要提交这类噪声文件：
- `qq-repro-logcat.txt`
- `metaagent-repro-logcat.txt`

## 提交信息建议

提交信息要写实际变化，不要写空话。

示例：
- `fix: tighten UI automation routing for mobile chat tasks`
- `ci: vendor terminal module and enable Android build on dev`
- `style: replace Operit app icon with MetaAgent adaptive icon`

## 当前协作重点

当前最重要的是：
- 自动化行为稳定性
- 演示链路稳定性
- 文档同步

当前不是优先做大规模重构的时候。
