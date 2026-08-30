package me.nikhilchaudhari.library

import me.nikhilchaudhari.library.internal.NeuShadowCache

/**
 * App-tunable performance knobs for neumorphic shadow rendering.
 *
 * The defaults were chosen to be safe and visually lossless for typical UI
 * (cards, buttons, switches at normal screen density). They're exposed here
 * because "typical" doesn't cover every app:
 *
 * - An app targeting low-end/entry-level devices might want more aggressive
 *   downsampling to save CPU, even at a small quality cost.
 * - An app with dozens of distinct neumorphic shapes on screen at once (many
 *   different sizes/colors, so the shared cache gets little reuse) might want
 *   a bigger cache budget to avoid thrashing; a very simple app might prefer
 *   a smaller budget to save memory.
 *
 * Set these once, early (e.g. in `Application.onCreate`), before any
 * neumorphic composable is drawn. They are safe to change later too, but a
 * change only affects shadows generated *after* the change - it does not
 * retroactively re-render what's already cached or on screen.
 *
 * ```kotlin
 * class MyApplication : Application() {
 *     override fun onCreate() {
 *         super.onCreate()
 *         NeuPerformanceConfig.blurDownsampling = 3       // lower-end devices
 *         NeuPerformanceConfig.shadowCacheBudgetKB = 12 * 1024  // bigger, varied UI
 *     }
 * }
 * ```
 */
object NeuPerformanceConfig {

    /**
     * How much shadow bitmaps are downsampled before blurring, then scaled
     * back up. `1` = full resolution (no downsampling - most expensive,
     * highest theoretical fidelity, though the difference from `2` is not
     * visible in practice since blur discards that detail anyway). `2` is the
     * library default. Higher values (3-4) trade a small, usually
     * imperceptible softness increase for a quadratic drop in CPU work
     * (`3` is ~2.25x less CPU than `2`; `4` is 4x less).
     *
     * Recommended range: 1-4. Values above 4 start to visibly blur shadow
     * edges into soft blobs, especially on small/low-elevation components.
     */
    var blurDownsampling: Int = 2
        set(value) {
            require(value >= 1) { "blurDownsampling must be >= 1, was $value" }
            field = value
        }

    /**
     * Maximum size, in KB, of the process-wide cache of generated shadow
     * bitmaps (see `NeuShadowCache`). Bigger budgets mean more distinct
     * shadow configurations (different sizes/elevations/colors) can stay
     * cached simultaneously before older ones are evicted to make room -
     * useful for screens with many *different* neumorphic components. Smaller
     * budgets trade some cache-hit rate for a lower memory ceiling.
     *
     * Library default: 6144 (6MB), which comfortably fits several dozen
     * typical card/button-sized shadow bitmaps at once.
     */
    var shadowCacheBudgetKB: Int = 6 * 1024
        set(value) {
            require(value >= 0) { "shadowCacheBudgetKB must be >= 0, was $value" }
            field = value
            NeuShadowCache.resizeBudget(value)
        }
}
