package com.godofcodes.simappblocker

import android.app.Application
import android.graphics.drawable.Drawable
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.godofcodes.simappblocker.data.AppRepository
import com.godofcodes.simappblocker.domain.GetInstalledAppsUseCase
import com.godofcodes.simappblocker.domain.ToggleAppVisibilityUseCase
import com.godofcodes.simappblocker.domain.UninstallAppUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppViewModel(app: Application) : AndroidViewModel(app) {

    val iconCache = mutableStateMapOf<String, Drawable>()

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState

    private var getAppsUseCase: GetInstalledAppsUseCase? = null
    private var toggleVisibilityUseCase: ToggleAppVisibilityUseCase? = null
    private var uninstallUseCase: UninstallAppUseCase? = null

    val filteredApps: StateFlow<List<AppItem>> = _uiState
        .map { state ->
            var base = if (state.showSystem) state.allApps
            else state.allApps.filter { !it.isSystem }
            if (state.showHiddenOnly) base = base.filter { it.isHidden }
            if (state.showBloatwareOnly) base = base.filter { it.isBloatware }
            if (state.query.isNotBlank()) {
                base = base.filter {
                    it.label.contains(state.query, ignoreCase = true) ||
                            it.packageName.contains(state.query, ignoreCase = true)
                }
            }
            base
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onServiceConnected(appManager: IAppManager) {
        val repo = AppRepository(getApplication(), appManager)
        getAppsUseCase = GetInstalledAppsUseCase(repo)
        toggleVisibilityUseCase = ToggleAppVisibilityUseCase(repo)
        uninstallUseCase = UninstallAppUseCase(repo)
        _uiState.update { it.copy(isServiceConnected = true) }
        loadApps()
    }

    fun onServiceDisconnected() {
        getAppsUseCase = null
        toggleVisibilityUseCase = null
        uninstallUseCase = null
        _uiState.update { AppUiState() }
    }

    private fun loadApps() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            val apps = getAppsUseCase?.invoke() ?: emptyList()
            _uiState.update { it.copy(isLoading = false, allApps = apps) }
            loadIconsAsync(apps)
        }
    }

    private fun loadIconsAsync(apps: List<AppItem>) {
        val pm = getApplication<Application>().packageManager
        apps.forEach { app ->
            if (iconCache.containsKey(app.packageName)) return@forEach
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val drawable = pm.getApplicationIcon(app.packageName)
                    withContext(Dispatchers.Main) { iconCache[app.packageName] = drawable }
                } catch (_: Exception) {}
            }
        }
    }

    fun selectItem(item: AppItem) = _uiState.update { it.copy(selectedItem = item) }
    fun clearSelection() = _uiState.update { it.copy(selectedItem = null) }

    fun requestDelete(item: AppItem) = _uiState.update { it.copy(selectedItem = null, confirmDeleteItem = item) }
    fun clearConfirmDelete() = _uiState.update { it.copy(confirmDeleteItem = null) }

    fun toggleVisibility(item: AppItem) {
        viewModelScope.launch {
            toggleVisibilityUseCase?.invoke(item)?.fold(
                onSuccess = { newHidden ->
                    val ctx = getApplication<Application>()
                    val updated = _uiState.value.allApps.map {
                        if (it.packageName == item.packageName) it.copy(isHidden = newHidden) else it
                    }
                    val msg = if (newHidden) ctx.getString(R.string.toast_hidden, item.label)
                    else ctx.getString(R.string.toast_visible, item.label)
                    _uiState.update { it.copy(allApps = updated, selectedItem = null, snackbarEvent = SnackbarEvent(msg)) }
                },
                onFailure = { e ->
                    val msg = getApplication<Application>().getString(R.string.toast_operation_failed, e.message)
                    _uiState.update { it.copy(snackbarEvent = SnackbarEvent(msg)) }
                }
            )
        }
    }

    fun uninstall(item: AppItem) {
        viewModelScope.launch {
            uninstallUseCase?.invoke(item)?.fold(
                onSuccess = {
                    val updated = _uiState.value.allApps.filter { it.packageName != item.packageName }
                    val msg = getApplication<Application>().getString(R.string.toast_deleted, item.label)
                    _uiState.update { it.copy(allApps = updated, confirmDeleteItem = null, snackbarEvent = SnackbarEvent(msg)) }
                },
                onFailure = { e ->
                    val msg = getApplication<Application>().getString(R.string.toast_failed, e.message)
                    _uiState.update { it.copy(snackbarEvent = SnackbarEvent(msg)) }
                }
            )
        }
    }

    fun setQuery(query: String) = _uiState.update { it.copy(query = query) }
    fun setShowSystem(show: Boolean) = _uiState.update { it.copy(showSystem = show) }
    fun setShowHiddenOnly(show: Boolean) = _uiState.update {
        it.copy(
            showHiddenOnly = show,
            showSystem = if (show) true else it.showSystem
        )
    }
    fun setShowBloatwareOnly(show: Boolean) = _uiState.update {
        it.copy(
            showBloatwareOnly = show,
            showSystem = if (show) true else it.showSystem
        )
    }
    fun clearSnackbar() = _uiState.update { it.copy(snackbarEvent = null) }
}
