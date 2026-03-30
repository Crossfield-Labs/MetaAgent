package com.ai.assistance.metaagent.ui.features.home.data

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.compose.runtime.mutableStateListOf
import com.ai.assistance.metaagent.data.preferences.UserPreferencesManager
import com.ai.assistance.metaagent.data.preferences.preferencesManager
import com.ai.assistance.metaagent.data.repository.MemoryRepository
import com.ai.assistance.metaagent.util.DocumentConversionUtil
import com.ai.assistance.metaagent.util.AppLogger
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import kotlin.random.Random

enum class StudyRagIndexStatus {
    PENDING,
    INDEXING,
    READY,
    FAILED
}

data class StudyCourseMaterial(
    val id: String,
    val displayName: String,
    val mimeType: String,
    val relativePath: String,
    val sizeBytes: Long,
    val addedAt: String,
    val ragMemoryIds: List<Long>? = emptyList(),
    val ragStatus: StudyRagIndexStatus? = StudyRagIndexStatus.PENDING,
    val ragIndexedAt: String = ""
)

data class CourseMaterialDraft(
    val sourceUri: String,
    val displayName: String,
    val mimeType: String? = null
)

data class StudyCourse(
    val id: String,
    val name: String,
    val category: String,
    val teacher: String,
    val overview: String,
    val progress: Int,
    val sectionCount: Int,
    val materialCount: Int,
    val highlights: List<String>,
    val latestNote: String,
    val materials: List<StudyCourseMaterial>? = emptyList()
)

enum class StudyTaskStatus(val label: String) {
    QUEUED("待执行"),
    RUNNING("执行中"),
    COMPLETED("已完成"),
    FAILED("失败")
}

data class StudyTask(
    val id: String,
    val title: String,
    val courseName: String,
    val summary: String = "",
    val expectedMinutes: Int = 20,
    val detailContent: String = "",
    val stages: List<String> = emptyList(),
    val completedStageCount: Int = 0,
    val promptHint: String = "",
    val status: StudyTaskStatus,
    val progress: Int,
    val statusDetail: String,
    val result: String,
    val createdAt: String,
    val updatedAt: String,
    val materials: List<StudyCourseMaterial>? = emptyList()
)

data class StudyTaskNotification(
    val id: String,
    val taskId: String,
    val title: String,
    val detail: String,
    val timeLabel: String,
    val isRead: Boolean = false
)

object StudyModuleStore {
    private const val TAG = "StudyModuleStore"
    private const val PREFS_NAME = "study_module_demo"
    private const val KEY_COURSES = "courses"
    private const val KEY_TASKS = "tasks"
    private const val KEY_TASK_NOTIFICATIONS = "task_notifications"
    private const val MATERIALS_ROOT = "study_module/materials"
    private const val TASK_MATERIALS_ROOT = "$MATERIALS_ROOT/tasks"
    private const val COURSE_RAG_FOLDER_ROOT = "study/course"
    private const val MAX_RAG_CONTENT_CHARS = 120_000
    private const val MAX_RAG_INDEX_CHARS = 8_000

    private val gson = Gson()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val coursesState = mutableStateListOf<StudyCourse>()
    private val tasksState = mutableStateListOf<StudyTask>()
    private val notificationsState = mutableStateListOf<StudyTaskNotification>()
    private val taskJobs = mutableMapOf<String, Job>()
    private val memoryRepositories = mutableMapOf<String, MemoryRepository>()

    private var initialized = false
    private var appContext: Context? = null

    val courses: List<StudyCourse>
        get() = coursesState

    val tasks: List<StudyTask>
        get() = tasksState

    val taskNotifications: List<StudyTaskNotification>
        get() = notificationsState

    @Synchronized
    fun ensureInitialized(context: Context) {
        if (initialized) {
            return
        }
        appContext = context.applicationContext
        loadOrCreateDemoData()
        initialized = true
    }

    fun getCourse(courseId: String): StudyCourse? {
        return coursesState.firstOrNull { it.id == courseId }
    }

    fun getTask(taskId: String): StudyTask? {
        return tasksState.firstOrNull { it.id == taskId }
    }

    fun getCourseRagFolderPath(courseId: String): String {
        return "$COURSE_RAG_FOLDER_ROOT/$courseId"
    }

    fun importCourse(name: String, category: String): StudyCourse {
        val trimmedName = name.trim()
        require(trimmedName.isNotEmpty()) { "课程名称不能为空" }

        val normalizedCategory = category.ifBlank { "自定义课程" }
        return createCourse(
            name = trimmedName,
            category = normalizedCategory,
            teacher = buildTeacherByCategory(normalizedCategory),
            overview = buildDefaultOverview(trimmedName, normalizedCategory),
            progress = 0,
            sectionCount = 8,
            highlights = listOf("课程导入完成", "已生成默认知识点", "可进入课程问答"),
            latestNote = "导入完成，等待整理",
            materials = emptyList()
        )
    }

    fun createCourse(
        name: String,
        category: String,
        teacher: String,
        overview: String,
        progress: Int,
        sectionCount: Int,
        highlights: List<String>,
        latestNote: String,
        materials: List<CourseMaterialDraft>
    ): StudyCourse {
        val trimmedName = name.trim()
        require(trimmedName.isNotEmpty()) { "课程名称不能为空" }

        val normalizedCategory = category.trim().ifBlank { "自定义课程" }
        val normalizedTeacher = teacher.trim().ifBlank { buildTeacherByCategory(normalizedCategory) }
        val normalizedOverview = overview.trim().ifBlank { buildDefaultOverview(trimmedName, normalizedCategory) }
        val normalizedHighlights = highlights
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .ifEmpty { listOf("课程重点待补充") }

        val courseId = "c_${System.currentTimeMillis()}"
        val persistedMaterials = persistCourseMaterials(courseId, materials)

        val newCourse = StudyCourse(
            id = courseId,
            name = trimmedName,
            category = normalizedCategory,
            teacher = normalizedTeacher,
            overview = normalizedOverview,
            progress = progress.coerceIn(0, 100),
            sectionCount = sectionCount.coerceAtLeast(1),
            materialCount = persistedMaterials.size,
            highlights = normalizedHighlights,
            latestNote = latestNote.trim().ifBlank { "课程已创建，等待整理" },
            materials = persistedMaterials
        )
        coursesState.add(0, newCourse)
        persistCourses()
        persistedMaterials.forEach { material ->
            scheduleCourseMaterialIndexing(courseId, material.id)
        }
        return newCourse
    }

