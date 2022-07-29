package com.mapbox.search.record

import com.mapbox.search.CompletionCallback
import com.mapbox.search.base.core.CoreSearchEngine
import com.mapbox.search.base.core.CoreUserRecordsLayer
import com.mapbox.search.base.failDebug
import com.mapbox.search.base.task.AsyncOperationTaskImpl
import com.mapbox.search.common.AsyncOperationTask
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

internal interface DataProviderEngineRegistrationService {

    fun <R : IndexableRecord> register(
        dataProvider: IndexableDataProvider<R>,
        callback: CompletionCallback<IndexableDataProviderEngineImpl>
    ): AsyncOperationTask
}

internal class DataProviderEngineRegistrationServiceImpl(
    private val registryExecutor: Executor = DEFAULT_EXECUTOR,
    private val coreLayerProvider: (String, Int) -> CoreUserRecordsLayer = Companion::createCoreLayer,
) : DataProviderEngineRegistrationService {

    private val processingProviders = mutableMapOf<String, RegistrationProcessMetadata>()
    private val processedProvider = mutableMapOf<String, IndexableDataProviderEngineImpl>()

    @Synchronized
    override fun <R : IndexableRecord> register(
        dataProvider: IndexableDataProvider<R>,
        callback: CompletionCallback<IndexableDataProviderEngineImpl>
    ): AsyncOperationTask {
        fun createTask(processMetadata: RegistrationProcessMetadata): AsyncOperationTaskImpl<Any> {
            val task = AsyncOperationTaskImpl<Any>()
            task.onCancelCallback = {
                onRegistrationTaskCancelled(dataProvider.dataProviderName, callback, processMetadata)
            }
            return task
        }

        val layer = processedProvider[dataProvider.dataProviderName]
        if (layer != null) {
            callback.onComplete(layer)
            return AsyncOperationTaskImpl.COMPLETED
        }

        val metadata = processingProviders[dataProvider.dataProviderName]
        if (metadata != null) {
            val task = createTask(metadata)
            metadata.addSubscriber(callback, task)
            return task
        }

        val engine = IndexableDataProviderEngineImpl(
            coreLayer = coreLayerProvider(
                dataProvider.dataProviderName,
                dataProvider.priority
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
        val subscribers: MutableMap<CompletionCallback<IndexableDataProviderEngineImpl>, AsyncOperationTaskImpl<Any>> = mutableMapOf(),
    ) {
        fun addSubscriber(
            callback: CompletionCallback<IndexableDataProviderEngineImpl>,
            task: AsyncOperationTaskImpl<Any>
        ) {
            if (subscribers.containsKey(callback)) {
                return
            }
            subscribers[callback] = task
        }
    }

    private companion object {

        val DEFAULT_EXECUTOR: ExecutorService = Executors.newSingleThreadExecutor { runnable ->
            Thread(runnable, "Global DataProviderRegistry executor")
        }

        fun createCoreLayer(name: String, priority: Int): CoreUserRecordsLayer {
            return CoreSearchEngine.createUserLayer(name, priority)
        }
    }
}
