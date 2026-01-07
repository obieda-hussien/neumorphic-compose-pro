package me.nikhilchaudhari.library

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * Theme configuration for neumorphic components
 * Supports Material You dynamic colors on Android 12+ and Material 3 Expressive
 */
object NeuTheme {
    
    /**
     * Default neumorphic color scheme for light/dark modes
     * Compatible with Material 3 Expressive color tokens
     */
    @Stable
    data class NeuColorScheme(
        val backgroundColor: Color,
        val lightShadowColor: Color,
        val darkShadowColor: Color,
        val accentColor: Color = Color.Unspecified,
        val onBackgroundColor: Color = Color.Unspecified
    )

    /**
     * Light theme color scheme - Material 3 Expressive compatible
     */
    val LightColorScheme = NeuColorScheme(
        backgroundColor = Color(0xFFECEAEB),
        lightShadowColor = Color.White,
        darkShadowColor = Color(0xFFC8C8C8),
        accentColor = Color(0xFF6750A4), // M3 Primary
        onBackgroundColor = Color(0xFF1C1B1F)
    )

    /**
     * Dark theme color scheme - Material 3 Expressive compatible
     */
    val DarkColorScheme = NeuColorScheme(
        backgroundColor = Color(0xFF2D2D2D),
        lightShadowColor = Color(0xFF3D3D3D),
        darkShadowColor = Color(0xFF1D1D1D),
        accentColor = Color(0xFFD0BCFF), // M3 Primary Dark
        onBackgroundColor = Color(0xFFE6E1E5)
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
     * Create a custom color scheme with auto-generated shadow colors
     */
    fun customColorScheme(
        backgroundColor: Color,
        lightShadowColor: Color = backgroundColor.lighten(0.15f),
        darkShadowColor: Color = backgroundColor.darken(0.15f),
        accentColor: Color = Color.Unspecified,
        onBackgroundColor: Color = if (backgroundColor.luminance() > 0.5f) Color.Black else Color.White
    ) = NeuColorScheme(
        backgroundColor = backgroundColor,
        lightShadowColor = lightShadowColor,
        darkShadowColor = darkShadowColor,
        accentColor = accentColor,
        onBackgroundColor = onBackgroundColor
    )

    /**
     * Get Material You dynamic colors based color scheme (Android 12+)
     * Falls back to default scheme on older versions
     * Material 3 Expressive compatible
     */
    @Composable
    @ReadOnlyComposable
    fun dynamicColorScheme(): NeuColorScheme {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val m3ColorScheme = MaterialTheme.colorScheme
            NeuColorScheme(
                backgroundColor = m3ColorScheme.surface,
                lightShadowColor = m3ColorScheme.surfaceVariant.copy(alpha = 0.8f),
                darkShadowColor = m3ColorScheme.outline.copy(alpha = 0.3f),
                accentColor = m3ColorScheme.primary,
                onBackgroundColor = m3ColorScheme.onSurface
            )
        } else {
            colorScheme()
        }
    }

    /**
     * Create neumorphic color scheme from Material 3 ColorScheme
     * Full integration with Material 3 Expressive theming
     */
    @Composable
    @ReadOnlyComposable
    fun fromMaterial3ColorScheme(colorScheme: ColorScheme = MaterialTheme.colorScheme): NeuColorScheme {
        return NeuColorScheme(
            backgroundColor = colorScheme.surface,
            lightShadowColor = colorScheme.surfaceVariant.lighten(0.1f),
            darkShadowColor = colorScheme.surfaceDim.darken(0.1f),
            accentColor = colorScheme.primary,
            onBackgroundColor = colorScheme.onSurface
        )
    }

    /**
     * Create expressive neumorphic color scheme
     * Uses Material 3 Expressive color tokens for vibrant, playful appearance
     */
    @Composable
    @ReadOnlyComposable
    fun expressiveColorScheme(): NeuColorScheme {
        val isDark = isSystemInDarkTheme()
        val m3Colors = MaterialTheme.colorScheme
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Use dynamic colors with expressive adjustments
            NeuColorScheme(
                backgroundColor = m3Colors.surfaceContainer,
                lightShadowColor = m3Colors.surfaceContainerHighest.copy(alpha = 0.9f),
                darkShadowColor = m3Colors.surfaceContainerLowest.copy(alpha = 0.7f),
                accentColor = m3Colors.primary,
                onBackgroundColor = m3Colors.onSurface
            )
        } else {
            // Fallback expressive colors
            if (isDark) {
                NeuColorScheme(
                    backgroundColor = Color(0xFF1E1E2E),
                    lightShadowColor = Color(0xFF2E2E3E),
                    darkShadowColor = Color(0xFF0E0E1E),
                    accentColor = Color(0xFFCBA6F7), // Lavender
                    onBackgroundColor = Color(0xFFCDD6F4)
                )
            } else {
                NeuColorScheme(
                    backgroundColor = Color(0xFFEFF1F5),
                    lightShadowColor = Color(0xFFFFFFFF),
                    darkShadowColor = Color(0xFFBCC0CC),
                    accentColor = Color(0xFF8839EF), // Mauve
                    onBackgroundColor = Color(0xFF4C4F69)
                )
            }
        }
    }

    /**
     * Warm neumorphic color scheme - cozy, friendly appearance
     */
    val WarmColorScheme = NeuColorScheme(
        backgroundColor = Color(0xFFFAF0E6), // Linen
        lightShadowColor = Color(0xFFFFFFFF),
        darkShadowColor = Color(0xFFDCD0C0),
        accentColor = Color(0xFFFF8C42), // Mango Tango
        onBackgroundColor = Color(0xFF5D4E37)
    )

    /**
     * Cool neumorphic color scheme - calm, professional appearance
     */
    val CoolColorScheme = NeuColorScheme(
        backgroundColor = Color(0xFFE8EEF2), // Ice Blue
        lightShadowColor = Color(0xFFFFFFFF),
        darkShadowColor = Color(0xFFC0CDD6),
        accentColor = Color(0xFF0077B6), // Honolulu Blue
        onBackgroundColor = Color(0xFF2D3748)
    )

    /**
     * Nature neumorphic color scheme - organic, earthy appearance
     */
    val NatureColorScheme = NeuColorScheme(
        backgroundColor = Color(0xFFE8F0E8), // Mint Cream
        lightShadowColor = Color(0xFFF8FFF8),
        darkShadowColor = Color(0xFFB8C8B8),
        accentColor = Color(0xFF2D8659), // Sea Green
        onBackgroundColor = Color(0xFF2D3D2D)
    )
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

/**
 * Generate a full NeuColorScheme from a single background color
 */
fun Color.toNeuColorScheme(): NeuTheme.NeuColorScheme {
    val (light, dark) = this.toNeuColors()
    return NeuTheme.NeuColorScheme(
        backgroundColor = this,
        lightShadowColor = light,
        darkShadowColor = dark,
        onBackgroundColor = if (this.luminance() > 0.5f) Color.Black else Color.White
    )
}

/**
 * Adjust color saturation
 * Uses grayscale value (calculated via luminance formula) as the neutral point
 */
fun Color.adjustSaturation(factor: Float): Color {
    val grayscaleValue = 0.299f * red + 0.587f * green + 0.114f * blue
    return Color(
        red = (grayscaleValue + (red - grayscaleValue) * factor).coerceIn(0f, 1f),
        green = (grayscaleValue + (green - grayscaleValue) * factor).coerceIn(0f, 1f),
        blue = (grayscaleValue + (blue - grayscaleValue) * factor).coerceIn(0f, 1f),
        alpha = alpha
    )
}
