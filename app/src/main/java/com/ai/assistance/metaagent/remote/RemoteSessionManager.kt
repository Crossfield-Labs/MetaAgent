package com.ai.assistance.metaagent.remote

import java.util.UUID
import java.util.concurrent.atomic.AtomicReference
import kotlinx.serialization.Serializable

@Serializable
data class RemoteSessionSnapshot(
    val sessionId: String,
    val token: String,
    val clientName: String,
    val createdAtEpochMs: Long,
    val lastSeenAtEpochMs: Long
)

object RemoteSessionManager {
    private val activeSession = AtomicReference<RemoteSessionSnapshot?>(null)

    fun openSession(clientName: String): RemoteSessionSnapshot {
        val now = System.currentTimeMillis()
        val snapshot = RemoteSessionSnapshot(
            sessionId = UUID.randomUUID().toString(),
            token = UUID.randomUUID().toString().replace("-", ""),
            clientName = clientName.ifBlank { "desktop-client" },
            createdAtEpochMs = now,
            lastSeenAtEpochMs = now
        )
        activeSession.set(snapshot)
        return snapshot
    }

    fun getActiveSession(): RemoteSessionSnapshot? = activeSession.get()

    fun validateToken(token: String?): Boolean {
        val session = activeSession.get() ?: return false
        if (token.isNullOrBlank() || token != session.token) {
            return false
        }
        touch()
        return true
    }

    fun touch() {
        val session = activeSession.get() ?: return
        activeSession.set(session.copy(lastSeenAtEpochMs = System.currentTimeMillis()))
    }

    fun closeSession(sessionId: String?): Boolean {
        val session = activeSession.get() ?: return false
        if (!sessionId.isNullOrBlank() && session.sessionId != sessionId) {
            return false
        }
        activeSession.set(null)
        return true
    }
}
