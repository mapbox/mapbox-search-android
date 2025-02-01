package com.mapbox.search.common.ev

import androidx.annotation.StringDef
import com.mapbox.annotation.MapboxExperimental

/**
 * OCPI EnergySourceCategory. Categories of energy sources.
 * Refer to this type in the [OCPI GitHub repository](https://github.com/ocpi/ocpi/blob/2.2.1/mod_locations.asciidoc#148-energysourcecategory-enum)
 * for more details.
 */
@MapboxExperimental
public object EvEnergySourceCategory {

    /**
     * Nuclear power sources.
     */
    public const val NUCLEAR: String = "NUCLEAR"

    /**
     * All kinds of fossil power sources.
     */
    public const val GENERAL_FOSSIL: String = "GENERAL_FOSSIL"

    /**
     * Fossil power from coal.
     */
    public const val COAL: String = "COAL"

    /**
     * Fossil power from gas.
     */
    public const val GAS: String = "GAS"

    /**
     * All kinds of regenerative power sources.
     */
    public const val GENERAL_GREEN: String = "GENERAL_GREEN"

    /**
     * Regenerative power from photo voltaic.
     */
    public const val SOLAR: String = "SOLAR"

    /**
     * Regenerative power from wind turbines.
     */
    public const val WIND: String = "WIND"

    /**
     * Regenerative power from water turbines.
     */
    public const val WATER: String = "WATER"

    /**
     * Category is unknown.
     */
    public const val UNKNOWN: String = "UNKNOWN"

    /**
     * Retention policy for the RoadObjectProvider.
     */
    @Retention(AnnotationRetention.BINARY)
    @StringDef(
        NUCLEAR,
        GENERAL_FOSSIL,
        COAL,
        GAS,
        GENERAL_GREEN,
        SOLAR,
        WIND,
        WATER,
        UNKNOWN,
    )
    public annotation class Type
}
