---
name: store-seo
description: Write or improve Play Store metadata (title, short description, full description) for AppVault. Use when the user wants to update store listing text, improve SEO, add keywords, or translate store descriptions.
---

## AppVault — Play Store SEO

**App identity**: AppVault is a debloater / app manager for Android that requires Shizuku (ADB-level access via wireless debugging — no root needed).

**Target users**: Power users, tech-savvy Android users, Xiaomi/Samsung/Huawei users frustrated by bloatware, users who know UAD (Universal Android Debloater) or Canta.

**Core value propositions**:
- Remove bloatware / pre-installed apps without root
- Hide/disable apps without uninstalling
- Recover uninstalled system apps
- Works via Shizuku (wireless debugging, no PC needed after setup)

**Key SEO keywords to include naturally**:
- debloater, debloat, bloatware remover, bloatware cleaner
- app manager, system app remover
- Shizuku, ADB, no root
- disable apps, hide apps, uninstall system apps
- Xiaomi, Samsung, MIUI, One UI bloatware
- UAD, Canta (alternative/similar apps — users searching these)
- privacy, performance, battery

**Rules for store text**:
1. Title: max 30 chars — keep "AppVault" brand
2. Short description: max 80 chars — hook + main keyword
3. Full description: 4000 chars max — use bullet points, clear sections, natural keyword density
4. Tone: direct, technical, confident — not marketing fluff
5. Each language must feel native, not machine-translated
6. Don't mention features that don't exist yet

**Supported languages**: en-US, tr-TR, de-DE, fr-FR, es-ES, ru-RU, ar, ja-JP, zh-CN, ko-KR, pt-BR

**Metadata file paths**:
```
fastlane/metadata/android/<lang>/
  title.txt            (30 chars max)
  short_description.txt (80 chars max)
  full_description.txt  (4000 chars max)
  changelogs/<versionCode>.txt (500 chars max)
```

When writing store text:
- Read existing descriptions first to understand current positioning
- Suggest improvements with keyword reasoning
- Write all 11 languages if asked, or a specific one
- For changelogs: be concise, user-facing language, bullet points, max ~6 items