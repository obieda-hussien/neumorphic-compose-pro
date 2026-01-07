package me.nikhilchaudhari.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
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
