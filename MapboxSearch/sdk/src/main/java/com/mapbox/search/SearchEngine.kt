package com.mapbox.search

import com.mapbox.search.analytics.AnalyticsService
import com.mapbox.search.base.StubCompletionCallback
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.common.CompletionCallback
import com.mapbox.search.common.concurrent.SearchSdkMainThreadWorker
import com.mapbox.search.record.IndexableDataProvider
import com.mapbox.search.record.IndexableRecord
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import java.util.concurrent.Executor

/**
 * Performs
 * - [forward geocoding](https://docs.mapbox.com/android/search/overview/#single-box-search)
 * (looking up a place by name to retrieve its geographic coordinates)
 * - [category search](https://docs.mapbox.com/android/search/overview/#single-box-search)(search for places by category name)
 * - [reverse geocoding](https://docs.mapbox.com/android/search/guides/#reverse-geocoding)(search for places by geographic coordinate)
 *
 * [SearchEngine] API requires an online connection to execute the requests.
 *
 * ## Instantiating
 *
 * Call [SearchEngine.createSearchEngine] to get instance of a [SearchEngine].
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
 *   will be executed and [list of results][SearchResult] will be passed to [SearchSelectionCallback.onResults];
 *       - For [brand suggestion][SearchSuggestionType.Brand] additional network request
 *   will be executed and [list of results][SearchResult] will be passed to [SearchSelectionCallback.onResults];
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
 *
 * @see SearchEngine.createSearchEngine
 */
public interface SearchEngine {

    /**
     * The type of the API used by the Search Engine.
     */
    public val apiType: ApiType

    /**
     * Settings used for [SearchEngine] initialization.
     */
    public val settings: SearchEngineSettings

    /**
     * [AnalyticsService] instance associated with this [SearchEngine].
     */
    public val analyticsService: AnalyticsService

    /**
     * The first step of forward geocoding. Returns a list of [SearchSuggestion] without coordinates.
     *
     * @param query Search query.
     * @param options Search options.
     * @param executor Executor used for events dispatching. By default events are dispatched on the main thread.
     * @param callback The callback to retrieve list of [SearchSuggestion].
     * @return [AsyncOperationTask] object representing pending completion of the request.
     */
    public fun search(
        query: String,
        options: SearchOptions,
        executor: Executor,
        callback: SearchSuggestionsCallback,
    ): AsyncOperationTask

