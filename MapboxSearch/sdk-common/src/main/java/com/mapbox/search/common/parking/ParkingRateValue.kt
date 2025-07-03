package com.mapbox.search.common.parking

import android.os.Parcelable
import com.mapbox.annotation.MapboxExperimental
import kotlinx.parcelize.Parcelize

/**
 * An ISO8601 interval denoting a fixed or additional duration [IsoValue] OR a custom duration value
 * represented by [ParkingRateCustomDurationValue.Type]
 */
@MapboxExperimental
public abstract class ParkingRateValue internal constructor() : Parcelable {

    /**
     * @property value ISO8601 interval.
     */
    @Parcelize
    public class IsoValue(public val value: String) : ParkingRateValue() {

        /**
         * @suppress
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as IsoValue

            return value == other.value
        }

        /**
         * @suppress
         */
        override fun hashCode(): Int {
            return value.hashCode()
        }

        /**
         * @suppress
         */
        override fun toString(): String {
            return "IsoValue(value='$value')"
        }
    }

    /**
     * @property value Custom duration value, one of [ParkingRateCustomDurationValue.Type].
     */
    @Parcelize
    public class CustomDurationValue(public val value: String) : ParkingRateValue() {

        /**
         * @suppress
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as CustomDurationValue

            return value == other.value
        }

        /**
         * @suppress
         */
        override fun hashCode(): Int {
            return value.hashCode()
        }

        /**
         * @suppress
         */
        override fun toString(): String {
            return "CustomDurationValue(value='$value')"
        }
    }
}
