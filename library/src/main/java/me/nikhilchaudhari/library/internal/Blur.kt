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
 *
 * ## Performance contract - read before using
 * Constructing [BlurMaker] is expensive: on API < 31 it lazily owns a
 * `RenderScript` context, which is one of the heaviest objects you can create
 * on Android (it spins up a driver-level GPU/CPU compute context). A [BlurMaker]
 * MUST be created once and reused - e.g. via `remember { }` in a composable -
 * rather than recreated on every draw or recomposition.
 *
 * Recreating a fresh RenderScript context on every animation frame (which is
 * what earlier versions of this library did implicitly, since the modifier
 * that owned it was rebuilt on every recomposition) was the single largest
 * source of CPU/battery drain in this library.
 */
class BlurMaker(context: Context, private val defaultBlurRadius: Int) {

    private val contextRef = WeakReference(context.applicationContext ?: context)

    // Created lazily on first use, then reused for the lifetime of this BlurMaker.
    // Only the (cheap) Allocation objects are recreated per-bitmap; the expensive
    // RenderScript + ScriptIntrinsicBlur context is created at most once.
    private var rs: RenderScript? = null
    private var blurScript: ScriptIntrinsicBlur? = null
    private var released = false

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

        // Downsampled working bitmap - this is what actually gets blurred.
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        Canvas(bitmap).run {
            scale(1 / sampling.toFloat(), 1 / sampling.toFloat())
            val paint = Paint().apply {
                flags = Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG
                colorFilter = PorterDuffColorFilter(blurConfig.color, PorterDuff.Mode.SRC_ATOP)
            }
            drawBitmap(source, 0f, 0f, paint)
        }

        // Blur radius must shrink along with the bitmap or the shadow looks
        // proportionally too soft/wide once scaled back up.
        val scaledRadius = (blurConfig.radius / sampling).coerceIn(1, BlurConfig.MAX_RADIUS)

        val blurBitmap: Bitmap? = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // RenderScript is deprecated/unavailable as of API 31; StackBlur is
                // the fallback here, but it now runs on a downsampled bitmap instead
                // of the full-resolution one.
                bitmap.stackBlur(scaledRadius)
            } else {
                blurWithRenderScript(bitmap, scaledRadius)
            }
        } catch (e: Exception) {
            bitmap.stackBlur(scaledRadius)
        }

        return blurBitmap?.let {
            if (sampling == 1) {
                it
            } else {
                // Upscale back to the requested size. Bilinear upscaling of an
                // already-blurred image is indistinguishable from blurring at full
                // resolution to the human eye, since blur removes high-frequency
                // detail anyway.
                val scaled = Bitmap.createScaledBitmap(it, blurConfig.width, blurConfig.height, true)
                if (scaled !== it) it.recycle()
                scaled
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun blurWithRenderScript(bitmap: Bitmap, radius: Int): Bitmap? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return bitmap.stackBlur(radius)
        }
        val context = contextRef.get() ?: return bitmap.stackBlur(radius)

        // Reuse the persistent context/script instead of creating a new
        // RenderScript instance (very expensive) on every single call.
        val renderScript = rs ?: RenderScript.create(context).also { rs = it }
        val script = blurScript
            ?: ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript)).also { blurScript = it }

        var input: Allocation? = null
        var output: Allocation? = null
        return try {
            input = Allocation.createFromBitmap(
                renderScript, bitmap, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT
            )
            output = Allocation.createTyped(renderScript, input.type)
            script.setInput(input)
            script.setRadius(radius.coerceIn(1, 25).toFloat())
            script.forEach(output)
            output.copyTo(bitmap)
            bitmap
        } catch (e: RSRuntimeException) {
            // Context went bad (e.g. low memory) - drop it so the next call rebuilds
            // it, and fall back to StackBlur for this frame.
            release()
            bitmap.stackBlur(radius)
        } finally {
            input?.destroy()
            output?.destroy()
        }
    }

    /**
     * Releases the persistent RenderScript context. Call this from the owning
     * composable's `DisposableEffect`/`onDispose` (or `View.onDetachedFromWindow`)
     * once this [BlurMaker] is no longer needed - never after every draw.
     */
    fun release() {
        if (released) return
        released = true
        blurScript?.destroy()
        blurScript = null
        rs?.destroy()
        rs = null
    }

    companion object {
        /**
         * Suggested blur radius for a device's display density, capped to avoid
         * runaway blur cost on very high density screens.
         */
        fun radiusForDensity(densityScale: Float): Int =
            BlurConfig.MAX_RADIUS.coerceAtMost((densityScale * 10).roundToInt())
    }
}

/**
 * App-wide shared [BlurMaker].
 *
 * ## Why this exists
 * A screen with N neumorphic components used to mean N separate [BlurMaker]
 * instances, each lazily owning its own `RenderScript` context on API < 31.
 * `RenderScript.create()` is heavy - creating 15-20+ of them in a single first
 * frame (a realistic count for a screen with several cards/buttons/switches)
 * is enough to visibly freeze app startup, since Compose's first frame must
 * finish drawing on the main thread before anything is shown.
 *
 * Sharing a single [BlurMaker] (and therefore a single `RenderScript` context)
 * across the whole app means that context is created **at most once** for the
 * app's entire lifetime, no matter how many neumorphic components exist.
 */
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

    /**
     * Optional: call this once, early, off the main thread (e.g. from a
     * background coroutine in `Application.onCreate`) to pay the one-time
     * `RenderScript` context creation cost before the first neumorphic
     * composable needs it, so the first frame never has to wait for it.
     * Safe to call multiple times or never - it's a pure optimization.
     */
    fun warmUp(context: Context) {
        get(context)
    }
}
