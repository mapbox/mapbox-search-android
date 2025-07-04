package com.mapbox.search.common.parking

import androidx.annotation.StringDef
import com.mapbox.annotation.MapboxExperimental

/**
 * Payment method.
 */
@MapboxExperimental
public object ParkingPaymentMethod {

    /**
     * Pay at a machine inside the parking facility before leaving.
     */
    public const val PAY_ON_FOOT: String = "PAY_ON_FOOT"

    /**
     * Pay-and-display system where a ticket is displayed in the vehicle.
     */
    public const val PAY_AND_DISPLAY: String = "PAY_AND_DISPLAY"

    /**
     * Pay at the exit gate.
     */
    public const val PAY_ON_EXIT: String = "PAY_ON_EXIT"

    /**
     * Pay at the entry gate.
     */
    public const val PAY_ON_ENTRY: String = "PAY_ON_ENTRY"

    /**
     * Pay using a parking meter.
     */
    public const val PARKING_METER: String = "PARKING_METER"

    /**
     * Multi-space parking meter.
     */
    public const val MULTI_SPACE_METER: String = "MULTI_SPACE_METER"

    /**
     * Honesty box payment (self-service without enforcement).
     */
    public const val HONESTY_BOX: String = "HONESTY_BOX"

    /**
     * Pay through an on-site parking attendant.
     */
    public const val ATTENDANT: String = "ATTENDANT"

    /**
     * Pay by entering the license plate number.
     */
    public const val PAY_BY_PLATE: String = "PAY_BY_PLATE"

    /**
     * Pay at the reception desk of a building or business.
     */
    public const val PAY_AT_RECEPTION: String = "PAY_AT_RECEPTION"

    /**
     * Pay using a phone call or mobile app.
     */
    public const val PAY_BY_PHONE: String = "PAY_BY_PHONE"

    /**
     * Pay using a coupon system.
     */
    public const val PAY_BY_COUPON: String = "PAY_BY_COUPON"

    /**
     * Electronic parking system payment.
     */
    public const val ELECTRONIC_PARKING_SYSTEM: String = "ELECTRONIC_PARKING_SYSTEM"

    /**
     * Payment method is unknown.
     */
    public const val UNKNOWN: String = "UNKNOWN"

    /**
     * Retention policy for [ParkingPaymentMethod] values.
     */
    @Retention(AnnotationRetention.BINARY)
    @StringDef(
        PAY_ON_FOOT,
        PAY_AND_DISPLAY,
        PAY_ON_EXIT,
        PAY_ON_ENTRY,
        PARKING_METER,
        MULTI_SPACE_METER,
        HONESTY_BOX,
        ATTENDANT,
        PAY_BY_PLATE,
        PAY_AT_RECEPTION,
        PAY_BY_PHONE,
        PAY_BY_COUPON,
        ELECTRONIC_PARKING_SYSTEM,
        UNKNOWN,
    )
    public annotation class Type
}
