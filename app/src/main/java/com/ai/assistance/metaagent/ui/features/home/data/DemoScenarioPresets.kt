package com.ai.assistance.metaagent.ui.features.home.data

enum class DemoScenarioDestination {
    HOME,
    REVIEW,
    COURSE,
    TASK,
    RESULT
}

data class DemoScenarioPreset(
    val id: String,
    val title: String,
    val subtitle: String,
    val destination: DemoScenarioDestination,
    val demoLine: String
)

object DemoScenarioPresets {
    val integratedFlow = listOf(
        DemoScenarioPreset(
            id = "scene_home",
            title = "首页总览",
            subtitle = "先让评委一眼看见今天该做什么、最近任务和课程入口。",
            destination = DemoScenarioDestination.HOME,
            demoLine = "我们不是堆功能，而是先把学习和任务组织成一个可理解的入口。"
        ),
        DemoScenarioPreset(
            id = "scene_course",
            title = "课程整理",
            subtitle = "导入资料、处理中、结构化笔记和课程内追问要连续出现。",
            destination = DemoScenarioDestination.COURSE,
            demoLine = "这不是泛问答，而是基于课程上下文的一次整理闭环。"
        ),
        DemoScenarioPreset(
            id = "scene_task",
            title = "跨端任务",
            subtitle = "手机发起、看计划、批准后交给电脑跑，中途还能继续聊。",
            destination = DemoScenarioDestination.TASK,
            demoLine = "手机不是遥控器，而是和电脑共做的主入口。"
        ),
        DemoScenarioPreset(
            id = "scene_result",
            title = "结果交付",
            subtitle = "结果不是一句完成了，而是能交付、能回看、能带回课程继续用。",
            destination = DemoScenarioDestination.RESULT,
            demoLine = "我们把任务结果重新沉淀回学习闭环，而不是一次性输出。"
        ),
        DemoScenarioPreset(
            id = "scene_review",
            title = "复习续上",
            subtitle = "答错、画像变化、晚些提醒、再次打开还能接着走。",
            destination = DemoScenarioDestination.REVIEW,
            demoLine = "AI 会记住你的学习状态，并在合适的时候主动回来找你。"
        )
    )
}
