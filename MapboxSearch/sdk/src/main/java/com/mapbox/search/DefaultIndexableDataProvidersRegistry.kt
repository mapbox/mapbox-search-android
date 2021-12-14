package com.mapbox.search

import com.mapbox.search.core.CoreSearchEngine
import com.mapbox.search.core.CoreSearchEngineInterface
import com.mapbox.search.core.CoreUserRecordsLayer
import com.mapbox.search.record.DataProviderResolver
import com.mapbox.search.record.IndexableDataProvider
import com.mapbox.search.record.IndexableDataProviderEngineLayerImpl
import com.mapbox.search.record.IndexableDataProviderEngineLayerImpl.CoreLayerContext
import com.mapbox.search.record.IndexableRecord
import com.mapbox.search.utils.SyncLocker
import java.util.Collections
import java.util.WeakHashMap
import java.util.concurrent.Executor

internal class DefaultIndexableDataProvidersRegistry(
    coreSearchEngines: List<CoreSearchEngineInterface> = emptyList(),
    private val registryExecutor: Executor,
    private val syncLocker: SyncLocker,
    private val coreLayerProvider: (String, Int) -> CoreUserRecordsLayer = Companion::createCoreLayer,
) : IndexableDataProvidersRegistry, DataProviderResolver {

    private val coreSearchEngines: MutableSet<CoreSearchEngineInterface> = Collections
        .newSetFromMap(WeakHashMap<CoreSearchEngineInterface, Boolean>())
        .apply {
            addAll(coreSearchEngines)
        }

    private val dataProvidersContextMap: MutableMap<String, DataProviderContext> = mutableMapOf()
    private val coreLayersLock = Any()

    private data class DataProviderContext(
        val engineLayer: IndexableDataProviderEngineLayerImpl,
        val provider: IndexableDataProvider<*>,
    )

    /**
     * Note: we don't use `syncLocker` here, because we assume this function is called
     * only during search engine creation, meaning that no one is using this engine and
     * we can safely add CoreUserLayer w/o `syncLocker` synchronization.
     */
    fun addCoreSearchEngine(coreEngine: CoreSearchEngineInterface) {
        synchronized(coreLayersLock) {
            if (coreSearchEngines.contains(coreEngine)) {
                return
            }

            coreSearchEngines.add(coreEngine)
            dataProvidersContextMap.forEach { (_, context) ->
                coreEngine.addUserLayer(context.engineLayer.coreLayerContext.coreLayer)
            }
        }
    }

    override fun <R : IndexableRecord> register(
        dataProvider: IndexableDataProvider<R>,
        priority: Int,
        executor: Executor,
        callback: IndexableDataProvidersRegistry.Callback
    ): AsyncOperationTask {
        if (dataProvidersContextMap.containsKey(dataProvider.dataProviderName)) {
            executor.execute { callback.onSuccess() }
            return CompletedAsyncOperationTask
        }

        val coreUserRecordsLayer = coreLayerProvider(dataProvider.dataProviderName, priority)
        val customDataProviderEngineLayer = IndexableDataProviderEngineLayerImpl.create(
            CoreLayerContext(coreUserRecordsLayer, syncLocker)
        )

        return dataProvider.registerIndexableDataProviderEngineLayer(
            dataProviderEngineLayer = customDataProviderEngineLayer,
            executor = registryExecutor,
            callback = object : CompletionCallback<Unit> {
                override fun onComplete(result: Unit) {
                    syncLocker.executeInSync {
                        synchronized(coreLayersLock) {
                            dataProvidersContextMap[dataProvider.dataProviderName] =
                                DataProviderContext(
                                    customDataProviderEngineLayer, dataProvider
                                )
                            coreSearchEngines.forEach { coreEngine ->
                                coreEngine.addUserLayer(coreUserRecordsLayer)
                            }
                        }
                    }

                    executor.execute {
                        callback.onSuccess()
                    }
                }

                override fun onError(e: Exception) {
                    executor.execute {
                        callback.onError(e)
                    }
                }
            }
        )
    }

    override fun <R : IndexableRecord> unregister(
        dataProvider: IndexableDataProvider<R>,
        executor: Executor,
        callback: IndexableDataProvidersRegistry.Callback
    ): AsyncOperationTask {
        val dataProviderEngine = dataProvidersContextMap[dataProvider.dataProviderName]?.engineLayer
        if (dataProviderEngine == null) {
            executor.execute { callback.onSuccess() }
            return CompletedAsyncOperationTask
        }

        return dataProvider.unregisterIndexableDataProviderEngineLayer(
            dataProviderEngineLayer = dataProviderEngine,
            executor = registryExecutor,
            callback = object : CompletionCallback<Boolean> {
                override fun onComplete(result: Boolean) {
                    syncLocker.executeInSync {
                        synchronized(coreLayersLock) {
                            dataProvidersContextMap.remove(dataProvider.dataProviderName)
                            coreSearchEngines.forEach { coreEngine ->
                                coreEngine.removeUserLayer(dataProvider.dataProviderName)
                            }
                        }
                    }

                    executor.execute {
                        callback.onSuccess()
                    }
                }

                override fun onError(e: Exception) {
                    executor.execute {
                        callback.onError(e)
                    }
                }
            }
        )
    }

    override fun getRecordsLayer(name: String): IndexableDataProvider<*>? {
        return synchronized(coreLayersLock) {
            dataProvidersContextMap[name]?.provider
        }
    }

    private companion object {

        @JvmSynthetic
        fun createCoreLayer(name: String, priority: Int): CoreUserRecordsLayer {
            return CoreSearchEngine.createUserLayer(name, priority)
        }
    }
}
