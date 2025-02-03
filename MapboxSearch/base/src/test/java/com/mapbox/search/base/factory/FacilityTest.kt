package com.mapbox.search.base.factory

import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.base.core.CoreFacility
import com.mapbox.search.common.Facility
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@OptIn(MapboxExperimental::class)
class FacilityTest {

    @Test
    fun `CoreFacility mapToPlatform() test`() {
        assertEquals(Facility.HOTEL, CoreFacility.HOTEL.mapToPlatform())
        assertEquals(Facility.RESTAURANT, CoreFacility.RESTAURANT.mapToPlatform())
        assertEquals(Facility.CAFE, CoreFacility.CAFE.mapToPlatform())
        assertEquals(Facility.MALL, CoreFacility.MALL.mapToPlatform())
        assertEquals(Facility.SUPERMARKET, CoreFacility.SUPERMARKET.mapToPlatform())
        assertEquals(Facility.SPORT, CoreFacility.SPORT.mapToPlatform())
        assertEquals(Facility.RECREATION_AREA, CoreFacility.RECREATION_AREA.mapToPlatform())
        assertEquals(Facility.NATURE, CoreFacility.NATURE.mapToPlatform())
        assertEquals(Facility.MUSEUM, CoreFacility.MUSEUM.mapToPlatform())
        assertEquals(Facility.BIKE_SHARING, CoreFacility.BIKE_SHARING.mapToPlatform())
        assertEquals(Facility.BUS_STOP, CoreFacility.BUS_STOP.mapToPlatform())
        assertEquals(Facility.TAXI_STAND, CoreFacility.TAXI_STAND.mapToPlatform())
        assertEquals(Facility.TRAM_STOP, CoreFacility.TRAM_STOP.mapToPlatform())
        assertEquals(Facility.METRO_STATION, CoreFacility.METRO_STATION.mapToPlatform())
        assertEquals(Facility.TRAIN_STATION, CoreFacility.TRAIN_STATION.mapToPlatform())
        assertEquals(Facility.AIRPORT, CoreFacility.AIRPORT.mapToPlatform())
        assertEquals(Facility.PARKING_LOT, CoreFacility.PARKING_LOT.mapToPlatform())
        assertEquals(Facility.CARPOOL_PARKING, CoreFacility.CARPOOL_PARKING.mapToPlatform())
        assertEquals(Facility.FUEL_STATION, CoreFacility.FUEL_STATION.mapToPlatform())
        assertEquals(Facility.WIFI, CoreFacility.WIFI.mapToPlatform())
        assertEquals(Facility.UNKNOWN, CoreFacility.UNKNOWN.mapToPlatform())
    }
}
