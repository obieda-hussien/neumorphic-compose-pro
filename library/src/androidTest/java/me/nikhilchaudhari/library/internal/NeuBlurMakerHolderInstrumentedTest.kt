package me.nikhilchaudhari.library.internal

import android.graphics.Bitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Runs on a real device/emulator since it needs a real [android.content.Context]
 * and the actual Android graphics stack (Bitmap, RenderScript/StackBlur).
 */
@RunWith(AndroidJUnit4::class)
class NeuBlurMakerHolderInstrumentedTest {

    @After
    fun cleanup() {
        NeuShadowCache.clear()
    }

    @Test
    fun getReturnsTheSameSharedInstanceAcrossMultipleCalls() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        val first = NeuBlurMakerHolder.get(context)
        val second = NeuBlurMakerHolder.get(context)
        val third = NeuBlurMakerHolder.get(context)

        assertSame(first, second)
        assertSame(second, third)
    }

    @Test
    fun warmUpDoesNotThrowAndInstanceIsUsableAfterwards() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        NeuBlurMakerHolder.warmUp(context)
        val blurMaker = NeuBlurMakerHolder.get(context)

        val source = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
        val blurred = blurMaker.blur(source)

        assertNotNull("blur() should return a bitmap after warmUp()", blurred)
        if (blurred !== source) blurred?.recycle()
        source.recycle()
    }

    @Test
    fun concurrentBlurCallsRemainSafeAndReturnUsableBitmaps() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val blurMaker = NeuBlurMakerHolder.get(context)
        val executor = Executors.newFixedThreadPool(4)
        val source = Bitmap.createBitmap(96, 64, Bitmap.Config.ARGB_8888)

        try {
            val tasks = (0 until 12).map {
                Callable {
                    val result = blurMaker.blur(
                        source,
                        radius = 12,
                        sampling = if (it % 2 == 0) 2 else 3
                    )
                    try {
                        result != null && !result.isRecycled && result.width == source.width && result.height == source.height
                    } finally {
                        if (result != null && result !== source && !result.isRecycled) result.recycle()
                    }
                }
            }

            val results = executor.invokeAll(tasks, 30, TimeUnit.SECONDS)
            assertEquals(12, results.size)
            results.forEach { future ->
                assertTrue("concurrent blur should complete successfully", future.get())
            }
        } finally {
            executor.shutdownNow()
            source.recycle()
        }
    }
}

@RunWith(AndroidJUnit4::class)
class NeuShadowCacheInstrumentedTest {

    @After
    fun cleanup() {
        NeuShadowCache.clear()
    }

    @Test
    fun putThenGetReturnsTheSameBitmap() {
        val bitmap = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888)
        val key = "test-key-${System.nanoTime()}"

        NeuShadowCache.put(key, bitmap)
        val cached = NeuShadowCache.get(key)

        assertSame(bitmap, cached)
    }

    @Test
    fun getOnMissingKeyReturnsNull() {
        val cached = NeuShadowCache.get("this-key-does-not-exist-${System.nanoTime()}")
        assertEquals(null, cached)
    }

    @Test
    fun getAfterRecycleReturnsNullAndDropsTheEntry() {
        val bitmap = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888)
        val key = "recycled-key-${System.nanoTime()}"
        NeuShadowCache.put(key, bitmap)

        bitmap.recycle()

        assertEquals(null, NeuShadowCache.get(key))
    }

    @Test
    fun clearEvictsEverything() {
        val bitmap = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888)
        val key = "clear-key-${System.nanoTime()}"
        NeuShadowCache.put(key, bitmap)
        assertTrue(NeuShadowCache.get(key) != null)

        NeuShadowCache.clear()

        assertEquals(null, NeuShadowCache.get(key))
    }

    @Test
    fun tinyBudgetAcceptsEntriesAndCanBeRestored() {
        val first = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
        val second = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)

        NeuShadowCache.resizeBudget(1)
        NeuShadowCache.put("pressure-a", first)
        NeuShadowCache.put("pressure-b", second)

        // LruCache may evict one or both depending on the entry size. The key
        // requirement is that the cache remains usable after aggressive trim.
        assertTrue(
            NeuShadowCache.get("pressure-a") == null || NeuShadowCache.get("pressure-b") == null
        )

        NeuShadowCache.resizeBudget(6 * 1024)
    }
}
