package me.nikhilchaudhari.library.internal

import android.graphics.Bitmap
import android.util.LruCache

/**
 * High-performance LRU Cache for neumorphic shadow Bitmaps.
 * Caches generated shadow Bitmaps based on shape parameters, dimensions, and colors
 * to eliminate CPU/GPU blur re-computation during recompositions and scroll.
 */
object NeuCache {

    // Allocate 1/8th of available memory for shadow bitmap caching
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = (maxMemory / 8).coerceIn(1024 * 4, 1024 * 32) // Min 4MB, Max 32MB

    private val shadowCache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount / 1024
        }
    }

    fun get(key: String): Bitmap? {
        val cached = shadowCache.get(key)
        return if (cached != null && !cached.isRecycled) {
            cached
        } else {
            if (cached?.isRecycled == true) {
                shadowCache.remove(key)
            }
            null
        }
    }

    fun put(key: String, bitmap: Bitmap) {
        if (!bitmap.isRecycled) {
            shadowCache.put(key, bitmap)
        }
    }

    fun clear() {
        shadowCache.evictAll()
    }
}
