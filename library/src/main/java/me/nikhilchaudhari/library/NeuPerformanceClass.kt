package me.nikhilchaudhari.library

/**
 * Coarse performance profile used to bound blur work across device classes.
 *
 * - [QUALITY]: prefer visual fidelity; only thermal/power policies may reduce work.
 * - [BALANCED]: default adaptive budget (good for mid-range phones).
 * - [BATTERY]: aggressive downsampling and lower work budget for low-end / saver modes.
 * - [AUTO]: pick [BALANCED] normally, [BATTERY] while power-save or strong thermal pressure.
 */
enum class NeuPerformanceClass {
    QUALITY,
    BALANCED,
    BATTERY,
    AUTO
}
