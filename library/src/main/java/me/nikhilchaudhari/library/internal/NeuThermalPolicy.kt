package me.nikhilchaudhari.library.internal

import android.content.Context
import android.os.Build
import android.os.PowerManager
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.roundToLong

/**
 * Tiny process-wide thermal signal used to trade visual quality for sustained
 * frame rate when Android reports thermal pressure.
 *
 * This is deliberately a coarse policy. It never changes layout or interaction
 * behavior, only the maximum blur work allowed for newly generated shadows.
 */
internal object NeuThermalPolicy {
    private const val TIER_NORMAL = 0
    private const val TIER_LIGHT = 1
    private const val TIER_MODERATE = 2
    private const val TIER_SEVERE = 3
    private const val TIER_CRITICAL = 4

    private val registered = AtomicBoolean(false)
    private val tier = AtomicInteger(TIER_NORMAL)

    fun register(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
        if (!registered.compareAndSet(false, true)) return

        val appContext = context.applicationContext ?: context
        val powerManager = appContext.getSystemService(PowerManager::class.java) ?: return
        tier.set(tierFor(powerManager.currentThermalStatus))

        powerManager.addThermalStatusListener(appContext.mainExecutor) { status ->
            tier.set(tierFor(status))
        }
    }

    fun cacheTier(): Int = tier.get()

    fun effectiveWorkBudget(baseBudget: Long): Long {
        val safe = baseBudget.coerceAtLeast(1L)
        val multiplier = when (tier.get()) {
            TIER_LIGHT -> 0.85
            TIER_MODERATE -> 0.70
            TIER_SEVERE -> 0.50
            TIER_CRITICAL -> 0.30
            else -> 1.0
        }
        return (safe.toDouble() * multiplier).roundToLong().coerceAtLeast(1L)
    }

    private fun tierFor(status: Int): Int {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return TIER_NORMAL
        return when (status) {
            PowerManager.THERMAL_STATUS_LIGHT -> TIER_LIGHT
            PowerManager.THERMAL_STATUS_MODERATE -> TIER_MODERATE
            PowerManager.THERMAL_STATUS_SEVERE -> TIER_SEVERE
            PowerManager.THERMAL_STATUS_CRITICAL,
            PowerManager.THERMAL_STATUS_EMERGENCY,
            PowerManager.THERMAL_STATUS_SHUTDOWN -> TIER_CRITICAL
            else -> TIER_NORMAL
        }
    }
}
