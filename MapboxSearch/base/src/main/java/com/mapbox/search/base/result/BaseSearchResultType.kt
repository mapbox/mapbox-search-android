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

val CoreResultType.isSearchResultType: Boolean
    get() = tryMapToSearchResultType() != null

fun CoreResultType.tryMapToSearchResultType(): BaseSearchResultType? {
    return when (this) {
        CoreResultType.COUNTRY -> BaseSearchResultType.COUNTRY
        CoreResultType.REGION -> BaseSearchResultType.REGION
        CoreResultType.PLACE -> BaseSearchResultType.PLACE
        CoreResultType.DISTRICT -> BaseSearchResultType.DISTRICT
        CoreResultType.LOCALITY -> BaseSearchResultType.LOCALITY
        CoreResultType.NEIGHBORHOOD -> BaseSearchResultType.NEIGHBORHOOD
        CoreResultType.ADDRESS -> BaseSearchResultType.ADDRESS
        CoreResultType.POI -> BaseSearchResultType.POI
        CoreResultType.STREET -> BaseSearchResultType.STREET
        CoreResultType.POSTCODE -> BaseSearchResultType.POSTCODE
        CoreResultType.BLOCK -> BaseSearchResultType.BLOCK
        CoreResultType.UNKNOWN,
        CoreResultType.USER_RECORD,
        CoreResultType.CATEGORY,
        CoreResultType.BRAND,
        CoreResultType.QUERY -> null
    }
}

val ALLOWED_MULTI_PLACE_TYPES = listOf(
    CoreResultType.COUNTRY,
    CoreResultType.REGION,
    CoreResultType.POSTCODE,
    CoreResultType.DISTRICT,
    CoreResultType.PLACE,
    CoreResultType.LOCALITY
)

fun Collection<CoreResultType>.isValidMultiType(): Boolean {
    return isNotEmpty() && (ALLOWED_MULTI_PLACE_TYPES.containsAll(this) || size == 1)
}
