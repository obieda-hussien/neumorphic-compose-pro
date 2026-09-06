package me.nikhilchaudhari.library.internal

import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.DisplayMetrics
import java.lang.ref.WeakReference
import kotlin.math.roundToInt
import me.nikhilchaudhari.library.NeuPerformanceConfig

data class BlurConfig(
    val width: Int,
    val height: Int,
    val radius: Int = MAX_RADIUS,
    val sampling: Int = DEFAULT_SAMPLING,
    val color: Int = Color.TRANSPARENT
) {
    companion object {
        const val MAX_RADIUS = 25
        const val DEFAULT_SAMPLING = 2
    }
}

/** Blur implementation shared by Compose and XML/View adapters. */
class BlurMaker(context: Context, private val defaultBlurRadius: Int) {

    private val stateLock = Any()
    private val contextRef = WeakReference(context.applicationContext ?: context)
    private var released = false
    private val blurEngine: BlurEngine = synchronized(stateLock) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            StackBlurEngine()
        } else {
            RenderScriptBlurEngine(contextRef.get() ?: context, stateLock)
        }
    }

    private val workingBitmapPool = mutableMapOf<Long, Bitmap>()

    private fun sizeKey(width: Int, height: Int): Long =
        (width.toLong() shl 32) or (height.toLong() and 0xFFFFFFFFL)

    private fun obtainWorkingBitmap(width: Int, height: Int): Bitmap = synchronized(stateLock) {
        val pooled = workingBitmapPool.remove(sizeKey(width, height))
        if (pooled != null && !pooled.isRecycled) {
            pooled.eraseColor(android.graphics.Color.TRANSPARENT)
            pooled
        } else {
            Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        }
    }

    private fun releaseWorkingBitmap(bitmap: Bitmap) {
        synchronized(stateLock) {
            if (bitmap.isRecycled) return
            if (released || workingBitmapPool.size >= MAX_POOLED_BITMAPS) {
                bitmap.recycle()
            } else {
                workingBitmapPool[sizeKey(bitmap.width, bitmap.height)]?.let {
                    if (!it.isRecycled) it.recycle()
                }
                workingBitmapPool[sizeKey(bitmap.width, bitmap.height)] = bitmap
            }
        }
    }

    /** Eagerly initializes the selected blur backend when it has persistent state. */
    fun warmUp() {
        synchronized(stateLock) {
            if (released) return
        }
        blurEngine.warmUp()
    }

    fun blur(
        source: Bitmap,
        radius: Int = defaultBlurRadius,
        sampling: Int = NeuPerformanceConfig.blurDownsampling
    ): Bitmap? {
        synchronized(stateLock) {
            if (released) return null
        }
        if (source.isRecycled || source.width <= 0 || source.height <= 0) return null

        val blurConfig = BlurConfig(
            width = source.width,
            height = source.height,
            radius = radius,
            sampling = sampling
        )
        return blur(source, blurConfig)
    }

    private fun blur(source: Bitmap, blurConfig: BlurConfig): Bitmap? {
        // Adaptive quality keeps the amount of work bounded as component size
        // or blur radius grows, while preserving the caller's configured
        // sampling value as the minimum quality level.
        val sampling = NeuRenderPolicy.effectiveBlurSampling(
            width = blurConfig.width,
            height = blurConfig.height,
            radius = blurConfig.radius,
            configuredSampling = blurConfig.sampling
        )

        // Use ceil division so the final source row/column are never clipped
        // when source dimensions are not exact multiples of the sampling factor.
        val width = ((blurConfig.width + sampling - 1) / sampling).coerceAtLeast(1)
        val height = ((blurConfig.height + sampling - 1) / sampling).coerceAtLeast(1)
        val bitmap = obtainWorkingBitmap(width, height)

        Canvas(bitmap).run {
            scale(1 / sampling.toFloat(), 1 / sampling.toFloat())
            val paint = Paint().apply {
                flags = Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG
                colorFilter = PorterDuffColorFilter(blurConfig.color, PorterDuff.Mode.SRC_ATOP)
            }
            drawBitmap(source, 0f, 0f, paint)
        }

        val scaledRadius = (blurConfig.radius / sampling).coerceIn(1, BlurConfig.MAX_RADIUS)

        val blurBitmap = try {
            blurEngine.blur(bitmap, scaledRadius)
        } catch (_: Exception) {
            StackBlurEngine().blur(bitmap, scaledRadius)
        }

        if (blurBitmap == null) {
            releaseWorkingBitmap(bitmap)
            return null
        }

        val result = if (sampling == 1) {
            blurBitmap
        } else {
            val scaled = Bitmap.createScaledBitmap(blurBitmap, blurConfig.width, blurConfig.height, true)
            if (scaled !== blurBitmap && blurBitmap !== bitmap) {
                blurBitmap.recycle()
            }
            scaled
        }

        if (bitmap !== result) {
            releaseWorkingBitmap(bitmap)
        }

        return result
    }

    fun release() {
        synchronized(stateLock) {
            if (released) return
            released = true
            blurEngine.release()
            workingBitmapPool.values.forEach { if (!it.isRecycled) it.recycle() }
            workingBitmapPool.clear()
        }
    }

    companion object {
        private const val MAX_POOLED_BITMAPS = 8

        fun radiusForDensity(densityScale: Float): Int =
            BlurConfig.MAX_RADIUS.coerceAtMost((densityScale * 10).roundToInt())
    }
}

object NeuBlurMakerHolder {

    @Volatile
    private var instance: BlurMaker? = null

    fun get(context: Context): BlurMaker {
        // Registration is idempotent, but do not resize the cache on every
        // composition. A trim event intentionally lowers the active budget;
        // restoring it here would immediately undo OS memory-pressure relief.
        NeuShadowCache.registerMemoryPressureListener(context)
        return instance ?: synchronized(this) {
            instance ?: BlurMaker(
                context,
                calculateDefaultBlurRadius(context.resources.displayMetrics)
            ).also { instance = it }
        }
    }

    private fun calculateDefaultBlurRadius(displayMetrics: DisplayMetrics): Int {
        val densityStable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            DisplayMetrics.DENSITY_DEVICE_STABLE / DisplayMetrics.DENSITY_DEFAULT.toFloat()
        } else {
            displayMetrics.density
        }
        return BlurMaker.radiusForDensity(densityStable)
    }

    fun warmUp(context: Context) {
        get(context).warmUp()
    }
}
