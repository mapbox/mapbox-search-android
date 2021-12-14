package com.mapbox.search.tests_support.record

import com.mapbox.search.record.HistoryService
import com.mapbox.search.record.IndexableDataProvider
import com.mapbox.search.record.IndexableDataProviderEngineLayer
import com.mapbox.search.record.IndexableRecord
import com.mapbox.search.result.SearchResult
import com.mapbox.search.tests_support.BlockingCompletionCallback
import java.util.concurrent.Executor

internal fun <T : IndexableRecord> IndexableDataProvider<T>.registerIndexableDataProviderEngineLayerBlocking(
    dataProviderEngineLayer: IndexableDataProviderEngineLayer,
    executor: Executor,
) {
    val callback = BlockingCompletionCallback<Unit>()
    registerIndexableDataProviderEngineLayer(dataProviderEngineLayer, executor, callback)
    return (callback.getResultBlocking() as BlockingCompletionCallback.CompletionCallbackResult.Result).result
}

internal fun <T : IndexableRecord> IndexableDataProvider<T>.unregisterIndexableDataProviderEngineLayerBlocking(
    dataProviderEngineLayer: IndexableDataProviderEngineLayer,
    executor: Executor,
): Boolean {
    val callback = BlockingCompletionCallback<Boolean>()
    unregisterIndexableDataProviderEngineLayer(dataProviderEngineLayer, executor, callback)
    return (callback.getResultBlocking() as BlockingCompletionCallback.CompletionCallbackResult.Result).result
}

internal fun <T : IndexableRecord> IndexableDataProvider<T>.getBlocking(id: String, executor: Executor): T? {
    val callback = BlockingCompletionCallback<T?>()
    get(id, executor, callback)
    return (callback.getResultBlocking() as BlockingCompletionCallback.CompletionCallbackResult.Result).result!!
}

internal fun <T : IndexableRecord> IndexableDataProvider<T>.getAllBlocking(executor: Executor): List<T> {
    val callback = BlockingCompletionCallback<List<T>>()
    getAll(executor, callback)
    return (callback.getResultBlocking() as BlockingCompletionCallback.CompletionCallbackResult.Result).result
}

internal fun <T : IndexableRecord> IndexableDataProvider<T>.containsBlocking(id: String, executor: Executor): Boolean {
    val callback = BlockingCompletionCallback<Boolean>()
    contains(id, executor, callback)
    return (callback.getResultBlocking() as BlockingCompletionCallback.CompletionCallbackResult.Result).result
}

internal fun <T : IndexableRecord> IndexableDataProvider<T>.addBlocking(record: T, executor: Executor) {
    val callback = BlockingCompletionCallback<Unit>()
    add(record, executor, callback)
    callback.getResultBlocking()
}

internal fun <T : IndexableRecord> IndexableDataProvider<T>.addAllBlocking(records: List<T>, executor: Executor) {
    val callback = BlockingCompletionCallback<Unit>()
    addAll(records, executor, callback)
    callback.getResultBlocking()
}

internal fun <T : IndexableRecord> IndexableDataProvider<T>.updateBlocking(record: T, executor: Executor) {
    val callback = BlockingCompletionCallback<Unit>()
    update(record, executor, callback)
    callback.getResultBlocking()
}

internal fun <T : IndexableRecord> IndexableDataProvider<T>.removeBlocking(id: String, executor: Executor): Boolean {
    val callback = BlockingCompletionCallback<Boolean>()
    remove(id, executor, callback)
    return (callback.getResultBlocking() as BlockingCompletionCallback.CompletionCallbackResult.Result).result
}

internal fun <T : IndexableRecord> IndexableDataProvider<T>.clearBlocking(executor: Executor) {
    val callback = BlockingCompletionCallback<Unit>()
    clear(executor, callback)
    callback.getResultBlocking()
}

internal fun <T : IndexableRecord> IndexableDataProvider<T>.getSizeBlocking(executor: Executor): Int = getAllBlocking(executor).size

internal fun HistoryService.addToHistoryIfNeededBlocking(searchResult: SearchResult, executor: Executor) {
    val callback = BlockingCompletionCallback<Boolean>()
    addToHistoryIfNeeded(searchResult, executor, callback)
    callback.getResultBlocking()
}
