package com.mapbox.search

import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.utils.concurrent.SearchSdkMainThreadWorker
import java.util.concurrent.Executor

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
     * Called when a category suggestion has been resolved.
     *
     * @param suggestion The category suggestion from which the [results] were resolved.
     * @param results Search results matched by category search.
     * @param responseInfo Search response and request information.
     */
    public fun onCategoryResult(
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

/**
 * Used for [forward geocoding](https://docs.mapbox.com/android/search/overview/#single-box-search)
 * (looking up a place by name to retrieve its geographic coordinates).
 *
 * ## Instantiating
 *
 * To obtain [SearchEngine] instance, please, use [MapboxSearchSdk.getSearchEngine].
 *
 * ## Forward geocoding algorithm
 *
 * Forward geocoding consists of 2 steps:
 * 1) Retrieve suggestions ([search]). For giving query we provide a list of [suggestions][SearchSuggestion],
 * which contain only basic information (name of a place/category/query, its description, its type and etc.).
 * Provided suggestions will be passed to [SearchSuggestionsCallback.onSuggestions].
 * Note, that such information as coordinates of place is not available at this step. To get this information,
 * proceed to next step;
 * 2) Get more details for suggestion ([select]). Depending on [suggestion type][SearchSuggestion.type],
 * different logic might be applied:
 *
 *       - For [default suggestion][SearchSuggestionType.SearchResultSuggestion] additional network
 *   request will be executed and [result][SearchResult] will be passed to [SearchSelectionCallback.onResult];
 *       - For [category suggestion][SearchSuggestionType.Category] additional network request
 *   will be executed and [list of results][SearchResult] will be passed to [SearchSelectionCallback.onCategoryResult];
 *       - For [query suggestion][SearchSuggestionType.Query] additional network request will be
 *   executed and another [list of suggestions][SearchSuggestion] will be passed to [SearchSelectionCallback.onSuggestions].
 *   To get more information for provided suggestions you should use the same step ([select]);
 *       - For [IndexableRecord suggestion][SearchSuggestionType.IndexableRecordItem] extra information
 *   will be retrieved locally and [result][SearchResult] will be passed to [SearchSelectionCallback.onResult].
 *
 *    If you want to get coordinates of search suggestion (or more details about suggestion),
 *  you should use [select] method. You will receive more [detailed information][SearchResult].
 *
 * ## Error handling
 *
 * If invalid parameters are provided, [RuntimeException] is thrown immediately.
 * Any other error will be propagated to [SearchSuggestionsCallback.onError].
 */
public interface SearchEngine {

    /**
     * Experimental API, can be changed or removed in the next SDK releases.
     * The type of the API used by the Search Engine.
     */
    public val apiType: ApiType

    /**
     * The first step of forward geocoding. Returns a list of [SearchSuggestion] without coordinates.
     *
     * @param query Search query.
     * @param options Search options.
     * @param executor Executor used for events dispatching. By default events are dispatched on the main thread.
     * @param callback The callback to retrieve list of [SearchSuggestion].
     * @return [SearchRequestTask] object which allows to cancel the request.
     */
    public fun search(
        query: String,
        options: SearchOptions,
        executor: Executor,
        callback: SearchSuggestionsCallback,
    ): SearchRequestTask

    /**
     * The first step of forward geocoding. Returns a list of [SearchSuggestion] without coordinates.
     *
     * @param query Search query.
     * @param options Search options.
     * @param callback The callback to retrieve list of [SearchSuggestion]. Events are dispatched on the main thread.
     * @return [SearchRequestTask] object which allows to cancel the request.
     */
    public fun search(
        query: String,
        options: SearchOptions,
        callback: SearchSuggestionsCallback,
    ): SearchRequestTask = search(
        query = query,
        options = options,
        executor = SearchSdkMainThreadWorker.mainExecutor,
        callback = callback,
    )

    /**
     * The second step of forward geocoding. Call this function to get a [SearchResult] (with coordinates) from a [SearchSuggestion].
     *
     * @param suggestion Search suggestion to resolve and get the final result with address and coordinates.
     * @param callback The callback to retrieve [SearchResult] with resolved coordinates. Events are dispatched on the main thread.
     * @return [SearchRequestTask] object which allows to cancel the request.
     */
    public fun select(
        suggestion: SearchSuggestion,
        callback: SearchSelectionCallback,
    ): SearchRequestTask = select(
        suggestion = suggestion,
        options = SelectOptions(),
        executor = SearchSdkMainThreadWorker.mainExecutor,
        callback = callback,
    )

    /**
     * The second step of forward geocoding. Call this function to get a [SearchResult] (with coordinates) from a [SearchSuggestion].
     *
     * @param suggestion Search suggestion to resolve and get the final result with address and coordinates.
     * @param options Options used for controlling internal "select" operation logic.
     * @param callback The callback to retrieve [SearchResult] with resolved coordinates.
     * @return [SearchRequestTask] object which allows to cancel the request.
     */
    public fun select(
        suggestion: SearchSuggestion,
        options: SelectOptions,
        callback: SearchSelectionCallback,
    ): SearchRequestTask = select(
        suggestion = suggestion,
        options = options,
        executor = SearchSdkMainThreadWorker.mainExecutor,
        callback = callback,
    )

    /**
     * The second step of forward geocoding. Call this function to get a [SearchResult] (with coordinates) from a [SearchSuggestion].
     *
     * @param suggestion Search suggestion to resolve and get the final result with address and coordinates.
     * @param options Options used for controlling internal "select" operation logic.
     * @param executor Executor used for events dispatching. By default events are dispatched on the main thread.
     * @param callback The callback to retrieve [SearchResult] with resolved coordinates.
     * @return [SearchRequestTask] object which allows to cancel the request.
     */
    public fun select(
        suggestion: SearchSuggestion,
        options: SelectOptions,
        executor: Executor,
        callback: SearchSelectionCallback,
    ): SearchRequestTask

    /**
     * Function to select multiple suggestions at once.
     * Unlike [select], resolving always ends up returning list of [SearchResult] and can't return new suggestions.
     *
     * Note that all the search suggestions must originate from the same search request and only certain suggestions
     * can be used in the batch selection, to check if a [SearchSuggestion] can be passed to this function,
     * call [SearchSuggestion.isBatchResolveSupported].
     * With the current implementation, only POI and indexable record suggestions support batch resolving.
     * All the suggestions that can't be used in batch resolving will be filtered.
     *
     * @param suggestions Search suggestions to resolve. Suggestions that don't support batch resolving will be filtered.
     * @param executor Executor used for events dispatching. By default events are dispatched on the main thread.
     * @param callback The callback to retrieve [SearchResult] with resolved coordinates.
     * @return [SearchRequestTask] object which allows to cancel the request.
     */
    public fun select(
        suggestions: List<SearchSuggestion>,
        executor: Executor,
        callback: SearchMultipleSelectionCallback,
    ): SearchRequestTask

    /**
     * Function to select multiple suggestions at once.
     * Unlike [select], resolving always ends up returning list of [SearchResult] and can't return new suggestions.
     *
     * Note that all the search suggestions must originate from the same search request and only certain suggestions
     * can be used in the batch selection, to check if a [SearchSuggestion] can be passed to this function,
     * call [SearchSuggestion.isBatchResolveSupported].
     * With the current implementation, only POI and indexable record suggestions support batch resolving.
     * All the suggestions that can't be used in batch resolving will be filtered.
     *
     * @param suggestions Search suggestions to resolve. Suggestions that don't support batch resolving will be filtered.
     * @param callback The callback to retrieve [SearchResult] with resolved coordinates. Events are dispatched on the main thread.
     * @return [SearchRequestTask] object which allows to cancel the request.
     */
    public fun select(
        suggestions: List<SearchSuggestion>,
        callback: SearchMultipleSelectionCallback,
    ): SearchRequestTask = select(
        suggestions = suggestions,
        executor = SearchSdkMainThreadWorker.mainExecutor,
        callback = callback,
    )
}
