package me.nikhilchaudhari.neumorphiccompose

import android.view.FrameMetrics
import android.view.Window

/** A bounded sample of actual TOTAL_DURATION values, in milliseconds; not a device benchmark verdict. */
object DemoFrameStats : Window.OnFrameMetricsAvailableListener {
    private val samples = ArrayDeque<Double>()
    @Synchronized override fun onFrameMetricsAvailable(window: Window, metrics: FrameMetrics, dropped: Int) {
        val duration = metrics.getMetric(FrameMetrics.TOTAL_DURATION)
        if (duration > 0) {
            if (samples.size == 600) samples.removeFirst()
            samples.addLast(duration / 1_000_000.0)
        }
    }
    @Synchronized fun snapshot(): Triple<Double, Double, Int> {
        val sorted = samples.sorted()
        fun percentile(p: Double) = if (sorted.isEmpty()) 0.0 else sorted[((sorted.size - 1) * p).toInt()]
        return Triple(percentile(0.95), percentile(0.99), sorted.size)
    }
}
