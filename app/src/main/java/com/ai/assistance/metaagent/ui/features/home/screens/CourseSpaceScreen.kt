package com.ai.assistance.metaagent.ui.features.home.screens

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ai.assistance.metaagent.ui.features.home.data.CourseItem
import com.ai.assistance.metaagent.ui.features.home.data.CourseMaterialDraft
import com.ai.assistance.metaagent.ui.features.home.data.StudyCourse
import com.ai.assistance.metaagent.ui.features.home.data.StudyModuleStore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CourseSpaceScreen(onCourseClick: (String) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    StudyModuleStore.ensureInitialized(context)

    val courses = StudyModuleStore.courses.map { it.toCourseItem() }
    val categoryOptions = courses.map { it.category }.distinct().sorted()
    val categories = listOf("全部") + categoryOptions

    var selectedCategory by remember { mutableStateOf("全部") }
    var categoryEditMode by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showSearchDialog by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    var pendingDeleteCourse by remember { mutableStateOf<CourseItem?>(null) }
    var pendingDeleteCategory by remember { mutableStateOf<String?>(null) }

    val filteredCourses = if (selectedCategory == "全部") courses else courses.filter { it.category == selectedCategory }
    val popularCourse = filteredCourses.maxByOrNull { it.rating }

    val wiggleTransition = rememberInfiniteTransition(label = "category_wiggle")
    val wiggleRotation by wiggleTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(180),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wiggle"
    )

    LazyColumn(modifier = modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 100.dp)) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Hello", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("@MetaAgent", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
                }
                Row {
                    IconButton(onClick = { showCreateDialog = true }) { Icon(Icons.Default.Add, contentDescription = "创建课程") }
                    IconButton(onClick = { showSearchDialog = true }) { Icon(Icons.Default.Search, contentDescription = "搜索") }
                    IconButton(onClick = { showNotificationDialog = true }) { Icon(Icons.Default.Notifications, contentDescription = "通知") }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.forEach { categoryName ->
                    val selected = selectedCategory == categoryName
                    val deletable = categoryName != "全部"

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                            .combinedClickable(
                                onClick = { selectedCategory = categoryName },
                                onLongClick = {
                                    if (deletable) {
                                        categoryEditMode = true
                                    }
                                }
                            )
                            .graphicsLayer {
                                rotationZ = if (categoryEditMode && deletable) wiggleRotation else 0f
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = categoryName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )

                        if (categoryEditMode && deletable) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 7.dp, y = (-7).dp)
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.error)
                                    .clickable { pendingDeleteCategory = categoryName },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
    imageVector = Icons.Default.Close,
    contentDescription = "删除课程集",
    tint = MaterialTheme.colorScheme.onError,
    modifier = Modifier.size(10.dp)
)
                            }
                        }
                    }
                }

                if (categoryEditMode) {
                    TextButton(onClick = { categoryEditMode = false }) {
                        Text("完成")
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }

        item {
            Text(
                "Popular",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(12.dp))
        }

        if (popularCourse != null) {
            item {
                CourseCard(popularCourse, isPopular = true, onClick = { onCourseClick(popularCourse.id) })
                Spacer(Modifier.height(24.dp))
            }
        }

        item {
            Text(
                "Continue",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(12.dp))
        }

        if (filteredCourses.isEmpty()) {
            item {
                Text(
                    text = "当前暂无课程，点击右上角 + 创建课程",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(filteredCourses, key = { it.id }) { course ->
            SwipeDeleteCourseCard(
                course = course,
                onClick = { onCourseClick(course.id) },
                onDeleteRequest = { pendingDeleteCourse = course }
            )
        }
    }

    if (showCreateDialog) {
        CreateCourseDialog(
            existingCategories = categoryOptions,
            onDismiss = { showCreateDialog = false },
            onConfirm = { payload ->
                StudyModuleStore.createCourse(
                    name = payload.name,
                    category = payload.category,
                    teacher = payload.teacher,
                    overview = payload.overview,
                    progress = payload.progress,
                    sectionCount = payload.sectionCount,
                    highlights = payload.highlights,
                    latestNote = payload.latestNote,
                    materials = payload.materials
                )
                selectedCategory = payload.category
                showCreateDialog = false
            }
        )
    }

    if (showSearchDialog) {
        SearchCourseDialog(
            courses = courses,
            onDismiss = { showSearchDialog = false },
            onSelect = { courseId ->
                showSearchDialog = false
                onCourseClick(courseId)
            }
        )
    }

    if (showNotificationDialog) {
        CourseNotificationDialog(
            notifications = buildCourseNotifications(courses, selectedCategory),
            onDismiss = { showNotificationDialog = false }
        )
    }

    pendingDeleteCourse?.let { targetCourse ->
        AlertDialog(
            onDismissRequest = { pendingDeleteCourse = null },
            title = { Text("删除课程") },
            text = { Text("确认删除《${targetCourse.name}》吗？课程资料也会一并删除。") },
            confirmButton = {
                TextButton(onClick = {
                    val deleted = StudyModuleStore.deleteCourse(targetCourse.id)
                    if (deleted) {
                        Toast.makeText(context, "课程已删除", Toast.LENGTH_SHORT).show()
                        if (selectedCategory != "全部" && StudyModuleStore.courses.none { it.category == selectedCategory }) {
                            selectedCategory = "全部"
                        }
                    }
                    pendingDeleteCourse = null
                }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteCourse = null }) {
                    Text("取消")
                }
            }
        )
    }

    pendingDeleteCategory?.let { targetCategory ->
        AlertDialog(
            onDismissRequest = { pendingDeleteCategory = null },
            title = { Text("删除课程集") },
            text = { Text("确认删除课程集“$targetCategory”吗？该课程集下的所有课程与资料都会被删除。") },
            confirmButton = {
                TextButton(onClick = {
                    val removedCount = StudyModuleStore.deleteCategory(targetCategory)
                    if (removedCount > 0) {
                        Toast.makeText(context, "已删除课程集和${removedCount}门课程", Toast.LENGTH_SHORT).show()
                        if (selectedCategory == targetCategory) {
                            selectedCategory = "全部"
                        }
                    }
                    pendingDeleteCategory = null
                    categoryEditMode = false
                }) {
                    Text("删除课程集", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteCategory = null }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun CourseCard(course: CourseItem, isPopular: Boolean, onClick: () -> Unit) {
    val bg = if (isPopular) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(course.icon, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(course.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                }
                if (isPopular) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(14.dp))
                        Text("${course.rating}", fontSize = 13.sp)
                    }
                } else {
                    Text("${(course.progress * 100).toInt()}%", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { course.progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(8.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                strokeCap = StrokeCap.Round
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(5.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurfaceVariant))
                Spacer(Modifier.width(6.dp))
                Text("${course.materialCount} 资料 · ${course.totalHours}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun SwipeDeleteCourseCard(
    course: CourseItem,
    onClick: () -> Unit,
    onDeleteRequest: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val threshold = 110f
    val progress = (abs(offsetX.value) / threshold).coerceIn(0f, 1f)

    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 5.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 108.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFE53935).copy(alpha = 0.1f + 0.7f * progress)),
            contentAlignment = if (offsetX.value >= 0f) Alignment.CenterStart else Alignment.CenterEnd
        ) {
            if (abs(offsetX.value) > 18f) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = null,
                    modifier = Modifier.padding(horizontal = 20.dp).size((18 + 8 * progress).dp),
                    tint = Color.White
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 108.dp)
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .clickable(onClick = onClick)
                .pointerInput(course.id) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, delta ->
                            scope.launch {
                                val updated = (offsetX.value + delta).coerceIn(-220f, 220f)
                                offsetX.snapTo(updated)
                            }
                        },
                        onDragEnd = {
                            scope.launch {
                                if (abs(offsetX.value) >= threshold) {
                                    onDeleteRequest()
                                }
                                offsetX.animateTo(0f, animationSpec = spring(dampingRatio = 0.65f))
                            }
                        },
                        onDragCancel = {
                            scope.launch {
                                offsetX.animateTo(0f, animationSpec = spring(dampingRatio = 0.65f))
                            }
                        }
                    )
                }
                .padding(16.dp)
        ) {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(course.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    Text("${(course.progress * 100).toInt()}%", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { course.progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(8.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    strokeCap = StrokeCap.Round
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(5.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurfaceVariant))
                    Spacer(Modifier.width(6.dp))
                    Text("${course.materialCount} 资料 · ${course.totalHours}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

private data class CourseCreatePayload(
    val name: String,
    val category: String,
    val teacher: String,
    val overview: String,
    val progress: Int,
    val sectionCount: Int,
    val highlights: List<String>,
    val latestNote: String,
    val materials: List<CourseMaterialDraft>
)

private data class DraftCourseMaterial(
    val uri: Uri,
    val displayName: String,
    val mimeType: String,
    val sizeBytes: Long
)

@Composable
private fun CreateCourseDialog(
    existingCategories: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (CourseCreatePayload) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var courseName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("软件工程") }
    var teacher by remember { mutableStateOf("") }
    var progressText by remember { mutableStateOf("0") }
    var sectionCountText by remember { mutableStateOf("8") }
    var overview by remember { mutableStateOf("") }
    var highlightsText by remember { mutableStateOf("") }
    var latestNote by remember { mutableStateOf("") }
    var categorySuggestionVisible by remember { mutableStateOf(false) }

    var materials by remember { mutableStateOf<List<DraftCourseMaterial>>(emptyList()) }
    var summaryStatus by remember { mutableStateOf("") }
    var isSummarizing by remember { mutableStateOf(false) }
    var summaryJob by remember { mutableStateOf<Job?>(null) }

    DisposableEffect(Unit) {
        onDispose { summaryJob?.cancel() }
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        val added = uris.mapNotNull { uri ->
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.resolveDraftCourseMaterial(uri)
        }
        if (added.isNotEmpty()) {
            materials = (materials + added).distinctBy { it.uri.toString() }
            summaryStatus = "已添加 ${materials.size} 个文件"
        }
    }

    fun startMockSummary() {
        if (materials.isEmpty() || isSummarizing) return
        summaryJob?.cancel()
        summaryJob = scope.launch {
            isSummarizing = true
            try {
                summaryStatus = "AI 正在解析 ${materials.size} 个文件..."
                delay(400)

                val names = materials.take(3).joinToString("、") { it.displayName.substringBeforeLast('.') }.ifBlank { "课程资料" }
                val generatedOverview = "本课程基于${names}等资料构建，重点覆盖核心概念理解、案例拆解与实操训练。"

                overview = ""
                generatedOverview.chunked(6).forEach { chunk ->
                    overview += chunk
                    delay(55)
                }

                val generatedHighlights = listOf(
                    "梳理课程主线与知识图谱，形成可复用学习框架",
                    "提炼高频考点与题型模板，提升解题效率",
                    "结合实验场景完成一次端到端实践"
                )
                highlightsText = ""
                generatedHighlights.forEachIndexed { index, point ->
                    summaryStatus = "AI 正在提炼课程重点（${index + 1}/${generatedHighlights.size}）..."
                    val line = "${index + 1}. $point"
                    line.forEach { ch ->
                        highlightsText += ch
                        delay(18)
                    }
                    if (index < generatedHighlights.lastIndex) {
                        highlightsText += "\n"
                        delay(120)
                    }
                }

                if (teacher.isBlank()) teacher = "课程助教"
                if (latestNote.isBlank()) latestNote = "AI 已根据上传资料完成课程摘要"
                if (progressText.toIntOrNull() == null) progressText = "0"
                if (sectionCountText.toIntOrNull() == null) sectionCountText = "8"
                summaryStatus = "AI 总结完成，可直接创建课程"
            } finally {
                isSummarizing = false
            }
        }
    }

    AlertDialog(
        onDismissRequest = { summaryJob?.cancel(); onDismiss() },
        confirmButton = {
            TextButton(
                enabled = courseName.trim().isNotEmpty() && !isSummarizing,
                onClick = {
                    onConfirm(
                        CourseCreatePayload(
                            name = courseName.trim(),
                            category = category.trim().ifBlank { "自定义课程" },
                            teacher = teacher.trim().ifBlank { "课程助教" },
                            overview = overview.trim().ifBlank { "课程简介待补充" },
                            progress = (progressText.toIntOrNull() ?: 0).coerceIn(0, 100),
                            sectionCount = (sectionCountText.toIntOrNull() ?: 8).coerceAtLeast(1),
                            highlights = parseHighlightsInput(highlightsText),
                            latestNote = latestNote.trim().ifBlank { "课程已创建，等待整理" },
                            materials = materials.map { CourseMaterialDraft(it.uri.toString(), it.displayName, it.mimeType) }
                        )
                    )
                }
            ) {
                Text("创建")
            }
        },
        dismissButton = {
            TextButton(enabled = !isSummarizing, onClick = { summaryJob?.cancel(); onDismiss() }) {
                Text("取消")
            }
        },
        title = { Text("创建课程") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(max = 520.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(courseName, { courseName = it }, singleLine = true, label = { Text("课程名称 *") }, modifier = Modifier.fillMaxWidth())

                val matchedCategories = existingCategories.filter { it.contains(category, ignoreCase = true) }.take(8)
                OutlinedTextField(
                    value = category,
                    onValueChange = {
                        category = it
                        categorySuggestionVisible = true
                    },
                    singleLine = true,
                    label = { Text("课程分类（可新建）") },
                    modifier = Modifier.fillMaxWidth()
                )
                if (categorySuggestionVisible && matchedCategories.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        matchedCategories.forEach { option ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        category = option
                                        categorySuggestionVisible = false
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Text(option, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
                if (category.isNotBlank() && existingCategories.none { it.equals(category, ignoreCase = true) }) {
                    Text(
                        text = "将自动创建新课程集：$category",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                OutlinedTextField(teacher, { teacher = it }, singleLine = true, label = { Text("授课老师") }, modifier = Modifier.fillMaxWidth())

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(progressText, { progressText = it.filter(Char::isDigit) }, singleLine = true, label = { Text("进度(0-100)") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(sectionCountText, { sectionCountText = it.filter(Char::isDigit) }, singleLine = true, label = { Text("章节数") }, modifier = Modifier.weight(1f))
                }

                OutlinedTextField(overview, { overview = it }, label = { Text("课程简介") }, minLines = 3, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(highlightsText, { highlightsText = it }, label = { Text("课程重点（每行一条）") }, minLines = 3, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(latestNote, { latestNote = it }, label = { Text("最近笔记") }, minLines = 2, modifier = Modifier.fillMaxWidth())

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("课程资料", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { picker.launch(arrayOf("*/*")) }) {
                            Icon(Icons.Default.AttachFile, null)
                            Spacer(Modifier.width(4.dp))
                            Text("添加文件")
                        }
                        TextButton(enabled = materials.isNotEmpty() && !isSummarizing, onClick = { startMockSummary() }) {
                            Icon(Icons.Default.Lightbulb, null)
                            Spacer(Modifier.width(4.dp))
                            Text("AI 自动总结")
                        }
                    }

                    if (isSummarizing) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text(summaryStatus, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else if (summaryStatus.isNotBlank()) {
                        Text(summaryStatus, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }

                    if (materials.isEmpty()) {
                        Text("暂未上传资料。可先添加文件，再点击 AI 自动总结。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        materials.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.displayName, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                                    Text(formatSize(item.sizeBytes), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(onClick = { materials = materials.filterNot { it.uri == item.uri } }) {
                                    Icon(Icons.Default.Close, contentDescription = "移除文件")
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

private fun parseHighlightsInput(raw: String): List<String> {
    val lines = raw.lineSequence()
        .map { it.trim().replace(Regex("^[-•]+\\s*"), "").replace(Regex("^\\d+[.)、]\\s*"), "") }
        .filter { it.isNotEmpty() }
        .toList()
    return if (lines.isEmpty()) listOf("课程重点待补充") else lines
}

private fun android.content.Context.resolveDraftCourseMaterial(uri: Uri): DraftCourseMaterial? {
    val resolver = contentResolver
    val defaultName = "material_${System.currentTimeMillis()}"
    var displayName = defaultName
    var sizeBytes = 0L
    val mimeType = resolver.getType(uri) ?: "application/octet-stream"

    runCatching {
        resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (nameIndex >= 0) displayName = cursor.getString(nameIndex) ?: defaultName
                if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) sizeBytes = cursor.getLong(sizeIndex)
            }
        }
    }

    return DraftCourseMaterial(uri, displayName, mimeType, sizeBytes)
}

private fun formatSize(sizeBytes: Long): String {
    if (sizeBytes <= 0L) return "未知大小"
    val kb = sizeBytes / 1024.0
    if (kb < 1024) return String.format(Locale.US, "%.1f KB", kb)
    return String.format(Locale.US, "%.1f MB", kb / 1024.0)
}

private data class CourseNotificationItem(
    val title: String,
    val detail: String,
    val timeLabel: String,
    val unread: Boolean = true
)

@Composable
private fun SearchCourseDialog(
    courses: List<CourseItem>,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    var keyword by remember { mutableStateOf("") }
    val matchedCourses = courses.filter {
        if (keyword.isBlank()) {
            true
        } else {
            it.name.contains(keyword, ignoreCase = true) || it.category.contains(keyword, ignoreCase = true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("关闭") } },
        title = { Text("搜索课程") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = keyword,
                    onValueChange = { keyword = it },
                    singleLine = true,
                    label = { Text("输入课程名或分类") },
                    modifier = Modifier.fillMaxWidth()
                )
                if (matchedCourses.isEmpty()) {
                    Text("没有找到匹配课程", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    matchedCourses.forEach { course ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                .clickable { onSelect(course.id) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(course.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                Text(course.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text("进入", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun CourseNotificationDialog(
    notifications: List<CourseNotificationItem>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("我知道了") } },
        title = { Text("课程通知") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                notifications.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(item.detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            if (item.unread) {
                                Box(
                                    modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary)
                                )
                                Spacer(Modifier.height(4.dp))
                            }
                            Text(item.timeLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    )
}

private fun buildCourseNotifications(courses: List<CourseItem>, selectedCategory: String): List<CourseNotificationItem> {
    val topCourse = courses.maxByOrNull { it.progress }
    val categoryLabel = if (selectedCategory == "全部") "当前课程集" else selectedCategory
    return listOf(
        CourseNotificationItem(
            title = "课程集更新",
            detail = "$categoryLabel 已同步最新课程列表",
            timeLabel = "刚刚",
            unread = true
        ),
        CourseNotificationItem(
            title = "学习进度提醒",
            detail = "建议继续学习《${topCourse?.name ?: "课程"}》",
            timeLabel = "今天 10:30",
            unread = true
        ),
        CourseNotificationItem(
            title = "整理建议",
            detail = "可在“一键整理”中为本周课程创建新任务",
            timeLabel = "昨天",
            unread = false
        )
    )
}

private fun StudyCourse.toCourseItem(): CourseItem {
    val icon: ImageVector = when {
        category.contains("软件") -> Icons.Default.Build
        category.contains("网络") || category.contains("计算机") -> Icons.Default.Public
        else -> Icons.Default.School
    }

    val accentColor = when {
        category.contains("软件") -> Color(0xFF7C4DFF)
        category.contains("计算机") -> Color(0xFF448AFF)
        category.contains("人工智能") || category.contains("AI", ignoreCase = true) -> Color(0xFF00C853)
        else -> Color(0xFF5C6BC0)
    }

    val progressFloat = progress.coerceIn(0, 100) / 100f
    val displayMaterialCount = maxOf(materialCount, materials.orEmpty().size)
    val derivedRating = 4.2f + progressFloat * 0.8f

    return CourseItem(
        id = id,
        name = name,
        icon = icon,
        accentColor = accentColor,
        materialCount = displayMaterialCount,
        reviewCount = highlights.size,
        experimentCount = 0,
        weakPoints = highlights.take(2),
        progress = progressFloat,
        totalHours = "${(sectionCount * 2).coerceAtLeast(8)} 小时",
        rating = String.format(Locale.US, "%.1f", derivedRating).toFloat(),
        category = category
    )
}

