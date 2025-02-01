package com.mapbox.search.common.metadata

import android.os.Parcelable
import androidx.annotation.Px
import com.mapbox.annotation.MapboxExperimental
import kotlinx.parcelize.Parcelize

/**
 * Image information.
 *
 * @property url image URL.
 * @property width image width in pixels.
 * @property height image height in pixels.
 * @property thumbnailUrl Optional URL from where a thumbnail of the image can be fetched.
 * @property imageCategory [ImageCategory.Type] value indicating what the image is used for.
 * @property type Optional image type like: GIF, JPG, PNG, SVG.
 */
@OptIn(MapboxExperimental::class)
@Parcelize
public class ImageInfo @MapboxExperimental constructor(
    public val url: String,
    @Px public val width: Int,
    @Px public val height: Int,
    @MapboxExperimental
    public val thumbnailUrl: String?,
    @MapboxExperimental
    @ImageCategory.Type
    public val imageCategory: String?,
    @MapboxExperimental
    public val type: String?,
) : Parcelable {

    /**
     * Image information constructor.
     *
     * @param url image URL.
     * @param width image width in pixels.
     * @param height image height in pixels.
     */
    public constructor(url: String, width: Int, height: Int) : this(
        url = url,
        width = width,
        height = height,
        thumbnailUrl = null,
        imageCategory = null,
        type = null
    )

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
        if (thumbnailUrl != other.thumbnailUrl) return false
        if (imageCategory != other.imageCategory) return false
        if (type != other.type) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = url.hashCode()
        result = 31 * result + width
        result = 31 * result + height
        result = 31 * result + (thumbnailUrl?.hashCode() ?: 0)
        result = 31 * result + (imageCategory?.hashCode() ?: 0)
        result = 31 * result + (type?.hashCode() ?: 0)
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "ImageInfo(" +
                "url='$url', " +
                "width=$width, " +
                "height=$height, " +
                "thumbnailUrl=$thumbnailUrl, " +
                "imageCategory=$imageCategory, " +
                "type=$type" +
                ")"
    }
}