    fun resolveMaterialFile(material: StudyCourseMaterial): File? {
        val context = appContext ?: return null
        if (material.relativePath.isBlank()) {
            return null
        }
        val file = File(context.filesDir, material.relativePath)
        return file.takeIf { it.exists() && it.isFile }
    }

    fun resolveTaskMaterialFile(material: StudyCourseMaterial): File? {
        return resolveMaterialFile(material)
    }

    fun addMaterialsToCourse(courseId: String, materials: List<CourseMaterialDraft>): Int {
        if (materials.isEmpty()) {
            return 0
        }
        val index = coursesState.indexOfFirst { it.id == courseId }
        if (index == -1) {
            return 0
        }

        val currentCourse = coursesState[index]
        val added = persistCourseMaterials(courseId, materials)
        if (added.isEmpty()) {
            return 0
        }

        val currentMaterials = currentCourse.materials.orEmpty()
        val merged = currentMaterials + added
        val baseCount = maxOf(currentCourse.materialCount, currentMaterials.size)

        coursesState[index] = currentCourse.copy(
            materials = merged,
            materialCount = baseCount + added.size
        )
        persistCourses()
        added.forEach { material ->
            scheduleCourseMaterialIndexing(courseId, material.id)
        }
        return added.size
    }

    fun removeCourseMaterial(courseId: String, materialId: String): Boolean {
        val index = coursesState.indexOfFirst { it.id == courseId }
        if (index == -1) {
            return false
        }

        val currentCourse = coursesState[index]
        val currentMaterials = currentCourse.materials.orEmpty()
        val target = currentMaterials.firstOrNull { it.id == materialId } ?: return false
        val remaining = currentMaterials.filterNot { it.id == materialId }

        deleteMaterialFile(target)
        deleteCourseMaterialFromKnowledgeBase(target)
        val baseCount = maxOf(currentCourse.materialCount, currentMaterials.size)
        val newCount = (baseCount - 1).coerceAtLeast(remaining.size)

        coursesState[index] = currentCourse.copy(
            materials = remaining,
            materialCount = newCount
        )
        persistCourses()
        return true
    }

    fun reindexCourseKnowledgeBase(courseId: String): Int {
        val course = coursesState.firstOrNull { it.id == courseId } ?: return 0
        val materials = course.materials.orEmpty()
        if (materials.isEmpty()) {
            return 0
        }
        materials.forEach { material ->
            scheduleCourseMaterialIndexing(courseId, material.id, force = true)
        }
        return materials.size
    }

    fun deleteCourse(courseId: String): Boolean {
        val index = coursesState.indexOfFirst { it.id == courseId }
        if (index == -1) {
            return false
        }

        val course = coursesState[index]
        deleteAllCourseMaterialFiles(course)
        coursesState.removeAt(index)
        persistCourses()
        return true
    }

    fun deleteCategory(category: String): Int {
        if (category.isBlank()) {
            return 0
        }
        val targets = coursesState.filter { it.category == category }
        if (targets.isEmpty()) {
            return 0
        }

        targets.forEach { deleteAllCourseMaterialFiles(it) }
        coursesState.removeAll { it.category == category }
        persistCourses()
        return targets.size
    }

    fun createTask(
        title: String,
        courseName: String,
        summary: String = "",
        expectedMinutes: Int = 20,
        detailContent: String = "",
        stages: List<String> = emptyList(),
        promptHint: String = "",
        materials: List<CourseMaterialDraft> = emptyList()
    ): StudyTask {
        val trimmedTitle = title.trim()
        require(trimmedTitle.isNotEmpty()) { "任务标题不能为空" }

        val normalizedCourseName = courseName.ifBlank { "通用任务" }
        val normalizedStages = stages
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .ifEmpty { defaultStagesForTask(trimmedTitle) }
        val normalizedSummary = summary.trim().ifBlank { "围绕《$normalizedCourseName》执行的整理任务。" }
        val normalizedDetail = detailContent.trim().ifBlank { normalizedSummary }

        val taskId = "t_${System.currentTimeMillis()}_${Random.nextInt(100, 999)}"
        val persistedMaterials = persistTaskMaterials(taskId, materials)
        val now = nowLabel()
        val task = StudyTask(
            id = taskId,
            title = trimmedTitle,
            courseName = normalizedCourseName,
            summary = normalizedSummary,
            expectedMinutes = expectedMinutes.coerceIn(5, 480),
            detailContent = normalizedDetail,
            stages = normalizedStages,
            completedStageCount = 0,
            promptHint = promptHint.trim(),
            status = StudyTaskStatus.QUEUED,
            progress = calculateStageProgress(0, normalizedStages.size),
            statusDetail = "任务已创建，等待执行",
            result = "",
            createdAt = now,
            updatedAt = now,
            materials = persistedMaterials
        )
        tasksState.add(0, task)
        persistTasks()
        return task
    }

    fun addMaterialsToTask(taskId: String, materials: List<CourseMaterialDraft>): Int {
        if (materials.isEmpty()) {
            return 0
        }
        val index = tasksState.indexOfFirst { it.id == taskId }
        if (index == -1) {
            return 0
        }

        val currentTask = tasksState[index]
        val added = persistTaskMaterials(taskId, materials)
        if (added.isEmpty()) {
            return 0
        }

        val merged = currentTask.materials.orEmpty() + added
        tasksState[index] = currentTask.copy(
            materials = merged,
            updatedAt = nowLabel()
        )
        persistTasks()
        return added.size
    }

