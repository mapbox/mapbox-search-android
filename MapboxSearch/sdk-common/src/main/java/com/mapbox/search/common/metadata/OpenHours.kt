package com.mapbox.search.common.metadata

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

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
        public val periods: List<OpenPeriod>
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

            return true
        }

        /**
         * @suppress
         */
        override fun hashCode(): Int {
            return periods.hashCode()
        }

        /**
         * @suppress
         */
        override fun toString(): String {
            return "Scheduled(periods=$periods)"
        }
    }
}
