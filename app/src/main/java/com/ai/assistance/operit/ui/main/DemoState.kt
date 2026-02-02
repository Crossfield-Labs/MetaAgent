package com.ai.assistance.operit.ui.main

import android.content.Context
import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Code
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.documentfile.provider.DocumentFile

/**
 * 文件项数据模型
 * 支持真实文件系统和演示数据
 */
data class FileItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val type: String, // 文档、图片、代码、音频、视频 etc.
    val tags: List<String> = emptyList(),
    val color: Color = Color(0xFFE0E0E0),
    val icon: ImageVector = Icons.Default.InsertDriveFile,
    // 真实文件属性
    val fullPath: String? = null,
    val uri: Uri? = null,
    val mimeType: String? = null,
    val isDirectory: Boolean = false,
    val size: Long = 0,
    val lastModified: Long = 0,
    val isSelected: Boolean = false
)

data class SkillItem(
    val id: String,
    val name: String,
    val desc: String,
    val icon: ImageVector,
    val tag: String,
    val bgColor: Color
)

/**
 * 文件与技能管理控制器
 */
class DemoController {
    val files = mutableStateListOf<FileItem>()
    val skills = mutableStateListOf<SkillItem>()
    
    // 多选状态
    val isSelectionMode = mutableStateOf(false)
    val selectedFiles = mutableStateListOf<FileItem>()

    init {
        files.addAll(getInitialFiles())
        skills.addAll(getInitialSkills())
    }

    // ===== 文件操作 =====
    
    fun addFile(file: FileItem) {
        files.add(0, file)
    }
    
    fun removeFile(file: FileItem) {
        files.remove(file)
        selectedFiles.remove(file)
    }
    
    fun clearFiles() {
        files.clear()
        selectedFiles.clear()
        isSelectionMode.value = false
    }
    
    /**
     * 从 DocumentFile 加载文件夹内容
     */
    fun loadFromFolder(context: Context, folderUri: Uri) {
        val documentFile = DocumentFile.fromTreeUri(context, folderUri) ?: return
        val newFiles = mutableListOf<FileItem>()
        
        documentFile.listFiles().forEach { doc ->
            if (doc.isFile) {
                val mimeType = doc.type ?: "application/octet-stream"
                val fileType = getFileTypeFromMime(mimeType)
                val color = getColorForType(fileType)
                val icon = getIconForMime(mimeType, doc.name ?: "")
                
                newFiles.add(
                    FileItem(
                        name = doc.name ?: "Unknown",
                        type = fileType,
                        tags = listOf(fileType),
                        color = color,
                        icon = icon,
                        fullPath = doc.uri.toString(),
                        uri = doc.uri,
                        mimeType = mimeType,
                        isDirectory = false,
                        size = doc.length(),
                        lastModified = doc.lastModified()
                    )
                )
            }
        }
        
        // 按最后修改时间排序（最新在前）
        newFiles.sortByDescending { it.lastModified }
        files.clear()
        files.addAll(newFiles)
    }
    
    // ===== 多选操作 =====
    
    fun toggleSelection(file: FileItem) {
        val index = files.indexOfFirst { it.id == file.id }
        if (index >= 0) {
            val current = files[index]
            val newFile = current.copy(isSelected = !current.isSelected)
            files[index] = newFile
            
            if (newFile.isSelected) {
                selectedFiles.add(newFile)
            } else {
                selectedFiles.removeAll { it.id == file.id }
            }
            
            isSelectionMode.value = selectedFiles.isNotEmpty()
        }
    }
    
    fun clearSelection() {
        files.forEachIndexed { index, file ->
            if (file.isSelected) {
                files[index] = file.copy(isSelected = false)
            }
        }
        selectedFiles.clear()
        isSelectionMode.value = false
    }
    
    fun getSelectedFilePaths(): List<String> {
        return selectedFiles.mapNotNull { it.fullPath ?: it.uri?.toString() }
    }
    
    // ===== 技能操作 =====
    
    fun addSkill(skill: SkillItem) {
        skills.add(0, skill)
    }

    // ===== 工具方法 =====
    
    private fun getFileTypeFromMime(mimeType: String): String {
        return when {
            mimeType.startsWith("image/") -> "图片"
            mimeType.startsWith("video/") -> "视频"
            mimeType.startsWith("audio/") -> "音频"
            mimeType.startsWith("text/") -> "文档"
            mimeType.contains("pdf") -> "文档"
            mimeType.contains("document") || mimeType.contains("word") -> "文档"
            mimeType.contains("sheet") || mimeType.contains("excel") -> "表格"
            mimeType.contains("presentation") || mimeType.contains("powerpoint") -> "演示"
            else -> "文件"
        }
    }
    
