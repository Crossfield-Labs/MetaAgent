package com.ai.assistance.metaagent.ui.features.home.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.ImportContacts
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

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

/**
 * 模拟数据 — 后续接入真实数据源
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
}
