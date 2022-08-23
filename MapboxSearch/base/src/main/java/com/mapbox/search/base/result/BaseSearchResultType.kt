package com.mapbox.search.base.result

import com.mapbox.search.base.core.CoreResultType

enum class BaseSearchResultType {
    COUNTRY,
    REGION,
    POSTCODE,
    BLOCK,
    DISTRICT,
    PLACE,
    LOCALITY,
    NEIGHBORHOOD,
    STREET,
    ADDRESS,
    POI,
}

internal fun BaseSearchResultType.mapToCore(): CoreResultType {
    return when (this) {
        BaseSearchResultType.COUNTRY -> CoreResultType.COUNTRY
        BaseSearchResultType.REGION -> CoreResultType.REGION
        BaseSearchResultType.POSTCODE -> CoreResultType.POSTCODE
        BaseSearchResultType.BLOCK -> CoreResultType.BLOCK
        BaseSearchResultType.PLACE -> CoreResultType.PLACE
        BaseSearchResultType.DISTRICT -> CoreResultType.DISTRICT
        BaseSearchResultType.LOCALITY -> CoreResultType.LOCALITY
        BaseSearchResultType.NEIGHBORHOOD -> CoreResultType.NEIGHBORHOOD
        BaseSearchResultType.STREET -> CoreResultType.STREET
        BaseSearchResultType.ADDRESS -> CoreResultType.ADDRESS
        BaseSearchResultType.POI -> CoreResultType.POI
    }
}
