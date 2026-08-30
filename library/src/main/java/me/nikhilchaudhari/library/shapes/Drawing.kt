package me.nikhilchaudhari.library.shapes

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.withTranslation
import me.nikhilchaudhari.library.LightSource
import me.nikhilchaudhari.library.internal.BlurMaker
import me.nikhilchaudhari.library.internal.NeuCache
import kotlin.math.roundToInt


/**
 * Calculate shadow offsets based on light source direction
 */
internal fun getLightShadowOffset(lightSource: LightSource, elevation: Float): Pair<Float, Float> {
    return when (lightSource) {
        LightSource.TOP_LEFT -> Pair(-elevation, -elevation)
        LightSource.TOP_RIGHT -> Pair(elevation, -elevation)
        LightSource.BOTTOM_LEFT -> Pair(-elevation, elevation)
        LightSource.BOTTOM_RIGHT -> Pair(elevation, elevation)
    }
}

internal fun getDarkShadowOffset(lightSource: LightSource, elevation: Float): Pair<Float, Float> {
    return when (lightSource) {
        LightSource.TOP_LEFT -> Pair(elevation, elevation)
        LightSource.TOP_RIGHT -> Pair(-elevation, elevation)
        LightSource.BOTTOM_LEFT -> Pair(elevation, -elevation)
        LightSource.BOTTOM_RIGHT -> Pair(-elevation, -elevation)
    }
}


/* pressed shape - draw after the content */
internal fun ContentDrawScope.drawOnForeground(
    shapeConfig: ShapeConfig,
    blurMaker: BlurMaker
) {
    val drawScope = this
    val elevation = this.density.run { shapeConfig.elevation.toPx() }
    val cornerType = shapeConfig.cornerType
    val radius =
        if (cornerType is CornerType.Rounded) this.density.run { cornerType.radius.toPx() } else 0f

    val size = drawScope.size
    val width = size.width.toInt()
    val height = size.height.toInt()

    if (width <= 0 || height <= 0) return

    val cacheKey = "fg_${width}_${height}_${shapeConfig.hashCode()}"
    val cachedBitmap = NeuCache.get(cacheKey)

    val shadowBitmap = cachedBitmap ?: run {
        val lightShadowDrawable = GradientDrawable()
        val darkShadowDrawable = GradientDrawable()
        val strokeWidth = this.density.run { shapeConfig.strokeWidth.toPx() }.toInt()
        val drawableWidth = width + elevation.toInt()
        val drawableHeight = height + elevation.toInt()

        lightShadowDrawable.apply {
            setSize(drawableWidth, drawableHeight)
            setStroke(strokeWidth, shapeConfig.lightShadowColor.toArgb())
            setBounds(0, 0, drawableWidth, drawableHeight)
            setColor(Color.Transparent.toArgb())
            setNeuShape(cornerType, ShadowForm.LightShadow, radius, shapeConfig.lightSource)
        }

        darkShadowDrawable.apply {
            setSize(drawableWidth, drawableHeight)
            setStroke(strokeWidth, shapeConfig.darkShadowColor.toArgb())
            setColor(Color.Transparent.toArgb())
            setBounds(0, 0, drawableWidth, drawableHeight)
            setNeuShape(cornerType, ShadowForm.DarkShadow, radius, shapeConfig.lightSource)
        }

        val lightOffset = getLightShadowOffset(shapeConfig.lightSource, elevation)
        val darkOffset = getDarkShadowOffset(shapeConfig.lightSource, elevation)

        generateShadowBitmap(
            width,
            height,
            lightShadowDrawable,
            darkShadowDrawable,
            elevation,
            blurMaker,
            lightOffset,
            darkOffset
        )?.also {
            NeuCache.put(cacheKey, it)
        }
    }

    shadowBitmap?.asImageBitmap()?.let {
        drawScope.drawImage(it)
    }
}


private fun generateShadowBitmap(
    w: Int,
    h: Int,
    lightShadowDrawable: GradientDrawable,
    darkShadowDrawable: GradientDrawable,
    elevation: Float,
    blurMaker: BlurMaker,
    lightOffset: Pair<Float, Float>,
    @Suppress("UNUSED_PARAMETER") darkOffset: Pair<Float, Float>
): Bitmap? {
    if (w <= 0 || h <= 0) return null
    val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    return bitmap.blurred(blurMaker) {
        withTranslation(
            x = lightOffset.first,
            y = lightOffset.second
        ) {
            lightShadowDrawable.draw(this)
        }
        // Dark shadow is drawn at origin for inner shadow effect
        darkShadowDrawable.draw(this)
    }
}


