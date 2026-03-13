package com.mapbox.search

import androidx.annotation.StringDef

/**
 * Defines the type of a search query filter.
 * Replaces the [QueryType] enum to provide a more flexible API and support for new types such as [BRAND].
 *
 * @see QueryType
 */
@Suppress("DEPRECATION")
public object NewQueryType {

    /**
     * Country query type.
     * @see QueryType.COUNTRY
     */
    public const val COUNTRY: String = "country"

    /**
     * Region query type.
     * @see QueryType.REGION
     */
    public const val REGION: String = "region"

    /**
     * Postcode query type.
     * @see QueryType.POSTCODE
     */
    public const val POSTCODE: String = "postcode"

    /**
     * District query type.
     * @see QueryType.DISTRICT
     */
    public const val DISTRICT: String = "district"

    /**
     * Place query type.
     * @see QueryType.PLACE
     */
    public const val PLACE: String = "place"

    /**
     * Locality query type.
     * @see QueryType.LOCALITY
     */
    public const val LOCALITY: String = "locality"

    /**
     * Neighborhood query type.
     * @see QueryType.NEIGHBORHOOD
     */
    public const val NEIGHBORHOOD: String = "neighborhood"

    /**
     * Street query type.
     * @see QueryType.STREET
     */
    public const val STREET: String = "street"

    /**
     * Address query type.
     * @see QueryType.ADDRESS
     */
    public const val ADDRESS: String = "address"

    /**
     * Poi query type.
     * @see QueryType.POI
     */
    public const val POI: String = "poi"

    /**
     * Category query type.
     * @see QueryType.CATEGORY
     */
    public const val CATEGORY: String = "category"

    /**
     * Brand query type.
     * Use to filter results to brands only.
     */
    public const val BRAND: String = "brand"

    /**
     * Retention policy for the [NewQueryType].
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
        CATEGORY,
        BRAND,
    )
    public annotation class Type
}