    fun removeTaskMaterial(taskId: String, materialId: String): Boolean {
        val index = tasksState.indexOfFirst { it.id == taskId }
        if (index == -1) {
            return false
        }
        val currentTask = tasksState[index]
        val currentMaterials = currentTask.materials.orEmpty()
        val target = currentMaterials.firstOrNull { it.id == materialId } ?: return false
        val remaining = currentMaterials.filterNot { it.id == materialId }

        deleteMaterialFile(target)
        tasksState[index] = currentTask.copy(
            materials = remaining,
            updatedAt = nowLabel()
        )
        persistTasks()
        return true
    }

    fun startTaskExecution(taskId: String): Boolean {
        val target = getTask(taskId) ?: return false
        if (target.status == StudyTaskStatus.RUNNING) {
            return true
        }
        simulateTaskProgress(taskId)
        return true
    }

    fun markAllTaskNotificationsRead() {
        if (notificationsState.isEmpty()) {
            return
        }
        var changed = false
        for (index in notificationsState.indices) {
            val item = notificationsState[index]
            if (!item.isRead) {
                notificationsState[index] = item.copy(isRead = true)
                changed = true
            }
        }
        if (changed) {
            persistTaskNotifications()
        }
    }

    fun markTaskNotificationRead(notificationId: String) {
        val index = notificationsState.indexOfFirst { it.id == notificationId }
        if (index == -1) {
            return
        }
        val target = notificationsState[index]
        if (target.isRead) {
            return
        }
        notificationsState[index] = target.copy(isRead = true)
        persistTaskNotifications()
    }

    fun buildCourseAnswer(course: StudyCourse, question: String): String {
        val q = question.lowercase()
        val highlights = course.highlights.ifEmpty { listOf("课程核心概念") }
        return when {
            q.contains("重点") || q.contains("考点") || q.contains("关键") -> {
                val point = highlights.joinToString("；")
                "基于《${course.name}》的课程资料，当前重点是：$point。建议先复习第1-2章核心概念，再做对应习题。"
            }

            q.contains("实验") || q.contains("作业") || q.contains("练习") -> {
                "结合《${course.name}》当前进度，建议先完成一个最小实验：实现基础案例并记录结果，再提交整理任务让系统生成报告。"
            }

            q.contains("总结") || q.contains("复习") || q.contains("怎么学") -> {
                "《${course.name}》推荐复习路径：1) 先看课程概览；2) 回顾最近笔记“${course.latestNote}”；3) 针对薄弱点进行5-10分钟快练。"
            }

            q.contains("进度") || q.contains("完成") -> {
                "《${course.name}》当前学习进度约 ${course.progress}% ，你可以先在“一键整理”新建任务，系统会异步生成阶段结果。"
            }

            else -> {
                "已在《${course.name}》课程上下文内处理你的问题。当前可参考：${highlights.firstOrNull() ?: "课程核心概念"}。如果你告诉我具体章节，我会给你更精准的答案。"
            }
        }
    }

    private fun simulateTaskProgress(taskId: String) {
        taskJobs.remove(taskId)?.cancel()
        val job = scope.launch {
            val seedTask = getTask(taskId) ?: return@launch
            val stages = seedTask.stages.ifEmpty { defaultStagesForTask(seedTask.title) }

            updateTask(taskId) {
                it.copy(
                    status = StudyTaskStatus.RUNNING,
                    stages = stages,
                    completedStageCount = 0,
                    progress = calculateStageProgress(0, stages.size),
                    statusDetail = "正在执行：${stages.firstOrNull() ?: "准备阶段"}",
                    updatedAt = nowLabel()
                )
            }

            stages.forEachIndexed { index, stage ->
                delay(1300)
                updateTask(taskId) {
                    val completedCount = (index + 1).coerceAtMost(stages.size)
                    val nextStage = stages.getOrNull(index + 1)
                    it.copy(
                        status = StudyTaskStatus.RUNNING,
                        completedStageCount = completedCount,
                        progress = calculateStageProgress(completedCount, stages.size),
                        statusDetail = if (nextStage != null) {
                            "已完成 ${completedCount}/${stages.size}：$stage；正在执行：$nextStage"
                        } else {
                            "已完成 ${completedCount}/${stages.size}：$stage"
                        },
                        updatedAt = nowLabel()
                    )
                }
            }

            delay(900)
            updateTask(taskId) {
                it.copy(
                    status = StudyTaskStatus.COMPLETED,
                    completedStageCount = it.stages.size,
                    progress = 100,
                    statusDetail = "任务已完成",
                    result = buildTaskResult(it),
                    updatedAt = nowLabel()
                )
            }
        }
        taskJobs[taskId] = job
    }

    private fun updateTask(taskId: String, update: (StudyTask) -> StudyTask) {
        val index = tasksState.indexOfFirst { it.id == taskId }
        if (index == -1) {
            return
        }
        val oldTask = tasksState[index]
        val newTask = update(oldTask)
        tasksState[index] = newTask
        persistTasks()
        if (oldTask.status != StudyTaskStatus.COMPLETED && newTask.status == StudyTaskStatus.COMPLETED) {
            appendTaskCompletionNotification(newTask)
        }
    }

