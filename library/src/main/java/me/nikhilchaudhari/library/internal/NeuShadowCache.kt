@file:Suppress("DEPRECATION")

package me.nikhilchaudhari.library.internal

import android.content.ComponentCallbacks2
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.util.LruCache
import androidx.compose.ui.graphics.Color
import java.util.concurrent.atomic.AtomicBoolean
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

    private val memoryCallbackRegistered = AtomicBoolean(false)

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
        if (bitmap.isRecycled || bitmap.width <= 0 || bitmap.height <= 0) return
        cache.put(key, bitmap)
    }

    fun clear() = cache.evictAll()

    fun resizeBudget(newBudgetKB: Int) {
        cache.resize(newBudgetKB.coerceAtLeast(1))
    }

    /**
     * Registers one process-wide memory-pressure callback. The callback uses
     * the application context so it cannot retain an Activity or other short-lived Context.
     */
    fun registerMemoryPressureListener(context: Context) {
        if (!memoryCallbackRegistered.compareAndSet(false, true)) return

        val applicationContext = context.applicationContext ?: context
        applicationContext.registerComponentCallbacks(object : ComponentCallbacks2 {
            override fun onConfigurationChanged(newConfig: Configuration) = Unit

            override fun onLowMemory() {
                clear()
            }

            override fun onTrimMemory(level: Int) {
                when {
                    level >= ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> clear()
                    level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> resizeBudget(1)
                    level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW -> resizeBudget(1024)
                    level >= ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> resizeBudget(2048)
                }
            }
        })
    }

    /**
     * Builds a stable cache key for a shadow bitmap.
     *
     * Float values are represented by their exact IEEE-754 bit pattern instead
     * of approximate buckets so a cache hit always corresponds to the exact
     * render parameters that generated the bitmap.
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
        val elevationBits = elevationPx.toRawBits()
        val strokeBits = strokeWidthPx.toRawBits()
        val blurDownsampling = NeuPerformanceConfig.blurDownsampling
        val adaptive = NeuPerformanceConfig.adaptiveBlurEnabled
        val workBudget = NeuPerformanceConfig.blurWorkBudget
        return buildString {
            append(pass).append('|')
            append(widthPx).append('x').append(heightPx).append('|')
            append("e").append(elevationBits).append('|')
            append("s").append(strokeBits).append('|')
            append("b").append(blurDownsampling).append('|')
            append("a").append(if (adaptive) 1 else 0).append('|')
            append("w").append(workBudget).append('|')
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
