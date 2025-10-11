package com.thatcakeid.zrytezene

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

/**
 * This is a simple class that contains
 * the app's build extra information
 */
object ExtraMetadata {
    private const val SHOW_WATERMARK = true
    private const val BUILD_INFO = "dev"

    fun setWatermarkColors(watermark: TextView, root: LinearLayout) {
        val color = when (BUILD_INFO) {
            "dev" -> 0xFFB71C1C.toInt()
            "beta" -> 0xFF2196F3.toInt()
            "stable" -> 0xFF2E7D32.toInt()

            else -> throw IllegalStateException("Unexpected value: $BUILD_INFO")
        }

        watermark.setBackgroundColor(color)
        root.visibility = if (SHOW_WATERMARK) View.VISIBLE else View.GONE
        watermark.text = BUILD_INFO
    }

    // TODO: Migrate to Media3
    /**
    fun getExoPlayerCacheDir(mContext: Context): File {
        return File(mContext.cacheDir, "audioCache")
    }

    @UnstableApi
    val exoPlayerCacheEvictor: CacheEvictor =
        LeastRecentlyUsedCacheEvictor((50 * 1024 * 1024).toLong()) // 50MB cache size
    **/
}