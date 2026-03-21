package com.ai.assistance.metaagent.remote

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.ai.assistance.metaagent.R
import com.ai.assistance.metaagent.core.application.ForegroundServiceCompat
import com.ai.assistance.metaagent.util.AppLogger
import java.io.IOException

class RemoteControlService : Service() {
    private var server: RemoteAgentServer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_REMOTE_SERVICE -> {
                stopSelf()
                return START_NOT_STICKY
            }
        }

        startAsForeground()
        ensureServerStarted()
        return START_STICKY
    }

    override fun onDestroy() {
        server?.stop()
        server = null
        RemoteAgentTaskManager.shutdown()
        isRunning = false
        super.onDestroy()
    }

    private fun ensureServerStarted() {
        if (server != null) {
            return
        }

        val remoteServer = RemoteAgentServer(this, DEFAULT_PORT)
        try {
            remoteServer.start()
            server = remoteServer
            isRunning = true
            AppLogger.d(TAG, "Remote agent server started on port $DEFAULT_PORT")
        } catch (e: IOException) {
            AppLogger.e(TAG, "Failed to start remote agent server", e)
            stopSelf()
        }
    }

    private fun startAsForeground() {
        val notification = createNotification()
        val types = ForegroundServiceCompat.buildTypes(dataSync = true, specialUse = true)
        ForegroundServiceCompat.startForeground(this, NOTIFICATION_ID, notification, types)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Remote Agent Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Keeps the remote control endpoint alive for desktop connections."
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Remote Agent Service")
            .setContentText("Desktop control endpoint is available on port $DEFAULT_PORT.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val TAG = "RemoteControlService"
        private const val CHANNEL_ID = "remote_agent_service"
        private const val NOTIFICATION_ID = 1405
        const val DEFAULT_PORT = RemoteAgentServer.DEFAULT_PORT
        const val ACTION_START_REMOTE_SERVICE = "com.ai.assistance.metaagent.action.START_REMOTE_SERVICE"
        const val ACTION_STOP_REMOTE_SERVICE = "com.ai.assistance.metaagent.action.STOP_REMOTE_SERVICE"

        @Volatile
        var isRunning: Boolean = false
            private set

        fun buildStartIntent(context: Context): Intent {
            return Intent(context, RemoteControlService::class.java).apply {
                action = ACTION_START_REMOTE_SERVICE
            }
        }

        fun buildStopIntent(context: Context): Intent {
            return Intent(context, RemoteControlService::class.java).apply {
                action = ACTION_STOP_REMOTE_SERVICE
            }
        }
    }
}
