@file:OptIn(MapboxExperimental::class)

package com.mapbox.search.base.factory

import androidx.annotation.RestrictTo
import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.base.core.CoreParkingRestriction
import com.mapbox.search.base.core.CoreParkingType
import com.mapbox.search.common.ParkingRestriction
import com.mapbox.search.common.ParkingType

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
@ParkingType.Type
fun CoreParkingType.mapToPlatform(): String {
    return when (this) {
        CoreParkingType.ALONG_MOTORWAY -> ParkingType.ALONG_MOTORWAY
        CoreParkingType.PARKING_GARAGE -> ParkingType.PARKING_GARAGE
        CoreParkingType.PARKING_LOT -> ParkingType.PARKING_LOT
        CoreParkingType.ON_DRIVEWAY -> ParkingType.ON_DRIVEWAY
        CoreParkingType.ON_STREET -> ParkingType.ON_STREET
        CoreParkingType.UNDERGROUND_GARAGE -> ParkingType.UNDERGROUND_GARAGE
        CoreParkingType.UNKNOWN -> ParkingType.UNKNOWN
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun createCoreParkingType(@ParkingType.Type type: String?): CoreParkingType? {
    return when (type) {
        ParkingType.ALONG_MOTORWAY -> CoreParkingType.ALONG_MOTORWAY
        ParkingType.PARKING_GARAGE -> CoreParkingType.PARKING_GARAGE
        ParkingType.PARKING_LOT -> CoreParkingType.PARKING_LOT
        ParkingType.ON_DRIVEWAY -> CoreParkingType.ON_DRIVEWAY
        ParkingType.ON_STREET -> CoreParkingType.ON_STREET
        ParkingType.UNDERGROUND_GARAGE -> CoreParkingType.UNDERGROUND_GARAGE
        ParkingType.UNKNOWN -> CoreParkingType.UNKNOWN
        else -> null
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
@ParkingRestriction.Type
fun CoreParkingRestriction.mapToPlatform(): String {
    return when (this) {
        CoreParkingRestriction.EV_ONLY -> ParkingRestriction.EV_ONLY
        CoreParkingRestriction.PLUGGED -> ParkingRestriction.PLUGGED
        CoreParkingRestriction.DISABLED -> ParkingRestriction.DISABLED
        CoreParkingRestriction.CUSTOMERS -> ParkingRestriction.CUSTOMERS
        CoreParkingRestriction.MOTOR_CYCLES -> ParkingRestriction.MOTOR_CYCLES
        CoreParkingRestriction.UNKNOWN -> ParkingRestriction.UNKNOWN
    }
}
