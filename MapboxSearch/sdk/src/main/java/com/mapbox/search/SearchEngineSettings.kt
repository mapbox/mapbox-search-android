package com.mapbox.search

import android.Manifest
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.search.base.location.defaultLocationEngine

/**
 * Settings used for [SearchEngine] configuration.
 * @see SearchEngine
 */
public class SearchEngineSettings @JvmOverloads constructor(

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
    public val locationEngine: LocationEngine = defaultLocationEngine(),

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
        accessToken: String = this.accessToken,
        locationEngine: LocationEngine = this.locationEngine,
        viewportProvider: ViewportProvider? = this.viewportProvider,
        baseUrl: String? = this.baseUrl,
    ): SearchEngineSettings = SearchEngineSettings(
        accessToken = accessToken,
        locationEngine = locationEngine,
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

        if (accessToken != other.accessToken) return false
        if (locationEngine != other.locationEngine) return false
        if (viewportProvider != other.viewportProvider) return false
        if (baseUrl != other.baseUrl) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = accessToken.hashCode()
        result = 31 * result + locationEngine.hashCode()
        result = 31 * result + (viewportProvider?.hashCode() ?: 0)
        result = 31 * result + (baseUrl?.hashCode() ?: 0)
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "SearchEngineSettings(" +
                "accessToken='$accessToken', " +
                "locationEngine=$locationEngine, " +
                "viewportProvider=$viewportProvider, " +
                "baseUrl=$baseUrl" +
                ")"
    }

    /**
     * Builder for [SearchEngineSettings].
     */
    public class Builder(

        /**
         * [Mapbox Access Token](https://docs.mapbox.com/help/glossary/access-token/).
         */
        public var accessToken: String,
    ) {

        private var locationEngine: LocationEngine? = null
        private var viewportProvider: ViewportProvider? = null
        private var baseUrl: String? = null

        internal constructor(settings: SearchEngineSettings) : this(settings.accessToken) {
            locationEngine = settings.locationEngine
            viewportProvider = settings.viewportProvider
            baseUrl = settings.baseUrl
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
         * Base endpoint URL.
         */
        public fun baseUrl(baseUrl: String?): Builder = apply {
            this.baseUrl = baseUrl
        }

        /**
         * Create [SearchEngineSettings] instance from builder data.
         */
        public fun build(): SearchEngineSettings = SearchEngineSettings(
            accessToken = accessToken,
            locationEngine = locationEngine ?: defaultLocationEngine(),
            viewportProvider = viewportProvider,
            baseUrl = baseUrl,
        )
    }
}
