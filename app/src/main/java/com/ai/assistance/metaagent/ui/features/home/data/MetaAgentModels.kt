package com.ai.assistance.metaagent.ui.features.home.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.ai.assistance.metaagent.data.model.ChatHistory
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * 对话项（MetaAgent 专用）
 */
data class MetaConversation(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val lastMessage: String,
    val timestamp: String,
    val isUnread: Boolean = false,
    val tag: String = "",
    val tagColor: Color = Color.Transparent,
    val hasGreenDot: Boolean = false
)

/**
 * 课程空间
 */
data class CourseItem(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val accentColor: Color,
    val materialCount: Int,
    val reviewCount: Int,
    val experimentCount: Int = 0,
    val weakPoints: List<String> = emptyList(),
    val progress: Float = 0f,
    val totalHours: String = "",
    val rating: Float = 0f,
    val category: String = ""
)

/**
 * 任务
 */
data class TaskItem(
    val id: String,
    val title: String,
    val status: TaskStatus,
    val statusDetail: String = "",
    val progress: Float = 0f,
    val isCrossDevice: Boolean = false,
    val startTime: String = "",
    val stepCount: Int = 0,
    val estimatedMinutes: Int = 0,
    val deliverables: String = ""
)

enum class TaskStatus(val label: String) {
    RUNNING("执行中"),
    AWAITING_APPROVAL("待审批"),
    COMPLETED("已完成"),
    PAUSED("已暂停"),
    FAILED("失败")
}

data class ReviewDrillItem(
    val id: String,
    val prompt: String,
    val course: String,
    val difficulty: String,
    val mastery: Float,
    val lastResult: String,
    val nextRecommendation: String
)

data class LearningProfileField(
    val title: String,
    val value: String,
    val evidence: String,
    val usableIn: String
)

data class ActivePushItem(
    val id: String,
    val title: String,
    val triggerReason: String,
    val targetWindow: String,
    val relatedState: String,
    val suggestedAction: String
)

// ══════════════════════════════════════════
// ChatHistory → MetaConversation 转换
// ══════════════════════════════════════════

/**
 * 可选的图标列表（用于图标选择器 Tooltip）
 */
data class IconOption(
    val name: String,
    val icon: ImageVector
)

val availableIcons: List<IconOption> = listOf(
    IconOption("对话", Icons.AutoMirrored.Filled.Chat),
    IconOption("学习", Icons.Default.School),
    IconOption("编辑", Icons.Default.Edit),
    IconOption("代码", Icons.Default.Code),
    IconOption("搜索", Icons.Default.Search),
    IconOption("收藏", Icons.Default.Star),
    IconOption("闪电", Icons.Default.FlashOn),
    IconOption("科学", Icons.Default.Science),
    IconOption("计算", Icons.Default.Calculate),
    IconOption("构建", Icons.Default.Build),
    IconOption("文档", Icons.Default.Description),
    IconOption("书本", Icons.Default.ImportContacts),
    IconOption("通知", Icons.Default.Notifications),
    IconOption("灯泡", Icons.Default.Lightbulb),
    IconOption("游戏", Icons.Default.SportsEsports),
    IconOption("音乐", Icons.Default.MusicNote),
    IconOption("相机", Icons.Default.CameraAlt),
    IconOption("面孔", Icons.Default.Face),
    IconOption("设置", Icons.Default.Settings),
    IconOption("工具", Icons.Default.Handyman),
    IconOption("日程", Icons.Default.CalendarToday),
    IconOption("图表", Icons.Default.BarChart),
    IconOption("地球", Icons.Default.Public),
    IconOption("心形", Icons.Default.Favorite),
    IconOption("任务", Icons.Default.TaskAlt),
    IconOption("终端", Icons.Default.Terminal),
    IconOption("画笔", Icons.Default.Brush),
    IconOption("翻译", Icons.Default.Translate),
    IconOption("机器人", Icons.Default.SmartToy),
    IconOption("安全", Icons.Default.Security),
)

/**
 * 根据 characterCardName 映射一个默认图标
 */
