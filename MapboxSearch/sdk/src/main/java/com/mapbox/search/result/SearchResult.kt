package com.mapbox.search.result

import android.os.Parcelable
import com.mapbox.geojson.Point
import com.mapbox.search.RequestOptions
import com.mapbox.search.SearchResultMetadata
import com.mapbox.search.base.result.BaseIndexableRecordSearchResultImpl
import com.mapbox.search.base.result.BaseSearchResult
import com.mapbox.search.base.result.BaseServerSearchResultImpl
import com.mapbox.search.common.RoutablePoint
import com.mapbox.search.mapToBase
import com.mapbox.search.mapToPlatform
import com.mapbox.search.record.IndexableRecord
import com.mapbox.search.record.mapToBase

/**
 * Resolved search object with populated fields.
 */
public sealed interface SearchResult : Parcelable {

    /**
     * Search request options.
     */
    public val requestOptions: RequestOptions

    /**
     * Result unique identifier.
     */
    public val id: String

    /**
     * Result name.
     */
    public val name: String

    /**
     * The feature name, as matched by the search algorithm.
     */
    public val matchingName: String?

    /**
     * Additional description for the search result.
     */
    public val descriptionText: String?

    /**
     * Result address.
     */
    public val address: SearchAddress?

    /**
     * List of points near [coordinate], that represents entries to associated building.
     */
    public val routablePoints: List<RoutablePoint>?

    /**
     * Poi categories. Always empty for non-POI search results.
     * @see types
     */
    public val categories: List<String>

    /**
     * [Maki](https://github.com/mapbox/maki/) icon name for search result.
     */
    public val makiIcon: String?

    /**
     * Result coordinates.
     */
    public val coordinate: Point?

    /**
     * A point accuracy metric for the returned address.
     */
    public val accuracy: ResultAccuracy?

    /**
     * Non-empty list of resolved [SearchResult] types.
     */
    public val types: List<SearchResultType>

    /**
     * Estimated time of arrival (in minutes) based on the specified in the [com.mapbox.search.SearchOptions] origin point and navigation profile.
     * For [com.mapbox.search.result.SearchResult] this property is not null
     * only if it was present in the corresponding [com.mapbox.search.result.SearchSuggestion].
     *
     * You can always calculate ETA on your own using user's location and result's [coordinate].
     */
    public val etaMinutes: Double?

    /**
     * Search result metadata containing geo place's detailed information if available.
     */
    public val metadata: SearchResultMetadata?

    /**
     * Experimental API, can be changed or removed in the next SDK releases.
     * Map of external ids. Returned Map instance is unmodifiable.
     */
    public val externalIDs: Map<String, String>

    /**
     * Distance in meters from result to requested origin (for forward geocoding and category search) or provided point (for reverse geocoding).
     * For provided point always returns non-null distance.
     */
    public val distanceMeters: Double?

    /**
     * Index in response from server.
     */
    public val serverIndex: Int?
}

/**
 * Resolved search object with populated fields and mandatory coordinates field.
*/
public sealed interface ServerSearchResult : SearchResult {

    /**
     * Result coordinates.
     */
    override val coordinate: Point
}

/**
 * Resolved search object based on some [IndexableRecord]. As an example, search result is one of user's FavoriteRecord.
 */
public sealed interface IndexableRecordSearchResult : SearchResult {

    /**
     * [IndexableRecord] on which search result based.
     */
    public val record: IndexableRecord
}

@JvmSynthetic
internal fun BaseSearchResult.mapToPlatform(): SearchResult {
    return when (val resultType = baseType) {
        is BaseSearchResult.Type.ServerResult -> {
            ServerSearchResultImpl(
                types = types.map { it.mapToPlatform() },
                rawSearchResult = rawSearchResult,
                requestOptions = requestOptions.mapToPlatform(),
            )
        }
        is BaseSearchResult.Type.IndexableRecordSearchResult -> {
            check(resultType.record.sdkResolvedRecord is IndexableRecord)

            IndexableRecordSearchResultImpl(
                record = resultType.record.sdkResolvedRecord as IndexableRecord,
                rawSearchResult = rawSearchResult,
                requestOptions = requestOptions.mapToPlatform(),
            )
        }
    }
}

@JvmSynthetic
internal fun SearchResult.mapToBase(): BaseSearchResult {
    return when (this) {
        is ServerSearchResultImpl -> {
            BaseServerSearchResultImpl(
                types = types.map { it.mapToBase() },
                rawSearchResult = rawSearchResult,
                requestOptions = requestOptions.mapToBase()
            )
        }
        is IndexableRecordSearchResultImpl -> {
            BaseIndexableRecordSearchResultImpl(
                record = record.mapToBase(),
                rawSearchResult = rawSearchResult,
                requestOptions = requestOptions.mapToBase(),
            )
        }
        else -> error("Unknown search result type: $this")
    }
}
