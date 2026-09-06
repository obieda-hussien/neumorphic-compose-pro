package me.nikhilchaudhari.library.internal

import kotlin.math.ceil
import kotlin.math.cbrt

/**
 * Adaptive renderer policy used to bound blur work while preserving useful
 * detail on small UI elements.
 *
 * A separable stack blur has a work profile that grows with image area and the
 * blur radius. Downsampling by `s` reduces the image area roughly by `1/s²`
 * and the effective blur radius by `1/s`, so a practical cost proxy is:
 *
 *     cost ~= width * height * radius / s³
 *
 * We choose the smallest sampling factor that keeps this proxy under a fixed
 * work budget. This is intentionally a deterministic, stateless policy: the
 * same geometry and radius always choose the same quality level, so it cannot
 * cause frame-to-frame quality oscillation.
 */
internal object NeuRenderPolicy {

    const val MIN_SAMPLING = 1
    const val MAX_SAMPLING = 4

    // Work units are intentionally conservative. A UI shadow should consume a
    // small fraction of a frame rather than behaving like an image processor.
    private const val DEFAULT_BLUR_WORK_BUDGET = 180_000L

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
}
