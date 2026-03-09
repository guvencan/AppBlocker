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

## Related Projects & Inspiration

AppVault is part of the Android debloating community, building on the work of:

- **[Universal Android Debloater (UAD)](https://github.com/0x192/universal-android-debloater)** — A cross-platform GUI tool for debloating Android devices via ADB from a PC. UAD pioneered the no-root debloating approach with its comprehensive package database covering Samsung, Xiaomi, Huawei, OPPO, and carrier bloatware.
- **[Canta](https://github.com/samolego/Canta)** — An on-device app uninstaller for Android that also uses Shizuku. AppVault extends this concept with app hiding/disabling capabilities and a modern Material 3 Compose UI.

AppVault brings the best of both worlds: the power of UAD's debloating philosophy and Canta's on-device Shizuku approach — no PC required.

## Common Bloatware AppVault Removes

AppVault is widely used to disable or remove:

| Category | Apps |
|----------|------|
| Samsung | Bixby, Galaxy Store, Samsung Pay, Samsung Internet, Knox, Game Launcher, AR Zone, Bixby Vision, Samsung Health |
| Carrier | SIM Toolkit, carrier services, operator bloatware (T-Mobile, AT&T, Verizon apps) |
| Facebook / Meta | Facebook App Manager, Facebook Services, pre-installed Instagram |
| Xiaomi / MIUI | Mi Browser, Mi Video, GetApps, Security app, Mi AI |
| Huawei | AppGallery, Petal Search, HiCare, Huawei Health |
| OPPO / Realme | OPPO Market, HeyTap, Theme Store, OPPO Browser |
| Google extras | Google One, Play Movies, Google TV, Stadia, Google Play Books |
| Others | LinkedIn (pre-installed), Netflix (pre-installed), TikTok, Snapchat (OEM deals) |

## License

MIT
