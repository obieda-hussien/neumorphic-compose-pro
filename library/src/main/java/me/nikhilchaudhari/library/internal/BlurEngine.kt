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
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.min

internal interface BlurEngine {
    fun warmUp()
    fun blur(bitmap: Bitmap, radius: Int): Bitmap?
    fun release()
    /** Hint that a size is about to be used heavily (pre-warm GPU sessions). */
    fun preferSize(width: Int, height: Int) {}
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
 * Production-grade GPU blur via [RenderEffect] + [HardwareRenderer] (API 31+).
 *
 * - Size-aware session pool with LRU + similarity matching
 * - Multi-pass blur for effective radius up to 64
 * - Preferred-size hints from Modifier.Node keep hot dimensions warm
 * - Safe fallback to [StackBlurEngine] on any failure
 */
internal class RenderEffectBlurEngine(
    private val lock: Any
) : BlurEngine {

    private data class Session(
        val imageReader: ImageReader,
        val renderNode: RenderNode,
        val hardwareRenderer: HardwareRenderer,
        val width: Int,
        val height: Int,
        var lastUsedNs: Long = System.nanoTime(),
        var useCount: Int = 0
    )

    private val sessionsBySize =
        LinkedHashMap<Pair<Int, Int>, Session>(8, 0.75f, true)

    private val preferredSizes = ConcurrentHashMap<Pair<Int, Int>, Long>()

    private val totalBlurs = AtomicLong(0)
    private val gpuBlurs = AtomicLong(0)
    private val fallbackBlurs = AtomicLong(0)
    private val multiPassBlurs = AtomicLong(0)

    override fun warmUp() = Unit

    override fun preferSize(width: Int, height: Int) {
        if (width <= 0 || height <= 0) return
        preferredSizes[width to height] = System.nanoTime()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            synchronized(lock) {
                if (sessionsBySize.size < MAX_SESSIONS && sessionsBySize[width to height] == null) {
                    try {
                        sessionForLocked(width, height)
                    } catch (_: Throwable) {
                        // Best-effort pre-warm only.
                    }
                }
            }
        }
    }

