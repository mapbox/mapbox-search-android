@file:Suppress("DEPRECATION")

package com.mapbox.search.internal

import androidx.annotation.RestrictTo
import com.mapbox.search.base.core.CoreResultType
import com.mapbox.search.base.failDebug
import com.mapbox.search.base.result.BaseSearchResultType
import com.mapbox.search.result.NewSearchResultType.ADDRESS
import com.mapbox.search.result.NewSearchResultType.BLOCK
import com.mapbox.search.result.NewSearchResultType.COUNTRY
import com.mapbox.search.result.NewSearchResultType.DISTRICT
import com.mapbox.search.result.NewSearchResultType.LOCALITY
import com.mapbox.search.result.NewSearchResultType.NEIGHBORHOOD
import com.mapbox.search.result.NewSearchResultType.PLACE
import com.mapbox.search.result.NewSearchResultType.POI
import com.mapbox.search.result.NewSearchResultType.POSTCODE
import com.mapbox.search.result.NewSearchResultType.REGION
import com.mapbox.search.result.NewSearchResultType.STREET
import com.mapbox.search.result.NewSearchResultType.Type
import com.mapbox.search.result.NewSearchResultType.UNKNOWN
import com.mapbox.search.result.SearchResultType

@JvmSynthetic
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
public fun newSearchResultTypeToOld(@Type type: String): SearchResultType {
    return when (type) {
        COUNTRY -> SearchResultType.COUNTRY
        REGION -> SearchResultType.REGION
        POSTCODE -> SearchResultType.POSTCODE
        DISTRICT -> SearchResultType.DISTRICT
        PLACE -> SearchResultType.PLACE
        LOCALITY -> SearchResultType.LOCALITY
        NEIGHBORHOOD -> SearchResultType.NEIGHBORHOOD
        STREET -> SearchResultType.STREET
        ADDRESS -> SearchResultType.ADDRESS
        POI -> SearchResultType.POI
        BLOCK -> SearchResultType.BLOCK
        else -> SearchResultType.DEFAULT
    }
}

@Type
@JvmSynthetic
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
public fun newSearchResultTypeToFromOld(type: SearchResultType): String {
    return when (type) {
        SearchResultType.COUNTRY -> COUNTRY
        SearchResultType.REGION -> REGION
        SearchResultType.POSTCODE -> POSTCODE
        SearchResultType.BLOCK -> BLOCK
        SearchResultType.PLACE -> PLACE
        SearchResultType.DISTRICT -> DISTRICT
        SearchResultType.LOCALITY -> LOCALITY
        SearchResultType.NEIGHBORHOOD -> NEIGHBORHOOD
        SearchResultType.STREET -> STREET
        SearchResultType.ADDRESS -> ADDRESS
        SearchResultType.POI -> POI
    }
}

@JvmSynthetic
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
public fun newSearchResultTypeToCore(@Type type: String): CoreResultType {
    return when (type) {
        COUNTRY -> CoreResultType.COUNTRY
        REGION -> CoreResultType.REGION
        POSTCODE -> CoreResultType.POSTCODE
        DISTRICT -> CoreResultType.DISTRICT
        PLACE -> CoreResultType.PLACE
        LOCALITY -> CoreResultType.LOCALITY
        NEIGHBORHOOD -> CoreResultType.NEIGHBORHOOD
        STREET -> CoreResultType.STREET
        ADDRESS -> CoreResultType.ADDRESS
        POI -> CoreResultType.POI
        BLOCK -> CoreResultType.BLOCK
        UNKNOWN -> CoreResultType.UNKNOWN
        else -> {
            failDebug {
                "Unknown result type: $type"
            }
            CoreResultType.UNKNOWN
        }
    }
}

@JvmSynthetic
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
public fun newSearchResultTypeToBase(@Type type: String): BaseSearchResultType {
    return when (type) {
        COUNTRY -> BaseSearchResultType.COUNTRY
        REGION -> BaseSearchResultType.REGION
        POSTCODE -> BaseSearchResultType.POSTCODE
        DISTRICT -> BaseSearchResultType.DISTRICT
        PLACE -> BaseSearchResultType.PLACE
        LOCALITY -> BaseSearchResultType.LOCALITY
        NEIGHBORHOOD -> BaseSearchResultType.NEIGHBORHOOD
        STREET -> BaseSearchResultType.STREET
        ADDRESS -> BaseSearchResultType.ADDRESS
        POI -> BaseSearchResultType.POI
        BLOCK -> BaseSearchResultType.BLOCK
        UNKNOWN -> BaseSearchResultType.UNKNOWN
        else -> {
            failDebug {
                "Unknown result type: $type"
            }
            BaseSearchResultType.UNKNOWN
        }
    }
}

@Type
@JvmSynthetic
internal fun BaseSearchResultType.mapToNewSearchResultType(): String {
    return when (this) {
        BaseSearchResultType.COUNTRY -> COUNTRY
        BaseSearchResultType.REGION -> REGION
        BaseSearchResultType.POSTCODE -> POSTCODE
        BaseSearchResultType.DISTRICT -> DISTRICT
        BaseSearchResultType.PLACE -> PLACE
        BaseSearchResultType.LOCALITY -> LOCALITY
        BaseSearchResultType.NEIGHBORHOOD -> NEIGHBORHOOD
        BaseSearchResultType.STREET -> STREET
        BaseSearchResultType.ADDRESS -> ADDRESS
        BaseSearchResultType.POI -> POI
        BaseSearchResultType.BLOCK -> BLOCK
        BaseSearchResultType.UNKNOWN -> UNKNOWN
    }
}