    private fun loadOrCreateDemoData() {
        val loadedCourses = loadCourses()
        val loadedTasks = loadTasks()
        val loadedNotifications = loadTaskNotifications()

        if (!loadedCourses.isNullOrEmpty()) {
            coursesState.clear()
            coursesState.addAll(loadedCourses.map { normalizeCourse(it) })
        } else {
            coursesState.clear()
            coursesState.addAll(defaultCourses())
            persistCourses()
        }
        schedulePendingCourseKnowledgeBaseIndexing()

        if (!loadedTasks.isNullOrEmpty()) {
            tasksState.clear()
            val normalizedTasks = loadedTasks.mapNotNull { rawTask ->
                runCatching { normalizeTask(rawTask) }.getOrNull()
            }
            if (normalizedTasks.isNotEmpty()) {
                tasksState.addAll(normalizedTasks)
            } else {
                tasksState.addAll(defaultTasks())
            }
            persistTasks()
        } else {
            tasksState.clear()
            tasksState.addAll(defaultTasks())
            persistTasks()
        }

        notificationsState.clear()
        if (!loadedNotifications.isNullOrEmpty()) {
            notificationsState.addAll(loadedNotifications.map { normalizeNotification(it) })
        }
    }

    private fun loadCourses(): List<StudyCourse>? {
        val json = prefs().getString(KEY_COURSES, null) ?: return null
        return runCatching {
            val type = object : TypeToken<List<StudyCourse>>() {}.type
            gson.fromJson<List<StudyCourse>>(json, type)
        }.getOrNull()
    }

    private fun loadTasks(): List<StudyTask>? {
        val json = prefs().getString(KEY_TASKS, null) ?: return null
        return runCatching {
            val type = object : TypeToken<List<StudyTask>>() {}.type
            gson.fromJson<List<StudyTask>>(json, type)
        }.getOrNull()
    }

    private fun loadTaskNotifications(): List<StudyTaskNotification>? {
        val json = prefs().getString(KEY_TASK_NOTIFICATIONS, null) ?: return null
        return runCatching {
            val type = object : TypeToken<List<StudyTaskNotification>>() {}.type
            gson.fromJson<List<StudyTaskNotification>>(json, type)
        }.getOrNull()
    }

    private fun persistCourses() {
        prefs().edit().putString(KEY_COURSES, gson.toJson(coursesState)).apply()
    }

    private fun persistTasks() {
        prefs().edit().putString(KEY_TASKS, gson.toJson(tasksState)).apply()
    }

    private fun persistTaskNotifications() {
        prefs().edit().putString(KEY_TASK_NOTIFICATIONS, gson.toJson(notificationsState)).apply()
    }

    private fun persistCourseMaterials(
        courseId: String,
        materials: List<CourseMaterialDraft>
    ): List<StudyCourseMaterial> {
        val context = appContext ?: return emptyList()
        if (materials.isEmpty()) {
            return emptyList()
        }

        val targetDir = File(context.filesDir, "$MATERIALS_ROOT/$courseId")
        if (!targetDir.exists() && !targetDir.mkdirs()) {
            return emptyList()
        }

        val resolver = context.contentResolver
        val savedMaterials = mutableListOf<StudyCourseMaterial>()

        materials.forEachIndexed { index, draft ->
            val uri = runCatching { Uri.parse(draft.sourceUri) }.getOrNull() ?: return@forEachIndexed
            val originalName = draft.displayName.ifBlank { "课程资料_${index + 1}" }
            val uniqueFileName = ensureUniqueFileName(targetDir, sanitizeFileName(originalName))
            val targetFile = File(targetDir, uniqueFileName)

            runCatching {
                resolver.openInputStream(uri)?.use { input ->
                    targetFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                } ?: throw IOException("无法读取源文件")

                savedMaterials += StudyCourseMaterial(
                    id = "m_${System.currentTimeMillis()}_${Random.nextInt(100, 999)}_$index",
                    displayName = originalName,
                    mimeType = draft.mimeType?.takeIf { it.isNotBlank() } ?: guessMimeType(originalName),
                    relativePath = targetFile.relativeTo(context.filesDir).invariantSeparatorsPath,
                    sizeBytes = targetFile.length(),
                    addedAt = nowLabel()
                )
            }.onFailure {
                targetFile.delete()
            }
        }
        return savedMaterials
    }

    private fun persistTaskMaterials(
        taskId: String,
        materials: List<CourseMaterialDraft>
    ): List<StudyCourseMaterial> {
        val context = appContext ?: return emptyList()
        if (materials.isEmpty()) {
            return emptyList()
        }

        val targetDir = File(context.filesDir, "$TASK_MATERIALS_ROOT/$taskId")
        if (!targetDir.exists() && !targetDir.mkdirs()) {
            return emptyList()
        }

        val resolver = context.contentResolver
        val savedMaterials = mutableListOf<StudyCourseMaterial>()

        materials.forEachIndexed { index, draft ->
            val uri = runCatching { Uri.parse(draft.sourceUri) }.getOrNull() ?: return@forEachIndexed
            val originalName = draft.displayName.ifBlank { "任务附件_${index + 1}" }
            val uniqueFileName = ensureUniqueFileName(targetDir, sanitizeFileName(originalName))
            val targetFile = File(targetDir, uniqueFileName)

            runCatching {
                resolver.openInputStream(uri)?.use { input ->
                    targetFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                } ?: throw IOException("无法读取源文件")

                savedMaterials += StudyCourseMaterial(
                    id = "tm_${System.currentTimeMillis()}_${Random.nextInt(100, 999)}_$index",
                    displayName = originalName,
                    mimeType = draft.mimeType?.takeIf { it.isNotBlank() } ?: guessMimeType(originalName),
                    relativePath = targetFile.relativeTo(context.filesDir).invariantSeparatorsPath,
                    sizeBytes = targetFile.length(),
                    addedAt = nowLabel()
                )
            }.onFailure {
                targetFile.delete()
            }
        }
        return savedMaterials
    }

    private fun deleteMaterialFile(material: StudyCourseMaterial) {
        val context = appContext ?: return
        if (material.relativePath.isBlank()) {
            return
        }
        val file = File(context.filesDir, material.relativePath)
        if (file.exists()) {
            file.delete()
        }
        val parent = file.parentFile
        if (parent != null && parent.exists() && parent.isDirectory && parent.listFiles().isNullOrEmpty()) {
            parent.delete()
        }
    }

