@file:OptIn(MapboxExperimental::class)

package com.mapbox.search.base.factory.parking

import androidx.annotation.RestrictTo
import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.base.core.CoreParkingRateCustomValue
import com.mapbox.search.common.parking.ParkingRateCustomDurationValue

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
@ParkingRateCustomDurationValue.Type
fun CoreParkingRateCustomValue.mapToPlatform(): String {
    return when (this) {
        CoreParkingRateCustomValue.SIX_MONTHS_MON_FRI -> ParkingRateCustomDurationValue.SIX_MONTHS_MON_FRI
        CoreParkingRateCustomValue.BANK_HOLIDAY -> ParkingRateCustomDurationValue.BANK_HOLIDAY
        CoreParkingRateCustomValue.DAYTIME -> ParkingRateCustomDurationValue.DAYTIME
        CoreParkingRateCustomValue.EARLY_BIRD -> ParkingRateCustomDurationValue.EARLY_BIRD
        CoreParkingRateCustomValue.EVENING -> ParkingRateCustomDurationValue.EVENING
        CoreParkingRateCustomValue.FLAT_RATE -> ParkingRateCustomDurationValue.FLAT_RATE
        CoreParkingRateCustomValue.MAX -> ParkingRateCustomDurationValue.MAX
        CoreParkingRateCustomValue.MAX_ONLY_ONCE -> ParkingRateCustomDurationValue.MAX_ONLY_ONCE
        CoreParkingRateCustomValue.MINIMUM -> ParkingRateCustomDurationValue.MINIMUM
        CoreParkingRateCustomValue.MONTH -> ParkingRateCustomDurationValue.MONTH
        CoreParkingRateCustomValue.MONTH_MON_FRI -> ParkingRateCustomDurationValue.MONTH_MON_FRI
        CoreParkingRateCustomValue.MONTH_RESERVED -> ParkingRateCustomDurationValue.MONTH_RESERVED
        CoreParkingRateCustomValue.MONTH_UNRESERVED -> ParkingRateCustomDurationValue.MONTH_UNRESERVED
        CoreParkingRateCustomValue.OVERNIGHT -> ParkingRateCustomDurationValue.OVERNIGHT
        CoreParkingRateCustomValue.QUARTER_MON_FRI -> ParkingRateCustomDurationValue.QUARTER_MON_FRI
        CoreParkingRateCustomValue.UNTIL_CLOSING -> ParkingRateCustomDurationValue.UNTIL_CLOSING
        CoreParkingRateCustomValue.WEEKEND -> ParkingRateCustomDurationValue.WEEKEND
        CoreParkingRateCustomValue.YEAR_MON_FRI -> ParkingRateCustomDurationValue.YEAR_MON_FRI
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun createCoreParkingRateCustomValue(@ParkingRateCustomDurationValue.Type type: String?): CoreParkingRateCustomValue? {
    return when (type) {
        ParkingRateCustomDurationValue.SIX_MONTHS_MON_FRI -> CoreParkingRateCustomValue.SIX_MONTHS_MON_FRI
        ParkingRateCustomDurationValue.BANK_HOLIDAY -> CoreParkingRateCustomValue.BANK_HOLIDAY
        ParkingRateCustomDurationValue.DAYTIME -> CoreParkingRateCustomValue.DAYTIME
        ParkingRateCustomDurationValue.EARLY_BIRD -> CoreParkingRateCustomValue.EARLY_BIRD
        ParkingRateCustomDurationValue.EVENING -> CoreParkingRateCustomValue.EVENING
        ParkingRateCustomDurationValue.FLAT_RATE -> CoreParkingRateCustomValue.FLAT_RATE
        ParkingRateCustomDurationValue.MAX -> CoreParkingRateCustomValue.MAX
        ParkingRateCustomDurationValue.MAX_ONLY_ONCE -> CoreParkingRateCustomValue.MAX_ONLY_ONCE
        ParkingRateCustomDurationValue.MINIMUM -> CoreParkingRateCustomValue.MINIMUM
        ParkingRateCustomDurationValue.MONTH -> CoreParkingRateCustomValue.MONTH
        ParkingRateCustomDurationValue.MONTH_MON_FRI -> CoreParkingRateCustomValue.MONTH_MON_FRI
        ParkingRateCustomDurationValue.MONTH_RESERVED -> CoreParkingRateCustomValue.MONTH_RESERVED
        ParkingRateCustomDurationValue.MONTH_UNRESERVED -> CoreParkingRateCustomValue.MONTH_UNRESERVED
        ParkingRateCustomDurationValue.OVERNIGHT -> CoreParkingRateCustomValue.OVERNIGHT
        ParkingRateCustomDurationValue.QUARTER_MON_FRI -> CoreParkingRateCustomValue.QUARTER_MON_FRI
        ParkingRateCustomDurationValue.UNTIL_CLOSING -> CoreParkingRateCustomValue.UNTIL_CLOSING
        ParkingRateCustomDurationValue.WEEKEND -> CoreParkingRateCustomValue.WEEKEND
        ParkingRateCustomDurationValue.YEAR_MON_FRI -> CoreParkingRateCustomValue.YEAR_MON_FRI
        else -> null
    }
}
