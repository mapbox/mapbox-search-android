package com.mapbox.search.offline

import android.os.Parcelable
import com.mapbox.geojson.Point
import com.mapbox.search.base.assertDebug
import com.mapbox.search.base.result.BaseRawSearchResult
import kotlinx.parcelize.Parcelize

/**
 * Result returned by the offline search engine.
 */
@Parcelize
public class OfflineSearchResult internal constructor(
    @JvmSynthetic internal val rawSearchResult: BaseRawSearchResult
) : Parcelable {

    init {
        assertDebug(rawSearchResult.center != null) {
            "Server search result must have a coordinate"
        }
        assertDebug(rawSearchResult.types.isNotEmpty()) { "Provided types should not be empty!" }
    }

    /**
     * Search result id.
     */
    public val id: String
        get() = rawSearchResult.id

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
     * Search result type.
     */
    public val type: OfflineSearchResultType
        get() = requireNotNull(rawSearchResult.types.first().tryMapToSearchResultType()?.mapToOfflineSdkType())

    /**
     * Distance in meters from search result's [coordinate] to the origin point specified in [OfflineSearchOptions.origin].
     */
    public val distanceMeters: Double?
        get() = rawSearchResult.distanceMeters

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OfflineSearchResult

        if (rawSearchResult != other.rawSearchResult) return false

        return true
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
    override fun toString(): String {
        return "OfflineSearchResult(" +
                "id='$id', " +
                "name='$name', " +
                "descriptionText=$descriptionText, " +
                "address=$address, " +
                "coordinate=$coordinate, " +
                "type=$type, " +
                "distanceMeters=$distanceMeters" +
                ")"
    }
}
