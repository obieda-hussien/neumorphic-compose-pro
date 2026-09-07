package me.nikhilchaudhari.library

import android.content.Context
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
    val context = LocalContext.current
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
        elevation = NeuRenderPolicy.quantizeElevation(animatedElevation),
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
        elevation = NeuRenderPolicy.quantizeElevation(animatedElevation),
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
        strokeWidth = NeuRenderPolicy.quantizeDp(animatedStrokeWidth),
        elevation = NeuRenderPolicy.quantizeElevation(animatedElevation),
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

/**
 * Production [DrawModifierNode] for neumorphic shadows.
 *
 * 1. Layout/style changes mint a monotonic request token.
 * 2. Generation runs on [Dispatchers.Default] with power/thermal awareness.
 * 3. Rapid successive requests (animation) are coalesced via debounce.
 * 4. Completed workers publish only when their token still matches.
 * 5. Draw path is never blocked; cold misses may still generate sync on first paint.
 * 6. GPU backend receives preferSize hints for hot list-item dimensions.
 */
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

    private var appContext: Context = context.applicationContext ?: context
    private val blurMaker: BlurMaker get() = NeuBlurMakerHolder.get(appContext)

    private val requestToken = AtomicLong(0L)
    private var runningJob: Job? = null
    private var lastRequestKey: String? = null
    private var lastSize: Size = Size.Zero
    private var consecutiveRapidChanges: Int = 0
    private var lastScheduleNs: Long = 0L

    fun update(
        context: Context,
        insets: NeuInsets,
        neuShape: NeuShape,
        lightShadowColor: Color,
        darkShadowColor: Color,
        strokeWidth: Dp,
        elevation: Dp,
        lightSource: LightSource
    ) {
        appContext = context.applicationContext ?: context
        this.insets = insets
        this.neuShape = neuShape
        this.lightShadowColor = lightShadowColor
        this.darkShadowColor = darkShadowColor
        this.strokeWidth = strokeWidth
        this.elevation = elevation
        this.lightSource = lightSource
        lastRequestKey = null
        invalidateDraw()
    }

    override fun onDetach() {
        runningJob?.cancel()
        runningJob = null
        consecutiveRapidChanges = 0
        super.onDetach()
    }

    override fun ContentDrawScope.draw() {
        val shapeConfig = ShapeConfig(
            neuInsets = insets,
            elevation = elevation,
            lightShadowColor = lightShadowColor,
            darkShadowColor = darkShadowColor,
            strokeWidth = strokeWidth,
            lightSource = lightSource
        )

        val widthPx = size.width.toInt()
        val heightPx = size.height.toInt()
        val requestKey = buildString {
            append(widthPx).append('x').append(heightPx).append('|')
            append(elevation.value).append('|')
            append(strokeWidth.value).append('|')
            append(lightSource.name).append('|')
            append(neuShape::class.java.name).append('|')
            append(lightShadowColor.value).append('|')
            append(darkShadowColor.value)
        }

        val sizeChanged = lastSize != size
        lastSize = size

        if (requestKey != lastRequestKey || sizeChanged) {
            lastRequestKey = requestKey
            scheduleAsyncGeneration(
                density = this,
                widthPx = widthPx,
                heightPx = heightPx,
                shapeConfig = shapeConfig,
                styleShape = neuShape
            )
        }

        neuShape.drawShadows(this, blurMaker, shapeConfig)
    }

    private fun scheduleAsyncGeneration(
        density: Density,
        widthPx: Int,
        heightPx: Int,
        shapeConfig: ShapeConfig,
        styleShape: NeuShape
    ) {
        if (!isAttached || widthPx <= 0 || heightPx <= 0) return

        val now = System.nanoTime()
        val elapsedMs = (now - lastScheduleNs) / 1_000_000L
        lastScheduleNs = now

        if (elapsedMs < RAPID_CHANGE_WINDOW_MS) {
            consecutiveRapidChanges++
        } else {
            consecutiveRapidChanges = 0
        }

        val token = requestToken.incrementAndGet()
        runningJob?.cancel()

        val style = shadowStyleFor(styleShape)
        val maker = blurMaker
        val densitySnapshot = Density(density.density, density.fontScale)
        val configSnapshot = shapeConfig.copy()

        val underPressure = NeuThermalPolicy.cacheTier() >= 2 || NeuPowerPolicy.isPowerSave()
        val debounceMs = when {
            underPressure && consecutiveRapidChanges >= 2 -> DEBOUNCE_PRESSURE_MS
            consecutiveRapidChanges >= 3 -> DEBOUNCE_ANIMATION_MS
            else -> 0L
        }

        maker.preferSize(widthPx, heightPx)

        runningJob = coroutineScope.launch(Dispatchers.Default) {
            if (debounceMs > 0L) {
                delay(debounceMs)
                if (token != requestToken.get() || !isActive) return@launch
            }

            try {
                ShadowGeneration.warmForShape(
                    density = densitySnapshot,
                    widthPx = widthPx,
                    heightPx = heightPx,
                    shapeConfig = configSnapshot,
                    blurMaker = maker,
                    style = style
                )
            } catch (_: Throwable) {
                // Never crash the UI pipeline from a background blur failure.
            }

            if (token != requestToken.get()) return@launch
            withContext(Dispatchers.Main.immediate) {
                if (token == requestToken.get() && isAttached) {
                    invalidateDraw()
                }
            }
        }
    }

    companion object {
        private const val RAPID_CHANGE_WINDOW_MS = 48L
        private const val DEBOUNCE_ANIMATION_MS = 32L
        private const val DEBOUNCE_PRESSURE_MS = 64L
    }
}
