package me.nikhilchaudhari.library.internal

import android.graphics.Bitmap
import kotlin.math.max
import kotlin.math.min

// Taken from - FastBlur - https://gist.github.com/xalexchen/9679178
/**
 * StackBlur v1.0 for Android, with a bounded thread-local workspace.
 *
 * The public [stackBlur] function preserves the historical non-mutating
 * behavior by copying the source first. Renderer internals use
 * [stackBlurInPlace] on disposable scratch bitmaps to avoid that extra copy.
 */
fun Bitmap.stackBlur(radius: Int): Bitmap? {
    if (radius < 1 || isRecycled) return null
    val bitmapConfig = config ?: Bitmap.Config.ARGB_8888
    val bitmap = copy(bitmapConfig, true) ?: return null
    return bitmap.stackBlurInPlace(radius)
}

/** Internal renderer variant that blurs this bitmap in place. */
internal fun Bitmap.stackBlurInPlace(radius: Int): Bitmap? {
    if (radius < 1 || isRecycled || width <= 0 || height <= 0) return null

    val w = width
    val h = height
    val workspace = STACK_BLUR_WORKSPACE.get()
    workspace.ensure(w, h, radius)

    val pix = workspace.pixels
    val r = workspace.r
    val g = workspace.g
    val b = workspace.b
    val vmin = workspace.vmin
    val dv = workspace.dv
    val stack = workspace.stack

    getPixels(pix, 0, w, 0, 0, w, h)

    val wm = w - 1
    val hm = h - 1
    val wh = w * h
    val div = radius + radius + 1

    var rsum: Int
    var gsum: Int
    var bsum: Int
    var x: Int
    var y: Int
    var i: Int
    var p: Int
    var yp: Int
    var yi: Int
    var yw: Int
    var stackpointer: Int
    var stackstart: Int
    var sir: IntArray
    var rbs: Int
    val r1 = radius + 1
    var routsum: Int
    var goutsum: Int
    var boutsum: Int
    var rinsum: Int
    var ginsum: Int
    var binsum: Int

    yi = 0
    yw = yi
    y = 0
    while (y < h) {
        bsum = 0
        gsum = bsum
        rsum = gsum
        boutsum = rsum
        goutsum = boutsum
        routsum = goutsum
        binsum = routsum
        ginsum = binsum
        rinsum = ginsum
        i = -radius
        while (i <= radius) {
            p = pix[yi + min(wm, max(i, 0))]
            sir = stack[i + radius]
            sir[0] = p and 0xff0000 shr 16
            sir[1] = p and 0x00ff00 shr 8
            sir[2] = p and 0x0000ff
            rbs = r1 - kotlin.math.abs(i)
            rsum += sir[0] * rbs
            gsum += sir[1] * rbs
            bsum += sir[2] * rbs
            if (i > 0) {
                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]
            } else {
                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]
            }
            i++
        }
        stackpointer = radius
        x = 0
        while (x < w) {
            r[yi] = dv[rsum]
            g[yi] = dv[gsum]
            b[yi] = dv[bsum]
            rsum -= routsum
            gsum -= goutsum
            bsum -= boutsum
            stackstart = stackpointer - radius + div
            sir = stack[stackstart % div]
            routsum -= sir[0]
            goutsum -= sir[1]
            boutsum -= sir[2]
            if (y == 0) {
                vmin[x] = min(x + radius + 1, wm)
            }
            p = pix[yw + vmin[x]]
            sir[0] = p and 0xff0000 shr 16
            sir[1] = p and 0x00ff00 shr 8
            sir[2] = p and 0x0000ff
            rinsum += sir[0]
            ginsum += sir[1]
            binsum += sir[2]
            rsum += rinsum
            gsum += ginsum
            bsum += binsum
            stackpointer = (stackpointer + 1) % div
            sir = stack[stackpointer]
            routsum += sir[0]
            goutsum += sir[1]
            boutsum += sir[2]
            rinsum -= sir[0]
            ginsum -= sir[1]
            binsum -= sir[2]
            yi++
            x++
        }
        yw += w
        y++
    }

    x = 0
    while (x < w) {
        bsum = 0
        gsum = bsum
        rsum = gsum
        boutsum = rsum
        goutsum = boutsum
        routsum = goutsum
        binsum = routsum
        ginsum = binsum
        rinsum = ginsum
        yp = -radius * w
        i = -radius
        while (i <= radius) {
            yi = max(0, yp) + x
            sir = stack[i + radius]
            sir[0] = r[yi]
            sir[1] = g[yi]
            sir[2] = b[yi]
            rbs = r1 - kotlin.math.abs(i)
            rsum += r[yi] * rbs
            gsum += g[yi] * rbs
            bsum += b[yi] * rbs
            if (i > 0) {
                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]
            } else {
                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]
            }
            if (i < hm) {
                yp += w
            }
            i++
        }
        yi = x
        stackpointer = radius
        y = 0
        while (y < h) {
            // Preserve alpha channel.
            pix[yi] = -0x1000000 and pix[yi] or (dv[rsum] shl 16) or (dv[gsum] shl 8) or dv[bsum]
            rsum -= routsum
            gsum -= goutsum
            bsum -= boutsum
            stackstart = stackpointer - radius + div
            sir = stack[stackstart % div]
            routsum -= sir[0]
            goutsum -= sir[1]
            boutsum -= sir[2]
            if (x == 0) {
                vmin[y] = min(y + r1, hm) * w
            }
            p = x + vmin[y]
            sir[0] = r[p]
            sir[1] = g[p]
            sir[2] = b[p]
            rinsum += sir[0]
            ginsum += sir[1]
            binsum += sir[2]
            rsum += rinsum
            gsum += ginsum
            bsum += binsum
            stackpointer = (stackpointer + 1) % div
            sir = stack[stackpointer]
            routsum += sir[0]
            goutsum += sir[1]
            boutsum += sir[2]
            rinsum -= sir[0]
            ginsum -= sir[1]
            binsum -= sir[2]
            yi += w
            y++
        }
        x++
    }

    setPixels(pix, 0, w, 0, 0, w, h)

    // Do not retain giant buffers after a one-off full-screen blur.
    if (wh > StackBlurWorkspace.MAX_RETAINED_PIXELS) {
        STACK_BLUR_WORKSPACE.remove()
    }

    return this
}

