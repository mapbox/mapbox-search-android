package com.mapbox.search.discover

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Class representing address of a place returned by the [Discover].
 */
@Parcelize
public class DiscoverAddress internal constructor(

    /**
     * Address house number.
     */
    public val houseNumber: String?,

    /**
     * Address street.
     */
    public val street: String?,

    /**
     * Address neighborhood.
     */
    public val neighborhood: String?,

    /**
     * Address locality.
     */
    public val locality: String?,

    /**
     * Address postcode.
     */
    public val postcode: String?,

    /**
     * Address place.
     */
    public val place: String?,

    /**
     * Address district.
     */
    public val district: String?,

    /**
     * Address region.
     */
    public val region: String?,

    /**
     * Address country.
     */
    public val country: String?,

    /**
     * The country code in ISO 3166-1.
     */
    public val countryIso1: String?,

    /**
     * The country code and its country subdivision code in ISO 3166-2.
     */
    public val countryIso2: String?,
) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DiscoverAddress

        if (houseNumber != other.houseNumber) return false
        if (street != other.street) return false
        if (neighborhood != other.neighborhood) return false
        if (locality != other.locality) return false
        if (postcode != other.postcode) return false
        if (place != other.place) return false
        if (district != other.district) return false
        if (region != other.region) return false
        if (country != other.country) return false
        if (countryIso1 != other.countryIso1) return false
        if (countryIso2 != other.countryIso2) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = houseNumber?.hashCode() ?: 0
        result = 31 * result + (street?.hashCode() ?: 0)
        result = 31 * result + (neighborhood?.hashCode() ?: 0)
        result = 31 * result + (locality?.hashCode() ?: 0)
        result = 31 * result + (postcode?.hashCode() ?: 0)
        result = 31 * result + (place?.hashCode() ?: 0)
        result = 31 * result + (district?.hashCode() ?: 0)
        result = 31 * result + (region?.hashCode() ?: 0)
        result = 31 * result + (country?.hashCode() ?: 0)
        result = 31 * result + (countryIso1?.hashCode() ?: 0)
        result = 31 * result + (countryIso2?.hashCode() ?: 0)
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "DiscoverApiAddress(" +
                "houseNumber=$houseNumber, " +
                "street=$street, " +
                "neighborhood=$neighborhood, " +
                "locality=$locality, " +
                "postcode=$postcode, " +
                "place=$place, " +
                "district=$district, " +
                "region=$region, " +
                "country=$country, " +
                "countryIso1=$countryIso1, " +
                "countryIso2=$countryIso2" +
                ")"
    }
}
