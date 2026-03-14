# Agent 路口文档

这份文档是 `agent/` 目录的主入口，用于把接手者导向正确的上下文，而不是让人直接淹没在源码里。

## 先看哪几份

进入 `MetaAgent` 后，按这个顺序看：

1. [状态文档](./STATUS.md)
2. [迁移决策文档](./DECISIONS.md)
3. [架构文档](./ARCHITECTURE.md)
4. [开发规则](./RULES.md)

含义如下：
- `STATUS.md`
  - 现在做到哪一步
  - 还剩哪些问题
- `DECISIONS.md`
  - 这次迁移过程中已经做过哪些关键决策
  - 哪些是过渡方案，哪些是明确方向
- `ARCHITECTURE.md`
  - 功能在哪一层
  - 出问题先看哪几个文件
- `RULES.md`
  - 当前仓库怎么改最不容易踩坑

## 根据什么进入工作流

如果你的问题像下面这样，就从对应文档进入：

```text
根据 agent/STATUS.md，接下来优先做什么？
根据 agent/DECISIONS.md，为什么 terminal 还保留着？
根据 agent/ARCHITECTURE.md，QQ 自动化应该改哪几层？
根据 agent/RULES.md，现在发布 APK 应该怎么做？
根据 app/src/main/java/com/ai/assistance/metaagent/core/tools/agent/PhoneAgent.kt，接下来怎么修复 UI 自动化早停？
```

不要只问：

```text
接下来怎么办？
这个项目怎么改？
```

这种问法没有上下文，容易得到泛化回答。

## 文档同步要求

接手这个仓库时，不要把 `agent/` 文档当成只读说明。出现下面这些情况后，需要同步更新文档：

- 完成了一个明确阶段
- 修掉了一个关键问题
- 发现之前文档里的判断已经过时
- 新确认了一个会影响后续判断的事实

最低要求是：
- 当前进度变化，更新 [状态文档](./STATUS.md)
- 关键方向或约束变化，更新 [迁移决策文档](./DECISIONS.md)

如果代码已经改了，但文档没有同步，后来的人就会根据过时信息继续工作，这是当前仓库要明确避免的情况。

## 当前路口判断

现在这个仓库的大方向不是“继续把空壳补齐”，而是：

1. 保持工程可编译、可安装、可发布
2. 继续稳定手机自动化
3. 清理迁移遗留

如果你接下来要做的事情属于下面几类，建议这样进入：

### 工程与构建

先看：
- [状态文档](./STATUS.md)
- [开发规则](./RULES.md)

再看关键文件：
- [settings.gradle.kts](../settings.gradle.kts)
- [build.gradle.kts](../build.gradle.kts)
- [README.md](../README.md)

### 自动化与手机操作

先看：
- [状态文档](./STATUS.md)
- [迁移决策文档](./DECISIONS.md)
- [架构文档](./ARCHITECTURE.md)

再看关键文件：
- [PhoneAgent.kt](../app/src/main/java/com/ai/assistance/metaagent/core/tools/agent/PhoneAgent.kt)
- [StandardUITools.kt](../app/src/main/java/com/ai/assistance/metaagent/core/tools/defaultTool/standard/StandardUITools.kt)
- [FunctionalPrompts.kt](../app/src/main/java/com/ai/assistance/metaagent/core/config/FunctionalPrompts.kt)
- [SystemPromptConfig.kt](../app/src/main/java/com/ai/assistance/metaagent/core/config/SystemPromptConfig.kt)
- [daily_life.js](../app/src/main/assets/packages/daily_life.js)

### 发布与协作

先看：
- [开发规则](./RULES.md)
- [协作文档](../.github/CONTRIBUTING.md)

再看：
- [README.md](../README.md)

## 当前最重要的上下文

如果你完全不知道这个项目，只看这一份文档，你能掌握的是：
- 这个仓库现在不是空壳
- 它已经能本地编译、安装、演示
- 当前最主要的问题是自动化行为正确性
- `QQ` 指定群聊发消息这条链还没有彻底解决
- `terminal` 现在仍保留，但这是过渡状态
- 本地模型不是当前目标

但只看这一份文档，还不能完整掌握全部历史对话。

你还会缺这些信息：
- 哪些判断是为了比赛演示
- 哪些修复只是暂时兜底
- 哪些问题已经在真机上复现过

所以真正接手时，至少要补读：
- [状态文档](./STATUS.md)
- [迁移决策文档](./DECISIONS.md)

## 目录结构

仓库里最常用的目录：

```text
MetaAgent/
  app/
  quickjs/
  terminal/
  dragonbones/
  mmd/
  showerclient/
  .github/
  agent/
```

## 重要文件位置

基础工程：
- [settings.gradle.kts](../settings.gradle.kts)
- [build.gradle.kts](../build.gradle.kts)
- [README.md](../README.md)

应用入口：
- [AndroidManifest.xml](../app/src/main/AndroidManifest.xml)
- [MetaAgentApplication.kt](../app/src/main/java/com/ai/assistance/metaagent/core/application/MetaAgentApplication.kt)
- [MetaAgentApp.kt](../app/src/main/java/com/ai/assistance/metaagent/ui/main/MetaAgentApp.kt)
- [MetaAgentScreens.kt](../app/src/main/java/com/ai/assistance/metaagent/ui/main/screens/MetaAgentScreens.kt)

自动化主链：
- [PhoneAgent.kt](../app/src/main/java/com/ai/assistance/metaagent/core/tools/agent/PhoneAgent.kt)
- [StandardUITools.kt](../app/src/main/java/com/ai/assistance/metaagent/core/tools/defaultTool/standard/StandardUITools.kt)
- [FunctionalPrompts.kt](../app/src/main/java/com/ai/assistance/metaagent/core/config/FunctionalPrompts.kt)
- [SystemPromptConfig.kt](../app/src/main/java/com/ai/assistance/metaagent/core/config/SystemPromptConfig.kt)
- [daily_life.js](../app/src/main/assets/packages/daily_life.js)

权限与虚拟屏：
- [AndroidPermissionPreferences.kt](../app/src/main/java/com/ai/assistance/metaagent/data/preferences/AndroidPermissionPreferences.kt)
- [DisplayPreferencesManager.kt](../app/src/main/java/com/ai/assistance/metaagent/data/preferences/DisplayPreferencesManager.kt)
- [ShizukuAuthorizer.kt](../app/src/main/java/com/ai/assistance/metaagent/core/tools/system/ShizukuAuthorizer.kt)
- [ShowerController.kt](../app/src/main/java/com/ai/assistance/metaagent/core/tools/agent/ShowerController.kt)

CI 与协作：
- [android-build.yml](../.github/workflows/android-build.yml)
- [CONTRIBUTING.md](../.github/CONTRIBUTING.md)
