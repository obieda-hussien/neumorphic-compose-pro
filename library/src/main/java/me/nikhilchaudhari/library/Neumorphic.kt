package me.nikhilchaudhari.library

import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
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
import me.nikhilchaudhari.library.internal.BlurConfig
import me.nikhilchaudhari.library.internal.BlurMaker
import me.nikhilchaudhari.library.shapes.NeuShape
import me.nikhilchaudhari.library.shapes.Punched
import me.nikhilchaudhari.library.shapes.ShapeConfig
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Insets configuration for neumorphic shadows
 */
data class NeuInsets(
    val horizontal: Dp = 6.dp,
    val vertical: Dp = 6.dp
)

/**
 * Light source direction for neumorphic shadows
 */
enum class LightSource {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT
}

/**
 * Animation type for neumorphic effects
 */
enum class NeuAnimationType {
    /** Linear animation with tween */
    TWEEN,
    /** Spring physics-based animation - more natural feel */
    SPRING,
    /** Bouncy spring animation - Material 3 Expressive */
    SPRING_BOUNCY,
    /** No animation */
    NONE
}

/**
 * Constants for neumorphic effects
 */
object NeuConstants {
    /** Factor by which elevation is reduced when pressed */
    const val PRESSED_ELEVATION_FACTOR = 0.5f
    
    /** Default animation duration in milliseconds */
    const val DEFAULT_ANIMATION_DURATION_MS = 150
    
    /** Default spring stiffness for animations */
    const val DEFAULT_SPRING_STIFFNESS = Spring.StiffnessMedium
    
    /** Default spring damping for animations */
    const val DEFAULT_SPRING_DAMPING = Spring.DampingRatioMediumBouncy
    
    /** Expressive spring stiffness - more bouncy and playful */
    const val EXPRESSIVE_SPRING_STIFFNESS = Spring.StiffnessLow
    
    /** Expressive spring damping - more bouncy effect */
    const val EXPRESSIVE_SPRING_DAMPING = Spring.DampingRatioLowBouncy
}

/**
 * Apply neumorphic effect to a composable
 *
 * @param neuInsets Shadow insets (horizontal and vertical)
 * @param neuShape Shape type (Punched, Pressed, Pot)
 * @param lightShadowColor Color of the light shadow (typically white or light gray)
 * @param darkShadowColor Color of the dark shadow (typically dark gray)
 * @param strokeWidth Stroke width for internal shadows
 * @param elevation Shadow elevation
 * @param lightSource Direction of the light source for shadow placement
 */
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
    this.then(
        NeumorphicModifier(
            context,
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

/**
 * Animated neumorphic effect with smooth transitions
 *
 * @param neuInsets Shadow insets (horizontal and vertical)
 * @param neuShape Shape type (Punched, Pressed, Pot)
 * @param lightShadowColor Color of the light shadow
 * @param darkShadowColor Color of the dark shadow
 * @param strokeWidth Stroke width for internal shadows
 * @param elevation Shadow elevation
 * @param lightSource Direction of the light source
 * @param pressed Whether the component is pressed (for animation)
 * @param animationDuration Duration of the animation in milliseconds
 */
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

/**
 * Spring physics-based animated neumorphic effect
 * Material 3 Expressive style with bouncy, natural feel
 *
 * @param neuInsets Shadow insets (horizontal and vertical)
 * @param neuShape Shape type (Punched, Pressed, Pot)
 * @param lightShadowColor Color of the light shadow
 * @param darkShadowColor Color of the dark shadow
 * @param strokeWidth Stroke width for internal shadows
 * @param elevation Shadow elevation
 * @param lightSource Direction of the light source
 * @param pressed Whether the component is pressed (for animation)
 * @param animationType Type of animation to use
 * @param stiffness Spring stiffness (only used for spring animations)
 * @param dampingRatio Spring damping ratio (only used for spring animations)
 */
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
        NeuAnimationType.NONE -> {
            animateDpAsState(
                targetValue = targetElevation,
                animationSpec = spring(stiffness = Spring.StiffnessHigh),
                label = "instantElevationAnimation"
            )
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

/**
 * Expressive neumorphic effect with Material 3 Expressive animations
 * Combines spring physics with expressive color transitions
 *
 * @param neuInsets Shadow insets (horizontal and vertical)
 * @param neuShape Shape type (Punched, Pressed, Pot)
 * @param lightShadowColor Color of the light shadow
 * @param darkShadowColor Color of the dark shadow
 * @param strokeWidth Stroke width for internal shadows
 * @param elevation Shadow elevation
 * @param lightSource Direction of the light source
 * @param pressed Whether the component is pressed (for animation)
 * @param hovered Whether the component is hovered (for animation)
 */
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
    context: Context,
    private val insets: NeuInsets,
    private val neuShape: NeuShape,
    private val lightShadowColor: Color,
    private val darkShadowColor: Color,
    private val strokeWidth: Dp,
    private val elevation: Dp,
    private val lightSource: LightSource,
    inspectorInfo: InspectorInfo.() -> Unit
) : DrawModifier, InspectorValueInfo(inspectorInfo) {

    private val blurMaker =
        BlurMaker(context, calculateDefaultBlurRadius(context.resources.displayMetrics))

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

    private fun calculateDefaultBlurRadius(displayMetrics: DisplayMetrics): Int {
        val densityStable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            DisplayMetrics.DENSITY_DEVICE_STABLE / DisplayMetrics.DENSITY_DEFAULT.toFloat()
        } else {
            displayMetrics.density
        }
        return min(BlurConfig.MAX_RADIUS, (densityStable * 10).roundToInt())
    }
}