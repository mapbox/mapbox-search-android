@file:OptIn(MapboxExperimental::class)

package com.mapbox.search.base.factory.parking

import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.common.parking.ParkingRateValue
import com.mapbox.search.common.tests.ToStringVerifier
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.Test

class ParkingRateValueTest {

    @Test
    fun `equals(), hashCode(), toString() functions are correct`() {
        EqualsVerifier.forClass(ParkingRateValue.IsoValue::class.java)
            .verify()

        EqualsVerifier.forClass(ParkingRateValue.CustomDurationValue::class.java)
            .verify()

        ToStringVerifier(
            clazz = ParkingRateValue.IsoValue::class,
        ).verify()

        ToStringVerifier(
            clazz = ParkingRateValue.CustomDurationValue::class,
        ).verify()
    }
}
