# 开发规则

这份规则不是流程口号，而是针对 `MetaAgent` 当前阶段最容易踩坑的点。

## 基本原则

1. 先判断问题类型
- 编译问题先看工程、模块依赖、JNI、资源
- 行为问题先看提示词、代理、动作执行验证

2. 不要把“能打开 app”当成“任务完成”
- 尤其是 QQ、微信、B 站、外卖类场景
- 进入页面不等于完成，必须验证目标对象和最终动作

3. 不要随便回退 `terminal`
- `MetaAgent` 虽然不想保留内置 Ubuntu 产品能力
- 但当前 `terminal core` 仍被一部分自动化或执行链路依赖

## 修改前必做

1. 先定位文件
- 不要先猜
- 用搜索找到真实入口和执行点

2. 先看当前状态
- 优先看 [状态文档](./STATUS.md)

3. 明确影响范围
- 是只影响提示词
- 还是影响工具路由
- 还是影响原生能力

## 自动化问题处理规则

1. 优先修“错误完成”
- 比如：
  - 只打开 app 就说完成
  - 没输入成功却说预填成功
  - 选错群聊还继续往下

2. 多步 App 内任务优先走 `run_ui_subagent`
- 不要轻易改成分享面板式工具
- 不要把用户手动操作当正常完成路径

3. 指定联系人/群聊时必须验证目标
- 不能进入任意聊天页就停
- 必须验证标题或显著标识

4. 说“任选一个”时不要继续追问
- 直接选一个继续

## 提交规则

1. 不提交调试日志
- 如：
  - `qq-repro-logcat.txt`
  - `metaagent-repro-logcat.txt`

2. 不提交无意义临时文件
- 保持工作区干净

3. 提交信息写实际变化
- 好例子：
  - `fix: tighten UI automation routing for mobile chat tasks`
  - `ci: vendor terminal module and enable Android build on dev`

## 构建与发布规则

1. 本地优先
- 当前阶段可以直接使用本地构建产物发布

2. APK 位置
- `app/build/outputs/apk/debug/app-debug.apk`

3. GitHub Release 不要求必须 CI 产出
- 直接上传本地 APK 即可

## 文档维护规则

当仓库状态变化明显时，要同步更新：
- [agent/STATUS.md](./STATUS.md)
- [agent/ARCHITECTURE.md](./ARCHITECTURE.md)
- [README.md](../README.md)

最少要更新：
- 已完成什么
- 还剩什么
- 当前主风险是什么
