package com.mapbox.search.common.parking

import android.os.Parcelable
import com.mapbox.annotation.MapboxExperimental
import kotlinx.parcelize.Parcelize

/**
 * Individual parking rate.
 */
@MapboxExperimental
@Parcelize
public class ParkingRate(

    /**
     * Maximum stay time in ISO 8601 duration format,
     * e.g., "PT6M" for 6 minutes, "PT2H30M" for 2.5 hours.
     */
    public val maxStay: String?,

    /**
     * Time periods when this rate applies.
     */
    public val times: List<ParkingRateTime>?,

    /**
     * Pricing information.
     */
    public val prices: List<ParkingRatePrice>?,
) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ParkingRate

        if (maxStay != other.maxStay) return false
        if (times != other.times) return false
        if (prices != other.prices) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = maxStay?.hashCode() ?: 0
        result = 31 * result + (times?.hashCode() ?: 0)
        result = 31 * result + (prices?.hashCode() ?: 0)
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "ParkingRate(maxStay=$maxStay, times=$times, prices=$prices)"
    }
}
