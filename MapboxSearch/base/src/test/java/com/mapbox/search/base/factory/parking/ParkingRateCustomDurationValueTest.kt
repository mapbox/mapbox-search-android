@file:OptIn(MapboxExperimental::class)

package com.mapbox.search.base.factory.parking

import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.base.core.CoreParkingRateCustomValue
import com.mapbox.search.common.parking.ParkingRateCustomDurationValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ParkingRateCustomDurationValueTest {

    @Test
    fun `CoreParkingRateCustomValue mapToPlatform() test`() {
        assertEquals(
            ParkingRateCustomDurationValue.SIX_MONTHS_MON_FRI,
            CoreParkingRateCustomValue.SIX_MONTHS_MON_FRI.mapToPlatform()
        )
        assertEquals(
            ParkingRateCustomDurationValue.BANK_HOLIDAY,
            CoreParkingRateCustomValue.BANK_HOLIDAY.mapToPlatform()
        )
        assertEquals(
            ParkingRateCustomDurationValue.DAYTIME,
            CoreParkingRateCustomValue.DAYTIME.mapToPlatform()
        )
        assertEquals(
            ParkingRateCustomDurationValue.EARLY_BIRD,
            CoreParkingRateCustomValue.EARLY_BIRD.mapToPlatform()
        )
        assertEquals(
            ParkingRateCustomDurationValue.EVENING,
            CoreParkingRateCustomValue.EVENING.mapToPlatform()
        )
        assertEquals(
            ParkingRateCustomDurationValue.FLAT_RATE,
            CoreParkingRateCustomValue.FLAT_RATE.mapToPlatform()
        )
        assertEquals(ParkingRateCustomDurationValue.MAX, CoreParkingRateCustomValue.MAX.mapToPlatform())
        assertEquals(
            ParkingRateCustomDurationValue.MAX_ONLY_ONCE,
            CoreParkingRateCustomValue.MAX_ONLY_ONCE.mapToPlatform()
        )
        assertEquals(
            ParkingRateCustomDurationValue.MINIMUM,
            CoreParkingRateCustomValue.MINIMUM.mapToPlatform()
        )
        assertEquals(ParkingRateCustomDurationValue.MONTH, CoreParkingRateCustomValue.MONTH.mapToPlatform())
        assertEquals(
            ParkingRateCustomDurationValue.MONTH_MON_FRI,
            CoreParkingRateCustomValue.MONTH_MON_FRI.mapToPlatform()
        )
        assertEquals(
            ParkingRateCustomDurationValue.MONTH_RESERVED,
            CoreParkingRateCustomValue.MONTH_RESERVED.mapToPlatform()
        )
        assertEquals(
            ParkingRateCustomDurationValue.MONTH_UNRESERVED,
            CoreParkingRateCustomValue.MONTH_UNRESERVED.mapToPlatform()
        )
        assertEquals(
            ParkingRateCustomDurationValue.OVERNIGHT,
            CoreParkingRateCustomValue.OVERNIGHT.mapToPlatform()
        )
        assertEquals(
            ParkingRateCustomDurationValue.QUARTER_MON_FRI,
            CoreParkingRateCustomValue.QUARTER_MON_FRI.mapToPlatform()
        )
        assertEquals(
            ParkingRateCustomDurationValue.UNTIL_CLOSING,
            CoreParkingRateCustomValue.UNTIL_CLOSING.mapToPlatform()
        )
        assertEquals(
            ParkingRateCustomDurationValue.WEEKEND,
            CoreParkingRateCustomValue.WEEKEND.mapToPlatform()
        )
        assertEquals(
            ParkingRateCustomDurationValue.YEAR_MON_FRI,
            CoreParkingRateCustomValue.YEAR_MON_FRI.mapToPlatform()
        )
    }

    @Test
    fun `createCoreParkingRateCustomValue() test`() {
        assertEquals(
            CoreParkingRateCustomValue.SIX_MONTHS_MON_FRI,
            createCoreParkingRateCustomValue(ParkingRateCustomDurationValue.SIX_MONTHS_MON_FRI)
        )
        assertEquals(
            CoreParkingRateCustomValue.BANK_HOLIDAY,
            createCoreParkingRateCustomValue(ParkingRateCustomDurationValue.BANK_HOLIDAY)
        )
        assertEquals(
            CoreParkingRateCustomValue.DAYTIME,
            createCoreParkingRateCustomValue(ParkingRateCustomDurationValue.DAYTIME)
        )
        assertEquals(
            CoreParkingRateCustomValue.EARLY_BIRD,
            createCoreParkingRateCustomValue(ParkingRateCustomDurationValue.EARLY_BIRD)
        )
        assertEquals(
            CoreParkingRateCustomValue.EVENING,
            createCoreParkingRateCustomValue(ParkingRateCustomDurationValue.EVENING)
        )
        assertEquals(
            CoreParkingRateCustomValue.FLAT_RATE,
            createCoreParkingRateCustomValue(ParkingRateCustomDurationValue.FLAT_RATE)
        )
        assertEquals(
            CoreParkingRateCustomValue.MAX,
            createCoreParkingRateCustomValue(ParkingRateCustomDurationValue.MAX)
        )
        assertEquals(
            CoreParkingRateCustomValue.MAX_ONLY_ONCE,
            createCoreParkingRateCustomValue(ParkingRateCustomDurationValue.MAX_ONLY_ONCE)
        )
        assertEquals(
            CoreParkingRateCustomValue.MINIMUM,
            createCoreParkingRateCustomValue(ParkingRateCustomDurationValue.MINIMUM)
        )
        assertEquals(
            CoreParkingRateCustomValue.MONTH,
            createCoreParkingRateCustomValue(ParkingRateCustomDurationValue.MONTH)
        )
        assertEquals(
            CoreParkingRateCustomValue.MONTH_MON_FRI,
            createCoreParkingRateCustomValue(ParkingRateCustomDurationValue.MONTH_MON_FRI)
        )
        assertEquals(
            CoreParkingRateCustomValue.MONTH_RESERVED,
            createCoreParkingRateCustomValue(ParkingRateCustomDurationValue.MONTH_RESERVED)
        )
        assertEquals(
            CoreParkingRateCustomValue.MONTH_UNRESERVED,
            createCoreParkingRateCustomValue(ParkingRateCustomDurationValue.MONTH_UNRESERVED)
        )
        assertEquals(
            CoreParkingRateCustomValue.OVERNIGHT,
            createCoreParkingRateCustomValue(ParkingRateCustomDurationValue.OVERNIGHT)
        )
        assertEquals(
            CoreParkingRateCustomValue.QUARTER_MON_FRI,
            createCoreParkingRateCustomValue(ParkingRateCustomDurationValue.QUARTER_MON_FRI)
        )
        assertEquals(
            CoreParkingRateCustomValue.UNTIL_CLOSING,
            createCoreParkingRateCustomValue(ParkingRateCustomDurationValue.UNTIL_CLOSING)
        )
        assertEquals(
            CoreParkingRateCustomValue.WEEKEND,
            createCoreParkingRateCustomValue(ParkingRateCustomDurationValue.WEEKEND)
        )
        assertEquals(
            CoreParkingRateCustomValue.YEAR_MON_FRI,
            createCoreParkingRateCustomValue(ParkingRateCustomDurationValue.YEAR_MON_FRI)
        )
        assertNull(createCoreParkingRateCustomValue("UNKNOWN_CUSTOM_VALUE"))
    }
}
