@file:OptIn(MapboxExperimental::class)

package com.mapbox.search.base.factory.parking

import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.base.core.CoreParkingAvailabilityLevel
import com.mapbox.search.base.core.CoreParkingInfo
import com.mapbox.search.base.core.CoreParkingPaymentMethod
import com.mapbox.search.base.core.CoreParkingPaymentType
import com.mapbox.search.base.core.CoreParkingPriceType
import com.mapbox.search.base.core.CoreParkingRate
import com.mapbox.search.base.core.CoreParkingRateCustomValue
import com.mapbox.search.base.core.CoreParkingRateInfo
import com.mapbox.search.base.core.CoreParkingRatePrice
import com.mapbox.search.base.core.CoreParkingRateTime
import com.mapbox.search.base.core.CoreParkingRateValue
import com.mapbox.search.base.core.CoreParkingRestriction
import com.mapbox.search.base.core.CoreParkingTrend
import com.mapbox.search.common.metadata.WeekDay
import com.mapbox.search.common.parking.ParkingAvailabilityLevel
import com.mapbox.search.common.parking.ParkingInfo
import com.mapbox.search.common.parking.ParkingPaymentMethod
import com.mapbox.search.common.parking.ParkingPaymentType
import com.mapbox.search.common.parking.ParkingPriceType
import com.mapbox.search.common.parking.ParkingRate
import com.mapbox.search.common.parking.ParkingRateCustomDurationValue
import com.mapbox.search.common.parking.ParkingRateInfo
import com.mapbox.search.common.parking.ParkingRatePrice
import com.mapbox.search.common.parking.ParkingRateTime
import com.mapbox.search.common.parking.ParkingRateValue
import com.mapbox.search.common.parking.ParkingRestriction
import com.mapbox.search.common.parking.ParkingTrend
import com.mapbox.search.common.tests.CustomTypeObjectCreatorImpl

internal object TestTypes {

    val PLATFORM_PARKING_RATE_TIME = ParkingRateTime(
        days = listOf(WeekDay.MONDAY, WeekDay.WEDNESDAY, WeekDay.FRIDAY),
        fromHour = 7,
        fromMinute = 30,
        toHour = 19,
        toMinute = 45,
    )

    val CORE_PARKING_RATE_TIME = CoreParkingRateTime(
        days = listOf(0, 2, 4),
        fromHour = 7,
        fromMinute = 30,
        toHour = 19,
        toMinute = 45,
    )

    val PLATFORM_PARKING_RATE_PRICE = ParkingRatePrice(
        type = ParkingPriceType.DURATION,
        amount = 15.5,
        value = ParkingRateValue.CustomDurationValue(ParkingRateCustomDurationValue.DAYTIME),
    )

    val CORE_PARKING_RATE_PRICE = CoreParkingRatePrice(
        type = CoreParkingPriceType.DURATION,
        amount = 15.5,
        value = CoreParkingRateValue.valueOf(CoreParkingRateCustomValue.DAYTIME),
    )

    val PLATFORM_PARKING_RATE = ParkingRate(
        maxStay = "PT6M",
        times = listOf(PLATFORM_PARKING_RATE_TIME),
        prices = listOf(PLATFORM_PARKING_RATE_PRICE)
    )

    val CORE_PARKING_RATE = CoreParkingRate(
        maxStay = "PT6M",
        times = listOf(CORE_PARKING_RATE_TIME),
        prices = listOf(CORE_PARKING_RATE_PRICE)
    )

    val PLATFORM_PARKING_RATE_INFO = ParkingRateInfo(
        currencySymbol = "€",
        currencyCode = "EUR",
        rates = listOf(PLATFORM_PARKING_RATE),
    )

    val CORE_PARKING_RATE_INFO = CoreParkingRateInfo(
        currencySymbol = "€",
        currencyCode = "EUR",
        rates = listOf(CORE_PARKING_RATE),
    )

    val PLATFORM_PARKING_INFO = ParkingInfo(
        capacity = 123,
        rateInfo = PLATFORM_PARKING_RATE_INFO,
        availability = 50,
        availabilityLevel = ParkingAvailabilityLevel.MID,
        availabilityUpdatedAt = "test-time",
        trend = ParkingTrend.INCREASING,
        paymentMethods = listOf(ParkingPaymentMethod.PARKING_METER, ParkingPaymentMethod.MULTI_SPACE_METER),
        paymentTypes = listOf(ParkingPaymentType.CARDS, ParkingPaymentType.COINS),
        restrictions = listOf(ParkingRestriction.NO_LPG, ParkingRestriction.CUSTOMERS)
    )

    val CORE_PARKING_INFO = CoreParkingInfo(
        capacity = 123,
        rateInfo = CORE_PARKING_RATE_INFO,
        availability = 50,
        availabilityLevel = CoreParkingAvailabilityLevel.MID,
        availabilityAt = "test-time",
        trend = CoreParkingTrend.INCREASING,
        paymentMethods = listOf(CoreParkingPaymentMethod.PARKING_METER, CoreParkingPaymentMethod.MULTI_SPACE_METER),
        paymentTypes = listOf(CoreParkingPaymentType.CARDS, CoreParkingPaymentType.COINS),
        restrictions = listOf(CoreParkingRestriction.NO_LPG, CoreParkingRestriction.CUSTOMERS)
    )

    val PARKING_RATE_PRICE_CREATOR = CustomTypeObjectCreatorImpl(ParkingRateValue::class) { mode ->
        listOf(
            ParkingRateValue.IsoValue("test"),
            ParkingRateValue.CustomDurationValue(ParkingRateCustomDurationValue.DAYTIME),
        )[mode.ordinal]
    }
}
