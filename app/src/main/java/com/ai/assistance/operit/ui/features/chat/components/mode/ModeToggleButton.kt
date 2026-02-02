package com.ai.assistance.operit.ui.features.chat.components.mode

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ai.assistance.operit.data.model.ConversationMode

/**
 * 模式切换按钮组件
 * 允许用户在 Chat 和 Agent 模式之间切换
 */
@Composable
fun ModeToggleButton(
    currentMode: ConversationMode,
    onModeChange: (ConversationMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val chatColor = MaterialTheme.colorScheme.primary
    val agentColor = MaterialTheme.colorScheme.tertiary
    
    val currentColor = if (currentMode == ConversationMode.CHAT) chatColor else agentColor
    
    Row(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
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
            selectedColor = chatColor
        )
        
        // Agent 模式按钮
        ModeButton(
            icon = Icons.Default.Rocket,
            label = "Agent",
            isSelected = currentMode == ConversationMode.AGENT,
            onClick = { onModeChange(ConversationMode.AGENT) },
            selectedColor = agentColor
        )
    }
}

@Composable
private fun ModeButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    selectedColor: Color
) {
    val backgroundColor by animateDpAsState(
        targetValue = if (isSelected) 1.dp else 0.dp,
        label = "bg_anim"
    )
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isSelected) selectedColor.copy(alpha = 0.2f) 
                else Color.Transparent
            )
            .clickable(enabled = !isSelected, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) selectedColor else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (isSelected) selectedColor else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
