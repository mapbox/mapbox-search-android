package com.mapbox.search.common.metadata

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Objects

/**
 * Availability information for the POI.
 */
public abstract class OpenHours internal constructor() : Parcelable {

    /**
     * POI is always opened.
     */
    @Parcelize
    public object AlwaysOpen : OpenHours()

    /**
     * POI is closed, but may be opened in the future.
     */
    @Parcelize
    public object TemporaryClosed : OpenHours()

    /**
     * POI is always closed
     */
    @Parcelize
    public object PermanentlyClosed : OpenHours()

    /**
     * POI has schedule for open/closed hours.
     */
    @Parcelize
    public class Scheduled(
        /**
         * Non-empty list of time periods, when POI is opened.
         */
        public val periods: List<OpenPeriod>,

        /**
         * Array of strings representing the opening hours for each day of the week,
         * Monday being the first day of week.
         */
        public val weekdayText: List<String>?,

        public val note: String?,
    ) : OpenHours() {

        init {
            require(periods.isNotEmpty()) { "List of time periods should not be empty!" }
        }

        /**
         * @suppress
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Scheduled

            if (periods != other.periods) return false
            if (weekdayText != other.weekdayText) return false
            if (note != other.note) return false

            return true
        }

        /**
         * @suppress
         */
        override fun hashCode(): Int {
            return Objects.hash(periods, weekdayText, note)
        }

        /**
         * @suppress
         */
        override fun toString(): String {
            return "Scheduled(periods=$periods, weekdayText=$weekdayText, note=$note)"
        }
    }
}
