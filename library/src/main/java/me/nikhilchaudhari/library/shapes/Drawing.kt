package me.nikhilchaudhari.library.shapes

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import androidx.compose.ui.graphics.Color
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
 * [NeuShadowCache] cache keys. `CornerType.Oval` is a plain Kotlin `object`
 * (not a `data object`), so its default `toString()` includes an identity
 * hash suffix (e.g. "Oval@1a2b3c") - stable for the lifetime of the app
 * process (it's a true singleton, always the same instance), but an opaque,
 * non-obvious thing to have baked into a cache key. This gives an explicit,
 * readable descriptor instead.
 */
internal fun CornerType.cacheDescriptor(): String = when (this) {
    is CornerType.Oval -> "Oval"
    is CornerType.Rounded -> "Rounded(${radius.value})"
}

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
internal fun DrawScope.drawOnForeground(
    shapeConfig: ShapeConfig,
    blurMaker: BlurMaker
) {

    val drawScope = this
    val lightShadowDrawable = GradientDrawable()
    val darkShadowDrawable = GradientDrawable()

    val elevation = this.density.run { shapeConfig.elevation.toPx() }
    val cornerType = shapeConfig.cornerType
    val radius =
        if (cornerType is CornerType.Rounded) this.density.run { cornerType.radius.toPx() } else 0f

    val size = drawScope.size
    val width = size.width.toInt() + elevation.toInt()
    val height = size.height.toInt() + elevation.toInt()
    val strokeWidth = this.density.run { shapeConfig.strokeWidth.toPx() }.toInt()

    lightShadowDrawable.apply {
        setSize(width, height)
        setStroke(strokeWidth, shapeConfig.lightShadowColor.toArgb())
        setBounds(0, 0, width, height)
        setColor(Color.Transparent.toArgb())
        setNeuShape(cornerType, ShadowForm.LightShadow, radius, shapeConfig.lightSource)
    }

    darkShadowDrawable.apply {
        setSize(width, height)
        setStroke(strokeWidth, shapeConfig.darkShadowColor.toArgb())
        setColor(Color.Transparent.toArgb())
        setBounds(0, 0, width, height)
        setNeuShape(cornerType, ShadowForm.DarkShadow, radius, shapeConfig.lightSource)
    }

    val lightOffset = getLightShadowOffset(shapeConfig.lightSource, elevation)
    val darkOffset = getDarkShadowOffset(shapeConfig.lightSource, elevation)

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

    val bitmap = NeuShadowCache.get(cacheKey) ?: generateShadowBitmap(
        size.width.toInt(),
        size.height.toInt(),
        lightShadowDrawable,
        darkShadowDrawable,
        elevation,
        blurMaker,
        lightOffset,
        darkOffset
    )?.also { NeuShadowCache.put(cacheKey, it) }

    bitmap?.asImageBitmap()?.let {
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
) = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).blurred(blurMaker) {
    withTranslation(
        x = lightOffset.first,
        y = lightOffset.second
    ) {
        lightShadowDrawable.draw(this)
    }
    // Dark shadow is drawn at origin for inner shadow effect
    darkShadowDrawable.draw(this)
}


/* Flat shape - before the content draw scope */
internal fun ContentDrawScope.drawOnBackground(
    shapeConfig: ShapeConfig,
    blurMaker: BlurMaker
) {
    val drawScope = this
    val lightShadowDrawable = GradientDrawable()
    val darkShadowDrawable = GradientDrawable()

    val elevation = this.density.run { shapeConfig.elevation.toPx() }
    val horizontalInset = this.density.run { shapeConfig.neuInsets.horizontal.toPx() }
    val verticalInset = this.density.run { shapeConfig.neuInsets.vertical.toPx() }
    val cornerType = shapeConfig.cornerType
    val radius =
        if (cornerType is CornerType.Rounded) this.density.run { cornerType.radius.toPx() } else 0f

    val size = drawScope.size
    val width = size.width.toInt()
    val height = size.height.toInt()

    lightShadowDrawable.apply {
        setColor(shapeConfig.lightShadowColor.toArgb())
        setSize(width, height)
        setBounds(0, 0, width, height)
        setNeuShape(cornerType, ShadowForm.Default, radius, shapeConfig.lightSource)
    }

    darkShadowDrawable.apply {
        setColor(shapeConfig.darkShadowColor.toArgb())
        setSize(width, height)
        setBounds(0, 0, width, height)
        setNeuShape(cornerType, ShadowForm.Default, radius, shapeConfig.lightSource)
    }

    val lightCacheKey = NeuShadowCache.keyFor(
        pass = "bg-light",
        widthPx = width,
        heightPx = height,
        elevationPx = elevation,
        strokeWidthPx = 0f,
        lightColor = shapeConfig.lightShadowColor,
        darkColor = Color.Transparent,
        cornerDescriptor = cornerType.cacheDescriptor(),
        lightSource = shapeConfig.lightSource.name
    )
    val darkCacheKey = NeuShadowCache.keyFor(
        pass = "bg-dark",
        widthPx = width,
        heightPx = height,
        elevationPx = elevation,
        strokeWidthPx = 0f,
        lightColor = Color.Transparent,
        darkColor = shapeConfig.darkShadowColor,
        cornerDescriptor = cornerType.cacheDescriptor(),
        lightSource = shapeConfig.lightSource.name
    )

    val lightShadowBitmap =
        (NeuShadowCache.get(lightCacheKey) ?: lightShadowDrawable
            .toBlurredBitmap(width, height, elevation, blurMaker)
            ?.also { NeuShadowCache.put(lightCacheKey, it) })
            ?.asImageBitmap()
    val darkShadowBitmap =
        (NeuShadowCache.get(darkCacheKey) ?: darkShadowDrawable
            .toBlurredBitmap(width, height, elevation, blurMaker)
            ?.also { NeuShadowCache.put(darkCacheKey, it) })
            ?.asImageBitmap()

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

    lightShadowBitmap?.let { bitmap ->
        drawScope.inset(lightHInset, lightVInset) {
            drawImage(bitmap)
        }
    }

    darkShadowBitmap?.let { bitmap ->
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
    return blurMaker.blur(
        this.also {
            Canvas(this).run {
                block()
            }
        },
        sampling = NeuPerformanceConfig.blurDownsampling
    )
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
