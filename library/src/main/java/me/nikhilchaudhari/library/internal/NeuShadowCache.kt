package me.nikhilchaudhari.library.internal

import android.graphics.Bitmap
import android.util.LruCache
import androidx.compose.ui.graphics.Color
import kotlin.math.roundToInt
import me.nikhilchaudhari.library.NeuPerformanceConfig

/**
 * Process-wide cache of generated (already blurred) shadow bitmaps, keyed by a
 * description of the shape/size/colors that produced them.
 *
 * A neumorphic shadow is a pure function of (size, elevation, stroke width,
 * colors, corner shape, light source, blur quality). Two composables with
 * identical params produce the same output for a given blur configuration.
 */
internal object NeuShadowCache {

    private val cache = object : LruCache<String, Bitmap>(6 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int =
            (value.byteCount / 1024).coerceAtLeast(1)
    }

    fun get(key: String): Bitmap? {
        val bitmap = cache.get(key) ?: return null
        return if (bitmap.isRecycled) {
            cache.remove(key)
            null
        } else {
            bitmap
        }
    }

    fun put(key: String, bitmap: Bitmap) {
        cache.put(key, bitmap)
    }

    fun clear() = cache.evictAll()

    fun resizeBudget(newBudgetKB: Int) {
        cache.resize(newBudgetKB.coerceAtLeast(1))
    }

    /**
     * Builds a stable cache key for a shadow bitmap.
     *
     * Elevation/stroke are quantized to half-pixel buckets because the inputs
     * are already expressed in px. The active blur downsampling factor is part
     * of the key so changing NeuPerformanceConfig cannot reuse a bitmap that
     * was generated with a different quality setting.
     */
    fun keyFor(
        pass: String,
        widthPx: Int,
        heightPx: Int,
        elevationPx: Float,
        strokeWidthPx: Float,
        lightColor: Color,
        darkColor: Color,
        cornerDescriptor: String,
        lightSource: String
    ): String {
        val elevationBucket = (elevationPx * 2).roundToInt()
        val strokeBucket = (strokeWidthPx * 2).roundToInt()
        val blurDownsampling = NeuPerformanceConfig.blurDownsampling
        return buildString {
            append(pass).append('|')
            append(widthPx).append('x').append(heightPx).append('|')
            append("e").append(elevationBucket).append('|')
            append("s").append(strokeBucket).append('|')
            append("b").append(blurDownsampling).append('|')
            append("l").append(lightColor.toArgbHex())
            append("d").append(darkColor.toArgbHex())
            append("c").append(cornerDescriptor).append('|')
            append("ls").append(lightSource)
        }
    }

    private fun Color.toArgbHex(): String {
        val a = (alpha * 255).roundToInt()
        val r = (red * 255).roundToInt()
        val g = (green * 255).roundToInt()
        val b = (blue * 255).roundToInt()
        return String.format("%02x%02x%02x%02x", a, r, g, b)
    }
}
