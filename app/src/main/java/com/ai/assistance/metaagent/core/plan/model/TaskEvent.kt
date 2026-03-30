package com.ai.assistance.metaagent.core.plan.model

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * 任务事件类型
 *
 * 三层事件分级：
 * - 原始执行事件（日志/调试）
 * - 结构化任务事件（业务逻辑）→ 对应本枚举
 * - 用户可读事件（手机 UI）→ 由 TaskEvent.message 承载
 */
@Serializable
enum class TaskEventType {
    // === 编排树生命周期 ===
    /** 编排树生成完成 */
    PLAN_GENERATED,
    /** 用户审批通过 */
    PLAN_APPROVED,
    /** 用户修改了编排树 */
    PLAN_MODIFIED,
    /** 触发重规划 */
    PLAN_REPLANNED,

    // === 节点执行 ===
    /** 节点开始执行 */
    NODE_STARTED,
    /** 节点执行进度更新 */
    NODE_PROGRESS,
    /** 节点执行完成 */
    NODE_COMPLETED,
    /** 节点执行失败 */
    NODE_FAILED,
    /** 节点需要用户确认（谦虚机制） */
    NODE_BLOCKED,

    // === 任务级别 ===
    /** 整个任务完成 */
    TASK_COMPLETED,
    /** 整个任务失败 */
    TASK_FAILED,
    /** 任务暂停 */
    TASK_PAUSED,
    /** 任务恢复 */
    TASK_RESUMED,

    // === 用户交互 ===
    /** 用户插话/改方向 */
    USER_INTERVENTION,
    /** 对话回路回复 */
    DIALOGUE_REPLY,

    // === PC Session projection ===
    /** PC 会话阶段变化 */
    PC_SESSION_PHASE,
    /** PC 会话摘要 */
    PC_SESSION_SUMMARY,
    /** PC 当前 worker / profile / sessionMode */
    PC_SESSION_WORKER,
    /** PC worker 运行态摘要 */
    PC_SESSION_RUNTIME,
    /** PC 产物摘要 */
    PC_SESSION_ARTIFACT,
    /** PC 权限摘要 */
    PC_SESSION_PERMISSION,
    /** PC MCP 状态摘要 */
    PC_SESSION_MCP_STATUS,
    /** PC follow-up / interrupt / redirect 被接受 */
    PC_SESSION_FOLLOWUP,
    /** PC snapshot 更新 */
    PC_SESSION_SNAPSHOT,
    /** PC 会话等待用户输入 */
    PC_SESSION_AWAIT_USER,

    // === 系统 ===
    /** 连接建立 */
    CONNECTED,
    /** 连接断开 */
    DISCONNECTED
}

/**
 * 任务事件
 *
 * 编排树执行过程中产生的所有事件。事件是不可变的、仅追加的记录。
 * 事件序号 [seq] 单调递增，支持断线重连后从 lastSeq 补拉。
 */
@Serializable
data class TaskEvent(
    /** 事件唯一 ID */
    val id: String = UUID.randomUUID().toString().take(12),
    /** 事件序号（单调递增，用于断线补拉） */
    val seq: Int,
    /** 事件类型 */
    val type: TaskEventType,
    /** 关联的任务 ID */
    val taskId: String,
    /** 关联的节点 ID（节点级事件） */
    val nodeId: String? = null,
    /** 用户可读消息 */
    val message: String = "",
    /** 进度值 0.0 ~ 1.0（进度事件） */
    val progress: Float? = null,
    /** 附加数据（JSON 字符串，灵活扩展） */
    val data: String? = null,
    /** 事件时间戳 */
    val timestamp: Long = System.currentTimeMillis()
)
