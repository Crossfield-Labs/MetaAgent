package com.ai.assistance.metaagent.ui.theme

import androidx.compose.ui.graphics.Color

// ══════════════════════════════════════════
// Material 3 莫奈蓝色系 — 种子色 #BAE3F9
// 基于 Hue ≈ 200° 生成的完整 Tonal Palette
// ══════════════════════════════════════════

// Primary (蓝)
val BluePrimary = Color(0xFF006590)         // tone40 — 主色
val BlueOnPrimary = Color(0xFFFFFFFF)       // 主色上文字
val BluePrimaryContainer = Color(0xFFC5E7FF) // tone90 — 主色容器（接近 #BAE3F9）
val BlueOnPrimaryContainer = Color(0xFF001E2E) // tone10

// Secondary (蓝灰)
val BlueSecondary = Color(0xFF4E616D)       // tone40
val BlueOnSecondary = Color(0xFFFFFFFF)
val BlueSecondaryContainer = Color(0xFFD1E5F4) // tone90
val BlueOnSecondaryContainer = Color(0xFF0A1E28)

// Tertiary (淡紫蓝)
val BlueTertiary = Color(0xFF5D5B7D)        // tone40
val BlueOnTertiary = Color(0xFFFFFFFF)
val BlueTertiaryContainer = Color(0xFFE3DFFF) // tone90
val BlueOnTertiaryContainer = Color(0xFF1A1836)

// Error
val BlueError = Color(0xFFBA1A1A)
val BlueOnError = Color(0xFFFFFFFF)
val BlueErrorContainer = Color(0xFFFFDAD6)
val BlueOnErrorContainer = Color(0xFF410002)

// Background & Surface (亮色)
val BlueBackground = Color(0xFFF6FAFE)       // 极淡蓝白
val BlueOnBackground = Color(0xFF181C1F)
val BlueSurface = Color(0xFFF6FAFE)
val BlueOnSurface = Color(0xFF181C1F)
val BlueSurfaceVariant = Color(0xFFDCE3E9)
val BlueOnSurfaceVariant = Color(0xFF41484D)
val BlueOutline = Color(0xFF71787E)
val BlueOutlineVariant = Color(0xFFC0C7CD)
val BlueSurfaceContainerLowest = Color(0xFFFFFFFF)
val BlueSurfaceContainerLow = Color(0xFFF0F4F8)
val BlueSurfaceContainer = Color(0xFFEAEEF3)
val BlueSurfaceContainerHigh = Color(0xFFE4E9ED)
val BlueSurfaceContainerHighest = Color(0xFFDFE3E7)
val BlueInverseSurface = Color(0xFF2D3134)
val BlueInverseOnSurface = Color(0xFFEDF1F5)
val BlueInversePrimary = Color(0xFF82CFFF)

// Dark 暗色
val BluePrimaryDark = Color(0xFF82CFFF)       // tone80
val BlueOnPrimaryDark = Color(0xFF00344D)
val BluePrimaryContainerDark = Color(0xFF004C6E)
val BlueOnPrimaryContainerDark = Color(0xFFC5E7FF)
val BlueSecondaryDark = Color(0xFFB5C9D8)
val BlueOnSecondaryDark = Color(0xFF20333E)
val BlueSecondaryContainerDark = Color(0xFF364955)
val BlueOnSecondaryContainerDark = Color(0xFFD1E5F4)
val BlueTertiaryDark = Color(0xFFC7C2EA)
val BlueOnTertiaryDark = Color(0xFF2F2D4D)
val BlueTertiaryContainerDark = Color(0xFF464364)
val BlueOnTertiaryContainerDark = Color(0xFFE3DFFF)
val BlueBackgroundDark = Color(0xFF101417)
val BlueOnBackgroundDark = Color(0xFFDFE3E7)
val BlueSurfaceDark = Color(0xFF101417)
val BlueOnSurfaceDark = Color(0xFFDFE3E7)
val BlueSurfaceVariantDark = Color(0xFF41484D)
val BlueOnSurfaceVariantDark = Color(0xFFC0C7CD)

// ══════════════════════════════════════════
// 应用级自定义色（适配蓝色系）
// ══════════════════════════════════════════

// 搜索/导航
val SearchBarBackground = Color(0xFFE3EEF6)       // 搜索栏背景 - 淡蓝灰
val BottomBarBackground = Color(0xFFF0F6FA)       // 底部导航栏
val BottomBarPillBackground = Color(0xFFFFFFFF)   // 胶囊白色
val FabColor = Color(0xFFB4D5F0)                  // FAB 蓝色
val FabIconColor = Color(0xFF1A4A6E)              // FAB 图标深蓝
val SelectedNavItem = Color(0xFFC5E7FF)           // 选中导航项
val DrawerBackground = Color(0xFFF0F6FA)          // 抽屉背景

// 通用
val UnreadDot = Color(0xFF41484D)
val AppBadgeBackground = Color(0xFFC5E7FF)
val AppBadgeText = Color(0xFF004C6E)
val SectionTitle = Color(0xFF181C1F)

// Google 品牌色
val GoogleBlue = Color(0xFF4285F4)
val GoogleRed = Color(0xFFEA4335)
val GoogleYellow = Color(0xFFFBBC05)
val GoogleGreen = Color(0xFF34A853)

// 聊天气泡
val ChatBubbleSent = Color(0xFFC5E7FF)            // 发送气泡 — 主色容器
val ChatBubbleReceived = Color(0xFFF0F4F8)        // 接收气泡 — surface low
val CopilotBubble = Color(0xFFE3DFFF)             // AI 气泡 — tertiary 容器
val SystemCardBg = Color(0xFFE3EEF6)              // 系统提示卡片

// ===== MetaAgent 专用色 =====

// 课程空间
val CoursePurple = Color(0xFF7C4DFF)
val CourseBlue = Color(0xFF448AFF)
val CourseGreen = Color(0xFF00C853)
val CourseTeal = Color(0xFF00BFA5)

// 任务状态
val TaskRunning = Color(0xFF2979FF)
val TaskPending = Color(0xFFBDBDBD)
val TaskCompleted = Color(0xFF00C853)
val TaskApproval = Color(0xFFFF9100)
val TaskFailed = Color(0xFFFF1744)

// 掌握度
val MasteryMastered = Color(0xFF00C853)
val MasteryFuzzy = Color(0xFFFFC107)
val MasteryWeak = Color(0xFFFF5252)

// 编排树
val PlanNodeDone = Color(0xFF00C853)
val PlanNodeRunning = Color(0xFF2979FF)
val PlanNodePending = Color(0xFFE0E0E0)
val PlanNodeBlocked = Color(0xFFFF9100)

// 异步双线程
val AsyncBannerStart = Color(0xFF006590)
val AsyncBannerEnd = Color(0xFF448AFF)
val EventCardBg = Color(0xFFE3EEF6)

// 设备配对
val ConnectionOnline = Color(0xFF00C853)
val ConnectionOffline = Color(0xFFBDBDBD)

// 学习画像
val ProfileStreakFire = Color(0xFFFF6D00)
val ProfileChipBg = Color(0xFFC5E7FF)
