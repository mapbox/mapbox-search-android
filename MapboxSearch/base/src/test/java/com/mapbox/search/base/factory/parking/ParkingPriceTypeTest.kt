@file:OptIn(MapboxExperimental::class)

package com.mapbox.search.base.factory.parking

import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.base.core.CoreParkingPriceType
import com.mapbox.search.common.parking.ParkingPriceType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ParkingPriceTypeTest {

    @Test
    fun `CoreParkingPriceType mapToPlatform() test`() {
        assertEquals(ParkingPriceType.DURATION, CoreParkingPriceType.DURATION.mapToPlatform())
        assertEquals(ParkingPriceType.DURATION_ADDITIONAL, CoreParkingPriceType.DURATION_ADDITIONAL.mapToPlatform())
        assertEquals(ParkingPriceType.CUSTOM, CoreParkingPriceType.CUSTOM.mapToPlatform())
    }

    @Test
    fun `createCoreParkingPriceType() test`() {
        assertEquals(CoreParkingPriceType.DURATION, createCoreParkingPriceType(ParkingPriceType.DURATION))
        assertEquals(CoreParkingPriceType.DURATION_ADDITIONAL, createCoreParkingPriceType(ParkingPriceType.DURATION_ADDITIONAL))
        assertEquals(CoreParkingPriceType.CUSTOM, createCoreParkingPriceType(ParkingPriceType.CUSTOM))
        assertNull(createCoreParkingPriceType("UNKNOWN_PRICE_TYPE"))
    }
}
