package com.mapbox.search.common

import androidx.annotation.StringDef
import com.mapbox.annotation.MapboxExperimental

/**
 * Represents the restriction to the parking spot for different purposes. One of the examples is
 * [OCPI ParkingRestriction](https://github.com/ocpi/ocpi/blob/2.2.1/mod_locations.asciidoc#1417-parkingrestriction-enum).
 */
@MapboxExperimental
public object ParkingRestriction {

    /**
     * Reserved parking spot for electric vehicles.
     */
    public const val EV_ONLY: String = "EV_ONLY"

    /**
     * Parking is only allowed while plugged in (charging).
     */
    public const val PLUGGED: String = "PLUGGED"

    /**
     * Reserved parking spot for disabled people with valid ID.
     */
    public const val DISABLED: String = "DISABLED"

    /**
     * Parking spot for customers/guests only, for example in case of a hotel or shop.
     */
    public const val CUSTOMERS: String = "CUSTOMERS"

    /**
     * Parking spot only suitable for (electric) motorcycles or scooters.
     */
    public const val MOTOR_CYCLES: String = "MOTOR_CYCLES"

    /**
     * Unknown parking restriction.
     */
    public const val UNKNOWN: String = "UNKNOWN"

    /**
     * Retention policy for the ParkingRestriction.
     */
    @Retention(AnnotationRetention.BINARY)
    @StringDef(
        EV_ONLY,
        PLUGGED,
        DISABLED,
        CUSTOMERS,
        MOTOR_CYCLES,
        UNKNOWN,
    )
    public annotation class Type
}