fun getIconForCharacterCard(characterCardName: String?): ImageVector {
    if (characterCardName == null) return Icons.AutoMirrored.Filled.Chat
    val lower = characterCardName.lowercase()
    return when {
        lower.contains("code") || lower.contains("编程") || lower.contains("dev") -> Icons.Default.Code
        lower.contains("学") || lower.contains("learn") || lower.contains("study") -> Icons.Default.School
        lower.contains("写") || lower.contains("write") || lower.contains("edit") -> Icons.Default.Edit
        lower.contains("搜") || lower.contains("search") -> Icons.Default.Search
        lower.contains("翻译") || lower.contains("translat") -> Icons.Default.Translate
        lower.contains("画") || lower.contains("art") || lower.contains("draw") -> Icons.Default.Brush
        lower.contains("音乐") || lower.contains("music") -> Icons.Default.MusicNote
        lower.contains("游戏") || lower.contains("game") -> Icons.Default.SportsEsports
        lower.contains("科学") || lower.contains("science") -> Icons.Default.Science
        else -> Icons.AutoMirrored.Filled.Chat
    }
}

/**
 * 将 LocalDateTime 格式化为"刚才"/"x分钟前"/"今天 HH:mm"/"昨天"/"MM-dd"
 */
fun formatRelativeTime(dateTime: LocalDateTime): String {
    val now = LocalDateTime.now()
    val minutesDiff = ChronoUnit.MINUTES.between(dateTime, now)
    val hoursDiff = ChronoUnit.HOURS.between(dateTime, now)
    val daysDiff = ChronoUnit.DAYS.between(dateTime.toLocalDate(), now.toLocalDate())

    return when {
        minutesDiff < 1 -> "刚才"
        minutesDiff < 60 -> "${minutesDiff}分钟前"
        hoursDiff < 24 && daysDiff == 0L -> "今天 ${String.format("%02d:%02d", dateTime.hour, dateTime.minute)}"
        daysDiff == 1L -> "昨天"
        daysDiff < 7 -> "${daysDiff}天前"
        else -> "${String.format("%02d", dateTime.monthValue)}-${String.format("%02d", dateTime.dayOfMonth)}"
    }
}

/**
 * 将 ChatHistory 转换为首页列表使用的 MetaConversation
 *
 * @param lastMessagePreview 最后一条消息的预览文本（需从外部传入，因为
 *   chatHistoriesFlow 中的 ChatHistory.messages 为空列表以优化性能）
 */
fun ChatHistory.toMetaConversation(
    lastMessagePreview: String = ""
): MetaConversation {
    return MetaConversation(
        id = this.id,
        title = this.title,
        icon = getIconForCharacterCard(this.characterCardName),
        lastMessage = lastMessagePreview.ifEmpty { "暂无消息" },
        timestamp = formatRelativeTime(this.updatedAt),
        isUnread = false, // TODO: 后续可扩展未读状态
        hasGreenDot = false
    )
}


/**
 * 模拟数据 — 保留用于开发/测试
 */
object MetaSampleData {

    val conversations = listOf(
        MetaConversation(
            "cv1", "MetaAgent 学伴", Icons.Default.Face,
            "CNN 实验报告已生成 ✅ 要看看吗？", "刚才",
            tag = "任务完成", tagColor = Color(0xFF448AFF), hasGreenDot = true
        ),
        MetaConversation(
            "cv2", "软件体系结构", Icons.Default.ImportContacts,
            "观察者模式的笔记已整理好", "2小时前"
        ),
        MetaConversation(
            "cv3", "复习提醒", Icons.Default.Notifications,
            "TCP 拥塞控制待复习，要花3分钟过一下吗？", "今天 09:30",
            isUnread = true
        ),
        MetaConversation(
            "cv4", "CNN 实验 Copilot", Icons.Default.Edit,
            "accuracy 93.7%，比上次提升6个点！🎉", "昨天"
        )
    )

    val courses = listOf(
        CourseItem(
            id = "c1", name = "软件体系结构", icon = Icons.Default.Build,
            accentColor = Color(0xFF7C4DFF),
            materialCount = 12, reviewCount = 5,
            weakPoints = listOf("观察者模式"),
            progress = 0.45f, totalHours = "28 小时", rating = 4.9f,
            category = "软件工程"
        ),
        CourseItem(
            id = "c2", name = "计算机网络", icon = Icons.Default.Public,
            accentColor = Color(0xFF448AFF),
            materialCount = 8, reviewCount = 3,
            weakPoints = listOf("TCP 拥塞控制"),
            progress = 0.32f, totalHours = "24 小时", rating = 4.7f,
            category = "计算机基础"
        ),
        CourseItem(
            id = "c3", name = "深度学习实践", icon = Icons.Default.Info,
            accentColor = Color(0xFF00C853),
            materialCount = 15, reviewCount = 2,
            experimentCount = 3,
            progress = 0.68f, totalHours = "32 小时", rating = 4.8f,
            category = "人工智能"
        )
    )

