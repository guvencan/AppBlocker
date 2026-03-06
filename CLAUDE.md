# CLAUDE.md — SIMAppBlocker / AppVault

Bu dosya Claude Code'un bu projede çalışırken uyması gereken kuralları ve proje bilgilerini içerir.

## Proje
- **Ad**: AppVault | **Package**: `com.godofcodes.simappblocker`
- **Mimari**: Jetpack Compose + MVVM + Shizuku
- **Dil**: Kotlin | **Build**: Gradle KTS

## Geliştirme Kuralları
- Yeni dosya oluşturmadan önce mevcut dosyaları incele ve mümkünse düzenle
- Gereksiz refactoring yapma, sadece istenen değişikliği uygula
- Commit yapmadan önce kullanıcıdan onay al
- Play Store'a push yapmadan önce her zaman onay iste
- `bold-syntax-489408-t6-b461291a74e0.json` dosyasını asla commit etme (Google API key)

## Build & Deploy
```bash
# Debug build
./gradlew assembleDebug

# Release AAB
./gradlew bundleRelease

# Fastlane lanes
bundle exec fastlane build       # AAB oluştur
bundle exec fastlane metadata    # Metadata yükle
bundle exec fastlane internal    # Internal track deploy
bundle exec fastlane promote     # Internal -> Production
bundle exec fastlane production  # Production draft
```

## Fastlane Metadata Dilleri
en-US, tr-TR, de-DE, fr-FR, es-ES, ru-RU, ar, ja-JP, zh-CN, ko-KR, pt-BR

## Mimari Notlar
- Shizuku bağlantısı `MainActivity`'de yönetilir
- `AppManagerService` AIDL ile Shizuku user service olarak çalışır
- ViewModel -> UseCase -> Repository katman yapısı korunmalı

## Hafıza Dosyası
`~/.claude/projects/-Users-guvencan-Desktop-Project-SIMAppBlocker/memory/MEMORY.md`