/* Flat shape - before the content draw scope */
internal fun ContentDrawScope.drawOnBackground(
    shapeConfig: ShapeConfig,
    blurMaker: BlurMaker
) {
    val drawScope = this
    val elevation = this.density.run { shapeConfig.elevation.toPx() }
    val horizontalInset = this.density.run { shapeConfig.neuInsets.horizontal.toPx() }
    val verticalInset = this.density.run { shapeConfig.neuInsets.vertical.toPx() }
    val cornerType = shapeConfig.cornerType
    val radius =
        if (cornerType is CornerType.Rounded) this.density.run { cornerType.radius.toPx() } else 0f

    val size = drawScope.size
    val width = size.width.toInt()
    val height = size.height.toInt()

    if (width <= 0 || height <= 0) return

    val lightCacheKey = "bg_light_${width}_${height}_${shapeConfig.hashCode()}"
    val darkCacheKey = "bg_dark_${width}_${height}_${shapeConfig.hashCode()}"

    var lightShadowBitmap = NeuCache.get(lightCacheKey)
    if (lightShadowBitmap == null) {
        val lightShadowDrawable = GradientDrawable().apply {
            setColor(shapeConfig.lightShadowColor.toArgb())
            setSize(width, height)
            setBounds(0, 0, width, height)
            setNeuShape(cornerType, ShadowForm.Default, radius, shapeConfig.lightSource)
        }
        lightShadowBitmap = lightShadowDrawable.toBlurredBitmap(width, height, elevation, blurMaker)
        lightShadowBitmap?.let { NeuCache.put(lightCacheKey, it) }
    }

    var darkShadowBitmap = NeuCache.get(darkCacheKey)
    if (darkShadowBitmap == null) {
        val darkShadowDrawable = GradientDrawable().apply {
            setColor(shapeConfig.darkShadowColor.toArgb())
            setSize(width, height)
            setBounds(0, 0, width, height)
            setNeuShape(cornerType, ShadowForm.Default, radius, shapeConfig.lightSource)
        }
        darkShadowBitmap = darkShadowDrawable.toBlurredBitmap(width, height, elevation, blurMaker)
        darkShadowBitmap?.let { NeuCache.put(darkCacheKey, it) }
    }

    // Calculate insets based on light source
    val (lightHInset, lightVInset) = when (shapeConfig.lightSource) {
        LightSource.TOP_LEFT -> Pair(-(horizontalInset + elevation), -(verticalInset + elevation))
        LightSource.TOP_RIGHT -> Pair(horizontalInset - elevation, -(verticalInset + elevation))
        LightSource.BOTTOM_LEFT -> Pair(-(horizontalInset + elevation), verticalInset - elevation)
        LightSource.BOTTOM_RIGHT -> Pair(horizontalInset - elevation, verticalInset - elevation)
    }
    
    val (darkHInset, darkVInset) = when (shapeConfig.lightSource) {
        LightSource.TOP_LEFT -> Pair(horizontalInset - elevation, verticalInset - elevation)
        LightSource.TOP_RIGHT -> Pair(-(horizontalInset + elevation), verticalInset - elevation)
        LightSource.BOTTOM_LEFT -> Pair(horizontalInset - elevation, -(verticalInset + elevation))
        LightSource.BOTTOM_RIGHT -> Pair(-(horizontalInset + elevation), -(verticalInset + elevation))
    }

    lightShadowBitmap?.asImageBitmap()?.let { bitmap ->
        drawScope.inset(lightHInset, lightVInset) {
            drawImage(bitmap)
        }
    }

    darkShadowBitmap?.asImageBitmap()?.let { bitmap ->
        drawScope.inset(darkHInset, darkVInset) {
            drawImage(bitmap)
        }
    }
}


private fun Drawable.toBlurredBitmap(
    w: Int,
    h: Int,
    elevation: Float,
    blurMaker: BlurMaker
): Bitmap? {
    val width = (w + elevation * 2).roundToInt()
    val height = (h + elevation * 2).roundToInt()
    if (width <= 0 || height <= 0) return null

    return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        .blurred(blurMaker) {
            withTranslation(elevation, elevation) {
                draw(this)
            }
        }
}


internal fun Bitmap.blurred(
    blurMaker: BlurMaker,
    block: Canvas.() -> Unit
): Bitmap? {
    Canvas(this).run {
        block()
    }
    return blurMaker.blur(this)
}

internal sealed class ShadowForm {
    object Default : ShadowForm()
    object LightShadow : ShadowForm()
    object DarkShadow : ShadowForm()
}


private fun GradientDrawable.setNeuShape(
    cornerType: CornerType,
    shadowForm: ShadowForm,
    radius: Float,
    lightSource: LightSource = LightSource.TOP_LEFT
) {
    when (cornerType) {
        is CornerType.Oval -> {
            shape = GradientDrawable.OVAL
        }
        is CornerType.Rounded -> {
            shape = GradientDrawable.RECTANGLE
            when (shadowForm) {
                is ShadowForm.Default -> {
                    cornerRadius = radius
                }
                is ShadowForm.LightShadow -> {
                    // Adjust corner radii based on light source
                    cornerRadii = when (lightSource) {
                        LightSource.TOP_LEFT -> floatArrayOf(
                            0f, 0f, radius, radius, radius, radius, radius, radius
                        )
                        LightSource.TOP_RIGHT -> floatArrayOf(
                            radius, radius, 0f, 0f, radius, radius, radius, radius
                        )
                        LightSource.BOTTOM_LEFT -> floatArrayOf(
                            radius, radius, radius, radius, 0f, 0f, radius, radius
                        )
                        LightSource.BOTTOM_RIGHT -> floatArrayOf(
                            radius, radius, radius, radius, radius, radius, 0f, 0f
                        )
                    }
                }
                is ShadowForm.DarkShadow -> {
                    // Adjust corner radii based on light source (opposite of light)
                    cornerRadii = when (lightSource) {
                        LightSource.TOP_LEFT -> floatArrayOf(
                            radius, radius, radius, radius, 0f, 0f, radius, radius
                        )
                        LightSource.TOP_RIGHT -> floatArrayOf(
                            radius, radius, radius, radius, radius, radius, 0f, 0f
                        )
                        LightSource.BOTTOM_LEFT -> floatArrayOf(
                            0f, 0f, radius, radius, radius, radius, radius, radius
                        )
                        LightSource.BOTTOM_RIGHT -> floatArrayOf(
                            radius, radius, 0f, 0f, radius, radius, radius, radius
                        )
                    }
                }
            }
        }
    }
}
