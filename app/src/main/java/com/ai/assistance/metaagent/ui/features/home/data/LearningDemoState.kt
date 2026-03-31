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

data class ReviewChoice(
    val id: String,
    val text: String
)

data class ReviewQuestion(
    val id: String,
    val deckTitle: String,
    val prompt: String,
    val stemHint: String,
    val choices: List<ReviewChoice>,
    val correctChoiceId: String,
    val explanation: String,
    val correctRecommendation: String,
    val wrongRecommendation: String,
    val correctInsight: String,
    val wrongInsight: String,
    val correctMastery: Float,
    val wrongMastery: Float
)

data class ReviewEvent(
    val title: String,
    val detail: String
)

object LearningDemoState {
    var taskStage by mutableStateOf(CrossDeviceTaskStage.DRAFT)
    var taskGoal by mutableStateOf("帮我整理 CNN 实验，并在电脑端继续跑训练")
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

    private val reviewQuestions = listOf(
        ReviewQuestion(
            id = "tcp_threshold_switch",
            deckTitle = "TCP 拥塞控制",
            prompt = "当 cwnd 先指数增长，达到阈值后改成线性增长，这里真正发生了什么切换？",
            stemHint = "本题属于概念辨析，先判断“增长方式变化”对应的是哪一段逻辑。",
            choices = listOf(
                ReviewChoice("a", "慢启动切到拥塞避免"),
                ReviewChoice("b", "快速重传切到快速恢复"),
                ReviewChoice("c", "流量控制切到拥塞控制"),
                ReviewChoice("d", "接收窗口切到发送窗口")
            ),
            correctChoiceId = "a",
            explanation = "指数增长到阈值后转为线性增长，是慢启动进入拥塞避免的典型表现，不是快速恢复。",
            correctRecommendation = "明天 07:40 再推 1 题场景判断，确认你对阈值切换已经稳定。",
            wrongRecommendation = "今晚 20:30 再推 1 题同类辨析，先把阈值切换这类题补牢。",
            correctInsight = "概念题回答稳定，下一题可以切到场景判断，不需要继续停留在定义本身。",
            wrongInsight = "你更容易把阈值切换和快速恢复混在一起，下一题改成更短、更聚焦的辨析题。",
            correctMastery = 0.64f,
            wrongMastery = 0.46f
        ),
        ReviewQuestion(
            id = "tcp_loss_scene",
            deckTitle = "TCP 拥塞控制",
            prompt = "收到 3 个重复 ACK 后，最该优先联想到的处理路径是什么？",
            stemHint = "这是上一题的跟进题，考的是场景触发，不是定义背诵。",
            choices = listOf(
                ReviewChoice("a", "直接回到慢启动，窗口清零"),
                ReviewChoice("b", "进入快速重传，并可能衔接快速恢复"),
                ReviewChoice("c", "只增大发送窗口，保持原策略"),
                ReviewChoice("d", "暂停发送直到应用层确认")
            ),
            correctChoiceId = "b",
            explanation = "3 个重复 ACK 更像丢包但链路仍在前进，所以优先联想到快速重传，再决定是否进入快速恢复。",
            correctRecommendation = "明天 08:10 推荐 1 题应用题，把概念和场景再串一次。",
            wrongRecommendation = "今晚 21:10 加 1 题场景判断，先把重复 ACK 和超时的触发差异分开。",
            correctInsight = "你已经能把概念迁移到场景里，下一次可以往应用题推进，不必继续重复基础辨析。",
            wrongInsight = "场景触发上还有混淆，系统会先补一题重复 ACK / 超时对比，再回到应用题。",
            correctMastery = 0.72f,
            wrongMastery = 0.55f
        )
    )

    var reviewStage by mutableStateOf(ReviewDemoStage.START)
    var wrongCardsToday by mutableStateOf(3)
    var mastery by mutableStateOf(0.58f)
    var nextRecommendation by mutableStateOf("今晚 20:30 复习 2 张 TCP 拥塞控制卡片")
    var profileSummary by mutableStateOf("先过一题概念辨析，再决定是继续强化还是稍后续上。")
    var reviewReminderVisible by mutableStateOf(false)
    var reviewQuestionIndex by mutableStateOf(0)
    var reviewSelectedChoiceId by mutableStateOf<String?>(null)
    var reviewAnswered by mutableStateOf(false)
    var reviewLastAnswerCorrect by mutableStateOf<Boolean?>(null)
    var reviewLastMasteryDelta by mutableStateOf(0f)
    var reviewSessionCompleted by mutableStateOf(false)
    var reviewResumeLabel by mutableStateOf("今晚 20:30 再看 1 题场景判断")
    val reviewEvents = mutableStateListOf(
        ReviewEvent(
            title = "已载入今日复习",
            detail = "先过 1 题概念辨析，再决定是立刻继续还是稍后续上。"
        )
    )

