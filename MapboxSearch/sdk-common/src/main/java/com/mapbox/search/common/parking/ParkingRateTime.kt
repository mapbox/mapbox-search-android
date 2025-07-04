package com.mapbox.search.common.parking

import android.os.Parcelable
import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.common.metadata.WeekDay
import kotlinx.parcelize.Parcelize

/**
 * Time period for parking rates
 */
@MapboxExperimental
@Parcelize
public class ParkingRateTime(

    /**
     * Days of Week
     */
    public val days: List<WeekDay>?,

    /**
     * From time hour
     */
    public val fromHour: Byte?,

    /**
     * From time minute
     */
    public val fromMinute: Byte?,

    /**
     * To time hour
     */
    public val toHour: Byte?,

    /**
     * To time minute
     */
    public val toMinute: Byte?
) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ParkingRateTime

        if (days != other.days) return false
        if (fromHour != other.fromHour) return false
        if (fromMinute != other.fromMinute) return false
        if (toHour != other.toHour) return false
        if (toMinute != other.toMinute) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = days?.hashCode() ?: 0
        result = 31 * result + (fromHour ?: 0)
        result = 31 * result + (fromMinute ?: 0)
        result = 31 * result + (toHour ?: 0)
        result = 31 * result + (toMinute ?: 0)
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "ParkingRateTime(" +
                "days=$days, " +
                "fromHour=$fromHour, " +
                "fromMinute=$fromMinute, " +
                "toHour=$toHour, " +
                "toMinute=$toMinute" +
                ")"
    }
}
