package me.nikhilchaudhari.library.internal

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * These tests cover [NeuShadowCache.keyFor] in isolation - the string-building
 * logic that decides which shadow bitmaps get shared vs regenerated. They run
 * as plain JVM unit tests (no Android framework needed) since they never touch
 * [android.graphics.Bitmap] or [android.util.LruCache], only the key builder.
 */
class NeuShadowCacheKeyTest {

    private fun key(
        pass: String = "fg",
        width: Int = 100,
        height: Int = 40,
        elevation: Float = 6f,
        stroke: Float = 6f,
        light: Color = Color.White,
        dark: Color = Color.DarkGray,
        corner: String = "Rounded(12.dp)",
        lightSource: String = "TOP_LEFT"
    ) = NeuShadowCache.keyFor(pass, width, height, elevation, stroke, light, dark, corner, lightSource)

    @Test
    fun `identical inputs produce identical keys`() {
        assertEquals(key(), key())
    }

    @Test
    fun `different pass name changes the key`() {
        // "fg" (Pressed shadow) and "bg-light"/"bg-dark" (Punched/Pot shadows)
        // must never collide, even with the same size/colors, since they hold
        // visually different bitmaps.
        assertNotEquals(key(pass = "fg"), key(pass = "bg-light"))
    }

    @Test
    fun `different size changes the key`() {
        assertNotEquals(key(width = 100), key(width = 101))
    }

    @Test
    fun `different light source changes the key`() {
        assertNotEquals(key(lightSource = "TOP_LEFT"), key(lightSource = "BOTTOM_RIGHT"))
    }

    @Test
    fun `different colors change the key`() {
        assertNotEquals(key(light = Color.White), key(light = Color.Red))
    }

    @Test
    fun `elevation is quantized to half-dp buckets so nearby values share a key`() {
        // During a spring press animation, elevation changes on essentially
        // every frame (e.g. 6.00dp, 5.97dp, 5.94dp, ...). Without quantization
        // every one of those frames would be a cache miss. Values within the
        // same 0.5dp bucket must produce the same key.
        assertEquals(key(elevation = 6.00f), key(elevation = 6.10f))
        assertEquals(key(elevation = 6.00f), key(elevation = 5.90f))
    }

    @Test
    fun `elevation crossing a half-dp boundary changes the key`() {
        assertNotEquals(key(elevation = 6.0f), key(elevation = 6.5f))
    }

    @Test
    fun `stroke width is quantized the same way as elevation`() {
        assertEquals(key(stroke = 5.0f), key(stroke = 5.2f))
        assertNotEquals(key(stroke = 5.0f), key(stroke = 5.6f))
    }
}

/**
 * [NeuShadowCache] itself (put/get/clear) touches [android.graphics.Bitmap] and
 * [android.util.LruCache], both of which are Android-framework classes that
 * throw under plain JUnit unless the module's testOptions enable
 * `returnDefaultValues`/Robolectric. Exercising the actual cache storage (as
 * opposed to just the key builder above) belongs in an instrumented
 * (androidTest) test running on a real device/emulator - see
 * NeuShadowCacheInstrumentedTest in src/androidTest.
 */
class BlurConfigTest {

    @Test
    fun `default sampling downsamples before blurring`() {
        // This is the whole point of the downsampling optimization - if this
        // regresses to 1, blur cost silently goes back to full resolution.
        assertEquals(2, BlurConfig.DEFAULT_SAMPLING)
    }

    @Test
    fun `max radius is capped to a sane value`() {
        assert(BlurConfig.MAX_RADIUS in 1..25)
    }
}
