package me.nikhilchaudhari.neumorphiccompose.macrobenchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.MacrobenchmarkRule
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.measureRepeated
import org.junit.Rule
import org.junit.Test

class StartupAndRenderingBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun coldStartup() {
        benchmarkRule.measureRepeated(
            packageName = "me.nikhilchaudhari.neumorphiccompose",
            metrics = listOf(StartupTimingMetric()),
            compilationMode = CompilationMode.Partial(),
            iterations = 5,
            startupMode = StartupMode.COLD,
            setupBlock = {
                pressHome()
            }
        ) {
            startActivityAndWait()
        }
    }

    @Test
    fun startupAndFirstFrames() {
        benchmarkRule.measureRepeated(
            packageName = "me.nikhilchaudhari.neumorphiccompose",
            metrics = listOf(FrameTimingMetric()),
            compilationMode = CompilationMode.Partial(),
            iterations = 5,
            startupMode = StartupMode.COLD,
            setupBlock = {
                pressHome()
            }
        ) {
            startActivityAndWait()
            device.waitForIdle()
        }
    }
}
