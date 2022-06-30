package com.mapbox.search

import android.Manifest
import android.content.Context
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.bindgen.Value
import com.mapbox.common.TileDataDomain
import com.mapbox.common.TileStore
import com.mapbox.common.TileStoreOptions
import java.net.URI

/**
 * Settings used for [OfflineSearchEngine] configuration.
 */
public class OfflineSearchEngineSettings @JvmOverloads constructor(

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
     * Tile store instance. It manages downloads and storage for requests to
     * tile-related API endpoints. When creating the [TileStore] make sure to call
     * [TileStore.setOption] with [TileStoreOptions.MAPBOX_ACCESS_TOKEN] and your token.
     * By default, the [TileStore] will be created with [accessToken] and [tilesBaseUri].
     *
     * You can reuse [TileStore] instance passed to Mapbox Maps or Navigation SDK.
     */
    public val tileStore: TileStore = defaultTileStore(),

    /**
     * Scheme and host, for example `"https://api.mapbox.com"`, chosen automatically if empty.
     * This Uri will be used for [TileStoreOptions.MAPBOX_APIURL] option with [TileDataDomain.SEARCH] domain.
     */
    public val tilesBaseUri: URI = DEFAULT_ENDPOINT_URI,
) {

    // TODO should it be done automatically by the SDK or be responsibility of SDK user?
    internal fun initializeTileStore(): TileStore {
        tileStore.setOption(
            TileStoreOptions.MAPBOX_APIURL,
            TileDataDomain.SEARCH,
            Value.valueOf(tilesBaseUri.toString())
        )

        tileStore.setOption(
            TileStoreOptions.MAPBOX_ACCESS_TOKEN,
            TileDataDomain.SEARCH,
            Value.valueOf(accessToken)
        )

        return tileStore
    }

    /**
     * Creates a copy of this object with overridden parameters.
     */
    @JvmSynthetic
    public fun copy(
        applicationContext: Context = this.applicationContext,
        accessToken: String = this.accessToken,
        locationEngine: LocationEngine = this.locationEngine,
        viewportProvider: ViewportProvider? = this.viewportProvider,
        tileStore: TileStore = this.tileStore,
        tilesBaseUri: URI = this.tilesBaseUri,
    ): OfflineSearchEngineSettings = OfflineSearchEngineSettings(
        applicationContext = applicationContext,
        accessToken = accessToken,
        locationEngine = locationEngine,
        viewportProvider = viewportProvider,
        tileStore = tileStore,
        tilesBaseUri = tilesBaseUri,
    )

    /**
     * Creates a new [OfflineSearchEngineSettings.Builder] from this instance.
     */
    public fun toBuilder(): Builder = Builder(this)

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OfflineSearchEngineSettings

        if (applicationContext != other.applicationContext) return false
        if (accessToken != other.accessToken) return false
        if (locationEngine != other.locationEngine) return false
        if (viewportProvider != other.viewportProvider) return false
        if (tileStore != other.tileStore) return false
        if (tilesBaseUri != other.tilesBaseUri) return false

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
        result = 31 * result + tileStore.hashCode()
        result = 31 * result + tilesBaseUri.hashCode()
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "OfflineSearchEngineSettings(" +
                "applicationContext=$applicationContext, " +
                "accessToken='$accessToken', " +
                "locationEngine=$locationEngine, " +
                "viewportProvider=$viewportProvider, " +
                "tileStore=$tileStore, " +
                "tilesBaseUri=$tilesBaseUri" +
                ")"
    }

    /**
     * Builder for [OfflineSearchEngineSettings].
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
        private var tileStore: TileStore? = null
        private var tilesBaseUri: URI? = null

        internal constructor(settings: OfflineSearchEngineSettings) : this(settings.applicationContext, settings.accessToken) {
            locationEngine = settings.locationEngine
            viewportProvider = settings.viewportProvider
            tileStore = settings.tileStore
            tilesBaseUri = settings.tilesBaseUri
        }

        /**
         * Tile store instance. It manages downloads and storage for requests to
         * tile-related API endpoints. When creating the [TileStore] make sure to call
         * [TileStore.setOption] with [TileStoreOptions.MAPBOX_ACCESS_TOKEN] and your token.
         * By default, the [TileStore] will be created with [accessToken] and [tilesBaseUri].
         *
         * You can reuse [TileStore] instance passed to Mapbox Maps or Navigation SDK.
        */
        public fun tileStore(tileStore: TileStore): Builder = apply { this.tileStore = tileStore }

        /**
         * Scheme and host, for example `"https://api.mapbox.com"`, chosen automatically if empty.
         * This Uri will be used for [TileStoreOptions.MAPBOX_APIURL] option with [TileDataDomain.SEARCH] domain.
         */
        public fun tilesBaseUri(tilesBaseUri: URI?): Builder = apply { this.tilesBaseUri = tilesBaseUri }

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
         * Create [OfflineSearchEngineSettings] instance from builder data.
         */
        public fun build(): OfflineSearchEngineSettings = OfflineSearchEngineSettings(
            applicationContext = applicationContext,
            accessToken = accessToken,
            locationEngine = locationEngine ?: defaultLocationEngine(applicationContext),
            viewportProvider = viewportProvider,
            tileStore = tileStore ?: defaultTileStore(),
            tilesBaseUri = tilesBaseUri ?: DEFAULT_ENDPOINT_URI,
        )
    }

    internal companion object {

        val DEFAULT_ENDPOINT_URI: URI = URI.create("https://api-offline-search-staging.tilestream.net")
        const val DEFAULT_DATASET = "test-dataset"
        const val DEFAULT_VERSION = ""

        private fun defaultTileStore() = TileStore.create()
        private fun defaultLocationEngine(context: Context) = LocationEngineProvider.getBestLocationEngine(context)
    }
}