    private fun deleteAllCourseMaterialFiles(course: StudyCourse) {
        val context = appContext ?: return
        course.materials.orEmpty().forEach { material ->
            deleteCourseMaterialFromKnowledgeBase(material)
        }
        val courseDir = File(context.filesDir, "$MATERIALS_ROOT/${course.id}")
        if (courseDir.exists()) {
            courseDir.deleteRecursively()
            return
        }
        course.materials.orEmpty().forEach { deleteMaterialFile(it) }
    }

    private fun schedulePendingCourseKnowledgeBaseIndexing() {
        coursesState.forEach { course ->
            course.materials.orEmpty().forEach { material ->
                if (material.ragStatus != StudyRagIndexStatus.READY || material.ragMemoryIds.orEmpty().isEmpty()) {
                    scheduleCourseMaterialIndexing(course.id, material.id)
                } else {
                    scope.launch(Dispatchers.IO) {
                        if (isLegacyDocumentRagMaterial(material)) {
                            scheduleCourseMaterialIndexing(course.id, material.id, force = true)
                        }
                    }
                }
            }
        }
    }

    private suspend fun isLegacyDocumentRagMaterial(material: StudyCourseMaterial): Boolean {
        val memoryId = material.ragMemoryIds.orEmpty().firstOrNull() ?: return false
        val context = appContext ?: return false
        val repository = runCatching { getMemoryRepository(context) }.getOrNull() ?: return false
        val memory = runCatching { repository.findMemoryById(memoryId) }.getOrNull() ?: return false
        return memory.isDocumentNode
    }

    private fun scheduleCourseMaterialIndexing(courseId: String, materialId: String, force: Boolean = false) {
        scope.launch(Dispatchers.IO) {
            val context = appContext ?: return@launch
            val (courseSnapshot, materialSnapshot) = withContext(Dispatchers.Main.immediate) {
                val course = coursesState.firstOrNull { it.id == courseId }
                val material = course?.materials.orEmpty().firstOrNull { it.id == materialId }
                course to material
            }
            val safeCourse = courseSnapshot ?: return@launch
            val safeMaterial = materialSnapshot ?: return@launch

            if (!force &&
                safeMaterial.ragStatus == StudyRagIndexStatus.READY &&
                safeMaterial.ragMemoryIds.orEmpty().isNotEmpty()
            ) {
                return@launch
            }

            withContext(Dispatchers.Main.immediate) {
                updateCourseMaterial(courseId, materialId) { current ->
                    current.copy(ragStatus = StudyRagIndexStatus.INDEXING)
                }
            }

            val file = resolveMaterialFile(safeMaterial)
            if (file == null || !file.exists()) {
                withContext(Dispatchers.Main.immediate) {
                    updateCourseMaterial(courseId, materialId) { current ->
                        current.copy(ragStatus = StudyRagIndexStatus.FAILED)
                    }
                }
                return@launch
            }

            val extractedText = extractMaterialTextForRag(context, file, safeMaterial)
            if (extractedText.isBlank()) {
                withContext(Dispatchers.Main.immediate) {
                    updateCourseMaterial(courseId, materialId) { current ->
                        current.copy(ragStatus = StudyRagIndexStatus.FAILED)
                    }
                }
                return@launch
            }

            val repository = runCatching { getMemoryRepository(context) }.getOrNull()
            if (repository == null) {
                withContext(Dispatchers.Main.immediate) {
                    updateCourseMaterial(courseId, materialId) { current ->
                        current.copy(ragStatus = StudyRagIndexStatus.FAILED)
                    }
                }
                return@launch
            }

            runCatching {
                safeMaterial.ragMemoryIds.orEmpty().forEach { memoryId ->
                    repository.deleteMemoryAndIndex(memoryId)
                }
            }

            val memoryId = runCatching {
                val courseName = withContext(Dispatchers.Main.immediate) {
                    coursesState.firstOrNull { it.id == courseId }?.name
                } ?: safeCourse.name
                val memory = repository.createMemory(
                    title = "${courseName} - ${safeMaterial.displayName}",
                    content = extractedText.take(MAX_RAG_INDEX_CHARS),
                    contentType = safeMaterial.mimeType.ifBlank { "text/plain" },
                    source = "study_course_material",
                    folderPath = getCourseRagFolderPath(courseId)
                )
                memory?.id
            }.onFailure { error ->
                AppLogger.e(TAG, "Failed to index course material for RAG: ${safeMaterial.displayName}", error)
            }.getOrNull()

            withContext(Dispatchers.Main.immediate) {
                updateCourseMaterial(courseId, materialId) { current ->
                    if (memoryId != null) {
                        current.copy(
                            ragMemoryIds = listOf(memoryId),
                            ragStatus = StudyRagIndexStatus.READY,
                            ragIndexedAt = nowLabel()
                        )
                    } else {
                        current.copy(ragStatus = StudyRagIndexStatus.FAILED)
                    }
                }
            }
        }
    }

    private fun updateCourseMaterial(
        courseId: String,
        materialId: String,
        transform: (StudyCourseMaterial) -> StudyCourseMaterial
    ) {
        val courseIndex = coursesState.indexOfFirst { it.id == courseId }
        if (courseIndex == -1) {
            return
        }
        val currentCourse = coursesState[courseIndex]
        val materials = currentCourse.materials.orEmpty()
        val materialIndex = materials.indexOfFirst { it.id == materialId }
        if (materialIndex == -1) {
            return
        }
        val updatedMaterials = materials.toMutableList()
        updatedMaterials[materialIndex] = transform(updatedMaterials[materialIndex])
        coursesState[courseIndex] = currentCourse.copy(materials = updatedMaterials)
        persistCourses()
    }

