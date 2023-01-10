package com.mapbox.search.base

import com.mapbox.search.base.core.CoreOpenHours
import com.mapbox.search.base.core.CoreOpenMode
import com.mapbox.search.base.core.CoreOpenPeriod
import com.mapbox.search.base.core.CoreParkingData
import com.mapbox.search.base.utils.printableName
import com.mapbox.search.common.metadata.OpenHours
import com.mapbox.search.common.metadata.OpenPeriod
import com.mapbox.search.common.metadata.ParkingData
import com.mapbox.search.common.metadata.WeekDay
import com.mapbox.search.common.metadata.WeekTimestamp

public fun CoreOpenHours.mapToPlatform() = when (mode) {
    CoreOpenMode.ALWAYS_OPEN -> OpenHours.AlwaysOpen
    CoreOpenMode.TEMPORARILY_CLOSED -> OpenHours.TemporaryClosed
    CoreOpenMode.PERMANENTLY_CLOSED -> OpenHours.PermanentlyClosed
    CoreOpenMode.SCHEDULED -> OpenHours.Scheduled(periods = periods.map { it.mapToPlatform() })
}

public fun OpenHours.mapToCore() = when (this) {
    OpenHours.AlwaysOpen -> CoreOpenHours(CoreOpenMode.ALWAYS_OPEN, emptyList())
    OpenHours.TemporaryClosed -> CoreOpenHours(CoreOpenMode.TEMPORARILY_CLOSED, emptyList())
    OpenHours.PermanentlyClosed -> CoreOpenHours(CoreOpenMode.PERMANENTLY_CLOSED, emptyList())
    is OpenHours.Scheduled -> CoreOpenHours(CoreOpenMode.SCHEDULED, periods.map { it.mapToCore() })
    else -> error("Unknown OpenHours subclass: ${javaClass.printableName}.")
}

public fun CoreOpenPeriod.mapToPlatform() = OpenPeriod(
    open = WeekTimestamp(
        day = weekDayFromCore(openD),
        hour = openH.toInt(),
        minute = openM.toInt()
    ),
    closed = WeekTimestamp(
        day = weekDayFromCore(closedD),
        hour = closedH.toInt(),
        minute = closedM.toInt()
    ),
)

public fun OpenPeriod.mapToCore() = CoreOpenPeriod(
    open.day.internalRawCode, open.hour.toByte(), open.minute.toByte(),
    closed.day.internalRawCode, closed.hour.toByte(), closed.minute.toByte(),
)

public fun CoreParkingData.mapToPlatform() = ParkingData(
    totalCapacity = capacity,
    reservedForDisabilities = forDisabilities
)

public fun ParkingData.mapToCore() = CoreParkingData(
    totalCapacity,
    reservedForDisabilities
)

public fun weekDayFromCore(dayCode: Byte): WeekDay {
    return WeekDay.values().firstOrNull { it.internalRawCode == dayCode }
        ?: throw IllegalArgumentException("Unknown day code (=$dayCode) from Core SDK.")
}
