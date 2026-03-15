# 状态文档

## 当前总体状态

`MetaAgent` 已经从 `Operit` 迁移出一个可编译、可安装、可演示的版本，但还没有完成彻底清理和稳定性收尾。

当前已经确认：
- 本地 `assembleDebug` 可通过
- APK 可安装到手机
- 基础聊天、页面导航、语音/悬浮窗、工作流、记忆库、工具系统等主体代码已迁入
- `terminal` 已恢复并收编进主仓库
- `Operit` 原仓库缺失的子模块已补齐，现在有了完整对照基线
- 本地模型模块 `mnn`、`llama` 已从主工程依赖中剔除

## 已完成

工程层：
- `MetaAgent` 已是独立仓库
- Windows GitHub Actions 已接入
- `terminal` 不再作为外部 submodule 依赖
- `Operit` 对照基线已补齐完整 submodule
- `MetaAgent` 的 Ubuntu 运行时不再随仓库存放，初始化时从固定 release 资产下载

命名与资源：
- 主包名和 `applicationId` 已切到 `metaagent`
- 主应用图标已替换为 `MetaAgent` 风格
- 一批显眼的 `Operit` 类名、文件名已改掉
- README 已改为简化版

运行与安装：
- 多处 JNI 名称残留已修复
- 当前 debug APK 可本地构建、安装和运行
- 工具权限已偏向演示模式处理

自动化链路：
- `run_ui_subagent` 已被明确设为多步 App 内操作的优先路径
- “任选一个”场景不再允许轻易 `Interact`
- QQ 分享式发送工具已禁用，避免错误降级到分享面板
- 对“打开了/预填了就 finish”的未完成行为已经增加拦截
- 自动化主链关键文件已按完整 `Operit` 基线回退一轮

## 当前主要问题

1. 手机自动化仍不稳定
- 典型问题是 QQ 场景会选错会话、误把群聊当个人、或进入错误聊天页后提前停下
- `打开 QQ，向指定群聊发送消息` 目前仍未彻底修好，属于明确未完成项
- 这类问题现在主要集中在 UI 子代理策略和执行验证，不是基础编译问题

2. `terminal` 仍然带有 Ubuntu 相关历史设计
- 产品目标上不强调内置 Ubuntu
- 但当前自动化和执行能力仍明显依赖 `terminal core + Ubuntu` 运行时
- 这条链已经补回，但大包不进 git，运行时包固定从 `runtime-assets-v1` release 获取

3. 仍有低优先级 `Operit` 残留
- 多在脚本、注释、模板、许可证、少量历史文案中
- 不影响演示，但影响整洁度

## 当前建议优先级

建议优先处理顺序：
1. 自动化稳定性，优先是 QQ 指定群聊发消息
2. 真机回归微信、QQ等关键自动化流程
3. 低优先级命名清理
4. 在验证完成前不要继续裁 `terminal` / Ubuntu 主链

## 近期容易误判的点

- `MetaAgent` 现在不是“没迁完导致不能跑”，而是“能跑，但自动化策略还需要继续收紧”
- `Operit` 现在已经不是之前那个缺子模块的半残基线，后续对比应以补齐后的仓库为准
- QQ 问题不等于 UI 工具完全失效，通常是选错路径、选错会话、过早结束
- GitHub Release 可以直接上传本地 APK，不需要强制走 CI 构建
- Ubuntu 运行时包不在仓库里，需要从 `https://github.com/Crossfield-Labs/MetaAgent/releases/download/runtime-assets-v1/ubuntu-noble-aarch64-pd-v4.18.0.tar.xz` 获取

## 当前发布方式

当前默认做法：
- 本地构建 APK
- 上传 `app/build/outputs/apk/debug/app-debug.apk` 到 GitHub Release

## 接手建议

如果你是新接手这个仓库的人，建议先做这三件事：
1. 跑一次 `assembleDebug`
2. 打开 [架构文档](./ARCHITECTURE.md)
3. 明确你要处理的是“工程问题”还是“自动化行为问题”
