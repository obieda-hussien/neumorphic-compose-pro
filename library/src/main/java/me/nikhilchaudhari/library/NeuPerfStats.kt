package me.nikhilchaudhari.library

import android.os.Build
import me.nikhilchaudhari.library.internal.*

/** Counters are process-wide and cumulative. Cache bytes exclude backend and caller-owned bitmaps. */
data class NeuPerfSnapshot(
    val cacheHits: Int, val cacheMisses: Int, val cacheBytes: Long, val cacheBudgetBytes: Long,
    val pendingRequests: Int, val averageGenerationMs: Double, val failedRequests: Long,
    val preferredBackend: String, val thermalTier: Int, val powerSave: Boolean,
    val lastBackend: String, val gpuFallbacks: Long, val estimatedGpuBytes: Long
)
object NeuPerfStats {
    fun snapshot(): NeuPerfSnapshot {
        val (hits, misses) = NeuShadowCache.snapshotStats()
        val (bytes, budget) = NeuShadowCache.memoryStats()
        val (pending, average, failures) = ShadowWorkQueue.stats()
        val (backend, fallbacks, gpuBytes) = NeuBlurMakerHolder.diagnostics()
        return NeuPerfSnapshot(hits, misses, bytes, budget, pending, average, failures,
            if (Build.VERSION.SDK_INT >= 31) "RenderEffect (CPU fallback available)" else "RenderScript (CPU fallback available)",
            NeuThermalPolicy.cacheTier(), NeuPowerPolicy.isPowerSave(), backend, fallbacks, gpuBytes)
    }
}
