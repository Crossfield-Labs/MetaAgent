package com.ai.assistance.operit.data.model

/**
 * 对话模式枚举
 * - CHAT: 普通聊天模式，适用于日常对话、快速问答
 * - AGENT: Agent任务模式，适用于复杂任务执行、多步骤操作
 */
enum class ConversationMode {
    CHAT,    // 普通聊天模式
    AGENT    // Agent 任务模式
}

/**
 * Agent Plan 状态
 */
enum class PlanStatus {
    PENDING,    // 待审批
    APPROVED,   // 已批准
    REJECTED,   // 已拒绝
    EXECUTING,  // 执行中
    COMPLETED,  // 已完成
    FAILED      // 执行失败
}

/**
 * Plan 步骤状态
 */
enum class StepStatus {
    PENDING,      // 待执行
    IN_PROGRESS,  // 执行中
    COMPLETED,    // 已完成
    FAILED        // 失败
}

/**
 * Agent Plan 步骤
 */
data class PlanStep(
    val stepNumber: Int,
    val description: String,
    val status: StepStatus = StepStatus.PENDING
)

/**
 * Agent Plan 数据模型
 * 用于 Agent 模式下的任务规划和审批
 */
data class AgentPlan(
    val taskDescription: String,            // 任务描述
    val steps: List<PlanStep> = emptyList(), // 执行步骤列表
    val estimatedTime: String = "",          // 预计耗时
    val requiredTools: List<String> = emptyList(), // 需要的工具
    val status: PlanStatus = PlanStatus.PENDING,   // Plan 状态
    val createdAt: Long = System.currentTimeMillis()
)
