package com.mapbox.search.utils.serialization

import com.google.gson.annotations.SerializedName
import com.mapbox.search.result.SearchAddress

internal data class SearchAddressDAO(
    @SerializedName("houseNumber") val houseNumber: String? = null,
    @SerializedName("street") val street: String? = null,
    @SerializedName("neighborhood") val neighborhood: String? = null,
    @SerializedName("locality") val locality: String? = null,
    @SerializedName("postcode") val postcode: String? = null,
    @SerializedName("place") val place: String? = null,
    @SerializedName("district") val district: String? = null,
    @SerializedName("region") val region: String? = null,
    @SerializedName("country") val country: String? = null
) : DataAccessObject<SearchAddress> {

    override val isValid: Boolean
        get() = true

    override fun createData(): SearchAddress {
        return SearchAddress(
            houseNumber = houseNumber,
            street = street,
            neighborhood = neighborhood,
            locality = locality,
            postcode = postcode,
            place = place,
            district = district,
            region = region,
            country = country
        )
    }

    companion object {

        fun create(searchAddress: SearchAddress?): SearchAddressDAO? {
            searchAddress ?: return null
            return with(searchAddress) {
                SearchAddressDAO(
                    houseNumber = houseNumber,
                    street = street,
                    neighborhood = neighborhood,
                    locality = locality,
                    postcode = postcode,
                    place = place,
                    district = district,
                    region = region,
                    country = country
                )
            }
        }
    }
}
