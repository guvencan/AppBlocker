# AppVault - Hide & Manage Apps

AppVault is an Android app that lets you hide, disable, and uninstall any app on your device — including system apps — without root access.

## Features

- **Hide apps** — Disable any app for the current user so it disappears from the launcher
- **Restore apps** — Re-enable hidden apps with a single tap
- **Uninstall apps** — Remove unwanted apps for the current user (reversible)
- **System app management** — Toggle to include pre-installed system apps
- **Search & filter** — Filter by name, package name, system/hidden status
- **No root required** — Works via Shizuku with a one-time ADB setup
- **Shimmer loading** — Smooth skeleton animation while the app list loads
- **Modern dark UI** — Dark grey + burgundy Compose UI
- **11 languages** — English, Turkish, German, French, Spanish, Russian, Arabic, Japanese, Chinese, Korean, Portuguese

## How It Works

AppVault uses [Shizuku](https://shizuku.rikka.app/) to run ADB-level shell commands directly from your phone:

| Action | Shell Command |
|--------|---------------|
| Hide app | `pm disable-user --user 0 <package>` |
| Show app | `pm enable <package>` |
| Uninstall | `pm uninstall -k --user 0 <package>` |
| List disabled | `pm list packages -d` |

All changes are **reversible**. Hidden apps are not deleted — they are only disabled for your user profile.

## Requirements

- Android 7.0+ (API 24)
- [Shizuku](https://shizuku.rikka.app/) app installed and running

## Shizuku Setup (One-Time)

1. Install Shizuku from the Play Store
2. On Android 11+, start Shizuku via Wireless Debugging (no PC needed)
3. On Android 10 and below, run once via ADB:
   ```
   adb shell sh /sdcard/Android/data/moe.shizuku.privileged.api/start.sh
   ```
4. Open AppVault — it will connect to Shizuku automatically

## Supported Languages

English, Türkçe, Deutsch, Français, Español, Русский, العربية, 日本語, 中文, 한국어, Português

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Architecture:** Single Activity, Compose Navigation, MVVM (ViewModel + StateFlow)
- **Business Logic:** Use Cases (`GetInstalledAppsUseCase`, `ToggleAppVisibilityUseCase`, `UninstallAppUseCase`)
- **Permissions bridge:** Shizuku 13.1.5 (UserService via AIDL)
- **Loading:** Custom Compose shimmer animation (no external library)
- **Build:** Gradle Kotlin DSL, compileSdk 36, minSdk 24

## License

MIT
