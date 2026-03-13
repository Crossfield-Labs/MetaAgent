package com.ai.assistance.metaagent.ui.floating.ui.pet

import com.ai.assistance.metaagent.util.AppLogger
import com.ai.assistance.metaagent.core.avatar.common.state.AvatarEmotion

/**
 * Avatar表情管理器
 * 从PetOverlayService迁移的表情推理逻辑
 */
object AvatarEmotionManager {
    
    // Mood枚举 - 用于解析AI返回的mood标签
    private enum class Mood { ANGRY, HAPPY, SHY, AOJIAO, CRY }
    
    /**
     * 从文本内容推理情感
     * 通过关键词匹配来判断应该使用哪种表情
     */
    fun inferEmotionFromText(text: String): AvatarEmotion {
        val t = text.lowercase()
        val happyKeywords = listOf("开心", "高兴", "不错", "棒", "太好了", "😀", "🙂", "😊", "😄", "赞")
        val angryKeywords = listOf("生气", "愤怒", "气死", "讨厌", "糟糕", "😡", "怒")
        val cryKeywords = listOf("难过", "伤心", "沮丧", "忧伤", "哭", "😭", "😢")
        val shyKeywords = listOf("害羞", "羞", "脸红", "不好意思", "///")
        
        fun containsAny(keys: List<String>): Boolean = 
            keys.any { t.contains(it) || text.contains(it) }
        
        return when {
            containsAny(happyKeywords) -> AvatarEmotion.HAPPY
            containsAny(angryKeywords) -> AvatarEmotion.SAD
            containsAny(cryKeywords) -> AvatarEmotion.SAD
            containsAny(shyKeywords) -> AvatarEmotion.CONFUSED
            else -> AvatarEmotion.IDLE
        }
    }
    
    /**
     * 从文本中提取mood标签
     * AI可能会在回复中包含<mood>标签来明确指定情感
     */
    private fun extractMoodTag(text: String): Mood? {
        return try {
            val regex = Regex("<mood>([^<]+)</mood>", RegexOption.IGNORE_CASE)
            val all = regex.findAll(text).toList()
            if (all.isEmpty()) return null
            val raw = all.last().groupValues[1].trim().lowercase()
            when (raw) {
                "angry" -> Mood.ANGRY
                "happy" -> Mood.HAPPY
                "shy" -> Mood.SHY
                "aojiao" -> Mood.AOJIAO
                "cry" -> Mood.CRY
                else -> null
            }
        } catch (_: Exception) { null }
    }
    
    /**
     * 将Mood转换为AvatarEmotion
     */
    private fun moodToEmotion(mood: Mood): AvatarEmotion = when (mood) {
        Mood.ANGRY -> AvatarEmotion.SAD
        Mood.HAPPY -> AvatarEmotion.HAPPY
        Mood.SHY -> AvatarEmotion.CONFUSED
        Mood.AOJIAO -> AvatarEmotion.CONFUSED
        Mood.CRY -> AvatarEmotion.SAD
    }
    
    /**
     * 综合分析文本，返回最合适的表情
     * 优先使用mood标签，如果没有则使用关键词推理
     */
    fun analyzeEmotion(text: String): AvatarEmotion {
        AppLogger.d("AvatarEmotionManager", "分析情感 - 原始文本: $text")
        
        // 首先尝试从mood标签获取
        val parsedMood = extractMoodTag(text)
        if (parsedMood != null) {
            val emotion = moodToEmotion(parsedMood)
            AppLogger.d("AvatarEmotionManager", "从mood标签解析: $parsedMood -> $emotion")
            return emotion
        }
        
        // 如果没有mood标签，则使用关键词推理
        val emotion = inferEmotionFromText(text)
        AppLogger.d("AvatarEmotionManager", "使用关键词推理: $emotion")
        return emotion
    }
    
    /**
     * 清除文本中的XML标签
     * 用于显示给用户时移除mood等标记标签
     */
    fun stripXmlLikeTags(text: String): String {
        var s = text
        // 匹配成对的标签 <tag>...</tag>
        val paired = Regex(
            pattern = "<([A-Za-z][A-Za-z0-9:_-]*)(\\s[^>]*)?>[\\s\\S]*?</\\1>",
            options = setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        )
        repeat(5) { _ ->
            val updated = s.replace(paired, "")
            if (updated == s) return@repeat
            s = updated
        }
        // 匹配自闭合标签 <tag />
        s = s.replace(
            Regex("<[A-Za-z][A-Za-z0-9:_-]*(\\s[^>]*)?/\\s*>", RegexOption.IGNORE_CASE),
            ""
        )
        // 匹配任何剩余的标签
        s = s.replace(
            Regex("</?[^>]+>", RegexOption.IGNORE_CASE),
            ""
        )
        return s.trim()
    }
} 