    private fun deleteCourseMaterialFromKnowledgeBase(material: StudyCourseMaterial) {
        val memoryIds = material.ragMemoryIds.orEmpty()
        if (memoryIds.isEmpty()) {
            return
        }
        val context = appContext ?: return
        scope.launch(Dispatchers.IO) {
            val repository = runCatching { getMemoryRepository(context) }.getOrNull() ?: return@launch
            memoryIds.forEach { memoryId ->
                runCatching { repository.deleteMemoryAndIndex(memoryId) }
                    .onFailure { error ->
                        AppLogger.e(TAG, "Failed to delete RAG memory id=$memoryId", error)
                    }
            }
        }
    }

    private fun getMemoryRepository(context: Context): MemoryRepository {
        val profileId = runCatching {
            UserPreferencesManager.getInstance(context)
            runBlocking { preferencesManager.activeProfileIdFlow.first() }
        }.getOrDefault("default")
        return synchronized(memoryRepositories) {
            memoryRepositories.getOrPut(profileId) { MemoryRepository(context, profileId) }
        }
    }

    private fun extractMaterialTextForRag(
        context: Context,
        file: File,
        material: StudyCourseMaterial
    ): String {
        val extension = file.extension.lowercase()
        val mimeType = material.mimeType.lowercase()
        return when {
            mimeType.startsWith("text/") ||
                extension in setOf("txt", "md", "markdown", "json", "csv", "tsv", "xml", "html", "htm", "kt", "java", "py", "js", "ts", "sql", "log") ->
                readTextSafely(file)
            extension == "pdf" || mimeType.contains("pdf") ->
                extractPdfText(context, file)
            extension == "doc" || extension == "docx" ||
                mimeType.contains("msword") || mimeType.contains("officedocument.wordprocessingml") ->
                extractWordText(context, file, extension.ifBlank { if (mimeType.contains("docx")) "docx" else "doc" })
            else -> readTextSafely(file)
        }
    }

    private fun extractPdfText(context: Context, sourceFile: File): String {
        val tempFile = File(context.cacheDir, "course_rag_pdf_${System.currentTimeMillis()}.txt")
        return try {
            if (!DocumentConversionUtil.extractTextFromPdf(context, sourceFile, tempFile)) {
                ""
            } else {
                readTextSafely(tempFile)
            }
        } finally {
            runCatching { tempFile.delete() }
        }
    }

    private fun extractWordText(context: Context, sourceFile: File, extension: String): String {
        val tempFile = File(context.cacheDir, "course_rag_word_${System.currentTimeMillis()}.txt")
        return try {
            if (!DocumentConversionUtil.extractTextFromWord(sourceFile, tempFile, extension)) {
                ""
            } else {
                readTextSafely(tempFile)
            }
        } finally {
            runCatching { tempFile.delete() }
        }
    }

    private fun readTextSafely(file: File): String {
        return runCatching {
            val bytes = file.readBytes()
            if (bytes.isEmpty()) {
                ""
            } else {
                decodeTextWithBestCharset(bytes)
                    .take(MAX_RAG_CONTENT_CHARS)
                    .trim()
            }
        }.getOrElse { error ->
            AppLogger.e(TAG, "Failed to read text file for RAG: ${file.name}", error)
            ""
        }
    }

    private fun decodeTextWithBestCharset(bytes: ByteArray): String {
        val candidates = listOf("UTF-8", "GB18030", "GBK", "UTF-16LE", "UTF-16BE")
        var bestText = ""
        var bestScore = Int.MIN_VALUE

        candidates.forEach { charsetName ->
            val text = runCatching { String(bytes, Charset.forName(charsetName)) }.getOrNull() ?: return@forEach
            val score = scoreDecodedText(text)
            if (score > bestScore) {
                bestScore = score
                bestText = text
            }
        }

        if (bestText.isNotBlank()) {
            return bestText
        }
        return String(bytes, Charsets.UTF_8)
    }

    private fun scoreDecodedText(text: String): Int {
        val normalized = text.trim()
        if (normalized.isEmpty()) {
            return Int.MIN_VALUE / 4
        }
        val chineseCount = normalized.count { it.code in 0x4E00..0x9FFF }
        val asciiCount = normalized.count { it in ' '..'~' }
        val replacementCount = normalized.count { it == '\uFFFD' }
        val controlCount = normalized.count {
            it.isISOControl() && it != '\n' && it != '\r' && it != '\t'
        }
        return chineseCount * 4 + asciiCount - replacementCount * 40 - controlCount * 30
    }

    private fun normalizeCourse(course: StudyCourse): StudyCourse {
        val normalizedHighlights = course.highlights
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .ifEmpty { listOf("课程重点待补充") }
        val normalizedMaterials = course.materials.orEmpty()
            .filter { it.relativePath.isNotBlank() && it.displayName.isNotBlank() }
            .map { material ->
                val memoryIds = material.ragMemoryIds.orEmpty().filter { it > 0L }
                val rawStatus = material.ragStatus ?: StudyRagIndexStatus.PENDING
                val status = when {
                    memoryIds.isNotEmpty() -> StudyRagIndexStatus.READY
                    rawStatus == StudyRagIndexStatus.INDEXING -> StudyRagIndexStatus.PENDING
                    else -> rawStatus
                }
                material.copy(
                    ragMemoryIds = memoryIds,
                    ragStatus = status
                )
            }

        return course.copy(
            category = course.category.ifBlank { "自定义课程" },
            teacher = course.teacher.ifBlank { buildTeacherByCategory(course.category) },
            overview = course.overview.ifBlank { buildDefaultOverview(course.name, course.category) },
            progress = course.progress.coerceIn(0, 100),
            sectionCount = course.sectionCount.coerceAtLeast(1),
            materialCount = maxOf(course.materialCount, normalizedMaterials.size),
            highlights = normalizedHighlights,
            latestNote = course.latestNote.ifBlank { "课程资料待补充" },
            materials = normalizedMaterials
        )
    }