    val currentReviewQuestion: ReviewQuestion
        get() = reviewQuestions[reviewQuestionIndex.coerceIn(0, reviewQuestions.lastIndex)]

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
        profileSummary = "先过一题概念辨析，再决定是继续强化还是稍后续上。"
        reviewReminderVisible = false
        reviewQuestionIndex = 0
        reviewSelectedChoiceId = null
        reviewAnswered = false
        reviewLastAnswerCorrect = null
        reviewLastMasteryDelta = 0f
        reviewSessionCompleted = false
        reviewResumeLabel = "今晚 20:30 再看 1 题场景判断"
        reviewEvents.clear()
        reviewEvents.add(
            ReviewEvent(
                title = "已载入今日复习",
                detail = "先过 1 题概念辨析，再决定是立刻继续还是稍后续上。"
            )
        )
    }

    fun selectReviewChoice(choiceId: String) {
        if (reviewAnswered || reviewSessionCompleted) return
        reviewSelectedChoiceId = choiceId
    }

    fun submitReviewAnswer(): Boolean {
        val selectedChoiceId = reviewSelectedChoiceId ?: return false
        if (reviewAnswered || reviewSessionCompleted) return false

        val question = currentReviewQuestion
        val answeredCorrectly = selectedChoiceId == question.correctChoiceId
        val previousMastery = mastery

        reviewAnswered = true
        reviewLastAnswerCorrect = answeredCorrectly
        mastery = if (answeredCorrectly) question.correctMastery else question.wrongMastery
        reviewLastMasteryDelta = mastery - previousMastery
        wrongCardsToday = if (answeredCorrectly) {
            (wrongCardsToday - 1).coerceAtLeast(0)
        } else {
            wrongCardsToday + 1
        }
        nextRecommendation = if (answeredCorrectly) question.correctRecommendation else question.wrongRecommendation
        profileSummary = if (answeredCorrectly) question.correctInsight else question.wrongInsight

        reviewEvents.add(
            0,
            ReviewEvent(
                title = if (answeredCorrectly) "本题已答对" else "本题答错了",
                detail = "掌握度 ${if (reviewLastMasteryDelta >= 0f) "+" else ""}${(reviewLastMasteryDelta * 100).toInt()}%，下一次推荐已更新。"
            )
        )

        if (reviewStage == ReviewDemoStage.RESUMED || reviewQuestionIndex == reviewQuestions.lastIndex) {
            reviewStage = ReviewDemoStage.RESUMED
            reviewSessionCompleted = true
            reviewReminderVisible = false
            reviewEvents.add(
                0,
                ReviewEvent(
                    title = "本轮已完成",
                    detail = "可以按新的推荐时间继续下一轮，也可以立刻再来一题。"
                )
            )
        } else {
            reviewStage = ReviewDemoStage.ANSWERED_WRONG
        }
        return true
    }

    fun continueReviewWithFollowUp() {
        if (reviewQuestionIndex >= reviewQuestions.lastIndex) {
            reviewStage = ReviewDemoStage.RESUMED
            reviewSessionCompleted = true
            return
        }
        reviewQuestionIndex = reviewQuestions.lastIndex
        reviewSelectedChoiceId = null
        reviewAnswered = false
        reviewLastAnswerCorrect = null
        reviewReminderVisible = false
        reviewStage = ReviewDemoStage.PROFILE_UPDATED
        reviewEvents.add(
            0,
            ReviewEvent(
                title = "已切到强化题",
                detail = "下一题会更贴近刚才的薄弱点，但页面不会中断。"
            )
        )
    }

    fun scheduleReviewReminder() {
        reviewReminderVisible = true
        reviewStage = ReviewDemoStage.REMINDER_READY
        reviewResumeLabel = "今晚 21:10 从提醒继续这组题"
        nextRecommendation = "今晚 21:10 回来继续 1 题场景判断"
        reviewEvents.add(
            0,
            ReviewEvent(
                title = "已安排稍后继续",
                detail = "本轮会保留到下一次提醒，回来时直接续上。"
            )
        )
    }

    fun resumeReviewFromReminder() {
        reviewReminderVisible = false
        reviewStage = ReviewDemoStage.RESUMED
        reviewQuestionIndex = reviewQuestions.lastIndex
        reviewSelectedChoiceId = null
        reviewAnswered = false
        reviewLastAnswerCorrect = null
        reviewSessionCompleted = false
        reviewEvents.add(
            0,
            ReviewEvent(
                title = "已从提醒续上",
                detail = "直接回到上一轮未完成的强化题，不需要重新开始。"
            )
        )
    }

    fun advanceReviewDemo() {
        when (reviewStage) {
            ReviewDemoStage.START -> submitReviewAnswer()
            ReviewDemoStage.ANSWERED_WRONG -> continueReviewWithFollowUp()
            ReviewDemoStage.PROFILE_UPDATED -> scheduleReviewReminder()
            ReviewDemoStage.REMINDER_READY -> resumeReviewFromReminder()
            ReviewDemoStage.RESUMED -> Unit
        }
    }
}
