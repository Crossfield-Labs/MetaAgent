package com.ai.assistance.metaagent.core.tools.agent

import java.util.concurrent.ConcurrentHashMap

object UiAutomationStepCallbackRegistry {
    private val callbacks = ConcurrentHashMap<String, suspend (StepResult) -> Unit>()

    fun register(callbackId: String, callback: suspend (StepResult) -> Unit) {
        callbacks[callbackId] = callback
    }

    fun get(callbackId: String): (suspend (StepResult) -> Unit)? = callbacks[callbackId]

    fun unregister(callbackId: String) {
        callbacks.remove(callbackId)
    }
}
