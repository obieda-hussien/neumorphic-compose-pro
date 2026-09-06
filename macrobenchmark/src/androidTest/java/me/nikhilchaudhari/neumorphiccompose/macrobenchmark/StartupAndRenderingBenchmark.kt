package me.nikhilchaudhari.neumorphiccompose.macrobenchmark

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.MacrobenchmarkRule
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.measureRepeated
import org.junit.Rule
import org.junit.Test

class StartupAndRenderingBenchmark {

    private val compilationMode = CompilationMode.Partial(
        baselineProfileMode = BaselineProfileMode.Require
    )

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun coldStartup() {
        benchmarkRule.measureRepeated(
            packageName = "me.nikhilchaudhari.neumorphiccompose",
            metrics = listOf(StartupTimingMetric()),
            compilationMode = compilationMode,
            iterations = 5,
            startupMode = StartupMode.COLD,
            setupBlock = { pressHome() }
        ) {
            startActivityAndWait()
        }
    }

    @Test
    fun startupAndFirstFrames() {
        benchmarkRule.measureRepeated(
            packageName = "me.nikhilchaudhari.neumorphiccompose",
            metrics = listOf(FrameTimingMetric()),
            compilationMode = compilationMode,
            iterations = 5,
            startupMode = StartupMode.COLD,
            setupBlock = { pressHome() }
        ) {
            startActivityAndWait()
            device.waitForIdle()
        }
    }

    @Test
    fun componentHeavyScrolling() {
        benchmarkRule.measureRepeated(
            packageName = "me.nikhilchaudhari.neumorphiccompose",
            metrics = listOf(FrameTimingMetric()),
            compilationMode = compilationMode,
            iterations = 5,
            startupMode = StartupMode.COLD,
            setupBlock = { pressHome() }
        ) {
            startActivityAndWait()
            device.waitForIdle()

            val centerX = device.displayWidth / 2
            val top = (device.displayHeight * 0.2f).toInt()
            val bottom = (device.displayHeight * 0.82f).toInt()

            repeat(3) {
                device.swipe(centerX, bottom, centerX, top, 900)
            }
            device.waitForIdle()
        }
    }
}
