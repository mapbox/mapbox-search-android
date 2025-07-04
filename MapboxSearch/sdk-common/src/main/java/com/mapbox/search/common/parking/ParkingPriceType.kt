package com.mapbox.search.common.parking

import androidx.annotation.StringDef
import com.mapbox.annotation.MapboxExperimental

/**
 * Type of parking price.
 */
@MapboxExperimental
public object ParkingPriceType {

    /**
     * Parking price is based on the duration of parking.
     */
    public const val DURATION: String = "DURATION"

    /**
     * Additional duration-based pricing after the initial period.
     */
    public const val DURATION_ADDITIONAL: String = "DURATION_ADDITIONAL"

    /**
     * Custom pricing scheme defined by the parking operator.
     */
    public const val CUSTOM: String = "CUSTOM"

    /**
     * Retention policy for [ParkingPaymentType] values.
     */
    @Retention(AnnotationRetention.BINARY)
    @StringDef(
        DURATION,
        DURATION_ADDITIONAL,
        CUSTOM,
    )
    public annotation class Type
}
