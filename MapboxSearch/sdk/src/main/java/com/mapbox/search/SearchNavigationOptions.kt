package com.mapbox.search

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Additional search options to improve navigation experience.
 *
 * If only [navigationProfile] is provided, search results will not have information about ETA,
 * but search ranking logic will be affected: the faster you can walk/drive to the point,
 * the higher search result rank.
 *
 * If both [navigationProfile] and [etaType] are provided, it indicates that the caller intends
 * to perform a higher cost navigation ETA estimate. Please note, either [SearchOptions.origin]
 * should be specified or [com.mapbox.android.core.location.LocationEngine] should provide location in order to calculate
 * ETA. Also, ETA will not be calculated, if it's impossible to build a route from provided
 * origin point to the destination.
 */
@Parcelize
public class SearchNavigationOptions @JvmOverloads public constructor(

    /**
     * Used to calculate ETA from [SearchOptions.origin] point to a search suggestion
     * and search result or to affect search ranking logic.
     */
    public val navigationProfile: SearchNavigationProfile,

    /**
     * Indicates that the caller intends to perform a higher cost navigation ETA estimate.
     * Please note, that either [SearchOptions.origin] should be specified or
     * [com.mapbox.android.core.location.LocationEngine] should provide location in order to calculate ETA.
     * Also, ETA will not be calculated, if it's impossible to build a route
     * from provided origin point to the destination.
     */
    public val etaType: EtaType? = null
) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SearchNavigationOptions

        if (navigationProfile != other.navigationProfile) return false
        if (etaType != other.etaType) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = navigationProfile.hashCode()
        result = 31 * result + (etaType?.hashCode() ?: 0)
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "SearchNavigationOptions(" +
                "navigationProfile=$navigationProfile, " +
                "etaType=$etaType" +
                ")"
    }
}
