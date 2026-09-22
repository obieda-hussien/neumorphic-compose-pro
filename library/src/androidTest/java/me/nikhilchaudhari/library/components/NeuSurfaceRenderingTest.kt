package me.nikhilchaudhari.library.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import me.nikhilchaudhari.library.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class NeuSurfaceRenderingTest {
    @get:Rule val compose = createComposeRule()
    @Test fun customSurfaceBecomesReadyWithoutBlockingContent() {
        compose.setContent {
            MaterialTheme {
                NeuDesignTheme(NeuTheme.customColorScheme(Color(0xFFE8E7F5))) {
                    NeuSurface(Modifier.size(180.dp).testTag("surface"),
                        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 4.dp, bottomEnd = 24.dp, bottomStart = 0.dp)) {
                        Text("Ready content", Modifier.padding(24.dp))
                    }
                }
            }
        }
        compose.onNodeWithText("Ready content").assertIsDisplayed()
        compose.waitUntil(10_000) { NeuPerfStats.snapshot().pendingRequests == 0 && NeuPerfStats.snapshot().cacheHits > 0 }
        val image = compose.onNodeWithTag("surface").captureToImage()
        assertTrue(image.width > 0 && image.height > 0)
        assertEquals(0, NeuPerfStats.snapshot().pendingRequests)
    }
}
