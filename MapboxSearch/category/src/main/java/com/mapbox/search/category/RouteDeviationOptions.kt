package com.mapbox.search.category

import android.os.Parcelable
import com.mapbox.search.base.utils.printableName
import kotlinx.parcelize.Parcelize
import java.util.concurrent.TimeUnit

/**
 * Option describing maximum detour from route. Used for search along the route.
 */
public abstract class RouteDeviationOptions internal constructor() : Parcelable {

    /**
     * Indicates that the caller intends to perform a higher cost search along a route.
     */
    public abstract val sarType: SarType?

    @get:JvmSynthetic
    internal val timeDeviationMinutes: Double
        get() = when (this) {
            is Time -> preciseDeviationInMinutes()
            else -> error("Unknown Deviation subclass: ${javaClass.printableName}.")
        }

    /**
     * Type of Search-Along-the-Route algorithm.
     * @property rawName raw name of SAR type, accepted by backend.
     */
    @Parcelize
    public class SarType(public val rawName: String) : Parcelable {

        /**
         * @suppress
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as SarType

            if (rawName != other.rawName) return false

            return true
        }

        /**
         * @suppress
         */
        override fun hashCode(): Int {
            return rawName.hashCode()
        }

        /**
         * @suppress
         */
        override fun toString(): String {
            return "SarType(rawName='$rawName')"
        }

        /**
         * Companion object.
         */
        public companion object {

            /**
             * SAR calculation algorithm using isochrome mechanism.
             */
            @JvmField
            public val ISOCHROME: SarType = SarType("isochrone")
        }
    }

    /**
     * Maximum detour in time from route.
     *
     * @param value deviation time value.
     * @param unit deviation time unit.
     * @param sarType algorithm of deviation calculation.
     */
    @Parcelize
    public class Time @JvmOverloads public constructor(
        public val value: Long,
        public val unit: TimeUnit,
        override val sarType: SarType? = SarType.ISOCHROME
    ) : RouteDeviationOptions() {

        @JvmSynthetic
        internal fun preciseDeviationInMinutes(): Double {
            return when (unit) {
                TimeUnit.NANOSECONDS -> value.toDouble() / TimeUnit.MINUTES.toNanos(1)
                TimeUnit.MICROSECONDS -> value.toDouble() / TimeUnit.MINUTES.toMicros(1)
                TimeUnit.MILLISECONDS -> value.toDouble() / TimeUnit.MINUTES.toMillis(1)
                TimeUnit.SECONDS -> value.toDouble() / TimeUnit.MINUTES.toSeconds(1)
                TimeUnit.MINUTES,
                TimeUnit.HOURS,
                TimeUnit.DAYS -> unit.toMinutes(value).toDouble()
            }
        }

        /**
         * @suppress
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Time

            if (value != other.value) return false
            if (unit != other.unit) return false
            if (sarType != other.sarType) return false

            return true
        }

        /**
         * @suppress
         */
        override fun hashCode(): Int {
            var result = value.hashCode()
            result = 31 * result + unit.hashCode()
            result = 31 * result + (sarType?.hashCode() ?: 0)
            return result
        }

        /**
         * @suppress
         */
        override fun toString(): String {
            return "Time(" +
                    "value=$value, " +
                    "unit=$unit, " +
                    "sarType=$sarType" +
                    ")"
        }
    }

    internal companion object {
        val DEFAULT_DEVIATION = Time(value = 10L, unit = TimeUnit.MINUTES)
    }
}
