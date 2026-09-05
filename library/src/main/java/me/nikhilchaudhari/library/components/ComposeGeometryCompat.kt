package me.nikhilchaudhari.library.components

import androidx.compose.ui.geometry.Size

/** Compatibility helper for Compose releases without Size.minDimension. */
internal val Size.minDimension: Float
    get() = minOf(width, height)
