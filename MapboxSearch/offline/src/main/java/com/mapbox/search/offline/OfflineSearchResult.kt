package com.mapbox.search.offline

import android.os.Parcelable
import com.mapbox.annotation.MapboxExperimental
import com.mapbox.geojson.Point
import com.mapbox.search.base.failDebug
import com.mapbox.search.base.result.BaseRawSearchResult
import com.mapbox.search.base.utils.extension.mapToPlatform
import com.mapbox.search.common.RoutablePoint
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * Result returned by the offline search engine.
 */
@Parcelize
public class OfflineSearchResult internal constructor(
    internal val rawSearchResult: BaseRawSearchResult
) : Parcelable {

    @IgnoredOnParcel
    private val offlineType: OfflineSearchResultType

    init {
        check(rawSearchResult.center != null) {
            "Server search result must have a coordinate"
        }

        val type = rawSearchResult.types.firstNotNullOfOrNull { it.tryMapToOfflineSdkType() }
        offlineType = if (type == null) {
            failDebug {
                "Unsupported in offline SDK result types: $rawSearchResult.types. Fallback to ${OfflineSearchResultType.PLACE}"
            }
            OfflineSearchResultType.PLACE
        } else {
            type
        }
    }

    /**
     * Search result id.
     */
    public val id: String
        get() = rawSearchResult.id

    /**
     * Result MapboxID
     */
    public val mapboxId: String?
        get() = rawSearchResult.mapboxId

    /**
     * Search result name.
     */
    public val name: String
        get() = rawSearchResult.names[0]

    /**
     * Search result description.
     */
    public val descriptionText: String?
        get() = rawSearchResult.descriptionAddress

    /**
     * Search result address.
     */
    public val address: OfflineSearchAddress?
        get() = rawSearchResult.addresses?.firstOrNull()?.mapToOfflineSdkType()

    /**
     * Search result coordinate.
     */
    public val coordinate: Point
        get() = requireNotNull(rawSearchResult.center)

    /**
     * List of points near [coordinate], that represents entries to associated building.
     */
    public val routablePoints: List<RoutablePoint>?
        get() = rawSearchResult.routablePoints?.map { it.mapToPlatform() }

    /**
     * Search result type.
     */
    public val type: OfflineSearchResultType
        get() = offlineType

    /**
     * Distance in meters from search result's [coordinate] to the origin point specified in [OfflineSearchOptions.origin].
     */
    public val distanceMeters: Double?
        get() = rawSearchResult.distanceMeters

    /**
     * Metadata containing geo place's detailed information if available.
     */
    @MapboxExperimental
    public val metadata: OfflineSearchResultMetadata?
        get() = rawSearchResult.metadata?.let {
            OfflineSearchResultMetadata(it)
        }

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OfflineSearchResult

        return rawSearchResult == other.rawSearchResult
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        return rawSearchResult.hashCode()
    }

    /**
     * @suppress
     */
    @OptIn(MapboxExperimental::class)
    override fun toString(): String {
        return "OfflineSearchResult(" +
                "id='$id', " +
                "mapboxId='$mapboxId', " +
                "name='$name', " +
                "descriptionText=$descriptionText, " +
                "address=$address, " +
                "coordinate=$coordinate, " +
                "routablePoints=$routablePoints, " +
                "type=$type, " +
                "distanceMeters=$distanceMeters, " +
                "metadata=$metadata" +
                ")"
    }
}
