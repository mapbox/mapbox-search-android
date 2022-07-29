package com.mapbox.search.base.result

import com.mapbox.search.base.core.CoreResultType

// TODO merge with BaseRawResultType?
enum class BaseRawResultType {
    UNKNOWN,
    COUNTRY,
    REGION,
    PLACE,
    DISTRICT,
    LOCALITY,
    NEIGHBORHOOD,
    ADDRESS,
    POI,
    STREET,
    POSTCODE,
    BLOCK,
    CATEGORY,
    QUERY,
    USER_RECORD;

    val isSearchResultType: Boolean
        get() = tryMapToSearchResultType() != null

    fun tryMapToSearchResultType(): BaseSearchResultType? {
        return when (this) {
            COUNTRY -> BaseSearchResultType.COUNTRY
            REGION -> BaseSearchResultType.REGION
            PLACE -> BaseSearchResultType.PLACE
            DISTRICT -> BaseSearchResultType.DISTRICT
            LOCALITY -> BaseSearchResultType.LOCALITY
            NEIGHBORHOOD -> BaseSearchResultType.NEIGHBORHOOD
            ADDRESS -> BaseSearchResultType.ADDRESS
            POI -> BaseSearchResultType.POI
            STREET -> BaseSearchResultType.STREET
            POSTCODE -> BaseSearchResultType.POSTCODE
            BLOCK -> BaseSearchResultType.BLOCK
            UNKNOWN,
            USER_RECORD,
            CATEGORY,
            QUERY -> null
        }
    }
}

internal fun BaseRawResultType.mapToCore(): CoreResultType {
    return when (this) {
        BaseRawResultType.UNKNOWN -> CoreResultType.UNKNOWN
        BaseRawResultType.COUNTRY -> CoreResultType.COUNTRY
        BaseRawResultType.REGION -> CoreResultType.REGION
        BaseRawResultType.PLACE -> CoreResultType.PLACE
        BaseRawResultType.DISTRICT -> CoreResultType.DISTRICT
        BaseRawResultType.LOCALITY -> CoreResultType.LOCALITY
        BaseRawResultType.NEIGHBORHOOD -> CoreResultType.NEIGHBORHOOD
        BaseRawResultType.ADDRESS -> CoreResultType.ADDRESS
        BaseRawResultType.POI -> CoreResultType.POI
        BaseRawResultType.CATEGORY -> CoreResultType.CATEGORY
        BaseRawResultType.USER_RECORD -> CoreResultType.USER_RECORD
        BaseRawResultType.STREET -> CoreResultType.STREET
        BaseRawResultType.POSTCODE -> CoreResultType.POSTCODE
        BaseRawResultType.BLOCK -> CoreResultType.BLOCK
        BaseRawResultType.QUERY -> CoreResultType.QUERY
    }
}

internal fun CoreResultType.mapToBase(): BaseRawResultType {
    return when (this) {
        CoreResultType.UNKNOWN -> BaseRawResultType.UNKNOWN
        CoreResultType.COUNTRY -> BaseRawResultType.COUNTRY
        CoreResultType.REGION -> BaseRawResultType.REGION
        CoreResultType.PLACE -> BaseRawResultType.PLACE
        CoreResultType.DISTRICT -> BaseRawResultType.DISTRICT
        CoreResultType.LOCALITY -> BaseRawResultType.LOCALITY
        CoreResultType.NEIGHBORHOOD -> BaseRawResultType.NEIGHBORHOOD
        CoreResultType.ADDRESS -> BaseRawResultType.ADDRESS
        CoreResultType.POI -> BaseRawResultType.POI
        CoreResultType.CATEGORY -> BaseRawResultType.CATEGORY
        CoreResultType.USER_RECORD -> BaseRawResultType.USER_RECORD
        CoreResultType.STREET -> BaseRawResultType.STREET
        CoreResultType.POSTCODE -> BaseRawResultType.POSTCODE
        CoreResultType.BLOCK -> BaseRawResultType.BLOCK
        CoreResultType.QUERY -> BaseRawResultType.QUERY
    }
}

val ALLOWED_MULTI_PLACE_TYPES = listOf(
    BaseRawResultType.COUNTRY,
    BaseRawResultType.REGION,
    BaseRawResultType.POSTCODE,
    BaseRawResultType.DISTRICT,
    BaseRawResultType.PLACE,
    BaseRawResultType.LOCALITY
)

fun Collection<BaseRawResultType>.isValidMultiType(): Boolean {
    return isNotEmpty() && (ALLOWED_MULTI_PLACE_TYPES.containsAll(this) || size == 1)
}
