package me.nikhilchaudhari.library.internal

import android.graphics.Bitmap
import kotlin.math.max
import kotlin.math.min

/**
 * StackBlur implementation. The public function preserves non-mutating
 * behavior; renderer internals use the in-place variant to avoid a full bitmap
 * copy on every cache miss.
 */
fun Bitmap.stackBlur(radius: Int): Bitmap? {
    if (radius !in 1..BlurConfig.MAX_RADIUS || isRecycled) return null
    val bitmap = copy(Bitmap.Config.ARGB_8888, true) ?: return null
    return bitmap.stackBlurInPlace(radius)
}

internal fun Bitmap.stackBlurInPlace(radius: Int): Bitmap? {
    if (radius !in 1..BlurConfig.MAX_RADIUS || isRecycled || width <= 0 || height <= 0) return null

    val w = width
    val h = height
    val workspace = STACK_BLUR_WORKSPACE.get() ?: StackBlurWorkspace().also {
        STACK_BLUR_WORKSPACE.set(it)
    }
    workspace.ensure(w, h, radius)

    val pix = workspace.pixels
    val r = workspace.r
    val g = workspace.g
    val b = workspace.b
    val a = workspace.a
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
    var asum: Int
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
    var ainsum: Int
    var aoutsum: Int

    yi = 0
    yw = yi
    y = 0
    while (y < h) {
        asum = 0; ainsum = 0; aoutsum = 0
        bsum = 0; gsum = bsum; rsum = gsum
        boutsum = rsum; goutsum = boutsum; routsum = goutsum
        binsum = routsum; ginsum = binsum; rinsum = ginsum
        i = -radius
        while (i <= radius) {
            p = pix[yi + min(wm, max(i, 0))]
            sir = stack[i + radius]
            sir[3] = p ushr 24
            sir[0] = ((p ushr 16) and 255) * sir[3] / 255
            sir[1] = ((p ushr 8) and 255) * sir[3] / 255
            sir[2] = (p and 255) * sir[3] / 255
            rbs = r1 - kotlin.math.abs(i)
            rsum += sir[0] * rbs; gsum += sir[1] * rbs; bsum += sir[2] * rbs; asum += sir[3] * rbs
            if (i > 0) { rinsum += sir[0]; ginsum += sir[1]; binsum += sir[2]; ainsum += sir[3] }
            else { routsum += sir[0]; goutsum += sir[1]; boutsum += sir[2]; aoutsum += sir[3] }
            i++
        }
        stackpointer = radius
        x = 0
        while (x < w) {
            r[yi] = dv[rsum]; g[yi] = dv[gsum]; b[yi] = dv[bsum]; a[yi] = dv[asum]
            rsum -= routsum; gsum -= goutsum; bsum -= boutsum; asum -= aoutsum
            stackstart = stackpointer - radius + div
            sir = stack[stackstart % div]
            routsum -= sir[0]; goutsum -= sir[1]; boutsum -= sir[2]; aoutsum -= sir[3]
            if (y == 0) vmin[x] = min(x + radius + 1, wm)
            p = pix[yw + vmin[x]]
            sir[3] = p ushr 24
            sir[0] = ((p ushr 16) and 255) * sir[3] / 255
            sir[1] = ((p ushr 8) and 255) * sir[3] / 255
            sir[2] = (p and 255) * sir[3] / 255
            rinsum += sir[0]; ginsum += sir[1]; binsum += sir[2]; ainsum += sir[3]
            rsum += rinsum; gsum += ginsum; bsum += binsum; asum += ainsum
            stackpointer = (stackpointer + 1) % div
            sir = stack[stackpointer]
            routsum += sir[0]; goutsum += sir[1]; boutsum += sir[2]; aoutsum += sir[3]
            rinsum -= sir[0]; ginsum -= sir[1]; binsum -= sir[2]; ainsum -= sir[3]
            yi++; x++
        }
        yw += w; y++
    }

    x = 0
    while (x < w) {
        asum = 0; ainsum = 0; aoutsum = 0
        bsum = 0; gsum = bsum; rsum = gsum
        boutsum = rsum; goutsum = boutsum; routsum = goutsum
        binsum = routsum; ginsum = binsum; rinsum = ginsum
        yp = -radius * w
        i = -radius
        while (i <= radius) {
            yi = max(0, yp) + x
            sir = stack[i + radius]
            sir[0] = r[yi]; sir[1] = g[yi]; sir[2] = b[yi]; sir[3] = a[yi]
            rbs = r1 - kotlin.math.abs(i)
            rsum += r[yi] * rbs; gsum += g[yi] * rbs; bsum += b[yi] * rbs; asum += a[yi] * rbs
            if (i > 0) { rinsum += sir[0]; ginsum += sir[1]; binsum += sir[2]; ainsum += sir[3] }
            else { routsum += sir[0]; goutsum += sir[1]; boutsum += sir[2]; aoutsum += sir[3] }
            if (i < hm) yp += w
            i++
        }
        yi = x; stackpointer = radius; y = 0
        while (y < h) {
            val na = dv[asum]
            // Bitmap.getPixels/setPixels use straight ARGB. Convolve premultiplied
            // color AND alpha, then unpremultiply once to avoid dark edge fringes.
            val nr = if (na == 0) 0 else (dv[rsum] * 255 / na).coerceAtMost(255)
            val ng = if (na == 0) 0 else (dv[gsum] * 255 / na).coerceAtMost(255)
            val nb = if (na == 0) 0 else (dv[bsum] * 255 / na).coerceAtMost(255)
            pix[yi] = (na shl 24) or (nr shl 16) or (ng shl 8) or nb
            rsum -= routsum; gsum -= goutsum; bsum -= boutsum; asum -= aoutsum
            stackstart = stackpointer - radius + div
            sir = stack[stackstart % div]
            routsum -= sir[0]; goutsum -= sir[1]; boutsum -= sir[2]; aoutsum -= sir[3]
            if (x == 0) vmin[y] = min(y + r1, hm) * w
            p = x + vmin[y]
            sir[0] = r[p]; sir[1] = g[p]; sir[2] = b[p]; sir[3] = a[p]
            rinsum += sir[0]; ginsum += sir[1]; binsum += sir[2]; ainsum += sir[3]
            rsum += rinsum; gsum += ginsum; bsum += binsum; asum += ainsum
            stackpointer = (stackpointer + 1) % div
            sir = stack[stackpointer]
            routsum += sir[0]; goutsum += sir[1]; boutsum += sir[2]; aoutsum += sir[3]
            rinsum -= sir[0]; ginsum -= sir[1]; binsum -= sir[2]; ainsum -= sir[3]
            yi += w; y++
        }
        x++
    }

    setPixels(pix, 0, w, 0, 0, w, h)
    if (wh > StackBlurWorkspace.MAX_RETAINED_PIXELS) STACK_BLUR_WORKSPACE.remove()
    return this
}

