package me.nikhilchaudhari.library

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

class NeuPerformanceConfigTest {

    @Before
    fun resetToDefaults() {
        // These are process-wide mutable settings, so tests must not leak
        // state into each other or into the app under test.
        NeuPerformanceConfig.blurDownsampling = 2
        NeuPerformanceConfig.shadowCacheBudgetKB = 6 * 1024
    }

    @Test
    fun `defaults match documented library defaults`() {
        assertEquals(2, NeuPerformanceConfig.blurDownsampling)
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
    fun `shadowCacheBudgetKB rejects negative values`() {
        assertThrows(IllegalArgumentException::class.java) {
            NeuPerformanceConfig.shadowCacheBudgetKB = -1
        }
    }
}
