package me.nikhilchaudhari.library.views

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import me.nikhilchaudhari.library.internal.NeuBlurMakerHolder
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifies that NeumorphicView, NeumorphicButton, and NeumorphicCardView all
 * end up sharing the single app-wide BlurMaker (see NeuBlurMakerHolder in the
 * :library module) rather than each constructing its own RenderScript
 * context - this is what the perf fix for slow/heavy screens with many
 * neumorphic Views relies on.
 */
@RunWith(AndroidJUnit4::class)
class NeumorphicViewsSharedBlurMakerTest {

    @Test
    fun allThreeViewsShareTheSameBlurMakerInstance() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        val view = NeumorphicView(context)
        val button = NeumorphicButton(context)
        val cardView = NeumorphicCardView(context)

        val sharedFromHolder = NeuBlurMakerHolder.get(context)

        assertSame(sharedFromHolder, view.blurMakerForTest())
        assertSame(sharedFromHolder, button.blurMakerForTest())
        assertSame(sharedFromHolder, cardView.blurMakerForTest())
    }
}
