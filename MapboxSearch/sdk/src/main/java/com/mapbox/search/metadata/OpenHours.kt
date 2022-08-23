package com.mapbox.search.metadata

import com.mapbox.search.base.assertDebug
import com.mapbox.search.base.core.CoreOpenHours
import com.mapbox.search.base.core.CoreOpenMode
import com.mapbox.search.base.utils.printableName

/**
 * Availability information for the POI.
 */
public abstract class OpenHours {

    init {
        @Suppress("LeakingThis")
        require(this is AlwaysOpen ||
                this is TemporaryClosed ||
                this is PermanentlyClosed ||
                this is Scheduled) {
            "OpenHours allows only the following subclasses: " +
                    "[OpenHours.AlwaysOpen | OpenHours.TemporaryClosed | OpenHours.PermanentlyClosed | OpenHours.Scheduled], " +
                    "but ${javaClass.printableName} was found."
        }
    }

    /**
     * POI is always opened.
     */
    public object AlwaysOpen : OpenHours()

    /**
     * POI is closed, but may be opened in the future.
     */
    public object TemporaryClosed : OpenHours()

    /**
     * POI is always closed
     */
    public object PermanentlyClosed : OpenHours()

    /**
     * POI has schedule for open/closed hours.
     */
    public class Scheduled(

        /**
         * Non-empty list of time periods, when POI is opened.
         */
        public val periods: List<OpenPeriod>
    ) : OpenHours() {

        init {
            assertDebug(periods.isNotEmpty()) { "List of time periods should not be empty!" }
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

@JvmSynthetic
internal fun CoreOpenHours.mapToPlatform() = when (mode) {
    CoreOpenMode.ALWAYS_OPEN -> OpenHours.AlwaysOpen
    CoreOpenMode.TEMPORARILY_CLOSED -> OpenHours.TemporaryClosed
    CoreOpenMode.PERMANENTLY_CLOSED -> OpenHours.PermanentlyClosed
    CoreOpenMode.SCHEDULED -> OpenHours.Scheduled(periods = periods.map { it.mapToPlatform() })
}

@JvmSynthetic
internal fun OpenHours.mapToCore() = when (this) {
    OpenHours.AlwaysOpen -> CoreOpenHours(CoreOpenMode.ALWAYS_OPEN, emptyList())
    OpenHours.TemporaryClosed -> CoreOpenHours(CoreOpenMode.TEMPORARILY_CLOSED, emptyList())
    OpenHours.PermanentlyClosed -> CoreOpenHours(CoreOpenMode.PERMANENTLY_CLOSED, emptyList())
    is OpenHours.Scheduled -> CoreOpenHours(CoreOpenMode.SCHEDULED, periods.map { it.mapToCore() })
    else -> error("Unknown OpenHours subclass: ${javaClass.printableName}.")
}
