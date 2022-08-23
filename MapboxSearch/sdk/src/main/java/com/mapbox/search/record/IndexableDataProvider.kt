package com.mapbox.search.record

import com.mapbox.search.CompletionCallback
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.common.concurrent.SearchSdkMainThreadWorker
import java.util.concurrent.Executor

/**
 * Defines an interface for external data indexing.
 *
 * @see LocalDataProvider
 * @see FavoritesDataProvider
 * @see HistoryDataProvider
 */
public interface IndexableDataProvider<R : IndexableRecord> {

    /**
     * Data provider name.
     */
    public val dataProviderName: String

    /**
     * Data provider priority.
     * In case of multiple data providers registered in a [com.mapbox.search.SearchEngine],
     * [IndexableRecord]'s with higher priority will be ranked higher in the search results list.
     *
     * @see [com.mapbox.search.SearchEngine.registerDataProvider]
     */
    public val priority: Int

    /**
     * Experimental API, can be changed or removed in the next SDK releases.
     *
     * Registers [engine][IndexableDataProviderEngine] with this data provider.
     * Allows to keep search index of [search engines][com.mapbox.search.SearchEngine],
     * associated with this data provider, up-to-date.
     *
     * @param dataProviderEngine engine, associated with this data provider.
     * @param executor Executor used for events dispatching. By default events are dispatched on the main thread.
     * @param callback Callback to handle result.
     * @return an object representing pending completion of the task.
     */
    public fun registerIndexableDataProviderEngine(
        dataProviderEngine: IndexableDataProviderEngine,
        executor: Executor,
        callback: CompletionCallback<Unit>
    ): AsyncOperationTask

    /**
     * Experimental API, can be changed or removed in the next SDK releases.
     *
     * Registers [engine][IndexableDataProviderEngine] with this data provider.
     * Allows to keep search index of [search engines][com.mapbox.search.SearchEngine],
     * associated with this data provider, up-to-date.
     *
     * @param dataProviderEngine engine, associated with this data provider.
     * @param callback Callback to handle result, triggered on the main thread.
     * @return an object representing pending completion of the task.
     */
    public fun registerIndexableDataProviderEngine(
        dataProviderEngine: IndexableDataProviderEngine,
        callback: CompletionCallback<Unit>,
    ): AsyncOperationTask = registerIndexableDataProviderEngine(
        dataProviderEngine = dataProviderEngine,
        executor = SearchSdkMainThreadWorker.mainExecutor,
        callback = callback,
    )

    /**
     * Experimental API, can be changed or removed in the next SDK releases.
     *
     * Unregisters [engine][IndexableDataProviderEngine] from this data provider.
     *
     * Passed to [callback] `true` if [dataProviderEngine] was removed and `false` if there was no such engine.
     *
     * @param dataProviderEngine engine, associated with this data provider.
     * @param executor Executor used for events dispatching. By default events are dispatched on the main thread.
     * @param callback Callback to handle result.
     * @return an object representing pending completion of the task.
     *
     * @see registerIndexableDataProviderEngine
     */
    public fun unregisterIndexableDataProviderEngine(
        dataProviderEngine: IndexableDataProviderEngine,
        executor: Executor,
        callback: CompletionCallback<Boolean>
    ): AsyncOperationTask

    /**
     * Experimental API, can be changed or removed in the next SDK releases.
     *
     * Unregisters [engine][IndexableDataProviderEngine] from this data provider.
     *
     * Passed to [callback] `true` if [dataProviderEngine] was removed and `false` if there was no such engine.
     *
     * @param dataProviderEngine engine, associated with this data provider.
     * @param callback Callback to handle result, triggered on the main thread.
     * @return an object representing pending completion of the task.
     *
     * @see registerIndexableDataProviderEngine
     */
    public fun unregisterIndexableDataProviderEngine(
        dataProviderEngine: IndexableDataProviderEngine,
        callback: CompletionCallback<Boolean>,
    ): AsyncOperationTask = unregisterIndexableDataProviderEngine(
        dataProviderEngine = dataProviderEngine,
        executor = SearchSdkMainThreadWorker.mainExecutor,
        callback = callback,
    )

    /**
     * Retrieves the record with specified [id] or `null` if there's no such record.
     *
     * @param id Record's id.
     * @param executor Executor used for events dispatching. By default events are dispatched on the main thread.
     * @param callback Callback to handle result.
     * @return an object representing pending completion of the task.
     */
    public fun get(
        id: String,
        executor: Executor,
        callback: CompletionCallback<in R?>,
    ): AsyncOperationTask

    /**
     * Retrieves the record with specified [id] or `null` if there's no such record.
     *
     * @param id Record's id.
     * @param callback Callback to handle result, triggered on the main thread.
     * @return an object representing pending completion of the task.
     */
    public fun get(
        id: String,
        callback: CompletionCallback<in R?>,
    ): AsyncOperationTask = get(
        id = id,
        executor = SearchSdkMainThreadWorker.mainExecutor,
        callback = callback,
    )

