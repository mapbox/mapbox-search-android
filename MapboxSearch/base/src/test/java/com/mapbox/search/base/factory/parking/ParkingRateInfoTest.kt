@file:OptIn(MapboxExperimental::class)

package com.mapbox.search.base.factory.parking

import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.base.factory.parking.TestTypes.CORE_PARKING_RATE_INFO
import com.mapbox.search.base.factory.parking.TestTypes.PLATFORM_PARKING_RATE_INFO
import com.mapbox.search.common.parking.ParkingRateInfo
import com.mapbox.search.common.tests.ReflectionObjectsFactory
import com.mapbox.search.common.tests.ToStringVerifier
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ParkingRateInfoTest {

    @Test
    fun `equals(), hashCode(), toString() functions are correct`() {
        EqualsVerifier.forClass(ParkingRateInfo::class.java)
            .verify()

        ToStringVerifier(
            clazz = ParkingRateInfo::class,
            objectsFactory = ReflectionObjectsFactory(
                extraCreators = listOf(TestTypes.PARKING_RATE_PRICE_CREATOR)
            ),
        ).verify()
    }

    @Test
    fun `mapToCore() test`() {
        assertTrue(equals(CORE_PARKING_RATE_INFO, PLATFORM_PARKING_RATE_INFO.mapToCore()))
    }

    @Test
    fun `mapToPlatform() test`() {
        assertEquals(PLATFORM_PARKING_RATE_INFO, CORE_PARKING_RATE_INFO.mapToPlatform())
    }
}
