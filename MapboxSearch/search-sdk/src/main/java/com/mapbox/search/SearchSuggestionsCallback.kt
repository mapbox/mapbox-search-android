package com.mapbox.search

import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.result.SearchSuggestionType

/**
 * Used in the first step of forward geocoding to get a list of [SearchSuggestion].
 */
public interface SearchSuggestionsCallback {

    /**
     * Called when the suggestions list is returned.
     * @param suggestions List of [SearchSuggestion] as result of the first step of forward geocoding.
     * @param responseInfo Search response and request information.
     */
    public fun onSuggestions(suggestions: List<SearchSuggestion>, responseInfo: ResponseInfo)

    /**
     * Called if an error occurred during the request.
     * @param e Exception, occurred during the request.
     */
    public fun onError(e: Exception)
}

/**
 * Callback for [SearchResult], resolved from [SearchSuggestion].
 */
public interface SearchSelectionCallback : SearchSuggestionsCallback {

    /**
     * Called when final [SearchResult] resolved.
     *
     * @param suggestion The suggestion from which the [result] was resolved.
     * @param result Resolved search result.
     * @param responseInfo Search response and request information.
     */
    public fun onResult(suggestion: SearchSuggestion, result: SearchResult, responseInfo: ResponseInfo)

    /**
     * Called when suggestion selection returned more than one result.
     * This may happen when selected suggestion of type [SearchSuggestionType.Category] or
     * [SearchSuggestionType.Brand].
     *
     * @param suggestion The suggestion from which the [results] were resolved.
     * @param results Search results matched by category search.
     * @param responseInfo Search response and request information.
     */
    public fun onResults(
        suggestion: SearchSuggestion,
        results: List<SearchResult>,
        responseInfo: ResponseInfo
    )
}

/**
 * Callback called when multiple selection request completes.
 */
public interface SearchMultipleSelectionCallback {

    /**
     * Called when suggestions have been resolved.
     *
     * @param suggestions The suggestions from which the [results] were resolved.
     * Note that these suggestions is not necessary the same as ones passed to [SearchEngine.select]
     * because the function filters suggestions that don't support batch resolving.
     * @param results Resolved search results for search suggestions.
     * @param responseInfo Search response and request information.
     */
    public fun onResult(
        suggestions: List<SearchSuggestion>,
        results: List<SearchResult>,
        responseInfo: ResponseInfo
    )

    /**
     * Called if an error occurred during the request.
     * @param e exception, occurred during the request.
     */
    public fun onError(e: Exception)
}
