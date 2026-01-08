package me.nikhilchaudhari.library

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.nikhilchaudhari.library.shapes.NeuShape
import me.nikhilchaudhari.library.shapes.Punched

/**
 * Clickable neumorphic modifier with press animation
 * 
 * @param onClick Action to perform when clicked
 * @param enabled Whether the click is enabled
 * @param neuInsets Shadow insets
 * @param neuShape Shape type
 * @param lightShadowColor Light shadow color
 * @param darkShadowColor Dark shadow color
 * @param strokeWidth Stroke width
 * @param elevation Shadow elevation
 * @param lightSource Light source direction
 */
fun Modifier.neumorphicClickable(
    onClick: () -> Unit,
    enabled: Boolean = true,
    neuInsets: NeuInsets = NeuInsets(),
    neuShape: NeuShape = Punched.Rounded(),
    lightShadowColor: Color = Color.White,
    darkShadowColor: Color = Color.LightGray,
    strokeWidth: Dp = 6.dp,
    elevation: Dp = 6.dp,
    lightSource: LightSource = LightSource.TOP_LEFT
) = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    this
        .animatedNeumorphic(
            neuInsets = neuInsets,
            neuShape = neuShape,
            lightShadowColor = lightShadowColor,
            darkShadowColor = darkShadowColor,
            strokeWidth = strokeWidth,
            elevation = elevation,
            lightSource = lightSource,
            pressed = isPressed
        )
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick
        )
}

/**
 * Material 3 Expressive clickable neumorphic modifier with spring animations and scale effect
 * 
 * @param onClick Action to perform when clicked
 * @param enabled Whether the click is enabled
 * @param colorScheme Neumorphic color scheme to use (overrides individual color parameters)
 * @param neuInsets Shadow insets
 * @param neuShape Shape type
 * @param lightShadowColor Light shadow color (used if colorScheme is null)
 * @param darkShadowColor Dark shadow color (used if colorScheme is null)
 * @param strokeWidth Stroke width
 * @param elevation Shadow elevation
 * @param lightSource Light source direction
 * @param role Semantic role of the element
 * @param enableScaleAnimation Whether to enable scale animation on press
 * @param pressedScale Scale factor when pressed (default 0.96)
 */
fun Modifier.expressiveNeumorphicClickable(
    onClick: () -> Unit,
    enabled: Boolean = true,
    colorScheme: NeuTheme.NeuColorScheme? = null,
    neuInsets: NeuInsets = NeuInsets(),
    neuShape: NeuShape = Punched.Rounded(),
    lightShadowColor: Color = Color.White,
    darkShadowColor: Color = Color.LightGray,
    strokeWidth: Dp = 6.dp,
    elevation: Dp = 6.dp,
    lightSource: LightSource = LightSource.TOP_LEFT,
    role: Role? = Role.Button,
    enableScaleAnimation: Boolean = true,
    pressedScale: Float = 0.96f
) = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    val effectiveLightShadowColor = colorScheme?.lightShadowColor ?: lightShadowColor
    val effectiveDarkShadowColor = colorScheme?.darkShadowColor ?: darkShadowColor
    
    val scale by animateFloatAsState(
        targetValue = when {
            isPressed && enableScaleAnimation -> pressedScale
            isHovered && enableScaleAnimation -> 1.02f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scaleAnimation"
    )
    
    this
        .scale(scale)
        .expressiveNeumorphic(
            neuInsets = neuInsets,
            neuShape = neuShape,
            lightShadowColor = effectiveLightShadowColor,
            darkShadowColor = effectiveDarkShadowColor,
            strokeWidth = strokeWidth,
            elevation = elevation,
            lightSource = lightSource,
            pressed = isPressed,
            hovered = isHovered
        )
        .hoverable(interactionSource = interactionSource)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            role = role,
            onClick = onClick
        )
}

/**
 * Apply neumorphic effect using theme colors
 * 
 * @param colorScheme The color scheme to use
 * @param neuInsets Shadow insets
 * @param neuShape Shape type
 * @param strokeWidth Stroke width
 * @param elevation Shadow elevation
 * @param lightSource Light source direction
 */
fun Modifier.themedNeumorphic(
    colorScheme: NeuTheme.NeuColorScheme,
    neuInsets: NeuInsets = NeuInsets(),
    neuShape: NeuShape = Punched.Rounded(),
    strokeWidth: Dp = 6.dp,
    elevation: Dp = 6.dp,
    lightSource: LightSource = LightSource.TOP_LEFT
) = this.neumorphic(
    neuInsets = neuInsets,
    neuShape = neuShape,
    lightShadowColor = colorScheme.lightShadowColor,
    darkShadowColor = colorScheme.darkShadowColor,
    strokeWidth = strokeWidth,
    elevation = elevation,
    lightSource = lightSource
)

