package com.ai.assistance.operit.ui.main.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import com.ai.assistance.operit.R
import com.ai.assistance.operit.ui.theme.*

/**
 * 底部导航项数据类
 */
data class BottomNavItem(
    val route: String,
    val labelResId: Int,
    val icon: ImageVector
)

/**
 * 底部导航栏配置
 */
object BottomNavConfig {
    val items = listOf(
        BottomNavItem("home", R.string.nav_home, Icons.Default.Home),
        BottomNavItem("ai_chat", R.string.nav_ai_chat, Icons.Default.SmartToy),
        BottomNavItem("files", R.string.nav_files, Icons.Default.Folder),
        BottomNavItem("skills", R.string.nav_skills, Icons.Default.Star)
    )
}

/**
 * Material 3 底部导航栏
 */
@Composable
fun BottomNavBar(
    currentRoute: String,
    onItemSelected: (String) -> Unit
) {
    NavigationBar(
        containerColor = Color.Transparent, // 透明背景，让 Scaffold 的 BottomBar 容器颜色生效
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        tonalElevation = 0.dp // 移除默认阴影
    ) {
        BottomNavConfig.items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                icon = { 
                    Icon(
                        item.icon, 
                        contentDescription = stringResource(item.labelResId),
                        modifier = Modifier.size(24.dp)
                    ) 
                },
                label = { 
                    Text(
                        stringResource(item.labelResId),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    ) 
                },
                selected = selected,
                onClick = { onItemSelected(item.route) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
