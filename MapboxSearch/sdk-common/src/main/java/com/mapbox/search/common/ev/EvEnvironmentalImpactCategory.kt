package com.mapbox.search.common.ev

import androidx.annotation.StringDef
import com.mapbox.annotation.MapboxExperimental

/**
 * OCPI EnvironmentalImpactCategory. Categories of environmental impact values.
 * Refer to this type in the [OCPI GitHub repository](https://github.com/ocpi/ocpi/blob/2.2.1/mod_locations.asciidoc#1410-environmentalimpactcategory-enum)
 * for more details.
 */
@MapboxExperimental
public object EvEnvironmentalImpactCategory {

    /**
     * Produced nuclear waste in grams per kilowatt hour.
     */
    public const val NUCLEAR_WASTE: String = "NUCLEAR_WASTE"

    /**
     * Exhausted carbon dioxide in grams per kilowatt hour.
     */
    public const val CARBON_DIOXIDE: String = "CARBON_DIOXIDE"

    /**
     * Category is unknown.
     */
    public const val UNKNOWN: String = "UNKNOWN"

    /**
     * Retention policy for the EvEnvironmentalImpactCategory.
     */
    @Retention(AnnotationRetention.BINARY)
    @StringDef(
        NUCLEAR_WASTE,
        CARBON_DIOXIDE,
        UNKNOWN,
    )
    public annotation class Type
}
