package com.mapbox.search

import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.common.CompletionCallback
import com.mapbox.search.common.concurrent.SearchSdkMainThreadWorker
import com.mapbox.search.record.IndexableDataProvider
import com.mapbox.search.record.IndexableRecord
import java.util.concurrent.Executor

public interface IndexableDataProviderManager {
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
}
