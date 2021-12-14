package com.mapbox.search

import com.mapbox.search.record.IndexableDataProvider
import com.mapbox.search.record.IndexableDataProviderEngineLayer
import com.mapbox.search.record.IndexableRecord
import com.mapbox.search.utils.concurrent.SearchSdkMainThreadWorker
import java.util.concurrent.Executor

/**
 * Experimental API, can be changed or removed in the next SDK releases.
 *
 * This class allows to add and remove custom [data providers][IndexableDataProvider] to a specific scope of [search engines][SearchEngine].
 */
public interface IndexableDataProvidersRegistry {

    /**
     * Registers provided [IndexableDataProvider] with all search engines, associated with this registry. This operation causes
     * search engines to attach search index, constructed from provided data provider's records. The search index can be managed
     * via [IndexableDataProviderEngineLayer] instance.
     *
     * @param dataProvider [IndexableDataProvider] to register.
     * @param priority priority of registered [dataProvider].
     * @param executor Executor used for events dispatching. By default events are dispatched on the main thread.
     * @param callback the callback to be invoked when [dataProvider] registering operation has completed.
     * @return an object representing pending completion of the task.
     *
     * @see IndexableDataProvider.registerIndexableDataProviderEngineLayer
     */
    public fun <R : IndexableRecord> register(
        dataProvider: IndexableDataProvider<R>,
        priority: Int,
        executor: Executor,
        callback: Callback,
    ): AsyncOperationTask

    /**
     * Registers provided [IndexableDataProvider] with all search engines, associated with this registry. This operation causes
     * search engines to attach search index, constructed from provided data provider's records. The search index can be managed
     * via [IndexableDataProviderEngineLayer] instance.
     *
     * @param dataProvider [IndexableDataProvider] to register.
     * @param priority priority of registered [dataProvider].
     * @param callback the callback to be invoked on the main thread when [dataProvider] registering operation has completed.
     * @return an object representing pending completion of the task.
     *
     * @see IndexableDataProvider.registerIndexableDataProviderEngineLayer
     */
    public fun <R : IndexableRecord> register(
        dataProvider: IndexableDataProvider<R>,
        priority: Int,
        callback: Callback,
    ): AsyncOperationTask = register(
        dataProvider = dataProvider,
        priority = priority,
        executor = SearchSdkMainThreadWorker.mainExecutor,
        callback = callback,
    )

    /**
     * Unregisters provided [IndexableDataProvider] from all search engines, associated with this registry.
     *
     * @param dataProvider [IndexableDataProvider] to register.
     * @param executor Executor used for events dispatching. By default events are dispatched on the main thread.
     * @param callback the callback to be invoked when [dataProvider] registering operation has completed.
     * @return an object representing pending completion of the task.
     *
     * @see IndexableDataProvider.unregisterIndexableDataProviderEngineLayer
     */
    public fun <R : IndexableRecord> unregister(
        dataProvider: IndexableDataProvider<R>,
        executor: Executor,
        callback: Callback,
    ): AsyncOperationTask

    /**
     * Unregisters provided [IndexableDataProvider] from all search engines, associated with this registry.
     *
     * @param dataProvider [IndexableDataProvider] to register.
     * @param callback the callback to be invoked on the main thread when [dataProvider] registering operation has completed.
     * @return an object representing pending completion of the task.
     *
     * @see IndexableDataProvider.unregisterIndexableDataProviderEngineLayer
     */
    public fun <R : IndexableRecord> unregister(
        dataProvider: IndexableDataProvider<R>,
        callback: Callback,
    ): AsyncOperationTask = unregister(
        dataProvider = dataProvider,
        executor = SearchSdkMainThreadWorker.mainExecutor,
        callback = callback,
    )

    /**
     * Interface definition for a callback to be invoked when [IndexableDataProvidersRegistry] operation has completed.
     */
    public interface Callback {

        /**
         * Invoked when an operation has completed successfully.
         */
        public fun onSuccess()

        /**
         * Invoked when an error has occurred.
         *
         * @param e [Exception], occurred during operation.
         */
        public fun onError(e: Exception)
    }
}