    /**
     * The first step of forward geocoding. Returns a list of [SearchSuggestion] without coordinates.
     *
     * @param query Search query.
     * @param options Search options.
     * @param callback The callback to retrieve list of [SearchSuggestion]. Events are dispatched on the main thread.
     * @return [AsyncOperationTask] object representing pending completion of the request.
     */
    public fun search(
        query: String,
        options: SearchOptions,
        callback: SearchSuggestionsCallback,
    ): AsyncOperationTask = search(
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
     * @return [AsyncOperationTask] object representing pending completion of the request.
     */
    public fun select(
        suggestion: SearchSuggestion,
        callback: SearchSelectionCallback,
    ): AsyncOperationTask = select(
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
     * @return [AsyncOperationTask] object representing pending completion of the request.
     */
    public fun select(
        suggestion: SearchSuggestion,
        options: SelectOptions,
        callback: SearchSelectionCallback,
    ): AsyncOperationTask = select(
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
     * @return [AsyncOperationTask] object representing pending completion of the request.
     */
    public fun select(
        suggestion: SearchSuggestion,
        options: SelectOptions,
        executor: Executor,
        callback: SearchSelectionCallback,
    ): AsyncOperationTask

    /**
     * Deprecated, consider making a selection of only one Search Suggestion.
     *
     * Function to select multiple suggestions at once.
     * Unlike [select], resolving always ends up returning list of [SearchResult] and can't return new suggestions.
     *
     * Note that all the search suggestions must originate from the same search request and only certain suggestions
     * can be used in the batch selection, to check if a [SearchSuggestion] can be passed to this function,
     * call [SearchSuggestion.isBatchResolveSupported].
     * With the current implementation, only POI and indexable record suggestions support batch resolving.
     * All the suggestions that can't be used in batch resolving will be filtered.
     *
     * Please note that this function is not supported for [ApiType.SEARCH_BOX].
     * If this [SearchEngine] was created with [ApiType.SEARCH_BOX],
     * the [SearchMultipleSelectionCallback.onError] will be triggered immediately.
     *
     * @param suggestions Search suggestions to resolve. Suggestions that don't support batch resolving will be filtered.
     * @param executor Executor used for events dispatching. By default events are dispatched on the main thread.
     * @param callback The callback to retrieve [SearchResult] with resolved coordinates.
     * @return [AsyncOperationTask] object representing pending completion of the request.
     */
    @Deprecated("Consider making a selection of only one Search Suggestion")
    public fun select(
        suggestions: List<SearchSuggestion>,
        executor: Executor,
        callback: SearchMultipleSelectionCallback,
    ): AsyncOperationTask

    /**
     * Deprecated, consider making a selection of only one Search Suggestion.
     *
     * Function to select multiple suggestions at once.
     * Unlike [select], resolving always ends up returning list of [SearchResult] and can't return new suggestions.
     *
     * Note that all the search suggestions must originate from the same search request and only certain suggestions
     * can be used in the batch selection, to check if a [SearchSuggestion] can be passed to this function,
     * call [SearchSuggestion.isBatchResolveSupported].
     * With the current implementation, only POI and indexable record suggestions support batch resolving.
     * All the suggestions that can't be used in batch resolving will be filtered.
     *
     * Please note that this function is not supported for [ApiType.SEARCH_BOX].
     * If this [SearchEngine] was created with [ApiType.SEARCH_BOX],
     * the [SearchMultipleSelectionCallback.onError] will be triggered immediately.
     *
     * @param suggestions Search suggestions to resolve. Suggestions that don't support batch resolving will be filtered.
     * @param callback The callback to retrieve [SearchResult] with resolved coordinates. Events are dispatched on the main thread.
     * @return [AsyncOperationTask] object representing pending completion of the request.
     */
    @Deprecated("Consider making a selection of only one Search Suggestion")
    @Suppress("DEPRECATION")
    public fun select(
        suggestions: List<SearchSuggestion>,
        callback: SearchMultipleSelectionCallback,
    ): AsyncOperationTask = select(
        suggestions = suggestions,
        executor = SearchSdkMainThreadWorker.mainExecutor,
        callback = callback,
    )

    /**
     * Function to retrieve the details for a given mapboxId. The callback will be invoked with
     * a [SearchResult] on successful execution. This method is only supported for a SearchEngine
     * with [ApiType.SEARCH_BOX] or [ApiType.SBS].
     *
     * @param mapboxId for the item to retrieve details for
     * @param executor [Executor] used for events dispatching, default is the main thread
     * @param callback used to receive the [SearchResult] on successful execution
     * @return [AsyncOperationTask] object representing pending completion of the request
     * @throws [UnsupportedOperationException] when invoked for any [ApiType] _except_ [ApiType.SEARCH_BOX] or [ApiType.SBS]
     */
    public fun retrieve(
        mapboxId: String,
        executor: Executor,
        callback: SearchResultCallback,
    ): AsyncOperationTask

    /**
     * Function to retrieve the details for a given mapboxId that dispatches events using the
     * main executor. The callback will be invoked with a [SearchResult] on successful execution.
     *
     * Note that this method is only supported for a SearchEngine with [ApiType.SEARCH_BOX] or [ApiType.SBS].
     *
     * @param mapboxId for the item to retrieve details for
     * @param callback used to receive the [SearchResult] on successful execution
     * @return [AsyncOperationTask] object representing pending completion of the request
     */
    public fun retrieve(
        mapboxId: String,
        callback: SearchResultCallback
    ): AsyncOperationTask = retrieve(
        mapboxId = mapboxId,
        executor = SearchSdkMainThreadWorker.mainExecutor,
        callback = callback
    )

    /**
     * Performs a search request for places based on a category.
     *
     * @param categoryName Name of category to search.
     * @param options Category search options.
     * @param executor Executor used for events dispatching. By default events are dispatched on the main thread.
     * @param callback Search callback to retrieve results.
     * @return [AsyncOperationTask] object representing pending completion of the request.
     */
    public fun search(
        categoryName: String,
        options: CategorySearchOptions,
        executor: Executor,
        callback: SearchCallback,
    ): AsyncOperationTask

    /**
     * Performs a search request for places based on a category.
     *
     * @param categoryName Name of category to search.
     * @param options Category search options.
     * @param callback Search callback to retrieve results. Events are dispatched on the main thread.
     * @return [AsyncOperationTask] object representing pending completion of the request.
     */
    public fun search(
        categoryName: String,
        options: CategorySearchOptions,
        callback: SearchCallback,
    ): AsyncOperationTask = search(
        categoryName = categoryName,
        options = options,
        executor = SearchSdkMainThreadWorker.mainExecutor,
        callback = callback,
    )

    /**
     * Performs reverse geocoding.
     *
     * @param options Reverse geocoding options.
     * @param executor Executor used for events dispatching. By default events are dispatched on the main thread.
     * @param callback Search result callback.
     * @return [AsyncOperationTask] object representing pending completion of the request.
     */
    public fun search(
        options: ReverseGeoOptions,
        executor: Executor,
        callback: SearchCallback,
    ): AsyncOperationTask

    /**
     * Performs reverse geocoding.
     *
     * @param options Reverse geocoding options.
     * @param callback Search result callback, delivers results on the main thread.
     * @return [AsyncOperationTask] object representing pending completion of the request.
     */
    public fun search(
        options: ReverseGeoOptions,
        callback: SearchCallback,
    ): AsyncOperationTask = search(
        options = options,
        executor = SearchSdkMainThreadWorker.mainExecutor,
        callback = callback,
    )

    /**
     * One-off search request to get a list of search results with coordinates and metadata.
     * In contrast to Interactive Search ([search] and [select] functions),
     * the [forward] request will not provide type-ahead suggestions,
     * e.g., brand and category suggestions, but will only provide relevant search results.
     * See the
     * [Text Search documentation page](https://docs.mapbox.com/api/search/search-box/#search-request)
     * for more details.
     *
     * This function makes a request to [Search Box](https://docs.mapbox.com/api/search/search-box)
     * '/forward' endpoint.
     *
     * Please note that this function is supported only for [ApiType.SEARCH_BOX].
     * If this [SearchEngine] was created with another [ApiType],
     * the [SearchCallback.onError] will be triggered immediately.
     *
     * @param query Search query.
     * @param options Search options.
     * @param executor Executor used for events dispatching. By default events are dispatched on the main thread.
     * @param callback Search result callback, delivers results using the provided [executor].
     * @return [AsyncOperationTask] object representing pending completion of the request.
     */
    @Reserved(Reserved.Flags.SEARCH_BOX)
    public fun forward(
        query: String,
        options: ForwardSearchOptions,
        executor: Executor,
        callback: SearchCallback,
    ): AsyncOperationTask

    /**
     * One-off search request to get a list of search results with coordinates and metadata.
     * In contrast to Interactive Search ([search] and [select] functions),
     * the [forward] request will not provide type-ahead suggestions,
     * e.g., brand and category suggestions, but will only provide relevant search results.
     * See the
     * [Text Search documentation page](https://docs.mapbox.com/api/search/search-box/#search-request)
     * for more details.
     *
     * This function makes a request to [Search Box](https://docs.mapbox.com/api/search/search-box)
     * '/forward' endpoint.
     *
     * Please note that this function is supported only for [ApiType.SEARCH_BOX].
     * If this [SearchEngine] was created with another [ApiType],
     * the [SearchCallback.onError] will be triggered immediately.
     *
     * @param query Search query.
     * @param options Search options.
     * @param callback Search result callback, delivers results on the main thread.
     * @return [AsyncOperationTask] object representing pending completion of the request.
     */
    @Reserved(Reserved.Flags.SEARCH_BOX)
    public fun forward(
        query: String,
        options: ForwardSearchOptions,
        callback: SearchCallback,
    ): AsyncOperationTask = forward(
        query = query,
        options = options,
        executor = SearchSdkMainThreadWorker.mainExecutor,
        callback = callback,
    )

    /**
     * Registers [dataProvider] in this [SearchEngine].
     *
     * @param dataProvider [IndexableDataProvider] to register.
     * @param executor Executor used for events dispatching. By default events are dispatched on the main thread.
     * @param callback Callback to handle result.
     * @return an object representing pending completion of the task.
     */
    public fun <R : IndexableRecord> registerDataProvider(
        dataProvider: IndexableDataProvider<R>,
        executor: Executor,
        callback: CompletionCallback<Unit>
    ): AsyncOperationTask

    /**
     * Registers [dataProvider] in this [SearchEngine].
     *
     * @param dataProvider [IndexableDataProvider] to register.
     * @param callback Callback to handle result. Events are dispatched on the main thread.
     * @return an object representing pending completion of the task.
     */
    public fun <R : IndexableRecord> registerDataProvider(
        dataProvider: IndexableDataProvider<R>,
        callback: CompletionCallback<Unit>
    ): AsyncOperationTask = registerDataProvider(
        dataProvider = dataProvider,
        executor = SearchSdkMainThreadWorker.mainExecutor,
        callback = callback,
    )

    /**
     * Unregisters previously registered [IndexableDataProvider].
     *
     * @param dataProvider [IndexableDataProvider] to unregister.
     * @param executor Executor used for events dispatching. By default events are dispatched on the main thread.
     * @param callback Callback to handle result.
     * @return an object representing pending completion of the task.
     */
    public fun <R : IndexableRecord> unregisterDataProvider(
        dataProvider: IndexableDataProvider<R>,
        executor: Executor,
        callback: CompletionCallback<Unit>,
    ): AsyncOperationTask

    /**
     * Unregisters previously registered [IndexableDataProvider].
     *
     * @param dataProvider [IndexableDataProvider] to unregister.
     * @param callback Callback to handle result. Events are dispatched on the main thread.
     * @return an object representing pending completion of the task.
     */
    public fun <R : IndexableRecord> unregisterDataProvider(
        dataProvider: IndexableDataProvider<R>,
        callback: CompletionCallback<Unit>,
    ): AsyncOperationTask = unregisterDataProvider(
        dataProvider = dataProvider,
        executor = SearchSdkMainThreadWorker.mainExecutor,
        callback = callback,
    )

    /**
     * Companion object.
     */
    public companion object {

        /**
         * Deprecated, use a function that takes [ApiType] as a parameter.
         *
         * Creates a new instance of the [SearchEngine] with [ApiType.GEOCODING] api type.
         * A new instance doesn't have any [IndexableDataProvider] registered by default.
         *
         * Note: The [ApiType.GEOCODING] API type is supported in compatibility mode. For example,
         * the Geocoding v5 API will align with Search Box response types.
         *
         * Additionally, please note that Points of Interest (POI) data will be removed
         * from the Geocoding v5 API on December 20, 2024.
         *
         * For more information, visit [Geocoding v5 API page](https://docs.mapbox.com/api/search/geocoding-v5/).
         *
         * @param settings [SearchEngine] settings.
         *
         * @return a new instance instance of [SearchEngine].
         * @see createSearchEngineWithBuiltInDataProviders
         * @see <a href="https://docs.mapbox.com/api/search/geocoding-v5/">Geocoding v5 API</a>
         */
        @Deprecated("Specify ApiType explicitly", ReplaceWith("createSearchEngine(apiType = , settings = )"))
        @JvmStatic
        public fun createSearchEngine(settings: SearchEngineSettings): SearchEngine {
            return createSearchEngine(ApiType.GEOCODING, settings)
        }

        /**
         * Creates a new instance of the [SearchEngine].
         * A new instance doesn't have any [IndexableDataProvider] registered by default.
         *
         * @param settings [SearchEngine] settings.
         * @param apiType The type of the API used by the Search Engines.
         * Note that [ApiType.GEOCODING] is the only available publicly.
         * You might need to [contact sales](https://www.mapbox.com/contact/sales/) to enable access for other API types.
         *
         * @return a new instance instance of [SearchEngine].
         * @see createSearchEngineWithBuiltInDataProviders
         */
        @JvmStatic
        public fun createSearchEngine(apiType: ApiType, settings: SearchEngineSettings): SearchEngine {
            return SearchEngineFactory().createSearchEngine(apiType, settings)
        }

        /**
         * Deprecated, use a function that takes [ApiType] as a parameter.
         *
         * Creates a new instance of the [SearchEngine] with [ApiType.GEOCODING] api type and default data providers (
         * [com.mapbox.search.record.HistoryDataProvider] and [com.mapbox.search.record.FavoritesDataProvider])
         * registered by default.
         *
         * Note: The [ApiType.GEOCODING] API type is supported in compatibility mode. For example,
         * the Geocoding v5 API will align with Search Box response types.
         *
         * Additionally, please note that Points of Interest (POI) data will be removed
         * from the Geocoding v5 API on December 20, 2024.
         *
         * For more information, visit [Geocoding v5 API page](https://docs.mapbox.com/api/search/geocoding-v5/).
         *
         * @param settings [SearchEngine] settings.
         * @param executor Executor used for events dispatching. By default events are dispatched on the main thread.
         * @param callback Callback to handle result.
         *
         * @return a new instance of [SearchEngine].
         * @see createSearchEngine
         * @see <a href="https://docs.mapbox.com/api/search/geocoding-v5/">Geocoding v5 API</a>
         */
        @Deprecated("Specify ApiType explicitly", ReplaceWith("createSearchEngineWithBuiltInDataProviders(apiType = , settings = )"))
        @JvmOverloads
        @JvmStatic
        public fun createSearchEngineWithBuiltInDataProviders(
            settings: SearchEngineSettings,
            executor: Executor = SearchSdkMainThreadWorker.mainExecutor,
            callback: CompletionCallback<Unit> = StubCompletionCallback()
        ): SearchEngine {
            return createSearchEngineWithBuiltInDataProviders(ApiType.GEOCODING, settings, executor, callback)
        }

        /**
         * Creates a new instance of the [SearchEngine] with default data providers (
         * [com.mapbox.search.record.HistoryDataProvider] and [com.mapbox.search.record.FavoritesDataProvider])
         * registered by default.
         *
         * @param settings [SearchEngine] settings.
         * @param apiType The type of the API used by the Search Engines. By default [ApiType.GEOCODING] will be used.
         * Note that [ApiType.GEOCODING] is the only available publicly.
         * You might need to [contact sales](https://www.mapbox.com/contact/sales/) to enable access for other API types.
         * @param executor Executor used for events dispatching. By default events are dispatched on the main thread.
         * @param callback Callback to handle result.
         *
         * @return a new instance of [SearchEngine].
         * @see createSearchEngine
         */
        @JvmOverloads
        @JvmStatic
        public fun createSearchEngineWithBuiltInDataProviders(
            apiType: ApiType,
            settings: SearchEngineSettings,
            executor: Executor = SearchSdkMainThreadWorker.mainExecutor,
            callback: CompletionCallback<Unit> = StubCompletionCallback()
        ): SearchEngine {
            return SearchEngineFactory().createSearchEngineWithBuiltInDataProviders(apiType, settings, executor, callback)
        }
    }
}
