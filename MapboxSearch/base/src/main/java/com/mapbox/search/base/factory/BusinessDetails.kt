@file:OptIn(MapboxExperimental::class)

package com.mapbox.search.base.factory

import androidx.annotation.RestrictTo
import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.base.core.CoreBusinessDetails
import com.mapbox.search.common.BusinessDetails

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun CoreBusinessDetails.mapToPlatform(): BusinessDetails {
    return BusinessDetails(
        name = name,
        website = website,
        logo = logo?.mapToPlatform(),
    )
}
