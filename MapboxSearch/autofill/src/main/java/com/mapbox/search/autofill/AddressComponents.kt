package com.mapbox.search.autofill

import android.os.Parcelable
import com.mapbox.search.base.core.CoreResultMetadata
import com.mapbox.search.base.core.countryIso1
import com.mapbox.search.base.core.countryIso2
import com.mapbox.search.base.result.BaseSearchAddress
import kotlinx.parcelize.Parcelize

/**
 * Search result address. It's guaranteed that at least one address component is not empty.
 */
@Parcelize
public class AddressComponents private constructor(
    private val coreSdkAddress: BaseSearchAddress,
    private val coreMetadata: CoreResultMetadata?,
) : Parcelable {

    init {
        check(!coreSdkAddress.isEmpty)
    }

    /**
     * Address house number.
     */
    public val houseNumber: String?
        get() = coreSdkAddress.houseNumber

    /**
     * Address street.
     */
    public val street: String?
        get() = coreSdkAddress.street

    /**
     * Address neighborhood.
     */
    public val neighborhood: String?
        get() = coreSdkAddress.neighborhood

    /**
     * Address locality.
     */
    public val locality: String?
        get() = coreSdkAddress.locality

    /**
     * Address postcode.
     */
    public val postcode: String?
        get() = coreSdkAddress.postcode

    /**
     * Address place.
     */
    public val place: String?
        get() = coreSdkAddress.place

    /**
     * Address district.
     */
    public val district: String?
        get() = coreSdkAddress.district

    /**
     * Address region.
     */
    public val region: String?
        get() = coreSdkAddress.region

    /**
     * Address country.
     */
    public val country: String?
        get() = coreSdkAddress.country

    /**
     * The country code in ISO 3166-1.
     */
    public val countryIso1: String?
        get() = coreMetadata?.countryIso1

    /**
     * The country code and its country subdivision code in ISO 3166-2.
     */
    public val countryIso2: String?
        get() = coreMetadata?.countryIso2

    @JvmSynthetic
    internal fun formattedAddress(): String {
        return listOf(
            houseNumber,
            street,
            neighborhood,
            locality,
            postcode,
            place,
            district,
            region,
            country,
        ).filter { !it.isNullOrEmpty() }.joinToString()
    }

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AddressComponents

        if (coreSdkAddress != other.coreSdkAddress) return false
        if (coreMetadata != other.coreMetadata) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = coreSdkAddress.hashCode()
        result = 31 * result + (coreMetadata?.hashCode() ?: 0)
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "AddressComponents(" +
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

    internal companion object {

        @JvmSynthetic
        fun fromCoreSdkAddress(
            address: BaseSearchAddress?,
            metadata: CoreResultMetadata?
        ): AddressComponents? = if (address == null || address.isEmpty) {
            null
        } else {
            AddressComponents(address, metadata)
        }

        private val BaseSearchAddress.isEmpty: Boolean
            get() {
                return listOf(
                    houseNumber,
                    street,
                    neighborhood,
                    locality,
                    postcode,
                    place,
                    district,
                    region,
                    country,
                ).all { it.isNullOrEmpty() }
            }
    }
}
