package com.mapbox.search.common.parking

import android.os.Parcelable
import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.common.safeCompareTo
import kotlinx.parcelize.Parcelize

/**
 * Pricing information for parking rates.
 */
@MapboxExperimental
@Parcelize
public class ParkingRatePrice(

    /**
     * Type of pricing, one of the values described in [ParkingPriceType].
     */
    @ParkingPriceType.Type
    public val type: String?,

    /**
     * Amount to pay.
     */
    public val amount: Double?,

    /**
     * Value associated with the price - either ISO duration string or custom value.
     */
    public val value: ParkingRateValue?
) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ParkingRatePrice

        if (type != other.type) return false
        if (!amount.safeCompareTo(other.amount)) return false
        if (value != other.value) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = type?.hashCode() ?: 0
        result = 31 * result + (amount?.hashCode() ?: 0)
        result = 31 * result + value.hashCode()
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "ParkingRatePrice(type=$type, amount=$amount, value=$value)"
    }
}
