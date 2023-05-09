package com.mapbox.search.common

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Type of movement. Used to calculate distance and time of arrival (ETA) from origin point
 * to a search suggestion and search result.
 *
 * @property rawName raw name of navigation profile, accepted by backend.
 */
@Parcelize
public class NavigationProfile(public val rawName: String) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NavigationProfile

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
        return "SearchNavigationProfile(rawName='$rawName')"
    }

    /**
     * Companion object.
     */
    public companion object {

        /**
         * Driving navigation profile.
         */
        @JvmField
        public val DRIVING: NavigationProfile = NavigationProfile("driving")

        /**
         * Cycling navigation profile.
         */
        @JvmField
        public val CYCLING: NavigationProfile = NavigationProfile("cycling")

        /**
         * Walking navigation profile.
         */
        @JvmField
        public val WALKING: NavigationProfile = NavigationProfile("walking")
    }
}
