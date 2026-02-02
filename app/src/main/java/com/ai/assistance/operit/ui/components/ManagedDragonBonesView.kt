package com.ai.assistance.operit.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Stub implementation - DragonBones module has been removed for simplification.
 * This file is kept to prevent compilation errors.
 */

// --- Animation Names (kept for compatibility) ---
const val IDLE_ANIMATION_NAME = "idle"
val RANDOM_IDLE_ANIMATIONS = listOf("blink", "shake_head", "wag_tail")
const val IK_TARGET_BONE_NAME = "ik_target"

/**
 * Stub function - DragonBones module has been removed.
 * This is a placeholder to prevent compilation errors.
 */
@Composable
fun ManagedDragonBonesView(
    model: Any,
    controller: Any,
    modifier: Modifier = Modifier,
    enableGestures: Boolean = true,
    zOrderOnTop: Boolean = true,
    onError: (String) -> Unit = {}
) {
    // Stub implementation - shows nothing
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        // Empty placeholder - DragonBones removed
    }
}
