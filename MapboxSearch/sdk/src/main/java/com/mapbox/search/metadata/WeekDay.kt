package com.mapbox.search.metadata

/**
 * Enum, representing each day of the week - Monday, Tuesday, Wednesday, Thursday, Friday, Saturday and Sunday.
 */
public enum class WeekDay(

    /**
     * ISO-8601 standard code for day of the week, from 1 (Monday) to 7 (Sunday).
     */
    public val isoCode: Int,

    /**
     * Internal SDK raw representation for day of the week.
     */
    internal val rawCode: Byte
) {

    /**
     * Instance of [WeekDay] for the Monday. ISO code is 1.
     */
    MONDAY(1, 0),

    /**
     * Instance of [WeekDay] for the Tuesday. ISO code is 2.
     */
    TUESDAY(2, 1),

    /**
     * Instance of [WeekDay] for the Wednesday. ISO code is 3.
     */
    WEDNESDAY(3, 2),

    /**
     * Instance of [WeekDay] for the Thursday. ISO code is 4.
     */
    THURSDAY(4, 3),

    /**
     * Instance of [WeekDay] for the Friday. ISO code is 5.
     */
    FRIDAY(5, 4),

    /**
     * Instance of [WeekDay] for the Saturday. ISO code is 6.
     */
    SATURDAY(6, 5),

    /**
     * Instance of [WeekDay] for the Sunday. ISO code is 7.
     */
    SUNDAY(7, 6);

    @JvmSynthetic
    internal fun toCore(): Byte = rawCode

    internal companion object {

        @JvmSynthetic
        fun fromCore(dayCode: Byte): WeekDay {
            return values().firstOrNull { it.rawCode == dayCode }
                ?: throw IllegalArgumentException("Unknown day code (=$dayCode) from Core SDK.")
        }
    }
}
