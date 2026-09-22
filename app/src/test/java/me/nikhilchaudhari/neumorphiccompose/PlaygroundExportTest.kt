package me.nikhilchaudhari.neumorphiccompose

import me.nikhilchaudhari.library.LightSource
import org.junit.Assert.*
import org.junit.Test

class PlaygroundExportTest {
    @Test fun exportUsesExactSelectedGeometryAndPalette() {
        val code = playgroundCode(12, 28, LightSource.BOTTOM_RIGHT, true, false, true, true, true)
        assertTrue(code.contains("elevation = 12.dp"))
        assertTrue(code.contains("cornerRadius = 28.dp"))
        assertTrue(code.contains("LightSource.BOTTOM_RIGHT"))
        assertTrue(code.contains("Color(0xFF292836)"))
        assertTrue(code.contains("recessed = true"))
        assertTrue(code.contains("highContrast = true, reduceMotion = true"))
    }
}
