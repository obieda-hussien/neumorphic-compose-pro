package me.nikhilchaudhari.library

import me.nikhilchaudhari.library.internal.NeuShadowCache

/** App-tunable performance knobs for neumorphic shadow rendering. */
object NeuPerformanceConfig {

    /**
     * Coarse profile that scales work budgets across phone classes.
     * Defaults to [NeuPerformanceClass.AUTO].
     */
    @Volatile
    var performanceClass: NeuPerformanceClass = NeuPerformanceClass.AUTO
        set(value) {
            field = value
            NeuShadowCache.clear()
        }

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

    /**
     * When enabled, battery saver / power-save mode reduces the blur work budget
     * and raises the minimum sampling floor.
     */
    @Volatile
    var batteryAwareRendering: Boolean = true
        set(value) {
            field = value
            NeuShadowCache.clear()
        }

    /**
     * Minimum time between quality *improvements* after a thermal/power demotion.
     * Escalations under pressure still apply immediately.
     */
    @Volatile
    var qualityHysteresisMs: Long = 400L
        set(value) {
            require(value >= 0L) { "qualityHysteresisMs must be >= 0, was $value" }
            field = value
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
