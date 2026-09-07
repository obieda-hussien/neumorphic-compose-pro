package me.nikhilchaudhari.library.internal

import androidx.compose.ui.unit.Dp
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.round
import me.nikhilchaudhari.library.NeuPerformanceClass
import me.nikhilchaudhari.library.NeuPerformanceConfig

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

    fun resolvedPerformanceClass(): NeuPerformanceClass {
        return when (val configured = NeuPerformanceConfig.performanceClass) {
            NeuPerformanceClass.AUTO -> {
                val thermal = if (NeuPerformanceConfig.thermalAwareRendering) {
                    NeuThermalPolicy.cacheTier()
                } else {
                    0
                }
                val powerSave = NeuPerformanceConfig.batteryAwareRendering && NeuPowerPolicy.isPowerSave()
                if (powerSave || thermal >= 3) NeuPerformanceClass.BATTERY else NeuPerformanceClass.BALANCED
            }
            else -> configured
        }
    }

    /**
     * Combine app budget with thermal, power-save, and performance-class multipliers.
     */
    fun effectiveWorkBudget(): Long {
        var budget = NeuPerformanceConfig.blurWorkBudget.coerceAtLeast(1L)
        when (resolvedPerformanceClass()) {
            NeuPerformanceClass.QUALITY -> budget = (budget * 140L) / 100L
            NeuPerformanceClass.BALANCED, NeuPerformanceClass.AUTO -> Unit
            NeuPerformanceClass.BATTERY -> budget = (budget * 55L) / 100L
        }
        if (NeuPerformanceConfig.thermalAwareRendering) {
            budget = NeuThermalPolicy.effectiveWorkBudget(budget)
        }
        if (NeuPerformanceConfig.batteryAwareRendering) {
            budget = NeuPowerPolicy.effectiveWorkBudget(budget)
        }
        return budget.coerceAtLeast(1L)
    }

    fun effectiveMinimumSampling(configuredSampling: Int): Int {
        val base = configuredSampling.coerceAtLeast(MIN_SAMPLING)
        val powerFloor = if (NeuPerformanceConfig.batteryAwareRendering) {
            NeuPowerPolicy.minimumSamplingFloor()
        } else {
            MIN_SAMPLING
        }
        val classFloor = when (resolvedPerformanceClass()) {
            NeuPerformanceClass.BATTERY -> 2
            else -> MIN_SAMPLING
        }
        return maxOf(base, powerFloor, classFloor)
    }

    fun effectiveBlurSampling(
        width: Int,
        height: Int,
        radius: Int,
        configuredSampling: Int,
        workBudget: Long = DEFAULT_BLUR_WORK_BUDGET
    ): Int {
        val base = effectiveMinimumSampling(configuredSampling)
        val safeWidth = width.coerceAtLeast(1)
        val safeHeight = height.coerceAtLeast(1)
        val safeRadius = radius.coerceIn(1, BlurConfig.MAX_RADIUS)
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
