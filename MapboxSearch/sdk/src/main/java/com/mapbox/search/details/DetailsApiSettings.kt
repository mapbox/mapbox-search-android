package com.mapbox.search.details

import android.Manifest
import com.mapbox.common.location.LocationProvider
import com.mapbox.common.location.LocationServiceFactory
import com.mapbox.search.ViewportProvider
import com.mapbox.search.base.ExperimentalMapboxSearchAPI
import com.mapbox.search.base.location.defaultLocationProvider

/**
 * Settings used for [DetailsApi] configuration.
 * @see DetailsApi
 */
@ExperimentalMapboxSearchAPI
public class DetailsApiSettings @JvmOverloads constructor(

    /**
     * The mechanism responsible for providing location approximations to the SDK.
     * By default [LocationProvider] is provided by [LocationServiceFactory].
     * Note that this class requires [Manifest.permission.ACCESS_COARSE_LOCATION] or
     * [Manifest.permission.ACCESS_FINE_LOCATION] to work properly.
     */
    public val locationProvider: LocationProvider? = defaultLocationProvider(),

    /**
     * Viewport provider instance.
     */
    public val viewportProvider: ViewportProvider? = null,

    /**
     * Base endpoint URL.
     */
    public val baseUrl: String? = null,
) {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DetailsApiSettings

        if (locationProvider != other.locationProvider) return false
        if (viewportProvider != other.viewportProvider) return false
        if (baseUrl != other.baseUrl) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = locationProvider?.hashCode() ?: 0
        result = 31 * result + (viewportProvider?.hashCode() ?: 0)
        result = 31 * result + (baseUrl?.hashCode() ?: 0)
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "DetailsApiSettings(" +
                "locationProvider=$locationProvider, " +
                "viewportProvider=$viewportProvider, " +
                "baseUrl=$baseUrl" +
                ")"
    }
}
