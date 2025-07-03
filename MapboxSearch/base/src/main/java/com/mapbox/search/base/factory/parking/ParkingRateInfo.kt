@file:OptIn(MapboxExperimental::class)

package com.mapbox.search.base.factory.parking

import androidx.annotation.RestrictTo
import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.base.core.CoreParkingRateInfo
import com.mapbox.search.common.parking.ParkingRateInfo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun CoreParkingRateInfo.mapToPlatform(): ParkingRateInfo {
    return ParkingRateInfo(
        currencySymbol = currencySymbol,
        currencyCode = currencyCode,
        rates = rates?.map { it.mapToPlatform() },
    )
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun ParkingRateInfo.mapToCore(): CoreParkingRateInfo {
    return CoreParkingRateInfo(
        currencySymbol = currencySymbol,
        currencyCode = currencyCode,
        rates = rates?.map { it.mapToCore() },
    )
}
