package me.nikhilchaudhari.library.components

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Dp

/** Compatibility helpers for Compose releases without Size.minDimension. */
internal val Size.minDimension: Float
    get() = minOf(width, height)

/**
 * Compatibility for call sites where a component parameter named `size`
 * shadows DrawScope.size. Keeps the existing API source-compatible while
 * providing a finite dimension for defensive stroke clamping.
 */
internal val Dp.minDimension: Float
    get() = value
