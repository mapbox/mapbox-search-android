package com.mapbox.search

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Type of ETA calculation.
 * @property rawName raw name of ETA type, accepted by backend.
 */
@Parcelize
public class EtaType(public val rawName: String) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EtaType

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
        return "EtaType(rawName='$rawName')"
    }

    /**
     * Companion object.
     */
    public companion object {

        /**
         * Navigation ETA calculation type.
         */
        @JvmField
        public val NAVIGATION: EtaType = EtaType("navigation")
    }
}
