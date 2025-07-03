@file:OptIn(MapboxExperimental::class)

package com.mapbox.search.base.factory.parking

import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.base.factory.parking.TestTypes.CORE_PARKING_RATE_PRICE
import com.mapbox.search.base.factory.parking.TestTypes.PLATFORM_PARKING_RATE_PRICE
import com.mapbox.search.common.parking.ParkingRatePrice
import com.mapbox.search.common.tests.ReflectionObjectsFactory
import com.mapbox.search.common.tests.ToStringVerifier
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ParkingRatePriceTest {

    @Test
    fun `equals(), hashCode(), toString() functions are correct`() {
        EqualsVerifier.forClass(ParkingRatePrice::class.java)
            .verify()

        ToStringVerifier(
            clazz = ParkingRatePrice::class,
            objectsFactory = ReflectionObjectsFactory(
                extraCreators = listOf(TestTypes.PARKING_RATE_PRICE_CREATOR)
            )
        ).verify()
    }

    @Test
    fun `mapToCore() test`() {
        assertTrue(equals(CORE_PARKING_RATE_PRICE, PLATFORM_PARKING_RATE_PRICE.mapToCore()))
    }

    @Test
    fun `mapToPlatform() test`() {
        assertEquals(PLATFORM_PARKING_RATE_PRICE, CORE_PARKING_RATE_PRICE.mapToPlatform())
    }
}
