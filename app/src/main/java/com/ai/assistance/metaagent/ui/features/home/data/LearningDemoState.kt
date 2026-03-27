package com.ai.assistance.metaagent.ui.features.home.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class CrossDeviceTaskStage {
    DRAFT,
    PLAN_READY,
    APPROVED,
    RUNNING_ON_DESKTOP,
    CHATTING_ON_PHONE,
    REDIRECTED
}

enum class CourseDemoStage {
    EMPTY,
    IMPORTED,
    PROCESSING,
    GENERATED,
    QA
}

enum class ReviewDemoStage {
    START,
    ANSWERED_WRONG,
    PROFILE_UPDATED,
    REMINDER_READY,
    RESUMED
}

object LearningDemoState {
    var taskStage by mutableStateOf(CrossDeviceTaskStage.DRAFT)
    var taskGoal by mutableStateOf("帮我整理 CNN 实验并在电脑端继续跑训练")
    var taskPlanApproved by mutableStateOf(false)
    var desktopProgress by mutableStateOf(0.18f)
    var taskDirection by mutableStateOf("主线继续训练，等待结果")
    val taskChatMessages = mutableStateListOf(
        "用户：帮我继续做 CNN 图像分类实验。",
        "助手：我可以先给出执行计划，再在电脑端继续跑。"
    )

    var courseStage by mutableStateOf(CourseDemoStage.EMPTY)
    var importedMaterialName by mutableStateOf("《计算机网络》第 4 章 PDF + 课堂截图")
    var generatedNoteTitle by mutableStateOf("计算机网络 · 拥塞控制结构化笔记")
    var generatedCardCount by mutableStateOf(6)
    val courseQaMessages = mutableStateListOf(
        "课程内追问：为什么慢启动不等于指数增长到无限大？",
        "回答：因为达到 ssthresh 后会切到拥塞避免，并受拥塞窗口控制。"
    )

    var reviewStage by mutableStateOf(ReviewDemoStage.START)
    var wrongCardsToday by mutableStateOf(3)
    var mastery by mutableStateOf(0.58f)
    var nextRecommendation by mutableStateOf("今晚 20:30 复习 2 张 TCP 拥塞控制卡片")
    var profileSummary by mutableStateOf("画像判断：你在阈值变化题上不稳定，建议先用例题再回到定义。")
    var reviewReminderVisible by mutableStateOf(false)

    fun resetTaskDemo() {
        taskStage = CrossDeviceTaskStage.DRAFT
        taskPlanApproved = false
        desktopProgress = 0.18f
        taskDirection = "主线继续训练，等待结果"
        taskChatMessages.clear()
        taskChatMessages.addAll(
            listOf(
                "用户：帮我继续做 CNN 图像分类实验。",
                "助手：我可以先给出执行计划，再在电脑端继续跑。"
            )
        )
    }

    fun advanceTaskDemo() {
        when (taskStage) {
            CrossDeviceTaskStage.DRAFT -> taskStage = CrossDeviceTaskStage.PLAN_READY
            CrossDeviceTaskStage.PLAN_READY -> {
                taskPlanApproved = true
                taskStage = CrossDeviceTaskStage.APPROVED
            }
            CrossDeviceTaskStage.APPROVED -> {
                desktopProgress = 0.64f
                taskStage = CrossDeviceTaskStage.RUNNING_ON_DESKTOP
            }
            CrossDeviceTaskStage.RUNNING_ON_DESKTOP -> {
                taskChatMessages.add("助手：电脑端训练已继续进行，你现在可以在手机里追问下一步。")
                taskStage = CrossDeviceTaskStage.CHATTING_ON_PHONE
            }
            CrossDeviceTaskStage.CHATTING_ON_PHONE -> {
                taskDirection = "改为优先分析误差样本，并补一轮混淆矩阵说明"
                taskChatMessages.add("用户：先别只跑训练了，改成先分析误差样本。")
                taskChatMessages.add("助手：已调整方向，计划切到误差分析。")
                taskStage = CrossDeviceTaskStage.REDIRECTED
            }
            CrossDeviceTaskStage.REDIRECTED -> Unit
        }
    }

    fun resetCourseDemo() {
        courseStage = CourseDemoStage.EMPTY
        courseQaMessages.clear()
        courseQaMessages.addAll(
            listOf(
                "课程内追问：为什么慢启动不等于指数增长到无限大？",
                "回答：因为达到 ssthresh 后会切到拥塞避免，并受拥塞窗口控制。"
            )
        )
    }

    fun advanceCourseDemo() {
        when (courseStage) {
            CourseDemoStage.EMPTY -> courseStage = CourseDemoStage.IMPORTED
            CourseDemoStage.IMPORTED -> courseStage = CourseDemoStage.PROCESSING
            CourseDemoStage.PROCESSING -> courseStage = CourseDemoStage.GENERATED
            CourseDemoStage.GENERATED -> courseStage = CourseDemoStage.QA
            CourseDemoStage.QA -> Unit
        }
    }

    fun resetReviewDemo() {
        reviewStage = ReviewDemoStage.START
        wrongCardsToday = 3
        mastery = 0.58f
        nextRecommendation = "今晚 20:30 复习 2 张 TCP 拥塞控制卡片"
        profileSummary = "画像判断：你在阈值变化题上不稳定，建议先用例题再回到定义。"
        reviewReminderVisible = false
    }

    fun advanceReviewDemo() {
        when (reviewStage) {
            ReviewDemoStage.START -> {
                wrongCardsToday = 4
                mastery = 0.46f
                reviewStage = ReviewDemoStage.ANSWERED_WRONG
            }
            ReviewDemoStage.ANSWERED_WRONG -> {
                profileSummary = "画像更新：连续两次在拥塞窗口/阈值切换题出错，后续优先推场景辨析。"
                reviewStage = ReviewDemoStage.PROFILE_UPDATED
            }
            ReviewDemoStage.PROFILE_UPDATED -> {
                reviewReminderVisible = true
                nextRecommendation = "今晚 21:00 收到复习提醒，继续上次未完成的 2 张卡片"
                reviewStage = ReviewDemoStage.REMINDER_READY
            }
            ReviewDemoStage.REMINDER_READY -> {
                mastery = 0.63f
                wrongCardsToday = 2
                reviewStage = ReviewDemoStage.RESUMED
            }
            ReviewDemoStage.RESUMED -> Unit
        }
    }
}
