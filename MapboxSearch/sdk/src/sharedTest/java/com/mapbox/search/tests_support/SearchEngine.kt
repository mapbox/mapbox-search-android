package com.mapbox.search.tests_support

import com.mapbox.search.ApiType
import com.mapbox.search.MapboxSearchSdk
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchEngineSettings
import com.mapbox.search.SearchOptions
import com.mapbox.search.SelectOptions
import com.mapbox.search.record.IndexableDataProvider
import com.mapbox.search.record.IndexableRecord
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.utils.concurrent.SearchSdkMainThreadWorker
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

internal fun MapboxSearchSdk.createSearchEngineWithBuiltInDataProvidersBlocking(
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
