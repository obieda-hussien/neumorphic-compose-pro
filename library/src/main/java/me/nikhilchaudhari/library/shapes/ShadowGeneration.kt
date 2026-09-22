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
    private fun keys(density: Density, width: Int, height: Int, config: ShapeConfig, style: ShadowStyle): List<String> {
        val passes = when (style) {
            ShadowStyle.BACKGROUND -> listOf("bg-mask")
            ShadowStyle.FOREGROUND -> listOf("fg-light-mask", "fg-dark-mask")
            ShadowStyle.BOTH -> listOf("bg-mask", "fg-light-mask", "fg-dark-mask")
        }
        return passes.map { pass -> NeuShadowCache.keyFor(pass, width, height,
            with(density) { config.elevation.toPx() },
            if (pass == "bg-mask") 0f else with(density) { config.strokeWidth.toPx() }.toInt().toFloat(),
            Color.Transparent, Color.Transparent, config.cornerType.cacheDescriptor(density.density), config.lightSource.name, config.renderSettings) }
    }
    fun requestKey(density: Density, width: Int, height: Int, config: ShapeConfig, style: ShadowStyle): String =
        keys(density, width, height, config, style).joinToString(";") + "|${density.density}|${config.neuInsets}"

    fun isReady(density: Density, width: Int, height: Int, config: ShapeConfig, style: ShadowStyle): Boolean =
        keys(density, width, height, config, style).all { NeuShadowCache.peek(it) != null }


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
            cornerDescriptor = cornerType.cacheDescriptor(density.density),
            lightSource = shapeConfig.lightSource.name, settings = shapeConfig.renderSettings
        )
        if (NeuShadowCache.get(key) != null) return false

        val maskDrawable = maskDrawable(cornerType, widthPx, heightPx, 0, ShadowForm.Default, radius, shapeConfig.lightSource)
        val produced = maskDrawable.toBlurredBitmapForGeneration(widthPx, heightPx, elevation, blurMaker, shapeConfig.renderSettings)
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
            cornerDescriptor = cornerType.cacheDescriptor(density.density),
            lightSource = shapeConfig.lightSource.name, settings = shapeConfig.renderSettings
        )
        val darkKey = NeuShadowCache.keyFor(
            pass = "fg-dark-mask",
            widthPx = widthPx,
            heightPx = heightPx,
            elevationPx = elevation,
            strokeWidthPx = strokeWidth.toFloat(),
            lightColor = Color.Transparent,
            darkColor = Color.Transparent,
            cornerDescriptor = cornerType.cacheDescriptor(density.density),
            lightSource = shapeConfig.lightSource.name, settings = shapeConfig.renderSettings
        )

        var produced = false
        if (NeuShadowCache.get(lightKey) == null) {
            val lightShadowDrawable = maskDrawable(cornerType, width, height, strokeWidth, ShadowForm.LightShadow, radius, shapeConfig.lightSource)
            generateSingleShadowMaskForGeneration(
                widthPx, heightPx, lightShadowDrawable, elevation, blurMaker, lightOffset, shapeConfig.renderSettings
            )?.also {
                NeuShadowCache.put(lightKey, it)
                produced = true
            }
        }

        if (NeuShadowCache.get(darkKey) == null) {
            val darkShadowDrawable = maskDrawable(cornerType, width, height, strokeWidth, ShadowForm.DarkShadow, radius, shapeConfig.lightSource)
            generateSingleShadowMaskForGeneration(
                widthPx, heightPx, darkShadowDrawable, elevation, blurMaker, 0f to 0f, shapeConfig.renderSettings
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
    is ComposeNeuShape -> if (shape.recessed) ShadowStyle.FOREGROUND else ShadowStyle.BACKGROUND
    is Pot -> ShadowStyle.BOTH
    is Pressed -> ShadowStyle.FOREGROUND
    else -> ShadowStyle.BACKGROUND
}
