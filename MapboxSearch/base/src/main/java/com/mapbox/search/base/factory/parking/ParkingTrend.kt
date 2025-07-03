@file:OptIn(MapboxExperimental::class)

package com.mapbox.search.base.factory.parking

import androidx.annotation.RestrictTo
import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.base.core.CoreParkingTrend
import com.mapbox.search.common.parking.ParkingTrend

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
@ParkingTrend.Type
fun CoreParkingTrend.mapToPlatform(): String {
    return when (this) {
        CoreParkingTrend.NO_CHANGE -> ParkingTrend.NO_CHANGE
        CoreParkingTrend.DECREASING -> ParkingTrend.DECREASING
        CoreParkingTrend.INCREASING -> ParkingTrend.INCREASING
        CoreParkingTrend.UNKNOWN -> ParkingTrend.UNKNOWN
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun createCoreParkingTrend(@ParkingTrend.Type type: String?): CoreParkingTrend? {
    return when (type) {
        ParkingTrend.NO_CHANGE -> CoreParkingTrend.NO_CHANGE
        ParkingTrend.DECREASING -> CoreParkingTrend.DECREASING
        ParkingTrend.INCREASING -> CoreParkingTrend.INCREASING
        ParkingTrend.UNKNOWN -> CoreParkingTrend.UNKNOWN
        else -> null
    }
}
