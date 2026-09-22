package me.nikhilchaudhari.library

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import android.content.Context
import android.os.Build
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.nikhilchaudhari.library.internal.BlurMaker
import me.nikhilchaudhari.library.internal.NeuBlurMakerHolder
import me.nikhilchaudhari.library.internal.NeuPowerPolicy
import me.nikhilchaudhari.library.internal.NeuRenderPolicy
import me.nikhilchaudhari.library.internal.NeuThermalPolicy
import me.nikhilchaudhari.library.shapes.NeuShape
import me.nikhilchaudhari.library.shapes.Punched
import me.nikhilchaudhari.library.shapes.ShadowGeneration
import me.nikhilchaudhari.library.shapes.ShapeConfig
import me.nikhilchaudhari.library.shapes.shadowStyleFor
import java.util.concurrent.atomic.AtomicLong

/** Insets configuration for neumorphic shadows. */
@Immutable
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
) = composed(
    inspectorInfo = debugInspectorInfo {
        name = "neumorphic"
        properties["neuInsets"] = neuInsets
        properties["neuShape"] = neuShape
        properties["elevation"] = elevation
        properties["strokeWidth"] = strokeWidth
        properties["lightShadowColor"] = lightShadowColor
        properties["darkShadowColor"] = darkShadowColor
        properties["lightSource"] = lightSource
    }
) {
    require(elevation.value.isFinite() && elevation.value >= 0f)
    require(strokeWidth.value.isFinite() && strokeWidth.value >= 0f)
    val context = LocalContext.current
    val outline = when (val corners = neuShape.shadowCorners) {
        is me.nikhilchaudhari.library.shapes.CornerType.Oval -> CircleShape
        is me.nikhilchaudhari.library.shapes.CornerType.Rounded -> RoundedCornerShape(corners.radius)
        else -> RoundedCornerShape(0.dp)
    }
    val contrastBorder = if (LocalNeuTokens.current.highContrast) Modifier.border(
        1.dp, neuContentColor(NeuTheme.colorScheme().backgroundColor),
        (neuShape as? me.nikhilchaudhari.library.shapes.ComposeNeuShape)?.shape ?: outline) else Modifier
    this.then(
        NeumorphicElement(
            context = context.applicationContext ?: context,
            insets = neuInsets,
            neuShape = neuShape,
            lightShadowColor = lightShadowColor,
            darkShadowColor = darkShadowColor,
            strokeWidth = strokeWidth,
            elevation = elevation,
            lightSource = lightSource
        )
    ).then(contrastBorder)
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
        animationSpec = if (LocalNeuTokens.current.reduceMotion) androidx.compose.animation.core.snap() else tween(durationMillis = animationDuration),
        label = "elevationAnimation"
    )

    neumorphic(
        neuInsets = neuInsets,
        neuShape = neuShape,
        lightShadowColor = lightShadowColor,
        darkShadowColor = darkShadowColor,
        strokeWidth = strokeWidth,
        elevation = NeuRenderPolicy.quantizeElevation(if (LocalNeuTokens.current.reduceMotion) (if (pressed) elevation * NeuConstants.PRESSED_ELEVATION_FACTOR else elevation) else animatedElevation),
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
                animationSpec = if (me.nikhilchaudhari.library.LocalNeuTokens.current.reduceMotion) androidx.compose.animation.core.snap() else spring(
                    dampingRatio = dampingRatio,
                    stiffness = stiffness
                ),
                label = "springElevationAnimation"
            )
        }
        NeuAnimationType.TWEEN -> {
            animateDpAsState(
                targetValue = targetElevation,
                animationSpec = if (LocalNeuTokens.current.reduceMotion) androidx.compose.animation.core.snap() else tween(durationMillis = NeuConstants.DEFAULT_ANIMATION_DURATION_MS),
                label = "tweenElevationAnimation"
            )
        }
        NeuAnimationType.NONE -> {
            rememberUpdatedState(targetElevation)
        }
    }

    neumorphic(
        neuInsets = neuInsets,
        neuShape = neuShape,
        lightShadowColor = lightShadowColor,
        darkShadowColor = darkShadowColor,
        strokeWidth = strokeWidth,
        elevation = NeuRenderPolicy.quantizeElevation(if (LocalNeuTokens.current.reduceMotion) (if (pressed) elevation * NeuConstants.PRESSED_ELEVATION_FACTOR else elevation) else animatedElevation),
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
        animationSpec = if (me.nikhilchaudhari.library.LocalNeuTokens.current.reduceMotion) androidx.compose.animation.core.snap() else spring(
            dampingRatio = NeuConstants.EXPRESSIVE_SPRING_DAMPING,
            stiffness = NeuConstants.EXPRESSIVE_SPRING_STIFFNESS
        ),
        label = "expressiveElevationAnimation"
    )

    val animatedStrokeWidth by animateDpAsState(
        targetValue = if (pressed) strokeWidth * 0.8f else strokeWidth,
        animationSpec = if (me.nikhilchaudhari.library.LocalNeuTokens.current.reduceMotion) androidx.compose.animation.core.snap() else spring(
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
        strokeWidth = NeuRenderPolicy.quantizeDp(animatedStrokeWidth),
        elevation = NeuRenderPolicy.quantizeElevation(if (LocalNeuTokens.current.reduceMotion) (if (pressed) elevation * NeuConstants.PRESSED_ELEVATION_FACTOR else elevation) else animatedElevation),
        lightSource = lightSource
    )
}

