package com.ai.assistance.metaagent.ui.features.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.QuestionAnswer
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * "更多"上拉底部弹窗 — 与 UITemplate MoreBottomSheet 完全一致
 *
 * 学习工具: 学习画像 | 闪卡复习 | 编排树
 * 系统功能: Copilot 对话 | 异步双线程 | 设备配对 | 设置
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreBottomSheet(
    onDismiss: () -> Unit,
    onNavigate: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        ) {
            // ── 学习工具 ──
            SectionHeader("学习工具")

            MoreSheetItem(
                icon = Icons.Outlined.AccountCircle,
                label = "学习画像",
                subtitle = "查看学习统计与掌握度",
                onClick = { onNavigate("learning_profile"); onDismiss() }
            )
            MoreSheetItem(
                icon = Icons.Outlined.Style,
                label = "闪卡复习",
                subtitle = "碎片化知识点快速复习",
                onClick = { onNavigate("flashcard_review"); onDismiss() }
            )
            MoreSheetItem(
                icon = Icons.Outlined.Timeline,
                label = "编排树",
                subtitle = "查看任务执行流程与状态",
                onClick = { onNavigate("plan_tree"); onDismiss() }
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // ── 系统功能 ──
            SectionHeader("系统功能")

            MoreSheetItem(
                icon = Icons.Outlined.Psychology,
                label = "Copilot 对话",
                subtitle = "AI 协作式问答与任务执行",
                onClick = { onNavigate("copilot_chat"); onDismiss() }
            )
            MoreSheetItem(
                icon = Icons.Outlined.QuestionAnswer,
                label = "异步双线程",
                subtitle = "后台任务实时进度 + 对话",
                onClick = { onNavigate("async_dual_thread"); onDismiss() }
            )
            MoreSheetItem(
                icon = Icons.Outlined.Devices,
                label = "设备配对",
                subtitle = "连接 PC 执行端",
                onClick = { onNavigate("device_pairing"); onDismiss() }
            )
            MoreSheetItem(
                icon = Icons.Outlined.Settings,
                label = "设置",
                subtitle = "主题、通知、AI 模型偏好",
                onClick = { onNavigate("settings"); onDismiss() }
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
    )
}

@Composable
private fun MoreSheetItem(
    icon: ImageVector,
    label: String,
    subtitle: String = "",
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(20.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
