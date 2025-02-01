@file:OptIn(MapboxExperimental::class)

package com.mapbox.search.base.factory

import androidx.annotation.RestrictTo
import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.base.core.CoreFacility
import com.mapbox.search.common.Facility

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
@Facility.Type
fun CoreFacility.mapToPlatform(): String {
    return when (this) {
        CoreFacility.HOTEL -> Facility.HOTEL
        CoreFacility.RESTAURANT -> Facility.RESTAURANT
        CoreFacility.CAFE -> Facility.CAFE
        CoreFacility.MALL -> Facility.MALL
        CoreFacility.SUPERMARKET -> Facility.SUPERMARKET
        CoreFacility.SPORT -> Facility.SPORT
        CoreFacility.RECREATION_AREA -> Facility.RECREATION_AREA
        CoreFacility.NATURE -> Facility.NATURE
        CoreFacility.MUSEUM -> Facility.MUSEUM
        CoreFacility.BIKE_SHARING -> Facility.BIKE_SHARING
        CoreFacility.BUS_STOP -> Facility.BUS_STOP
        CoreFacility.TAXI_STAND -> Facility.TAXI_STAND
        CoreFacility.TRAM_STOP -> Facility.TRAM_STOP
        CoreFacility.METRO_STATION -> Facility.METRO_STATION
        CoreFacility.TRAIN_STATION -> Facility.TRAIN_STATION
        CoreFacility.AIRPORT -> Facility.AIRPORT
        CoreFacility.PARKING_LOT -> Facility.PARKING_LOT
        CoreFacility.CARPOOL_PARKING -> Facility.CARPOOL_PARKING
        CoreFacility.FUEL_STATION -> Facility.FUEL_STATION
        CoreFacility.WIFI -> Facility.WIFI
        CoreFacility.UNKNOWN -> Facility.UNKNOWN
    }
}
