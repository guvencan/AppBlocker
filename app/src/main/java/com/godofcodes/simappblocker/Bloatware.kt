package com.godofcodes.simappblocker

object Bloatware {

    val packages: Set<String> = setOf(

        // ── Samsung / One UI ──────────────────────────────────────────────
        "com.samsung.android.bixby.agent",
        "com.samsung.android.bixby.wakeup",
        "com.samsung.android.bixbyvision.framework",
        "com.samsung.android.app.tips",
        "com.samsung.android.game.gamehome",
        "com.samsung.android.app.spage",           // Samsung Daily / Upday
        "com.samsung.android.aremoji",
        "com.samsung.android.arzone",
        "com.samsung.android.video",
        "com.samsung.android.rubin.app",
        "com.samsung.android.livestreaming",
        "com.samsung.android.stickercenter",
        "com.samsung.android.app.sharelive",
        "com.samsung.android.app.omcagent",
        "com.samsung.android.kidsinstaller",
        "com.samsung.android.smartswitchassistant",
        "com.samsung.android.mateagent",
        "com.sec.android.app.popupcalculator",
        "com.sec.android.easyMover",
        "com.samsung.android.app.dofviewer",
        "com.samsung.android.app.watchmanagerstub",
        "com.samsung.android.beaconmanager",
        "com.samsung.android.samsungpositioning",
        "com.samsung.android.app.social",
        "com.samsung.android.shortcutbackupservice",

        // ── Huawei / Honor / EMUI ─────────────────────────────────────────
        "com.huawei.himovie.overseas",
        "com.huawei.music",
        "com.huawei.browser",
        "com.huawei.tips",
        "com.huawei.gamecenter",
        "com.hwcloudai.assistant",
        "com.huawei.vassistant",
        "com.huawei.search",
        "com.huawei.intelligent",
        "com.huawei.android.totemweather",
        "com.huawei.iconnect",
        "com.huawei.wallet",
        "com.huawei.nearby",
        "com.huawei.hiskytone",
        "com.huawei.android.thememanager",
        "com.huawei.android.tips",
        "com.huawei.phoneservice",
        "com.huawei.livewallpaper.fantasysky",

        // ── Xiaomi / MIUI / Redmi / POCO ─────────────────────────────────

        // Telemetri & Reklam
        "com.miui.analytics",                      // kullanım istatistikleri
        "com.miui.msa.global",                     // merkezi reklam ve veri servisi
        "com.miui.systemAdSolution",               // sistem içi reklam motoru
        "com.xiaomi.joyose",                       // davranış analitik servisi
        "com.miui.hybrid",                         // web tabanlı reklam servisi
        "com.miui.hybrid.accessory",               // reklam servis eklentisi
        "com.miui.contentextension",               // içerik takip motoru
        "com.xiaomi.ab",                           // A/B test framework

        // Arka planda internete çıkan servisler
        "com.miui.weather2",                       // hava durumu (veri satar)
        "com.miui.daemon",                         // günlük push bildirimleri
        "com.miui.catcherpatch",                   // uygulama çökme raporları (Xiaomi'ye gider)
        "com.miui.bugreport",                      // hata raporu servisi
        // "com.miui.core",                        // MIUI çekirdek telemetri — sistem kararlılığını etkileyebilir

        // Operatör / Reklam ortaklıkları
        "com.miui.contentcatcher",                 // içerik öneri motoru
        "com.miui.newhome",                        // kilit ekranı reklamları
        "com.miui.personalassistant",              // AI öneri motoru

        // Gereksiz ön yüklü uygulamalar
        "com.xiaomi.mipicks",                      // Mi App Store önerileri
        "com.miui.videoplayer",                    // Mi Video
        "com.miui.player",                         // Mi Müzik
        "com.mi.globalbrowser",                    // Mi Tarayıcı
        "com.mi.globalnews",                       // Mi Haber
        "com.xiaomi.midrop",                       // dosya paylaşım uygulaması
        "com.duokan.phone.remoter",                // Duokan uzaktan kumanda
        "com.xiaomi.shareme",                      // ShareMe dosya transferi
        "com.xiaomi.scanner",                      // Mi QR tarayıcı
        "com.miui.voiceassist",                    // Ses asistanı
        "com.miui.cleanmaster",                    // Mi Temizleyici
        "com.xiaomi.payment",                      // Mi Pay
        "com.mipay.wallet",                        // Mi Cüzdan
        "com.xiaomi.micreditglobal",               // Mi Kredi

        // Before Logout Mi account
        // !! Cihaz kilitlenme riski — dikkatli sil !!
        // "com.xiaomi.account",                   // Mi Hesabı servisi — silme, cihaz kilitlenebilir
        // "com.miui.cloudservice",                // Mi Cloud senkron servisi — silme, cihaz kilitlenebilir
        // "com.miui.cloudservice.global",         // Global bulut servisi — silme, cihaz kilitlenebilir
        // "com.miui.cloudbackup",                 // Bulut yedekleme — silme, cihaz kilitlenebilir

        // !! Duvar kağıdı gider, silme !!
        // "com.miui.miwallpaper",                 // MIUI duvar kağıdı motoru — silersen duvar kağıdı gider
        "com.miui.android.fashiongallery",         // Duvar Kağıdı Döngüsü (MIUI)
        "com.mfashiongallery.emag",                // Duvar Kağıdı Döngüsü (HyperOS)


        // ── OnePlus / OxygenOS ────────────────────────────────────────────
        "com.oneplus.brickmode",
        "com.oneplus.tips",
        "com.oneplus.games",
        "net.oneplus.odm",
        "com.oneplus.wallpaperstore",
        "com.oneplus.weather",
        "com.oneplus.appmarket",

        // ── OPPO / ColorOS ────────────────────────────────────────────────
        "com.oppo.market",
        "com.heytap.browser",
        "com.heytap.usercenter",
        "com.coloros.gamespaceui",
        "com.oplus.ocloud",
        "com.coloros.weather2",
        "com.coloros.wallpaperpicker",
        "com.heytap.pictorial",

        // ── Vivo / FuntouchOS ─────────────────────────────────────────────
        "com.vivo.appstore",
        "com.vivo.browser",
        "com.vivo.game",
        "com.vivo.abe",
        "com.vivo.smartshot",
        "com.vivo.hybridcomponent",

        // ── Realme / RealmeUI ─────────────────────────────────────────────
        "com.realme.market",
        "com.heytap.cloud",
        "com.realme.store",

        // ── Facebook (ön yüklü) ───────────────────────────────────────────
        "com.facebook.katana",
        "com.facebook.services",
        "com.facebook.system",
        "com.facebook.appmanager",
        "com.facebook.orca",
        "com.facebook.lite",

        // ── Yaygın partner / operatör bloatware ──────────────────────────
        "com.amazon.mShop.android.shopping",
        "com.booking",
        "com.linkedin.android",
        "com.opera.browser",
        "com.opera.mini.native",
        "com.microsoft.skypeforusiness",
        "com.ebay.mobile",
        "com.tripadvisor.tripadvisor",
        "com.gameloft.android.ANMP.GloftDIHM"
    )
}
