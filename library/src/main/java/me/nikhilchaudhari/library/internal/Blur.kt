package me.nikhilchaudhari.library.internal

import android.content.Context
import android.graphics.*
import android.os.Build
import android.renderscript.*
import android.util.DisplayMetrics
import java.lang.ref.WeakReference
import kotlin.math.roundToInt

/**
 * Holds required data for blur operation
 */
data class BlurConfig(
    val width: Int,
    val height: Int,
    val radius: Int = MAX_RADIUS,
    val sampling: Int = DEFAULT_SAMPLING,
    val color: Int = Color.TRANSPARENT
) {
    companion object {
        const val MAX_RADIUS = 25

        /**
         * Shadows are blurred, so downsampling before the blur pass is visually
         * lossless (the blur itself discards fine detail) but cuts the number of
         * pixels the CPU has to touch by [DEFAULT_SAMPLING]^2. This is one of the
         * biggest single levers on CPU/battery cost for this library, since the
         * blur pass runs on the CPU on all API levels.
         */
        const val DEFAULT_SAMPLING = 2
    }
}

/**
 * Blur implementation with a **persistent** RenderScript context (API < 31) and
 * downsampled StackBlur (API 31+, since RenderScript is deprecated there).
 */
@Suppress("DEPRECATION")
class BlurMaker(context: Context, private val defaultBlurRadius: Int) {

    private val contextRef = WeakReference(context.applicationContext ?: context)
    private var rs: RenderScript? = null
    private var blurScript: ScriptIntrinsicBlur? = null
    private var released = false

    private val workingBitmapPool = mutableMapOf<Long, Bitmap>()

    private fun sizeKey(width: Int, height: Int): Long =
        (width.toLong() shl 32) or (height.toLong() and 0xFFFFFFFFL)

    private fun obtainWorkingBitmap(width: Int, height: Int): Bitmap {
        val pooled = workingBitmapPool.remove(sizeKey(width, height))
        return if (pooled != null && !pooled.isRecycled) {
            pooled.eraseColor(android.graphics.Color.TRANSPARENT)
            pooled
        } else {
            Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        }
    }

    private fun releaseWorkingBitmap(bitmap: Bitmap) {
        if (bitmap.isRecycled) return
        if (workingBitmapPool.size < MAX_POOLED_BITMAPS) {
            workingBitmapPool[sizeKey(bitmap.width, bitmap.height)] = bitmap
        } else {
            bitmap.recycle()
        }
    }

    fun blur(
        source: Bitmap,
        radius: Int = defaultBlurRadius,
        sampling: Int = BlurConfig.DEFAULT_SAMPLING
    ): Bitmap? {
        if (released) return null
        val blurConfig = BlurConfig(
            width = source.width,
            height = source.height,
            radius = radius,
            sampling = sampling
        )
        return blur(source, blurConfig)
    }

    private fun blur(source: Bitmap, blurConfig: BlurConfig): Bitmap? {
        val sampling = blurConfig.sampling.coerceAtLeast(1)
        val width = (blurConfig.width / sampling).coerceAtLeast(1)
        val height = (blurConfig.height / sampling).coerceAtLeast(1)
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

        val blurBitmap: Bitmap? = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                bitmap.stackBlur(scaledRadius)
            } else {
                blurWithRenderScript(bitmap, scaledRadius)
            }
        } catch (e: Exception) {
            bitmap.stackBlur(scaledRadius)
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

    private fun blurWithRenderScript(bitmap: Bitmap, radius: Int): Bitmap? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return bitmap.stackBlur(radius)
        }
        val context = contextRef.get() ?: return bitmap.stackBlur(radius)

        val renderScript = rs ?: RenderScript.create(context).also { rs = it }
        val script = blurScript
            ?: ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript)).also { blurScript = it }

        return try {
            val (input, output) = allocationsFor(renderScript, bitmap)
            input.copyFrom(bitmap)
            script.setInput(input)
            script.setRadius(radius.coerceIn(1, 25).toFloat())
            script.forEach(output)
            output.copyTo(bitmap)
            bitmap
        } catch (e: RSRuntimeException) {
            resetRenderScriptResources()
            bitmap.stackBlur(radius)
        }
    }

    // Small, size-bounded LRU cache of (input, output) Allocation pairs.
    // Access-order is enabled so frequently reused dimensions stay hot.
    private val allocationsBySize =
        LinkedHashMap<Pair<Int, Int>, Pair<Allocation, Allocation>>(16, 0.75f, true)

    private fun allocationsFor(renderScript: RenderScript, bitmap: Bitmap): Pair<Allocation, Allocation> {
        val key = bitmap.width to bitmap.height
        allocationsBySize[key]?.let { return it }

        if (allocationsBySize.size >= MAX_CACHED_ALLOCATION_SIZES) {
            val oldestKey = allocationsBySize.keys.firstOrNull()
            oldestKey?.let { allocationsBySize.remove(it) }?.let { (input, output) ->
                input.destroy()
                output.destroy()
            }
        }

        val input = Allocation.createFromBitmap(
            renderScript, bitmap, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT
        )
        val output = Allocation.createTyped(renderScript, input.type)
        val pair = input to output
        allocationsBySize[key] = pair
        return pair
    }

    private fun resetRenderScriptResources() {
        allocationsBySize.values.forEach { (input, output) ->
            input.destroy()
            output.destroy()
        }
        allocationsBySize.clear()
        blurScript?.destroy()
        blurScript = null
        rs?.destroy()
        rs = null
        released = false
    }

    fun release() {
        if (released) return
        resetRenderScriptResources()
        workingBitmapPool.values.forEach { if (!it.isRecycled) it.recycle() }
        workingBitmapPool.clear()
        released = true
    }

    companion object {
        private const val MAX_POOLED_BITMAPS = 8
        private const val MAX_CACHED_ALLOCATION_SIZES = 8

        fun radiusForDensity(densityScale: Float): Int =
            BlurConfig.MAX_RADIUS.coerceAtMost((densityScale * 10).roundToInt())
    }
}

object NeuBlurMakerHolder {

    @Volatile
    private var instance: BlurMaker? = null

    fun get(context: Context): BlurMaker {
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
        get(context)
    }
}
