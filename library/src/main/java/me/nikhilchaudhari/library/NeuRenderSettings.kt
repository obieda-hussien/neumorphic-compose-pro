package me.nikhilchaudhari.library

import me.nikhilchaudhari.library.internal.NeuRenderPolicy

/** Immutable work settings shared by the cache key and actual blur operation. */
data class NeuRenderSettings internal constructor(
    val sampling: Int,
    val adaptive: Boolean,
    val workBudget: Long,
    val policyIdentity: String
) {
    companion object {
        internal fun capture() = NeuRenderSettings(
            NeuRenderPolicy.effectiveMinimumSampling(NeuPerformanceConfig.blurDownsampling),
            NeuPerformanceConfig.adaptiveBlurEnabled,
            NeuRenderPolicy.effectiveWorkBudget(),
            "${NeuPerformanceConfig.thermalAwareRendering}:${NeuPerformanceConfig.batteryAwareRendering}:${NeuRenderPolicy.resolvedPerformanceClass()}"
        )
    }
}
