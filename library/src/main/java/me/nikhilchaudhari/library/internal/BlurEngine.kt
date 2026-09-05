package me.nikhilchaudhari.library.internal

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.RSRuntimeException
import android.renderscript.ScriptIntrinsicBlur
import java.lang.ref.WeakReference

/**
 * Internal blur backend boundary. Keeping API-level-specific blur implementations
 * behind this interface lets the renderer evolve without changing public APIs.
 */
internal interface BlurEngine {
    fun warmUp()
    fun blur(bitmap: Bitmap, radius: Int): Bitmap?
    fun release()
}

internal class StackBlurEngine : BlurEngine {
    override fun warmUp() = Unit

    override fun blur(bitmap: Bitmap, radius: Int): Bitmap? {
        if (bitmap.isRecycled || bitmap.width <= 0 || bitmap.height <= 0) return null
        return bitmap.stackBlur(radius.coerceIn(1, BlurConfig.MAX_RADIUS))
    }

    override fun release() = Unit
}

@Suppress("DEPRECATION")
internal class RenderScriptBlurEngine(
    context: Context,
    private val lock: Any
) : BlurEngine {

    private val contextRef = WeakReference(context.applicationContext ?: context)
    private var rs: RenderScript? = null
    private var blurScript: ScriptIntrinsicBlur? = null

    // Small, size-bounded LRU cache of (input, output) Allocation pairs.
    private val allocationsBySize =
        LinkedHashMap<Pair<Int, Int>, Pair<Allocation, Allocation>>(16, 0.75f, true)

    override fun warmUp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) return
        synchronized(lock) {
            ensureResourcesLocked()
        }
    }

    override fun blur(bitmap: Bitmap, radius: Int): Bitmap? {
        if (bitmap.isRecycled || bitmap.width <= 0 || bitmap.height <= 0) return null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return bitmap.stackBlur(radius.coerceIn(1, BlurConfig.MAX_RADIUS))
        }

        val context = contextRef.get() ?: return StackBlurEngine().blur(bitmap, radius)

        return synchronized(lock) {
            val renderScript = try {
                ensureResourcesLocked(context)
            } catch (_: Exception) {
                return@synchronized StackBlurEngine().blur(bitmap, radius)
            }
            val script = blurScript ?: return@synchronized StackBlurEngine().blur(bitmap, radius)

            try {
                val (input, output) = allocationsFor(renderScript, bitmap)
                input.copyFrom(bitmap)
                script.setInput(input)
                script.setRadius(radius.coerceIn(1, BlurConfig.MAX_RADIUS).toFloat())
                script.forEach(output)
                output.copyTo(bitmap)
                bitmap
            } catch (e: RSRuntimeException) {
                resetLocked()
                StackBlurEngine().blur(bitmap, radius)
            }
        }
    }

    private fun ensureResourcesLocked(contextOverride: Context? = null): RenderScript {
        rs?.let { return it }
        val context = contextOverride ?: contextRef.get()
            ?: throw IllegalStateException("Application context is unavailable")
        val created = RenderScript.create(context)
        return try {
            val script = ScriptIntrinsicBlur.create(created, Element.U8_4(created))
            rs = created
            blurScript = script
            created
        } catch (error: Throwable) {
            created.destroy()
            throw error
        }
    }

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
            renderScript,
            bitmap,
            Allocation.MipmapControl.MIPMAP_NONE,
            Allocation.USAGE_SCRIPT
        )
        val output = Allocation.createTyped(renderScript, input.type)
        val pair = input to output
        allocationsBySize[key] = pair
        return pair
    }

    private fun resetLocked() {
        allocationsBySize.values.forEach { (input, output) ->
            input.destroy()
            output.destroy()
        }
        allocationsBySize.clear()
        blurScript?.destroy()
        blurScript = null
        rs?.destroy()
        rs = null
    }

    override fun release() {
        synchronized(lock) {
            resetLocked()
        }
    }

    companion object {
        private const val MAX_CACHED_ALLOCATION_SIZES = 8
    }
}
