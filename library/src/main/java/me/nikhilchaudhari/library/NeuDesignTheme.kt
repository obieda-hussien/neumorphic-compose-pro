package me.nikhilchaudhari.library

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class NeuDesignTokens(
    val elevation: Dp = 6.dp,
    val cornerRadius: Dp = 16.dp,
    val lightSource: LightSource = LightSource.TOP_LEFT,
    val highContrast: Boolean = false,
    val reduceMotion: Boolean = false
)
val LocalNeuTokens = staticCompositionLocalOf { NeuDesignTokens() }
internal val LocalNeuColors = staticCompositionLocalOf<NeuTheme.NeuColorScheme?> { null }

/** Scoped configuration; nested themes do not mutate process-wide rendering policy. */
@Composable
fun NeuDesignTheme(
    colors: NeuTheme.NeuColorScheme = NeuTheme.colorScheme(),
    tokens: NeuDesignTokens = NeuDesignTokens(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalNeuColors provides colors, LocalNeuTokens provides tokens, content = content)
}

/** Select the higher-contrast black/white foreground using relative luminance. */
fun neuContentColor(background: Color): Color =
    if (background.luminance() > 0.179f) Color.Black else Color.White
