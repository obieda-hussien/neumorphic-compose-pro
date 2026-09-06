package me.nikhilchaudhari.neumorphiccompose.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Generates a real baseline profile from the demo app's critical UI journey.
 * Keep this journey representative of the library's hot rendering paths:
 * startup, first frame, scrolling through many neumorphic components, and
 * repeated return to the top where cached shadows are reused.
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() {
        baselineProfileRule.collect(
            packageName = "me.nikhilchaudhari.neumorphiccompose",
            stableIterations = 3,
            maxIterations = 8
        ) {
            startActivityAndWait()
            device.waitForIdle()

            val width = device.displayWidth
            val height = device.displayHeight
            val centerX = width / 2
            val bottom = (height * 0.82f).toInt()
            val top = (height * 0.20f).toInt()

            repeat(3) {
                device.swipe(centerX, bottom, centerX, top, 12)
                device.waitForIdle()
            }

            repeat(3) {
                device.swipe(centerX, top, centerX, bottom, 12)
                device.waitForIdle()
            }
        }
    }
}
