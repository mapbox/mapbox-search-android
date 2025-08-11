package com.mapbox.search.utils.serialization

import com.google.gson.annotations.SerializedName
import com.mapbox.search.common.SearchAddressCountry

internal data class SearchAddressCountryDAO(
    @SerializedName("name") val name: String?,
    @SerializedName("isoCodeAlpha2") val isoCodeAlpha2: String?,
    @SerializedName("isoCodeAlpha3") val isoCodeAlpha3: String?
) : DataAccessObject<SearchAddressCountry> {

    override val isValid: Boolean
        get() = name != null

    override fun createData(): SearchAddressCountry {
        return SearchAddressCountry(
            name = name!!,
            isoCodeAlpha2 = isoCodeAlpha2,
            isoCodeAlpha3 = isoCodeAlpha3,
        )
    }

    companion object {

        fun create(searchAddressCountry: SearchAddressCountry?): SearchAddressCountryDAO? {
            searchAddressCountry ?: return null
            return with(searchAddressCountry) {
                SearchAddressCountryDAO(
                    name = name,
                    isoCodeAlpha2 = isoCodeAlpha2,
                    isoCodeAlpha3 = isoCodeAlpha3,
                )
            }
        }
    }
}
