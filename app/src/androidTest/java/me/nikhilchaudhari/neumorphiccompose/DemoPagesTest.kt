package me.nikhilchaudhari.neumorphiccompose

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test

/** Navigate the real activity: catches layout, scroll, dialog and saveable-state regressions. */
class DemoPagesTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun playgroundControlsPersistAcrossTabsAndExport() {
        compose.onNodeWithTag("tab-1").performClick()
        compose.onNodeWithTag("playground-list").performScrollToKey("depth")
        compose.onNodeWithTag("depth-slider").performSemanticsAction(SemanticsActions.SetProgress) { it(12f) }
        compose.onNodeWithText("Depth 12 dp").assertIsDisplayed()
        compose.onNodeWithTag("playground-list").performScrollToKey("options")
        compose.onNodeWithTag("playground-dark").performClick().assertIsOn()
        compose.onNodeWithTag("tab-2").performClick()
        compose.onNodeWithTag("tab-1").performClick()
        compose.onNodeWithTag("playground-dark").assertIsOn()
        compose.onNodeWithTag("playground-list").performScrollToKey("depth")
        compose.onNodeWithText("Depth 12 dp").assertIsDisplayed()
        compose.onNodeWithTag("playground-list").performScrollToKey("export")
        compose.onNodeWithTag("copy-kotlin").performClick()
        compose.onNodeWithText("Copied Kotlin").assertIsDisplayed()
    }

    @Test fun playgroundDialogAndSheetOpenAndDismiss() {
        compose.onNodeWithTag("tab-1").performClick()
        compose.onNodeWithTag("playground-list").performScrollToKey("overlays")
        compose.onNodeWithTag("open-dialog").performClick()
        compose.onNodeWithTag("close-dialog").assertIsDisplayed().performClick()
        compose.onNodeWithTag("close-dialog").assertDoesNotExist()
        compose.onNodeWithTag("open-sheet").performClick()
        compose.onNodeWithTag("close-sheet").assertIsDisplayed().performClick()
        compose.onNodeWithTag("close-sheet").assertDoesNotExist()
    }

    @Test fun stressListScrollsToEndAndRestoresPositionAcrossTabs() {
        compose.onNodeWithTag("tab-2").performClick()
        compose.onNodeWithTag("stress-list").performScrollToKey("surface-299")
        compose.onNodeWithTag("stress-card-299").assertIsDisplayed()
        compose.onNodeWithTag("tab-1").performClick()
        compose.onNodeWithTag("tab-2").performClick()
        compose.onNodeWithTag("stress-card-299").assertIsDisplayed()
        compose.onNodeWithTag("stress-list").performScrollToKey("controls")
        compose.onNodeWithTag("stress-dark").performClick().assertIsOn()
    }

    @Test fun stressAnimationCanBeStopped() {
        compose.onNodeWithTag("tab-2").performClick()
        compose.onNodeWithTag("stress-animation").performClick().assertIsOn()
        compose.mainClock.autoAdvance = false
        compose.mainClock.advanceTimeBy(1200)
        compose.onNodeWithTag("stress-animation").performClick()
        compose.mainClock.autoAdvance = true
        compose.onNodeWithTag("stress-animation").assertIsOff()
        compose.onNodeWithTag("stress-list").performScrollToKey("surface-25")
        compose.onNodeWithTag("stress-card-25").assertIsDisplayed()
    }
}
