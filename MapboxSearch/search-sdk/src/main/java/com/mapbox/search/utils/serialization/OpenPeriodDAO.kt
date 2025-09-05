package com.mapbox.search.utils.serialization

import com.google.gson.annotations.SerializedName
import com.mapbox.search.common.metadata.OpenPeriod
import com.mapbox.search.common.metadata.WeekTimestamp

internal class OpenPeriodDAO(
    @SerializedName("openWeekDay") val openWeekDay: WeekDayDAO? = null,
    @SerializedName("openHour") val openHour: Int? = null,
    @SerializedName("openMinute") val openMinute: Int? = null,
    @SerializedName("closedWeekDay") val closedWeekDay: WeekDayDAO? = null,
    @SerializedName("closedHour") val closedHour: Int? = null,
    @SerializedName("closedMinute") val closedMinute: Int? = null,
) : DataAccessObject<OpenPeriod> {

    override val isValid: Boolean
        get() = openWeekDay != null && openHour != null && openMinute != null &&
                closedWeekDay != null && closedHour != null && closedMinute != null

    override fun createData(): OpenPeriod {
        return OpenPeriod(
            open = WeekTimestamp(openWeekDay!!.createData(), openHour!!, openMinute!!),
            closed = WeekTimestamp(closedWeekDay!!.createData(), closedHour!!, closedMinute!!)
        )
    }

    companion object {

        fun create(openPeriod: OpenPeriod): OpenPeriodDAO {
            return with(openPeriod) {
                OpenPeriodDAO(
                    openWeekDay = WeekDayDAO.create(open.day),
                    openHour = open.hour,
                    openMinute = open.minute,
                    closedWeekDay = WeekDayDAO.create(closed.day),
                    closedHour = closed.hour,
                    closedMinute = closed.minute,
                )
            }
        }
    }
}