/**
 * Apply expressive neumorphic effect using theme colors with spring animations
 * 
 * @param colorScheme The color scheme to use
 * @param neuInsets Shadow insets
 * @param neuShape Shape type
 * @param strokeWidth Stroke width
 * @param elevation Shadow elevation
 * @param lightSource Light source direction
 * @param pressed Whether the component is pressed
 * @param hovered Whether the component is hovered
 */
fun Modifier.themedExpressiveNeumorphic(
    colorScheme: NeuTheme.NeuColorScheme,
    neuInsets: NeuInsets = NeuInsets(),
    neuShape: NeuShape = Punched.Rounded(),
    strokeWidth: Dp = 6.dp,
    elevation: Dp = 6.dp,
    lightSource: LightSource = LightSource.TOP_LEFT,
    pressed: Boolean = false,
    hovered: Boolean = false
) = this.expressiveNeumorphic(
    neuInsets = neuInsets,
    neuShape = neuShape,
    lightShadowColor = colorScheme.lightShadowColor,
    darkShadowColor = colorScheme.darkShadowColor,
    strokeWidth = strokeWidth,
    elevation = elevation,
    lightSource = lightSource,
    pressed = pressed,
    hovered = hovered
)

/**
 * Apply soft neumorphic effect with reduced elevation
 */
fun Modifier.softNeumorphic(
    neuInsets: NeuInsets = NeuInsets(4.dp, 4.dp),
    neuShape: NeuShape = Punched.Rounded(8.dp),
    lightShadowColor: Color = Color.White.copy(alpha = 0.8f),
    darkShadowColor: Color = Color.LightGray.copy(alpha = 0.8f),
    lightSource: LightSource = LightSource.TOP_LEFT
) = this.neumorphic(
    neuInsets = neuInsets,
    neuShape = neuShape,
    lightShadowColor = lightShadowColor,
    darkShadowColor = darkShadowColor,
    strokeWidth = 4.dp,
    elevation = 4.dp,
    lightSource = lightSource
)

/**
 * Apply deep neumorphic effect with increased elevation
 */
fun Modifier.deepNeumorphic(
    neuInsets: NeuInsets = NeuInsets(10.dp, 10.dp),
    neuShape: NeuShape = Punched.Rounded(16.dp),
    lightShadowColor: Color = Color.White,
    darkShadowColor: Color = Color.Gray,
    lightSource: LightSource = LightSource.TOP_LEFT
) = this.neumorphic(
    neuInsets = neuInsets,
    neuShape = neuShape,
    lightShadowColor = lightShadowColor,
    darkShadowColor = darkShadowColor,
    strokeWidth = 10.dp,
    elevation = 10.dp,
    lightSource = lightSource
)

/**
 * Apply subtle neumorphic effect - very light, almost flat with slight depth
 */
fun Modifier.subtleNeumorphic(
    neuInsets: NeuInsets = NeuInsets(2.dp, 2.dp),
    neuShape: NeuShape = Punched.Rounded(4.dp),
    lightShadowColor: Color = Color.White.copy(alpha = 0.6f),
    darkShadowColor: Color = Color.LightGray.copy(alpha = 0.4f),
    lightSource: LightSource = LightSource.TOP_LEFT
) = this.neumorphic(
    neuInsets = neuInsets,
    neuShape = neuShape,
    lightShadowColor = lightShadowColor,
    darkShadowColor = darkShadowColor,
    strokeWidth = 2.dp,
    elevation = 2.dp,
    lightSource = lightSource
)

/**
 * Apply bold neumorphic effect - strong shadows for emphasis
 */
fun Modifier.boldNeumorphic(
    neuInsets: NeuInsets = NeuInsets(12.dp, 12.dp),
    neuShape: NeuShape = Punched.Rounded(20.dp),
    lightShadowColor: Color = Color.White,
    darkShadowColor: Color = Color.DarkGray.copy(alpha = 0.6f),
    lightSource: LightSource = LightSource.TOP_LEFT
) = this.neumorphic(
    neuInsets = neuInsets,
    neuShape = neuShape,
    lightShadowColor = lightShadowColor,
    darkShadowColor = darkShadowColor,
    strokeWidth = 12.dp,
    elevation = 14.dp,
    lightSource = lightSource
)
