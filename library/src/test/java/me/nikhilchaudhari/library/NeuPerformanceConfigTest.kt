package me.nikhilchaudhari.library

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

class NeuPerformanceConfigTest {

    @Before
    @After
    fun reset() {
        NeuPerformanceConfig.performanceClass = NeuPerformanceClass.AUTO
        NeuPerformanceConfig.blurDownsampling = 2
        NeuPerformanceConfig.adaptiveBlurEnabled = true
        NeuPerformanceConfig.blurWorkBudget = 180_000L
        NeuPerformanceConfig.thermalAwareRendering = true
        NeuPerformanceConfig.batteryAwareRendering = true
        NeuPerformanceConfig.qualityHysteresisMs = 400L
        NeuPerformanceConfig.shadowCacheBudgetKB = 6 * 1024
    }

    @Test fun `rejects non positive downsampling`() {
        assertThrows(IllegalArgumentException::class.java) {
            NeuPerformanceConfig.blurDownsampling = 0
        }
    }

    @Test fun `rejects non positive work budget`() {
        assertThrows(IllegalArgumentException::class.java) {
            NeuPerformanceConfig.blurWorkBudget = 0L
        }
    }

    @Test fun `rejects negative hysteresis`() {
        assertThrows(IllegalArgumentException::class.java) {
            NeuPerformanceConfig.qualityHysteresisMs = -1L
        }
    }

    @Test fun `accepts performance class changes`() {
        NeuPerformanceConfig.performanceClass = NeuPerformanceClass.QUALITY
        assertEquals(NeuPerformanceClass.QUALITY, NeuPerformanceConfig.performanceClass)
    }
}
