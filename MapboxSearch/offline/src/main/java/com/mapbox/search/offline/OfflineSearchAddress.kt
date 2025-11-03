package com.mapbox.search.offline

import android.os.Parcelable
import com.mapbox.search.base.core.CoreSearchAddress
import com.mapbox.search.base.utils.extension.nullIfEmpty
import kotlinx.parcelize.Parcelize

/**
 * Represents address of the search result.
 */
@Parcelize
public class OfflineSearchAddress internal constructor(

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
     * Address place.
     */
    public val place: String?,

    /**
     * Address region.
     */
    public val region: String?,

    /**
     * Address country.
     */
    public val country: String?
) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OfflineSearchAddress

        if (houseNumber != other.houseNumber) return false
        if (street != other.street) return false
        if (neighborhood != other.neighborhood) return false
        if (locality != other.locality) return false
        if (place != other.place) return false
        if (region != other.region) return false
        if (country != other.country) return false

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
        result = 31 * result + (place?.hashCode() ?: 0)
        result = 31 * result + (region?.hashCode() ?: 0)
        result = 31 * result + (country?.hashCode() ?: 0)
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "OfflineSearchAddress(" +
                "houseNumber=$houseNumber, " +
                "street=$street, " +
                "neighborhood=$neighborhood, " +
                "locality=$locality, " +
                "place=$place, " +
                "region=$region, " +
                "country=$country" +
                ")"
    }
}

@JvmSynthetic
internal fun CoreSearchAddress.mapToOfflineSdkType(): OfflineSearchAddress {
    return OfflineSearchAddress(
        houseNumber = houseNumber?.nullIfEmpty(),
        street = street?.nullIfEmpty(),
        neighborhood = neighborhood?.nullIfEmpty(),
        locality = locality?.nullIfEmpty(),
        place = place?.nullIfEmpty(),
        region = region?.name?.nullIfEmpty(),
        country = country?.name?.nullIfEmpty(),
    )
}
