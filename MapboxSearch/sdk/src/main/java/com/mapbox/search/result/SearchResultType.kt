@file:Suppress("DEPRECATION")

package com.mapbox.search.result

import com.mapbox.search.result.SearchResultType.Companion.DEFAULT

/**
 * Defines type of search result.
 *
 * This enum has been replaced by [NewSearchResultType].
 * It no longer represents the currently supported types in the Search Engine
 * because it cannot be extended without violating SemVer rules for public API changes.
 *
 * If a type of a [SearchResult] is not present in [SearchResultType], it will default to [DEFAULT].
 */
@Deprecated(
    message = "Replaced by NewSearchResultType. This enum no longer represents the current supported types.",
    replaceWith = ReplaceWith("NewSearchResultType"),
)
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
    POI;

    /**
     * Companion object.
     */
    public companion object {

        /**
         * The default type used when the type of an [SearchResult] is not present in [SearchResultType].
         */
        public val DEFAULT: SearchResultType = ADDRESS
    }
}
