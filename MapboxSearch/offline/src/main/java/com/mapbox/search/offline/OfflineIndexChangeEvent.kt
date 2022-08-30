package com.mapbox.search.offline

import com.mapbox.search.base.core.CoreOfflineIndexChangeEvent
import com.mapbox.search.base.core.CoreOfflineIndexChangeEventType

/**
 * Type that holds information about changes in the offline search index.
 */
public class OfflineIndexChangeEvent internal constructor(

    /**
     * Type of event.
     */
    public val type: EventType,

    /**
     * The tile region identifier. This is the same identifier that was passed as a parameter to any of
     * [com.mapbox.common.TileStore] functions, such as [com.mapbox.common.TileStore.loadTileRegion].
     */
    public val regionId: String,

    /**
     * Dataset of the region for which this event is generated.
     *
     * See [OfflineSearchEngine.createTilesetDescriptor], [OfflineSearchEngine.createPlacesTilesetDescriptor].
     */
    public val dataset: String,

    /**
     * Version of the dataset for which this event is generated.
     *
     * See [OfflineSearchEngine.createTilesetDescriptor], [OfflineSearchEngine.createPlacesTilesetDescriptor]
     */
    public val version: String,
) {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OfflineIndexChangeEvent

        if (type != other.type) return false
        if (regionId != other.regionId) return false
        if (dataset != other.dataset) return false
        if (version != other.version) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + regionId.hashCode()
        result = 31 * result + dataset.hashCode()
        result = 31 * result + version.hashCode()
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "OfflineIndexChangeEvent(" +
                "type=$type, " +
                "regionId='$regionId', " +
                "dataset='$dataset', " +
                "version='$version'" +
                ")"
    }

    /**
     * Type of [OfflineIndexChangeEvent].
     */
    public enum class EventType {

        /**
         * [OfflineIndexChangeEvent] generated when new data added to the search index.
         */
        ADD,

        /**
         * [OfflineIndexChangeEvent] generated when data removed from the search index.
         */
        UPDATE,

        /**
         * [OfflineIndexChangeEvent] generated when data from the search index was updated.
         */
        REMOVE,
    }
}

@JvmSynthetic
internal fun CoreOfflineIndexChangeEventType.mapToPlatformType(): OfflineIndexChangeEvent.EventType {
    return when (this) {
        CoreOfflineIndexChangeEventType.ADDED -> OfflineIndexChangeEvent.EventType.ADD
        CoreOfflineIndexChangeEventType.REMOVED -> OfflineIndexChangeEvent.EventType.REMOVE
        CoreOfflineIndexChangeEventType.UPDATED -> OfflineIndexChangeEvent.EventType.UPDATE
    }
}

@JvmSynthetic
internal fun CoreOfflineIndexChangeEvent.mapToPlatformType(): OfflineIndexChangeEvent {
    return OfflineIndexChangeEvent(
        type = type.mapToPlatformType(),
        regionId = region,
        dataset = dataset,
        version = version,
    )
}
