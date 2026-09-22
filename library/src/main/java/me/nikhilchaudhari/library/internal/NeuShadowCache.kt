package me.nikhilchaudhari.library.internal

import android.content.ComponentCallbacks2
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.roundToInt
import me.nikhilchaudhari.library.NeuPerformanceConfig

/** One byte budget covers probation and protected entries; bitmaps are never recycled on eviction. */
internal object NeuShadowCache {
    private data class Entry(val bitmap: Bitmap, var hits: Int = 0)
    private val entries = LinkedHashMap<String, Entry>(32, 0.75f, true)
    private var budgetBytes = 6L * 1024 * 1024
    private var usedBytes = 0L
    private var hits = 0
    private var misses = 0
    private val registered = AtomicBoolean()
    private fun cost(bitmap: Bitmap) = bitmap.byteCount.toLong()

    @Synchronized fun peek(key: String): Bitmap? = entries[key]?.bitmap?.takeUnless { it.isRecycled }
    @Synchronized fun get(key: String): Bitmap? {
        val entry = entries[key]
        if (entry == null || entry.bitmap.isRecycled) { misses++; return null }
        hits++
        entry.hits = (entry.hits + 1).coerceAtMost(2)
        return entry.bitmap
    }
    @Synchronized fun put(key: String, bitmap: Bitmap) {
        if (bitmap.isRecycled || cost(bitmap) > budgetBytes) return
        entries.remove(key)?.let { usedBytes -= cost(it.bitmap) }
        entries[key] = Entry(bitmap)
        usedBytes += cost(bitmap)
        trim()
    }
    private fun trim() {
        while (usedBytes > budgetBytes && entries.isNotEmpty()) {
            val victim = entries.entries.firstOrNull { it.value.hits < 2 } ?: entries.entries.first()
            usedBytes -= cost(victim.value.bitmap)
            entries.remove(victim.key)
        }
    }
    @Synchronized fun clear() { entries.clear(); usedBytes = 0 }
    @Synchronized fun resizeBudget(newBudgetKB: Int) {
        budgetBytes = newBudgetKB.coerceAtLeast(1).toLong() * 1024
        trim()
    }
    fun restoreConfiguredBudget() = resizeBudget(NeuPerformanceConfig.shadowCacheBudgetKB)
    @Synchronized fun snapshotStats(): Pair<Int, Int> = hits to misses
    @Synchronized fun memoryStats(): Pair<Long, Long> = usedBytes to budgetBytes

    fun registerMemoryPressureListener(context: Context) {
        if (!registered.compareAndSet(false, true)) return
        (context.applicationContext ?: context).registerComponentCallbacks(object : ComponentCallbacks2 {
            override fun onConfigurationChanged(newConfig: Configuration) = Unit
            override fun onLowMemory() = clear()
            override fun onTrimMemory(level: Int) {
                // UI_HIDDEN (20) is not RUNNING_CRITICAL (15); handle independently.
                if (level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
                    NeuBlurMakerHolder.onAppBackgrounded()
                    return
                }
                when {
                    level >= ComponentCallbacks2.TRIM_MEMORY_BACKGROUND -> clear()
                    level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> resizeBudget(1)
                    level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW -> resizeBudget(1024)
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

    private fun Color.toArgbHex(): String {
        val a = (alpha * 255).roundToInt()
        val r = (red * 255).roundToInt()
        val g = (green * 255).roundToInt()
        val b = (blue * 255).roundToInt()
        return String.format("%02x%02x%02x%02x", a, r, g, b)
    }
}
