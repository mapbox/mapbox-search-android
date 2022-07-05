package com.mapbox.search.autofill

import android.os.Parcelable
import com.mapbox.search.result.SearchAddress
import kotlinx.parcelize.Parcelize

/**
 * Search result address. It's guaranteed that at least one address component is not empty.
 */
@Parcelize
public class AddressComponents private constructor(private val coreSdkAddress: SearchAddress) : Parcelable {

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

    @JvmSynthetic
    internal fun formattedAddress(): String {
        return coreSdkAddress.formattedAddress(SearchAddress.FormatStyle.Full) ?: toString()
    }

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AddressComponents

        if (coreSdkAddress != other.coreSdkAddress) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        return coreSdkAddress.hashCode()
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
                "country=$country" +
                ")"
    }

    internal companion object {

        @JvmSynthetic
        fun fromCoreSdkAddress(address: SearchAddress?): AddressComponents? = if (address == null || address.isEmpty) {
            null
        } else {
            AddressComponents(address)
        }

        private val SearchAddress.isEmpty: Boolean
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
