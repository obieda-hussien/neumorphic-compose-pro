package me.nikhilchaudhari.library

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

/**
 * Theme configuration for neumorphic components
 * Supports Material You dynamic colors on Android 12+
 */
object NeuTheme {
    
    /**
     * Default neumorphic color scheme for light mode
     */
    data class NeuColorScheme(
        val backgroundColor: Color,
        val lightShadowColor: Color,
        val darkShadowColor: Color
    )

    /**
     * Light theme color scheme
     */
    val LightColorScheme = NeuColorScheme(
        backgroundColor = Color(0xFFECEAEB),
        lightShadowColor = Color.White,
        darkShadowColor = Color(0xFFC8C8C8)
    )

    /**
     * Dark theme color scheme
     */
    val DarkColorScheme = NeuColorScheme(
        backgroundColor = Color(0xFF2D2D2D),
        lightShadowColor = Color(0xFF3D3D3D),
        darkShadowColor = Color(0xFF1D1D1D)
    )

    /**
     * Get the appropriate color scheme based on system theme
     */
    @Composable
    @ReadOnlyComposable
    fun colorScheme(): NeuColorScheme {
        return if (isSystemInDarkTheme()) DarkColorScheme else LightColorScheme
    }

    /**
     * Create a custom color scheme
     */
    fun customColorScheme(
        backgroundColor: Color,
        lightShadowColor: Color = backgroundColor.lighten(0.15f),
        darkShadowColor: Color = backgroundColor.darken(0.15f)
    ) = NeuColorScheme(
        backgroundColor = backgroundColor,
        lightShadowColor = lightShadowColor,
        darkShadowColor = darkShadowColor
    )

    /**
     * Get Material You dynamic colors based color scheme (Android 12+)
     * Falls back to default scheme on older versions
     */
    @Composable
    @ReadOnlyComposable
    fun dynamicColorScheme(): NeuColorScheme {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val surface = MaterialTheme.colorScheme.surface
            NeuColorScheme(
                backgroundColor = surface,
                lightShadowColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                darkShadowColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        } else {
            colorScheme()
        }
    }
}

/**
 * Lighten a color by a percentage
 */
fun Color.lighten(factor: Float): Color {
    return Color(
        red = (red + (1f - red) * factor).coerceIn(0f, 1f),
        green = (green + (1f - green) * factor).coerceIn(0f, 1f),
        blue = (blue + (1f - blue) * factor).coerceIn(0f, 1f),
        alpha = alpha
    )
}

/**
 * Darken a color by a percentage
 */
fun Color.darken(factor: Float): Color {
    return Color(
        red = (red * (1f - factor)).coerceIn(0f, 1f),
        green = (green * (1f - factor)).coerceIn(0f, 1f),
        blue = (blue * (1f - factor)).coerceIn(0f, 1f),
        alpha = alpha
    )
}

/**
 * Generate neumorphic shadow colors from a background color
 */
fun Color.toNeuColors(): Pair<Color, Color> {
    return Pair(
        this.lighten(0.2f),
        this.darken(0.15f)
    )
}
