package com.mapbox.search.offline

import android.os.Parcelable
import com.mapbox.annotation.MapboxExperimental
import com.mapbox.geojson.Point
import com.mapbox.search.base.failDebug
import com.mapbox.search.base.result.BaseRawSearchResult
import com.mapbox.search.base.utils.extension.mapToPlatform
import com.mapbox.search.common.RestrictedMapboxSearchAPI
import com.mapbox.search.common.RoutablePoint
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * Result returned by the offline search engine.
 */
@Suppress("DEPRECATION")
@Parcelize
public class OfflineSearchResult internal constructor(
    internal val rawSearchResult: BaseRawSearchResult
) : Parcelable {

    @NewOfflineSearchResultType.Type
    @IgnoredOnParcel
    private val offlineType: String

    init {
        check(rawSearchResult.center != null) {
            "Offline search result must have a coordinate"
        }

        val type = rawSearchResult.types.firstNotNullOfOrNull {
            NewOfflineSearchResultType.createFromRawResultType(it)
        }
        offlineType = if (type == null) {
            val fallbackType = NewOfflineSearchResultType.FALLBACK_TYPE
            failDebug {
                "Unsupported in offline SDK result types: ${rawSearchResult.types}. " +
                        "Fallback to $fallbackType"
            }
            fallbackType
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
     *
     * This property is of type [OfflineSearchResultType], which has been replaced by [NewOfflineSearchResultType].
     * Use [newType] to identify the actual type of this [OfflineSearchResult].
     */
    @Deprecated(
        message = "This property is of type OfflineSearchResultType, which has been replaced by NewOfflineSearchResultType",
        replaceWith = ReplaceWith("newType"),
    )
    public val type: OfflineSearchResultType
        get() = NewOfflineSearchResultType.toOldResultType(newType)

    /**
     * The type of the search result.
     */
    @get:NewOfflineSearchResultType.Type
    public val newType: String
        get() = offlineType

    /**
     * Distance in meters from search result's [coordinate] to the origin point specified in [OfflineSearchOptions.origin].
     */
    public val distanceMeters: Double?
        get() = rawSearchResult.distanceMeters

    /**
     * Metadata containing geo place's detailed information if available.
     *
     * Only specific datasets support metadata. Contact our [sales](https://www.mapbox.com/contact/sales)
     * team to access datasets that include EV charging station data.
     */
    @MapboxExperimental
    @RestrictedMapboxSearchAPI
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
                "newType=$newType, " +
                "distanceMeters=$distanceMeters, " +
                "metadata=$metadata" +
                ")"
    }
}
