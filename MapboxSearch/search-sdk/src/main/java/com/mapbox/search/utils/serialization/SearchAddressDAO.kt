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
    @SerializedName("country") val country: String? = null,
    @SerializedName("regionInfo") val regionInfo: SearchAddressRegionDAO? = null,
    @SerializedName("countryInfo") val countryInfo: SearchAddressCountryDAO? = null,
) : DataAccessObject<SearchAddress> {

    override val isValid: Boolean
        get() = (regionInfo == null || regionInfo.isValid) &&
                (countryInfo == null || countryInfo.isValid)

    override fun createData(): SearchAddress {
        val resultingRegionInfo = when {
            regionInfo != null -> regionInfo
            region != null -> SearchAddressRegionDAO(region, null, null)
            else -> null
        }

        val resultingCountryInfo = when {
            countryInfo != null -> countryInfo
            country != null -> SearchAddressCountryDAO(country, null, null)
            else -> null
        }

        return SearchAddress(
            houseNumber = houseNumber,
            street = street,
            neighborhood = neighborhood,
            locality = locality,
            postcode = postcode,
            place = place,
            district = district,
            region = region,
            country = country,
            regionInfo = resultingRegionInfo?.createData(),
            countryInfo = resultingCountryInfo?.createData(),
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
                    country = country,
                    regionInfo = SearchAddressRegionDAO.create(regionInfo),
                    countryInfo = SearchAddressCountryDAO.create(countryInfo),
                )
            }
        }
    }
}
