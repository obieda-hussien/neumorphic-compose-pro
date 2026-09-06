package me.nikhilchaudhari.library

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test

class NeuPerformanceConfigTest {

    @Before
    fun resetToDefaults() {
        NeuPerformanceConfig.blurDownsampling = 2
        NeuPerformanceConfig.adaptiveBlurEnabled = true
        NeuPerformanceConfig.blurWorkBudget = 180_000L
        NeuPerformanceConfig.shadowCacheBudgetKB = 6 * 1024
    }

    @After
    fun restoreDefaults() {
        NeuPerformanceConfig.blurDownsampling = 2
        NeuPerformanceConfig.adaptiveBlurEnabled = true
        NeuPerformanceConfig.blurWorkBudget = 180_000L
        NeuPerformanceConfig.shadowCacheBudgetKB = 6 * 1024
    }

    @Test
    fun `defaults match documented library defaults`() {
        assertEquals(2, NeuPerformanceConfig.blurDownsampling)
        assertTrue(NeuPerformanceConfig.adaptiveBlurEnabled)
        assertEquals(180_000L, NeuPerformanceConfig.blurWorkBudget)
        assertEquals(6 * 1024, NeuPerformanceConfig.shadowCacheBudgetKB)
    }

    @Test
    fun `blurDownsampling accepts a custom value`() {
        NeuPerformanceConfig.blurDownsampling = 3
        assertEquals(3, NeuPerformanceConfig.blurDownsampling)
    }

    @Test
    fun `blurDownsampling rejects values below 1`() {
        assertThrows(IllegalArgumentException::class.java) {
            NeuPerformanceConfig.blurDownsampling = 0
        }
    }

    @Test
    fun `adaptive blur can be disabled`() {
        NeuPerformanceConfig.adaptiveBlurEnabled = false
        assertEquals(false, NeuPerformanceConfig.adaptiveBlurEnabled)
    }

    @Test
    fun `blur work budget rejects values below 1`() {
        assertThrows(IllegalArgumentException::class.java) {
            NeuPerformanceConfig.blurWorkBudget = 0L
        }
        assertThrows(IllegalArgumentException::class.java) {
            NeuPerformanceConfig.blurWorkBudget = -1L
        }
    }

    @Test
    fun `shadowCacheBudgetKB rejects values below 1`() {
        assertThrows(IllegalArgumentException::class.java) {
            NeuPerformanceConfig.shadowCacheBudgetKB = 0
        }
        assertThrows(IllegalArgumentException::class.java) {
            NeuPerformanceConfig.shadowCacheBudgetKB = -1
        }
    }
}
