package com.ai.assistance.operit.ui.features.chat.components.mode

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Rocket
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ai.assistance.operit.data.model.ConversationMode

/**
 * 模式切换按钮组件
 * 允许用户在 Chat 和 Agent 模式之间切换
 * 
 * Agent 模式使用更醒目的渐变色和边框，突出"元智能体"特性
 */
@Composable
fun ModeToggleButton(
    currentMode: ConversationMode,
    onModeChange: (ConversationMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val chatColor = MaterialTheme.colorScheme.primary
    // Agent 模式使用更醒目的橙色/紫色渐变风格
    val agentColor = Color(0xFFFF6B35) // 活力橙色
    val agentSecondaryColor = Color(0xFF9C27B0) // 紫色
    
    val containerColor by animateColorAsState(
        targetValue = if (currentMode == ConversationMode.AGENT) 
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
        else 
            MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "container_color"
    )
    
    val borderColor by animateColorAsState(
        targetValue = if (currentMode == ConversationMode.AGENT) 
            agentColor.copy(alpha = 0.5f)
        else 
            Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "border_color"
    )
    
    Row(
        modifier = modifier
            .shadow(
                elevation = if (currentMode == ConversationMode.AGENT) 4.dp else 0.dp,
                shape = RoundedCornerShape(24.dp)
            )
            .border(
                width = if (currentMode == ConversationMode.AGENT) 1.5.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(24.dp)
            )
            .background(
                color = containerColor,
                shape = RoundedCornerShape(24.dp)
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Chat 模式按钮
        ModeButton(
            icon = Icons.Default.Chat,
            label = "Chat",
            isSelected = currentMode == ConversationMode.CHAT,
            onClick = { onModeChange(ConversationMode.CHAT) },
            selectedColor = chatColor,
            isAgentStyle = false
        )
        
        // Agent 模式按钮 - 使用渐变效果
        ModeButton(
            icon = Icons.Default.Rocket,
            label = "Agent",
            isSelected = currentMode == ConversationMode.AGENT,
            onClick = { onModeChange(ConversationMode.AGENT) },
            selectedColor = agentColor,
            isAgentStyle = true
        )
    }
}

@Composable
private fun ModeButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    selectedColor: Color,
    isAgentStyle: Boolean = false
) {
    val animatedPadding by animateDpAsState(
        targetValue = if (isSelected) 2.dp else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "padding_anim"
    )
    
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected && isAgentStyle -> selectedColor.copy(alpha = 0.25f)
            isSelected -> selectedColor.copy(alpha = 0.2f)
            else -> Color.Transparent
        },
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "bg_color"
    )
    
    val iconTint by animateColorAsState(
        targetValue = if (isSelected) selectedColor else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "icon_tint"
    )
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .then(
                if (isSelected && isAgentStyle) {
                    Modifier.border(
                        width = 1.dp,
                        color = selectedColor.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(20.dp)
                    )
                } else Modifier
            )
            .clickable(enabled = !isSelected, onClick = onClick)
            .padding(horizontal = 16.dp + animatedPadding, vertical = 8.dp + animatedPadding / 2),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(if (isSelected) 22.dp else 20.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = iconTint,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
