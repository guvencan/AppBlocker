package com.godofcodes.simappblocker

data class SnackbarEvent(val message: String, val id: Long = System.currentTimeMillis())

data class AppUiState(
    val isServiceConnected: Boolean = false,
    val isLoading: Boolean = false,
    val allApps: List<AppItem> = emptyList(),
    val query: String = "",
    val showSystem: Boolean = false,
    val showHiddenOnly: Boolean = false,
    val selectedItem: AppItem? = null,
    val confirmDeleteItem: AppItem? = null,
    val snackbarEvent: SnackbarEvent? = null
)
