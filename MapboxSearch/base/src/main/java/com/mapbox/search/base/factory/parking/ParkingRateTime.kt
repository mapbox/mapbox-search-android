@file:OptIn(MapboxExperimental::class)

package com.mapbox.search.base.factory.parking

import androidx.annotation.RestrictTo
import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.base.core.CoreParkingRateTime
import com.mapbox.search.base.weekDayFromCore
import com.mapbox.search.common.parking.ParkingRateTime

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun CoreParkingRateTime.mapToPlatform(): ParkingRateTime {
    return ParkingRateTime(
        days = days?.map {
            weekDayFromCore(it)
        },
        fromHour = fromHour,
        fromMinute = fromMinute,
        toHour = toHour,
        toMinute = toMinute,
    )
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun ParkingRateTime.mapToCore(): CoreParkingRateTime {
    return CoreParkingRateTime(
        days = days?.map { it.internalRawCode },
        fromHour = fromHour,
        fromMinute = fromMinute,
        toHour = toHour,
        toMinute = toMinute,
    )
}
