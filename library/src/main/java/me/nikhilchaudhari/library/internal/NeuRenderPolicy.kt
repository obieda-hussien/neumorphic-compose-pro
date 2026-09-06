package me.nikhilchaudhari.library.internal

import androidx.compose.ui.unit.Dp
import kotlin.math.ceil
import kotlin.math.cbrt
import kotlin.math.round

/**
 * Deterministic policies that keep expensive shadow rendering bounded.
 *
 * The adaptive blur policy uses the practical cost model
 * `work ~= width * height * radius / sampling^3` because downsampling reduces
 * both the pixel count and the effective blur radius. The animation policy
 * separately quantizes renderer inputs so spring animations do not create a
 * unique cache entry for every floating-point frame value.
 */
internal object NeuRenderPolicy {

    const val MIN_SAMPLING = 1
    const val MAX_SAMPLING = 4

    private const val DEFAULT_BLUR_WORK_BUDGET = 180_000L
    private const val ELEVATION_QUANTUM_DP = 0.5f

    fun effectiveBlurSampling(
        width: Int,
        height: Int,
        radius: Int,
        configuredSampling: Int,
        workBudget: Long = DEFAULT_BLUR_WORK_BUDGET
    ): Int {
        val base = configuredSampling.coerceIn(MIN_SAMPLING, MAX_SAMPLING)
        val safeWidth = width.coerceAtLeast(1)
        val safeHeight = height.coerceAtLeast(1)
        val safeRadius = radius.coerceIn(1, BlurConfig.MAX_RADIUS)
        val safeBudget = workBudget.coerceAtLeast(1L)

        val estimatedWork = safeWidth.toDouble() * safeHeight.toDouble() * safeRadius
        val required = cbrt(estimatedWork / safeBudget.toDouble()).let(::ceil).toInt()

        return required.coerceIn(base, MAX_SAMPLING)
    }

    /**
     * Snap renderer elevation to a small perceptual step.
     *
     * Compose animation remains continuous; only the expensive bitmap
     * generation sees the quantized value. This makes nearby animation frames
     * converge on the same cache entries instead of repeatedly blurring.
     */
    fun quantizeElevation(elevation: Dp): Dp {
        if (!elevation.value.isFinite() || elevation.value <= 0f) return elevation
        return Dp(round(elevation.value / ELEVATION_QUANTUM_DP) * ELEVATION_QUANTUM_DP)
    }

    fun quantizeDp(value: Dp): Dp {
        if (!value.value.isFinite() || value.value <= 0f) return value
        return Dp(round(value.value / ELEVATION_QUANTUM_DP) * ELEVATION_QUANTUM_DP)
    }
}
