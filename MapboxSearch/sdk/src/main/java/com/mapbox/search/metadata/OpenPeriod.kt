package com.mapbox.search.metadata

import com.mapbox.search.core.CoreOpenPeriod

/**
 * Time interval, when POI is available.
 */
public data class OpenPeriod(

    /**
     * Time, when POI opens.
     */
    public val open: WeekTimestamp,

    /**
     * Time, when POI closes.
     */
    public val closed: WeekTimestamp
)

@JvmSynthetic
internal fun CoreOpenPeriod.mapToPlatform() = OpenPeriod(
    open = WeekTimestamp(
        day = WeekDay.fromCore(openD),
        hour = openH.toInt(),
        minute = openM.toInt()
    ),
    closed = WeekTimestamp(
        day = WeekDay.fromCore(closedD),
        hour = closedH.toInt(),
        minute = closedM.toInt()
    ),
)

@JvmSynthetic
internal fun OpenPeriod.mapToCore() = CoreOpenPeriod(
    open.day.toCore(), open.hour.toByte(), open.minute.toByte(),
    closed.day.toCore(), closed.hour.toByte(), closed.minute.toByte(),
)
