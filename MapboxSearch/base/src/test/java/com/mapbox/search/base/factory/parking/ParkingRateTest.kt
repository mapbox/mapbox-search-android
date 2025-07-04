@file:OptIn(MapboxExperimental::class)

package com.mapbox.search.base.factory.parking

import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.base.factory.parking.TestTypes.CORE_PARKING_RATE
import com.mapbox.search.base.factory.parking.TestTypes.PLATFORM_PARKING_RATE
import com.mapbox.search.common.parking.ParkingRate
import com.mapbox.search.common.tests.ReflectionObjectsFactory
import com.mapbox.search.common.tests.ToStringVerifier
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ParkingRateTest {

    @Test
    fun `equals(), hashCode(), toString() functions are correct`() {
        EqualsVerifier.forClass(ParkingRate::class.java)
            .verify()

        ToStringVerifier(
            clazz = ParkingRate::class,
            objectsFactory = ReflectionObjectsFactory(
                extraCreators = listOf(TestTypes.PARKING_RATE_PRICE_CREATOR)
            ),
        ).verify()
    }

    @Test
    fun `mapToCore() test`() {
        assertTrue(equals(CORE_PARKING_RATE, PLATFORM_PARKING_RATE.mapToCore()))
    }

    @Test
    fun `mapToPlatform() test`() {
        assertEquals(PLATFORM_PARKING_RATE, CORE_PARKING_RATE.mapToPlatform())
    }
}
