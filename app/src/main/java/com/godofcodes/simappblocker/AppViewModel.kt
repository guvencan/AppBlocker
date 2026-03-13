package com.godofcodes.simappblocker

import android.app.Application
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.godofcodes.simappblocker.data.AppRepository
import com.godofcodes.simappblocker.domain.GetInstalledAppsUseCase
import com.godofcodes.simappblocker.domain.RecoverAppUseCase
// import com.godofcodes.simappblocker.domain.ToggleAppVisibilityUseCase
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
    // private var toggleVisibilityUseCase: ToggleAppVisibilityUseCase? = null
    private var uninstallUseCase: UninstallAppUseCase? = null
    private var recoverUseCase: RecoverAppUseCase? = null

    val filteredApps: StateFlow<List<AppItem>> = _uiState
        .map { state ->
            var base = if (state.showSystem) state.allApps
            else state.allApps.filter { !it.isSystem || it.isBloatware } // bloatware always visible
            if (state.showHiddenOnly) base = base.filter { it.isHidden }
            if (state.showRemovedOnly) base = base.filter { it.isUninstalled }
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

    init {
        loadAppsFromSystem()
    }

    private fun loadAppsFromSystem() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            val apps = GetInstalledAppsUseCase(AppRepository(getApplication())).invoke()
            _uiState.update { it.copy(isLoading = false, allApps = apps, scrollToTopEvent = System.currentTimeMillis()) }
            loadIconsAsync(apps)
        }
    }

    fun onServiceConnected(appManager: IAppManager) {
        val repo = AppRepository(getApplication(), appManager)
        getAppsUseCase = GetInstalledAppsUseCase(repo)
        // toggleVisibilityUseCase = ToggleAppVisibilityUseCase(repo)
        uninstallUseCase = UninstallAppUseCase(repo)
        recoverUseCase = RecoverAppUseCase(repo)
        _uiState.update { it.copy(isServiceConnected = true) }
        loadApps()
    }

    fun onServiceDisconnected() {
        getAppsUseCase = null
        // toggleVisibilityUseCase = null
        uninstallUseCase = null
        recoverUseCase = null
        _uiState.update { it.copy(isServiceConnected = false, selectedItem = null, confirmDeleteItem = null) }
        loadAppsFromSystem()
    }

    private fun loadApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val newApps = getAppsUseCase?.invoke() ?: return@launch
            val current = _uiState.value.allApps.associateBy { it.packageName }
            // Update existing items in-place, append truly new ones
            val merged = newApps.map { new ->
                current[new.packageName]?.copy(
                    isHidden = new.isHidden,
                    isSystem = new.isSystem,
                    isBloatware = new.isBloatware,
                    isUninstalled = new.isUninstalled
                ) ?: new
            }
            val brandNew = newApps.filter { it.packageName !in current }
            _uiState.update { it.copy(allApps = merged) }
            loadIconsAsync(brandNew)
        }
    }

    private fun loadIconsAsync(apps: List<AppItem>) {
        val pm = getApplication<Application>().packageManager
        val flags = PackageManager.MATCH_DISABLED_COMPONENTS or PackageManager.GET_UNINSTALLED_PACKAGES
        apps.forEach { app ->
            if (iconCache.containsKey(app.packageName)) return@forEach
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    @Suppress("DEPRECATION")
                    val info = pm.getApplicationInfo(app.packageName, flags)
                    val drawable = pm.getApplicationIcon(info)
                    withContext(Dispatchers.Main) { iconCache[app.packageName] = drawable }
                } catch (_: Exception) {}
            }
        }
    }

    fun selectItem(item: AppItem) = _uiState.update { it.copy(selectedItem = item) }
    fun clearSelection() = _uiState.update { it.copy(selectedItem = null) }

    fun requestDelete(item: AppItem) = _uiState.update { it.copy(selectedItem = null, confirmDeleteItem = item) }
    fun clearConfirmDelete() = _uiState.update { it.copy(confirmDeleteItem = null) }

    /*
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
    */

    fun uninstall(item: AppItem) {
        viewModelScope.launch {
            uninstallUseCase?.invoke(item)?.fold(
                onSuccess = {
                    val updated = _uiState.value.allApps.map {
                        if (it.packageName == item.packageName) it.copy(isUninstalled = true) else it
                    }
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

    fun recover(item: AppItem) {
        viewModelScope.launch {
            recoverUseCase?.invoke(item)?.fold(
                onSuccess = {
                    val updated = _uiState.value.allApps.map {
                        if (it.packageName == item.packageName) it.copy(isUninstalled = false) else it
                    }
                    val msg = getApplication<Application>().getString(R.string.toast_recovered, item.label)
                    _uiState.update { it.copy(allApps = updated, selectedItem = null, snackbarEvent = SnackbarEvent(msg)) }
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
    fun setShowRemovedOnly(show: Boolean) = _uiState.update {
        it.copy(
            showRemovedOnly = show,
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
