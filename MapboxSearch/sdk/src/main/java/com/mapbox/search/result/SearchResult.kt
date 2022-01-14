package com.mapbox.search.result

import android.os.Parcelable
import com.mapbox.geojson.Point
import com.mapbox.search.RequestOptions
import com.mapbox.search.SearchResultMetadata
import com.mapbox.search.record.IndexableRecord

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
     * Additional description for the search result.
     */
    public val descriptionText: String?

    /**
     * Result address.
     */
    public val address: SearchAddress?

    /**
     * List of points near [coordinate], that represents entries to associated building.
     * @see [RoutablePoint]
     */
    public val routablePoints: List<RoutablePoint>?

    /**
     * Result categories.
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
     * Distance in meters from result to requested origin (for forward geocoding and category search) or provided point (for reverse geocoding).
     * For provided point always returns non-null distance.
     */
    public val distanceMeters: Double?
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
