package com.ai.assistance.metaagent.remote

import android.content.Context
import android.os.Build
import com.ai.assistance.metaagent.core.tools.agent.ShowerController
import com.ai.assistance.metaagent.core.tools.system.AndroidPermissionLevel
import com.ai.assistance.metaagent.core.tools.system.ShizukuAuthorizer
import com.ai.assistance.metaagent.data.preferences.DisplayPreferencesManager
import com.ai.assistance.metaagent.data.preferences.androidPermissionPreferences
import com.ai.assistance.metaagent.data.repository.UIHierarchyManager
import java.net.NetworkInterface
import java.util.Collections
import kotlinx.coroutines.runBlocking

object RemoteRuntimeInspector {
    fun collectCapabilities(context: Context): RemoteCapabilitiesPayload {
        val preferredLevel =
            androidPermissionPreferences.getPreferredPermissionLevel()
                ?: AndroidPermissionLevel.STANDARD
        val displayPrefs = DisplayPreferencesManager.getInstance(context.applicationContext)
        val accessibilityEnabled =
            runBlocking { UIHierarchyManager.isAccessibilityServiceEnabled(context.applicationContext) }
        val shizukuRunning = ShizukuAuthorizer.isShizukuServiceRunning()
        val shizukuGranted = if (shizukuRunning) ShizukuAuthorizer.hasShizukuPermission() else false

        return RemoteCapabilitiesPayload(
            permissionLevel = preferredLevel.name,
            accessibilityEnabled = accessibilityEnabled,
            shizukuRunning = shizukuRunning,
            shizukuGranted = shizukuGranted,
            experimentalVirtualDisplayEnabled = displayPrefs.isExperimentalVirtualDisplayEnabled(),
            activeDisplayId = ShowerController.getDisplayId(),
            sdkInt = Build.VERSION.SDK_INT
        )
    }

    fun localIpv4Address(): String? {
        return Collections.list(NetworkInterface.getNetworkInterfaces())
            .asSequence()
            .filter { it.isUp && !it.isLoopback }
            .flatMap { network -> Collections.list(network.inetAddresses).asSequence() }
            .firstOrNull { address ->
                !address.isLoopbackAddress && address.hostAddress?.contains(':') == false
            }
            ?.hostAddress
    }
}
