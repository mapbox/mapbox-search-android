package com.mapbox.search

import android.Manifest
import com.mapbox.common.location.LocationProvider
import com.mapbox.common.location.LocationServiceFactory
import com.mapbox.search.base.location.defaultLocationProvider

/**
 * Settings used for [SearchEngine] configuration.
 * @see SearchEngine
 */
public class SearchEngineSettings @JvmOverloads constructor(

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
     * Geocoding API endpoint URL.
     */
    public val geocodingEndpointBaseUrl: String = DEFAULT_ENDPOINT_GEOCODING,

    /**
     * Single Box Search endpoint URL.
     */
    public val singleBoxSearchBaseUrl: String? = null,

    /**
     * SearchBox API end point URL
     */
    public val searchBoxEndpointUrl: String? = null,
) {

    /**
     * Creates a copy of this object with overridden parameters.
     */
    @JvmSynthetic
    public fun copy(
        locationProvider: LocationProvider? = this.locationProvider,
        viewportProvider: ViewportProvider? = this.viewportProvider,
        geocodingEndpointBaseUrl: String = this.geocodingEndpointBaseUrl,
        singleBoxSearchBaseUrl: String? = this.singleBoxSearchBaseUrl,
    ): SearchEngineSettings = SearchEngineSettings(
        locationProvider = locationProvider,
        viewportProvider = viewportProvider,
        geocodingEndpointBaseUrl = geocodingEndpointBaseUrl,
        singleBoxSearchBaseUrl = singleBoxSearchBaseUrl,
    )

    /**
     * Creates a new [SearchEngineSettings.Builder] from this instance.
     */
    public fun toBuilder(): Builder = Builder(this)

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SearchEngineSettings

        if (locationProvider != other.locationProvider) return false
        if (viewportProvider != other.viewportProvider) return false
        if (geocodingEndpointBaseUrl != other.geocodingEndpointBaseUrl) return false
        if (singleBoxSearchBaseUrl != other.singleBoxSearchBaseUrl) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = locationProvider.hashCode()
        result = 31 * result + (viewportProvider?.hashCode() ?: 0)
        result = 31 * result + geocodingEndpointBaseUrl.hashCode()
        result = 31 * result + (singleBoxSearchBaseUrl?.hashCode() ?: 0)
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "SearchEngineSettings(" +
                "locationProvider=$locationProvider, " +
                "viewportProvider=$viewportProvider, " +
                "geocodingEndpointBaseUrl='$geocodingEndpointBaseUrl', " +
                "singleBoxSearchBaseUrl=$singleBoxSearchBaseUrl" +
                ")"
    }

    /**
     * Builder for [SearchEngineSettings].
     */
    public class Builder() {

        private var locationProvider: LocationProvider ? = null
        private var viewportProvider: ViewportProvider? = null
        private var geocodingEndpointBaseUrl: String? = null
        private var singleBoxSearchBaseUrl: String? = null

        internal constructor(settings: SearchEngineSettings) : this() {
            locationProvider = settings.locationProvider
            viewportProvider = settings.viewportProvider
            geocodingEndpointBaseUrl = settings.geocodingEndpointBaseUrl
            singleBoxSearchBaseUrl = settings.singleBoxSearchBaseUrl
        }

        /**
         * The mechanism responsible for providing location approximations to the SDK.
         * By default [LocationProvider] is provided by [LocationServiceFactory].
         * Note that this class requires [Manifest.permission.ACCESS_COARSE_LOCATION] or
         * [Manifest.permission.ACCESS_FINE_LOCATION] to work properly.
         */
        public fun locationProvider(locationProvider: LocationProvider): Builder = apply {
            this.locationProvider = locationProvider
        }

        /**
         * Viewport provider instance.
         */
        public fun viewportProvider(viewportProvider: ViewportProvider?): Builder = apply {
            this.viewportProvider = viewportProvider
        }

        /**
         * Geocoding API endpoint URL.
         */
        public fun geocodingEndpointBaseUrl(geocodingEndpointBaseUrl: String): Builder = apply {
            this.geocodingEndpointBaseUrl = geocodingEndpointBaseUrl
        }

        /**
         * Single Box Search endpoint URL.
         */
        public fun singleBoxSearchBaseUrl(singleBoxSearchBaseUrl: String?): Builder = apply {
            this.singleBoxSearchBaseUrl = singleBoxSearchBaseUrl
        }

        /**
         * Create [SearchEngineSettings] instance from builder data.
         */
        public fun build(): SearchEngineSettings = SearchEngineSettings(
            locationProvider = locationProvider ?: defaultLocationProvider(),
            viewportProvider = viewportProvider,
            geocodingEndpointBaseUrl = geocodingEndpointBaseUrl ?: DEFAULT_ENDPOINT_GEOCODING,
            singleBoxSearchBaseUrl = singleBoxSearchBaseUrl,
        )
    }

    internal companion object {
        const val DEFAULT_ENDPOINT_GEOCODING: String = "https://api.mapbox.com"
    }
}
