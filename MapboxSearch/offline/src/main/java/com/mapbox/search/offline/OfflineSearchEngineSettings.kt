package com.mapbox.search.offline

import android.Manifest
import com.mapbox.common.TileDataDomain
import com.mapbox.common.TileStore
import com.mapbox.common.TileStoreOptions
import com.mapbox.common.location.LocationService
import com.mapbox.common.location.LocationServiceFactory
import com.mapbox.search.base.location.defaultLocationService
import java.net.URI

/**
 * Settings used for [OfflineSearchEngine] configuration.
 * @see OfflineSearchEngine
 */
public class OfflineSearchEngineSettings @JvmOverloads constructor(

    /**
     * [Mapbox Access Token](https://docs.mapbox.com/help/glossary/access-token/).
     */
    public val accessToken: String,

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

    /**
     * The mechanism responsible for providing location approximations to the SDK.
     * By default [LocationService] is retrieved from [LocationServiceFactory.getOrCreate].
     * Note that this class requires [Manifest.permission.ACCESS_COARSE_LOCATION] or
     * [Manifest.permission.ACCESS_FINE_LOCATION] to work properly.
     */
    public val locationService: LocationService = defaultLocationService(),
) {

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

        if (accessToken != other.accessToken) return false
        if (locationService != other.locationService) return false
        if (tileStore != other.tileStore) return false
        if (tilesBaseUri != other.tilesBaseUri) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = accessToken.hashCode()
        result = 31 * result + locationService.hashCode()
        result = 31 * result + tileStore.hashCode()
        result = 31 * result + tilesBaseUri.hashCode()
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "OfflineSearchEngineSettings(" +
                "accessToken='$accessToken', " +
                "locationEngine=$locationService, " +
                "tileStore=$tileStore, " +
                "tilesBaseUri=$tilesBaseUri" +
                ")"
    }

    /**
     * Builder for [OfflineSearchEngineSettings].
     */
    public class Builder(

        /**
         * [Mapbox Access Token](https://docs.mapbox.com/help/glossary/access-token/).
         */
        public var accessToken: String,
    ) {

        private var locationService: LocationService? = null
        private var tileStore: TileStore? = null
        private var tilesBaseUri: URI? = null

        internal constructor(settings: OfflineSearchEngineSettings) : this(settings.accessToken) {
            locationService = settings.locationService
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
         * By default [LocationService] is retrieved from [LocationServiceFactory.getOrCreate].
         * Note that this class requires [Manifest.permission.ACCESS_COARSE_LOCATION] or
         * [Manifest.permission.ACCESS_FINE_LOCATION] to work properly.
         */
        public fun locationService(locationService: LocationService): Builder = apply {
            this.locationService = locationService
        }

        /**
         * Create [OfflineSearchEngineSettings] instance from builder data.
         */
        public fun build(): OfflineSearchEngineSettings = OfflineSearchEngineSettings(
            accessToken = accessToken,
            tileStore = tileStore ?: defaultTileStore(),
            tilesBaseUri = tilesBaseUri ?: DEFAULT_ENDPOINT_URI,
            locationService = locationService ?: defaultLocationService(),
        )
    }

    internal companion object {

        val DEFAULT_ENDPOINT_URI: URI = URI.create("https://api.mapbox.com")
        const val DEFAULT_DATASET = "mbx-main"
        const val DEFAULT_VERSION = ""

        private fun defaultTileStore() = TileStore.create()
    }
}
