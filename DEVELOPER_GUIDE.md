# AppVault — Developer Guide

## Project Structure

```
app/src/main/
├── aidl/com/godofcodes/simappblocker/
│   └── IAppManager.aidl              # AIDL interface for Shizuku UserService
└── java/com/godofcodes/simappblocker/
    ├── data/
    │   └── AppRepository.kt          # Data access: PackageManager + Shizuku commands
    ├── domain/
    │   ├── GetInstalledAppsUseCase.kt
    │   ├── ToggleAppVisibilityUseCase.kt
    │   └── UninstallAppUseCase.kt
    ├── ui/
    │   ├── theme/
    │   │   ├── Color.kt              # Dark grey + burgundy palette
    │   │   └── Theme.kt              # MaterialTheme (darkColorScheme)
    │   ├── SetupScreen.kt            # Shizuku setup instructions screen
    │   ├── AppListScreen.kt          # App list, shimmer, action/delete dialogs
    │   └── AppNavigation.kt          # NavHost with "setup" and "applist" routes
    ├── AppItem.kt                    # Data class (packageName, label, icon, isHidden, isSystem)
    ├── AppUiState.kt                 # Single state class for the entire app UI
    ├── AppViewModel.kt               # AndroidViewModel, StateFlow, viewModelScope
    ├── AppManagerService.kt          # Shizuku UserService — executes shell commands
    └── MainActivity.kt              # ComponentActivity: Shizuku binding + setContent
```

## Architecture

```
MainActivity (Shizuku binding)
    ↓ onServiceConnected(IAppManager)
AppViewModel (AndroidViewModel)
    ↓ creates
AppRepository (data layer)
    ↓ injected into
UseCases (domain layer)
    ↓ called by ViewModel
StateFlow<AppUiState>  ←→  Compose UI
```

### Navigation

Two Compose screens under a single `NavHost`:

| Route | Screen | When shown |
|-------|--------|-----------|
| `setup` | `SetupScreen` | Shizuku not connected |
| `applist` | `AppListScreen` | Shizuku connected |

`AppNavigation` observes `uiState.isServiceConnected` and calls `navController.navigate()` reactively via `LaunchedEffect`.

### State management

All UI state lives in `AppUiState` (single source of truth):

```kotlin
data class AppUiState(
    val isServiceConnected: Boolean,
    val isLoading: Boolean,
    val allApps: List<AppItem>,
    val query: String,
    val showSystem: Boolean,
    val showHiddenOnly: Boolean,
    val selectedItem: AppItem?,       // drives ActionDialog visibility
    val confirmDeleteItem: AppItem?,  // drives ConfirmDeleteDialog visibility
    val snackbarEvent: SnackbarEvent?  // carries message + unique id to handle same-message-twice
)
```

`filteredApps` is a derived `StateFlow` mapped from `_uiState` — filtering is pure and runs automatically when state changes.

### Shizuku / AIDL

`AppManagerService` runs in an ADB-privileged process via Shizuku's `bindUserService`. It executes shell commands and returns stdout/stderr.

**Why UserService instead of `Shizuku.newProcess`:** `newProcess` was deprecated/made private in Shizuku API 13. UserService via AIDL is the recommended approach.

### AppItem immutability

`AppItem` is a fully immutable data class (`val` fields). When toggling visibility, the ViewModel replaces the item in `allApps`:

```kotlin
val updated = allApps.map {
    if (it.packageName == item.packageName) it.copy(isHidden = newHidden) else it
}
```

This ensures Compose recomposition correctly detects changes.

### Shimmer loading

The shimmer effect is implemented with pure Compose animations (`rememberInfiniteTransition` + `Brush.linearGradient`) — no external library required. Facebook Shimmer dependency was removed during the Compose migration.

### Drawable → Compose Image

App icons are stored as `Drawable` (from `PackageManager`). They are converted to `ImageBitmap` via a `remember`-cached bitmap render:

```kotlin
@Composable
fun rememberDrawablePainter(drawable: Drawable): BitmapPainter {
    val bitmap = remember(drawable) {
        val bmp = Bitmap.createBitmap(...)
        drawable.draw(Canvas(bmp))
        bmp.asImageBitmap()
    }
    return BitmapPainter(bitmap)
}
```

## Build Configuration

**`gradle/libs.versions.toml`** — key additions:
```toml
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
```

**`app/build.gradle.kts`:**
```kotlin
plugins {
    alias(libs.plugins.kotlin.compose)  // Kotlin 2.0+ compose compiler plugin
}
buildFeatures {
    compose = true
    aidl = true
    buildConfig = true  // exposes BuildConfig.DEBUG for Shizuku UserService debuggable flag
}
```

With Kotlin 2.0+, the Compose compiler is bundled — no `kotlinCompilerExtensionVersion` needed.

## Building & Running

```bash
./gradlew assembleDebug
./gradlew installDebug
```

Minimum: Android Studio Meerkat+, JDK 11, Android SDK 36.

## Adding a New Language

1. Create `app/src/main/res/values-<locale>/strings.xml` with all keys from `values/strings.xml`
2. Add `fastlane/metadata/android/<locale>/title.txt`, `short_description.txt`, `full_description.txt`

## Key Patterns & Gotchas

### Snackbar same-message bug
`LaunchedEffect(message: String)` won't refire if the same string is emitted twice. Use `SnackbarEvent(message, id = System.currentTimeMillis())` as the `LaunchedEffect` key.

### NavHost + edge-to-edge double-inset
`navigation-compose` 2.7+ internally applies window insets to each destination. If screens also apply `statusBarsPadding()`, insets are applied twice. Solution: apply `safeDrawingPadding()` only on the `NavHost` modifier (it applies *and* consumes insets for children); use `Scaffold(contentWindowInsets = WindowInsets(0))` inside screens so Scaffold doesn't re-add insets.

### Shizuku UserService vs newProcess
`Shizuku.newProcess()` was deprecated in API 13. Use `Shizuku.bindUserService()` with an AIDL `UserService`. Declare the service in `AndroidManifest.xml` with `android:process` and `android:permission`.

### AppItem immutability
All `AppItem` fields are `val`. When toggling visibility, create a new list via `.map { if (it.packageName == ...) it.copy(isHidden = ...) else it }`. This ensures Compose detects the state change.

### PackageManager flags for hidden apps
`MATCH_DISABLED_COMPONENTS or MATCH_UNINSTALLED_PACKAGES` must both be set in `getInstalledApplications()` to retrieve apps disabled via `pm disable-user`. Without these flags, hidden apps silently disappear from the list.

## Common Issues

| Issue | Cause | Fix |
|-------|-------|-----|
| Blank screen / white flash | Window background not set | `themes.xml` sets `android:colorBackground` |
| App list empty | Shizuku not running | Start Shizuku, tap Connect |
| Hidden apps missing from list | Missing PM flags | `MATCH_DISABLED_COMPONENTS or MATCH_UNINSTALLED_PACKAGES` in `AppRepository` |
| Compose compiler error | Missing plugin | Add `alias(libs.plugins.kotlin.compose)` and `compose = true` in buildFeatures |
| AIDL not generated | `aidl = true` missing | Add to `buildFeatures {}` in `app/build.gradle.kts` |
