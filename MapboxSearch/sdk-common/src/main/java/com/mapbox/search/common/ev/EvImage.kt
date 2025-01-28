package com.mapbox.search.common.ev

import android.os.Parcelable
import androidx.annotation.RestrictTo
import com.mapbox.annotation.MapboxExperimental
import kotlinx.parcelize.Parcelize

/**
 * OCPI Image. This class references an image related to an EVSE as a file name or URL.
 * Refer to this type in the [OCPI GitHub repository](https://github.com/ocpi/ocpi/blob/2.2.1/mod_locations.asciidoc#1415-image-class)
 * for more details.
 *
 * @property url URL from where the image data can be fetched through a web browser.
 * @property thumbnail URL from where a thumbnail of the image can be fetched through a web browser.
 * @property category ImageCategory value indicating what the image is used for.
 * @property type Image type like: GIF, JPG, PNG, SVG.
 * @property width Width of the full-scale image.
 * @property height Height of the full-scale image.
 */
@MapboxExperimental
@Parcelize
public class EvImage @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX) constructor(
    public val url: String,
    public val thumbnail: String? = null,
    @EvImageCategory.Type public val category: String,
    public val type: String,
    public val width: Int? = null,
    public val height: Int? = null,
) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EvImage

        if (url != other.url) return false
        if (thumbnail != other.thumbnail) return false
        if (category != other.category) return false
        if (type != other.type) return false
        if (width != other.width) return false
        if (height != other.height) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = url.hashCode()
        result = 31 * result + (thumbnail?.hashCode() ?: 0)
        result = 31 * result + category.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + (width ?: 0)
        result = 31 * result + (height ?: 0)
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "EvImage(" +
                "url=$url, " +
                "thumbnail=$thumbnail, " +
                "category=$category, " +
                "type=$type, " +
                "width=$width, " +
                "height=$height" +
                ")"
    }
}
