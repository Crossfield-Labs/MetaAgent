package com.ai.assistance.operit.core.avatar.impl.factory

import com.ai.assistance.operit.core.avatar.common.factory.AvatarModelFactory
import com.ai.assistance.operit.core.avatar.common.model.AvatarModel
import com.ai.assistance.operit.core.avatar.common.model.AvatarType
import com.ai.assistance.operit.core.avatar.common.state.AvatarEmotion
import com.ai.assistance.operit.core.avatar.impl.webp.model.WebPAvatarModel

/**
 * A concrete implementation of [AvatarModelFactory] that can create virtual avatar models
 * from various data sources and configurations.
 */
class AvatarModelFactoryImpl : AvatarModelFactory {

    override fun createModel(
        id: String,
        name: String,
        type: AvatarType,
        data: Map<String, Any>
    ): AvatarModel? {
        return when (type) {
            AvatarType.DRAGONBONES -> null // DragonBones module removed
            AvatarType.WEBP -> createWebPModel(id, name, data)
            AvatarType.LIVE2D -> {
                // TODO: Implement Live2D model creation when available
                null
            }
            AvatarType.MMD -> {
                // TODO: Implement MMD model creation when available
                null
            }
        }
    }

    override fun createModelFromData(dataModel: Any): AvatarModel? {
        return when (dataModel) {
            is Map<*, *> -> {
                val dataMap = dataModel as? Map<String, Any> ?: return null
                val id = dataMap["id"] as? String ?: return null
                val name = dataMap["name"] as? String ?: return null
                val typeStr = dataMap["type"] as? String ?: return null
                val type = try {
                    AvatarType.valueOf(typeStr)
                } catch (e: IllegalArgumentException) {
                    return null
                }
                return createModel(id, name, type, dataMap)
            }
            else -> null
        }
    }

    override fun createDefaultModel(type: AvatarType, baseName: String): AvatarModel? {
        return when (type) {
            AvatarType.DRAGONBONES -> null // DragonBones module removed
            AvatarType.WEBP -> {
                // Create a default WebP virtual avatar model with standard emotion mapping
                WebPAvatarModel.createStandard(
                    id = "default_webp",
                    name = baseName,
                    basePath = "assets/avatars/default"
                )
            }
            AvatarType.LIVE2D -> {
                // TODO: Implement default Live2D virtual avatar model when available
                null
            }
            AvatarType.MMD -> {
                // TODO: Implement default MMD virtual avatar model when available
                null
            }
        }
    }

    override fun validateData(type: AvatarType, data: Map<String, Any>): Boolean {
        return when (type) {
            AvatarType.DRAGONBONES -> false // DragonBones module removed
            AvatarType.WEBP -> {
                val requiredKeys = getRequiredDataKeys(type)
                requiredKeys.all { key -> data.containsKey(key) && data[key] != null }
            }
            else -> false // Unsupported types
        }
    }

    override val supportedTypes: List<AvatarType>
        get() = listOf(AvatarType.WEBP) // DragonBones removed

    override fun getRequiredDataKeys(type: AvatarType): List<String> {
        return when (type) {
            AvatarType.DRAGONBONES -> emptyList() // DragonBones module removed
            AvatarType.WEBP -> listOf(
                "basePath"
            )
            else -> emptyList()
        }
    }

    /**
     * Creates a WebP virtual avatar model from the provided data.
     */
    private fun createWebPModel(id: String, name: String, data: Map<String, Any>): AvatarModel? {
        return try {
            val basePath = data["basePath"] as? String ?: return null
            val emotionMapData = data["emotionToFileMap"] as? Map<String, String>

            if (emotionMapData != null) {
                // Convert string keys to AvatarEmotion enum
                val emotionMap = emotionMapData.mapNotNull { (emotionStr, fileName) ->
                    try {
                        val emotion = AvatarEmotion.valueOf(emotionStr.uppercase())
                        emotion to fileName
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                }.toMap()

                val currentEmotionStr = data["currentEmotion"] as? String
                val currentEmotion = if (currentEmotionStr != null) {
                    try {
                        AvatarEmotion.valueOf(currentEmotionStr.uppercase())
                    } catch (e: IllegalArgumentException) {
                        AvatarEmotion.IDLE
                    }
                } else {
                    AvatarEmotion.IDLE
                }

                WebPAvatarModel(
                    id = id,
                    name = name,
                    basePath = basePath,
                    emotionToFileMap = emotionMap,
                    currentEmotion = currentEmotion
                )
            } else {
                // Use standard emotion mapping
                WebPAvatarModel.createStandard(
                    id = id,
                    name = name,
                    basePath = basePath
                )
            }
        } catch (e: Exception) {
            null
        }
    }
}