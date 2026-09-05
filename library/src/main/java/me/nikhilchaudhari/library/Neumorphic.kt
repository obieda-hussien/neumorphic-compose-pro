package me.nikhilchaudhari.library

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.DrawModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.InspectorValueInfo
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.nikhilchaudhari.library.internal.BlurMaker
import me.nikhilchaudhari.library.internal.NeuBlurMakerHolder
import me.nikhilchaudhari.library.shapes.NeuShape
import me.nikhilchaudhari.library.shapes.Punched
import me.nikhilchaudhari.library.shapes.ShapeConfig

/** Insets configuration for neumorphic shadows. */
data class NeuInsets(
    val horizontal: Dp = 6.dp,
    val vertical: Dp = 6.dp
)

enum class LightSource {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT
}

enum class NeuAnimationType {
    TWEEN,
    SPRING,
    SPRING_BOUNCY,
    NONE
}

object NeuConstants {
    const val PRESSED_ELEVATION_FACTOR = 0.5f
    const val DEFAULT_ANIMATION_DURATION_MS = 150
    const val DEFAULT_SPRING_STIFFNESS = Spring.StiffnessMedium
    const val DEFAULT_SPRING_DAMPING = Spring.DampingRatioMediumBouncy
    const val EXPRESSIVE_SPRING_STIFFNESS = Spring.StiffnessLow
    const val EXPRESSIVE_SPRING_DAMPING = Spring.DampingRatioLowBouncy
}

/** Apply a neumorphic effect to a composable. */
fun Modifier.neumorphic(
    neuInsets: NeuInsets = NeuInsets(),
    neuShape: NeuShape = Punched.Rounded(),
    lightShadowColor: Color = Color.White,
    darkShadowColor: Color = Color.LightGray,
    strokeWidth: Dp = 6.dp,
    elevation: Dp = 6.dp,
    lightSource: LightSource = LightSource.TOP_LEFT
) = composed {
    val context = LocalContext.current
    val blurMaker = remember(context) { NeuBlurMakerHolder.get(context) }

    this.then(
        NeumorphicModifier(
            blurMaker,
            neuInsets,
            neuShape,
            lightShadowColor,
            darkShadowColor,
            strokeWidth,
            elevation,
            lightSource,
            inspectorInfo = debugInspectorInfo {
                name = "neumorphic"
                properties["context"] = context
                properties["neuInsets"] = neuInsets
                properties["neuShape"] = neuShape
                properties["elevation"] = elevation
                properties["strokeWidth"] = strokeWidth
                properties["lightShadowColor"] = lightShadowColor
                properties["darkShadowColor"] = darkShadowColor
                properties["lightSource"] = lightSource
            }
        )
    )
}

fun Modifier.animatedNeumorphic(
    neuInsets: NeuInsets = NeuInsets(),
    neuShape: NeuShape = Punched.Rounded(),
    lightShadowColor: Color = Color.White,
    darkShadowColor: Color = Color.LightGray,
    strokeWidth: Dp = 6.dp,
    elevation: Dp = 6.dp,
    lightSource: LightSource = LightSource.TOP_LEFT,
    pressed: Boolean = false,
    animationDuration: Int = NeuConstants.DEFAULT_ANIMATION_DURATION_MS
) = composed {
    val animatedElevation by animateDpAsState(
        targetValue = if (pressed) elevation * NeuConstants.PRESSED_ELEVATION_FACTOR else elevation,
        animationSpec = tween(durationMillis = animationDuration),
        label = "elevationAnimation"
    )

    neumorphic(
        neuInsets = neuInsets,
        neuShape = neuShape,
        lightShadowColor = lightShadowColor,
        darkShadowColor = darkShadowColor,
        strokeWidth = strokeWidth,
        elevation = animatedElevation,
        lightSource = lightSource
    )
}

