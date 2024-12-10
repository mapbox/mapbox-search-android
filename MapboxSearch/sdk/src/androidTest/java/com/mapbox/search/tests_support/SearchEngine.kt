package com.mapbox.search.tests_support

import com.mapbox.geojson.Point
import com.mapbox.search.ApiType
import com.mapbox.search.CategorySearchOptions
import com.mapbox.search.ReverseGeoOptions
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchEngine.Companion.createSearchEngineWithBuiltInDataProviders
import com.mapbox.search.SearchEngineSettings
import com.mapbox.search.SearchOptions
import com.mapbox.search.SelectOptions
import com.mapbox.search.common.concurrent.SearchSdkMainThreadWorker
import com.mapbox.search.record.IndexableDataProvider
import com.mapbox.search.record.IndexableRecord
import com.mapbox.search.result.SearchSuggestion
import java.util.concurrent.Executor

internal fun SearchEngine.searchBlocking(
    query: String,
    options: SearchOptions = SearchOptions(),
    executor: Executor = SearchSdkMainThreadWorker.mainExecutor,
): BlockingSearchSelectionCallback.SearchEngineResult {
    val callback = BlockingSearchSelectionCallback()
    search(query, options, executor, callback)
    return callback.getResultBlocking()
}

internal fun SearchEngine.selectBlocking(
    suggestion: SearchSuggestion,
    options: SelectOptions = SelectOptions(),
    executor: Executor = SearchSdkMainThreadWorker.mainExecutor,
): BlockingSearchSelectionCallback.SearchEngineResult {
    val callback = BlockingSearchSelectionCallback()
    select(suggestion, options, executor, callback)
    return callback.getResultBlocking()
}

internal fun SearchEngine.selectBlocking(
    suggestions: List<SearchSuggestion>,
    executor: Executor = SearchSdkMainThreadWorker.mainExecutor,
): BlockingSearchSelectionCallback.SearchEngineResult {
    val callback = BlockingSearchSelectionCallback()
    @Suppress("DEPRECATION")
    select(suggestions, executor, callback)
    return callback.getResultBlocking()
}

internal fun SearchEngine.categorySearchBlocking(
    categoryName: String,
    options: CategorySearchOptions = CategorySearchOptions(),
    executor: Executor = SearchSdkMainThreadWorker.mainExecutor,
): BlockingSearchCallback.SearchEngineResult {
    return categorySearchBlocking(listOf(categoryName), options, executor)
}

internal fun SearchEngine.categorySearchBlocking(
    categories: List<String>,
    options: CategorySearchOptions = CategorySearchOptions(),
    executor: Executor = SearchSdkMainThreadWorker.mainExecutor,
): BlockingSearchCallback.SearchEngineResult {
    val callback = BlockingSearchCallback()
    search(categories, options, executor, callback)
    return callback.getResultBlocking()
}

internal fun SearchEngine.reverseBlocking(
    point: Point,
    executor: Executor = SearchSdkMainThreadWorker.mainExecutor,
) = reverseBlocking(ReverseGeoOptions(point), executor)

internal fun SearchEngine.reverseBlocking(
    options: ReverseGeoOptions,
    executor: Executor = SearchSdkMainThreadWorker.mainExecutor,
): BlockingSearchCallback.SearchEngineResult {
    val callback = BlockingSearchCallback()
    search(options, executor, callback)
    return callback.getResultBlocking()
}

internal fun <R : IndexableRecord> SearchEngine.registerDataProviderBlocking(
    dataProvider: IndexableDataProvider<R>,
    executor: Executor = SearchSdkMainThreadWorker.mainExecutor
): BlockingCompletionCallback.CompletionCallbackResult<Unit> {
    val callback = BlockingCompletionCallback<Unit>()
    registerDataProvider(dataProvider, executor, callback)
    return callback.getResultBlocking()
}

internal fun <R : IndexableRecord> SearchEngine.unregisterDataProviderBlocking(
    dataProvider: IndexableDataProvider<R>,
    executor: Executor = SearchSdkMainThreadWorker.mainExecutor
): BlockingCompletionCallback.CompletionCallbackResult<Unit> {
    val callback = BlockingCompletionCallback<Unit>()
    unregisterDataProvider(dataProvider, executor, callback)
    return callback.getResultBlocking()
}

internal fun createSearchEngineWithBuiltInDataProvidersBlocking(
    apiType: ApiType,
    settings: SearchEngineSettings,
    executor: Executor = SearchSdkMainThreadWorker.mainExecutor,
): SearchEngine {
    val callback = BlockingCompletionCallback<Unit>()
    val searchEngine = createSearchEngineWithBuiltInDataProviders(apiType, settings, executor, callback)
    val result = callback.getResultBlocking()
    require(result.isResult)
    return searchEngine
}
