package com.mapbox.search

import com.mapbox.search.Reserved.Flags.SBS
import com.mapbox.search.Reserved.Flags.SEARCH_BOX
import com.mapbox.search.base.core.CoreQueryType
import com.mapbox.search.base.failDebug

/**
 * Values to filter results to include only a subset (one or more) of the available feature types.
 */
public enum class QueryType {

    /**
     * Country query type.
     */
    COUNTRY,

    /**
     * Region query type.
     */
    REGION,

    /**
     * Postcode query type.
     */
    POSTCODE,

    /**
     * District query type.
     */
    DISTRICT,

    /**
     * Place query type.
     */
    PLACE,

    /**
     * Locality query type.
     */
    LOCALITY,

    /**
     * Neighborhood query type.
     */
    NEIGHBORHOOD,

    /**
     * Street query type.
     *
     * Note: Supported for Single Box Search and Search Box APIs only. Reserved for internal and special use.
     */
    @Reserved(SBS, SEARCH_BOX)
    STREET,

    /**
     * Address query type.
     */
    ADDRESS,

    /**
     * Poi query type.
     */
    POI,

    /**
     * Category query type.
     *
     * Note: Supported for Single Box Search and Search Box APIs only. Reserved for internal and special use.
     */
    @Reserved(SBS, SEARCH_BOX)
    CATEGORY,
}

@JvmSynthetic
internal fun QueryType.mapToCore(): CoreQueryType {
    return when (this) {
        QueryType.COUNTRY -> CoreQueryType.COUNTRY
        QueryType.REGION -> CoreQueryType.REGION
        QueryType.POSTCODE -> CoreQueryType.POSTCODE
        QueryType.DISTRICT -> CoreQueryType.DISTRICT
        QueryType.PLACE -> CoreQueryType.PLACE
        QueryType.LOCALITY -> CoreQueryType.LOCALITY
        QueryType.NEIGHBORHOOD -> CoreQueryType.NEIGHBORHOOD
        QueryType.STREET -> CoreQueryType.STREET
        QueryType.ADDRESS -> CoreQueryType.ADDRESS
        QueryType.POI -> CoreQueryType.POI
        QueryType.CATEGORY -> CoreQueryType.CATEGORY
    }
}

@JvmSynthetic
internal fun CoreQueryType.mapToPlatform(): QueryType {
    return when (this) {
        CoreQueryType.COUNTRY -> QueryType.COUNTRY
        CoreQueryType.REGION -> QueryType.REGION
        CoreQueryType.POSTCODE -> QueryType.POSTCODE
        CoreQueryType.DISTRICT -> QueryType.DISTRICT
        CoreQueryType.PLACE -> QueryType.PLACE
        CoreQueryType.LOCALITY -> QueryType.LOCALITY
        CoreQueryType.NEIGHBORHOOD -> QueryType.NEIGHBORHOOD
        CoreQueryType.STREET -> QueryType.STREET
        CoreQueryType.ADDRESS -> QueryType.ADDRESS
        CoreQueryType.POI -> QueryType.POI
        CoreQueryType.CATEGORY -> QueryType.CATEGORY
        CoreQueryType.BRAND -> {
            failDebug {
                "CoreQueryType.BRAND is not supported"
            }
            QueryType.POI
        }
    }
}

@JvmSynthetic
internal fun List<QueryType>?.mapToCoreTypes(): List<CoreQueryType>? {
    return this?.map { it.mapToCore() }
}

@JvmSynthetic
internal fun List<CoreQueryType>?.mapToPlatformTypes(): List<QueryType>? {
    return this?.map { it.mapToPlatform() }
}
