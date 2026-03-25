package com.ai.assistance.metaagent.ui.features.home.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.ai.assistance.metaagent.ui.theme.FabColor
import com.ai.assistance.metaagent.ui.theme.FabIconColor
import com.ai.assistance.metaagent.ui.theme.SelectedNavItem


/**
 * 底部导航项数据
 */
data class BottomNavItem(
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val label: String
)

/**
 * MetaAgent 悬浮底部导航 — 主要功能入口
 *
 * 悬浮胶囊: 首页 | 课程 | 任务 | 更多(···)
 * 右侧 FAB: 新建对话
 */
val bottomNavItems = listOf(
    BottomNavItem("meta_home", Icons.Filled.Home, Icons.Outlined.Home, "首页"),
    BottomNavItem("course_space", Icons.AutoMirrored.Filled.MenuBook, Icons.AutoMirrored.Outlined.MenuBook, "课程"),
    BottomNavItem("task_center", Icons.Filled.TaskAlt, Icons.Outlined.TaskAlt, "任务"),
    BottomNavItem("more", Icons.Filled.MoreHoriz, Icons.Outlined.MoreHoriz, "更多")
)

/**
 * Google Chat 风格的悬浮胶囊导航栏 + FAB
 */
@Composable
fun ChatBottomBar(
    currentRoute: String,
    onNavItemClick: (String) -> Unit,
    onFabClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // 居中容器：胶囊 + FAB
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 底部导航胶囊
            Row(
                modifier = Modifier
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(28.dp)
                    )
                    .clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                bottomNavItems.forEach { item ->
                    val isSelected = currentRoute == item.route

                    val bgColor by animateColorAsState(
                        targetValue = if (isSelected) SelectedNavItem else MaterialTheme.colorScheme.surfaceContainerLowest,
                        label = "navBg"
                    )

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(bgColor)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                // 所有按钮统一走 onNavItemClick，'more' 由上层显示 MoreBottomSheet
                                onNavItemClick(item.route)
                            },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label,
                                modifier = Modifier.size(24.dp),
                                tint = if (isSelected)
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                }
            }

            // FAB
            FloatingActionButton(
                onClick = onFabClick,
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(16.dp),
                containerColor = FabColor,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 4.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "新建对话",
                    tint = FabIconColor,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
