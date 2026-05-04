package com.vulnscanner.analyzer.data.repository

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.vulnscanner.analyzer.data.model.ComponentType
import com.vulnscanner.analyzer.data.model.VulnerableApp
import com.vulnscanner.analyzer.data.model.VulnerableComponent

/**
 * Core engine that:
 * 1. Enumerates all user-installed apps
 * 2. Parses their AndroidManifest components
 * 3. Flags exported components without permission protection
 */
internal class ComponentAnalyzer(private val packageManager: PackageManager) {

    /**
     * Returns list of all user-installed package names.
     * Filters out system apps by checking ApplicationInfo.FLAG_SYSTEM.
     */
    fun getInstalledUserApps(): List<String> {
        return packageManager
            .getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { appInfo ->
                // Keep only user-installed apps (exclude pure system apps)
                (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0
            }
            .map { it.packageName }
    }

    /**
     * Analyzes a single package and returns its VulnerableApp result (or null if none found / error).
     */
    fun analyzePackage(packageName: String): VulnerableApp? {
        return try {
            val flags = PackageManager.GET_ACTIVITIES or
                    PackageManager.GET_SERVICES or
                    PackageManager.GET_RECEIVERS

            val packageInfo: PackageInfo = packageManager.getPackageInfo(packageName, flags)
            // val appName = packageManager.getApplicationLabel(packageInfo.applicationInfo).toString()

            // FIX START: Handle nullability of applicationInfo
            val appInfo = packageInfo.applicationInfo
            val appName = if (appInfo != null) {
                packageManager.getApplicationLabel(appInfo).toString()
            } else {
                packageName
            }
            // FIX END

            val vulnerableComponents = mutableListOf<VulnerableComponent>()

            // ── Activities ──────────────────────────────────────────────────
            packageInfo.activities?.forEach { activity ->
                if (activity.exported && activity.permission == null) {
                    vulnerableComponents.add(
                        VulnerableComponent(
                            name = activity.name.substringAfterLast('.'),
                            type = ComponentType.ACTIVITY,
                            isExported = true,
                            permission = null
                        )
                    )
                }
            }

            // ── Services ─────────────────────────────────────────────────────
            packageInfo.services?.forEach { service ->
                if (service.exported && service.permission == null) {
                    vulnerableComponents.add(
                        VulnerableComponent(
                            name = service.name.substringAfterLast('.'),
                            type = ComponentType.SERVICE,
                            isExported = true,
                            permission = null
                        )
                    )
                }
            }

            // ── BroadcastReceivers ───────────────────────────────────────────
            packageInfo.receivers?.forEach { receiver ->
                if (receiver.exported && receiver.permission == null) {
                    vulnerableComponents.add(
                        VulnerableComponent(
                            name = receiver.name.substringAfterLast('.'),
                            type = ComponentType.RECEIVER,
                            isExported = true,
                            permission = null
                        )
                    )
                }
            }

            if (vulnerableComponents.isNotEmpty()) {
                VulnerableApp(
                    packageName = packageName,
                    appName = appName,
                    vulnerableComponents = vulnerableComponents
                )
            } else null

        } catch (e: PackageManager.NameNotFoundException) {
            null // App was uninstalled during scan
        } catch (e: Exception) {
            null // Skip unreadable packages silently
        }
    }
}