    override fun blur(bitmap: Bitmap, radius: Int): Bitmap? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return StackBlurEngine().blur(bitmap, radius)
        }
        if (bitmap.isRecycled || bitmap.width <= 0 || bitmap.height <= 0) return null
        val safeRadius = radius.coerceIn(1, MAX_EFFECTIVE_RADIUS)

        totalBlurs.incrementAndGet()

        return synchronized(lock) {
            try {
                val result = blurWithRenderEffectLocked(bitmap, safeRadius)
                if (result != null) {
                    gpuBlurs.incrementAndGet()
                    result
                } else {
                    fallbackBlurs.incrementAndGet()
                    StackBlurEngine().blur(bitmap, radius.coerceIn(1, BlurConfig.MAX_RADIUS))
                }
            } catch (_: Throwable) {
                destroySessionsLocked()
                fallbackBlurs.incrementAndGet()
                StackBlurEngine().blur(bitmap, radius.coerceIn(1, BlurConfig.MAX_RADIUS))
            }
        }
    }

    private fun blurWithRenderEffectLocked(bitmap: Bitmap, radius: Int): Bitmap? {
        val width = bitmap.width
        val height = bitmap.height
        val session = sessionForLocked(width, height)

        val passes = computePasses(radius)
        if (passes.size > 1) multiPassBlurs.incrementAndGet()

        var current: Bitmap = bitmap
        var ownedIntermediate: Bitmap? = null

        try {
            for ((index, passRadius) in passes.withIndex()) {
                val isLast = index == passes.lastIndex
                val source = current
                val blurred = singlePassLocked(session, source, passRadius.toFloat())
                    ?: return null

                if (ownedIntermediate != null && ownedIntermediate !== bitmap) {
                    ownedIntermediate.recycle()
                }
                ownedIntermediate = if (blurred !== bitmap) blurred else null
                current = blurred

                if (!isLast && current.config == Bitmap.Config.HARDWARE) {
                    val software = current.copy(Bitmap.Config.ARGB_8888, false) ?: return null
                    if (ownedIntermediate != null && ownedIntermediate !== bitmap) {
                        ownedIntermediate.recycle()
                    }
                    ownedIntermediate = software
                    current = software
                }
            }

            val finalBitmap = current
            if (finalBitmap.config == Bitmap.Config.HARDWARE) {
                val software = finalBitmap.copy(Bitmap.Config.ARGB_8888, false)
                if (finalBitmap !== bitmap) finalBitmap.recycle()
                return software
            }
            return finalBitmap
        } finally {
            // Do not recycle the original input.
        }
    }

    private fun singlePassLocked(session: Session, source: Bitmap, radius: Float): Bitmap? {
        val width = session.width
        val height = session.height
        val node = session.renderNode

        node.setPosition(0, 0, width, height)
        node.setRenderEffect(
            RenderEffect.createBlurEffect(radius, radius, Shader.TileMode.CLAMP)
        )

        val canvas = node.beginRecording(width, height)
        try {
            canvas.drawBitmap(source, 0f, 0f, PASSTHROUGH_PAINT)
        } finally {
            node.endRecording()
        }

        session.hardwareRenderer.createRenderRequest()
            .setWaitForPresent(true)
            .syncAndDraw()

        session.lastUsedNs = System.nanoTime()
        session.useCount++

        val image = session.imageReader.acquireLatestImage() ?: return null
        try {
            val buffer = image.hardwareBuffer ?: return null
            try {
                val hardware = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Bitmap.wrapHardwareBuffer(buffer, ColorSpace.get(ColorSpace.Named.SRGB))
                } else {
                    null
                } ?: return null

                val software = hardware.copy(Bitmap.Config.ARGB_8888, false)
                hardware.recycle()
                return software
            } finally {
                buffer.close()
            }
        } finally {
            image.close()
        }
    }

    private fun computePasses(radius: Int): List<Int> {
        if (radius <= MAX_SINGLE_PASS_RADIUS) return listOf(radius)
        val passes = ArrayList<Int>(4)
        var remaining = radius
        while (remaining > 0) {
            val pass = min(remaining, MAX_SINGLE_PASS_RADIUS)
            passes.add(pass)
            remaining = (remaining - pass * 0.85).toInt().coerceAtLeast(0)
            if (passes.size >= MAX_PASSES) break
        }
        if (passes.isEmpty()) passes.add(1)
        return passes
    }

    private fun sessionForLocked(width: Int, height: Int): Session {
        val exactKey = width to height
        sessionsBySize[exactKey]?.let { return it }

        val targetArea = width.toLong() * height
        var best: Session? = null
        var bestScore = Double.MAX_VALUE
        for ((_, session) in sessionsBySize) {
            val area = session.width.toLong() * session.height
            val areaRatio = area.toDouble() / targetArea.coerceAtLeast(1)
            if (areaRatio < 0.85 || areaRatio > 1.15) continue
            val aspectDiff = kotlin.math.abs(
                session.width.toDouble() / session.height - width.toDouble() / height
            )
            if (aspectDiff > 0.08) continue
            val score = kotlin.math.abs(areaRatio - 1.0) + aspectDiff
            if (score < bestScore) {
                bestScore = score
                best = session
            }
        }
        if (best != null && best.width >= width && best.height >= height) {
            return best
        }

        if (sessionsBySize.size >= MAX_SESSIONS) {
            evictOneLocked()
        }

        return createSessionLocked(width, height)
    }

    private fun createSessionLocked(width: Int, height: Int): Session {
        val usage = HardwareBuffer.USAGE_GPU_SAMPLED_IMAGE or HardwareBuffer.USAGE_GPU_COLOR_OUTPUT
        val imageReader = ImageReader.newInstance(
            width, height, PixelFormat.RGBA_8888, /* maxImages = */ 2, usage
        )
        val renderNode = RenderNode("neu-render-effect-blur")
        val hardwareRenderer = HardwareRenderer().apply {
            setSurface(imageReader.surface)
            setContentRoot(renderNode)
            setOpaque(false)
        }
        val session = Session(imageReader, renderNode, hardwareRenderer, width, height)
        sessionsBySize[width to height] = session
        return session
    }

    private fun evictOneLocked() {
        val now = System.nanoTime()
        var victimKey: Pair<Int, Int>? = null
        var victimScore = Double.NEGATIVE_INFINITY

        for ((key, session) in sessionsBySize) {
            val preferredBoost = if (preferredSizes.containsKey(key)) 0.0 else 1.0
            val ageSec = (now - session.lastUsedNs) / 1_000_000_000.0
            val score = preferredBoost * 10.0 + ageSec - session.useCount * 0.01
            if (score > victimScore) {
                victimScore = score
                victimKey = key
            }
        }

        victimKey?.let { key ->
            sessionsBySize.remove(key)?.let { destroySession(it) }
        }
    }

    private fun destroySession(session: Session) {
        try {
            session.hardwareRenderer.destroy()
        } catch (_: Throwable) {
        }
        try {
            session.renderNode.discardDisplayList()
        } catch (_: Throwable) {
        }
        try {
            session.imageReader.close()
        } catch (_: Throwable) {
        }
    }

    private fun destroySessionsLocked() {
        sessionsBySize.values.forEach { destroySession(it) }
        sessionsBySize.clear()
    }

    override fun release() {
        synchronized(lock) {
            destroySessionsLocked()
            preferredSizes.clear()
        }
    }

    fun stats(): Map<String, Long> = mapOf(
        "total" to totalBlurs.get(),
        "gpu" to gpuBlurs.get(),
        "fallback" to fallbackBlurs.get(),
        "multiPass" to multiPassBlurs.get(),
        "sessions" to sessionsBySize.size.toLong()
    )

    companion object {
        private const val MAX_SESSIONS = 6
        private const val MAX_SINGLE_PASS_RADIUS = 25
        private const val MAX_EFFECTIVE_RADIUS = 64
        private const val MAX_PASSES = 4
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

    companion object {
        private const val MAX_CACHED_ALLOCATION_SIZES = 8
    }
}
