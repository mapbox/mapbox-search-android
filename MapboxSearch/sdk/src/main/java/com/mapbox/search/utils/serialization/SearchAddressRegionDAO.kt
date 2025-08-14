package com.mapbox.search.utils.serialization

import com.google.gson.annotations.SerializedName
import com.mapbox.search.common.SearchAddressRegion

internal data class SearchAddressRegionDAO(
    @SerializedName("name") val name: String?,
    @SerializedName("code") val code: String?,
    @SerializedName("codeFull") val codeFull: String?
) : DataAccessObject<SearchAddressRegion> {

    override val isValid: Boolean
        get() = name != null

    override fun createData(): SearchAddressRegion {
        return SearchAddressRegion(
            name = name!!,
            code = code,
            codeFull = codeFull,
        )
    }

    companion object {

        fun create(searchAddressRegion: SearchAddressRegion?): SearchAddressRegionDAO? {
            searchAddressRegion ?: return null
            return with(searchAddressRegion) {
                SearchAddressRegionDAO(
                    name = name,
                    code = code,
                    codeFull = codeFull,
                )
            }
        }
    }
}
