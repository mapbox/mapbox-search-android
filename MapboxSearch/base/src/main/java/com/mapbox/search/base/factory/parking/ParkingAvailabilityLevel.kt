@file:OptIn(MapboxExperimental::class)

package com.mapbox.search.base.factory.parking

import androidx.annotation.RestrictTo
import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.base.core.CoreParkingAvailabilityLevel
import com.mapbox.search.common.parking.ParkingAvailabilityLevel

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
@ParkingAvailabilityLevel.Type
fun CoreParkingAvailabilityLevel.mapToPlatform(): String {
    return when (this) {
        CoreParkingAvailabilityLevel.LOW -> ParkingAvailabilityLevel.LOW
        CoreParkingAvailabilityLevel.MID -> ParkingAvailabilityLevel.MID
        CoreParkingAvailabilityLevel.HIGH -> ParkingAvailabilityLevel.HIGH
        CoreParkingAvailabilityLevel.UNKNOWN -> ParkingAvailabilityLevel.UNKNOWN
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun createCoreParkingAvailabilityLevel(
    @ParkingAvailabilityLevel.Type level: String?,
): CoreParkingAvailabilityLevel? {
    return when (level) {
        ParkingAvailabilityLevel.LOW -> CoreParkingAvailabilityLevel.LOW
        ParkingAvailabilityLevel.MID -> CoreParkingAvailabilityLevel.MID
        ParkingAvailabilityLevel.HIGH -> CoreParkingAvailabilityLevel.HIGH
        ParkingAvailabilityLevel.UNKNOWN -> CoreParkingAvailabilityLevel.UNKNOWN
        else -> null
    }
}
