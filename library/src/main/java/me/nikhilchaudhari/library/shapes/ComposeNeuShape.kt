package me.nikhilchaudhari.library.shapes

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.unit.*
import me.nikhilchaudhari.library.LightSource
import me.nikhilchaudhari.library.internal.BlurMaker
import java.util.WeakHashMap

/** Adapts any immutable Compose Shape, including asymmetric corners and generic paths. */
class ComposeNeuShape(val shape: Shape, val recessed: Boolean = false) : NeuShape {
    override fun resolveCorners(size: Size, density: Density, layoutDirection: LayoutDirection): CornerType {
        val outline = shape.createOutline(size, layoutDirection, density)
        val path = Path().apply {
            when (outline) {
                is Outline.Rectangle -> addRect(outline.rect)
                is Outline.Rounded -> addRoundRect(outline.roundRect)
                is Outline.Generic -> addPath(outline.path)
            }
        }
        val id = synchronized(ids) { ids.getOrPut(shape) { ++nextId } }
        return CornerType.Custom(path, "compose:$id:$size:${density.density}:$layoutDirection")
    }
    override fun drawShadows(drawScope: ContentDrawScope, blurMaker: BlurMaker, shapeConfig: ShapeConfig) {
        with(drawScope) {
            if (!recessed) drawOnBackground(shapeConfig, blurMaker)
            drawContent()
            if (recessed) clippedToCornerType(shapeConfig.cornerType) { drawOnForeground(shapeConfig, blurMaker) }
        }
    }
    private companion object {
        val ids = WeakHashMap<Shape, Long>()
        var nextId = 0L
    }
}

internal fun maskDrawable(corners: CornerType, width: Int, height: Int, stroke: Int,
    form: ShadowForm, radius: Float, light: LightSource): Drawable {
    if (corners !is CornerType.Custom) return GradientDrawable().apply {
        setSize(width, height)
        setBounds(0, 0, width, height)
        setColor(if (stroke == 0) android.graphics.Color.WHITE else android.graphics.Color.TRANSPARENT)
        if (stroke > 0) setStroke(stroke, android.graphics.Color.WHITE)
        setNeuShapeForGeneration(corners, form, radius, light)
    }
    val path = android.graphics.Path(corners.path.asAndroidPath())
    return object : Drawable() {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            style = if (stroke == 0) Paint.Style.FILL else Paint.Style.STROKE
            strokeWidth = stroke.toFloat()
        }
        override fun draw(canvas: Canvas) { canvas.drawPath(path, paint) }
        override fun setAlpha(alpha: Int) { paint.alpha = alpha }
        override fun setColorFilter(colorFilter: ColorFilter?) { paint.colorFilter = colorFilter }
        @Deprecated("Deprecated in Android")
        override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
    }
}