    private fun normalizeTask(task: StudyTask): StudyTask {
        val safeId = runCatching { task.id }.getOrNull().orEmpty().ifBlank {
            "t_recovered_${System.currentTimeMillis()}_${Random.nextInt(100, 999)}"
        }
        val safeTitle = runCatching { task.title }.getOrNull().orEmpty().ifBlank { "未命名任务" }
        val safeCourseName = runCatching { task.courseName }.getOrNull().orEmpty().ifBlank { "通用任务" }
        val safeStatus = runCatching { task.status }.getOrNull() ?: StudyTaskStatus.QUEUED
        val safeCreatedAt = runCatching { task.createdAt }.getOrNull().orEmpty().ifBlank { nowLabel() }
        val safeUpdatedAt = runCatching { task.updatedAt }.getOrNull().orEmpty().ifBlank { safeCreatedAt }
        val safeStatusDetailRaw = runCatching { task.statusDetail }.getOrNull().orEmpty()
        val safeSummaryRaw = runCatching { task.summary }.getOrNull().orEmpty()
        val safeDetailRaw = runCatching { task.detailContent }.getOrNull().orEmpty()
        val safePromptRaw = runCatching { task.promptHint }.getOrNull().orEmpty()
        val safeResultRaw = runCatching { task.result }.getOrNull().orEmpty()
        val safeExpectedMinutes = runCatching { task.expectedMinutes }.getOrDefault(20)
        val safeProgressRaw = runCatching { task.progress }.getOrDefault(0)
        val safeCompletedStageCount = runCatching { task.completedStageCount }.getOrDefault(0)
        val rawMaterials = runCatching { task.materials }.getOrNull().orEmpty()

        val rawStages = runCatching { task.stages }.getOrNull().orEmpty()
        val normalizedStages = rawStages
            .map { it.toString().trim() }
            .filter { it.isNotEmpty() }
            .ifEmpty { defaultStagesForTask(safeTitle) }
        val normalizedMaterials = rawMaterials
            .filter { it.relativePath.isNotBlank() && it.displayName.isNotBlank() }

        val inferredCompleted = if (safeCompletedStageCount <= 0 && safeProgressRaw > 0 && normalizedStages.isNotEmpty()) {
            ((safeProgressRaw / 100f) * normalizedStages.size).roundToInt()
        } else {
            safeCompletedStageCount
        }

        val completedCount = when (safeStatus) {
            StudyTaskStatus.COMPLETED -> normalizedStages.size
            else -> inferredCompleted.coerceIn(0, normalizedStages.size)
        }

        val normalizedProgress = when (safeStatus) {
            StudyTaskStatus.COMPLETED -> 100
            else -> calculateStageProgress(completedCount, normalizedStages.size)
        }

        val summary = safeSummaryRaw.trim().ifBlank { "围绕《$safeCourseName》执行的整理任务。" }
        val detailContent = safeDetailRaw.trim().ifBlank { summary }

        val statusDetail = safeStatusDetailRaw.ifBlank {
            when (safeStatus) {
                StudyTaskStatus.QUEUED -> "任务已创建，等待执行"
                StudyTaskStatus.RUNNING -> {
                    val currentStage = normalizedStages.getOrNull(completedCount) ?: normalizedStages.lastOrNull() ?: "执行阶段"
                    "正在执行：$currentStage"
                }
                StudyTaskStatus.COMPLETED -> "任务已完成"
                StudyTaskStatus.FAILED -> "任务执行失败"
            }
        }

        val result = if (safeStatus == StudyTaskStatus.COMPLETED && safeResultRaw.isBlank()) {
            buildTaskResult(task.copy(title = safeTitle, courseName = safeCourseName, stages = normalizedStages))
        } else {
            safeResultRaw
        }

        return task.copy(
            id = safeId,
            title = safeTitle,
            courseName = safeCourseName,
            status = safeStatus,
            summary = summary,
            expectedMinutes = safeExpectedMinutes.coerceIn(5, 480),
            detailContent = detailContent,
            stages = normalizedStages,
            completedStageCount = completedCount,
            promptHint = safePromptRaw.trim(),
            progress = normalizedProgress,
            statusDetail = statusDetail,
            result = result,
            createdAt = safeCreatedAt,
            updatedAt = safeUpdatedAt,
            materials = normalizedMaterials
        )
    }

    private fun normalizeNotification(notification: StudyTaskNotification): StudyTaskNotification {
        val safeId = runCatching { notification.id }.getOrNull().orEmpty().ifBlank {
            "n_${System.currentTimeMillis()}_${Random.nextInt(100, 999)}"
        }
        val safeTaskId = runCatching { notification.taskId }.getOrNull().orEmpty()
        val safeTitle = runCatching { notification.title }.getOrNull().orEmpty().ifBlank { "任务通知" }
        val safeDetail = runCatching { notification.detail }.getOrNull().orEmpty().ifBlank { "任务状态更新" }
        val safeTimeLabel = runCatching { notification.timeLabel }.getOrNull().orEmpty().ifBlank { nowLabel() }
        val safeRead = runCatching { notification.isRead }.getOrDefault(false)
        return notification.copy(
            id = safeId,
            taskId = safeTaskId,
            title = safeTitle,
            detail = safeDetail,
            timeLabel = safeTimeLabel,
            isRead = safeRead
        )
    }

    private fun appendTaskCompletionNotification(task: StudyTask) {
        val notice = StudyTaskNotification(
            id = "n_${System.currentTimeMillis()}_${Random.nextInt(100, 999)}",
            taskId = task.id,
            title = "任务已完成",
            detail = "《${task.title}》执行完成，可查看结果与产物。",
            timeLabel = nowLabel(),
            isRead = false
        )
        notificationsState.add(0, notice)
        if (notificationsState.size > 60) {
            while (notificationsState.size > 60) {
                notificationsState.removeAt(notificationsState.lastIndex)
            }
        }
        persistTaskNotifications()
    }

    private fun calculateStageProgress(completedCount: Int, totalCount: Int): Int {
        if (totalCount <= 0) {
            return 0
        }
        return ((completedCount.coerceIn(0, totalCount).toFloat() / totalCount) * 100f).roundToInt()
            .coerceIn(0, 100)
    }

