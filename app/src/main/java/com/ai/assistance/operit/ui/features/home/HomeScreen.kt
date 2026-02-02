package com.ai.assistance.operit.ui.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ai.assistance.operit.ui.theme.*
import java.util.Calendar

/**
 * 首页 - 现代化 Material You 设计
 */
@Composable
fun HomeScreen(
    userName: String = "Alex", // 示例用户名
    onNavigateToChat: () -> Unit = {},
    onNavigateToTask: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val greeting = getGreeting()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MonetGreenBackground) // 使用统一背景色
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp) // 增加左右边距
            .statusBarsPadding()
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        // 顶部问候区域
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = "$greeting, $userName",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Medium,
                    color = MonetGreenOnSurface,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "MetaAgent 已激活 • 3 个待办任务", // 模拟数据
                    fontSize = 14.sp,
                    color = MonetGreenSecondary
                )
            }
            
            // 通知按钮 - 圆形，浅色背景
            IconButton(
                onClick = { },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.5f))
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = "通知",
                    tint = MonetGreenSecondary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 主卡片 - 软件体系结构：设计模式 (示例)
        MainActionCard(
            title = "软件体系结构 : 设计模式",
            subtitle = "我已经整理了上次讲座的笔记，并找到了 3 篇关于单例模式的相关论文。",
            time = "14:00",
            tag = "即将开始",
            onClick = onNavigateToChat
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 活跃智能体区域标题
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "活跃智能体",
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                color = MonetGreenOnSurface
            )
            TextButton(
                onClick = { },
                contentPadding = PaddingValues(0.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("查看全", fontSize = 12.sp, color = MonetGreenSecondary)
                    Text("部", fontSize = 12.sp, color = MonetGreenSecondary)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 活跃智能体卡片
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // CNN 模型训练卡片
            AgentCard(
                modifier = Modifier.weight(1f),
                title = "CNN 模型训练",
                status = "运行中",
                progress = 0.87f,
                iconColor = Color(0xFF4A6741),
                backgroundColor = MonetGreenContainer.copy(alpha = 0.5f)
            )
            
            // 每日总结卡片
            AgentCard(
                modifier = Modifier.weight(1f),
                title = "每日总结",
                status = "已计划",
                isScheduled = true,
                description = "将在晚上 8 点聚合 Notion 和 Slack 的笔记。",
                iconColor = Color(0xFF8D6E63),
                backgroundColor = Color(0xFFFFF8E1) // 浅黄背景
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 语音任务卡片
        VoiceTaskCard()
        
        Spacer(modifier = Modifier.height(100.dp)) // 底部导航栏留空
    }
}

/**
 * 获取问候语
 */
private fun getGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 6 -> "夜深了"
        hour < 12 -> "早上好"
        hour < 14 -> "中午好"
        hour < 18 -> "下午好"
        else -> "晚上好"
    }
}

/**
 * 主操作卡片 (大卡片)
 */
@Composable
private fun MainActionCard(
    title: String,
    subtitle: String,
    time: String,
    tag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MonetGreenContainer
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // 标签和时间
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MonetGreenOnContainer.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = tag,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MonetGreenOnContainer
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = time,
                    fontSize = 14.sp,
                    color = MonetGreenSecondary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = MonetGreenOnSurface,
                lineHeight = 32.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = MonetGreenSecondary,
                lineHeight = 22.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MonetGreenOnSurface // 深色按钮
                ),
                shape = RoundedCornerShape(100.dp), // 胶囊形状
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text("复习资料", color = Color.White, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
            }
        }
    }
}

/**
 * 智能体卡片
 */
@Composable
private fun AgentCard(
    modifier: Modifier = Modifier,
    title: String,
    status: String,
    isScheduled: Boolean = false,
    progress: Float = 0f,
    description: String? = null,
    iconColor: Color,
    backgroundColor: Color
) {
    Card(
        modifier = modifier.height(180.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 顶部图标和状态
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            if (isScheduled) Icons.Default.Schedule else Icons.Outlined.SmartToy,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = iconColor
                        )
                    }
                }
                
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.White.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = status,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MonetGreenSecondary
                    )
                }
            }
            
            Column {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MonetGreenOnSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (description != null) {
                    Text(
                        text = description,
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        color = MonetGreenSecondary,
                        maxLines = 2
                    )
                } else {
                    // 进度条
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MonetGreenOnSurface,
                        trackColor = MonetGreenOnSurface.copy(alpha = 0.1f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${(progress * 100).toInt()}% 完成",
                        fontSize = 10.sp,
                        color = MonetGreenSecondary,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}

/**
 * 语音任务卡片
 */
@Composable
private fun VoiceTaskCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MonetGreenContainer.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Mic,
                    contentDescription = "语音",
                    tint = MonetGreenOnSurface
                )
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            Column {
                Text(
                    text = "新建语音任务",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MonetGreenOnSurface
                )
                Text(
                    text = "\"嘿 Meta，整理我的下载文件夹...\"",
                    fontSize = 13.sp,
                    color = MonetGreenSecondary
                )
            }
        }
    }
}
