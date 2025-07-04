package com.mapbox.search.common.parking

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
     * Parking is not allowed at this location.
     */
    public const val NO_PARKING: String = "NO_PARKING"

    /**
     * Parking is limited to a maximum stay duration.
     */
    public const val MAX_STAY: String = "MAX_STAY"

    /**
     * Parking is reserved for monthly pass holders only.
     */
    public const val MONTHLY_ONLY: String = "MONTHLY_ONLY"

    /**
     * SUVs are not allowed to park here.
     */
    public const val NO_SUV: String = "NO_SUV"

    /**
     * LPG-powered vehicles are prohibited from parking here.
     */
    public const val NO_LPG: String = "NO_LPG"

    /**
     * Parking is reserved for valet service only.
     */
    public const val VALET_ONLY: String = "VALET_ONLY"

    /**
     * Parking is for visitors only.
     */
    public const val VISITORS_ONLY: String = "VISITORS_ONLY"

    /**
     * Parking is reserved for event use only.
     */
    public const val EVENTS_ONLY: String = "EVENTS_ONLY"

    /**
     * No parking restrictions outside specified hours.
     */
    public const val NO_RESTRICTIONS_OUTSIDE_HOURS: String = "NO_RESTRICTIONS_OUTSIDE_HOURS"

    /**
     * Parking allowed only with a booking.
     */
    public const val BOOKING_ONLY: String = "BOOKING_ONLY"

    /**
     * Parking is limited to vehicles displaying a parking disk.
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
