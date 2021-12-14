package com.mapbox.search.tests_support.record

import com.mapbox.search.AsyncOperationTask
import com.mapbox.search.AsyncOperationTaskImpl
import com.mapbox.search.CompletionCallback
import com.mapbox.search.plusAssign
import com.mapbox.search.record.IndexableDataProviderEngineLayer
import com.mapbox.search.record.IndexableRecord
import com.mapbox.search.record.LocalDataProviderImpl
import com.mapbox.search.runIfNotCancelled
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import kotlin.Exception

internal class TestDataProvider<R : IndexableRecord> : LocalDataProviderImpl<R> {

    var mode: Mode = Mode.Default

    internal constructor(
        dataProviderName: String = "TEST_DATA_PROVIDER",
        stubStorage: StubRecordsStorage<R> = StubRecordsStorage(),
    ) : super(
        dataProviderName = dataProviderName,
        recordsStorage = stubStorage,
    )

    internal constructor(
        executorService: ExecutorService,
        dataProviderName: String = "TEST_DATA_PROVIDER",
        stubStorage: StubRecordsStorage<R> = StubRecordsStorage(),
    ) : super(
        dataProviderName = dataProviderName,
        recordsStorage = stubStorage,
        backgroundTaskExecutorService = executorService,
    )

    val records: List<R>?
        get() = (dataState as? DataState.Data)?.records?.values?.toList()

    override fun registerIndexableDataProviderEngineLayer(
        dataProviderEngineLayer: IndexableDataProviderEngineLayer,
        executor: Executor,
        callback: CompletionCallback<Unit>
    ): AsyncOperationTask {
        return trySubmitOverrideOperation(executor, callback)
            ?: super.registerIndexableDataProviderEngineLayer(dataProviderEngineLayer, executor, callback)
    }

    override fun unregisterIndexableDataProviderEngineLayer(
        dataProviderEngineLayer: IndexableDataProviderEngineLayer,
        executor: Executor,
        callback: CompletionCallback<Boolean>
    ): AsyncOperationTask {
        return trySubmitOverrideOperation(executor, callback)
            ?: super.unregisterIndexableDataProviderEngineLayer(dataProviderEngineLayer, executor, callback)
    }

    override fun get(id: String, executor: Executor, callback: CompletionCallback<in R?>): AsyncOperationTask {
        return trySubmitOverrideOperation(executor, callback) ?: super.get(id, executor, callback)
    }

    override fun getAll(executor: Executor, callback: CompletionCallback<List<R>>): AsyncOperationTask {
        return trySubmitOverrideOperation(executor, callback) ?: super.getAll(executor, callback)
    }

    override fun contains(id: String, executor: Executor, callback: CompletionCallback<Boolean>): AsyncOperationTask {
        return trySubmitOverrideOperation(executor, callback) ?: super.contains(id, executor, callback)
    }

    private fun trySubmitOverrideOperation(executor: Executor, callback: CompletionCallback<*>): AsyncOperationTask? {
        return when (val mode = mode) {
            Mode.Default -> null
            is Mode.Fail -> AsyncOperationTaskImpl().apply {
                executor.execute {
                    runIfNotCancelled {
                        onComplete()
                        callback.onError(mode.e)
                    }
                }
            }
        }
    }

    sealed class Mode {
        object Default : Mode()
        data class Fail(val e: Exception) : Mode()
    }
}
