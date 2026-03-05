package com.godofcodes.simappblocker

import android.app.Dialog
import android.content.ComponentName
import android.content.ServiceConnection
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.godofcodes.simappblocker.databinding.ActivityMainBinding
import rikka.shizuku.Shizuku

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: AppListAdapter
    private var allApps: MutableList<AppItem> = mutableListOf()
    private var appManager: IAppManager? = null
    private var userServiceArgs: Shizuku.UserServiceArgs? = null
    private var isServiceBound = false
    private val shizukuPermCode = 1001

    private val permissionResultListener =
        Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
            if (requestCode == shizukuPermCode && grantResult == PackageManager.PERMISSION_GRANTED) {
                bindAppManagerService()
            } else {
                Toast.makeText(this, R.string.toast_shizuku_denied, Toast.LENGTH_SHORT).show()
            }
        }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            appManager = IAppManager.Stub.asInterface(service)
            isServiceBound = true
            runOnUiThread { showAppList() }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            appManager = null
            isServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Shizuku.addRequestPermissionResultListener(permissionResultListener)

        adapter = AppListAdapter { item -> showActionDialog(item) }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.btnConnect.setOnClickListener { checkShizukuAndProceed() }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filterApps(newText.orEmpty())
                return true
            }
        })

        binding.btnShowSystem.setOnClickListener {
            filterApps(binding.searchView.query?.toString().orEmpty())
        }

        binding.btnShowHidden.setOnClickListener {
            filterApps(binding.searchView.query?.toString().orEmpty())
        }

        checkShizukuAndProceed()
    }

    private fun checkShizukuAndProceed() {
        if (isServiceBound && appManager != null) return
        try {
            if (!Shizuku.pingBinder()) {
                showSetupScreen()
                return
            }
            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                bindAppManagerService()
            } else {
                Shizuku.requestPermission(shizukuPermCode)
            }
        } catch (e: Exception) {
            showSetupScreen()
        }
    }

    private fun bindAppManagerService() {
        if (isServiceBound) return
        userServiceArgs = Shizuku.UserServiceArgs(
            ComponentName(packageName, AppManagerService::class.java.name)
        )
            .daemon(false)
            .processNameSuffix("app_manager")
            .debuggable(true)
            .version(1)

        Shizuku.bindUserService(userServiceArgs!!, serviceConnection)
    }

    private fun showSetupScreen() {
        binding.setupLayout.visibility = View.VISIBLE
        binding.appListLayout.visibility = View.GONE
    }

    private fun showAppList() {
        binding.setupLayout.visibility = View.GONE
        binding.appListLayout.visibility = View.VISIBLE
        if (allApps.isEmpty()) {
            loadApps()
        }
    }

    private fun loadApps() {
        val pm = packageManager
        val flags = PackageManager.GET_META_DATA or
                PackageManager.MATCH_DISABLED_COMPONENTS or
                PackageManager.MATCH_UNINSTALLED_PACKAGES
        val installed = pm.getInstalledApplications(flags)
        val disabledPackages = getDisabledPackages()

        allApps = installed
            .filter { it.packageName != packageName }
            .mapNotNull { info ->
                try {
                    val label = pm.getApplicationLabel(info).toString()
                    val icon = try {
                        pm.getApplicationIcon(info)
                    } catch (e: Exception) {
                        pm.defaultActivityIcon
                    }
                    val hidden = disabledPackages.contains(info.packageName)
                    AppItem(info.packageName, label, icon, hidden)
                } catch (e: Exception) {
                    null
                }
            }
            .sortedWith(compareBy({ !it.isHidden }, { isSystemApp(it.packageName) }, { it.label.lowercase() }))
            .toMutableList()

        filterApps("")
    }

    private fun getDisabledPackages(): Set<String> {
        return try {
            val result = appManager?.executeCommand("pm list packages -d") ?: return emptySet()
            result.lines()
                .filter { it.startsWith("package:") }
                .map { it.removePrefix("package:") }
                .toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    private fun filterApps(query: String) {
        val showSystem = binding.btnShowSystem.isChecked
        val showHiddenOnly = binding.btnShowHidden.isChecked
        var base = if (showSystem) allApps else allApps.filter { !isSystemApp(it.packageName) }
        if (showHiddenOnly) base = base.filter { it.isHidden }
        val filtered = if (query.isBlank()) base else base.filter {
            it.label.contains(query, ignoreCase = true) ||
                    it.packageName.contains(query, ignoreCase = true)
        }
        binding.emptyText.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        adapter.submitList(filtered.map { it.copy() })
    }

    private fun showActionDialog(item: AppItem) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_action)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.88).toInt(),
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dialog.findViewById<ImageView>(R.id.dialogAppIcon).setImageDrawable(item.icon)
        dialog.findViewById<TextView>(R.id.dialogAppName).text = item.label
        dialog.findViewById<TextView>(R.id.dialogAppPackage).text = item.packageName

        val toggleText = dialog.findViewById<TextView>(R.id.textToggle)
        val toggleIcon = dialog.findViewById<ImageView>(R.id.iconToggle)
        if (item.isHidden) {
            toggleText.text = getString(R.string.action_show)
            toggleIcon.setImageResource(android.R.drawable.ic_menu_view)
            toggleIcon.setColorFilter(getColor(R.color.visible_badge_text))
        } else {
            toggleText.text = getString(R.string.action_hide)
            toggleIcon.setImageResource(android.R.drawable.ic_secure)
            toggleIcon.setColorFilter(getColor(R.color.secondary))
        }

        dialog.findViewById<View>(R.id.btnToggle).setOnClickListener {
            dialog.dismiss()
            toggleAppVisibility(item)
        }

        dialog.findViewById<View>(R.id.btnDelete).setOnClickListener {
            dialog.dismiss()
            confirmUninstall(item)
        }

        dialog.findViewById<View>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun confirmUninstall(item: AppItem) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_confirm_delete)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.88).toInt(),
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dialog.findViewById<TextView>(R.id.confirmMessage).text =
            getString(R.string.delete_confirm_message, item.label)

        dialog.findViewById<View>(R.id.btnConfirmDelete).setOnClickListener {
            dialog.dismiss()
            uninstallApp(item)
        }

        dialog.findViewById<View>(R.id.btnConfirmCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun uninstallApp(item: AppItem) {
        try {
            val result = appManager?.executeCommand(
                "pm uninstall -k --user 0 ${item.packageName}"
            ) ?: getString(R.string.toast_service_disconnected)

            if (result.contains("Success")) {
                Toast.makeText(this, getString(R.string.toast_deleted, item.label), Toast.LENGTH_SHORT).show()
                allApps.remove(item)
                filterApps(binding.searchView.query?.toString().orEmpty())
            } else {
                Toast.makeText(this, getString(R.string.toast_failed, result), Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.toast_error, e.message), Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleAppVisibility(item: AppItem) {
        try {
            val command = if (item.isHidden) {
                "pm enable ${item.packageName}"
            } else {
                "pm disable-user --user 0 ${item.packageName}"
            }
            val result = appManager?.executeCommand(command) ?: getString(R.string.toast_service_disconnected)

            if (result.contains("new state") || result.contains("enabled")) {
                val newHidden = !item.isHidden
                val idx = allApps.indexOf(item)
                if (idx >= 0) {
                    allApps[idx] = item.copy(isHidden = newHidden)
                }
                val msg = if (newHidden) {
                    getString(R.string.toast_hidden, item.label)
                } else {
                    getString(R.string.toast_visible, item.label)
                }
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                filterApps(binding.searchView.query?.toString().orEmpty())
            } else {
                Toast.makeText(this, getString(R.string.toast_operation_failed, result), Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.toast_error, e.message), Toast.LENGTH_SHORT).show()
        }
    }

    private fun isSystemApp(packageName: String): Boolean {
        return try {
            val flags = PackageManager.MATCH_DISABLED_COMPONENTS or PackageManager.MATCH_UNINSTALLED_PACKAGES
            val info = packageManager.getApplicationInfo(packageName, flags)
            (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        } catch (e: Exception) {
            false
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isServiceBound) {
            checkShizukuAndProceed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Shizuku.removeRequestPermissionResultListener(permissionResultListener)
        try {
            userServiceArgs?.let { Shizuku.unbindUserService(it, serviceConnection, true) }
        } catch (_: Exception) {}
    }
}
