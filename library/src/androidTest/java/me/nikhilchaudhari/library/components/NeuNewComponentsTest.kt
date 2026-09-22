package me.nikhilchaudhari.library.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

class NeuNewComponentsTest {
    @get:Rule val compose = createComposeRule()
    @Test fun knobSetProgressClampsAndDisabledRejects() {
        var value by mutableFloatStateOf(0.5f)
        var enabled by mutableStateOf(true)
        compose.setContent { MaterialTheme { NeuRotaryKnob(value, { value = it }, enabled = enabled, label = "Gain") } }
        compose.onNodeWithContentDescription("Gain").performSemanticsAction(SemanticsActions.SetProgress) { it(2f) }
        compose.runOnIdle { assertEquals(1f, value); enabled = false }
        compose.onNodeWithContentDescription("Gain").assertIsNotEnabled()
        compose.onNodeWithContentDescription("Gain").performSemanticsAction(SemanticsActions.SetProgress) { assertFalse(it(0f)) }
        compose.runOnIdle { assertEquals(1f, value) }
    }
    @Test fun segmentedSelectionIsCallerOwned() {
        var selected by mutableIntStateOf(0)
        compose.setContent { MaterialTheme { NeuSegmentedButton(listOf("First", "Second"), selected, { selected = it }) } }
        compose.onNodeWithText("Second").performClick().assertIsSelected()
        compose.runOnIdle { assertEquals(1, selected) }
    }
    @Test fun textFieldEnforcesMaximumLength() {
        var value by mutableStateOf("")
        compose.setContent { MaterialTheme { NeuTextField(value, { value = it }, label = "Name", maxLength = 3) } }
        compose.onNodeWithContentDescription("Name").performTextInput("abcd")
        compose.runOnIdle { assertEquals("", value) }
        compose.onNodeWithContentDescription("Name").performTextInput("abc")
        compose.runOnIdle { assertEquals("abc", value) }
    }
}
