package com.mapbox.search.utils.serialization

import com.google.gson.annotations.SerializedName
import com.mapbox.search.result.SearchResultType

internal enum class SearchResultTypeDAO : DataAccessObject<SearchResultType> {

    @SerializedName("ADDRESS")
    ADDRESS,
    @SerializedName("POI")
    POI,
    @SerializedName("COUNTRY")
    COUNTRY,
    @SerializedName("REGION")
    REGION,
    @SerializedName("PLACE")
    PLACE,
    @SerializedName("DISTRICT")
    DISTRICT,
    @SerializedName("LOCALITY")
    LOCALITY,
    @SerializedName("NEIGHBORHOOD")
    NEIGHBORHOOD,
    @SerializedName("STREET")
    STREET,
    @SerializedName("POSTCODE")
    POSTCODE,
    @SerializedName("BLOCK")
    BLOCK;

    override val isValid: Boolean
        get() = true

    override fun createData(): SearchResultType {
        return when (this) {
            ADDRESS -> SearchResultType.ADDRESS
            POI -> SearchResultType.POI
            COUNTRY -> SearchResultType.COUNTRY
            REGION -> SearchResultType.REGION
            PLACE -> SearchResultType.PLACE
            DISTRICT -> SearchResultType.DISTRICT
            LOCALITY -> SearchResultType.LOCALITY
            NEIGHBORHOOD -> SearchResultType.NEIGHBORHOOD
            STREET -> SearchResultType.STREET
            POSTCODE -> SearchResultType.POSTCODE
            BLOCK -> SearchResultType.BLOCK
        }
    }

    companion object {

        fun create(type: SearchResultType): SearchResultTypeDAO {
            return when (type) {
                SearchResultType.ADDRESS -> ADDRESS
                SearchResultType.POI -> POI
                SearchResultType.COUNTRY -> COUNTRY
                SearchResultType.REGION -> REGION
                SearchResultType.PLACE -> PLACE
                SearchResultType.DISTRICT -> DISTRICT
                SearchResultType.LOCALITY -> LOCALITY
                SearchResultType.NEIGHBORHOOD -> NEIGHBORHOOD
                SearchResultType.STREET -> STREET
                SearchResultType.POSTCODE -> POSTCODE
                SearchResultType.BLOCK -> BLOCK
            }
        }
    }
}
