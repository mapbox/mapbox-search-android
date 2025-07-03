@file:OptIn(MapboxExperimental::class)

package com.mapbox.search.base.factory.parking

import androidx.annotation.RestrictTo
import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.base.core.CoreParkingPriceType
import com.mapbox.search.common.parking.ParkingPriceType

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
@ParkingPriceType.Type
fun CoreParkingPriceType.mapToPlatform(): String {
    return when (this) {
        CoreParkingPriceType.DURATION -> ParkingPriceType.DURATION
        CoreParkingPriceType.DURATION_ADDITIONAL -> ParkingPriceType.DURATION_ADDITIONAL
        CoreParkingPriceType.CUSTOM -> ParkingPriceType.CUSTOM
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun createCoreParkingPriceType(@ParkingPriceType.Type type: String?): CoreParkingPriceType? {
    return when (type) {
        ParkingPriceType.DURATION -> CoreParkingPriceType.DURATION
        ParkingPriceType.DURATION_ADDITIONAL -> CoreParkingPriceType.DURATION_ADDITIONAL
        ParkingPriceType.CUSTOM -> CoreParkingPriceType.CUSTOM
        else -> null
    }
}