    val tasks = listOf(
        TaskItem(
            "t1", "CNN 图像分类实验", TaskStatus.RUNNING,
            "Epoch 15/20", 0.75f, true, "30 分钟前"
        ),
        TaskItem(
            "t2", "计算机网络课报告", TaskStatus.AWAITING_APPROVAL,
            "编排树已生成", 0f, false, "", 6, 20
        ),
        TaskItem(
            "t3", "观察者模式笔记整理", TaskStatus.COMPLETED,
            "用时 8 分钟", 1f, false, "",
            deliverables = "结构化笔记 + 3 张知识卡片"
        ),
        TaskItem(
            "t4", "策略模式 vs 观察者模式对比", TaskStatus.COMPLETED,
            "昨天", 1f
        )
    )

    val reviewDrills = listOf(
        ReviewDrillItem(
            id = "r1",
            prompt = "TCP 拥塞控制的慢启动和拥塞避免分别在什么条件下切换？",
            course = "计算机网络",
            difficulty = "中等",
            mastery = 0.46f,
            lastResult = "上次答错，混淆了阈值变化",
            nextRecommendation = "今晚 20:30 再推 1 题概念辨析"
        ),
        ReviewDrillItem(
            id = "r2",
            prompt = "观察者模式和发布订阅模式的核心差异是什么？",
            course = "软件体系结构",
            difficulty = "基础",
            mastery = 0.72f,
            lastResult = "最近两次答对，但解释不够完整",
            nextRecommendation = "明天上午推荐 1 题场景判断"
        ),
        ReviewDrillItem(
            id = "r3",
            prompt = "CNN 中卷积核共享参数为什么能减少过拟合风险？",
            course = "深度学习实践",
            difficulty = "提升",
            mastery = 0.58f,
            lastResult = "能说出结论，但不会联系实验现象",
            nextRecommendation = "实验完成后立即追问 1 题应用题"
        )
    )

    val learningProfile = listOf(
        LearningProfileField(
            title = "当前薄弱点",
            value = "TCP 拥塞控制阈值变化、模式区分题",
            evidence = "来自最近 5 次复习题中 3 次错误记录",
            usableIn = "用于复习推荐、问答时优先补背景解释"
        ),
        LearningProfileField(
            title = "偏好学习方式",
            value = "先看例子，再记抽象定义",
            evidence = "观察者模式对比例题正确率高于纯定义题",
            usableIn = "用于生成讲解顺序、题目展示方式"
        ),
        LearningProfileField(
            title = "任务推进风格",
            value = "倾向短链路可交付，接受 15-25 分钟的小任务",
            evidence = "任务中心近 7 天完成任务平均时长 18 分钟",
            usableIn = "用于主动推送的节奏与任务拆分"
        ),
        LearningProfileField(
            title = "课程状态",
            value = "软件体系结构稳定推进，计算机网络需要补复习闭环",
            evidence = "课程进度 + 复习命中率 + 任务完成率组合判断",
            usableIn = "用于课程摘要、下一步建议"
        )
    )

    val activePushes = listOf(
        ActivePushItem(
            id = "p1",
            title = "3 分钟复习提醒",
            triggerReason = "你在计算机网络的薄弱点连续两天未复习",
            targetWindow = "今晚 20:30 - 21:00",
            relatedState = "复习状态：待回顾 4 题，掌握度低于 0.5",
            suggestedAction = "直接进入 1 题快练，答后更新掌握度"
        ),
        ActivePushItem(
            id = "p2",
            title = "任务接力提醒",
            triggerReason = "CNN 实验已运行到 75%，适合补一轮误差分析",
            targetWindow = "实验结束后 10 分钟内",
            relatedState = "任务状态：进行中；课程状态：深度学习实践正在推进",
            suggestedAction = "推送实验复盘卡片和 2 个易错点"
        ),
        ActivePushItem(
            id = "p3",
            title = "课程收口提醒",
            triggerReason = "软件体系结构本周材料学完，但复习覆盖不足",
            targetWindow = "周日 16:00",
            relatedState = "课程状态：进度 45%，复习完成率仅 38%",
            suggestedAction = "推送本周总结 + 观察者模式专项复习"
        )
    )
}
