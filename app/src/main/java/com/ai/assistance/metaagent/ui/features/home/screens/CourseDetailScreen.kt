package com.ai.assistance.metaagent.ui.features.home.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.ai.assistance.metaagent.ui.features.home.data.CourseMaterialDraft
import com.ai.assistance.metaagent.ui.features.home.data.StudyCourseMaterial
import com.ai.assistance.metaagent.ui.features.home.data.StudyModuleStore
import com.ai.assistance.metaagent.ui.features.home.data.StudyRagIndexStatus
import java.util.Locale

@Composable
fun CourseDetailScreen(
    courseId: String,
    onAskAiClick: (courseId: String, courseName: String) -> Unit,
    onDeleteCourse: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    StudyModuleStore.ensureInitialized(context)
    var showDeleteCourseDialog by remember { mutableStateOf(false) }

    val addMaterialLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isEmpty()) {
            return@rememberLauncherForActivityResult
        }
        val drafts = uris.mapNotNull { uri ->
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            context.resolveCourseMaterialDraft(uri)
        }
        val addedCount = StudyModuleStore.addMaterialsToCourse(courseId, drafts)
        if (addedCount > 0) {
            Toast.makeText(context, "已添加 $addedCount 个文件", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "未添加成功，请重试", Toast.LENGTH_SHORT).show()
        }
    }

    val course = StudyModuleStore.getCourse(courseId)

    if (course == null) {
        Column(
            modifier = modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("未找到课程，可能已被删除。", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    val materials = course.materials.orEmpty()
    val ragReadyCount = materials.count {
        it.ragStatus == StudyRagIndexStatus.READY && it.ragMemoryIds.orEmpty().isNotEmpty()
    }
    val ragIndexingCount = materials.count { it.ragStatus == StudyRagIndexStatus.INDEXING }
    val ragFailedCount = materials.count { it.ragStatus == StudyRagIndexStatus.FAILED }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(course.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        TextButton(onClick = { showDeleteCourseDialog = true }) {
                            Text("删除课程", color = MaterialTheme.colorScheme.error)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("${course.category} · ${course.teacher}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(progress = { (course.progress.coerceIn(0, 100) / 100f) }, modifier = Modifier.fillMaxWidth().height(8.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("当前进度 ${course.progress}%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("课程简介", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text(course.overview, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("课程重点", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    course.highlights.forEachIndexed { index, point ->
                        Text("${index + 1}. $point", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp))
                    }
                }
            }
        }

        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("课程资源", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        TextButton(onClick = { addMaterialLauncher.launch(arrayOf("*/*")) }) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("添加文件")
                        }
                    }
                    if (materials.isEmpty()) {
                        val tips = if (course.materialCount > 0) {
                            "当前为示例课程，已统计 ${course.materialCount} 份资料；如需本地打开，请在新建课程时上传文件。"
                        } else {
                            "暂无上传资源。点击右上角“添加文件”可直接上传。"
                        }
                        Text(tips, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        materials.forEach { material ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                    .padding(horizontal = 10.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(material.displayName, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                                        Text("${formatSize(material.sizeBytes)} · ${material.addedAt}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            buildRagStatusText(material),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ragStatusColor(material, MaterialTheme.colorScheme.primary)
                                        )
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "打开",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.clickable { openMaterialExternally(context, material) }
                                    )
                                    IconButton(onClick = {
                                        val removed = StudyModuleStore.removeCourseMaterial(courseId, material.id)
                                        if (removed) {
                                            Toast.makeText(context, "资源已删除", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "删除失败", Toast.LENGTH_SHORT).show()
                                        }
                                    }) {
                                        Icon(
                                            Icons.Default.DeleteOutline,
                                            contentDescription = "删除资源",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("课程知识库", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        TextButton(onClick = {
                            val count = StudyModuleStore.reindexCourseKnowledgeBase(courseId)
                            if (count > 0) {
                                Toast.makeText(context, "已触发 $count 个资源重新入库", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "暂无可重建资源", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Text("重建入库")
                        }
                    }
                    Text(
                        "已入库 $ragReadyCount / ${materials.size} · 入库中 $ragIndexingCount · 失败 $ragFailedCount",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "课程问答会优先检索当前课程知识库内容。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainer)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("课程内问答（跳转 AI 对话）", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text("点击后会直接进入现有 AI 对话页，并新建课程会话。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(10.dp))
                    Button(onClick = { onAskAiClick(course.id, course.name) }) { Text("进入 AI 对话") }
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ElevatedCard(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("章节", style = MaterialTheme.typography.labelMedium)
                        Text(course.sectionCount.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    }
                }
                ElevatedCard(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("资料", style = MaterialTheme.typography.labelMedium)
                        Text(maxOf(course.materialCount, materials.size).toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showDeleteCourseDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteCourseDialog = false },
            title = { Text("删除课程") },
            text = { Text("确认删除《${course.name}》吗？课程资料也会一并删除。") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteCourseDialog = false
                    val deleted = StudyModuleStore.deleteCourse(course.id)
                    if (deleted) {
                        Toast.makeText(context, "课程已删除", Toast.LENGTH_SHORT).show()
                        onDeleteCourse()
                    } else {
                        Toast.makeText(context, "删除失败", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteCourseDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

private fun Context.resolveCourseMaterialDraft(uri: Uri): CourseMaterialDraft? {
    val resolver = contentResolver
    val defaultName = "material_${System.currentTimeMillis()}"
    var displayName = defaultName

    runCatching {
        resolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    displayName = cursor.getString(nameIndex) ?: defaultName
                }
            }
        }
    }

    return CourseMaterialDraft(
        sourceUri = uri.toString(),
        displayName = displayName,
        mimeType = resolver.getType(uri)
    )
}

private fun buildRagStatusText(material: StudyCourseMaterial): String {
    return when (material.ragStatus ?: StudyRagIndexStatus.PENDING) {
        StudyRagIndexStatus.READY -> {
            val indexedAt = material.ragIndexedAt.takeIf { it.isNotBlank() } ?: "刚刚"
            "知识库：已入库（$indexedAt）"
        }
        StudyRagIndexStatus.INDEXING -> "知识库：入库中..."
        StudyRagIndexStatus.FAILED -> "知识库：入库失败，可点击“重建入库”"
        StudyRagIndexStatus.PENDING -> "知识库：待入库"
    }
}

private fun ragStatusColor(material: StudyCourseMaterial, primary: Color): Color {
    return when (material.ragStatus ?: StudyRagIndexStatus.PENDING) {
        StudyRagIndexStatus.READY -> Color(0xFF2E7D32)
        StudyRagIndexStatus.INDEXING -> primary
        StudyRagIndexStatus.FAILED -> Color(0xFFC62828)
        StudyRagIndexStatus.PENDING -> Color(0xFF757575)
    }
}

private fun openMaterialExternally(context: Context, material: StudyCourseMaterial) {
    val file = StudyModuleStore.resolveMaterialFile(material)
    if (file == null) {
        Toast.makeText(context, "文件不存在或已失效", Toast.LENGTH_SHORT).show()
        return
    }

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val viewIntent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, material.mimeType.ifBlank { "*/*" })
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    try {
        context.startActivity(Intent.createChooser(viewIntent, "选择打开方式"))
    } catch (_: ActivityNotFoundException) {
        val fallback = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "*/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(Intent.createChooser(fallback, "选择打开方式")) }
            .onFailure {
                Toast.makeText(context, "没有可用应用打开该文件", Toast.LENGTH_SHORT).show()
            }
    }
}

private fun formatSize(sizeBytes: Long): String {
    if (sizeBytes <= 0L) return "未知大小"
    val kb = sizeBytes / 1024.0
    if (kb < 1024) return String.format(Locale.US, "%.1f KB", kb)
    return String.format(Locale.US, "%.1f MB", kb / 1024.0)
}
