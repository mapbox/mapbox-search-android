@file:OptIn(MapboxExperimental::class)

package com.mapbox.search.base.factory.parking

import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.base.factory.parking.TestTypes.CORE_PARKING_INFO
import com.mapbox.search.base.factory.parking.TestTypes.PLATFORM_PARKING_INFO
import com.mapbox.search.common.parking.ParkingInfo
import com.mapbox.search.common.tests.ReflectionObjectsFactory
import com.mapbox.search.common.tests.ToStringVerifier
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ParkingInfoTest {

    @Test
    fun `equals(), hashCode(), toString() functions are correct`() {
        EqualsVerifier.forClass(ParkingInfo::class.java)
            .verify()

        ToStringVerifier(
            clazz = ParkingInfo::class,
            objectsFactory = ReflectionObjectsFactory(
                extraCreators = listOf(TestTypes.PARKING_RATE_PRICE_CREATOR)
            ),
        ).verify()
    }

    @Test
    fun `mapToCore() test`() {
        assertTrue(equals(CORE_PARKING_INFO, PLATFORM_PARKING_INFO.mapToCore()))
    }

    @Test
    fun `mapToPlatform() test`() {
        assertEquals(PLATFORM_PARKING_INFO, CORE_PARKING_INFO.mapToPlatform())
    }
}
