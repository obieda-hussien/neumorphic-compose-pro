package me.nikhilchaudhari.library.internal

import android.graphics.Bitmap
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import me.nikhilchaudhari.library.*
import me.nikhilchaudhari.library.shapes.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicInteger

@RunWith(AndroidJUnit4::class)
class NeuEngineRegressionTest {
    @After fun restore() { NeuShadowCache.clear(); NeuShadowCache.restoreConfiguredBudget() }

    @Test fun protectedEntriesRespectTotalBudget() {
        NeuShadowCache.clear()
        NeuShadowCache.resizeBudget(16)
        repeat(20) { i ->
            NeuShadowCache.put("$i", Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888))
            repeat(3) { NeuShadowCache.get("$i") }
        }
        assertTrue(NeuShadowCache.memoryStats().first <= 16 * 1024L)
        NeuShadowCache.resizeBudget(1)
        assertEquals(0L, NeuShadowCache.memoryStats().first)
    }

    @Test fun workerUsesActualCorners() {
        val oval = Punched.Oval()
        val rounded = Punched.Rounded(32.dp)
        assertEquals(CornerType.Oval, oval.shadowCorners)
        assertEquals(CornerType.Rounded(32.dp), rounded.shadowCorners)
        val base = ShapeConfig(NeuInsets(), 6.dp, Color.White, Color.Black, 4.dp, oval.shadowCorners)
        val first = ShadowGeneration.requestKey(Density(2f), 100, 100, base, ShadowStyle.BACKGROUND)
        val second = ShadowGeneration.requestKey(Density(2f), 100, 100, base.copy(cornerType = rounded.shadowCorners), ShadowStyle.BACKGROUND)
        assertNotEquals(first, second)
    }

    @Test fun concurrentRequestsShareOneGeneration() = runBlocking {
        val executions = AtomicInteger()
        val gate = java.util.concurrent.CountDownLatch(1)
        val requests = List(12) { async { ShadowWorkQueue.generate("single-flight-test") {
            executions.incrementAndGet()
            gate.await(2, java.util.concurrent.TimeUnit.SECONDS)
            true
        } } }
        yield()
        delay(100)
        gate.countDown()
        assertTrue(requests.awaitAll().all { it })
        assertEquals(1, executions.get())
    }

    @Test fun gpuOutputKeepsExactRequestedDimensions() {
        Assume.assumeTrue(Build.VERSION.SDK_INT >= 31)
        if (Build.VERSION.SDK_INT >= 31) {
            val engine = RenderEffectBlurEngine(Any())
            try {
                listOf(100 to 100, 96 to 96, 103 to 99).forEach { (w, h) ->
                    val source = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                    source.eraseColor(android.graphics.Color.WHITE)
                    val result = engine.blur(source, 8)
                    assertNotNull(result)
                    assertEquals(w, result!!.width)
                    assertEquals(h, result.height)
                    if (result !== source) result.recycle()
                    source.recycle()
                }
                assertTrue("API 31+ test must exercise RenderEffect, not only its fallback", (engine.stats()["gpu"] ?: 0L) > 0L)
            } finally { engine.release() }
        }
    }
}
