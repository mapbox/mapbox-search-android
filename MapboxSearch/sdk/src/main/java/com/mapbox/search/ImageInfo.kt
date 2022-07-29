package com.mapbox.search

import android.os.Parcelable
import androidx.annotation.Px
import com.mapbox.search.base.core.CoreImageInfo
import kotlinx.parcelize.Parcelize

/**
 * Image information.
 *
 * @property url image URL.
 * @property width image width in pixels.
 * @property height image height in pixels.
 */
@Parcelize
public class ImageInfo(
    public val url: String,
    @Px public val width: Int,
    @Px public val height: Int,
) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ImageInfo

        if (url != other.url) return false
        if (width != other.width) return false
        if (height != other.height) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = url.hashCode()
        result = 31 * result + width
        result = 31 * result + height
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "ImageInfo(" +
                "url='$url', " +
                "width=$width, " +
                "height=$height" +
                ")"
    }
}

@JvmSynthetic
internal fun CoreImageInfo.mapToPlatform(): ImageInfo = ImageInfo(
    url = url,
    width = width,
    height = height
)

@JvmSynthetic
internal fun ImageInfo.mapToCore(): CoreImageInfo = CoreImageInfo(
    url,
    width,
    height
)
