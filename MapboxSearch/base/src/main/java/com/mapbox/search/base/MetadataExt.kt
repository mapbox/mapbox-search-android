package com.mapbox.search.base

import com.mapbox.search.base.core.CoreChildMetadata
import com.mapbox.search.base.core.CoreImageInfo
import com.mapbox.search.base.core.CoreOpenHours
import com.mapbox.search.base.core.CoreOpenMode
import com.mapbox.search.base.core.CoreOpenPeriod
import com.mapbox.search.base.core.CoreParkingData
import com.mapbox.search.base.core.createCoreOpenHours
import com.mapbox.search.base.utils.printableName
import com.mapbox.search.common.metadata.ChildMetadata
import com.mapbox.search.common.metadata.ImageInfo
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
        val periods = periods.map { it.mapToPlatform() }
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

fun CoreOpenPeriod.mapToPlatform() = OpenPeriod(
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

fun weekDayFromCore(dayCode: Byte): WeekDay {
    return WeekDay.values().firstOrNull { it.internalRawCode == dayCode }
        ?: throw IllegalArgumentException("Unknown day code (=$dayCode) from Core SDK.")
}

fun CoreImageInfo.mapToPlatform(): ImageInfo = ImageInfo(
    url = url,
    width = width,
    height = height
)

fun ImageInfo.mapToCore(): CoreImageInfo = CoreImageInfo(
    url,
    width,
    height
)
