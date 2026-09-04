package me.nikhilchaudhari.library.internal

import android.graphics.Bitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
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

    @Test
    fun getReturnsTheSameSharedInstanceAcrossMultipleCalls() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        // This is the core guarantee the launch-freeze fix depends on: no
        // matter how many neumorphic components ask for a BlurMaker, they must
        // all get back the exact same instance (and therefore share the same
        // underlying RenderScript context on API < 31) rather than each
        // triggering their own expensive construction.
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
    }
}

@RunWith(AndroidJUnit4::class)
class NeuShadowCacheInstrumentedTest {

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

        // A recycled bitmap is unusable - the cache must treat it as a miss
        // rather than handing back a bitmap that will crash on draw.
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
}
