package com.mapbox.search.utils.bitmap

import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Helper function to encode Bitmap in Base64 String. Useful for sending screenshots
 * over Events SDK, which doesn't support Bitmap encoding out of the box.
 */
internal fun Bitmap.encodeBase64(encodeOptions: BitmapEncodeOptions): String {
    val (scaledWidth, scaledHeight) = if (width <= height) {
        val scaledWidth: Int = min(width, encodeOptions.minSideSize)
        val scaledHeight = ((scaledWidth.toFloat() / width) * height).roundToInt()
        scaledWidth to scaledHeight
    } else {
        val scaledHeight: Int = min(height, encodeOptions.minSideSize)
        val scaledWidth = ((scaledHeight.toFloat() / height) * width).roundToInt()
        scaledWidth to scaledHeight
    }

    val scaled = Bitmap.createScaledBitmap(this, scaledWidth, scaledHeight, true)
    val stream = ByteArrayOutputStream()
    scaled.compress(Bitmap.CompressFormat.JPEG, encodeOptions.compressQuality, stream)

    return Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT) ?: ""
}
