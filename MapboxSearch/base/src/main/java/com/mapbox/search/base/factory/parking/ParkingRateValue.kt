@file:OptIn(MapboxExperimental::class)

package com.mapbox.search.base.factory.parking

import androidx.annotation.RestrictTo
import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.base.core.CoreParkingRateValue
import com.mapbox.search.common.parking.ParkingRateValue

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun CoreParkingRateValue.mapToPlatform(): ParkingRateValue {
    return if (isString) {
        ParkingRateValue.IsoValue(string)
    } else {
        ParkingRateValue.CustomDurationValue(parkingRateCustomValue.mapToPlatform())
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun ParkingRateValue.mapToCore(): CoreParkingRateValue {
    return when (this) {
        is ParkingRateValue.IsoValue -> CoreParkingRateValue.valueOf(value)
        is ParkingRateValue.CustomDurationValue -> {
            createCoreParkingRateCustomValue(value)?.let {
                CoreParkingRateValue.valueOf(it)
            } ?: error("Unknown ParkingRateType $value")
        }
        else -> error("Unknown subtype of ParkingRateValue")
    }
}
