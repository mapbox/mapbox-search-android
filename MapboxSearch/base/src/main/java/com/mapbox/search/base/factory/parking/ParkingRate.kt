@file:OptIn(MapboxExperimental::class)

package com.mapbox.search.base.factory.parking

import androidx.annotation.RestrictTo
import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.base.core.CoreParkingRate
import com.mapbox.search.common.parking.ParkingRate

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun CoreParkingRate.mapToPlatform(): ParkingRate {
    return ParkingRate(
        maxStay = maxStay,
        times = times?.map { it.mapToPlatform() },
        prices = prices?.map { it.mapToPlatform() }
    )
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun ParkingRate.mapToCore(): CoreParkingRate {
    return CoreParkingRate(
        maxStay = maxStay,
        times = times?.map { it.mapToCore() },
        prices = prices?.map { it.mapToCore() },
    )
}