private data class NeumorphicElement(
    val context: Context,
    val insets: NeuInsets,
    val neuShape: NeuShape,
    val lightShadowColor: Color,
    val darkShadowColor: Color,
    val strokeWidth: Dp,
    val elevation: Dp,
    val lightSource: LightSource
) : ModifierNodeElement<NeumorphicNode>() {

    override fun create(): NeumorphicNode =
        NeumorphicNode(
            context = context,
            insets = insets,
            neuShape = neuShape,
            lightShadowColor = lightShadowColor,
            darkShadowColor = darkShadowColor,
            strokeWidth = strokeWidth,
            elevation = elevation,
            lightSource = lightSource
        )

    override fun update(node: NeumorphicNode) {
        node.update(
            context = context,
            insets = insets,
            neuShape = neuShape,
            lightShadowColor = lightShadowColor,
            darkShadowColor = darkShadowColor,
            strokeWidth = strokeWidth,
            elevation = elevation,
            lightSource = lightSource
        )
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "neumorphic"
        properties["elevation"] = elevation
        properties["strokeWidth"] = strokeWidth
        properties["lightSource"] = lightSource
        properties["neuShape"] = neuShape::class.simpleName
    }
}

/** Built-in shadows are generated outside draw; content remains visible on cold misses. */
internal class NeumorphicNode(
    context: Context,
    private var insets: NeuInsets,
    private var neuShape: NeuShape,
    private var lightShadowColor: Color,
    private var darkShadowColor: Color,
    private var strokeWidth: Dp,
    private var elevation: Dp,
    private var lightSource: LightSource
) : Modifier.Node(), DrawModifierNode {
    private var appContext = context.applicationContext ?: context
    private var runningJob: Job? = null
    private var requested: String? = null
    private var retries = 0
    private var ready: ShapeConfig? = null
    private var readyShape: NeuShape? = null
    private var readySize = Size.Zero

    fun update(context: Context, insets: NeuInsets, neuShape: NeuShape,
        lightShadowColor: Color, darkShadowColor: Color, strokeWidth: Dp,
        elevation: Dp, lightSource: LightSource) {
        appContext = context.applicationContext ?: context
        this.insets = insets
        this.neuShape = neuShape
        this.lightShadowColor = lightShadowColor
        this.darkShadowColor = darkShadowColor
        this.strokeWidth = strokeWidth
        this.elevation = elevation
        this.lightSource = lightSource
        invalidateDraw()
    }

    override fun onDetach() {
        runningJob?.cancel()
        requested = null
        ready = null
        readyShape = null
    }

    override fun ContentDrawScope.draw() {
        if (size.width < 1f || size.height < 1f) { drawContent(); return }
        val maker = NeuBlurMakerHolder.get(appContext)
        val config = ShapeConfig(insets, elevation, lightShadowColor, darkShadowColor,
            strokeWidth, neuShape.resolveCorners(size, this, layoutDirection), lightSource, false)
        val style = shadowStyleFor(neuShape)
        val densitySnapshot = Density(density, fontScale)
        val requestSize = size
        val shape = neuShape
        val key = ShadowGeneration.requestKey(densitySnapshot, size.width.toInt(), size.height.toInt(), config, style)
        if (requested != key || (runningJob?.isActive != true && !ShadowGeneration.isReady(
                densitySnapshot, size.width.toInt(), size.height.toInt(), config, style))) {
            if (requested != key) retries = 0
            requested = key
            runningJob?.cancel()
            runningJob = coroutineScope.launch {
                // Coalesce rapidly changing animation values without doing work in draw().
                delay(16)
                val success = me.nikhilchaudhari.library.internal.ShadowWorkQueue.generate(key) {
                    ShadowGeneration.warmForShape(densitySnapshot, requestSize.width.toInt(),
                        requestSize.height.toInt(), config, maker, style)
                    ShadowGeneration.isReady(densitySnapshot, requestSize.width.toInt(),
                        requestSize.height.toInt(), config, style)
                }
                withContext(Dispatchers.Main.immediate) {
                    if (isAttached && requested == key && success) {
                        retries = 0
                        ready = config
                        readyShape = shape
                        readySize = requestSize
                        invalidateDraw()
                    } else if (isAttached && requested == key && retries < 3) {
                        retries++
                        delay(100L * retries)
                        if (isAttached && requested == key) {
                            // Mark the request complete before retrying on a later frame.
                            runningJob = null
                            invalidateDraw()
                        }
                    }
                }
            }
        }
        if (ShadowGeneration.isReady(densitySnapshot, size.width.toInt(), size.height.toInt(), config, style)) {
            ready = config
            readyShape = shape
            readySize = size
        }
        val previous = ready
        if (previous != null && readySize == size) {
            readyShape?.drawShadows(this, maker, previous.copy(
                lightShadowColor = lightShadowColor, darkShadowColor = darkShadowColor))
        } else {
            // Cheap first-frame placeholder: content alone, never a synchronous blur.
            drawContent()
        }
    }
}
