package me.nikhilchaudhari.library.shapes

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.withTranslation
import me.nikhilchaudhari.library.LightSource
import me.nikhilchaudhari.library.NeuPerformanceConfig
import me.nikhilchaudhari.library.internal.BlurMaker
import me.nikhilchaudhari.library.internal.NeuShadowCache
import kotlin.math.roundToInt

/**
 * Human-readable, stable string for a [CornerType], for use in
 * [NeuShadowCache] cache keys.
 */
internal fun CornerType.cacheDescriptor(): String = when (this) {
    is CornerType.Oval -> "Oval"
    is CornerType.Rounded -> "Rounded(${radius.value})"
}

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

/* Pressed shape - draw after the content. */
internal fun DrawScope.drawOnForeground(
    shapeConfig: ShapeConfig,
    blurMaker: BlurMaker
) {
    val drawScope = this
    val elevation = density.run { shapeConfig.elevation.toPx() }
    val cornerType = shapeConfig.cornerType
    val radius = if (cornerType is CornerType.Rounded) density.run { cornerType.radius.toPx() } else 0f

    val size = drawScope.size
    val width = size.width.toInt() + elevation.toInt()
    val height = size.height.toInt() + elevation.toInt()
    val strokeWidth = density.run { shapeConfig.strokeWidth.toPx() }.toInt()
    val lightOffset = getLightShadowOffset(shapeConfig.lightSource, elevation)

    val cacheKey = NeuShadowCache.keyFor(
        pass = "fg",
        widthPx = size.width.toInt(),
        heightPx = size.height.toInt(),
        elevationPx = elevation,
        strokeWidthPx = strokeWidth.toFloat(),
        lightColor = shapeConfig.lightShadowColor,
        darkColor = shapeConfig.darkShadowColor,
        cornerDescriptor = cornerType.cacheDescriptor(),
        lightSource = shapeConfig.lightSource.name
    )

    val bitmap = NeuShadowCache.get(cacheKey) ?: run {
        val lightShadowDrawable = GradientDrawable().apply {
            setSize(width, height)
            setStroke(strokeWidth, shapeConfig.lightShadowColor.toArgb())
            setBounds(0, 0, width, height)
            setColor(Color.Transparent.toArgb())
            setNeuShape(cornerType, ShadowForm.LightShadow, radius, shapeConfig.lightSource)
        }
        val darkShadowDrawable = GradientDrawable().apply {
            setSize(width, height)
            setStroke(strokeWidth, shapeConfig.darkShadowColor.toArgb())
            setColor(Color.Transparent.toArgb())
            setBounds(0, 0, width, height)
            setNeuShape(cornerType, ShadowForm.DarkShadow, radius, shapeConfig.lightSource)
        }

        generateShadowBitmap(
            size.width.toInt(),
            size.height.toInt(),
            lightShadowDrawable,
            darkShadowDrawable,
            elevation,
            blurMaker,
            lightOffset
        )?.also { NeuShadowCache.put(cacheKey, it) }
    }

    bitmap?.asImageBitmap()?.let { drawScope.drawImage(it) }
}

private fun generateShadowBitmap(
    w: Int,
    h: Int,
    lightShadowDrawable: GradientDrawable,
    darkShadowDrawable: GradientDrawable,
    elevation: Float,
    blurMaker: BlurMaker,
    lightOffset: Pair<Float, Float>
) = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).blurred(blurMaker) {
    withTranslation(x = lightOffset.first, y = lightOffset.second) {
        lightShadowDrawable.draw(this)
    }
    // Dark shadow is drawn at origin for the inner shadow effect.
    darkShadowDrawable.draw(this)
}

