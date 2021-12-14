package com.mapbox.search

import com.mapbox.search.core.CoreOfflineIndexError

/**
 * Type that holds information about error event in the offline search index.
 */
public class OfflineIndexErrorEvent(

    /**
     * The tile region identifier. This is the same identifier that was passed as a parameter to any of
     * [com.mapbox.common.TileStore] functions, such as [com.mapbox.common.TileStore.loadTileRegion].
     */
    public val regionId: String,

    /**
     * Dataset of the region for which this error event is generated.
     *
     * See [OfflineSearchEngine.createTilesetDescriptor], [OfflineSearchEngine.createPlacesTilesetDescriptor].
     */
    public val dataset: String,

    /**
     * Version of the dataset for which this error event is generated.
     *
     * See [OfflineSearchEngine.createTilesetDescriptor], [OfflineSearchEngine.createPlacesTilesetDescriptor]
     */
    public val version: String,

    /**
     * The tile for which this error event is generated.
     */
    public val tile: String,

    /**
     * The detail error message string.
     */
    public val message: String,
) {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OfflineIndexErrorEvent

        if (regionId != other.regionId) return false
        if (dataset != other.dataset) return false
        if (version != other.version) return false
        if (tile != other.tile) return false
        if (message != other.message) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = regionId.hashCode()
        result = 31 * result + dataset.hashCode()
        result = 31 * result + version.hashCode()
        result = 31 * result + tile.hashCode()
        result = 31 * result + message.hashCode()
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "OfflineIndexError(" +
                "regionId='$regionId', " +
                "dataset='$dataset', " +
                "version='$version', " +
                "tile='$tile', " +
                "message='$message'" +
                ")"
    }
}

@JvmSynthetic
internal fun CoreOfflineIndexError.mapToPlatformType(): OfflineIndexErrorEvent {
    return OfflineIndexErrorEvent(
        regionId = region,
        dataset = dataset,
        version = version,
        tile = tile,
        message = message,
    )
}
