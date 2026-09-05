package me.nikhilchaudhari.library.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelectable
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertIsToggleable
import androidx.compose.ui.test.assertRangeInfoEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.math.abs
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NeuComponentsInteractionTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun switchExposesCheckedSemanticsAndToggles() {
        var checked by mutableStateOf(false)

        composeRule.setContent {
            MaterialTheme {
                NeuSwitch(
                    checked = checked,
                    onCheckedChange = { checked = it },
                    modifier = Modifier.testTag("switch")
                )
            }
        }

        onNodeWithTag("switch")
            .assertIsToggleable()
            .performClick()

        composeRule.runOnIdle {
            assertTrue(checked)
        }
        onNodeWithTag("switch").assertIsOn()
    }

    @Test
    fun checkboxExposesCheckedSemanticsAndToggles() {
        var checked by mutableStateOf(false)

        composeRule.setContent {
            MaterialTheme {
                NeuCheckbox(
                    checked = checked,
                    onCheckedChange = { checked = it },
                    modifier = Modifier.testTag("checkbox")
                )
            }
        }

        onNodeWithTag("checkbox")
            .assertIsToggleable()
            .performClick()

        composeRule.runOnIdle {
            assertTrue(checked)
        }
        onNodeWithTag("checkbox").assertIsOn()
    }

    @Test
    fun radioButtonExposesSelectedSemanticsAndSelects() {
        var selected by mutableStateOf(false)

        composeRule.setContent {
            MaterialTheme {
                NeuRadioButton(
                    selected = selected,
                    onClick = { selected = true },
                    modifier = Modifier.testTag("radio")
                )
            }
        }

        onNodeWithTag("radio")
            .assertIsSelectable()
            .performClick()

        composeRule.runOnIdle {
            assertTrue(selected)
        }
        onNodeWithTag("radio").assertIsSelected()
    }

    @Test
    fun sliderTapUpdatesValueInsideRange() {
        var value by mutableStateOf(0f)

        composeRule.setContent {
            MaterialTheme {
                NeuSlider(
                    value = value,
                    onValueChange = { value = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("slider")
                )
            }
        }

        onNodeWithTag("slider")
            .assertRangeInfoEquals(ProgressBarRangeInfo(0f, 0f..1f))
            .performTouchInput {
                click(centerRight)
            }

        composeRule.runOnIdle {
            assertTrue(value > 0.8f)
            assertTrue(value <= 1f)
        }
    }

    @Test
    fun sliderDragUpdatesValueInsideRange() {
        var value by mutableStateOf(0f)

        composeRule.setContent {
            MaterialTheme {
                NeuSlider(
                    value = value,
                    onValueChange = { value = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("slider-drag")
                )
            }
        }

        onNodeWithTag("slider-drag").performTouchInput {
            swipeRight()
        }

        composeRule.runOnIdle {
            assertTrue(value in 0f..1f)
            assertTrue(abs(value) > 0.01f)
        }
    }

    @Test
    fun disabledSliderDoesNotChangeValue() {
        var value by mutableStateOf(0.4f)

        composeRule.setContent {
            MaterialTheme {
                NeuSlider(
                    value = value,
                    onValueChange = { value = it },
                    enabled = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("slider-disabled")
                )
            }
        }

        onNodeWithTag("slider-disabled").performTouchInput {
            click(centerRight)
        }

        composeRule.runOnIdle {
            assertTrue(abs(value - 0.4f) < 0.0001f)
        }
    }
}
