package com.ai.assistance.metaagent.ui.main.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.ArrowDropUp
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.DoNotDisturb
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ai.assistance.metaagent.R
import com.ai.assistance.metaagent.ui.common.NavItem
import com.ai.assistance.metaagent.ui.main.NavGroup
import com.ai.assistance.metaagent.ui.main.screens.MetaAgentRouter
import com.ai.assistance.metaagent.ui.main.screens.Screen
import com.ai.assistance.metaagent.ui.theme.DrawerBackground
import com.ai.assistance.metaagent.ui.theme.GoogleBlue
import com.ai.assistance.metaagent.ui.theme.GoogleRed
import com.ai.assistance.metaagent.ui.theme.GoogleYellow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * 侧边抽屉 — ChatDrawer 统一风格
 *
 * 所有行使用相同的 Row(Icon 24dp + Spacer 16dp + Text bodyLarge) 样式，
 * 组之间用 HorizontalDivider 分隔，无分组标题。
 */
@Composable
fun DrawerContent(
        navGroups: List<NavGroup>,
        currentScreen: Screen,
        selectedItem: NavItem,
        isNetworkAvailable: Boolean,
        networkType: String,
        scope: CoroutineScope,
        drawerState: androidx.compose.material3.DrawerState,
        onScreenSelected: (Screen, NavItem) -> Unit
) {
        var statusExpanded by remember { mutableStateOf(false) }
        var currentStatus by remember { mutableStateOf("在线") }

        Column(
                modifier = Modifier
                        .fillMaxHeight()
                        .width(300.dp)
                        .background(DrawerBackground)
                        .verticalScroll(rememberScrollState())
                        .padding(
                                bottom = WindowInsets.navigationBars
                                        .asPaddingValues()
                                        .calculateBottomPadding()
                        )
        ) {
                // ── Meta Agent 品牌标题 ──
                Spacer(modifier = Modifier.height(48.dp))
                Row(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        Text(
                                text = buildAnnotatedString {
                                        withStyle(SpanStyle(color = GoogleBlue, fontWeight = FontWeight.Bold)) { append("M") }
                                        withStyle(SpanStyle(color = GoogleRed, fontWeight = FontWeight.Bold)) { append("e") }
                                        withStyle(SpanStyle(color = GoogleYellow, fontWeight = FontWeight.Bold)) { append("t") }
                                        withStyle(SpanStyle(color = GoogleBlue, fontWeight = FontWeight.Bold)) { append("a") }
                                },
                                fontSize = 22.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                                text = "Agent",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface
                        )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ── 状态行 — 可展开 ──
                Row(
                        modifier = Modifier
                                .fillMaxWidth()
                                .clickable { statusExpanded = !statusExpanded }
                                .padding(horizontal = 24.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        Icon(
                                imageVector = Icons.Outlined.Circle,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = when (currentStatus) {
                                        "在线" -> Color(0xFF34A853)
                                        "离开" -> Color(0xFFFBBC05)
                                        "勿扰" -> Color(0xFFEA4335)
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                                text = currentStatus,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                        )
                        Icon(
                                imageVector = if (statusExpanded) Icons.Outlined.ArrowDropUp else Icons.Outlined.ArrowDropDown,
                                contentDescription = "切换状态",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                }

                // 状态下拉列表
                AnimatedVisibility(
                        visible = statusExpanded,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(start = 40.dp)) {
                                StatusOption("在线", Icons.Outlined.Circle, Color(0xFF34A853), currentStatus == "在线") {
                                        currentStatus = "在线"; statusExpanded = false
                                }
                                StatusOption("离开", Icons.Outlined.Circle, Color(0xFFFBBC05), currentStatus == "离开") {
                                        currentStatus = "离开"; statusExpanded = false
                                }
                                StatusOption("勿扰", Icons.Outlined.DoNotDisturb, Color(0xFFEA4335), currentStatus == "勿扰") {
                                        currentStatus = "勿扰"; statusExpanded = false
                                }
                                StatusOption("离线", Icons.Outlined.Circle, MaterialTheme.colorScheme.onSurfaceVariant, currentStatus == "离线") {
                                        currentStatus = "离线"; statusExpanded = false
                                }
                        }
                }

                // 添加状态
                DrawerRow(icon = Icons.Outlined.Edit, label = "添加状态", onClick = { })

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(8.dp))

                // ── 导航项 — 按组渲染，组之间用分割线分隔 ──
                navGroups.forEachIndexed { groupIndex, group ->
                        group.items.forEach { item ->
                                DrawerRow(
                                        icon = item.icon,
                                        label = stringResource(id = item.titleResId),
                                        onClick = {
                                                onScreenSelected(
                                                        MetaAgentRouter.getScreenForNavItem(item),
                                                        item
                                                )
                                                scope.launch { drawerState.close() }
                                        }
                                )
                        }
                        // 组之间加分割线（最后一组不加）
                        if (groupIndex < navGroups.size - 1) {
                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                                Spacer(modifier = Modifier.height(8.dp))
                        }
                }

                Spacer(modifier = Modifier.height(16.dp))
        }
}

/** 抽屉统一行样式 — 和 ChatDrawer 一致 */
@Composable
private fun DrawerRow(
        icon: ImageVector,
        label: String,
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
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge
                )
        }
}

/** Content for the collapsed navigation drawer (for tablet mode) */
@Composable
fun CollapsedDrawerContent(
        navItems: List<NavItem>,
        selectedItem: NavItem,
        isNetworkAvailable: Boolean,
        onScreenSelected: (Screen, NavItem) -> Unit
) {
        Column(
                modifier = Modifier
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
                Spacer(modifier = Modifier.height(8.dp))
                IconButton(onClick = { }) {
                        Icon(
                                imageVector = if (isNetworkAvailable) Icons.Default.Wifi else Icons.Default.WifiOff,
                                contentDescription = stringResource(id = R.string.network_status_label),
                                tint = if (isNetworkAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                        )
                }
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(modifier = Modifier.fillMaxWidth(0.6f))
                Spacer(modifier = Modifier.height(16.dp))

                for (item in navItems) {
                        IconButton(
                                onClick = { onScreenSelected(MetaAgentRouter.getScreenForNavItem(item), item) },
                                modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                                Icon(
                                        imageVector = item.icon,
                                        contentDescription = stringResource(id = item.titleResId),
                                        tint = if (selectedItem == item) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(24.dp)
                                )
                        }
                }
                Spacer(modifier = Modifier.height(16.dp))
        }
}

@Composable
private fun StatusOption(
        label: String,
        icon: ImageVector,
        iconTint: Color,
        isSelected: Boolean,
        onClick: () -> Unit
) {
        Row(
                modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onClick)
                        .background(
                                if (isSelected) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                                else Color.Transparent
                        )
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
                Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(20.dp),
                        tint = iconTint
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface
                )
        }
}
