# Changelog

## [Unreleased]

### Fixed — Code Review Pass
- **SnackbarEvent**: Replaced `snackbarMessage: String?` with `SnackbarEvent(message, id)` to correctly refire `LaunchedEffect` when the same message is sent twice
- **BuildConfig.DEBUG**: Added `buildConfig = true` to `buildFeatures`; replaced hardcoded `debuggable(true)` in `MainActivity` with `debuggable(BuildConfig.DEBUG)`; fixed wrong `rikka.shizuku.shared.BuildConfig` import
- **Null-safe Shizuku binding**: Eliminated `!!` operator on `userServiceArgs` by extracting to a local `val args` before calling `bindUserService`
- **AppRepository**: Removed `GET_META_DATA` flag (unnecessary, caused 2–3× slower list load); `getDisabledPackages()` made `private`
- **AppNavigation**: Prevented redundant navigation back to `setup` if already on `setup` when service disconnects; removed `consumeWindowInsets` hack — `safeDrawingPadding()` on NavHost modifier handles insets end-to-end
- **AppListScreen**: Removed unused `CircleShape` import; `LaunchedEffect` now keyed on `snackbarEvent` (not `snackbarMessage`)
- **SetupScreen**: Simplified to single button with `ShizukuButtonState` enum (NOT_INSTALLED → Play Store, OPEN_SHIZUKU → launch app, CONNECT → bind service)

### Changed — Architecture & UI Migration
- **Compose migration**: Entire UI rewritten in Jetpack Compose (Material 3), ViewBinding removed
- **Single Activity + Compose Navigation**: Two screens (`SetupScreen`, `AppListScreen`) via `NavHost`; replaces XML layouts and RecyclerView
- **MVVM architecture**: Introduced `AppViewModel` (AndroidViewModel + StateFlow), `AppUiState` data class
- **Data layer**: `AppRepository` extracted from `MainActivity`
- **Domain layer**: Three use cases — `GetInstalledAppsUseCase`, `ToggleAppVisibilityUseCase`, `UninstallAppUseCase`
- **Shimmer**: Replaced Facebook Shimmer library with a pure Compose shimmer animation (`rememberInfiniteTransition` + `Brush.linearGradient`)
- **Dialogs**: `ActionDialog` and `ConfirmDeleteDialog` rewritten as Compose `AlertDialog`, state-driven via `AppUiState.selectedItem` / `confirmDeleteItem`
- **isSystem flag**: Added to `AppItem`; filtering no longer needs `PackageManager` calls at runtime
- **AppItem immutability**: `isHidden` changed from `var` to `val`; all mutations go through `copy()`
- **Deleted files**: `AppListAdapter.kt`, `activity_main.xml`, `item_app.xml`, `dialog_action.xml`, `dialog_confirm_delete.xml`

---

## [1.0.0] — Initial Release

### Added
- **App hiding** via `pm disable-user --user 0 <package>` (Shizuku/ADB)
- **App restore** via `pm enable <package>`
- **App uninstall** via `pm uninstall -k --user 0 <package>` (keeps data, reversible)
- **System app support** — toggle to include pre-installed apps in the list
- **Hidden-only filter** — show only currently hidden apps
- **Search** — filter by app name or package name
- **Custom action dialog** — Hide/Show, Delete, Cancel actions per app
- **Delete confirmation dialog** — prevents accidental uninstalls
- **Shizuku integration** via UserService AIDL (replaces deprecated `Shizuku.newProcess`)
- **Dark theme** — grey (#121212) + burgundy (#C62828) palette
- **11 language support** — en, tr, de, fr, es, ru, ar, ja, zh, ko, pt
- **SEO-optimized Play Store metadata** via Fastlane for all 11 locales
- App renamed from "SIMAppBlocker" to **AppVault**

### Fixed
- Hidden apps correctly appear in list (`MATCH_DISABLED_COMPONENTS | MATCH_UNINSTALLED_PACKAGES` flags)
- Status badge updates correctly after toggle (DiffUtil immutability fix)
- List no longer reloads on every `onResume`
