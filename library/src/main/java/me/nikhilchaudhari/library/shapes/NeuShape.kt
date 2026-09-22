package me.nikhilchaudhari.library.shapes


import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import me.nikhilchaudhari.library.internal.BlurMaker

/**
 * Represents neumorphic shape
 */
interface NeuShape {
    /** Geometry used by both the worker and renderer. */
    val shadowCorners: CornerType get() = CornerType.Rounded()
    fun resolveCorners(size: androidx.compose.ui.geometry.Size, density: androidx.compose.ui.unit.Density,
        layoutDirection: androidx.compose.ui.unit.LayoutDirection): CornerType = shadowCorners


    fun drawShadows(drawScope: ContentDrawScope, blurMaker: BlurMaker, shapeConfig: ShapeConfig)
}