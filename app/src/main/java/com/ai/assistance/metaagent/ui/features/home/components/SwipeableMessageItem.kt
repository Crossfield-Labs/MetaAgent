package com.ai.assistance.metaagent.ui.features.home.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.MarkEmailUnread
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ai.assistance.metaagent.ui.features.home.data.MetaConversation
import com.ai.assistance.metaagent.ui.theme.ConnectionOnline
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

// ── 常量 ──
private val SwipeReadBg = Color(0xFF4CAF50)
private val SwipeUnreadBg = Color(0xFF2196F3)
private val DangerRed = Color(0xFFE53935)
private const val SWIPE_THRESHOLD = 100f
private val GROUP_CORNER = 16.dp
private val FLAT_CORNER = 2.dp

/**
 * 分组消息卡片 — Android 16 通知栏堆叠效果
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwipeableMessageItem(
    conversation: MetaConversation,
    topCorner: Dp,
    bottomCorner: Dp,
    onClick: () -> Unit,
    onMarkRead: () -> Unit,
    onMarkUnread: () -> Unit,
    onMute: () -> Unit,
    onDelete: () -> Unit,
    onDragProgressChanged: (Float) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    val offsetX = remember { Animatable(0f) }
    var hasTriggeredHaptic by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    val absOffset = abs(offsetX.value)
    val progress = (absOffset / SWIPE_THRESHOLD).coerceIn(0f, 1f)
    val isSwipingLeft = offsetX.value < 0

    val bgColor by animateColorAsState(
        targetValue = when {
            absOffset < 5f -> Color.Transparent
            isSwipingLeft -> SwipeReadBg.copy(alpha = 0.15f + 0.85f * progress)
            else -> SwipeUnreadBg.copy(alpha = 0.15f + 0.85f * progress)
        },
        animationSpec = tween(80),
        label = "bg"
    )

    val selfDragCorner = (FLAT_CORNER + (GROUP_CORNER - FLAT_CORNER) * progress)
    val effectiveTopCorner = maxOf(topCorner, selfDragCorner)
    val effectiveBottomCorner = maxOf(bottomCorner, selfDragCorner)

    val shape = RoundedCornerShape(
        topStart = effectiveTopCorner,
        topEnd = effectiveTopCorner,
        bottomStart = effectiveBottomCorner,
        bottomEnd = effectiveBottomCorner
    )

    if (showMenu) {
        MessageLongPressMenu(
            conversation = conversation,
            onDismiss = { showMenu = false },
            onMarkRead = { onMarkRead(); showMenu = false },
            onMarkUnread = { onMarkUnread(); showMenu = false },
            onMute = { onMute(); showMenu = false },
            onDelete = { onDelete(); showMenu = false }
        )
    }

    Box(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(bgColor),
            contentAlignment = if (isSwipingLeft) Alignment.CenterEnd else Alignment.CenterStart
        ) {
            if (absOffset > 20f) {
                Icon(
                    imageVector = if (isSwipingLeft) Icons.Outlined.DoneAll
                    else Icons.Outlined.MarkEmailUnread,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .size((20 + 8 * progress).dp),
                    tint = Color.White
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showMenu = true
                    }
                )
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = { hasTriggeredHaptic = false },
                        onDragEnd = {
                            scope.launch {
                                val finalOffset = offsetX.value
                                if (abs(finalOffset) > SWIPE_THRESHOLD) {
                                    if (finalOffset < 0) onMarkRead()
                                    else onMarkUnread()
                                }
                                offsetX.animateTo(
                                    0f,
                                    animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f)
                                )
                                onDragProgressChanged(0f)
                            }
                        },
                        onDragCancel = {
                            scope.launch {
                                offsetX.animateTo(0f, animationSpec = spring(dampingRatio = 0.6f))
                                onDragProgressChanged(0f)
                            }
                        },
                        onHorizontalDrag = { _, delta ->
                            scope.launch {
                                val newValue = (offsetX.value + delta).coerceIn(-250f, 250f)
                                offsetX.snapTo(newValue)
                                val newProgress = (abs(newValue) / SWIPE_THRESHOLD).coerceIn(0f, 1f)
                                onDragProgressChanged(newProgress)
                                if (abs(newValue) >= SWIPE_THRESHOLD && !hasTriggeredHaptic) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    hasTriggeredHaptic = true
                                }
                                if (abs(newValue) < SWIPE_THRESHOLD) {
                                    hasTriggeredHaptic = false
                                }
                            }
                        }
                    )
                }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = conversation.icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                if (conversation.hasGreenDot) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(ConnectionOnline)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (conversation.isUnread) FontWeight.Bold
                        else FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = conversation.timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = conversation.lastMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (conversation.tag.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(conversation.tagColor.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = conversation.tag,
                                style = MaterialTheme.typography.labelSmall,
                                color = conversation.tagColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    if (conversation.isUnread) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════
// 计算分组圆角的工具函数
// ══════════════════════════════════════════

fun calculateGroupCorners(
    index: Int,
    total: Int,
    prevDragProgress: Float = 0f,
    nextDragProgress: Float = 0f
): Pair<Dp, Dp> {
    val isFirst = index == 0
    val isLast = index == total - 1

    val baseTop = if (isFirst) GROUP_CORNER else FLAT_CORNER
    val baseBottom = if (isLast) GROUP_CORNER else FLAT_CORNER

    val topFromNeighbor = FLAT_CORNER + (GROUP_CORNER - FLAT_CORNER) * prevDragProgress
    val bottomFromNeighbor = FLAT_CORNER + (GROUP_CORNER - FLAT_CORNER) * nextDragProgress

    return Pair(maxOf(baseTop, topFromNeighbor), maxOf(baseBottom, bottomFromNeighbor))
}

// ══════════════════════════════════════════
// 长按菜单
// ══════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun MessageLongPressMenu(
    conversation: MetaConversation,
    onDismiss: () -> Unit,
    onMarkRead: () -> Unit,
    onMarkUnread: () -> Unit,
    onMute: () -> Unit,
    onDelete: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = conversation.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            if (conversation.isUnread) {
                MenuSheetItem(icon = Icons.Outlined.DoneAll, label = "标记为已读", onClick = onMarkRead)
            } else {
                MenuSheetItem(icon = Icons.Outlined.MarkEmailUnread, label = "标记为未读", onClick = onMarkUnread)
            }

            MenuSheetItem(icon = Icons.Outlined.NotificationsOff, label = "静音", onClick = onMute)
            MenuSheetItem(icon = Icons.Default.Notifications, label = "通知设置", onClick = onDismiss)
            MenuSheetItem(icon = Icons.AutoMirrored.Filled.ExitToApp, label = "退出", onClick = onDismiss)

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            MenuSheetItem(icon = Icons.Default.Block, label = "屏蔽并举报", tint = DangerRed, onClick = onDelete)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MenuSheetItem(
    icon: ImageVector,
    label: String,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(22.dp),
            tint = tint.copy(alpha = 0.75f)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = tint
        )
    }
}
