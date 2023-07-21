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
     * Base endpoint URL.
     */
    public val baseUrl: String? = null,
) {

    /**
     * Creates a copy of this object with overridden parameters.
     */
    @JvmSynthetic
    public fun copy(
        locationProvider: LocationProvider? = this.locationProvider,
        viewportProvider: ViewportProvider? = this.viewportProvider,
        baseUrl: String? = this.baseUrl,
    ): SearchEngineSettings = SearchEngineSettings(
        locationProvider = locationProvider,
        viewportProvider = viewportProvider,
        baseUrl = baseUrl,
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
        if (baseUrl != other.baseUrl) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = locationProvider.hashCode()
        result = 31 * result + (viewportProvider?.hashCode() ?: 0)
        result = 31 * result + (baseUrl?.hashCode() ?: 0)
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "SearchEngineSettings(" +
                "locationProvider=$locationProvider, " +
                "viewportProvider=$viewportProvider, " +
                "baseUrl=$baseUrl" +
                ")"
    }

    /**
     * Builder for [SearchEngineSettings].
     */
    public class Builder() {

        private var locationProvider: LocationProvider ? = null
        private var viewportProvider: ViewportProvider? = null
        private var baseUrl: String? = null

        internal constructor(settings: SearchEngineSettings) : this() {
            locationProvider = settings.locationProvider
            viewportProvider = settings.viewportProvider
            baseUrl = settings.baseUrl
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
         * Base endpoint URL.
         */
        public fun baseUrl(baseUrl: String?): Builder = apply {
            this.baseUrl = baseUrl
        }

        /**
         * Create [SearchEngineSettings] instance from builder data.
         */
        public fun build(): SearchEngineSettings = SearchEngineSettings(
            locationProvider = locationProvider ?: defaultLocationProvider(),
            viewportProvider = viewportProvider,
            baseUrl = baseUrl,
        )
    }
}