private class StackBlurWorkspace {
    var pixels = IntArray(0)
    var r = IntArray(0)
    var g = IntArray(0)
    var b = IntArray(0)
    var vmin = IntArray(0)
    var dv = IntArray(0)
    var stack = emptyArray<IntArray>()

    fun ensure(width: Int, height: Int, radius: Int) {
        val wh = width * height
        if (pixels.size < wh) pixels = IntArray(wh)
        if (r.size < wh) r = IntArray(wh)
        if (g.size < wh) g = IntArray(wh)
        if (b.size < wh) b = IntArray(wh)

        val maxDimension = max(width, height)
        if (vmin.size < maxDimension) vmin = IntArray(maxDimension)

        val div = radius + radius + 1
        var divsum = div + 1 shr 1
        divsum *= divsum
        val requiredDv = 256 * divsum
        if (dv.size < requiredDv) {
            dv = IntArray(requiredDv)
            var i = 0
            while (i < requiredDv) {
                dv[i] = i / divsum
                i++
            }
        }

        if (stack.size < div) {
            val old = stack
            stack = Array(div) { index ->
                if (index < old.size) old[index] else IntArray(3)
            }
        }
    }

    companion object {
        const val MAX_RETAINED_PIXELS = 1_000_000
    }
}

private val STACK_BLUR_WORKSPACE = ThreadLocal.withInitial { StackBlurWorkspace() }
