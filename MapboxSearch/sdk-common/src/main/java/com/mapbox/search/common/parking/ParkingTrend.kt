package com.mapbox.search.common.parking

import androidx.annotation.StringDef
import com.mapbox.annotation.MapboxExperimental

/**
 * Availability trend over time.
 */
@MapboxExperimental
public object ParkingTrend {

    /**
     * No change in parking availability.
     */
    public const val NO_CHANGE: String = "NO_CHANGE"

    /**
     * Parking availability is decreasing.
     */
    public const val DECREASING: String = "DECREASING"

    /**
     * Parking availability is increasing.
     */
    public const val INCREASING: String = "INCREASING"

    /**
     * Unknown trend.
     */
    public const val UNKNOWN: String = "UNKNOWN"

    /**
     * Retention policy for [ParkingTrend] values.
     */
    @Retention(AnnotationRetention.BINARY)
    @StringDef(
        NO_CHANGE,
        DECREASING,
        INCREASING,
        UNKNOWN,
    )
    public annotation class Type
}
