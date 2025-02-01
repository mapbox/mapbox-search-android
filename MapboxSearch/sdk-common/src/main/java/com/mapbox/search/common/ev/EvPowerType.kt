package com.mapbox.search.common.ev

import androidx.annotation.StringDef

/**
 * OCPI PowerType. This value indicates an electrical power configuration of a connector.
 * Refer to this type in the [OCPI GitHub repository](https://github.com/ocpi/ocpi/blob/2.2.1/mod_locations.asciidoc#1419-powertype-enum)
 * for more details.
 */
public object EvPowerType {

    /**
     * AC single phase.
     */
    public const val AC_1_PHASE: String = "AC_1_PHASE"

    /**
     * AC two phases, only two of the three available phases connected.
     */
    public const val AC_2_PHASE: String = "AC_2_PHASE"

    /**
     * AC two phases using split phase system.
     */
    public const val AC_2_PHASE_SPLIT: String = "AC_2_PHASE_SPLIT"

    /**
     * AC three phases.
     */
    public const val AC_3_PHASE: String = "AC_3_PHASE"

    /**
     * Direct Current.
     */
    public const val DC: String = "DC"

    /**
     * Unknown power type.
     */
    public const val UNKNOWN: String = "UNKNOWN"

    /**
     * Retention policy for the PowerType.
     */
    @Retention(AnnotationRetention.BINARY)
    @StringDef(
        AC_1_PHASE,
        AC_2_PHASE,
        AC_2_PHASE_SPLIT,
        AC_3_PHASE,
        DC,
        UNKNOWN,
    )
    public annotation class Type
}
