package com.mapbox.search.utils.bitmap

import androidx.annotation.IntRange

internal data class BitmapEncodeOptions(
    @IntRange(from = 1) val minSideSize: Int,
    @IntRange(from = 0, to = 100) val compressQuality: Int
) {
    init {
        check(minSideSize > 0) { "minSideSize should be greater than 0!" }
        check(compressQuality in 0..100) { "Compress quality should be in [0..100] range!" }
    }
}
