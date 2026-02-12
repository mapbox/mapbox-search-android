package com.mapbox.search

import android.os.Parcelable
import com.mapbox.geojson.Point
import com.mapbox.search.Reserved.Flags.SBS
import com.mapbox.search.Reserved.Flags.SEARCH_BOX
import com.mapbox.search.base.utils.printableName
import kotlinx.parcelize.Parcelize
import java.util.concurrent.TimeUnit
import kotlin.math.floor

/**
 * Options to configure Route for search along the route functionality.
 *
 * Note: Supported for Single Box Search and Search Box APIs only. Reserved for internal and special use.
 *
 * @property route route to search across; please note, at least 2 points should be provided.
 * @property deviation option describing maximum detour from route.
 */
@Reserved(SBS, SEARCH_BOX)
@Parcelize
public class RouteOptions(
    public val route: List<Point>,
    public val deviation: Deviation
) : Parcelable {

    init {
        require(route.size > 1) {
            "Route should contain at least 2 points!"
        }
    }

    @get:JvmSynthetic
    internal val timeDeviationMinutes: Double
        get() = when (deviation) {
            is Deviation.Time -> deviation.preciseDeviationInMinutes()
            else -> error("Unknown Deviation subclass: ${deviation.javaClass.printableName}.")
        }

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RouteOptions

        if (route != other.route) return false
        if (deviation != other.deviation) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = route.hashCode()
        result = 31 * result + deviation.hashCode()
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "RouteOptions(" +
                "route=$route, " +
                "deviation=$deviation" +
                ")"
    }

    /**
     * Option describing maximum detour from route.
     *
     * Note: Supported for Single Box Search and Search Box APIs only. Reserved for internal and special use.
     */
    @Reserved(SBS, SEARCH_BOX)
    public abstract class Deviation internal constructor() : Parcelable {

        /**
         * Indicates that the caller intends to perform a higher cost search along a route.
         */
        public abstract val sarType: SarType?

        /**
         * Type of Search-Along-the-Route algorithm.
         *
         * Note: Supported for Single Box Search and Search Box APIs only. Reserved for internal and special use.
         *
         * @property rawName raw name of SAR type, accepted by backend.
         */
        @Reserved(SBS, SEARCH_BOX)
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
             * @suppress
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
         * Note: Supported for Single Box Search and Search Box APIs only. Reserved for internal and special use.
         *
         * @property value deviation time value.
         * @property unit deviation time unit.
         * @property sarType algorithm of deviation calculation.
         */
        @Reserved(SBS, SEARCH_BOX)
        @Parcelize
        public class Time @JvmOverloads public constructor(
            public val value: Long,
            public val unit: TimeUnit,
            override val sarType: SarType? = SarType.ISOCHROME
        ) : Deviation() {

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

            internal companion object {

                // TODO: Find better way to not lose in precision due to conversion
                @JvmSynthetic
                fun fromMinutes(minutes: Double, sarType: SarType?): Time {
                    return if (floor(minutes) == minutes) {
                        Time(minutes.toLong(), TimeUnit.MINUTES, sarType)
                    } else {
                        val nanos = (minutes * TimeUnit.MINUTES.toNanos(1)).toLong()
                        Time(nanos, TimeUnit.NANOSECONDS, sarType)
                    }
                }
            }
        }
    }
}
