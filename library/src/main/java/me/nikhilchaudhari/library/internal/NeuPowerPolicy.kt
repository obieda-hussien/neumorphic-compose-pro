package me.nikhilchaudhari.library.internal

import android.content.Context
import android.os.PowerManager
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * Process-wide power-save signal. Reduces blur work when the user or system
 * has enabled battery saver, without changing layout or interaction.
 */
internal object NeuPowerPolicy {
    private const val TIER_NORMAL = 0
    private const val TIER_POWER_SAVE = 1

    private val registered = AtomicBoolean(false)
    private val tier = AtomicInteger(TIER_NORMAL)

    fun register(context: Context) {
        if (!registered.compareAndSet(false, true)) return
        val appContext = context.applicationContext ?: context
        val powerManager = appContext.getSystemService(PowerManager::class.java) ?: return
        refresh(powerManager)
    }

    fun refresh(powerManager: PowerManager? = null) {
        val pm = powerManager
        if (pm != null) {
            tier.set(if (pm.isPowerSaveMode) TIER_POWER_SAVE else TIER_NORMAL)
            return
        }
        // Keep last known value when no manager is supplied.
    }

    fun cacheTier(): Int = tier.get()

    fun isPowerSave(): Boolean = tier.get() == TIER_POWER_SAVE

    fun effectiveWorkBudget(baseBudget: Long): Long {
        val safe = baseBudget.coerceAtLeast(1L)
        return if (isPowerSave()) (safe * 55L) / 100L else safe
    }

    fun minimumSamplingFloor(): Int = if (isPowerSave()) 3 else 1
}
