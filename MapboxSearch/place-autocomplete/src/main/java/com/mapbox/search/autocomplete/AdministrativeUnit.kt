package com.mapbox.search.autocomplete

import com.mapbox.search.base.core.CoreQueryType

/**
 * Values to filter Place Autocomplete results to include only a subset (one or more) of the available feature types.
 */
public enum class AdministrativeUnit(
    @JvmSynthetic
    internal val coreType: CoreQueryType,
) {

    /**
     * Generally recognized countries or, in some cases like Hong Kong, an area of quasi-national administrative status
     * that has been given a designated country code under ISO 3166-1.
     */
    COUNTRY(CoreQueryType.COUNTRY),

    /**
     * Top-level sub-national administrative features,
     * such as states in the United States or provinces in Canada or China.
     */
    REGION(CoreQueryType.REGION),

    /**
     * Postal codes used in country-specific national addressing systems.
     */
    POSTCODE(CoreQueryType.POSTCODE),

    /**
     * Features that are smaller than top-level administrative features but typically larger than cities,
     * in countries that use such an additional layer in postal addressing (for example, prefectures in China).
     */
    DISTRICT(CoreQueryType.DISTRICT),

    /**
     * Typically these are cities, villages, municipalities, etc. They’re usually features used in postal addressing,
     * and are suitable for display in ambient end-user applications where current-location context is needed
     * (for example, in weather displays).
     */
    PLACE(CoreQueryType.PLACE),

    /**
     * Official sub-city features present in countries where such an additional administrative layer
     * is used in postal addressing, or where such features are commonly referred to in local parlance.
     * Examples include city districts in Brazil and Chile and arrondissements in France.
     */
    LOCALITY(CoreQueryType.LOCALITY),

    /**
     * Colloquial sub-city features often referred to in local parlance. Unlike locality features,
     * these typically lack official status and may lack universally agreed-upon boundaries.
     * Note: Not available for reverse geocoding requests.
     */
    NEIGHBORHOOD(CoreQueryType.NEIGHBORHOOD),

    /**
     * The street, with no house number.
     */
    STREET(CoreQueryType.STREET),

    /**
     * Individual residential or business addresses as a street with house number. In a Japanese context,
     * this is the block number and the house number. All components smaller than `chome` are designated as an `address`.
     */
    ADDRESS(CoreQueryType.ADDRESS),
}
