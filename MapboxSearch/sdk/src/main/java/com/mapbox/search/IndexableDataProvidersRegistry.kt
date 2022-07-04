package com.mapbox.search

import com.mapbox.search.core.CoreSearchEngineInterface
import com.mapbox.search.record.IndexableDataProvider
import com.mapbox.search.record.IndexableRecord
import java.util.concurrent.Executor

internal interface IndexableDataProvidersRegistry {

    fun <R : IndexableRecord> preregister(
        dataProvider: IndexableDataProvider<R>,
        executor: Executor,
        callback: CompletionCallback<Unit>
    ): AsyncOperationTask

    fun <R : IndexableRecord> register(
        dataProvider: IndexableDataProvider<R>,
        searchEngine: CoreSearchEngineInterface,
        executor: Executor,
        callback: CompletionCallback<Unit>
    ): AsyncOperationTask

    fun <R : IndexableRecord> unregister(
        dataProvider: IndexableDataProvider<R>,
        searchEngine: CoreSearchEngineInterface,
        executor: Executor,
        callback: CompletionCallback<Unit>,
    ): AsyncOperationTask
}
