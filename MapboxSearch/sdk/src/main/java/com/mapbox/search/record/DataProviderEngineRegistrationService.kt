package com.mapbox.search.record

import com.mapbox.search.AsyncOperationTask
import com.mapbox.search.AsyncOperationTaskImpl
import com.mapbox.search.CompletedAsyncOperationTask
import com.mapbox.search.CompletionCallback
import com.mapbox.search.common.failDebug
import com.mapbox.search.core.CoreSearchEngine
import com.mapbox.search.core.CoreUserRecordsLayer
import com.mapbox.search.utils.SyncLocker
import java.util.concurrent.Executor

internal interface DataProviderEngineRegistrationService {

    fun <R : IndexableRecord> register(
        dataProvider: IndexableDataProvider<R>,
        callback: CompletionCallback<IndexableDataProviderEngineImpl>
    ): AsyncOperationTask
}

internal class DataProviderEngineRegistrationServiceImpl(
    private val registryExecutor: Executor,
    private val syncLocker: SyncLocker,
    private val coreLayerProvider: (String, Int) -> CoreUserRecordsLayer = ::createCoreLayer,
) : DataProviderEngineRegistrationService {

    private val processingProviders = mutableMapOf<String, RegistrationProcessMetadata>()
    private val processedProvider = mutableMapOf<String, IndexableDataProviderEngineImpl>()

    @Synchronized
    override fun <R : IndexableRecord> register(
        dataProvider: IndexableDataProvider<R>,
        callback: CompletionCallback<IndexableDataProviderEngineImpl>
    ): AsyncOperationTask {
        fun createTask(processMetadata: RegistrationProcessMetadata): AsyncOperationTaskImpl {
            val task = AsyncOperationTaskImpl()
            task.onCancelCallback = {
                onRegistrationTaskCancelled(dataProvider.dataProviderName, callback, processMetadata)
            }
            return task
        }

        val layer = processedProvider[dataProvider.dataProviderName]
        if (layer != null) {
            callback.onComplete(layer)
            return CompletedAsyncOperationTask
        }

        val metadata = processingProviders[dataProvider.dataProviderName]
        if (metadata != null) {
            val task = createTask(metadata)
            metadata.addSubscriber(callback, task)
            return task
        }

        val engine = IndexableDataProviderEngineImpl(
            coreLayerContext = IndexableDataProviderEngineImpl.CoreLayerContext(
                coreLayerProvider(
                    dataProvider.dataProviderName,
                    dataProvider.priority
                ), syncLocker
            )
        )

        val processTask = dataProvider.registerIndexableDataProviderEngine(
            dataProviderEngine = engine,
            executor = registryExecutor,
            callback = object : CompletionCallback<Unit> {
                override fun onComplete(result: Unit) {
                    onEngineRegistered(dataProvider.dataProviderName, engine)
                }

                override fun onError(e: Exception) {
                    onEngineRegistrationError(dataProvider.dataProviderName, e)
                }
            }
        )

        val processMetadata = RegistrationProcessMetadata(processTask)
        val task = createTask(processMetadata)
        processMetadata.addSubscriber(callback, task)
        processingProviders[dataProvider.dataProviderName] = processMetadata
        return task
    }

    @Synchronized
    private fun onRegistrationTaskCancelled(
        providerName: String,
        callback: CompletionCallback<IndexableDataProviderEngineImpl>,
        processMetadata: RegistrationProcessMetadata
    ) {
        processMetadata.subscribers.remove(callback)
        if (processMetadata.subscribers.isEmpty()) {
            // If all the subscribers cancelled registration process, we should cancel an actual registration task as well
            processMetadata.processTask.cancel()
            processingProviders.remove(providerName)
        }
    }

    @Synchronized
    private fun onEngineRegistered(providerName: String, layer: IndexableDataProviderEngineImpl) {
        processedProvider[providerName] = layer

        val processMetadata = processingProviders.remove(providerName)
        if (processMetadata == null) {
            failDebug { "No callbacks registered" }
            return
        } else {
            registryExecutor.execute {
                processMetadata.subscribers.entries.forEach { (callback, task) ->
                    task.onComplete()
                    callback.onComplete(layer)
                }
                processMetadata.subscribers.clear()
            }
        }
    }

    @Synchronized
    private fun onEngineRegistrationError(providerName: String, e: Exception) {
        val processMetadata = processingProviders.remove(providerName)
        if (processMetadata == null) {
            failDebug { "No callbacks registered" }
            return
        } else {
            registryExecutor.execute {
                processMetadata.subscribers.entries.forEach { (callback, task) ->
                    task.onComplete()
                    callback.onError(e)
                }
                processMetadata.subscribers.clear()
            }
        }
    }

    private data class RegistrationProcessMetadata(
        val processTask: AsyncOperationTask,
        val subscribers: MutableMap<CompletionCallback<IndexableDataProviderEngineImpl>, AsyncOperationTaskImpl> = mutableMapOf(),
    ) {
        fun addSubscriber(
            callback: CompletionCallback<IndexableDataProviderEngineImpl>,
            task: AsyncOperationTaskImpl
        ) {
            if (subscribers.containsKey(callback)) {
                return
            }
            subscribers[callback] = task
        }
    }

    private companion object {
        fun createCoreLayer(name: String, priority: Int): CoreUserRecordsLayer {
            return CoreSearchEngine.createUserLayer(name, priority)
        }
    }
}
