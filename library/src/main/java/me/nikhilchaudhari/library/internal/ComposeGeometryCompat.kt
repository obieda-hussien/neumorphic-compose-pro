package me.nikhilchaudhari.library.internal

import androidx.compose.ui.geometry.Size

/**
 * Compatibility helper for Compose releases where [Size] does not expose a
 * `minDimension` property directly.
 */
internal val Size.minDimension: Float
    get() = minOf(width, height)
