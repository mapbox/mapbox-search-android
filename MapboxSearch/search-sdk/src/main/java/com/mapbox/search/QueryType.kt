package com.mapbox.search

import com.mapbox.search.Reserved.Flags.SBS
import com.mapbox.search.Reserved.Flags.SEARCH_BOX
import com.mapbox.search.base.core.CoreQueryType
import com.mapbox.search.base.logger.logw

/**
 * Values to filter results to include only a subset (one or more) of the available feature types.
 * See the
 * [Administrative unit types section](https://docs.mapbox.com/api/search/search-box/#administrative-unit-types)
 * for details about the types.
 *
 * This enum has been replaced by [NewQueryType].
 */
@Deprecated(
    message = "Replaced by NewQueryType. This enum no longer represents the current supported types (e.g. BRAND).",
    replaceWith = ReplaceWith("NewQueryType"),
)
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
@Suppress("DEPRECATION")
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
@Suppress("DEPRECATION")
internal fun List<QueryType>?.mapToCoreTypes(): List<CoreQueryType>? {
    return this?.map { it.mapToCore() }
}

@JvmSynthetic
internal fun CoreQueryType.mapToNewQueryType(): String {
    return when (this) {
        CoreQueryType.COUNTRY -> NewQueryType.COUNTRY
        CoreQueryType.REGION -> NewQueryType.REGION
        CoreQueryType.POSTCODE -> NewQueryType.POSTCODE
        CoreQueryType.DISTRICT -> NewQueryType.DISTRICT
        CoreQueryType.PLACE -> NewQueryType.PLACE
        CoreQueryType.LOCALITY -> NewQueryType.LOCALITY
        CoreQueryType.NEIGHBORHOOD -> NewQueryType.NEIGHBORHOOD
        CoreQueryType.STREET -> NewQueryType.STREET
        CoreQueryType.ADDRESS -> NewQueryType.ADDRESS
        CoreQueryType.POI -> NewQueryType.POI
        CoreQueryType.CATEGORY -> NewQueryType.CATEGORY
        CoreQueryType.BRAND -> NewQueryType.BRAND
    }
}

@JvmSynthetic
internal fun List<CoreQueryType>?.mapToNewQueryTypes(): List<String>? {
    return this?.map { it.mapToNewQueryType() }
}

@JvmSynthetic
@Suppress("DEPRECATION")
internal fun List<String>?.mapNewQueryTypesToCore(): List<CoreQueryType>? {
    return this?.map { newQueryTypeToCore(it) }
}

@JvmSynthetic
internal fun newQueryTypeToCore(type: String): CoreQueryType {
    return when (type) {
        NewQueryType.COUNTRY -> CoreQueryType.COUNTRY
        NewQueryType.REGION -> CoreQueryType.REGION
        NewQueryType.POSTCODE -> CoreQueryType.POSTCODE
        NewQueryType.DISTRICT -> CoreQueryType.DISTRICT
        NewQueryType.PLACE -> CoreQueryType.PLACE
        NewQueryType.LOCALITY -> CoreQueryType.LOCALITY
        NewQueryType.NEIGHBORHOOD -> CoreQueryType.NEIGHBORHOOD
        NewQueryType.STREET -> CoreQueryType.STREET
        NewQueryType.ADDRESS -> CoreQueryType.ADDRESS
        NewQueryType.POI -> CoreQueryType.POI
        NewQueryType.CATEGORY -> CoreQueryType.CATEGORY
        NewQueryType.BRAND -> CoreQueryType.BRAND
        else -> error("Unsupported query type: $type")
    }
}

@JvmSynthetic
@Suppress("DEPRECATION")
internal fun resolveQueryTypesToCore(
    types: List<QueryType>?,
    newTypes: List<String>?,
): List<CoreQueryType>? {
    return when {
        !newTypes.isNullOrEmpty() -> {
            if (!types.isNullOrEmpty()) {
                logw(
                    message = "Both QueryType (types) and NewQueryType (newTypes) provided, newTypes take priority",
                    tag = "QueryType",
                )
            }
            newTypes.mapNewQueryTypesToCore()
        }
        else -> types.mapToCoreTypes()
    }
}
