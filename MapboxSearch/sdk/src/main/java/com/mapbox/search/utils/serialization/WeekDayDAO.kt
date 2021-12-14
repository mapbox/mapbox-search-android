package com.mapbox.search.utils.serialization

import com.google.gson.annotations.SerializedName
import com.mapbox.search.metadata.WeekDay

internal enum class WeekDayDAO : DataAccessObject<WeekDay> {

    @SerializedName("MONDAY")
    MONDAY,
    @SerializedName("TUESDAY")
    TUESDAY,
    @SerializedName("WEDNESDAY")
    WEDNESDAY,
    @SerializedName("THURSDAY")
    THURSDAY,
    @SerializedName("FRIDAY")
    FRIDAY,
    @SerializedName("SATURDAY")
    SATURDAY,
    @SerializedName("SUNDAY")
    SUNDAY;

    override val isValid: Boolean
        get() = true

    override fun createData(): WeekDay {
        return when (this) {
            MONDAY -> WeekDay.MONDAY
            TUESDAY -> WeekDay.TUESDAY
            WEDNESDAY -> WeekDay.WEDNESDAY
            THURSDAY -> WeekDay.THURSDAY
            FRIDAY -> WeekDay.FRIDAY
            SATURDAY -> WeekDay.SATURDAY
            SUNDAY -> WeekDay.SUNDAY
        }
    }

    companion object {

        fun create(type: WeekDay): WeekDayDAO {
            return when (type) {
                WeekDay.MONDAY -> MONDAY
                WeekDay.TUESDAY -> TUESDAY
                WeekDay.WEDNESDAY -> WEDNESDAY
                WeekDay.THURSDAY -> THURSDAY
                WeekDay.FRIDAY -> FRIDAY
                WeekDay.SATURDAY -> SATURDAY
                WeekDay.SUNDAY -> SUNDAY
            }
        }
    }
}
