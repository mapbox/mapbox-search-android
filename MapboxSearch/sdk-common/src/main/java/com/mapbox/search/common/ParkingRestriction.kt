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
     * No parking allowed.
     */
    public const val NO_PARKING: String = "NO_PARKING"

    /**
     * Maximum stay time restriction.
     */
    public const val MAX_STAY: String = "MAX_STAY"

    /**
     * Monthly parking only.
     */
    public const val MONTHLY_ONLY: String = "MONTHLY_ONLY"

    /**
     * No SUV parking allowed.
     */
    public const val NO_SUV: String = "NO_SUV"

    /**
     * No LPG vehicles allowed.
     */
    public const val NO_LPG: String = "NO_LPG"

    /**
     * Valet parking only.
     */
    public const val VALET_ONLY: String = "VALET_ONLY"

    /**
     * Visitors only parking.
     */
    public const val VISITORS_ONLY: String = "VISITORS_ONLY"

    /**
     * Events only parking.
     */
    public const val EVENTS_ONLY: String = "EVENTS_ONLY"

    /**
     * No restrictions outside of specified hours.
     */
    public const val NO_RESTRICTIONS_OUTSIDE_HOURS: String = "NO_RESTRICTIONS_OUTSIDE_HOURS"

    /**
     * Booking required for parking.
     */
    public const val BOOKING_ONLY: String = "BOOKING_ONLY"

    /**
     * Parking disk required.
     */
    public const val PARKING_DISK: String = "PARKING_DISK"

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
        NO_PARKING,
        MAX_STAY,
        MONTHLY_ONLY,
        NO_SUV,
        NO_LPG,
        VALET_ONLY,
        VISITORS_ONLY,
        EVENTS_ONLY,
        NO_RESTRICTIONS_OUTSIDE_HOURS,
        BOOKING_ONLY,
        PARKING_DISK,
        UNKNOWN,
    )
    public annotation class Type
}
