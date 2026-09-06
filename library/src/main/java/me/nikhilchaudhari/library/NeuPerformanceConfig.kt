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
 */
object NeuPerformanceConfig {

    /**
     * How much shadow bitmaps are downsampled before blurring, then scaled
     * back up. `1` = full resolution, `2` is the default.
     */
    @Volatile
    var blurDownsampling: Int = 2
        set(value) {
            require(value >= 1) { "blurDownsampling must be >= 1, was $value" }
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
