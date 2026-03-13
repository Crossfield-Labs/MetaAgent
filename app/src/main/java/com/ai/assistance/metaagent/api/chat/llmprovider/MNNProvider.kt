package com.ai.assistance.metaagent.api.chat.llmprovider

import android.content.Context
import android.os.Environment
import com.ai.assistance.metaagent.data.model.ApiProviderType
import com.ai.assistance.metaagent.data.model.ModelOption
import com.ai.assistance.metaagent.data.model.ModelParameter
import com.ai.assistance.metaagent.data.model.ToolPrompt
import com.ai.assistance.metaagent.util.stream.Stream
import com.ai.assistance.metaagent.util.stream.stream
import java.io.File

class MNNProvider(
    private val context: Context,
    private val modelName: String,
    private val forwardType: Int,
    private val threadCount: Int,
    private val providerType: ApiProviderType = ApiProviderType.MNN,
    private val enableToolCall: Boolean = false,
    private val supportsVision: Boolean = false,
    private val supportsAudio: Boolean = false,
    private val supportsVideo: Boolean = false
) : AIService {

    companion object {
        fun getModelDir(_context: Context, modelName: String): String {
            val modelsDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "MetaAgent/models/mnn"
            )
            return File(modelsDir, modelName).absolutePath
        }
    }

    override val inputTokenCount: Int = 0
    override val cachedInputTokenCount: Int = 0
    override val outputTokenCount: Int = 0
    override val providerModel: String
        get() = "${providerType.name}:$modelName"

    override fun resetTokenCounts() = Unit

    override fun cancelStreaming() = Unit

    override suspend fun getModelsList(context: Context): Result<List<ModelOption>> {
        return Result.failure(IllegalStateException("Local MNN support has been removed from MetaAgent."))
    }

    override suspend fun sendMessage(
        context: Context,
        message: String,
        chatHistory: List<Pair<String, String>>,
        modelParameters: List<ModelParameter<*>>,
        enableThinking: Boolean,
        stream: Boolean,
        availableTools: List<ToolPrompt>?,
        preserveThinkInHistory: Boolean,
        onTokensUpdated: suspend (input: Int, cachedInput: Int, output: Int) -> Unit,
        onNonFatalError: suspend (error: String) -> Unit,
        enableRetry: Boolean
    ): Stream<String> = stream {
        onNonFatalError("Local MNN support has been removed from MetaAgent.")
    }

    override suspend fun testConnection(context: Context): Result<String> {
        return Result.failure(IllegalStateException("Local MNN support has been removed from MetaAgent."))
    }

    override suspend fun calculateInputTokens(
        message: String,
        chatHistory: List<Pair<String, String>>,
        availableTools: List<ToolPrompt>?
    ): Int = 0
}
