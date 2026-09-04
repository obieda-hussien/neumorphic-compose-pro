package me.nikhilchaudhari.library.internal

import android.graphics.Bitmap
import android.util.LruCache
import androidx.compose.ui.graphics.Color
import kotlin.math.roundToInt

/**
 * Process-wide cache of generated (already blurred) shadow bitmaps, keyed by a
 * description of the shape/size/colors that produced them.
 *
 * A neumorphic shadow is a pure function of (size, elevation, stroke width,
 * colors, corner shape, light source). Two composables with identical params -
 * e.g. 40 repeated cards in a `LazyColumn`, or a button that returns to the
 * exact same rest elevation after being pressed - produce byte-identical
 * shadow bitmaps. Without this cache every one of them re-runs bitmap alloc +
 * blur independently, even though the output is the same image. This cache
 * lets equal configurations share one bitmap instead of generating N copies.
 *
 * Sized in KB rather than entry count so it adapts to bitmap size/density.
 */
internal object NeuShadowCache {

    // Mutable so NeuPerformanceConfig.shadowCacheBudgetKB can resize it at
    // runtime. LruCache.resize() (from android.util) already handles trimming
    // down to the new budget if it shrinks, so no manual eviction needed here.
    private val cache = object : LruCache<String, Bitmap>(6 * 1024) { // ~6MB default
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

    /** Drops all cached bitmaps, e.g. in response to a system low-memory callback. */
    fun clear() = cache.evictAll()

    /**
     * Resizes the cache budget (in KB). Called by [me.nikhilchaudhari.library.NeuPerformanceConfig]
     * when the app changes `shadowCacheBudgetKB`. Existing entries are kept as
     * long as they still fit; if the new budget is smaller, the least-recently-used
     * entries are evicted immediately to bring the cache under budget.
     */
    fun resizeBudget(newBudgetKB: Int) {
        cache.resize(newBudgetKB.coerceAtLeast(1))
    }

    /**
     * Builds a stable cache key for a shadow bitmap.
     *
     * Elevation/stroke are quantized to half-dp steps: during a spring-driven
     * press animation, elevation changes on every single frame, which would
     * otherwise mean every frame is a cache miss. Rounding to 0.5dp buckets is
     * visually indistinguishable but collapses a ~200-frame animation down to a
     * handful of distinct shadow bitmaps that get reused as the animation
     * settles and repeats.
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
        return buildString {
            append(pass).append('|')
            append(widthPx).append('x').append(heightPx).append('|')
            append("e").append(elevationBucket).append('|')
            append("s").append(strokeBucket).append('|')
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
        // Zero-padded hex, not decimal concatenation: without padding,
        // "$a$r$g$b" can collide between different colors (e.g. a=1,r=23 and
        // a=12,r=3 both produce "123..."), which would return the wrong
        // cached shadow bitmap for a component with the colliding color.
        return String.format("%02x%02x%02x%02x", a, r, g, b)
    }
}
