package com.ai.assistance.metaagent.ui.features.home.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class CourseRagChatBinding(
    val courseId: String,
    val courseName: String,
    val folderPath: String,
    val boundAt: String
)

object CourseRagChatBindingStore {
    private const val PREFS_NAME = "study_course_rag_chat_binding"
    private const val KEY_BINDINGS = "bindings"
    private val gson = Gson()

    fun bindChatToCourse(
        context: Context,
        chatId: String,
        courseId: String,
        courseName: String,
        folderPath: String,
        boundAt: String
    ) {
        if (chatId.isBlank() || folderPath.isBlank()) {
            return
        }
        val bindings = loadBindings(context).toMutableMap()
        bindings[chatId] = CourseRagChatBinding(
            courseId = courseId,
            courseName = courseName,
            folderPath = folderPath,
            boundAt = boundAt
        )
        saveBindings(context, bindings)
    }

    fun getBinding(context: Context, chatId: String): CourseRagChatBinding? {
        return loadBindings(context)[chatId]
    }

    fun getFolderPath(context: Context, chatId: String): String? {
        return getBinding(context, chatId)?.folderPath?.takeIf { it.isNotBlank() }
    }

    fun removeBinding(context: Context, chatId: String) {
        val bindings = loadBindings(context).toMutableMap()
        if (bindings.remove(chatId) != null) {
            saveBindings(context, bindings)
        }
    }

    private fun loadBindings(context: Context): Map<String, CourseRagChatBinding> {
        val json = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_BINDINGS, null)
            ?: return emptyMap()
        return runCatching {
            val type = object : TypeToken<Map<String, CourseRagChatBinding>>() {}.type
            gson.fromJson<Map<String, CourseRagChatBinding>>(json, type).orEmpty()
        }.getOrDefault(emptyMap())
    }

    private fun saveBindings(context: Context, bindings: Map<String, CourseRagChatBinding>) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_BINDINGS, gson.toJson(bindings))
            .apply()
    }
}
