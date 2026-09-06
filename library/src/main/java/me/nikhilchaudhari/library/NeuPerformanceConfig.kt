package me.nikhilchaudhari.library

import me.nikhilchaudhari.library.internal.NeuShadowCache

/** App-tunable performance knobs for neumorphic shadow rendering. */
object NeuPerformanceConfig {

    /** Minimum sampling factor requested by the application. `1` is highest quality. */
    @Volatile
    var blurDownsampling: Int = 2
        set(value) {
            require(value >= 1) { "blurDownsampling must be >= 1, was $value" }
            field = value
            NeuShadowCache.clear()
        }

    /** Enables deterministic adaptive blur quality selection. */
    @Volatile
    var adaptiveBlurEnabled: Boolean = true
        set(value) {
            field = value
            NeuShadowCache.clear()
        }

    /** Approximate blur work budget in pixels*radius before adaptive downsampling. */
    @Volatile
    var blurWorkBudget: Long = 180_000L
        set(value) {
            require(value >= 1L) { "blurWorkBudget must be >= 1, was $value" }
            field = value
            NeuShadowCache.clear()
        }

    /**
     * When enabled, the renderer reduces blur work while Android reports thermal
     * pressure, then naturally returns to the normal budget as the device cools.
     */
    @Volatile
    var thermalAwareRendering: Boolean = true
        set(value) {
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
