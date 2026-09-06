package me.nikhilchaudhari.library.internal

import android.graphics.Bitmap
import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.util.Locale
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Lightweight on-device performance smoke benchmark.
 *
 * It deliberately reports timing instead of enforcing a hard threshold because
 * emulator and CI hardware are not stable enough for a reliable millisecond budget.
 */
@RunWith(AndroidJUnit4::class)
class NeuBlurPerformanceInstrumentedTest {

    @Test
    fun repeatedBlurBenchmarkReportsWarmLatency() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val blurMaker = NeuBlurMakerHolder.get(context)
        val warmup = Bitmap.createBitmap(160, 96, Bitmap.Config.ARGB_8888)
        val samples = Bitmap.createBitmap(160, 96, Bitmap.Config.ARGB_8888)
        val timingsMs = ArrayList<Double>(10)

        try {
            val warm = blurMaker.blur(warmup, radius = 12, sampling = 2)
            assertNotNull(warm)
            if (warm !== warmup) warm?.recycle()

            repeat(10) {
                val startNs = SystemClock.elapsedRealtimeNanos()
                val result = blurMaker.blur(samples, radius = 12, sampling = 2)
                val elapsedMs = (SystemClock.elapsedRealtimeNanos() - startNs) / 1_000_000.0
                assertNotNull(result)
                timingsMs += elapsedMs
                if (result !== samples) result?.recycle()
            }

            timingsMs.sort()
            val median = timingsMs[timingsMs.lastIndex / 2]
            val p95 = timingsMs[(timingsMs.size * 0.95).toInt().coerceAtMost(timingsMs.lastIndex)]
            val average = timingsMs.average()

            println(
                String.format(
                    Locale.US,
                    "NEU_BLUR_BENCHMARK samples=%d avg=%.3fms median=%.3fms p95=%.3fms",
                    timingsMs.size,
                    average,
                    median,
                    p95
                )
            )
        } finally {
            warmup.recycle()
            samples.recycle()
            NeuShadowCache.clear()
        }
    }
}
