package com.mapbox.search

import com.mapbox.search.base.core.CoreSearchEngineInterface
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.common.CompletionCallback
import com.mapbox.search.record.IndexableDataProvider
import com.mapbox.search.record.IndexableRecord
import java.util.concurrent.Executor

public interface IndexableDataProvidersRegistry {
    public fun <R : IndexableRecord> preregister(
        dataProvider: IndexableDataProvider<R>,
        executor: Executor,
        callback: CompletionCallback<Unit>
    ): AsyncOperationTask

    public fun <R : IndexableRecord> register(
        dataProvider: IndexableDataProvider<R>,
        searchEngine: CoreSearchEngineInterface,
        executor: Executor,
        callback: CompletionCallback<Unit>
    ): AsyncOperationTask

    public fun <R : IndexableRecord> unregister(
        dataProvider: IndexableDataProvider<R>,
        searchEngine: CoreSearchEngineInterface,
        executor: Executor,
        callback: CompletionCallback<Unit>,
    ): AsyncOperationTask
}