fun Modifier.springNeumorphic(
    neuInsets: NeuInsets = NeuInsets(),
    neuShape: NeuShape = Punched.Rounded(),
    lightShadowColor: Color = Color.White,
    darkShadowColor: Color = Color.LightGray,
    strokeWidth: Dp = 6.dp,
    elevation: Dp = 6.dp,
    lightSource: LightSource = LightSource.TOP_LEFT,
    pressed: Boolean = false,
    animationType: NeuAnimationType = NeuAnimationType.SPRING_BOUNCY,
    stiffness: Float = NeuConstants.EXPRESSIVE_SPRING_STIFFNESS,
    dampingRatio: Float = NeuConstants.EXPRESSIVE_SPRING_DAMPING
) = composed {
    val targetElevation = if (pressed) elevation * NeuConstants.PRESSED_ELEVATION_FACTOR else elevation

    val animatedElevation by when (animationType) {
        NeuAnimationType.SPRING, NeuAnimationType.SPRING_BOUNCY -> {
            animateDpAsState(
                targetValue = targetElevation,
                animationSpec = spring(
                    dampingRatio = dampingRatio,
                    stiffness = stiffness
                ),
                label = "springElevationAnimation"
            )
        }
        NeuAnimationType.TWEEN -> {
            animateDpAsState(
                targetValue = targetElevation,
                animationSpec = tween(durationMillis = NeuConstants.DEFAULT_ANIMATION_DURATION_MS),
                label = "tweenElevationAnimation"
            )
        }
        NeuAnimationType.NONE -> targetElevation.let {
            androidx.compose.runtime.rememberUpdatedState(it)
        }
    }

    neumorphic(
        neuInsets = neuInsets,
        neuShape = neuShape,
        lightShadowColor = lightShadowColor,
        darkShadowColor = darkShadowColor,
        strokeWidth = strokeWidth,
        elevation = animatedElevation,
        lightSource = lightSource
    )
}

fun Modifier.expressiveNeumorphic(
    neuInsets: NeuInsets = NeuInsets(),
    neuShape: NeuShape = Punched.Rounded(),
    lightShadowColor: Color = Color.White,
    darkShadowColor: Color = Color.LightGray,
    strokeWidth: Dp = 6.dp,
    elevation: Dp = 6.dp,
    lightSource: LightSource = LightSource.TOP_LEFT,
    pressed: Boolean = false,
    hovered: Boolean = false
) = composed {
    val targetElevation = when {
        pressed -> elevation * 0.3f
        hovered -> elevation * 1.2f
        else -> elevation
    }

    val animatedElevation by animateDpAsState(
        targetValue = targetElevation,
        animationSpec = spring(
            dampingRatio = NeuConstants.EXPRESSIVE_SPRING_DAMPING,
            stiffness = NeuConstants.EXPRESSIVE_SPRING_STIFFNESS
        ),
        label = "expressiveElevationAnimation"
    )

    val animatedStrokeWidth by animateDpAsState(
        targetValue = if (pressed) strokeWidth * 0.8f else strokeWidth,
        animationSpec = spring(
            dampingRatio = NeuConstants.EXPRESSIVE_SPRING_DAMPING,
            stiffness = NeuConstants.EXPRESSIVE_SPRING_STIFFNESS
        ),
        label = "expressiveStrokeAnimation"
    )

    neumorphic(
        neuInsets = neuInsets,
        neuShape = neuShape,
        lightShadowColor = lightShadowColor,
        darkShadowColor = darkShadowColor,
        strokeWidth = animatedStrokeWidth,
        elevation = animatedElevation,
        lightSource = lightSource
    )
}

internal class NeumorphicModifier(
    private val blurMaker: BlurMaker,
    private val insets: NeuInsets,
    private val neuShape: NeuShape,
    private val lightShadowColor: Color,
    private val darkShadowColor: Color,
    private val strokeWidth: Dp,
    private val elevation: Dp,
    private val lightSource: LightSource,
    inspectorInfo: InspectorInfo.() -> Unit
) : DrawModifier, InspectorValueInfo(inspectorInfo) {

    override fun ContentDrawScope.draw() {
        val shapeConfig = ShapeConfig(
            insets,
            elevation,
            lightShadowColor,
            darkShadowColor,
            strokeWidth,
            lightSource = lightSource
        )
        neuShape.drawShadows(this, blurMaker, shapeConfig)
    }
}
