package com.mapbox.search.result

import android.os.Parcelable
import com.mapbox.search.RequestOptions

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
     * Denotes whether this suggestion can be passed as a parameter to a batch selection method of the [com.mapbox.search.SearchEngine].
     */
    public val isBatchResolveSupported: Boolean
}