    /**
     * Retrieves all records from this provider.
     *
     * @param executor Executor used for events dispatching. By default events are dispatched on the main thread.
     * @param callback Callback to handle result.
     * @return an object representing pending completion of the task.
     */
    public fun getAll(executor: Executor, callback: CompletionCallback<List<R>>): AsyncOperationTask

    /**
     * Retrieves all records from this provider.
     *
     * @param callback Callback to handle result, triggered on the main thread.
     * @return an object representing pending completion of the task.
     */
    public fun getAll(callback: CompletionCallback<List<R>>): AsyncOperationTask = getAll(
        executor = SearchSdkMainThreadWorker.mainExecutor,
        callback = callback,
    )

    /**
     * Checks whether this data provider contains a record with specified id.
     *
     * @param id Record's id to check.
     * @param executor Executor used for events dispatching. By default events are dispatched on the main thread.
     * @param callback Callback to handle result.
     * @return an object representing pending completion of the task.
     */
    public fun contains(id: String, executor: Executor, callback: CompletionCallback<Boolean>): AsyncOperationTask

    /**
     * Checks whether this data provider contains a record with specified id.
     *
     * @param id Record's id to check.
     * @param callback Callback to handle result, triggered on the main thread.
     * @return an object representing pending completion of the task.
     */
    public fun contains(id: String, callback: CompletionCallback<Boolean>): AsyncOperationTask = contains(
        id = id,
        executor = SearchSdkMainThreadWorker.mainExecutor,
        callback = callback,
    )

    /**
     * Insert or update a [record] in this data provider.
     *
     * @param record Record to be added.
     * @param executor Executor used for events dispatching. By default events are dispatched on the main thread.
     * @param callback Callback to handle result.
     * @return an object representing pending completion of the task.
     */
    public fun upsert(record: R, executor: Executor, callback: CompletionCallback<Unit>): AsyncOperationTask

    /**
     * Insert or update a [record] to this data provider.
     *
     * @param record Record to be added.
     * @param callback Callback to handle result, triggered on the main thread.
     * @return an object representing pending completion of the task.
     */
    public fun upsert(record: R, callback: CompletionCallback<Unit>): AsyncOperationTask = upsert(
        record = record,
        executor = SearchSdkMainThreadWorker.mainExecutor,
        callback = callback,
    )

    /**
     * Upsert (insert or update) multiple records to this data provider.
     *
     * @param records Records to be added.
     * @param executor Executor used for events dispatching. By default events are dispatched on the main thread.
     * @param callback Callback to handle result.
     * @return an object representing pending completion of the task.
     */
    public fun upsertAll(records: List<R>, executor: Executor, callback: CompletionCallback<Unit>): AsyncOperationTask

    /**
     * Upsert (insert or update) multiple records to this data provider.
     *
     * @param records Records to be added.
     * @param callback Callback to handle result, triggered on the main thread.
     * @return an object representing pending completion of the task.
     */
    public fun upsertAll(records: List<R>, callback: CompletionCallback<Unit>): AsyncOperationTask = upsertAll(
        records = records,
        executor = SearchSdkMainThreadWorker.mainExecutor,
        callback = callback,
    )

    /**
     * Removes the record with specified [id].
     * Passed to [callback] true if the record was removed and `false` if there was no record with the specified [id].
     *
     * @param id Id of the record to remove.
     * @param executor Executor used for events dispatching. By default events are dispatched on the main thread.
     * @param callback Callback to handle result.
     * @return an object representing pending completion of the task.
     */
    public fun remove(id: String, executor: Executor, callback: CompletionCallback<Boolean>): AsyncOperationTask

    /**
     * Removes the record with specified [id].
     * Passed to [callback] true if the record was removed and `false` if there was no record with the specified [id].
     *
     * @param id Id of the record to remove.
     * @param callback Callback to handle result, triggered on the main thread.
     * @return an object representing pending completion of the task.
     */
    public fun remove(id: String, callback: CompletionCallback<Boolean>): AsyncOperationTask = remove(
        id = id,
        executor = SearchSdkMainThreadWorker.mainExecutor,
        callback = callback,
    )

    /**
     * Removes all the records in this data provider.
     *
     * @param executor Executor used for events dispatching. By default events are dispatched on the main thread.
     * @param callback Callback to handle result.
     * @return an object representing pending completion of the task.
     */
    public fun clear(executor: Executor, callback: CompletionCallback<Unit>): AsyncOperationTask

    /**
     * Removes all the records in this data provider.
     *
     * @param callback Callback to handle result, triggered on the main thread.
     * @return an object representing pending completion of the task.
     */
    public fun clear(callback: CompletionCallback<Unit>): AsyncOperationTask = clear(
        executor = SearchSdkMainThreadWorker.mainExecutor,
        callback = callback,
    )
}
