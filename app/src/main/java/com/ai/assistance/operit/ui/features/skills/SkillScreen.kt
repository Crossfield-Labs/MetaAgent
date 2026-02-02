package com.ai.assistance.operit.ui.features.skills

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ai.assistance.operit.ui.main.LocalDemoController
import com.ai.assistance.operit.ui.main.SkillItem
import com.ai.assistance.operit.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SkillScreen() {
    val demoController = LocalDemoController.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isTeaching by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            //.statusBarsPadding() // AppContent TopBar is shown, so avoid double padding
    ) {
        // AppContent TopBar 存在，所以不需要太多顶部 spacing
        Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))


        // Header (更紧凑)
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = MonetGreenPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "技能库",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                }
                Text(
                    text = "MetaAgent 进化记录",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            
            // Level Badge
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Lv. 4", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Teach New Skill Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2D4A3D))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Surface(color = Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp)) {
                                Text(if(isTeaching) "正在观察..." else "主动学习", color = Color.White.copy(alpha = 0.9f), fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.White.copy(alpha = 0.6f))
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("教授新技能", color = Color.White, style = MaterialTheme.typography.titleLarge)
                        Text("演示演示一次操作，Agent 将自动学习。", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        Button(
                            onClick = {
                                if (isTeaching) return@Button
                                scope.launch {
                                    isTeaching = true
                                    Toast.makeText(context, "请开始操作 App，Agent 正在观察...", Toast.LENGTH_LONG).show()
                                    // 模拟学习延迟
                                    delay(5000) 
                                    demoController.addSkill(demoController.createLearningTongSkill())
                                    Toast.makeText(context, "学习完成！已生成[学习通助手]技能", Toast.LENGTH_SHORT).show()
                                    isTeaching = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB5D3BC)),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            if (isTeaching) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("正在分析操作流...", color = Color.Black, style = MaterialTheme.typography.labelLarge)
                            } else {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("开始演示模式", color = Color.Black, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }

            // Stats Row
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StatsCard(modifier = Modifier.weight(1f), icon = Icons.Default.Book, value = "24", label = "已习得")
                    StatsCard(modifier = Modifier.weight(1f), icon = Icons.Default.Schedule, value = "4.5h", label = "本周节省", iconColor = Color(0xFF7E57C2), iconBg = Color(0xFFEDE7F6))
                }
            }

            // Skill Lists
            item { Text("最新获取", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface) }
            
            items(demoController.skills) { skill ->
                SkillItemCard(skill) {
                    if (skill.id == "learning_tong") {
                        scope.launch {
                            Toast.makeText(context, "正在执行：学习通资料提取...", Toast.LENGTH_SHORT).show()
                            // 尝试打开学习通 (Intent)
                            val intent = context.packageManager.getLaunchIntentForPackage("com.chaoxing.mobile")
                            if (intent != null) {
                                context.startActivity(intent)
                                delay(4000) // 等待4秒模拟操作
                                // 模拟回到本应用（这里很难自动切回，除非有后台权限，或者只Toast）
                                // 我们假设用户会切回来，或者直接在后台添加文件
                                demoController.addFile(demoController.createLearningTongFile())
                                demoController.addFile(demoController.createSummaryFile())
                                Toast.makeText(context, "提取完成！文件已保存", Toast.LENGTH_LONG).show()
                                // TODO: Navigate to Files screen automatically? 
                                // 由于没法直接控制 Router，这里依赖用户手动去 File 页面看结果
                            } else {
                                Toast.makeText(context, "未安装学习通，仅模拟数据生成", Toast.LENGTH_SHORT).show()
                                delay(2000)
                                demoController.addFile(demoController.createLearningTongFile())
                                demoController.addFile(demoController.createSummaryFile())
                                Toast.makeText(context, "模拟完成！文件已保存", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, "执行技能：${skill.name}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}

@Composable
fun StatsCard(
    modifier: Modifier = Modifier, 
    icon: ImageVector, 
    value: String, 
    label: String,
    iconColor: Color = Color(0xFF2E7D32),
    iconBg: Color = Color(0xFFE8F5E9)
) {
    Card(
        modifier = modifier.height(100.dp), // 稍微调低高度
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Surface(shape = CircleShape, color = iconBg, modifier = Modifier.size(28.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SkillItemCard(skill: SkillItem, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(72.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = skill.bgColor) // Keep custom color for distinction
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(shape = CircleShape, color = Color.White, modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(skill.icon, contentDescription = null, tint = Color.Black.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(skill.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A1A))
                Text(skill.desc, style = MaterialTheme.typography.bodySmall, color = Color(0xFF666666))
            }
            Surface(shape = RoundedCornerShape(8.dp), color = Color.White.copy(alpha = 0.6f)) {
                Text(skill.tag, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = Color(0xFF1A1A1A))
            }
        }
    }
}
