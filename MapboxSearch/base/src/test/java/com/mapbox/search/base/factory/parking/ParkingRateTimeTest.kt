@file:OptIn(MapboxExperimental::class)

package com.mapbox.search.base.factory.parking

import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.base.factory.parking.TestTypes.CORE_PARKING_RATE_TIME
import com.mapbox.search.base.factory.parking.TestTypes.PLATFORM_PARKING_RATE_TIME
import com.mapbox.search.common.parking.ParkingRateTime
import com.mapbox.search.common.tests.ToStringVerifier
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ParkingRateTimeTest {

    @Test
    fun `equals(), hashCode(), toString() functions are correct`() {
        EqualsVerifier.forClass(ParkingRateTime::class.java)
            .verify()

        ToStringVerifier(
            clazz = ParkingRateTime::class,
        ).verify()
    }

    @Test
    fun `mapToCore() test`() {
        assertEquals(CORE_PARKING_RATE_TIME, PLATFORM_PARKING_RATE_TIME.mapToCore())
    }

    @Test
    fun `mapToPlatform() test`() {
        assertEquals(PLATFORM_PARKING_RATE_TIME, CORE_PARKING_RATE_TIME.mapToPlatform())
    }
}
