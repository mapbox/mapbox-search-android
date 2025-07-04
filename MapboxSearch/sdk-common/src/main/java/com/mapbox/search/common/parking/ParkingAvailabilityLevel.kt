package com.mapbox.search.common.parking

import androidx.annotation.StringDef
import com.mapbox.annotation.MapboxExperimental

/**
 * Availability or Probability based indicator,
 * based on reported or predicted availability/probability.
 */
@MapboxExperimental
public object ParkingAvailabilityLevel {

    /**
     * Low availability of parking spaces.
     */
    public const val LOW: String = "LOW"

    /**
     * Medium availability of parking spaces.
     */
    public const val MID: String = "MID"

    /**
     * High availability of parking spaces.
     */
    public const val HIGH: String = "HIGH"

    /**
     * Unknown availability level.
     */
    public const val UNKNOWN: String = "UNKNOWN"

    /**
     * Retention policy for [ParkingAvailabilityLevel] values.
     */
    @Retention(AnnotationRetention.BINARY)
    @StringDef(
        UNKNOWN,
        LOW,
        MID,
        HIGH,
    )
    public annotation class Type
}
