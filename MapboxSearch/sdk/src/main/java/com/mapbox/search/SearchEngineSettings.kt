package com.mapbox.search

import android.Manifest
import android.app.Application
import android.content.Context
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.search.location.LocationEngineAdapter
import com.mapbox.search.location.WrapperLocationProvider

/**
 * Settings used for [SearchEngine] configuration.
 * @see SearchEngine
 */
public class SearchEngineSettings @JvmOverloads constructor(

    /**
     * The Context of the Android Application.
     */
    public val applicationContext: Context,

    /**
     * [Mapbox Access Token](https://docs.mapbox.com/help/glossary/access-token/).
     */
    public val accessToken: String,

    /**
     * The mechanism responsible for providing location approximations to the SDK.
     * By default [LocationEngine] is retrieved from [LocationEngineProvider.getBestLocationEngine].
     * Note that this class requires [Manifest.permission.ACCESS_COARSE_LOCATION] or
     * [Manifest.permission.ACCESS_FINE_LOCATION] to work properly.
     */
    public val locationEngine: LocationEngine = defaultLocationEngine(applicationContext),

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
) {

    internal val application: Application
        get() = applicationContext.applicationContext as Application

    internal val wrapperLocationProvider: WrapperLocationProvider
        get() = WrapperLocationProvider(
            LocationEngineAdapter(application, locationEngine),
            viewportProvider
        )

    /**
     * Creates a copy of this object with overridden parameters.
     */
    @JvmSynthetic
    public fun copy(
        applicationContext: Context = this.applicationContext,
        accessToken: String = this.accessToken,
        locationEngine: LocationEngine = this.locationEngine,
        viewportProvider: ViewportProvider? = this.viewportProvider,
        geocodingEndpointBaseUrl: String = this.geocodingEndpointBaseUrl,
        singleBoxSearchBaseUrl: String? = this.singleBoxSearchBaseUrl,
    ): SearchEngineSettings = SearchEngineSettings(
        applicationContext = applicationContext,
        accessToken = accessToken,
        locationEngine = locationEngine,
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

        if (applicationContext != other.applicationContext) return false
        if (accessToken != other.accessToken) return false
        if (locationEngine != other.locationEngine) return false
        if (viewportProvider != other.viewportProvider) return false
        if (geocodingEndpointBaseUrl != other.geocodingEndpointBaseUrl) return false
        if (singleBoxSearchBaseUrl != other.singleBoxSearchBaseUrl) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = applicationContext.hashCode()
        result = 31 * result + accessToken.hashCode()
        result = 31 * result + locationEngine.hashCode()
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
                "applicationContext=$applicationContext, " +
                "accessToken='$accessToken', " +
                "locationEngine=$locationEngine, " +
                "viewportProvider=$viewportProvider, " +
                "geocodingEndpointBaseUrl='$geocodingEndpointBaseUrl', " +
                "singleBoxSearchBaseUrl=$singleBoxSearchBaseUrl" +
                ")"
    }

    /**
     * Builder for [SearchEngineSettings].
     */
    public class Builder(

        /**
         * The Context of the Android Application.
         */
        public var applicationContext: Context,

        /**
         * [Mapbox Access Token](https://docs.mapbox.com/help/glossary/access-token/).
         */
        public var accessToken: String,
    ) {

        private var locationEngine: LocationEngine? = null
        private var viewportProvider: ViewportProvider? = null
        private var geocodingEndpointBaseUrl: String? = null
        private var singleBoxSearchBaseUrl: String? = null

        internal constructor(settings: SearchEngineSettings) : this(settings.applicationContext, settings.accessToken) {
            locationEngine = settings.locationEngine
            viewportProvider = settings.viewportProvider
            geocodingEndpointBaseUrl = settings.geocodingEndpointBaseUrl
            singleBoxSearchBaseUrl = settings.singleBoxSearchBaseUrl
        }

        /**
         * The mechanism responsible for providing location approximations to the SDK.
         * By default [LocationEngine] is retrieved from [LocationEngineProvider.getBestLocationEngine].
         * Note that this class requires [Manifest.permission.ACCESS_COARSE_LOCATION] or
         * [Manifest.permission.ACCESS_FINE_LOCATION] to work properly.
         */
        public fun locationEngine(locationEngine: LocationEngine): Builder = apply {
            this.locationEngine = locationEngine
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
            applicationContext = applicationContext,
            accessToken = accessToken,
            locationEngine = locationEngine ?: defaultLocationEngine(applicationContext),
            viewportProvider = viewportProvider,
            geocodingEndpointBaseUrl = geocodingEndpointBaseUrl ?: DEFAULT_ENDPOINT_GEOCODING,
            singleBoxSearchBaseUrl = singleBoxSearchBaseUrl,
        )
    }

    internal companion object {
        const val DEFAULT_ENDPOINT_GEOCODING: String = "https://api.mapbox.com"
        fun defaultLocationEngine(context: Context) = LocationEngineProvider.getBestLocationEngine(context)
    }
}
