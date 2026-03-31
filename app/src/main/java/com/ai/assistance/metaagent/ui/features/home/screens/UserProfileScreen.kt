package com.ai.assistance.metaagent.ui.features.home.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ai.assistance.metaagent.R
import com.ai.assistance.metaagent.ui.theme.GoogleBlue
import com.ai.assistance.metaagent.ui.theme.GoogleGreen
import com.ai.assistance.metaagent.ui.theme.GoogleRed
import com.ai.assistance.metaagent.ui.theme.GoogleYellow
import com.yalantis.ucrop.UCrop
import java.io.File

// ── uCrop 的 ActivityResultContract ──
class CropImageContract : ActivityResultContract<Pair<Uri, Uri>, Uri?>() {
    override fun createIntent(context: Context, input: Pair<Uri, Uri>): Intent {
        val (sourceUri, destUri) = input
        return UCrop.of(sourceUri, destUri)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(512, 512)
            .withOptions(UCrop.Options().apply {
                setCompressionQuality(90)
                setCircleDimmedLayer(true)
                setShowCropFrame(false)
                setShowCropGrid(false)
            })
            .getIntent(context)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return if (resultCode == Activity.RESULT_OK && intent != null) {
            UCrop.getOutput(intent)
        } else null
    }
}

/**
 * 用户个人中心 — 忠实还原 Figma 设计稿
 *
 * 功能：
 * 1. 头像区（支持从相册选取裁剪上传）
 * 2. Hello + 用户名 + 搜索/通知
 * 3. 标签横滑栏
 * 4. Popular 统计卡片（使用 Figma 导出的 Vector Drawable）
 * 5. Continue 对话学习进度列表
 * 6. 功能菜单列表
 */