/* Flat shape - before the content draw scope. */
internal fun ContentDrawScope.drawOnBackground(
    shapeConfig: ShapeConfig,
    blurMaker: BlurMaker
) {
    val drawScope = this
    val elevation = density.run { shapeConfig.elevation.toPx() }
    val horizontalInset = density.run { shapeConfig.neuInsets.horizontal.toPx() }
    val verticalInset = density.run { shapeConfig.neuInsets.vertical.toPx() }
    val cornerType = shapeConfig.cornerType
    val radius = if (cornerType is CornerType.Rounded) density.run { cornerType.radius.toPx() } else 0f

    val size = drawScope.size
    val width = size.width.toInt()
    val height = size.height.toInt()

    // Cache only the expensive blurred geometry/alpha mask. Light and dark
    // colors are applied at draw time so theme/color changes no longer force a
    // second blur of identical geometry.
    val maskCacheKey = NeuShadowCache.keyFor(
        pass = "bg-mask",
        widthPx = width,
        heightPx = height,
        elevationPx = elevation,
        strokeWidthPx = 0f,
        lightColor = Color.Transparent,
        darkColor = Color.Transparent,
        cornerDescriptor = cornerType.cacheDescriptor(),
        lightSource = shapeConfig.lightSource.name
    )

    val shadowMask = NeuShadowCache.get(maskCacheKey) ?: run {
        val maskDrawable = GradientDrawable().apply {
            setColor(Color.White.toArgb())
            setSize(width, height)
            setBounds(0, 0, width, height)
            setNeuShape(cornerType, ShadowForm.Default, radius, shapeConfig.lightSource)
        }
        maskDrawable.toBlurredBitmap(width, height, elevation, blurMaker)
            ?.also { NeuShadowCache.put(maskCacheKey, it) }
    }

    val lightShadowBitmap = shadowMask?.asImageBitmap()
    val darkShadowBitmap = lightShadowBitmap
    val lightColorFilter = ColorFilter.tint(shapeConfig.lightShadowColor, BlendMode.SrcIn)
    val darkColorFilter = ColorFilter.tint(shapeConfig.darkShadowColor, BlendMode.SrcIn)

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

    lightShadowBitmap?.let { bitmap ->
        drawScope.inset(lightHInset, lightVInset) {
            drawImage(bitmap, colorFilter = lightColorFilter)
        }
    }

    darkShadowBitmap?.let { bitmap ->
        drawScope.inset(darkHInset, darkVInset) {
            drawImage(bitmap, colorFilter = darkColorFilter)
        }
    }
}

private fun Drawable.toBlurredBitmap(
    w: Int,
    h: Int,
    elevation: Float,
    blurMaker: BlurMaker
): Bitmap? {
    val width = (w + elevation * 2).roundToInt().coerceAtLeast(1)
    val height = (h + elevation * 2).roundToInt().coerceAtLeast(1)

    return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        .blurred(blurMaker) {
            withTranslation(elevation, elevation) { draw(this) }
        }
}

internal fun Bitmap.blurred(
    blurMaker: BlurMaker,
    block: Canvas.() -> Unit
): Bitmap? {
    Canvas(this).run(block)
    return blurMaker.blur(this, sampling = NeuPerformanceConfig.blurDownsampling)
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
        is CornerType.Oval -> shape = GradientDrawable.OVAL
        is CornerType.Rounded -> {
            shape = GradientDrawable.RECTANGLE
            when (shadowForm) {
                is ShadowForm.Default -> cornerRadius = radius
                is ShadowForm.LightShadow -> {
                    cornerRadii = when (lightSource) {
                        LightSource.TOP_LEFT -> floatArrayOf(0f, 0f, radius, radius, radius, radius, radius, radius)
                        LightSource.TOP_RIGHT -> floatArrayOf(radius, radius, 0f, 0f, radius, radius, radius, radius)
                        LightSource.BOTTOM_LEFT -> floatArrayOf(radius, radius, radius, radius, 0f, 0f, radius, radius)
                        LightSource.BOTTOM_RIGHT -> floatArrayOf(radius, radius, radius, radius, radius, radius, 0f, 0f)
                    }
                }
                is ShadowForm.DarkShadow -> {
                    cornerRadii = when (lightSource) {
                        LightSource.TOP_LEFT -> floatArrayOf(radius, radius, radius, radius, 0f, 0f, radius, radius)
                        LightSource.TOP_RIGHT -> floatArrayOf(radius, radius, radius, radius, radius, radius, 0f, 0f)
                        LightSource.BOTTOM_LEFT -> floatArrayOf(0f, 0f, radius, radius, radius, radius, radius, radius)
                        LightSource.BOTTOM_RIGHT -> floatArrayOf(radius, radius, 0f, 0f, radius, radius, radius, radius)
                    }
                }
            }
        }
    }
}
