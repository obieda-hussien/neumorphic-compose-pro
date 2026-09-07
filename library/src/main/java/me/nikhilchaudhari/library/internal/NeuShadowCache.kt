@file:Suppress("DEPRECATION")

package me.nikhilchaudhari.library.internal

import android.content.ComponentCallbacks2
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.util.LruCache
import androidx.compose.ui.graphics.Color
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.roundToInt
import me.nikhilchaudhari.library.NeuPerformanceConfig

/**
 * Two-level shadow cache:
 * - hot: small access-ordered map that resists normal LRU eviction for repeated list items
 * - main: size-budgeted LRU measured in KB
 */
internal object NeuShadowCache {
    private const val MAX_HOT_ENTRIES = 32
    private const val PROMOTE_AFTER_HITS = 2

    private val cache = object : LruCache<String, Bitmap>(6 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int = (value.byteCount / 1024).coerceAtLeast(1)
    }

    private val hotLock = Any()
    private val hot = object : LinkedHashMap<String, Bitmap>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Bitmap>?): Boolean {
            return size > MAX_HOT_ENTRIES
        }
    }
    private val hitCounts = HashMap<String, Int>(64)

    private val memoryCallbackRegistered = AtomicBoolean(false)
    private val hits = AtomicInteger(0)
    private val misses = AtomicInteger(0)

    fun get(key: String): Bitmap? {
        synchronized(hotLock) {
            hot[key]?.let { bitmap ->
                if (bitmap.isRecycled) {
                    hot.remove(key)
                } else {
                    hits.incrementAndGet()
                    return bitmap
                }
            }
        }
        val bitmap = cache.get(key)
        if (bitmap == null) {
            misses.incrementAndGet()
            return null
        }
        if (bitmap.isRecycled) {
            cache.remove(key)
            misses.incrementAndGet()
            return null
        }
        hits.incrementAndGet()
        maybePromote(key, bitmap)
        return bitmap
    }

    fun put(key: String, bitmap: Bitmap) {
        if (bitmap.isRecycled || bitmap.width <= 0 || bitmap.height <= 0) return
        cache.put(key, bitmap)
    }

    fun clear() {
        cache.evictAll()
        synchronized(hotLock) {
            hot.clear()
            hitCounts.clear()
        }
    }

    fun resizeBudget(newBudgetKB: Int) {
        cache.resize(newBudgetKB.coerceAtLeast(1))
    }

    /** Restore the application's configured cache budget after temporary memory pressure. */
    fun restoreConfiguredBudget() {
        cache.resize(NeuPerformanceConfig.shadowCacheBudgetKB.coerceAtLeast(1))
    }

    fun snapshotStats(): Pair<Int, Int> = hits.get() to misses.get()

    fun registerMemoryPressureListener(context: Context) {
        if (!memoryCallbackRegistered.compareAndSet(false, true)) return
        val applicationContext = context.applicationContext ?: context
        applicationContext.registerComponentCallbacks(object : ComponentCallbacks2 {
            override fun onConfigurationChanged(newConfig: Configuration) = Unit
            override fun onLowMemory() = clear()
            override fun onTrimMemory(level: Int) {
                when {
                    level >= ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> clear()
                    level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> {
                        synchronized(hotLock) {
                            hot.clear()
                            hitCounts.clear()
                        }
                        resizeBudget(1)
                    }
                    level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW -> resizeBudget(1024)
                    level >= ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN ->
                        NeuBlurMakerHolder.onAppBackgrounded()
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
        val batteryAware = NeuPerformanceConfig.batteryAwareRendering
        val thermalTier = if (thermalAware) NeuThermalPolicy.cacheTier() else 0
        val powerTier = if (batteryAware) NeuPowerPolicy.cacheTier() else 0
        val perfClass = NeuRenderPolicy.resolvedPerformanceClass().name
        return buildString {
            append(pass).append('|')
            append(widthPx).append('x').append(heightPx).append('|')
            append("e").append(elevationBits).append('|')
            append("s").append(strokeBits).append('|')
            append("b").append(blurDownsampling).append('|')
            append("a").append(if (adaptive) 1 else 0).append('|')
            append("w").append(workBudget).append('|')
            append("t").append(if (thermalAware) 1 else 0).append(thermalTier).append('|')
            append("p").append(if (batteryAware) 1 else 0).append(powerTier).append('|')
            append("pc").append(perfClass).append('|')
            append("l").append(lightColor.toArgbHex())
            append("d").append(darkColor.toArgbHex())
            append("c").append(cornerDescriptor).append('|')
            append("ls").append(lightSource)
        }
    }

    private fun maybePromote(key: String, bitmap: Bitmap) {
        synchronized(hotLock) {
            val count = (hitCounts[key] ?: 0) + 1
            hitCounts[key] = count
            if (count >= PROMOTE_AFTER_HITS) {
                hot[key] = bitmap
            }
            if (hitCounts.size > MAX_HOT_ENTRIES * 4) {
                hitCounts.clear()
            }
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
