package me.nikhilchaudhari.library.internal

import android.graphics.Bitmap
import android.graphics.Color
import org.junit.Test
import org.junit.Assert.*

class StackBlurRegressionTest {
    @Test fun opaqueBlackDoesNotBecomeTransparent() {
        val image = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888)
        try {
            image.eraseColor(Color.BLACK)
            image.stackBlurInPlace(4)
            assertEquals(Color.BLACK, image.getPixel(8, 8))
        } finally { image.recycle() }
    }
    @Test fun transparentEdgeBlursAlphaWithoutDarkeningWhite() {
        val image = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888)
        try {
            for (y in 0 until 16) for (x in 8 until 16) image.setPixel(x, y, Color.WHITE)
            image.stackBlurInPlace(3)
            val edge = image.getPixel(7, 8)
            assertTrue(Color.alpha(edge) in 1..254)
            assertTrue(Color.red(edge) >= 250)
        } finally { image.recycle() }
    }
    @Test fun shrinkingRadiusRebuildsDivisionTable() {
        val image = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888)
        try {
            image.eraseColor(Color.WHITE)
            image.stackBlurInPlace(12)
            image.eraseColor(Color.WHITE)
            image.stackBlurInPlace(2)
            assertEquals(Color.WHITE, image.getPixel(8, 8))
        } finally { image.recycle() }
    }
}