    private fun getColorForType(type: String): Color {
        return when (type) {
            "图片" -> Color(0xFFEBDDFF)
            "视频" -> Color(0xFFFFDDE6)
            "音频" -> Color(0xFFFFF4DE)
            "文档" -> Color(0xFFDDE6FF)
            "表格" -> Color(0xFFE2F5E5)
            "演示" -> Color(0xFFFFE0B2)
            "代码" -> Color(0xFFF5F7F5)
            else -> Color(0xFFE0E0E0)
        }
    }
    
    private fun getIconForMime(mimeType: String, fileName: String): ImageVector {
        return when {
            mimeType.startsWith("image/") -> Icons.Default.Image
            mimeType.startsWith("video/") -> Icons.Default.VideoFile
            mimeType.startsWith("audio/") -> Icons.Default.MusicNote
            mimeType.contains("pdf") -> Icons.Default.PictureAsPdf
            mimeType.contains("document") || mimeType.contains("word") -> Icons.Default.Description
            mimeType.contains("sheet") || mimeType.contains("excel") -> Icons.Default.TableChart
            mimeType.contains("presentation") -> Icons.Default.Slideshow
            fileName.endsWith(".py") || fileName.endsWith(".kt") || 
            fileName.endsWith(".java") || fileName.endsWith(".js") -> Icons.Outlined.Code
            else -> Icons.Default.InsertDriveFile
        }
    }

    // ===== 演示数据 =====
    
    private fun getInitialFiles() = listOf(
        FileItem(name = "软件架构_Lec3.pdf", type = "文档", tags = listOf("大学", "CS302"), color = Color(0xFFDDE6FF), icon = Icons.Default.PictureAsPdf),
        FileItem(name = "白板_讨论_最终版.png", type = "图片", tags = listOf("项目", "头脑风暴"), color = Color(0xFFEBDDFF), icon = Icons.Default.Image),
        FileItem(name = "研究提案_草稿_v2.docx", type = "文档", tags = listOf("论文", "草稿"), color = Color(0xFFE2F5E5), icon = Icons.Default.Description),
        FileItem(name = "访谈_参与者_04.mp3", type = "音频", tags = listOf("研究", "音频"), color = Color(0xFFFFF4DE), icon = Icons.Default.MusicNote),
        FileItem(name = "train_model.py", type = "代码", tags = listOf("开发", "Python"), color = Color(0xFFF5F7F5), icon = Icons.Outlined.Code),
        FileItem(name = "设计模式_速查表.pdf", type = "文档", tags = listOf("学习", "参考"), color = Color(0xFFFFDDE6), icon = Icons.Default.PictureAsPdf)
    )

    private fun getInitialSkills() = listOf(
        SkillItem("git", "Git 工作流", "自动提交并推送", Icons.Default.Code, "专家", Color(0xFFE2F5E5)),
        SkillItem("python", "Python 脚本", "运行本地分析", Icons.Default.Code, "高级", Color(0xFFE2F5E5)),
        SkillItem("paper", "论文总结", "PDF 生成摘要", Icons.Default.Book, "专家", Color(0xFFE8EAF6)),
        SkillItem("citation", "引用管理", "格式化参考文献", Icons.Default.Book, "专家", Color(0xFFE8EAF6)),
        SkillItem("file_org", "文件整理", "智能分类下载", Icons.Default.Layers, "入门", Color(0xFFFFEBEE))
    )
    
    // ===== Demo 创建方法（用于技能演示）=====
    
    fun createLearningTongSkill() = SkillItem(
        id = "learning_tong",
        name = "学习通资料提取",
        desc = "自动下载课件并生成摘要",
        icon = Icons.Default.School,
        tag = "新技能",
        bgColor = Color(0xFFE8F5E9)
    )
    
    fun createLearningTongFile() = FileItem(
        name = "深度学习_期末复习_2026.pdf",
        type = "文档",
        tags = listOf("学习通", "期末"),
        color = Color(0xFFE8F5E9),
        icon = Icons.Default.PictureAsPdf
    )
    
    fun createSummaryFile() = FileItem(
        name = "重点摘要_自动生成.md",
        type = "文档",
        tags = listOf("AI总结", "重点"),
        color = Color(0xFFFFF8E1),
        icon = Icons.Default.Description
    )
}

val LocalDemoController = compositionLocalOf { DemoController() }
