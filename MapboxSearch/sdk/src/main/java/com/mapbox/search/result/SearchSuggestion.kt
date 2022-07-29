package com.mapbox.search.result

import android.os.Parcelable
import com.mapbox.search.RequestOptions
import com.mapbox.search.SearchResultMetadata
import com.mapbox.search.base.result.BaseGeocodingCompatSearchSuggestion
import com.mapbox.search.base.result.BaseIndexableRecordSearchSuggestion
import com.mapbox.search.base.result.BaseSearchSuggestion
import com.mapbox.search.base.result.BaseServerSearchSuggestion
import com.mapbox.search.mapToBase
import com.mapbox.search.mapToPlatform
import com.mapbox.search.record.IndexableRecord
import com.mapbox.search.record.mapToBase

/**
 * Autocomplete common suggestion type.
 */
public sealed interface SearchSuggestion : Parcelable {

    /**
     * Unique identifier for suggestion result.
     * Attention: Mapbox backend may change the identifier of the object in the future.
     */
    public val id: String

    /**
     * Suggestion name.
     */
    public val name: String

    /**
     * The feature name, as matched by the search algorithm.
     */
    public val matchingName: String?

    /**
     * Suggestion description.
     */
    public val descriptionText: String?

    /**
     * Address, might be null or incomplete.
     */
    public val address: SearchAddress?

    /**
     * Request options, that produced this suggestion.
     */
    public val requestOptions: RequestOptions

    /**
     * Distance in meters from result to requested origin.
     */
    public val distanceMeters: Double?

    /**
     * Poi categories. Always empty for non-POI suggestions.
     * @see type
     */
    public val categories: List<String>

    /**
     * Experimental field, can be changed or removed in the next SDK releases.
     * [Maki](https://github.com/mapbox/maki/) icon name for search suggestion.
     */
    public val makiIcon: String?

    /**
     * Type of the suggestion.
     */
    public val type: SearchSuggestionType

    /**
     * Estimated time of arrival (in minutes) based on the specified in the [com.mapbox.search.SearchOptions] origin point and navigation profile.
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
     * Denotes whether this suggestion can be passed as a parameter to a batch selection method of the [com.mapbox.search.SearchEngine].
     */
    public val isBatchResolveSupported: Boolean

    /**
     * Index in response from server.
     */
    public val serverIndex: Int?
}

@JvmSynthetic
internal fun BaseSearchSuggestion.mapToPlatform(): SearchSuggestion {
    return when (this) {
        is BaseServerSearchSuggestion -> {
            ServerSearchSuggestion(
                rawSearchResult = rawSearchResult,
                requestOptions = requestOptions.mapToPlatform(),
            )
        }
        is BaseIndexableRecordSearchSuggestion -> {
            check(record.sdkResolvedRecord is IndexableRecord)

            IndexableRecordSearchSuggestion(
                record = record.sdkResolvedRecord as IndexableRecord,
                rawSearchResult = rawSearchResult,
                requestOptions = requestOptions.mapToPlatform(),
            )
        }
        is BaseGeocodingCompatSearchSuggestion -> {
            GeocodingCompatSearchSuggestion(
                rawSearchResult = rawSearchResult,
                requestOptions = requestOptions.mapToPlatform(),
            )
        }
    }
}

@JvmSynthetic
internal fun SearchSuggestion.mapToBase(): BaseSearchSuggestion {
    return when (this) {
        is ServerSearchSuggestion -> {
            BaseServerSearchSuggestion(
                rawSearchResult = rawSearchResult,
                requestOptions = requestOptions.mapToBase(),
            )
        }
        is IndexableRecordSearchSuggestion -> {
            BaseIndexableRecordSearchSuggestion(
                record = record.mapToBase(),
                rawSearchResult = rawSearchResult,
                requestOptions = requestOptions.mapToBase(),
            )
        }
        is GeocodingCompatSearchSuggestion -> {
            BaseGeocodingCompatSearchSuggestion(
                rawSearchResult = rawSearchResult,
                requestOptions = requestOptions.mapToBase(),
            )
        }
    }
}
