package com.mapbox.search

import android.os.Parcelable
import com.mapbox.search.Reserved.Flags.SBS
import kotlinx.parcelize.Parcelize

/**
 * Type of movement. Used to calculate ETA from origin point to a search suggestion and search result.
 *
 * Note: Supported for Single Box Search API only. Reserved for internal and special use.
 *
 * @property rawName raw name of navigation profile, accepted by backend.
 */
@Reserved(SBS)
@Parcelize
public class SearchNavigationProfile(public val rawName: String) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SearchNavigationProfile

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
     * @suppress
     */
    public companion object {

        /**
         * Driving the car.
         *
         * Note: Supported for Single Box Search API only. Reserved for internal and special use.
         */
        @Reserved(SBS)
        @JvmField
        public val DRIVING: SearchNavigationProfile = SearchNavigationProfile("driving")

        /**
         * Cycling.
         *
         * Note: Supported for Single Box Search API only. Reserved for internal and special use.
         */
        @Reserved(SBS)
        @JvmField
        public val CYCLING: SearchNavigationProfile = SearchNavigationProfile("cycling")

        /**
         * Walking.
         *
         * Note: Supported for Single Box Search API only. Reserved for internal and special use.
         */
        @Reserved(SBS)
        @JvmField
        public val WALKING: SearchNavigationProfile = SearchNavigationProfile("walking")
    }
}
