package me.nikhilchaudhari.library

import me.nikhilchaudhari.library.internal.NeuShadowCache

/**
 * App-tunable performance knobs for neumorphic shadow rendering.
 *
 * The configuration separates visual quality from the amount of CPU work used
 * to produce each blur. Changes are visible across renderer threads and affect
 * newly generated shadows.
 */
object NeuPerformanceConfig {

    /** Minimum sampling factor requested by the application. `1` is highest quality. */
    @Volatile
    var blurDownsampling: Int = 2
        set(value) {
            require(value >= 1) { "blurDownsampling must be >= 1, was $value" }
            field = value
            NeuShadowCache.clear()
        }

    /**
     * Enables the deterministic adaptive blur policy. When enabled, large/high-radius
     * shadows can use stronger downsampling while small shadows keep the requested floor.
     */
    @Volatile
    var adaptiveBlurEnabled: Boolean = true
        set(value) {
            field = value
            NeuShadowCache.clear()
        }

    /**
     * Approximate blur work budget in pixels*radius before adaptive downsampling.
     * Larger values favor quality; smaller values favor CPU/battery usage.
     */
    @Volatile
    var blurWorkBudget: Long = 180_000L
        set(value) {
            require(value >= 1L) { "blurWorkBudget must be >= 1, was $value" }
            field = value
            NeuShadowCache.clear()
        }

    /** Maximum size, in KB, of the process-wide generated-shadow cache. */
    @Volatile
    var shadowCacheBudgetKB: Int = 6 * 1024
        set(value) {
            require(value >= 1) { "shadowCacheBudgetKB must be >= 1, was $value" }
            field = value
            NeuShadowCache.resizeBudget(value)
        }
}
