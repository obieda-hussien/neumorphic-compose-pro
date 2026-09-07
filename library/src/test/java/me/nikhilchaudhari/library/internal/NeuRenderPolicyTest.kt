package me.nikhilchaudhari.library.internal

import me.nikhilchaudhari.library.NeuPerformanceClass
import me.nikhilchaudhari.library.NeuPerformanceConfig
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NeuRenderPolicyTest {

    @Before
    fun resetConfig() {
        NeuPerformanceConfig.performanceClass = NeuPerformanceClass.BALANCED
        NeuPerformanceConfig.blurDownsampling = 2
        NeuPerformanceConfig.adaptiveBlurEnabled = true
        NeuPerformanceConfig.blurWorkBudget = 180_000L
        NeuPerformanceConfig.thermalAwareRendering = false
        NeuPerformanceConfig.batteryAwareRendering = false
        NeuPerformanceConfig.qualityHysteresisMs = 400L
    }

    @After
    fun cleanup() = resetConfig()

    @Test fun `small shadows keep configured minimum quality`() {
        assertEquals(1, NeuRenderPolicy.effectiveBlurSampling(64, 48, 6, 1))
    }

    @Test fun `medium shadows only increase sampling when budget requires it`() {
        assertEquals(2, NeuRenderPolicy.effectiveBlurSampling(300, 300, 10, 1))
    }

    @Test fun `large shadows increase sampling to bound work`() {
        val sampling = NeuRenderPolicy.effectiveBlurSampling(1200, 800, 25, 1)
        assertTrue(sampling > 1)
        assertTrue(sampling <= NeuRenderPolicy.MAX_AUTO_SAMPLING)
    }

    @Test fun `configured sampling remains a lower bound`() {
        val sampling = NeuRenderPolicy.effectiveBlurSampling(100, 100, 8, 3)
        assertTrue(sampling >= 3)
    }

    @Test fun `explicit sampling above automatic ceiling is preserved`() {
        assertEquals(6, NeuRenderPolicy.effectiveBlurSampling(100, 100, 8, 6))
    }

    @Test fun `invalid inputs are clamped instead of crashing`() {
        assertEquals(1, NeuRenderPolicy.effectiveBlurSampling(0, -10, 0, 0, 0))
    }

    @Test fun `battery class raises minimum sampling floor`() {
        NeuPerformanceConfig.performanceClass = NeuPerformanceClass.BATTERY
        val floor = NeuRenderPolicy.effectiveMinimumSampling(1)
        assertTrue(floor >= 2)
    }

    @Test fun `quality class expands effective budget versus battery`() {
        NeuPerformanceConfig.performanceClass = NeuPerformanceClass.QUALITY
        val qualityBudget = NeuRenderPolicy.effectiveWorkBudget()
        NeuPerformanceConfig.performanceClass = NeuPerformanceClass.BATTERY
        val batteryBudget = NeuRenderPolicy.effectiveWorkBudget()
        assertTrue(qualityBudget > batteryBudget)
    }
}
