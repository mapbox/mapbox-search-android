package com.mapbox.search.common.parking

import androidx.annotation.StringDef
import com.mapbox.annotation.MapboxExperimental

/**
 * Custom parking rate duration values.
 */
@MapboxExperimental
public object ParkingRateCustomDurationValue {

    /**
     * Parking rate for a 6-month period, Monday to Friday.
     */
    public const val SIX_MONTHS_MON_FRI: String = "SIX_MONTHS_MON_FRI"

    /**
     * Parking rate applies during a bank holiday.
     */
    public const val BANK_HOLIDAY: String = "BANK_HOLIDAY"

    /**
     * Parking rate applies during daytime hours.
     */
    public const val DAYTIME: String = "DAYTIME"

    /**
     * Early bird discount rate.
     */
    public const val EARLY_BIRD: String = "EARLY_BIRD"

    /**
     * Parking rate applies in the evening.
     */
    public const val EVENING: String = "EVENING"

    /**
     * Flat rate parking fee.
     */
    public const val FLAT_RATE: String = "FLAT_RATE"

    /**
     * Maximum parking fee limit.
     */
    public const val MAX: String = "MAX"

    /**
     * Maximum parking fee applied only once per period.
     */
    public const val MAX_ONLY_ONCE: String = "MAX_ONLY_ONCE"

    /**
     * Minimum parking fee.
     */
    public const val MINIMUM: String = "MINIMUM"

    /**
     * Monthly parking rate.
     */
    public const val MONTH: String = "MONTH"

    /**
     * Monthly rate, Monday to Friday.
     */
    public const val MONTH_MON_FRI: String = "MONTH_MON_FRI"

    /**
     * Monthly reserved parking space.
     */
    public const val MONTH_RESERVED: String = "MONTH_RESERVED"

    /**
     * Monthly unreserved parking space.
     */
    public const val MONTH_UNRESERVED: String = "MONTH_UNRESERVED"

    /**
     * Overnight parking rate.
     */
    public const val OVERNIGHT: String = "OVERNIGHT"

    /**
     * Quarterly rate, Monday to Friday.
     */
    public const val QUARTER_MON_FRI: String = "QUARTER_MON_FRI"

    /**
     * Rate applies until closing time.
     */
    public const val UNTIL_CLOSING: String = "UNTIL_CLOSING"

    /**
     * Weekend parking rate.
     */
    public const val WEEKEND: String = "WEEKEND"

    /**
     * Yearly rate, Monday to Friday.
     */
    public const val YEAR_MON_FRI: String = "YEAR_MON_FRI"

    /**
     * Retention policy for [ParkingRateCustomDurationValue] values.
     */
    @Retention(AnnotationRetention.BINARY)
    @StringDef(
        SIX_MONTHS_MON_FRI,
        BANK_HOLIDAY,
        DAYTIME,
        EARLY_BIRD,
        EVENING,
        FLAT_RATE,
        MAX,
        MAX_ONLY_ONCE,
        MINIMUM,
        MONTH,
        MONTH_MON_FRI,
        MONTH_RESERVED,
        MONTH_UNRESERVED,
        OVERNIGHT,
        QUARTER_MON_FRI,
        UNTIL_CLOSING,
        WEEKEND,
        YEAR_MON_FRI,
    )
    public annotation class Type
}
