package com.ai.assistance.operit.ui.main

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Code

// Domain Models
data class FileItem(
    val name: String,
    val type: String,
    val tags: List<String>,
    val color: Color,
    val icon: ImageVector
)

data class SkillItem(
    val id: String,
    val name: String,
    val desc: String,
    val icon: ImageVector,
    val tag: String,
    val bgColor: Color
)

// Demo Logic Controller
class DemoController {
    val files = mutableStateListOf<FileItem>()
    val skills = mutableStateListOf<SkillItem>()

    init {
        files.addAll(getInitialFiles())
        skills.addAll(getInitialSkills())
    }

    fun addFile(file: FileItem) {
        files.add(0, file)
    }

    fun addSkill(skill: SkillItem) {
        skills.add(0, skill)
    }

    private fun getInitialFiles() = listOf(
        FileItem("软件架构_Lec3.pdf", "文档", listOf("大学", "CS302"), Color(0xFFDDE6FF), Icons.Default.Description),
        FileItem("白板_讨论_最终版.png", "图片", listOf("项目", "头脑风暴"), Color(0xFFEBDDFF), Icons.Default.Image),
        FileItem("研究提案_草稿_v2.docx", "文档", listOf("论文", "草稿"), Color(0xFFE2F5E5), Icons.Default.Description),
        FileItem("访谈_参与者_04.mp3", "音频", listOf("研究", "音频"), Color(0xFFFFF4DE), Icons.Default.MusicNote),
        FileItem("train_model.py", "代码", listOf("开发", "Python"), Color(0xFFF5F7F5), Icons.Outlined.Code),
        FileItem("设计模式_速查表.pdf", "文档", listOf("学习", "参考"), Color(0xFFFFDDE6), Icons.Default.Description),
        FileItem("屏幕截图_2026-02-01...", "图片", listOf("杂项"), Color(0xFFCEE5E5), Icons.Default.Image),
        FileItem("会议纪要_2月.docx", "文档", listOf("工作", "行政"), Color(0xFFFFF4DE), Icons.Default.Description)
    )

    private fun getInitialSkills() = listOf(
        SkillItem("git", "Git 工作流", "自动提交并推送", Icons.Default.Code, "专家", Color(0xFFE2F5E5)),
        SkillItem("python", "Python 脚本", "运行本地分析", Icons.Default.Code, "高级", Color(0xFFE2F5E5)),
        SkillItem("paper", "论文总结", "PDF 生成摘要", Icons.Default.Book, "专家", Color(0xFFE8EAF6)),
        SkillItem("citation", "引用管理", "格式化参考文献", Icons.Default.Book, "专家", Color(0xFFE8EAF6)),
        SkillItem("file_org", "文件整理", "智能分类下载", Icons.Default.Layers, "入门", Color(0xFFFFEBEE))
    )
    
    fun createLearningTongSkill(): SkillItem {
        return SkillItem(
            id = "learning_tong",
            name = "学习通资料提取",
            desc = "自动下载课件并生成摘要",
            icon = Icons.Default.School,
            tag = "新技能",
            bgColor = Color(0xFFE8F5E9)
        )
    }
    
    fun createLearningTongFile(): FileItem {
        return FileItem(
            name = "深度学习_期末复习_2026.pdf",
            type = "文档",
            tags = listOf("学习通", "期末"),
            color = Color(0xFFE8F5E9),
            icon = Icons.Default.PictureAsPdf
        )
    }
    
    fun createSummaryFile(): FileItem {
        return FileItem(
            name = "重点摘要_自动生成.md",
            type = "文档",
            tags = listOf("AI总结", "重点"),
            color = Color(0xFFFFF8E1),
            icon = Icons.Default.Description
        )
    }
}

val LocalDemoController = compositionLocalOf { DemoController() }
