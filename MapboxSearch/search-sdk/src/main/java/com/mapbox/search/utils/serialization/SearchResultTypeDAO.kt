package com.mapbox.search.utils.serialization

import com.google.gson.annotations.SerializedName
import com.mapbox.search.base.failDebug
import com.mapbox.search.result.NewSearchResultType

internal enum class SearchResultTypeDAO : DataAccessObject<String> {

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
    BLOCK,
    @SerializedName("UNKNOWN")
    UNKNOWN;

    override val isValid: Boolean
        get() = true

    @NewSearchResultType.Type
    override fun createData(): String {
        return when (this) {
            ADDRESS -> NewSearchResultType.ADDRESS
            POI -> NewSearchResultType.POI
            COUNTRY -> NewSearchResultType.COUNTRY
            REGION -> NewSearchResultType.REGION
            PLACE -> NewSearchResultType.PLACE
            DISTRICT -> NewSearchResultType.DISTRICT
            LOCALITY -> NewSearchResultType.LOCALITY
            NEIGHBORHOOD -> NewSearchResultType.NEIGHBORHOOD
            STREET -> NewSearchResultType.STREET
            POSTCODE -> NewSearchResultType.POSTCODE
            BLOCK -> NewSearchResultType.BLOCK
            UNKNOWN -> NewSearchResultType.UNKNOWN
        }
    }

    companion object {

        fun create(@NewSearchResultType.Type type: String): SearchResultTypeDAO {
            return when (type) {
                NewSearchResultType.ADDRESS -> ADDRESS
                NewSearchResultType.POI -> POI
                NewSearchResultType.COUNTRY -> COUNTRY
                NewSearchResultType.REGION -> REGION
                NewSearchResultType.PLACE -> PLACE
                NewSearchResultType.DISTRICT -> DISTRICT
                NewSearchResultType.LOCALITY -> LOCALITY
                NewSearchResultType.NEIGHBORHOOD -> NEIGHBORHOOD
                NewSearchResultType.STREET -> STREET
                NewSearchResultType.POSTCODE -> POSTCODE
                NewSearchResultType.BLOCK -> BLOCK
                NewSearchResultType.UNKNOWN -> UNKNOWN
                else -> {
                    failDebug {
                        "Unknown type: $type"
                    }
                    UNKNOWN
                }
            }
        }
    }
}