@Composable
fun UserProfileScreen(
    onClose: () -> Unit,
    onNavigate: (String) -> Unit = {},
    avatarUri: Uri? = null,
    onAvatarChanged: (Uri) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTag by remember { mutableStateOf("学习") }
    val tags = listOf("学习", "任务", "复习", "闲聊", "实验", "笔记")

    // ── 头像选取与 uCrop 裁剪 ──
    val context = androidx.compose.ui.platform.LocalContext.current

    // Step 2: uCrop 裁剪完成 → 更新头像
    val cropLauncher = rememberLauncherForActivityResult(
        contract = CropImageContract()
    ) { croppedUri: Uri? ->
        croppedUri?.let { onAvatarChanged(it) }
    }

    // Step 1: 选图完成 → 启动 uCrop
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { sourceUri ->
            val destFile = File(context.cacheDir, "avatar_crop_${System.currentTimeMillis()}.jpg")
            val destUri = Uri.fromFile(destFile)
            cropLauncher.launch(Pair(sourceUri, destUri))
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        // ═══ 顶部返回 ═══
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = Color.Black
                )
            }
        }

        // ═══ 可滚动内容 ═══
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            // ── 头像 + 用户信息 ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 左：头像（带彩色圆环 + 相机图标）
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .border(
                                width = 2.5.dp,
                                shape = CircleShape,
                                brush = Brush.sweepGradient(
                                    colors = listOf(
                                        GoogleBlue, GoogleGreen, GoogleYellow,
                                        GoogleRed, GoogleBlue
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (avatarUri != null) {
                            // 用户选取的头像
                            AsyncImage(
                                model = avatarUri,
                                contentDescription = "头像",
                                modifier = Modifier
                                    .size(62.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // 默认字母头像
                            Box(
                                modifier = Modifier
                                    .size(62.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "M",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                    // 相机按钮 — 点击打开相册
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF0F0F0))
                            .clickable { imagePicker.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "上传头像",
                            modifier = Modifier.size(14.dp),
                            tint = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // 右：Hello + @用户名
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Hello",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Text(
                        text = "@MetaAgent同学",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                }

                // 搜索 + 通知
                Icon(
                    Icons.Default.Search,
                    contentDescription = "搜索",
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onNavigate("ai_chat") },
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.width(12.dp))
                BadgedBox(
                    badge = {
                        Badge(containerColor = Color.Red) {
                            Text("2", color = Color.White, fontSize = 10.sp)
                        }
                    },
                    modifier = Modifier.clickable { onNavigate("task_center") }
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "通知",
                        modifier = Modifier.size(20.dp),
                        tint = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── 标签横滑栏 ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                tags.forEach { tag ->
                    val isSelected = tag == selectedTag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(5.dp))
                            .then(
                                if (isSelected) Modifier.background(Color.Black)
                                else Modifier.border(
                                    1.dp,
                                    Color.Black.copy(alpha = 0.1f),
                                    RoundedCornerShape(5.dp)
                                )
                            )
                            .clickable {
                                selectedTag = tag
                                when (tag) {
                                    "学习" -> onNavigate("course_space")
                                    "任务" -> onNavigate("task_center")
                                    "复习" -> onNavigate("review")
                                    "闲聊" -> onNavigate("ai_chat")
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = tag,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isSelected) Color.White else Color.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Popular 标题 ──
            Text(
                text = "Popular",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(10.dp))

            // ── Popular 卡片 (使用 Figma 导出的 Vector Drawable) ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
            ) {
                // 背景装饰图 — 从 Figma 导出的完整卡片
                Image(
                    painter = painterResource(id = R.drawable.card_popular_deco),
                    contentDescription = "学习统计装饰",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(335f / 209f),
                    contentScale = ContentScale.FillWidth
                )

                // 叠加文字内容
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(335f / 209f)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // 顶部：标题 + 数字
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("✦", fontSize = 18.sp, color = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "学习统计",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                        }
                        Text(
                            text = "85",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                    }

                    // 底部白色信息条
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color.White)
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "5 课程", fontSize = 12.sp,
                            fontWeight = FontWeight.Medium, color = Color.Black
                        )
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(Color.Black)
                        )
                        Text(
                            "128 小时", fontSize = 12.sp,
                            fontWeight = FontWeight.Medium, color = Color.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Continue 标题 ──
            Text(
                text = "Continue",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(10.dp))

            // ── 对话学习进度列表 ──
            ContinueItem("高等数学 · 微积分", 0.72f, "72%")
            Spacer(modifier = Modifier.height(10.dp))
            ContinueItem("数据结构 · 图论", 0.45f, "45%")
            Spacer(modifier = Modifier.height(10.dp))
            ContinueItem("操作系统 · 进程调度", 0.33f, "33%")

            Spacer(modifier = Modifier.height(24.dp))

            // ── 功能列表 ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, Color.Black.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
            ) {
                ProfileNavItem(
                    icon = Icons.Outlined.Psychology,
                    label = "学习画像",
                    onClick = { onNavigate("learning_profile") }
                )
                ProfileNavDivider()
                ProfileNavItem(
                    icon = Icons.Outlined.Timeline,
                    label = "编排树",
                    onClick = { onNavigate("plan_tree/t1") }
                )
                ProfileNavDivider()
                ProfileNavItem(
                    icon = Icons.Outlined.Devices,
                    label = "设备配对",
                    onClick = { onNavigate("device_pairing") }
                )
                ProfileNavDivider()
                ProfileNavItem(
                    icon = Icons.Outlined.Notifications,
                    label = "通知设置",
                    onClick = { onNavigate("settings") }
                )
                ProfileNavDivider()
                ProfileNavItem(
                    icon = Icons.Outlined.Settings,
                    label = "设置",
                    onClick = { onNavigate("settings") }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ══════════════════════════════════════════════
// Figma Continue 列表项
// ══════════════════════════════════════════════

@Composable
private fun ContinueItem(
    title: String,
    progress: Float,
    progressText: String
) {
    var animTarget by remember { mutableFloatStateOf(0f) }
    val animProgress by animateFloatAsState(
        targetValue = animTarget,
        animationSpec = tween(durationMillis = 800),
        label = "progress"
    )
    LaunchedEffect(progress) { animTarget = progress }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, Color.Black.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
            .background(Color.White)
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title, fontSize = 14.sp,
                    fontWeight = FontWeight.Medium, color = Color.Black
                )
                Text(
                    text = progressText, fontSize = 14.sp,
                    fontWeight = FontWeight.Medium, color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 进度条 (Figma: 黑色/灰色, 10dp高, 全圆角)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFE7E7E7))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animProgress)
                        .height(10.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black)
                )
            }
        }
    }
}

// ══════════════════════════════════════════════
// 功能菜单列表项
// ══════════════════════════════════════════════

@Composable
private fun ProfileNavItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(22.dp),
            tint = Color.Black.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label, fontSize = 14.sp,
            fontWeight = FontWeight.Medium, color = Color.Black
        )
    }
}

@Composable
private fun ProfileNavDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 20.dp),
        color = Color.Black.copy(alpha = 0.06f)
    )
}
