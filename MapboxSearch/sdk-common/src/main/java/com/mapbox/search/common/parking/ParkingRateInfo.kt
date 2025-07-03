package com.mapbox.search.common.parking

import android.os.Parcelable
import com.mapbox.annotation.MapboxExperimental
import kotlinx.parcelize.Parcelize

/**
 * Rate information for parking facilities.
 */
@MapboxExperimental
@Parcelize
public class ParkingRateInfo(

    /**
     * Currency symbol (e.g., $, €).
     */
    public val currencySymbol: String?,

    /**
     * Currency code (e.g., USD, EUR).
     */
    public val currencyCode: String?,

    /**
     * Parking rates.
     */
    public val rates: List<ParkingRate>?
) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ParkingRateInfo

        if (currencySymbol != other.currencySymbol) return false
        if (currencyCode != other.currencyCode) return false
        if (rates != other.rates) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = currencySymbol?.hashCode() ?: 0
        result = 31 * result + (currencyCode?.hashCode() ?: 0)
        result = 31 * result + (rates?.hashCode() ?: 0)
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "ParkingRateInfo(" +
                "currencySymbol=$currencySymbol, " +
                "currencyCode=$currencyCode, " +
                "rates=$rates" +
                ")"
    }
}