    private fun defaultStagesForTask(taskTitle: String): List<String> {
        return listOf(
            "阅读并理解任务目标（$taskTitle）",
            "准备执行环境与资料",
            "执行主要操作与产出",
            "验证结果并记录问题",
            "整理最终结论与提交材料"
        )
    }

    private fun buildTaskResult(task: StudyTask): String {
        val stageCount = task.stages.size
        val promptPart = if (task.promptHint.isBlank()) {
            "已按默认策略完成。"
        } else {
            "已应用提示词约束：${task.promptHint.take(24)}${if (task.promptHint.length > 24) "..." else ""}"
        }
        return "已输出：${task.title}（Demo）· 完成 $stageCount 个阶段，$promptPart"
    }

    private fun prefs() = requireNotNull(appContext).getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun nowLabel(): String {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM-dd HH:mm"))
    }

    private fun buildTeacherByCategory(category: String): String {
        return when {
            category.contains("软件") -> "王老师"
            category.contains("计算机") -> "李老师"
            category.contains("AI", ignoreCase = true) || category.contains("智能") -> "陈老师"
            else -> "课程助教"
        }
    }

    private fun buildDefaultOverview(courseName: String, category: String): String {
        return "《$courseName》属于$category，已完成基础信息创建。你可以上传课程资料后，使用 AI 自动补全课程简介与重点。"
    }

    private fun sanitizeFileName(fileName: String): String {
        val cleaned = fileName.trim().replace(Regex("[\\\\/:*?\"<>|]"), "_")
        return if (cleaned.isBlank()) "course_material" else cleaned
    }

    private fun ensureUniqueFileName(dir: File, desiredName: String): String {
        if (!File(dir, desiredName).exists()) {
            return desiredName
        }
        val dotIndex = desiredName.lastIndexOf('.')
        val baseName = if (dotIndex > 0) desiredName.substring(0, dotIndex) else desiredName
        val ext = if (dotIndex > 0) desiredName.substring(dotIndex) else ""

        var index = 1
        var candidate = "${baseName}_$index$ext"
        while (File(dir, candidate).exists()) {
            index++
            candidate = "${baseName}_$index$ext"
        }
        return candidate
    }

    private fun guessMimeType(fileName: String): String {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        if (extension.isBlank()) {
            return "application/octet-stream"
        }
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            ?: "application/octet-stream"
    }

    private fun defaultCourses(): List<StudyCourse> {
        return listOf(
            StudyCourse(
                id = "c_sw_arch",
                name = "软件体系结构",
                category = "软件工程",
                teacher = "王老师",
                overview = "覆盖架构风格、设计原则和系统演进，支持课程内快速问答演示。",
                progress = 45,
                sectionCount = 12,
                materialCount = 26,
                highlights = listOf("分层架构", "观察者模式", "高可用设计"),
                latestNote = "已整理观察者模式优缺点",
                materials = emptyList()
            ),
            StudyCourse(
                id = "c_network",
                name = "计算机网络基础",
                category = "计算机基础",
                teacher = "李老师",
                overview = "聚焦传输层与网络层核心概念，便于演示导入后统一管理。",
                progress = 32,
                sectionCount = 10,
                materialCount = 19,
                highlights = listOf("TCP拥塞控制", "IP分片", "HTTP缓存"),
                latestNote = "已完成TCP三次握手复习",
                materials = emptyList()
            ),
            StudyCourse(
                id = "c_ai_practice",
                name = "深度学习实践",
                category = "人工智能",
                teacher = "陈老师",
                overview = "包含CNN实验、训练日志分析和模型调优建议。",
                progress = 68,
                sectionCount = 15,
                materialCount = 31,
                highlights = listOf("CNN图像分类", "过拟合处理", "实验报告模板"),
                latestNote = "CNN实验准确率提升到93%",
                materials = emptyList()
            )
        )
    }

    private fun defaultTasks(): List<StudyTask> {
        val now = nowLabel()
        val runningStages = listOf(
            "阅读并理解实验要求",
            "准备实验环境",
            "编写并调试核心代码",
            "测试运行并记录结果",
            "撰写实验报告"
        )
        val completedStages = listOf(
            "阅读章节并提炼重点",
            "整理结构化笔记",
            "补充示例与图表",
            "完成复习建议清单"
        )
        return listOf(
            StudyTask(
                id = "t_seed_1",
                title = "CNN图像分类实验整理",
                courseName = "深度学习实践",
                summary = "把 CNN 图像分类实验整理为可复现实验记录。",
                expectedMinutes = 45,
                detailContent = "目标是形成一份可复现的实验整理结果，包含环境说明、核心代码、测试结论与报告要点。",
                stages = runningStages,
                completedStageCount = 2,
                promptHint = "优先保证可复现性，重点标注训练参数与结果对比。",
                status = StudyTaskStatus.RUNNING,
                progress = calculateStageProgress(2, runningStages.size),
                statusDetail = "已完成 2/5：准备实验环境；正在执行：编写并调试核心代码",
                result = "",
                createdAt = now,
                updatedAt = now
            ),
            StudyTask(
                id = "t_seed_2",
                title = "软件体系结构章节速记",
                courseName = "软件体系结构",
                summary = "输出一份章节速记，便于期末前快速复习。",
                expectedMinutes = 20,
                detailContent = "聚焦分层架构、观察者模式和高可用设计，提炼核心概念并给出复习顺序建议。",
                stages = completedStages,
                completedStageCount = completedStages.size,
                promptHint = "尽量用短句，保证手机端阅读友好。",
                status = StudyTaskStatus.COMPLETED,
                progress = 100,
                statusDetail = "任务已完成",
                result = "已输出：核心概念摘要 + 3条复习建议（Demo）",
                createdAt = now,
                updatedAt = now
            )
        )
    }
}
