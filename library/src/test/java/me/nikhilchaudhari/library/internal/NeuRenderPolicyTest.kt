package me.nikhilchaudhari.library.internal

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NeuRenderPolicyTest {

    @Test
    fun `small shadows keep configured minimum quality`() {
        assertEquals(
            1,
            NeuRenderPolicy.effectiveBlurSampling(
                width = 64,
                height = 48,
                radius = 6,
                configuredSampling = 1
            )
        )
    }

    @Test
    fun `large shadows increase sampling to bound work`() {
        val sampling = NeuRenderPolicy.effectiveBlurSampling(
            width = 1200,
            height = 800,
            radius = 25,
            configuredSampling = 1
        )
        assertTrue(sampling > 1)
        assertTrue(sampling <= NeuRenderPolicy.MAX_SAMPLING)
    }

    @Test
    fun `configured sampling remains a lower bound`() {
        val sampling = NeuRenderPolicy.effectiveBlurSampling(
            width = 100,
            height = 100,
            radius = 8,
            configuredSampling = 3
        )
        assertTrue(sampling >= 3)
    }

    @Test
    fun `invalid inputs are clamped instead of crashing`() {
        val sampling = NeuRenderPolicy.effectiveBlurSampling(
            width = 0,
            height = -10,
            radius = 0,
            configuredSampling = 0,
            workBudget = 0
        )
        assertEquals(1, sampling)
    }
}
