package com.godofcodes.simappblocker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.godofcodes.simappblocker.AppViewModel

internal object Routes {
    const val SETUP = "setup"
    const val APP_LIST = "applist"
}

@Composable
fun AppNavigation(viewModel: AppViewModel, onConnectClick: () -> Unit) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentRoute by navController.currentBackStackEntryAsState()

    LaunchedEffect(uiState.isServiceConnected) {
        if (uiState.isServiceConnected) {
            navController.navigate(Routes.APP_LIST) {
                popUpTo(Routes.SETUP) { inclusive = true }
            }
        } else if (currentRoute?.destination?.route != Routes.SETUP) {
            navController.navigate(Routes.SETUP) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.SETUP,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
    ) {
        composable(Routes.SETUP) {
            SetupScreen(onConnectClick = onConnectClick)
        }
        composable(Routes.APP_LIST) {
            AppListScreen(viewModel = viewModel)
        }
    }
}
