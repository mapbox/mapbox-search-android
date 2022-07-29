package com.mapbox.search

import com.mapbox.common.TilesetDescriptor
import com.mapbox.geojson.Point
import com.mapbox.search.OfflineSearchEngine.EngineReadyCallback
import com.mapbox.search.OfflineSearchEngine.OnIndexChangeListener
import com.mapbox.search.adapter.BaseSearchCallbackAdapter
import com.mapbox.search.analytics.AnalyticsService
import com.mapbox.search.base.SearchRequestContextProvider
import com.mapbox.search.base.core.CoreApiType
import com.mapbox.search.base.core.CoreOfflineIndexObserver
import com.mapbox.search.base.core.CoreSearchEngine
import com.mapbox.search.base.core.CoreSearchEngineInterface
import com.mapbox.search.base.engine.BaseSearchEngine
import com.mapbox.search.base.engine.OneStepRequestCallbackWrapper
import com.mapbox.search.base.logger.logd
import com.mapbox.search.base.result.SearchResultFactory
import com.mapbox.search.internal.bindgen.OfflineIndexChangeEvent
import com.mapbox.search.internal.bindgen.OfflineIndexError
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

internal class OfflineSearchEngineImpl(
    override val analyticsService: AnalyticsService,
    override val settings: OfflineSearchEngineSettings,
    private val coreEngine: CoreSearchEngineInterface,
    private val requestContextProvider: SearchRequestContextProvider,
    private val searchResultFactory: SearchResultFactory,
    private val engineExecutorService: ExecutorService = DEFAULT_EXECUTOR,
) : BaseSearchEngine(), OfflineSearchEngine {

    private val initializationLock = Any()
    private val engineReadyCallbacks = mutableMapOf<EngineReadyCallback, Executor>()

    private val onIndexChangeListenersLock = Any()
    private val onIndexChangeListeners = mutableMapOf<OnIndexChangeListener, CoreOfflineIndexObserver>()

    @Volatile
    private var isEngineReady: Boolean = true

    init {
        coreEngine.setTileStore(settings.tileStore) {
            synchronized(initializationLock) {
                isEngineReady = true
                engineReadyCallbacks.forEach { (callback, executor) ->
                    executor.execute {
                        callback.onEngineReady()
                    }
                }
                engineReadyCallbacks.clear()
            }
            logd("setTileStore done")
        }
    }

    override fun selectTileset(dataset: String?, version: String?) {
        coreEngine.selectTileset(dataset, version)
    }

    override fun createPlacesTilesetDescriptor(dataset: String, version: String): TilesetDescriptor {
        return CoreSearchEngine.createPlacesTilesetDescriptor(dataset, version)
    }

    override fun createTilesetDescriptor(dataset: String, version: String): TilesetDescriptor {
        return CoreSearchEngine.createTilesetDescriptor(dataset, version)
    }

    override fun search(
        query: String,
        options: OfflineSearchOptions,
        executor: Executor,
        callback: SearchCallback
    ): SearchRequestTask {
        logd("search($query, $options) called")

        val task = makeRequest(BaseSearchCallbackAdapter(callback)) { request ->
            coreEngine.searchOffline(
                query, emptyList(), options.mapToCore(),
                OneStepRequestCallbackWrapper(
                    searchResultFactory = searchResultFactory,
                    callbackExecutor = executor,
                    workerExecutor = engineExecutorService,
                    searchRequestTask = request,
                    searchRequestContext = requestContextProvider.provide(CoreApiType.SBS),
                    isOffline = true,
                )
            )
        }
        return SearchRequestTaskAsyncAdapter(task)
    }

    override fun reverseGeocoding(
        options: OfflineReverseGeoOptions,
        executor: Executor,
        callback: SearchCallback
    ): SearchRequestTask {
        val task = makeRequest(BaseSearchCallbackAdapter(callback)) { request ->
            coreEngine.reverseGeocodingOffline(
                options.mapToCore(),
                OneStepRequestCallbackWrapper(
                    searchResultFactory = searchResultFactory,
                    callbackExecutor = executor,
                    workerExecutor = engineExecutorService,
                    searchRequestTask = request,
                    searchRequestContext = requestContextProvider.provide(CoreApiType.SBS),
                    isOffline = true,
                )
            )
        }
        return SearchRequestTaskAsyncAdapter(task)
    }

    override fun searchAddressesNearby(
        street: String,
        proximity: Point,
        radiusMeters: Double,
        executor: Executor,
        callback: SearchCallback
    ): SearchRequestTask {
        if (radiusMeters < 0.0) {
            executor.execute {
                callback.onError(IllegalArgumentException("Negative radius"))
            }
            return SearchRequestTaskImpl.completed()
        }

        val task = makeRequest(BaseSearchCallbackAdapter(callback)) { request ->
            coreEngine.getAddressesOffline(
                street,
                proximity,
                radiusMeters,
                OneStepRequestCallbackWrapper(
                    searchResultFactory = searchResultFactory,
                    callbackExecutor = executor,
                    workerExecutor = engineExecutorService,
                    searchRequestTask = request,
                    searchRequestContext = requestContextProvider.provide(CoreApiType.SBS),
                    isOffline = true,
                )
            )
        }
        return SearchRequestTaskAsyncAdapter(task)
    }

    override fun addEngineReadyCallback(executor: Executor, callback: EngineReadyCallback) {
        synchronized(initializationLock) {
            val result = isEngineReady
            if (result) {
                executor.execute {
                    callback.onEngineReady()
                }
            } else {
                engineReadyCallbacks[callback] = executor
            }
        }
    }

    override fun removeEngineReadyCallback(callback: EngineReadyCallback) {
        synchronized(initializationLock) {
            engineReadyCallbacks.remove(callback)
        }
    }

    override fun addOnIndexChangeListener(executor: Executor, listener: OnIndexChangeListener) {
        synchronized(onIndexChangeListenersLock) {
            if (onIndexChangeListeners.contains(listener)) {
                return
            }
            val adapter = OnIndexChangeListenerAdapter(executor, listener)
            coreEngine.addOfflineIndexObserver(adapter)
            onIndexChangeListeners[listener] = adapter
        }
    }

    override fun removeOnIndexChangeListener(listener: OnIndexChangeListener) {
        synchronized(onIndexChangeListenersLock) {
            onIndexChangeListeners[listener]?.let { coreObserver ->
                coreEngine.removeOfflineIndexObserver(coreObserver)
            }
            onIndexChangeListeners.remove(listener)
        }
    }

    private class OnIndexChangeListenerAdapter(
        private val executor: Executor,
        private val listener: OnIndexChangeListener,
    ) : CoreOfflineIndexObserver {

        override fun onIndexChanged(e: OfflineIndexChangeEvent) {
            executor.execute {
                listener.onIndexChange(e.mapToPlatformType())
            }
        }

        override fun onError(e: OfflineIndexError) {
            executor.execute {
                listener.onError(e.mapToPlatformType())
            }
        }
    }

    private companion object {
        val DEFAULT_EXECUTOR: ExecutorService = Executors.newSingleThreadExecutor { runnable ->
            Thread(runnable, "OfflineSearchEngine executor")
        }
    }
}
