package com.mapbox.search.result

import androidx.annotation.StringDef

/**
 * Defines the type of a [SearchResult].
 * Replaces the [SearchResultType] enum to provide a more flexible API.
 */
@Suppress("DEPRECATION")
public object NewSearchResultType {

    /**
     * Generally recognized countries.
     * @see SearchResultType.COUNTRY
     */
    public const val COUNTRY: String = "country"

    /**
     * Top-level sub-national administrative features, like states or provinces.
     * @see SearchResultType.REGION
     */
    public const val REGION: String = "region"

    /**
     * Postal codes.
     * @see SearchResultType.POSTCODE
     */
    public const val POSTCODE: String = "postcode"

    /**
     * Administrative features smaller than regions but larger than cities.
     * @see SearchResultType.DISTRICT
     */
    public const val DISTRICT: String = "district"

    /**
     * Typically cities, villages, municipalities, etc.
     * @see SearchResultType.PLACE
     */
    public const val PLACE: String = "place"

    /**
     * Official sub-city features like city districts.
     * @see SearchResultType.LOCALITY
     */
    public const val LOCALITY: String = "locality"

    /**
     * Colloquial sub-city features, like neighborhoods.
     * @see SearchResultType.NEIGHBORHOOD
     */
    public const val NEIGHBORHOOD: String = "neighborhood"

    /**
     * Streets.
     * @see SearchResultType.STREET
     */
    public const val STREET: String = "street"

    /**
     * Individual residential or business addresses.
     * @see SearchResultType.ADDRESS
     */
    public const val ADDRESS: String = "address"

    /**
     * Points of interest.
     * These include restaurants, stores, concert venues, parks, museums, etc.
     * @see SearchResultType.POI
     */
    public const val POI: String = "poi"

    /**
     * The block number, specifically for Japan.
     * @see SearchResultType.BLOCK
     */
    public const val BLOCK: String = "block"

    /**
     * Unknown type.
     */
    public const val UNKNOWN: String = "unknown"

    /**
     * Retention policy for the [NewSearchResultType].
     */
    @Retention(AnnotationRetention.BINARY)
    @StringDef(
        COUNTRY,
        REGION,
        POSTCODE,
        DISTRICT,
        PLACE,
        LOCALITY,
        NEIGHBORHOOD,
        STREET,
        ADDRESS,
        POI,
        BLOCK,
        UNKNOWN,
    )
    public annotation class Type
}
