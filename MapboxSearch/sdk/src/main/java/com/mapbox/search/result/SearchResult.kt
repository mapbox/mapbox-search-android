package com.mapbox.search.result

import android.os.Parcelable
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.search.RequestOptions
import com.mapbox.search.SearchResultMetadata
import com.mapbox.search.base.result.BaseSearchResult
import com.mapbox.search.base.utils.extension.mapToPlatform
import com.mapbox.search.base.utils.extension.safeCompareTo
import com.mapbox.search.common.RoutablePoint
import com.mapbox.search.internal.mapToNewSearchResultType
import com.mapbox.search.internal.newSearchResultTypeToOld
import com.mapbox.search.mapToPlatform
import com.mapbox.search.record.IndexableRecord
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * Resolved search object with populated fields.
 */
@Suppress("DEPRECATION")
@Parcelize
public class SearchResult internal constructor(
    internal val base: BaseSearchResult
) : Parcelable {

    /**
     * Search request options.
     */
    @IgnoredOnParcel
    public val requestOptions: RequestOptions = base.requestOptions.mapToPlatform()

    /**
     * Result unique identifier.
     */
    @IgnoredOnParcel
    public val id: String = base.id

    /**
     * Result MapboxID
     */
    public val mapboxId: String?
        get() = base.mapboxId

    /**
     * Result name.
     */
    @IgnoredOnParcel
    public val name: String = base.name

    /**
     * The feature name, as matched by the search algorithm.
     */
    @IgnoredOnParcel
    public val matchingName: String? = base.matchingName

    /**
     * Additional description for the search result.
     */
    @IgnoredOnParcel
    public val descriptionText: String? = base.descriptionText

    /**
     * Result address.
     */
    @IgnoredOnParcel
    public val address: SearchAddress? = base.address?.mapToPlatform()

    /**
     * Full formatted address.
     */
    @IgnoredOnParcel
    public val fullAddress: String? = base.fullAddress

    /**
     * List of points near [coordinate], that represents entries to associated building.
     */
    @IgnoredOnParcel
    public val routablePoints: List<RoutablePoint>? = base.routablePoints?.map { it.mapToPlatform() }

    /**
     * Optional bounding box that represents the geographical boundaries of a location,
     * e.g., a building.
     */
    @IgnoredOnParcel
    public val boundingBox: BoundingBox? = base.bbox

    /**
     * POI categories. Always empty for non-POI search results.
     * @see types
     */
    @IgnoredOnParcel
    public val categories: List<String>? = base.categories

    /**
     * Canonical POI category IDs. Always empty for non-POI suggestions.
     * @see types
     */
    @IgnoredOnParcel
    public val categoryIds: List<String>? = base.categoryIds

    /**
     * [Maki](https://github.com/mapbox/maki/) icon name for search result.
     */
    @IgnoredOnParcel
    public val makiIcon: String? = base.makiIcon

    /**
     * Result coordinates.
     */
    @IgnoredOnParcel
    public val coordinate: Point = base.coordinate

    /**
     * A point accuracy metric for the returned address.
     */
    @IgnoredOnParcel
    public val accuracy: ResultAccuracy? = base.accuracy?.mapToPlatform()

    /**
     * Non-empty list of [NewSearchResultType.Type] values.
     */
    @IgnoredOnParcel
    public val newTypes: List<String> = base.types.map { it.mapToNewSearchResultType() }

    /**
     * Non-empty list of resolved [SearchResult] types.
     *
     * List values are of type [SearchResultType], which have been replaced by [NewSearchResultType].
     * Use [newTypes] to identify the actual type of this [SearchResult].
     */
    @IgnoredOnParcel
    @Deprecated(
        message = "This property is deprecated and should be replaced by newTypes",
        replaceWith = ReplaceWith("newTypes"),
    )
    public val types: List<SearchResultType> = newTypes.map { newSearchResultTypeToOld(it) }

    /**
     * Estimated time of arrival (in minutes) based on the specified in the [com.mapbox.search.SearchOptions] origin point and navigation profile.
     * For [com.mapbox.search.result.SearchResult] this property is not null
     * only if it was present in the corresponding [com.mapbox.search.result.SearchSuggestion].
     *
     * You can always calculate ETA on your own using user's location and result's [coordinate].
     */
    @IgnoredOnParcel
    public val etaMinutes: Double? = base.etaMinutes

    /**
     * Search result metadata containing geo place's detailed information if available.
     */
    @IgnoredOnParcel
    public val metadata: SearchResultMetadata? = base.metadata?.let { SearchResultMetadata(it) }

    /**
     * Map of external ids. Returned Map instance is unmodifiable.
     */
    @IgnoredOnParcel
    public val externalIDs: Map<String, String> = base.externalIDs

    /**
     * Distance in meters from result to requested origin (for forward geocoding and category search) or provided point (for reverse geocoding).
     * For provided point always returns non-null distance.
     */
    @IgnoredOnParcel
    public val distanceMeters: Double? = base.distanceMeters

    /**
     * Index in response from server.
     */
    @IgnoredOnParcel
    public val serverIndex: Int? = base.serverIndex

    /**
     * Returns [IndexableRecord] if this [SearchResult] is based on
     * item from [com.mapbox.search.record.IndexableDataProvider], null otherwise.
     */
    @IgnoredOnParcel
    public val indexableRecord: IndexableRecord? =
        base.indexableRecord?.sdkResolvedRecord as? IndexableRecord

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SearchResult

        if (requestOptions != other.requestOptions) return false
        if (id != other.id) return false
        if (mapboxId != other.mapboxId) return false
        if (name != other.name) return false
        if (matchingName != other.matchingName) return false
        if (descriptionText != other.descriptionText) return false
        if (address != other.address) return false
        if (fullAddress != other.fullAddress) return false
        if (routablePoints != other.routablePoints) return false
        if (boundingBox != other.boundingBox) return false
        if (categories != other.categories) return false
        if (categoryIds != other.categoryIds) return false
        if (makiIcon != other.makiIcon) return false
        if (coordinate != other.coordinate) return false
        if (accuracy != other.accuracy) return false
        if (newTypes != other.newTypes) return false
        if (types != other.types) return false
        if (!etaMinutes.safeCompareTo(other.etaMinutes)) return false
        if (metadata != other.metadata) return false
        if (externalIDs != other.externalIDs) return false
        if (!distanceMeters.safeCompareTo(other.distanceMeters)) return false
        if (serverIndex != other.serverIndex) return false
        if (indexableRecord != other.indexableRecord) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = requestOptions.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + mapboxId.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + matchingName.hashCode()
        result = 31 * result + descriptionText.hashCode()
        result = 31 * result + address.hashCode()
        result = 31 * result + fullAddress.hashCode()
        result = 31 * result + routablePoints.hashCode()
        result = 31 * result + boundingBox.hashCode()
        result = 31 * result + categories.hashCode()
        result = 31 * result + categoryIds.hashCode()
        result = 31 * result + makiIcon.hashCode()
        result = 31 * result + coordinate.hashCode()
        result = 31 * result + accuracy.hashCode()
        result = 31 * result + newTypes.hashCode()
        result = 31 * result + types.hashCode()
        result = 31 * result + etaMinutes.hashCode()
        result = 31 * result + metadata.hashCode()
        result = 31 * result + externalIDs.hashCode()
        result = 31 * result + distanceMeters.hashCode()
        result = 31 * result + (serverIndex ?: 0)
        result = 31 * result + indexableRecord.hashCode()
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "SearchResult(" +
                "requestOptions=$requestOptions, " +
                "id='$id', " +
                "mapboxId='$mapboxId', " +
                "name='$name', " +
                "matchingName=$matchingName, " +
                "descriptionText=$descriptionText, " +
                "address=$address, " +
                "fullAddress=$fullAddress, " +
                "routablePoints=$routablePoints, " +
                "boundingBox=$boundingBox, " +
                "categories=$categories, " +
                "categoryIds=$categoryIds, " +
                "makiIcon=$makiIcon, " +
                "coordinate=$coordinate, " +
                "accuracy=$accuracy, " +
                "newTypes=$newTypes, " +
                "types=$types, " +
                "etaMinutes=$etaMinutes, " +
                "metadata=$metadata, " +
                "externalIDs=$externalIDs, " +
                "distanceMeters=$distanceMeters, " +
                "serverIndex=$serverIndex, " +
                "indexableRecord=$indexableRecord" +
                ")"
    }
}
