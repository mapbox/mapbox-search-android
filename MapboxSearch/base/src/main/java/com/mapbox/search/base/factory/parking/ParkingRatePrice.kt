@file:OptIn(MapboxExperimental::class)

package com.mapbox.search.base.factory.parking

import androidx.annotation.RestrictTo
import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.base.core.CoreParkingRatePrice
import com.mapbox.search.common.parking.ParkingRatePrice

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun CoreParkingRatePrice.mapToPlatform(): ParkingRatePrice {
    return ParkingRatePrice(
        type = type?.mapToPlatform(),
        amount = amount,
        value = value?.mapToPlatform()
    )
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun ParkingRatePrice.mapToCore(): CoreParkingRatePrice {
    return CoreParkingRatePrice(
        type = type?.let {
            createCoreParkingPriceType(it)
        },
        amount = amount,
        value = value?.mapToCore(),
    )
}
