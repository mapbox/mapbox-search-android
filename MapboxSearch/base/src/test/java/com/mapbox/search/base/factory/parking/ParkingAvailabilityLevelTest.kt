@file:OptIn(MapboxExperimental::class)

package com.mapbox.search.base.factory.parking

import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.base.core.CoreParkingAvailabilityLevel
import com.mapbox.search.common.parking.ParkingAvailabilityLevel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ParkingAvailabilityLevelTest {

    @Test
    fun `CoreParkingAvailabilityLevel mapToPlatform() test`() {
        assertEquals(ParkingAvailabilityLevel.LOW, CoreParkingAvailabilityLevel.LOW.mapToPlatform())
        assertEquals(ParkingAvailabilityLevel.MID, CoreParkingAvailabilityLevel.MID.mapToPlatform())
        assertEquals(
            ParkingAvailabilityLevel.HIGH,
            CoreParkingAvailabilityLevel.HIGH.mapToPlatform()
        )
        assertEquals(
            ParkingAvailabilityLevel.UNKNOWN,
            CoreParkingAvailabilityLevel.UNKNOWN.mapToPlatform()
        )
    }

    @Test
    fun `createCoreParkingAvailabilityLevel() test`() {
        assertEquals(
            CoreParkingAvailabilityLevel.UNKNOWN,
            createCoreParkingAvailabilityLevel(ParkingAvailabilityLevel.UNKNOWN)
        )
        assertEquals(
            CoreParkingAvailabilityLevel.LOW,
            createCoreParkingAvailabilityLevel(ParkingAvailabilityLevel.LOW)
        )
        assertEquals(
            CoreParkingAvailabilityLevel.MID,
            createCoreParkingAvailabilityLevel(ParkingAvailabilityLevel.MID)
        )
        assertEquals(
            CoreParkingAvailabilityLevel.HIGH,
            createCoreParkingAvailabilityLevel(ParkingAvailabilityLevel.HIGH)
        )
        assertNull(createCoreParkingAvailabilityLevel("UNKNOWN_AVAILABILITY_123"))
    }
}
