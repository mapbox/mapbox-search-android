@file:OptIn(MapboxExperimental::class)

package com.mapbox.search.base.factory.parking

import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.base.core.CoreParkingTrend
import com.mapbox.search.common.parking.ParkingTrend
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ParkingTrendTest {

    @Test
    fun `CoreParkingTrend mapToPlatform() test`() {
        assertEquals(ParkingTrend.NO_CHANGE, CoreParkingTrend.NO_CHANGE.mapToPlatform())
        assertEquals(ParkingTrend.DECREASING, CoreParkingTrend.DECREASING.mapToPlatform())
        assertEquals(ParkingTrend.INCREASING, CoreParkingTrend.INCREASING.mapToPlatform())
        assertEquals(ParkingTrend.UNKNOWN, CoreParkingTrend.UNKNOWN.mapToPlatform())
    }

    @Test
    fun `createCoreParkingTrend() test`() {
        assertEquals(CoreParkingTrend.NO_CHANGE, createCoreParkingTrend(ParkingTrend.NO_CHANGE))
        assertEquals(CoreParkingTrend.DECREASING, createCoreParkingTrend(ParkingTrend.DECREASING))
        assertEquals(CoreParkingTrend.INCREASING, createCoreParkingTrend(ParkingTrend.INCREASING))
        assertEquals(CoreParkingTrend.UNKNOWN, createCoreParkingTrend(ParkingTrend.UNKNOWN))
        assertNull(createCoreParkingTrend("UNKNOWN_TREND_123"))
    }
}
