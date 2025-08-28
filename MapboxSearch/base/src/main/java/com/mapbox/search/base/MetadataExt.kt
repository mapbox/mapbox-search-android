@file:OptIn(MapboxExperimental::class)

package com.mapbox.search.base

import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.base.core.CoreChildMetadata
import com.mapbox.search.base.core.CoreOpenHours
import com.mapbox.search.base.core.CoreOpenMode
import com.mapbox.search.base.core.CoreOpenPeriod
import com.mapbox.search.base.core.CoreParkingData
import com.mapbox.search.base.core.createCoreOpenHours
import com.mapbox.search.base.utils.printableName
import com.mapbox.search.common.metadata.ChildMetadata
import com.mapbox.search.common.metadata.OpenHours
import com.mapbox.search.common.metadata.OpenPeriod
import com.mapbox.search.common.metadata.ParkingData
import com.mapbox.search.common.metadata.WeekDay
import com.mapbox.search.common.metadata.WeekTimestamp

fun CoreOpenHours.mapToPlatform(): OpenHours? = when (mode) {
    CoreOpenMode.ALWAYS_OPEN -> OpenHours.AlwaysOpen
    CoreOpenMode.TEMPORARILY_CLOSED -> OpenHours.TemporaryClosed
    CoreOpenMode.PERMANENTLY_CLOSED -> OpenHours.PermanentlyClosed
    CoreOpenMode.SCHEDULED -> {
        val periods = periods.mapNotNull { it.mapToPlatform() }
        if (periods.isEmpty()) {
            failDebug {
                "CoreOpenHours type is SCHEDULED, but periods is empty"
            }
            null
        } else {
            OpenHours.Scheduled(periods = periods, weekdayText = weekdayText, note)
        }
    }
}

fun OpenHours.mapToCore() = when (this) {
    OpenHours.AlwaysOpen -> createCoreOpenHours(CoreOpenMode.ALWAYS_OPEN, emptyList())
    OpenHours.TemporaryClosed -> createCoreOpenHours(CoreOpenMode.TEMPORARILY_CLOSED, emptyList())
    OpenHours.PermanentlyClosed -> createCoreOpenHours(CoreOpenMode.PERMANENTLY_CLOSED, emptyList())
    is OpenHours.Scheduled -> createCoreOpenHours(
        CoreOpenMode.SCHEDULED,
        periods.map { it.mapToCore() },
        weekdayText,
        note,
    )
    else -> error("Unknown OpenHours subclass: ${javaClass.printableName}.")
}

fun createWeekTimestamp(day: WeekDay, hour: Int, minute: Int): WeekTimestamp? {
    if (hour !in 0..24) {
        failDebug {
            "Hour should be specified in [0..24] range."
        }
        return null
    }

    if (minute !in 0..59) {
        failDebug {
            "Minute should be specified in [0..60) range."
        }
        return null
    }

    if ((hour * 60 + minute) !in 0..1440) {
        failDebug {
            "There can't be $hour hours and $minute minutes in the day."
        }
        return null
    }
    return WeekTimestamp(day, hour, minute)
}

fun CoreOpenPeriod.mapToPlatform(): OpenPeriod? {
    return OpenPeriod(
        open = createWeekTimestamp(
            day = weekDayFromCore(openD) ?: return null,
            hour = openH.toInt(),
            minute = openM.toInt()
        ) ?: return null,
        closed = createWeekTimestamp(
            day = weekDayFromCore(closedD) ?: return null,
            hour = closedH.toInt(),
            minute = closedM.toInt()
        ) ?: return null,
    )
}

fun OpenPeriod.mapToCore() = CoreOpenPeriod(
    open.day.internalRawCode, open.hour.toByte(), open.minute.toByte(),
    closed.day.internalRawCode, closed.hour.toByte(), closed.minute.toByte(),
)

fun CoreParkingData.mapToPlatform() = ParkingData(
    totalCapacity = capacity,
    reservedForDisabilities = forDisabilities
)

fun ParkingData.mapToCore() = CoreParkingData(
    totalCapacity,
    reservedForDisabilities
)

fun CoreChildMetadata.mapToPlatform() = ChildMetadata(
    mapboxId,
    name,
    category,
    coordinates
)

fun ChildMetadata.mapToCore() = CoreChildMetadata(
    category,
    coordinates,
    mapboxId,
    name
)

fun weekDayFromCore(dayCode: Byte): WeekDay? {
    val result = WeekDay.values().firstOrNull { it.internalRawCode == dayCode }
    assertDebug(result != null) {
        "Unknown day code (=$dayCode) from Core SDK."
    }
    return result
}
