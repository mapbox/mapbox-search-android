package com.mapbox.search.common

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Address country information
 */
@Parcelize
public class SearchAddressCountry(

    /**
     * Country name
     */
    public val name: String,

    /**
     * Country ISO 3166-1 alpha 2 code
     */
    public val isoCodeAlpha2: String?,

    /**
     * Country ISO 3166-1 alpha 3 code
     */
    public val isoCodeAlpha3: String?
) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SearchAddressCountry

        if (name != other.name) return false
        if (isoCodeAlpha2 != other.isoCodeAlpha2) return false
        if (isoCodeAlpha3 != other.isoCodeAlpha3) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + (isoCodeAlpha2?.hashCode() ?: 0)
        result = 31 * result + (isoCodeAlpha3?.hashCode() ?: 0)
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "SearchAddressCountry(" +
                "name='$name', " +
                "isoCodeAlpha2=$isoCodeAlpha2, " +
                "isoCodeAlpha3=$isoCodeAlpha3" +
                ")"
    }
}
