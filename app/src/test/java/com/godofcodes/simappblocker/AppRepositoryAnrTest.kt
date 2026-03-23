package com.godofcodes.simappblocker

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.godofcodes.simappblocker.data.AppRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.max

/**
 * ANR regresyon testi: getInstalledApps() içindeki loadLabel() çağrılarının
 * flatMapMerge(concurrency=4) ile paralel yürütüldüğünü doğrular.
 *
 * Senaryo: Her getApplicationLabel() 50ms senkron I/O simüle eder.
 *   - Sıralı (eski kod) : 20 × 50ms = ~1000ms → ANR riski
 *   - Paralel (yeni kod): ceil(20/4) × 50ms = ~250ms
 */
class AppRepositoryAnrTest {

    @Test
    fun `getInstalledApps completes fast with parallel label loading`() = runBlocking {
        val appCount = 20
        val labelDelayMs = 50L

        val concurrentCalls = AtomicInteger(0)
        val maxConcurrentCalls = AtomicInteger(0)

        val mockPm = mockk<PackageManager>()
        val mockContext = mockk<Context>()

        val fakeInfos = (1..appCount).map { i ->
            ApplicationInfo().apply { packageName = "com.fake.app$i" }
        }

        every { mockContext.packageName } returns "com.godofcodes.simappblocker"
        every { mockContext.packageManager } returns mockPm
        every { mockPm.getInstalledApplications(any<Int>()) } returns fakeInfos
        every { mockPm.getApplicationLabel(any()) } answers {
            val current = concurrentCalls.incrementAndGet()
            maxConcurrentCalls.updateAndGet { max(it, current) }
            Thread.sleep(labelDelayMs)
            concurrentCalls.decrementAndGet()
            "App ${firstArg<ApplicationInfo>().packageName}"
        }
        every { mockPm.getApplicationEnabledSetting(any()) } returns PackageManager.COMPONENT_ENABLED_STATE_DEFAULT

        val repo = AppRepository(mockContext)

        // Üretimde Dispatchers.IO üzerinde çağrılır — burada da aynı şartı sağlıyoruz
        val startMs = System.currentTimeMillis()
        val result = withContext(Dispatchers.IO) { repo.getInstalledApps() }
        val elapsedMs = System.currentTimeMillis() - startMs

        // Tüm uygulamalar dönmeli
        assertEquals(appCount, result.size)

        // Etiketler doğru atanmış olmalı
        result.forEach { item ->
            assertTrue(item.label.startsWith("App com.fake.app"))
        }

        // Paralel yürütme: sıralı limit 1000ms, paralel beklenti ~250ms → 600ms eşiği
        assertTrue(
            "loadLabel() sıralı çalışıyor olabilir: ${elapsedMs}ms (beklenen < 600ms)",
            elapsedMs < 600
        )

        // En az 4 eş zamanlı çağrıya ulaşıldığını doğrula
        assertTrue(
            "Eş zamanlı çağrı sayısı yetersiz: maks=${maxConcurrentCalls.get()} (beklenen >= 4)",
            maxConcurrentCalls.get() >= 4
        )
    }

    @Test
    fun `getInstalledApps returns correct AppItem fields`() = runBlocking {
        val mockPm = mockk<PackageManager>()
        val mockContext = mockk<Context>()

        val systemApp = ApplicationInfo().apply {
            packageName = "com.android.system"
            flags = ApplicationInfo.FLAG_SYSTEM
        }
        val userApp = ApplicationInfo().apply {
            packageName = "com.user.app"
            flags = 0
        }

        every { mockContext.packageName } returns "com.godofcodes.simappblocker"
        every { mockContext.packageManager } returns mockPm
        every { mockPm.getInstalledApplications(any<Int>()) } returns listOf(systemApp, userApp)
        every { mockPm.getApplicationLabel(match { it.packageName == "com.android.system" }) } returns "System App"
        every { mockPm.getApplicationLabel(match { it.packageName == "com.user.app" }) } returns "User App"
        every { mockPm.getApplicationEnabledSetting(any()) } returns PackageManager.COMPONENT_ENABLED_STATE_DEFAULT

        val result = AppRepository(mockContext).getInstalledApps()

        val sys = result.first { it.packageName == "com.android.system" }
        val usr = result.first { it.packageName == "com.user.app" }

        assertEquals("System App", sys.label)
        assertTrue(sys.isSystem)

        assertEquals("User App", usr.label)
        assertTrue(!usr.isSystem)
    }
}
