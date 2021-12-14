package com.mapbox.search.result

import com.mapbox.search.core.CoreResultType

internal enum class OriginalResultType {
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
    CATEGORY,
    QUERY,
    USER_RECORD;

    val isSearchResultType: Boolean
        get() = tryMapToSearchResultType() != null

    fun tryMapToSearchResultType(): SearchResultType? {
        return when (this) {
            COUNTRY -> SearchResultType.COUNTRY
            REGION -> SearchResultType.REGION
            PLACE -> SearchResultType.PLACE
            DISTRICT -> SearchResultType.DISTRICT
            LOCALITY -> SearchResultType.LOCALITY
            NEIGHBORHOOD -> SearchResultType.NEIGHBORHOOD
            ADDRESS -> SearchResultType.ADDRESS
            POI -> SearchResultType.POI
            STREET -> SearchResultType.STREET
            POSTCODE -> SearchResultType.POSTCODE
            UNKNOWN,
            USER_RECORD,
            CATEGORY,
            QUERY -> null
        }
    }
}

internal fun OriginalResultType.mapToCore(): CoreResultType {
    return when (this) {
        OriginalResultType.UNKNOWN -> CoreResultType.UNKNOWN
        OriginalResultType.COUNTRY -> CoreResultType.COUNTRY
        OriginalResultType.REGION -> CoreResultType.REGION
        OriginalResultType.PLACE -> CoreResultType.PLACE
        OriginalResultType.DISTRICT -> CoreResultType.DISTRICT
        OriginalResultType.LOCALITY -> CoreResultType.LOCALITY
        OriginalResultType.NEIGHBORHOOD -> CoreResultType.NEIGHBORHOOD
        OriginalResultType.ADDRESS -> CoreResultType.ADDRESS
        OriginalResultType.POI -> CoreResultType.POI
        OriginalResultType.CATEGORY -> CoreResultType.CATEGORY
        OriginalResultType.USER_RECORD -> CoreResultType.USER_RECORD
        OriginalResultType.STREET -> CoreResultType.STREET
        OriginalResultType.POSTCODE -> CoreResultType.POSTCODE
        OriginalResultType.QUERY -> CoreResultType.QUERY
    }
}

internal fun CoreResultType.mapToPlatform(): OriginalResultType {
    return when (this) {
        CoreResultType.UNKNOWN -> OriginalResultType.UNKNOWN
        CoreResultType.COUNTRY -> OriginalResultType.COUNTRY
        CoreResultType.REGION -> OriginalResultType.REGION
        CoreResultType.PLACE -> OriginalResultType.PLACE
        CoreResultType.DISTRICT -> OriginalResultType.DISTRICT
        CoreResultType.LOCALITY -> OriginalResultType.LOCALITY
        CoreResultType.NEIGHBORHOOD -> OriginalResultType.NEIGHBORHOOD
        CoreResultType.ADDRESS -> OriginalResultType.ADDRESS
        CoreResultType.POI -> OriginalResultType.POI
        CoreResultType.CATEGORY -> OriginalResultType.CATEGORY
        CoreResultType.USER_RECORD -> OriginalResultType.USER_RECORD
        CoreResultType.STREET -> OriginalResultType.STREET
        CoreResultType.POSTCODE -> OriginalResultType.POSTCODE
        CoreResultType.QUERY -> OriginalResultType.QUERY
    }
}

internal val ALLOWED_MULTI_PLACE_TYPES = listOf(
    OriginalResultType.COUNTRY,
    OriginalResultType.REGION,
    OriginalResultType.POSTCODE,
    OriginalResultType.DISTRICT,
    OriginalResultType.PLACE,
    OriginalResultType.LOCALITY
)

internal fun Collection<OriginalResultType>.isValidMultiType(): Boolean {
    return isNotEmpty() && (ALLOWED_MULTI_PLACE_TYPES.containsAll(this) || size == 1)
}
