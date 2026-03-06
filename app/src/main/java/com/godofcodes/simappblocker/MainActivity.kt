package com.godofcodes.simappblocker

import android.content.ComponentName
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.activity.enableEdgeToEdge
import com.godofcodes.simappblocker.ui.AppNavigation
import com.godofcodes.simappblocker.ui.theme.AppVaultTheme
import rikka.shizuku.Shizuku
import com.godofcodes.simappblocker.BuildConfig

class MainActivity : ComponentActivity() {

    private val viewModel: AppViewModel by viewModels()
    private var userServiceArgs: Shizuku.UserServiceArgs? = null
    private var isServiceBound = false
    private val shizukuPermCode = 1001

    private val permissionResultListener =
        Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
            if (requestCode == shizukuPermCode && grantResult == PackageManager.PERMISSION_GRANTED) {
                bindAppManagerService()
            }
        }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val appManager = IAppManager.Stub.asInterface(service)
            isServiceBound = true
            viewModel.onServiceConnected(appManager)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isServiceBound = false
            viewModel.onServiceDisconnected()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Shizuku.addRequestPermissionResultListener(permissionResultListener)

        setContent {
            AppVaultTheme {
                AppNavigation(
                    viewModel = viewModel,
                    onConnectClick = ::checkShizukuAndProceed
                )
            }
        }

        checkShizukuAndProceed()
    }

    private fun checkShizukuAndProceed() {
        if (isServiceBound) return
        try {
            if (!Shizuku.pingBinder()) return
            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                bindAppManagerService()
            } else {
                Shizuku.requestPermission(shizukuPermCode)
            }
        } catch (_: Exception) {}
    }

    private fun bindAppManagerService() {
        if (isServiceBound) return
        val args = Shizuku.UserServiceArgs(
            ComponentName(packageName, AppManagerService::class.java.name)
        )
            .daemon(false)
            .processNameSuffix("app_manager")
            .debuggable(BuildConfig.DEBUG)
            .version(1)
        userServiceArgs = args
        Shizuku.bindUserService(args, serviceConnection)
    }

    override fun onResume() {
        super.onResume()
        if (!isServiceBound) checkShizukuAndProceed()
    }

    override fun onDestroy() {
        super.onDestroy()
        Shizuku.removeRequestPermissionResultListener(permissionResultListener)
        try {
            userServiceArgs?.let { Shizuku.unbindUserService(it, serviceConnection, true) }
        } catch (_: Exception) {}
    }
}
