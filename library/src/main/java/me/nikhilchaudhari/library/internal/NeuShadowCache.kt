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

internal object NeuShadowCache {
    private val cache = object : LruCache<String, Bitmap>(6 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int = (value.byteCount / 1024).coerceAtLeast(1)
    }

    private val memoryCallbackRegistered = AtomicBoolean(false)

    fun get(key: String): Bitmap? {
        val bitmap = cache.get(key) ?: return null
        return if (bitmap.isRecycled) {
            cache.remove(key)
            null
        } else bitmap
    }

    fun put(key: String, bitmap: Bitmap) {
        if (bitmap.isRecycled || bitmap.width <= 0 || bitmap.height <= 0) return
        cache.put(key, bitmap)
    }

    fun clear() = cache.evictAll()
    fun resizeBudget(newBudgetKB: Int) { cache.resize(newBudgetKB.coerceAtLeast(1)) }

    fun registerMemoryPressureListener(context: Context) {
        if (!memoryCallbackRegistered.compareAndSet(false, true)) return
        val applicationContext = context.applicationContext ?: context
        applicationContext.registerComponentCallbacks(object : ComponentCallbacks2 {
            override fun onConfigurationChanged(newConfig: Configuration) = Unit
            override fun onLowMemory() = clear()
            override fun onTrimMemory(level: Int) {
                when {
                    level >= ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> clear()
                    level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> resizeBudget(1)
                    level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW -> resizeBudget(1024)
                    // TRIM_MEMORY_UI_HIDDEN means the app UI moved to the background.
                    // Do not evict the hot shadow cache here: doing so makes the next
                    // Activity resume pay the full CPU/bitmap generation cost again.
                    // Stronger memory-pressure levels above still trim aggressively.
                    level >= ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> Unit
                }
            }
        })
    }

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
        val thermalAware = NeuPerformanceConfig.thermalAwareRendering
        val thermalTier = if (thermalAware) NeuThermalPolicy.cacheTier() else 0
        return buildString {
            append(pass).append('|')
            append(widthPx).append('x').append(heightPx).append('|')
            append("e").append(elevationBits).append('|')
            append("s").append(strokeBits).append('|')
            append("b").append(blurDownsampling).append('|')
            append("a").append(if (adaptive) 1 else 0).append('|')
            append("w").append(workBudget).append('|')
            append("t").append(if (thermalAware) 1 else 0).append(thermalTier).append('|')
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
