@file:Suppress("DEPRECATION")

package me.nikhilchaudhari.library.internal

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ColorSpace
import android.graphics.HardwareRenderer
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RenderEffect
import android.graphics.RenderNode
import android.graphics.Shader
import android.hardware.HardwareBuffer
import android.media.ImageReader
import android.os.Build
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.RSRuntimeException
import android.renderscript.ScriptIntrinsicBlur
import java.lang.ref.WeakReference

internal interface BlurEngine {
    fun warmUp()
    fun blur(bitmap: Bitmap, radius: Int): Bitmap?
    fun release()
}

internal class StackBlurEngine : BlurEngine {
    override fun warmUp() = Unit

    override fun blur(bitmap: Bitmap, radius: Int): Bitmap? {
        if (bitmap.isRecycled || bitmap.width <= 0 || bitmap.height <= 0) return null
        return bitmap.stackBlurInPlace(radius.coerceIn(1, BlurConfig.MAX_RADIUS))
    }

    override fun release() = Unit
}

/**
 * GPU blur via [RenderEffect] + [HardwareRenderer] (API 31+).
 *
 * Resources are pooled per bitmap size. Any failure falls back to [StackBlurEngine]
 * so the public renderer never loses shadows on exotic devices / drivers.
 */
internal class RenderEffectBlurEngine(
    private val lock: Any
) : BlurEngine {

    private data class Session(
        val imageReader: ImageReader,
        val renderNode: RenderNode,
        val hardwareRenderer: HardwareRenderer
    )

    private val sessionsBySize =
        LinkedHashMap<Pair<Int, Int>, Session>(8, 0.75f, true)

    override fun warmUp() = Unit

    override fun blur(bitmap: Bitmap, radius: Int): Bitmap? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return StackBlurEngine().blur(bitmap, radius)
        }
        if (bitmap.isRecycled || bitmap.width <= 0 || bitmap.height <= 0) return null
        val safeRadius = radius.coerceIn(1, BlurConfig.MAX_RADIUS).toFloat()

        return synchronized(lock) {
            try {
                blurWithRenderEffectLocked(bitmap, safeRadius)
            } catch (_: Throwable) {
                destroySessionsLocked()
                StackBlurEngine().blur(bitmap, radius)
            }
        }
    }

    private fun blurWithRenderEffectLocked(bitmap: Bitmap, radius: Float): Bitmap? {
        val width = bitmap.width
        val height = bitmap.height
        val session = sessionFor(width, height)

        val node = session.renderNode
        node.setPosition(0, 0, width, height)
        node.setRenderEffect(
            RenderEffect.createBlurEffect(radius, radius, Shader.TileMode.CLAMP)
        )

        val canvas = node.beginRecording(width, height)
        canvas.drawBitmap(bitmap, 0f, 0f, PASSTHROUGH_PAINT)
        node.endRecording()

        session.hardwareRenderer.createRenderRequest()
            .setWaitForPresent(true)
            .syncAndDraw()

        val image = session.imageReader.acquireLatestImage() ?: return StackBlurEngine().blur(bitmap, radius.toInt())
        try {
            val buffer = image.hardwareBuffer ?: return StackBlurEngine().blur(bitmap, radius.toInt())
            try {
                val hardware = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Bitmap.wrapHardwareBuffer(buffer, ColorSpace.get(ColorSpace.Named.SRGB))
                } else {
                    null
                }
                if (hardware == null) {
                    return StackBlurEngine().blur(bitmap, radius.toInt())
                }
                val software = hardware.copy(Bitmap.Config.ARGB_8888, false)
                hardware.recycle()
                return software ?: StackBlurEngine().blur(bitmap, radius.toInt())
            } finally {
                buffer.close()
            }
        } finally {
            image.close()
        }
    }

    private fun sessionFor(width: Int, height: Int): Session {
        val key = width to height
        sessionsBySize[key]?.let { return it }

        if (sessionsBySize.size >= MAX_SESSIONS) {
            val oldest = sessionsBySize.keys.firstOrNull()
            oldest?.let { sessionsBySize.remove(it) }?.let { destroySession(it) }
        }

        val usage = HardwareBuffer.USAGE_GPU_SAMPLED_IMAGE or HardwareBuffer.USAGE_GPU_COLOR_OUTPUT
        val imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 1, usage)
        val renderNode = RenderNode("neu-render-effect-blur")
        val hardwareRenderer = HardwareRenderer().apply {
            setSurface(imageReader.surface)
            setContentRoot(renderNode)
        }
        val session = Session(imageReader, renderNode, hardwareRenderer)
        sessionsBySize[key] = session
        return session
    }

    private fun destroySession(session: Session) {
        try { session.hardwareRenderer.destroy() } catch (_: Throwable) {}
        try { session.renderNode.discardDisplayList() } catch (_: Throwable) {}
        try { session.imageReader.close() } catch (_: Throwable) {}
    }

    private fun destroySessionsLocked() {
        sessionsBySize.values.forEach { destroySession(it) }
        sessionsBySize.clear()
    }

    override fun release() {
        synchronized(lock) { destroySessionsLocked() }
    }

    companion object {
        private const val MAX_SESSIONS = 4
        private val PASSTHROUGH_PAINT = Paint(Paint.FILTER_BITMAP_FLAG)
    }
}

@Suppress("DEPRECATION")
internal class RenderScriptBlurEngine(
    context: Context,
    private val lock: Any
) : BlurEngine {

    private val contextRef = WeakReference(context.applicationContext ?: context)
    private var rs: RenderScript? = null
    private var blurScript: ScriptIntrinsicBlur? = null

    private val allocationsBySize =
        LinkedHashMap<Pair<Int, Int>, Pair<Allocation, Allocation>>(16, 0.75f, true)

    override fun warmUp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) return
        synchronized(lock) { ensureResourcesLocked() }
    }

    override fun blur(bitmap: Bitmap, radius: Int): Bitmap? {
        if (bitmap.isRecycled || bitmap.width <= 0 || bitmap.height <= 0) return null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return bitmap.stackBlurInPlace(radius.coerceIn(1, BlurConfig.MAX_RADIUS))
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
            } catch (_: RSRuntimeException) {
                resetLocked()
                StackBlurEngine().blur(bitmap, radius)
            } catch (_: IllegalStateException) {
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
        synchronized(lock) { resetLocked() }
    }

    companion object { private const val MAX_CACHED_ALLOCATION_SIZES = 8 }
}
