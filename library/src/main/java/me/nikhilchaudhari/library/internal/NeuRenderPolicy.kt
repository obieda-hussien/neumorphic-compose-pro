package me.nikhilchaudhari.library.internal

import androidx.compose.ui.unit.Dp
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.round

/**
 * Deterministic policies that keep expensive shadow rendering bounded.
 *
 * Sampling is selected by a small discrete cost model instead of a continuous
 * approximation. Each candidate accounts for the downsampled blur work and a
 * small allowance for scaling the result back to output size. The controller
 * always chooses the highest visual quality that fits the requested work budget.
 */
internal object NeuRenderPolicy {
    const val MIN_SAMPLING = 1
    const val MAX_AUTO_SAMPLING = 4
    private const val DEFAULT_BLUR_WORK_BUDGET = 180_000L
    private const val ELEVATION_QUANTUM_DP = 0.5f
    private const val UPSCALE_WORK_FACTOR = 0.125

    fun effectiveBlurSampling(
        width: Int,
        height: Int,
        radius: Int,
        configuredSampling: Int,
        workBudget: Long = DEFAULT_BLUR_WORK_BUDGET
    ): Int {
        val base = configuredSampling.coerceAtLeast(MIN_SAMPLING)
        val safeWidth = width.coerceAtLeast(1)
        val safeHeight = height.coerceAtLeast(1)
        val safeRadius = radius.coerceIn(1, BlurConfig.MAX_RADIUS)
        // A non-positive budget is an invalid/unset value. Treat it as the
        // policy default rather than as a one-operation budget that forces the
        // renderer to its lowest quality tier.
        val safeBudget = if (workBudget > 0L) workBudget else DEFAULT_BLUR_WORK_BUDGET

        if (base > MAX_AUTO_SAMPLING) return base

        for (sampling in base..MAX_AUTO_SAMPLING) {
            if (estimatedWork(safeWidth, safeHeight, safeRadius, sampling) <= safeBudget) {
                return sampling
            }
        }

        return max(base, MAX_AUTO_SAMPLING)
    }

    private fun estimatedWork(width: Int, height: Int, radius: Int, sampling: Int): Double {
        val sampledWidth = ceil(width / sampling.toDouble()).coerceAtLeast(1.0)
        val sampledHeight = ceil(height / sampling.toDouble()).coerceAtLeast(1.0)
        val sampledRadius = ceil(radius / sampling.toDouble()).coerceAtLeast(1.0)
        val blurWork = sampledWidth * sampledHeight * sampledRadius
        val upscaleWork = width.toDouble() * height.toDouble() * UPSCALE_WORK_FACTOR
        return blurWork + upscaleWork
    }

    fun quantizeElevation(elevation: Dp): Dp {
        if (!elevation.value.isFinite() || elevation.value <= 0f) return elevation
        return Dp(round(elevation.value / ELEVATION_QUANTUM_DP) * ELEVATION_QUANTUM_DP)
    }

    fun quantizeDp(value: Dp): Dp {
        if (!value.value.isFinite() || value.value <= 0f) return value
        return Dp(round(value.value / ELEVATION_QUANTUM_DP) * ELEVATION_QUANTUM_DP)
    }
}
