package com.godofcodes.simappblocker.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.godofcodes.simappblocker.AppItem
import com.godofcodes.simappblocker.Bloatware
import com.godofcodes.simappblocker.IAppManager

class AppRepository(
    private val context: Context,
    private val appManager: IAppManager? = null
) {

    fun getInstalledApps(): List<AppItem> {
        val pm = context.packageManager
        val installed = pm.getInstalledApplications(PackageManager.MATCH_DISABLED_COMPONENTS)
        val disabled = getDisabledPackages()
        val uninstalledPkgs = getUninstalledPackages()

        val installedItems = installed
            .filter { it.packageName != context.packageName }
            .mapNotNull { info ->
                try {
                    val label = pm.getApplicationLabel(info).toString()
                    val isHidden = disabled.contains(info.packageName)
                    val isSystem = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    val isBloatware = Bloatware.packages.contains(info.packageName)
                    AppItem(info.packageName, label, null, isHidden, isSystem, isBloatware = isBloatware, isUninstalled = false)
                } catch (e: Exception) {
                    null
                }
            }

        val installedPkgNames = installedItems.map { it.packageName }.toSet()

        val uninstalledItems = uninstalledPkgs
            .filter { it !in installedPkgNames }
            .mapNotNull { pkg ->
                try {
                    @Suppress("DEPRECATION")
                    val info = pm.getApplicationInfo(pkg, PackageManager.GET_UNINSTALLED_PACKAGES)
                    val label = pm.getApplicationLabel(info).toString()
                    val isSystem = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    val isBloatware = Bloatware.packages.contains(pkg)
                    AppItem(pkg, label, null, isHidden = false, isSystem = isSystem, isBloatware = isBloatware, isUninstalled = true)
                } catch (e: Exception) {
                    null
                }
            }

        return (installedItems + uninstalledItems)
            .sortedWith(compareBy({ it.isUninstalled }, { !it.isBloatware }, { !it.isHidden }, { it.isSystem }, { it.label.lowercase() }))
    }

    private fun getDisabledPackages(): Set<String> {
        if (appManager != null) {
            return try {
                val result = appManager.executeCommand("pm list packages -d")
                result.lines()
                    .filter { it.startsWith("package:") }
                    .map { it.removePrefix("package:") }
                    .toSet()
            } catch (e: Exception) {
                emptySet()
            }
        }
        // Fallback: detect via PackageManager enabled state
        return try {
            context.packageManager
                .getInstalledApplications(PackageManager.MATCH_DISABLED_COMPONENTS)
                .filter { info ->
                    val state = context.packageManager.getApplicationEnabledSetting(info.packageName)
                    state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED ||
                    state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER ||
                    state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED
                }
                .map { it.packageName }
                .toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    // Returns packages that are registered in system but uninstalled for current user (Shizuku only)
    private fun getUninstalledPackages(): Set<String> {
        if (appManager == null) return emptySet()
        return try {
            val allResult = appManager.executeCommand("pm list packages -u")
            val installedResult = appManager.executeCommand("pm list packages")
            val allPkgs = allResult.lines()
                .filter { it.startsWith("package:") }
                .map { it.removePrefix("package:").trim() }
                .toSet()
            val installedPkgs = installedResult.lines()
                .filter { it.startsWith("package:") }
                .map { it.removePrefix("package:").trim() }
                .toSet()
            allPkgs - installedPkgs - setOf(context.packageName)
        } catch (e: Exception) {
            emptySet()
        }
    }

    fun executeCommand(command: String): String =
        appManager?.executeCommand(command) ?: error("Shizuku not connected")
}
