@file:OptIn(MapboxExperimental::class)

package com.mapbox.search.base.factory

import androidx.annotation.RestrictTo
import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.base.core.CoreParkingRestriction
import com.mapbox.search.base.core.CoreParkingType
import com.mapbox.search.common.parking.ParkingRestriction
import com.mapbox.search.common.parking.ParkingType

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
@ParkingType.Type
fun CoreParkingType.mapToPlatform(): String {
    return when (this) {
        CoreParkingType.ALONG_MOTORWAY -> ParkingType.ALONG_MOTORWAY
        CoreParkingType.PARKING_GARAGE -> ParkingType.PARKING_GARAGE
        CoreParkingType.PARKING_LOT -> ParkingType.PARKING_LOT
        CoreParkingType.ON_DRIVEWAY -> ParkingType.ON_DRIVEWAY
        CoreParkingType.ON_STREET -> ParkingType.ON_STREET
        CoreParkingType.OFF_STREET -> ParkingType.OFF_STREET
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
        ParkingType.OFF_STREET -> CoreParkingType.OFF_STREET
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
        CoreParkingRestriction.NO_PARKING -> ParkingRestriction.NO_PARKING
        CoreParkingRestriction.MAX_STAY -> ParkingRestriction.MAX_STAY
        CoreParkingRestriction.MONTHLY_ONLY -> ParkingRestriction.MONTHLY_ONLY
        CoreParkingRestriction.NO_SUV -> ParkingRestriction.NO_SUV
        CoreParkingRestriction.NO_LPG -> ParkingRestriction.NO_LPG
        CoreParkingRestriction.VALET_ONLY -> ParkingRestriction.VALET_ONLY
        CoreParkingRestriction.VISITORS_ONLY -> ParkingRestriction.VISITORS_ONLY
        CoreParkingRestriction.EVENTS_ONLY -> ParkingRestriction.EVENTS_ONLY
        CoreParkingRestriction.NO_RESTRICTIONS_OUTSIDE_HOURS -> ParkingRestriction.NO_RESTRICTIONS_OUTSIDE_HOURS
        CoreParkingRestriction.BOOKING_ONLY -> ParkingRestriction.BOOKING_ONLY
        CoreParkingRestriction.PARKING_DISK -> ParkingRestriction.PARKING_DISK
        CoreParkingRestriction.UNKNOWN -> ParkingRestriction.UNKNOWN
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun createCoreParkingRestriction(@ParkingRestriction.Type type: String): CoreParkingRestriction? {
    return when (type) {
        ParkingRestriction.EV_ONLY -> CoreParkingRestriction.EV_ONLY
        ParkingRestriction.PLUGGED -> CoreParkingRestriction.PLUGGED
        ParkingRestriction.DISABLED -> CoreParkingRestriction.DISABLED
        ParkingRestriction.CUSTOMERS -> CoreParkingRestriction.CUSTOMERS
        ParkingRestriction.MOTOR_CYCLES -> CoreParkingRestriction.MOTOR_CYCLES
        ParkingRestriction.NO_PARKING -> CoreParkingRestriction.NO_PARKING
        ParkingRestriction.MAX_STAY -> CoreParkingRestriction.MAX_STAY
        ParkingRestriction.MONTHLY_ONLY -> CoreParkingRestriction.MONTHLY_ONLY
        ParkingRestriction.NO_SUV -> CoreParkingRestriction.NO_SUV
        ParkingRestriction.NO_LPG -> CoreParkingRestriction.NO_LPG
        ParkingRestriction.VALET_ONLY -> CoreParkingRestriction.VALET_ONLY
        ParkingRestriction.VISITORS_ONLY -> CoreParkingRestriction.VISITORS_ONLY
        ParkingRestriction.EVENTS_ONLY -> CoreParkingRestriction.EVENTS_ONLY
        ParkingRestriction.NO_RESTRICTIONS_OUTSIDE_HOURS ->
            CoreParkingRestriction.NO_RESTRICTIONS_OUTSIDE_HOURS
        ParkingRestriction.BOOKING_ONLY -> CoreParkingRestriction.BOOKING_ONLY
        ParkingRestriction.PARKING_DISK -> CoreParkingRestriction.PARKING_DISK
        ParkingRestriction.UNKNOWN -> CoreParkingRestriction.UNKNOWN
        else -> null
    }
}
