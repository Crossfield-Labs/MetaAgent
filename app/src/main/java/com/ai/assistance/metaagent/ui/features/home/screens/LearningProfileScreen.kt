package com.ai.assistance.metaagent.ui.features.home.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ai.assistance.metaagent.ui.components.CustomScaffold
import com.ai.assistance.metaagent.ui.features.home.data.LearningDemoState
import com.ai.assistance.metaagent.ui.features.home.data.ReviewDemoStage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningProfileScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state = LearningDemoState

    // 根据复习状态推断画像数据
    val masteryPct = (state.mastery * 100).toInt()
    val reviewedCount = when (state.reviewStage) {
        ReviewDemoStage.START -> 0
        ReviewDemoStage.ANSWERED_WRONG -> 1
        ReviewDemoStage.PROFILE_UPDATED -> 2
        ReviewDemoStage.REMINDER_READY -> 3
        ReviewDemoStage.RESUMED -> 4
    }

    CustomScaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("学习画像") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                // 总览卡片
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Psychology,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    "MetaAgent 同学",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    state.profileSummary,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            ProfileStatChip("掌握度", "$masteryPct%", MaterialTheme.colorScheme.primary)
                            ProfileStatChip("已复习", "${reviewedCount} 题", Color(0xFF0F9D7A))
                            ProfileStatChip("错题", "${state.wrongCardsToday} 张", MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            item {
                // 能力分布
                Card(shape = RoundedCornerShape(22.dp)) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("能力分布", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        SkillBar("概念辨析", state.mastery.coerceIn(0.3f, 0.95f))
                        SkillBar("场景判断", (state.mastery * 0.85f).coerceIn(0.2f, 0.9f))
                        SkillBar("应用推导", (state.mastery * 0.7f).coerceIn(0.15f, 0.8f))
                        SkillBar("跨章节关联", (state.mastery * 0.6f).coerceIn(0.1f, 0.75f))
                    }
                }
            }

            item {
                // 学习节奏
                Card(shape = RoundedCornerShape(22.dp)) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("学习节奏", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        ProfileInsightRow(
                            icon = Icons.Default.Schedule,
                            label = "最佳复习时段",
                            value = "晚上 20:00–21:30"
                        )
                        ProfileInsightRow(
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            label = "平均单题用时",
                            value = "约 2.4 分钟"
                        )
                        ProfileInsightRow(
                            icon = Icons.Default.CheckCircle,
                            label = "连续正确最高",
                            value = "${(reviewedCount + 1)} 题"
                        )
                        ProfileInsightRow(
                            icon = Icons.Default.Star,
                            label = "最强知识点",
                            value = "TCP 慢启动机制"
                        )
                    }
                }
            }

            item {
                // 系统推荐
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("系统推荐", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            state.nextRecommendation,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "画像会随每次答题自动更新，推荐节奏和题型都会跟着调整。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun SkillBar(label: String, progress: Float) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(999.dp))
        )
    }
}

@Composable
private fun ProfileStatChip(label: String, value: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun ProfileInsightRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(10.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
