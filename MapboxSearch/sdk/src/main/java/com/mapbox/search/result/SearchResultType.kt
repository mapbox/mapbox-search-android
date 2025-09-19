package com.mapbox.search.result

import com.mapbox.search.base.core.CoreResultType
import com.mapbox.search.base.result.BaseSearchResultType

/**
 * Defines type of search result.
 */
public enum class SearchResultType {

    /**
     * Generally recognized countries or, in some cases like Hong Kong, an area of quasi-national
     * administrative status that has been given a designated country code under ISO 3166-1.
     */
    COUNTRY,

    /**
     * Top-level sub-national administrative features, such as states in the United States
     * or provinces in Canada or China.
     */
    REGION,

    /**
     * Postal codes used in country-specific national addressing systems.
     */
    POSTCODE,

    /**
     * The block number. Available specifically for Japan.
     */
    BLOCK,

    /**
     * Features that are smaller than top-level administrative features but typically larger than cities,
     * in countries that use such an additional layer in postal addressing (for example, prefectures in China).
     */
    DISTRICT,

    /**
     * Typically these are cities, villages, municipalities, etc.
     * They’re usually features used in postal addressing, and are suitable for display in ambient
     * end-user applications where current-location context is needed (for example, in weather displays).
     */
    PLACE,

    /**
     * Official sub-city features present in countries where such an additional administrative layer
     * is used in postal addressing, or where such features are commonly referred to in local parlance.
     * Examples include city districts in Brazil and Chile and arrondissements in France.
     */
    LOCALITY,

    /**
     * Colloquial sub-city features often referred to in local parlance.
     * Unlike locality features, these typically lack official status and may lack universally agreed-upon boundaries.
     */
    NEIGHBORHOOD,

    /**
     * Features that are smaller than places and that correspond to streets in cities, villages, etc.
     */
    STREET,

    /**
     * Individual residential or business addresses.
     */
    ADDRESS,

    /**
     * Points of interest.
     * These include restaurants, stores, concert venues, parks, museums, etc.
     */
    POI,
}

@JvmSynthetic
internal fun SearchResultType.mapToCore(): CoreResultType {
    return when (this) {
        SearchResultType.COUNTRY -> CoreResultType.COUNTRY
        SearchResultType.REGION -> CoreResultType.REGION
        SearchResultType.POSTCODE -> CoreResultType.POSTCODE
        SearchResultType.BLOCK -> CoreResultType.BLOCK
        SearchResultType.PLACE -> CoreResultType.PLACE
        SearchResultType.DISTRICT -> CoreResultType.DISTRICT
        SearchResultType.LOCALITY -> CoreResultType.LOCALITY
        SearchResultType.NEIGHBORHOOD -> CoreResultType.NEIGHBORHOOD
        SearchResultType.STREET -> CoreResultType.STREET
        SearchResultType.ADDRESS -> CoreResultType.ADDRESS
        SearchResultType.POI -> CoreResultType.POI
    }
}

@JvmSynthetic
internal fun BaseSearchResultType.mapToPlatform(): SearchResultType {
    return when (this) {
        BaseSearchResultType.COUNTRY -> SearchResultType.COUNTRY
        BaseSearchResultType.REGION -> SearchResultType.REGION
        BaseSearchResultType.POSTCODE -> SearchResultType.POSTCODE
        BaseSearchResultType.BLOCK -> SearchResultType.BLOCK
        BaseSearchResultType.PLACE -> SearchResultType.PLACE
        BaseSearchResultType.DISTRICT -> SearchResultType.DISTRICT
        BaseSearchResultType.LOCALITY -> SearchResultType.LOCALITY
        BaseSearchResultType.NEIGHBORHOOD -> SearchResultType.NEIGHBORHOOD
        BaseSearchResultType.STREET -> SearchResultType.STREET
        BaseSearchResultType.ADDRESS -> SearchResultType.ADDRESS
        BaseSearchResultType.POI -> SearchResultType.POI
    }
}

@JvmSynthetic
internal fun SearchResultType.mapToBase(): BaseSearchResultType {
    return when (this) {
        SearchResultType.COUNTRY -> BaseSearchResultType.COUNTRY
        SearchResultType.REGION -> BaseSearchResultType.REGION
        SearchResultType.POSTCODE -> BaseSearchResultType.POSTCODE
        SearchResultType.BLOCK -> BaseSearchResultType.BLOCK
        SearchResultType.PLACE -> BaseSearchResultType.PLACE
        SearchResultType.DISTRICT -> BaseSearchResultType.DISTRICT
        SearchResultType.LOCALITY -> BaseSearchResultType.LOCALITY
        SearchResultType.NEIGHBORHOOD -> BaseSearchResultType.NEIGHBORHOOD
        SearchResultType.STREET -> BaseSearchResultType.STREET
        SearchResultType.ADDRESS -> BaseSearchResultType.ADDRESS
        SearchResultType.POI -> BaseSearchResultType.POI
    }
}
