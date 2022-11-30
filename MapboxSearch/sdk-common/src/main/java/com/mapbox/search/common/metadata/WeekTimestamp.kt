package com.mapbox.search.common.metadata

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Specific time in a week span in Local Timezone.
 * Please note, that [hour = 0, minute = 0] represents midnight in morning, [hour = 24, minute = 0] represents midnight at night.
 */
@Parcelize
public data class WeekTimestamp(

    /**
     * Specific day of the week (Monday, Tuesday, ..., Sunday).
     */
    val day: WeekDay,

    /**
     * Specific hour of the day.
     */
    val hour: Int,

    /**
     * Specific minute of the hour.
     */
    val minute: Int
) : Parcelable {

    init {
        require(hour in 0..24) { "Hour should be specified in [0..24] range." }
        require(minute in 0..59) { "Minute should be specified in [0..60) range." }
        require((hour * 60 + minute) in 0..1440) { "There can't be $hour hours and $minute minutes in the day." }
    }
}
