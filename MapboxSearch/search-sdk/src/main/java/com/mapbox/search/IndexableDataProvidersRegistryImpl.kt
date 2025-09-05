package com.mapbox.search

import com.mapbox.search.base.assertDebug
import com.mapbox.search.base.core.CoreSearchEngineInterface
import com.mapbox.search.base.record.BaseIndexableRecord
import com.mapbox.search.base.record.IndexableRecordResolver
import com.mapbox.search.base.task.AsyncOperationTaskImpl
import com.mapbox.search.base.utils.extension.addValue
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.common.CompletionCallback
import com.mapbox.search.record.DataProviderEngineRegistrationService
import com.mapbox.search.record.IndexableDataProvider
import com.mapbox.search.record.IndexableDataProviderEngineImpl
import com.mapbox.search.record.IndexableRecord
import com.mapbox.search.record.mapToBase
import java.util.concurrent.Executor

internal class IndexableDataProvidersRegistryImpl(
    private val dataProviderEngineRegistrationService: DataProviderEngineRegistrationService
) : IndexableDataProvidersRegistry, IndexableRecordResolver {

    private val registry = Registry()

    @Synchronized
    override fun resolve(
        dataProviderName: String,
        userRecordId: String,
        executor: Executor,
        callback: (Result<BaseIndexableRecord>) -> Unit
    ): AsyncOperationTask {
        val dataProvider = registry.dataProviderContext(dataProviderName)?.provider
        if (dataProvider == null) {
            executor.execute {
                callback(Result.failure(Exception("Unable to find data provider with name: $dataProvider")))
            }
            return AsyncOperationTaskImpl.COMPLETED
        }

        return dataProvider.get(userRecordId, executor, object : CompletionCallback<IndexableRecord?> {
            override fun onComplete(result: IndexableRecord?) {
                val callbackResult = if (result == null) {
                    Result.failure(Exception("No record with id `$userRecordId` in `$dataProviderName` data provider"))
                } else {
                    Result.success(result.mapToBase())
                }
                callback(callbackResult)
            }

            override fun onError(e: Exception) {
                callback(Result.failure(e))
            }
        })
    }

    override fun <R : IndexableRecord> preregister(
        dataProvider: IndexableDataProvider<R>,
        executor: Executor,
        callback: CompletionCallback<Unit>
    ): AsyncOperationTask {
        val dataProviderContext = registry.dataProviderContext(dataProvider.dataProviderName)
        if (dataProviderContext != null) {
            executor.execute { callback.onComplete(Unit) }
            return AsyncOperationTaskImpl.COMPLETED
        }

        val task = AsyncOperationTaskImpl<Any>()
        task += dataProviderEngineRegistrationService.register(
            dataProvider,
            object : CompletionCallback<IndexableDataProviderEngineImpl> {
                override fun onComplete(result: IndexableDataProviderEngineImpl) {
                    task.runIfNotCancelled {
                        runSynchronized {
                            registry.registerDataProviderContext(
                                dataProvider,
                                DataProviderContext(
                                    engine = result,
                                    provider = dataProvider,
                                )
                            )
                        }

                        executor.execute {
                            task.onComplete()
                            callback.onComplete(Unit)
                        }
                    }
                }

                override fun onError(e: Exception) {
                    executor.execute {
                        task.runIfNotCancelled {
                            task.onComplete()
                            callback.onError(e)
                        }
                    }
                }
            }
        )
        return task
    }

    @Synchronized
    override fun <R : IndexableRecord> register(
        dataProvider: IndexableDataProvider<R>,
        searchEngine: CoreSearchEngineInterface,
        executor: Executor,
        callback: CompletionCallback<Unit>
    ): AsyncOperationTask {
        if (registry.isProviderRegistered(dataProvider, searchEngine)) {
            executor.execute {
                callback.onError(
                    IllegalStateException(
                        "${dataProvider.dataProviderName} has already been registered in the provided search engine"
                    )
                )
            }
            return AsyncOperationTaskImpl.COMPLETED
        }

        val dataProviderContext = registry.dataProviderContext(dataProvider.dataProviderName)
        if (dataProviderContext != null) {
            registry.register(dataProvider, searchEngine)
            searchEngine.addUserLayer(dataProviderContext.engine.coreLayer)
            executor.execute { callback.onComplete(Unit) }
            return AsyncOperationTaskImpl.COMPLETED
        }

        val task = AsyncOperationTaskImpl<Any>()
        task += dataProviderEngineRegistrationService.register(
            dataProvider,
            object : CompletionCallback<IndexableDataProviderEngineImpl> {
                override fun onComplete(result: IndexableDataProviderEngineImpl) {
                    task.runIfNotCancelled {
                        runSynchronized {
                            registry.registerDataProviderContext(
                                dataProvider,
                                DataProviderContext(
                                    engine = result,
                                    provider = dataProvider,
                                )
                            )
                            registry.register(dataProvider, searchEngine)
                            searchEngine.addUserLayer(result.coreLayer)
                        }

                        executor.execute {
                            task.onComplete()
                            callback.onComplete(Unit)
                        }
                    }
                }

                override fun onError(e: Exception) {
                    executor.execute {
                        task.runIfNotCancelled {
                            task.onComplete()
                            callback.onError(e)
                        }
                    }
                }
            }
        )
        return task
    }

    @Synchronized
    override fun <R : IndexableRecord> unregister(
        dataProvider: IndexableDataProvider<R>,
        searchEngine: CoreSearchEngineInterface,
        executor: Executor,
        callback: CompletionCallback<Unit>
    ): AsyncOperationTask {
        if (!registry.isProviderRegistered(dataProvider, searchEngine)) {
            executor.execute {
                callback.onError(
                    Exception(
                        "Data provider ${dataProvider.dataProviderName} is not associated with this search engine"
                    )
                )
            }
            return AsyncOperationTaskImpl.COMPLETED
        }

        val context = registry.dataProviderContext(dataProvider.dataProviderName)
        assertDebug(context != null) {
            "Null context for registered data provider. Data provider: ${dataProvider.dataProviderName}"
        }
        context ?: return AsyncOperationTaskImpl.COMPLETED

        val task = AsyncOperationTaskImpl<Any>()
        task.runIfNotCancelled {
            runSynchronized {
                registry.unregister(dataProvider, searchEngine)
                searchEngine.removeUserLayer(context.engine.coreLayer)
            }
            executor.execute {
                task.onComplete()
                callback.onComplete(Unit)
            }
        }
        return task
    }

    @Synchronized
    private fun runSynchronized(action: () -> Unit) {
        action()
    }

    private data class DataProviderContext(
        val engine: IndexableDataProviderEngineImpl,
        val provider: IndexableDataProvider<*>,
    )

    private class Registry {

        private val engineProviders: MutableMap<CoreSearchEngineInterface, MutableSet<String>> = mutableMapOf()
        private val registeredProviders: MutableMap<String, MutableSet<CoreSearchEngineInterface>> = mutableMapOf()

        private val dataProvidersContextMap: MutableMap<String, DataProviderContext> = mutableMapOf()

        fun register(dataProvider: IndexableDataProvider<*>, searchEngine: CoreSearchEngineInterface) {
            engineProviders.addValue(searchEngine, dataProvider.dataProviderName)
            registeredProviders.addValue(dataProvider.dataProviderName, searchEngine)
        }

        fun unregister(dataProvider: IndexableDataProvider<*>, searchEngine: CoreSearchEngineInterface) {
            engineProviders[searchEngine]?.remove(dataProvider.dataProviderName)
            registeredProviders[dataProvider.dataProviderName]?.remove(searchEngine)
        }

        fun isProviderRegistered(dataProvider: IndexableDataProvider<*>, searchEngine: CoreSearchEngineInterface): Boolean {
            return engineProviders[searchEngine]?.contains(dataProvider.dataProviderName) == true
        }

        fun registerDataProviderContext(dataProvider: IndexableDataProvider<*>, context: DataProviderContext) {
            val oldContext = dataProvidersContextMap[dataProvider.dataProviderName]
            assertDebug(oldContext == null || context == oldContext) {
                "Registered data provider contexts are not the same"
            }
            dataProvidersContextMap[dataProvider.dataProviderName] = context
        }

        fun dataProviderContext(dataProvider: String): DataProviderContext? {
            return dataProvidersContextMap[dataProvider]
        }
    }
}
