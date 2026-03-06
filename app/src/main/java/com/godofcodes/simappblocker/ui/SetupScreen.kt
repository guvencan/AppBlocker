package com.godofcodes.simappblocker.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.godofcodes.simappblocker.R
import com.godofcodes.simappblocker.ui.theme.SurfaceVariant
import rikka.shizuku.Shizuku

private const val SHIZUKU_PACKAGE = "moe.shizuku.privileged.api"

private enum class ShizukuButtonState { NOT_INSTALLED, OPEN_SHIZUKU, CONNECT }

@Composable
fun SetupScreen(onConnectClick: () -> Unit) {
    val context = LocalContext.current

    val buttonState = remember {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(SHIZUKU_PACKAGE)
        when {
            launchIntent == null -> ShizukuButtonState.NOT_INSTALLED
            runCatching {
                Shizuku.pingBinder() &&
                        Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
            }.getOrDefault(false) -> ShizukuButtonState.CONNECT
            else -> ShizukuButtonState.OPEN_SHIZUKU
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(android.R.drawable.ic_secure),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(SurfaceVariant)
                .padding(20.dp)
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.app_name),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.setup_subtitle),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(40.dp))

        SetupStep(number = "1", text = stringResource(R.string.setup_step1))
        Spacer(Modifier.height(12.dp))
        SetupStep(number = "2", text = stringResource(R.string.setup_step2))
        Spacer(Modifier.height(12.dp))
        SetupStep(number = "3", text = stringResource(R.string.setup_step3))

        Spacer(Modifier.height(40.dp))

        val buttonLabel = when (buttonState) {
            ShizukuButtonState.NOT_INSTALLED -> stringResource(R.string.btn_install_shizuku)
            ShizukuButtonState.OPEN_SHIZUKU -> stringResource(R.string.btn_open_shizuku)
            ShizukuButtonState.CONNECT -> stringResource(R.string.btn_connect)
        }

        Button(
            onClick = {
                when (buttonState) {
                    ShizukuButtonState.NOT_INSTALLED -> {
                        val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$SHIZUKU_PACKAGE"))
                        val fallback = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$SHIZUKU_PACKAGE"))
                        try { context.startActivity(marketIntent) } catch (_: Exception) { context.startActivity(fallback) }
                    }
                    ShizukuButtonState.OPEN_SHIZUKU -> {
                        context.packageManager.getLaunchIntentForPackage(SHIZUKU_PACKAGE)
                            ?.let { context.startActivity(it) }
                    }
                    ShizukuButtonState.CONNECT -> onConnectClick()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(text = buttonLabel, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SetupStep(number: String, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceVariant)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = number,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 10.dp, vertical = 4.dp)
        )
        Spacer(Modifier.size(12.dp))
        Text(text = text, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}
