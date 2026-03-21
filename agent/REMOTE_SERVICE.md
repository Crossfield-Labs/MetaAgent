# 远程服务说明

## 目标

给电脑端提供一个独立的手机远程控制入口，不把“远程通信”塞进聊天 WebView 本地服务里。

补充说明：
- 这条手机端 HTTP 服务会继续保留
- 但当前阶段的主线目标已经切到“手机端控制电脑端 `nanoclaw`”
- 所以这里更适合作为备用通道、底层能力和后续扩展基础，而不是唯一远控方向

当前实现落在：

- `app/src/main/java/com/ai/assistance/metaagent/remote/RemoteControlService.kt`
- `app/src/main/java/com/ai/assistance/metaagent/remote/RemoteAgentServer.kt`
- `app/src/main/java/com/ai/assistance/metaagent/remote/RemoteAgentTaskManager.kt`
- `app/src/main/java/com/ai/assistance/metaagent/remote/RemoteSessionManager.kt`

## 运行方式

- 服务类型：前台服务
- 默认端口：`8095`
- 协议：HTTP JSON
- 鉴权：`Authorization: Bearer <token>`

先启动 `RemoteControlService`，再通过 HTTP 创建 session。

## 通信模型

分成三层：

1. 会话层
- 负责创建 `sessionId + token`
- 当前同时只保留一个活跃桌面端会话

2. 控制层
- 接收电脑端的截图、输入、启动 agent、查询状态、记忆库操作请求

3. 执行层
- 手工远控优先走 `ShowerController`
- 没有虚拟屏时回退到 `ToolGetter.getUITools(...)`
- AI 自动化任务走 `run_ui_subagent`

## 已实现接口

### 基础

- `GET /api/remote/health`
- `POST /api/remote/session/open`
- `GET /api/remote/session`
- `POST /api/remote/session/close`
- `GET /api/remote/heartbeat`
- `GET /api/remote/capabilities`

### 屏幕与输入

- `GET /api/remote/screenshot`
- `POST /api/remote/input/tap`
- `POST /api/remote/input/swipe`
- `POST /api/remote/input/key`
- `POST /api/remote/input/text`
- `POST /api/remote/app/launch`

### Agent

- `POST /api/remote/agent/run`
- `GET /api/remote/agent/tasks`
- `GET /api/remote/agent/{taskId}/state`
- `POST /api/remote/agent/{taskId}/cancel`

### 记忆库

- `GET /api/remote/memory/query`
- `GET /api/remote/memory/item`
- `GET /api/remote/memory/document`
- `POST /api/remote/memory/create`
- `POST /api/remote/memory/update`
- `POST /api/remote/memory/delete`
- `POST /api/remote/memory/link`
- `GET /api/remote/memory/links`
- `GET /api/remote/memory/graph`

## 请求示例

创建会话：

```http
POST /api/remote/session/open
Content-Type: application/json

{
  "clientName": "desktop"
}
```

返回里会包含：

- `sessionId`
- `token`

之后所有受保护接口都带：

```http
Authorization: Bearer <token>
```

启动一个 UI agent：

```http
POST /api/remote/agent/run
Authorization: Bearer <token>
Content-Type: application/json

{
  "intent": "打开QQ，向测试群发送“你好”",
  "targetApp": "com.tencent.mobileqq",
  "maxSteps": 20,
  "agentId": "desktop-main"
}
```

查询记忆：

```http
GET /api/remote/memory/query?query=比赛项目&limit=10
Authorization: Bearer <token>
```

## 设计约束

- 当前不是产品化多租户设计，只做比赛项目可用的单设备远控入口
- session 目前是单活
- screenshot 返回 `base64`，优先求接入快，不优先求带宽效率
- 记忆库接口暴露的是显式对象，不是“拼 prompt 后的黑箱记忆”

## 当前适合桌面端怎么接

建议桌面端按下面顺序接：

1. `session/open`
2. `heartbeat`
3. `screenshot`
4. `input/tap` / `input/swipe` / `input/key`
5. `agent/run` + `agent/{taskId}/state`
6. `memory/*`

这样可以先完成“连接设备 -> 看到画面 -> 手工控制 -> 跑 agent -> 浏览记忆”的闭环。

## 当前真机结果

已经确认：
- 手机设置页中的“远程控制”入口已可打开
- 可在手机内启动 `RemoteControlService`
- 使用 `adb forward tcp:18095 tcp:8095` 后，开发机上可正常访问：
  - `GET /api/remote/health`
  - `POST /api/remote/session/open`
  - `GET /api/remote/capabilities`
  - `GET /api/remote/heartbeat`

当前未通过：
- `GET /api/remote/screenshot`

已知原因：
- 当前测试机虽然显示权限级别为 `DEBUGGER`
- 但 app 内实际没有可用的 `DEBUGGER/Shizuku` 执行器
- 无障碍也未启用，所以截图回退链路失败

联调备注：
- 当前开发机直连手机局域网地址时，可能被本地代理劫持成 `502 Bad Gateway`
- 调试阶段更稳妥的方式是先用：

```bash
adb forward tcp:18095 tcp:8095
```

然后访问：

```text
http://127.0.0.1:18095
```
