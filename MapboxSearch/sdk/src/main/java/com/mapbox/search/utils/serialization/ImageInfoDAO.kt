package com.mapbox.search.utils.serialization

import com.google.gson.annotations.SerializedName
import com.mapbox.search.ImageInfo

internal class ImageInfoDAO(
    @SerializedName("url") val url: String? = null,
    @SerializedName("width") val width: Int? = null,
    @SerializedName("height") val height: Int? = null
) : DataAccessObject<ImageInfo> {

    override val isValid: Boolean
        get() = !url.isNullOrBlank() && width != null && width > 0 && height != null && height > 0

    override fun createData(): ImageInfo {
        return ImageInfo(url = url!!, width = width!!, height = height!!)
    }

    companion object {
        fun create(imageInfo: ImageInfo): ImageInfoDAO {
            return with(imageInfo) {
                ImageInfoDAO(url = url, width = width, height = height)
            }
        }
    }
}
