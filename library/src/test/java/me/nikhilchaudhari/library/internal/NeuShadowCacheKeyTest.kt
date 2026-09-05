package me.nikhilchaudhari.library.internal

import androidx.compose.ui.graphics.Color
import me.nikhilchaudhari.library.NeuPerformanceConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Test

class NeuShadowCacheKeyTest {

    @After
    fun resetPerformanceConfig() {
        NeuPerformanceConfig.blurDownsampling = 2
    }

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
    fun `elevation is quantized to half-pixel buckets so nearby values share a key`() {
        assertEquals(key(elevation = 6.00f), key(elevation = 6.10f))
        assertEquals(key(elevation = 6.00f), key(elevation = 5.90f))
    }

    @Test
    fun `elevation crossing a half-pixel boundary changes the key`() {
        assertNotEquals(key(elevation = 6.0f), key(elevation = 6.5f))
    }

    @Test
    fun `stroke width is quantized the same way as elevation`() {
        assertEquals(key(stroke = 5.0f), key(stroke = 5.2f))
        assertNotEquals(key(stroke = 5.0f), key(stroke = 5.6f))
    }

    @Test
    fun `changing blur downsampling changes the key`() {
        NeuPerformanceConfig.blurDownsampling = 2
        val normalQualityKey = key()

        NeuPerformanceConfig.blurDownsampling = 3
        val lowerCpuQualityKey = key()

        assertNotEquals(normalQualityKey, lowerCpuQualityKey)
    }
}

class BlurConfigTest {

    @Test
    fun `default sampling downsamples before blurring`() {
        assertEquals(2, BlurConfig.DEFAULT_SAMPLING)
    }

    @Test
    fun `max radius is capped to a sane value`() {
        assertTrue(BlurConfig.MAX_RADIUS in 1..25)
    }
}
