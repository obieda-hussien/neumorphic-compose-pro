package me.nikhilchaudhari.library.shapes

import android.graphics.drawable.GradientDrawable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Density
import me.nikhilchaudhari.library.internal.BlurMaker
import me.nikhilchaudhari.library.internal.NeuShadowCache

/**
 * Off-main-thread shadow production. Populates [NeuShadowCache] so the draw path
 * only composites already-blurred masks.
 */
internal object ShadowGeneration {

    fun warmForShape(
        density: Density,
        widthPx: Int,
        heightPx: Int,
        shapeConfig: ShapeConfig,
        blurMaker: BlurMaker,
        style: ShadowStyle
    ): Boolean {
        if (widthPx <= 0 || heightPx <= 0) return false
        return when (style) {
            ShadowStyle.BACKGROUND -> warmBackground(density, widthPx, heightPx, shapeConfig, blurMaker)
            ShadowStyle.FOREGROUND -> warmForeground(density, widthPx, heightPx, shapeConfig, blurMaker)
            ShadowStyle.BOTH -> {
                val a = warmBackground(density, widthPx, heightPx, shapeConfig, blurMaker)
                val b = warmForeground(density, widthPx, heightPx, shapeConfig, blurMaker)
                a || b
            }
        }
    }

    private fun warmBackground(
        density: Density,
        widthPx: Int,
        heightPx: Int,
        shapeConfig: ShapeConfig,
        blurMaker: BlurMaker
    ): Boolean {
        val elevation = with(density) { shapeConfig.elevation.toPx() }
        val cornerType = shapeConfig.cornerType
        val radius = if (cornerType is CornerType.Rounded) with(density) { cornerType.radius.toPx() } else 0f
        val key = NeuShadowCache.keyFor(
            pass = "bg-mask",
            widthPx = widthPx,
            heightPx = heightPx,
            elevationPx = elevation,
            strokeWidthPx = 0f,
            lightColor = Color.Transparent,
            darkColor = Color.Transparent,
            cornerDescriptor = cornerType.cacheDescriptor(),
            lightSource = shapeConfig.lightSource.name
        )
        if (NeuShadowCache.get(key) != null) return false

        val maskDrawable = GradientDrawable().apply {
            setColor(Color.White.toArgb())
            setSize(widthPx, heightPx)
            setBounds(0, 0, widthPx, heightPx)
            setNeuShapeForGeneration(cornerType, ShadowForm.Default, radius, shapeConfig.lightSource)
        }
        val produced = maskDrawable.toBlurredBitmapForGeneration(widthPx, heightPx, elevation, blurMaker)
            ?.also { NeuShadowCache.put(key, it) }
        return produced != null
    }

    private fun warmForeground(
        density: Density,
        widthPx: Int,
        heightPx: Int,
        shapeConfig: ShapeConfig,
        blurMaker: BlurMaker
    ): Boolean {
        val elevation = with(density) { shapeConfig.elevation.toPx() }
        val cornerType = shapeConfig.cornerType
        val radius = if (cornerType is CornerType.Rounded) with(density) { cornerType.radius.toPx() } else 0f
        val strokeWidth = with(density) { shapeConfig.strokeWidth.toPx() }.toInt()
        val width = widthPx + elevation.toInt()
        val height = heightPx + elevation.toInt()
        val lightOffset = getLightShadowOffset(shapeConfig.lightSource, elevation)

        val lightKey = NeuShadowCache.keyFor(
            pass = "fg-light-mask",
            widthPx = widthPx,
            heightPx = heightPx,
            elevationPx = elevation,
            strokeWidthPx = strokeWidth.toFloat(),
            lightColor = Color.Transparent,
            darkColor = Color.Transparent,
            cornerDescriptor = cornerType.cacheDescriptor(),
            lightSource = shapeConfig.lightSource.name
        )
        val darkKey = NeuShadowCache.keyFor(
            pass = "fg-dark-mask",
            widthPx = widthPx,
            heightPx = heightPx,
            elevationPx = elevation,
            strokeWidthPx = strokeWidth.toFloat(),
            lightColor = Color.Transparent,
            darkColor = Color.Transparent,
            cornerDescriptor = cornerType.cacheDescriptor(),
            lightSource = shapeConfig.lightSource.name
        )

        var produced = false
        if (NeuShadowCache.get(lightKey) == null) {
            val lightShadowDrawable = GradientDrawable().apply {
                setSize(width, height)
                setStroke(strokeWidth, android.graphics.Color.WHITE)
                setBounds(0, 0, width, height)
                setColor(Color.Transparent.toArgb())
                setNeuShapeForGeneration(cornerType, ShadowForm.LightShadow, radius, shapeConfig.lightSource)
            }
            generateSingleShadowMaskForGeneration(
                widthPx, heightPx, lightShadowDrawable, elevation, blurMaker, lightOffset
            )?.also {
                NeuShadowCache.put(lightKey, it)
                produced = true
            }
        }

        if (NeuShadowCache.get(darkKey) == null) {
            val darkShadowDrawable = GradientDrawable().apply {
                setSize(width, height)
                setStroke(strokeWidth, android.graphics.Color.WHITE)
                setColor(Color.Transparent.toArgb())
                setBounds(0, 0, width, height)
                setNeuShapeForGeneration(cornerType, ShadowForm.DarkShadow, radius, shapeConfig.lightSource)
            }
            generateSingleShadowMaskForGeneration(
                widthPx, heightPx, darkShadowDrawable, elevation, blurMaker, 0f to 0f
            )?.also {
                NeuShadowCache.put(darkKey, it)
                produced = true
            }
        }
        return produced
    }
}

internal enum class ShadowStyle {
    BACKGROUND,
    FOREGROUND,
    BOTH
}

internal fun shadowStyleFor(shape: NeuShape): ShadowStyle = when (shape) {
    is Pot -> ShadowStyle.BOTH
    is Pressed -> ShadowStyle.FOREGROUND
    else -> ShadowStyle.BACKGROUND
}
