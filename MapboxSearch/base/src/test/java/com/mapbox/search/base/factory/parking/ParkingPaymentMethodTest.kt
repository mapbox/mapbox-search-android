@file:OptIn(MapboxExperimental::class)

package com.mapbox.search.base.factory.parking

import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.base.core.CoreParkingPaymentMethod
import com.mapbox.search.common.parking.ParkingPaymentMethod
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ParkingPaymentMethodTest {

    @Test
    fun `CoreParkingPaymentMethod mapToPlatform() test`() {
        assertEquals(
            ParkingPaymentMethod.PAY_ON_FOOT,
            CoreParkingPaymentMethod.PAY_ON_FOOT.mapToPlatform()
        )
        assertEquals(
            ParkingPaymentMethod.PAY_AND_DISPLAY,
            CoreParkingPaymentMethod.PAY_AND_DISPLAY.mapToPlatform()
        )
        assertEquals(
            ParkingPaymentMethod.PAY_ON_EXIT,
            CoreParkingPaymentMethod.PAY_ON_EXIT.mapToPlatform()
        )
        assertEquals(
            ParkingPaymentMethod.PAY_ON_ENTRY,
            CoreParkingPaymentMethod.PAY_ON_ENTRY.mapToPlatform()
        )
        assertEquals(
            ParkingPaymentMethod.PARKING_METER,
            CoreParkingPaymentMethod.PARKING_METER.mapToPlatform()
        )
        assertEquals(
            ParkingPaymentMethod.MULTI_SPACE_METER,
            CoreParkingPaymentMethod.MULTI_SPACE_METER.mapToPlatform()
        )
        assertEquals(
            ParkingPaymentMethod.HONESTY_BOX,
            CoreParkingPaymentMethod.HONESTY_BOX.mapToPlatform()
        )
        assertEquals(
            ParkingPaymentMethod.ATTENDANT,
            CoreParkingPaymentMethod.ATTENDANT.mapToPlatform()
        )
        assertEquals(
            ParkingPaymentMethod.PAY_BY_PLATE,
            CoreParkingPaymentMethod.PAY_BY_PLATE.mapToPlatform()
        )
        assertEquals(
            ParkingPaymentMethod.PAY_AT_RECEPTION,
            CoreParkingPaymentMethod.PAY_AT_RECEPTION.mapToPlatform()
        )
        assertEquals(
            ParkingPaymentMethod.PAY_BY_PHONE,
            CoreParkingPaymentMethod.PAY_BY_PHONE.mapToPlatform()
        )
        assertEquals(
            ParkingPaymentMethod.PAY_BY_COUPON,
            CoreParkingPaymentMethod.PAY_BY_COUPON.mapToPlatform()
        )
        assertEquals(
            ParkingPaymentMethod.ELECTRONIC_PARKING_SYSTEM,
            CoreParkingPaymentMethod.ELECTRONIC_PARKING_SYSTEM.mapToPlatform()
        )
        assertEquals(ParkingPaymentMethod.UNKNOWN, CoreParkingPaymentMethod.UNKNOWN.mapToPlatform())
    }

    @Test
    fun `createCoreParkingPaymentMethod() test`() {
        assertEquals(
            CoreParkingPaymentMethod.PAY_ON_FOOT,
            createCoreParkingPaymentMethod(ParkingPaymentMethod.PAY_ON_FOOT)
        )
        assertEquals(
            CoreParkingPaymentMethod.PAY_AND_DISPLAY,
            createCoreParkingPaymentMethod(ParkingPaymentMethod.PAY_AND_DISPLAY)
        )
        assertEquals(
            CoreParkingPaymentMethod.PAY_ON_EXIT,
            createCoreParkingPaymentMethod(ParkingPaymentMethod.PAY_ON_EXIT)
        )
        assertEquals(
            CoreParkingPaymentMethod.PAY_ON_ENTRY,
            createCoreParkingPaymentMethod(ParkingPaymentMethod.PAY_ON_ENTRY)
        )
        assertEquals(
            CoreParkingPaymentMethod.PARKING_METER,
            createCoreParkingPaymentMethod(ParkingPaymentMethod.PARKING_METER)
        )
        assertEquals(
            CoreParkingPaymentMethod.MULTI_SPACE_METER,
            createCoreParkingPaymentMethod(ParkingPaymentMethod.MULTI_SPACE_METER)
        )
        assertEquals(
            CoreParkingPaymentMethod.HONESTY_BOX,
            createCoreParkingPaymentMethod(ParkingPaymentMethod.HONESTY_BOX)
        )
        assertEquals(
            CoreParkingPaymentMethod.ATTENDANT,
            createCoreParkingPaymentMethod(ParkingPaymentMethod.ATTENDANT)
        )
        assertEquals(
            CoreParkingPaymentMethod.PAY_BY_PLATE,
            createCoreParkingPaymentMethod(ParkingPaymentMethod.PAY_BY_PLATE)
        )
        assertEquals(
            CoreParkingPaymentMethod.PAY_AT_RECEPTION,
            createCoreParkingPaymentMethod(ParkingPaymentMethod.PAY_AT_RECEPTION)
        )
        assertEquals(
            CoreParkingPaymentMethod.PAY_BY_PHONE,
            createCoreParkingPaymentMethod(ParkingPaymentMethod.PAY_BY_PHONE)
        )
        assertEquals(
            CoreParkingPaymentMethod.PAY_BY_COUPON,
            createCoreParkingPaymentMethod(ParkingPaymentMethod.PAY_BY_COUPON)
        )
        assertEquals(
            CoreParkingPaymentMethod.ELECTRONIC_PARKING_SYSTEM,
            createCoreParkingPaymentMethod(ParkingPaymentMethod.ELECTRONIC_PARKING_SYSTEM)
        )
        assertEquals(
            CoreParkingPaymentMethod.UNKNOWN,
            createCoreParkingPaymentMethod(ParkingPaymentMethod.UNKNOWN)
        )

        assertNull(createCoreParkingPaymentMethod("UNKNOWN_METHOD_123"))
    }
}
