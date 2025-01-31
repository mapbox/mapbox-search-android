@file:OptIn(MapboxExperimental::class)

package com.mapbox.search.base.factory

import androidx.annotation.RestrictTo
import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.base.core.CoreImageCategory
import com.mapbox.search.base.core.CoreImageInfo
import com.mapbox.search.common.metadata.ImageCategory
import com.mapbox.search.common.metadata.ImageInfo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
@ImageCategory.Type
fun CoreImageCategory.mapToPlatform(): String {
    return when (this) {
        CoreImageCategory.CHARGER -> ImageCategory.EV_CHARGER
        CoreImageCategory.ENTRANCE -> ImageCategory.ENTRANCE
        CoreImageCategory.LOCATION -> ImageCategory.LOCATION
        CoreImageCategory.NETWORK -> ImageCategory.EV_NETWORK
        CoreImageCategory.OPERATOR -> ImageCategory.EV_OPERATOR
        CoreImageCategory.OTHER -> ImageCategory.OTHER
        CoreImageCategory.OWNER -> ImageCategory.EV_OWNER
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun createCoreImageCategory(@ImageCategory.Type type: String?): CoreImageCategory? {
    return when (type) {
        ImageCategory.EV_CHARGER -> CoreImageCategory.CHARGER
        ImageCategory.EV_NETWORK -> CoreImageCategory.NETWORK
        ImageCategory.EV_OPERATOR -> CoreImageCategory.OPERATOR
        ImageCategory.EV_OWNER -> CoreImageCategory.OWNER
        ImageCategory.ENTRANCE -> CoreImageCategory.ENTRANCE
        ImageCategory.LOCATION -> CoreImageCategory.LOCATION
        ImageCategory.OTHER -> CoreImageCategory.OTHER
        else -> null
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun createCoreImageInfo(
    url: String,
    width: Int,
    height: Int,
    thumbnail: String? = null,
    category: CoreImageCategory? = null,
    type: String? = null,
) = CoreImageInfo(
    url = url,
    width = width,
    height = height,
    thumbnail = thumbnail,
    category = category,
    type = type,
)

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun CoreImageInfo.mapToPlatform(): ImageInfo = ImageInfo(
    url = url,
    width = width,
    height = height,
    thumbnailUrl = thumbnail,
    imageCategory = category?.mapToPlatform(),
    type = type,
)

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun ImageInfo.mapToCore(): CoreImageInfo = CoreImageInfo(
    url = url,
    width = width,
    height = height,
    thumbnail = thumbnailUrl,
    category = createCoreImageCategory(imageCategory),
    type = type,
)
