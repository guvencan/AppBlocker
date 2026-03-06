package com.godofcodes.simappblocker.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.godofcodes.simappblocker.AppItem
import com.godofcodes.simappblocker.Bloatware
import com.godofcodes.simappblocker.IAppManager

class AppRepository(
    private val context: Context,
    private val appManager: IAppManager
) {

    fun getInstalledApps(): List<AppItem> {
        val pm = context.packageManager
        val flags = PackageManager.MATCH_DISABLED_COMPONENTS
        //val flags = PackageManager.MATCH_DISABLED_COMPONENTS or PackageManager.MATCH_UNINSTALLED_PACKAGES
        val installed = pm.getInstalledApplications(flags)
        val disabled = getDisabledPackages()

        return installed
            .filter { it.packageName != context.packageName }
            .mapNotNull { info ->
                try {
                    val label = pm.getApplicationLabel(info).toString()
                    val isHidden = disabled.contains(info.packageName)
                    val isSystem = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    val isBloatware = Bloatware.packages.contains(info.packageName)
                    val isUninstalled = false //(info.flags and ApplicationInfo.FLAG_INSTALLED) == 0
                    AppItem(info.packageName, label, null, isHidden, isSystem, isBloatware = isBloatware, isUninstalled = isUninstalled)
                } catch (e: Exception) {
                    null
                }
            }
            .sortedWith(compareBy({ !it.isHidden }, { it.isSystem }, { it.label.lowercase() }))
    }

    private fun getDisabledPackages(): Set<String> {
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

    fun executeCommand(command: String): String = appManager.executeCommand(command)
}
