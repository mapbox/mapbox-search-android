package com.mapbox.search

import com.mapbox.common.TileDataDomain
import com.mapbox.common.TileStore
import com.mapbox.common.TileStoreOptions
import java.net.URI

/**
 * Defines options for offline tiles endpoint.
 */
public class OfflineSearchSettings(

    /**
     * Tile store instance. It manages downloads and storage for requests to
     * tile-related API endpoints. When creating the [TileStore] make sure to call
     * [TileStore.setOption] with [TileStoreOptions.MAPBOX_ACCESS_TOKEN] and your token.
     * If this parameter is null, the [TileStore] will be created with a token passed to [MapboxSearchSdk]
     * and [tilesBaseUri].
     *
     * You can reuse [TileStore] instance passed to Mapbox Maps or Navigation SDK.
     */
    public val tileStore: TileStore? = null,

    /**
     * Scheme and host, for example `"https://api.mapbox.com"`, chosen automatically if empty.
     * This Uri will be used for [TileStoreOptions.MAPBOX_APIURL] option with [TileDataDomain.SEARCH] domain.
     */
    public val tilesBaseUri: URI? = null,
) {

    internal fun tilesBaseUriOrDefault() = tilesBaseUri ?: DEFAULT_ENDPOINT_URI

    /**
     * Creates a copy of this object with overridden parameters.
     */
    @JvmSynthetic
    public fun copy(
        tileStore: TileStore? = this.tileStore,
        tilesBaseUri: URI? = this.tilesBaseUri,
    ): OfflineSearchSettings = OfflineSearchSettings(
        tileStore = tileStore,
        tilesBaseUri = tilesBaseUri,
    )

    /**
     * Creates a new [OfflineSearchSettings.Builder] from this instance.
     */
    public fun toBuilder(): Builder = Builder(this)

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OfflineSearchSettings

        if (tilesBaseUri != other.tilesBaseUri) return false
        if (tileStore != other.tileStore) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = tilesBaseUri?.hashCode() ?: 0
        result = 31 * result + (tileStore?.hashCode() ?: 0)
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "OfflineSearchSettings(" +
                "tilesBaseUri=$tilesBaseUri, " +
                "tileStore=$tileStore" +
                ")"
    }

    /**
     * Builder for [OfflineSearchSettings].
     */
    public class Builder() {

        private var tileStore: TileStore? = null
        private var tilesBaseUri: URI? = null

        internal constructor(settings: OfflineSearchSettings) : this() {
            tileStore = settings.tileStore
            tilesBaseUri = settings.tilesBaseUri
        }

        /**
         * Tile store instance. It manages downloads and storage for requests to
         * tile-related API endpoints. When creating the [TileStore] make sure to call
         * [TileStore.setOption] with [TileStoreOptions.MAPBOX_ACCESS_TOKEN] and your token.
         * By default (if `null` is provided here), the [TileStore] will be created with a token passed to [MapboxSearchSdk].
         *
         * You can reuse [TileStore] instance passed to Mapbox Maps or Navigation SDK.
        */
        public fun tileStore(tileStore: TileStore?): Builder = apply { this.tileStore = tileStore }

        /**
         * Scheme and host, for example `"https://api.mapbox.com"`, chosen automatically if empty.
         * This Uri will be used for [TileStoreOptions.MAPBOX_APIURL] option with [TileDataDomain.SEARCH] domain.
         */
        public fun tilesBaseUri(tilesBaseUri: URI?): Builder = apply { this.tilesBaseUri = tilesBaseUri }

        /**
         * Create [OfflineSearchSettings] instance from builder data.
         */
        public fun build(): OfflineSearchSettings = OfflineSearchSettings(
            tileStore = tileStore,
            tilesBaseUri = tilesBaseUri,
        )
    }

    internal companion object {
        val DEFAULT_ENDPOINT_URI: URI = URI.create("https://api-offline-search-staging.tilestream.net")
        const val DEFAULT_DATASET = "test-dataset"
        const val DEFAULT_VERSION = ""
    }
}