private class StackBlurWorkspace {
    var pixels = IntArray(0)
    var r = IntArray(0)
    var g = IntArray(0)
    var b = IntArray(0)
    var a = IntArray(0)
    private var divisor = 0
    var vmin = IntArray(0)
    var dv = IntArray(0)
    var stack = emptyArray<IntArray>()

    fun ensure(width: Int, height: Int, radius: Int) {
        val wh = width * height
        if (pixels.size < wh) pixels = IntArray(wh)
        if (r.size < wh) r = IntArray(wh)
        if (g.size < wh) g = IntArray(wh)
        if (b.size < wh) b = IntArray(wh)
        if (a.size < wh) a = IntArray(wh)
        val maxDimension = max(width, height)
        if (vmin.size < maxDimension) vmin = IntArray(maxDimension)

        val div = radius + radius + 1
        var divsum = div + 1 shr 1
        divsum *= divsum
        val requiredDv = 256 * divsum
        if (dv.size < requiredDv) dv = IntArray(requiredDv)
        if (divisor != divsum) {
            divisor = divsum
            var i = 0
            while (i < requiredDv) { dv[i] = i / divsum; i++ }
        }
        if (stack.size < div) {
            val old = stack
            stack = Array(div) { index -> if (index < old.size) old[index] else IntArray(4) }
        }
    }

    companion object { const val MAX_RETAINED_PIXELS = 1_000_000 }
}

private val STACK_BLUR_WORKSPACE = ThreadLocal.withInitial { StackBlurWorkspace() }
