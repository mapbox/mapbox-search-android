package com.mapbox.search.tests_support.record

import com.mapbox.search.record.HistoryService
import com.mapbox.search.record.IndexableDataProvider
import com.mapbox.search.record.IndexableDataProviderEngine
import com.mapbox.search.record.IndexableRecord
import com.mapbox.search.result.SearchResult
import com.mapbox.search.tests_support.BlockingCompletionCallback
import com.mapbox.search.utils.concurrent.SearchSdkMainThreadWorker
import java.util.concurrent.Executor

internal fun <T : IndexableRecord> IndexableDataProvider<T>.registerIndexableDataProviderEngineBlocking(
    dataProviderEngine: IndexableDataProviderEngine,
    executor: Executor = SearchSdkMainThreadWorker.mainExecutor,
) {
    val callback = BlockingCompletionCallback<Unit>()
    registerIndexableDataProviderEngine(dataProviderEngine, executor, callback)
    return (callback.getResultBlocking() as BlockingCompletionCallback.CompletionCallbackResult.Result).result
}

internal fun <T : IndexableRecord> IndexableDataProvider<T>.unregisterIndexableDataProviderEngineBlocking(
    dataProviderEngine: IndexableDataProviderEngine,
    executor: Executor = SearchSdkMainThreadWorker.mainExecutor,
): Boolean {
    val callback = BlockingCompletionCallback<Boolean>()
    unregisterIndexableDataProviderEngine(dataProviderEngine, executor, callback)
    return (callback.getResultBlocking() as BlockingCompletionCallback.CompletionCallbackResult.Result).result
}

internal fun <T : IndexableRecord> IndexableDataProvider<T>.getBlocking(
    id: String,
    executor: Executor = SearchSdkMainThreadWorker.mainExecutor
): T? {
    val callback = BlockingCompletionCallback<T?>()
    get(id, executor, callback)
    return (callback.getResultBlocking() as BlockingCompletionCallback.CompletionCallbackResult.Result).result!!
}

internal fun <T : IndexableRecord> IndexableDataProvider<T>.getAllBlocking(
    executor: Executor = SearchSdkMainThreadWorker.mainExecutor
): List<T> {
    val callback = BlockingCompletionCallback<List<T>>()
    getAll(executor, callback)
    return (callback.getResultBlocking() as BlockingCompletionCallback.CompletionCallbackResult.Result).result
}

internal fun <T : IndexableRecord> IndexableDataProvider<T>.containsBlocking(
    id: String,
    executor: Executor = SearchSdkMainThreadWorker.mainExecutor
): Boolean {
    val callback = BlockingCompletionCallback<Boolean>()
    contains(id, executor, callback)
    return (callback.getResultBlocking() as BlockingCompletionCallback.CompletionCallbackResult.Result).result
}

internal fun <T : IndexableRecord> IndexableDataProvider<T>.upsertBlocking(
    record: T,
    executor: Executor = SearchSdkMainThreadWorker.mainExecutor
) {
    val callback = BlockingCompletionCallback<Unit>()
    upsert(record, executor, callback)
    callback.getResultBlocking()
}

internal fun <T : IndexableRecord> IndexableDataProvider<T>.upsertAllBlocking(
    records: List<T>,
    executor: Executor = SearchSdkMainThreadWorker.mainExecutor
) {
    val callback = BlockingCompletionCallback<Unit>()
    upsertAll(records, executor, callback)
    callback.getResultBlocking()
}

internal fun <T : IndexableRecord> IndexableDataProvider<T>.removeBlocking(
    id: String,
    executor: Executor = SearchSdkMainThreadWorker.mainExecutor
): Boolean {
    val callback = BlockingCompletionCallback<Boolean>()
    remove(id, executor, callback)
    return (callback.getResultBlocking() as BlockingCompletionCallback.CompletionCallbackResult.Result).result
}

internal fun <T : IndexableRecord> IndexableDataProvider<T>.clearBlocking(
    executor: Executor = SearchSdkMainThreadWorker.mainExecutor
) {
    val callback = BlockingCompletionCallback<Unit>()
    clear(executor, callback)
    callback.getResultBlocking()
}

internal fun <T : IndexableRecord> IndexableDataProvider<T>.getSizeBlocking(
    executor: Executor = SearchSdkMainThreadWorker.mainExecutor
): Int = getAllBlocking(executor).size

internal fun HistoryService.addToHistoryIfNeededBlocking(
    searchResult: SearchResult,
    executor: Executor = SearchSdkMainThreadWorker.mainExecutor
) {
    val callback = BlockingCompletionCallback<Boolean>()
    addToHistoryIfNeeded(searchResult, executor, callback)
    callback.getResultBlocking()
}
