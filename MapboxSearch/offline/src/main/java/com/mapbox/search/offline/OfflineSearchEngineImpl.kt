package com.mapbox.search.offline

import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Feature
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.search.base.SearchRequestContextProvider
import com.mapbox.search.base.core.CoreApiType
import com.mapbox.search.base.core.CoreOfflineIndexObserver
import com.mapbox.search.base.core.CoreSearchEngineInterface
import com.mapbox.search.base.engine.BaseSearchEngine
import com.mapbox.search.base.engine.OneStepRequestCallbackWrapper
import com.mapbox.search.base.logger.logd
import com.mapbox.search.base.result.SearchResultFactory
import com.mapbox.search.base.task.AsyncOperationTaskImpl
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.internal.bindgen.OfflineIndexChangeEvent
import com.mapbox.search.internal.bindgen.OfflineIndexError
import com.mapbox.search.internal.bindgen.UserActivityReporterInterface
import com.mapbox.search.offline.OfflineSearchEngine.EngineReadyCallback
import com.mapbox.search.offline.OfflineSearchEngine.OnIndexChangeListener
import com.mapbox.turf.TurfMeasurement
import com.mapbox.turf.TurfMisc
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

internal class OfflineSearchEngineImpl(
    override val settings: OfflineSearchEngineSettings,
    private val coreEngine: CoreSearchEngineInterface,
    private val activityReporter: UserActivityReporterInterface,
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

    override fun search(
        query: String,
        options: OfflineSearchOptions,
        executor: Executor,
        callback: OfflineSearchCallback
    ): AsyncOperationTask {
        logd("search($query, $options) called")

        activityReporter.reportActivity("offline-search-engine-forward-geocoding")

        return makeRequest(OfflineSearchCallbackAdapter(callback)) { request ->
            coreEngine.searchOffline(
                query, emptyList(), options.mapToCore(),
                OneStepRequestCallbackWrapper(
                    searchResultFactory = searchResultFactory,
                    callbackExecutor = executor,
                    workerExecutor = engineExecutorService,
                    searchRequestTask = request,
                    searchRequestContext = requestContextProvider.provide(CoreApiType.SEARCH_BOX),
                    isOffline = true,
                )
            )
        }
    }

    override fun reverseGeocoding(
        options: OfflineReverseGeoOptions,
        executor: Executor,
        callback: OfflineSearchCallback
    ): AsyncOperationTask {
        activityReporter.reportActivity("offline-search-engine-reverse-geocoding")

        return makeRequest(OfflineSearchCallbackAdapter(callback)) { request ->
            coreEngine.reverseGeocodingOffline(
                options.mapToCore(),
                OneStepRequestCallbackWrapper(
                    searchResultFactory = searchResultFactory,
                    callbackExecutor = executor,
                    workerExecutor = engineExecutorService,
                    searchRequestTask = request,
                    searchRequestContext = requestContextProvider.provide(CoreApiType.SEARCH_BOX),
                    isOffline = true,
                )
            )
        }
    }

    override fun searchAddressesNearby(
        street: String,
        proximity: Point,
        radiusMeters: Double,
        executor: Executor,
        callback: OfflineSearchCallback
    ): AsyncOperationTask {
        activityReporter.reportActivity("offline-search-engine-search-nearby-street")
        if (radiusMeters <= 0.0) {
            executor.execute {
                callback.onError(IllegalArgumentException("Negative or zero radius: $radiusMeters"))
            }
            return AsyncOperationTaskImpl.COMPLETED
        }

        val lon = proximity.longitude()
        val lat = proximity.latitude()
        if (lon <= -180.0 || lon >= 180.0 ||
            lat <= -90.0 || lat >= 90.0) {
            executor.execute {
                callback.onError(IllegalArgumentException("Invalid proximity(lon=$lon,lat=$lat)"))
            }
            return AsyncOperationTaskImpl.COMPLETED
        }

        return makeRequest(OfflineSearchCallbackAdapter(callback)) { request ->
            coreEngine.getAddressesOffline(
                street,
                proximity,
                radiusMeters,
                OneStepRequestCallbackWrapper(
                    searchResultFactory = searchResultFactory,
                    callbackExecutor = executor,
                    workerExecutor = engineExecutorService,
                    searchRequestTask = request,
                    searchRequestContext = requestContextProvider.provide(CoreApiType.SEARCH_BOX),
                    isOffline = true,
                )
            )
        }
    }

    override fun searchAlongRoute(
        query: String,
        proximity: Point,
        route: List<Point>,
        executor: Executor,
        callback: OfflineSearchCallback
    ): AsyncOperationTask {
        val remainingRoute = TurfMisc.lineSlice(proximity, route.last(), LineString.fromLngLats(route))
        val coords = bufferBoundingBox(TurfMeasurement.bbox(remainingRoute))
        val boundingBox = BoundingBox.fromLngLats(coords[0], coords[1], coords[2], coords[3])

        val options = OfflineSearchOptions(
            proximity = proximity,
            boundingBox = boundingBox,
        )

        return this.search(
            query = query,
            options = options,
            executor = executor,
            callback = callback
        )
    }

    override fun retrieve(
        feature: Feature,
        executor: Executor,
        callback: OfflineSearchResultCallback
    ): AsyncOperationTask {
        val name = feature.getStringProperty("name")

        val searchOptions = when (val geometry = feature.geometry()) {
            is Point -> {
                OfflineSearchOptions(
                    origin = geometry,
                    proximity = geometry
                )
            }
            else -> {
                val coords = TurfMeasurement.bbox(geometry)
                OfflineSearchOptions(
                    origin = TurfMeasurement.center(feature).geometry() as Point,
                    boundingBox = BoundingBox.fromLngLats(coords[0], coords[1], coords[2], coords[3])
                )
            }
        }

        return makeRequest(OfflineSearchResultCallbackAdapter(feature, callback)) { request ->
            coreEngine.searchOffline(
                name, emptyList(), searchOptions.mapToCore(),
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

    private fun bufferBoundingBox(coords: DoubleArray, percentage: Double = 5.0): DoubleArray {
        var minLon = coords[0]
        var minLat = coords[1]
        var maxLon = coords[2]
        var maxLat = coords[3]

        val latHeight = maxLat - minLat
        val lonWidth = maxLon - minLon

        val latBuffer = latHeight * percentage / 100
        val lonBuffer = lonWidth * percentage / 100

        minLat -= latBuffer
        minLon -= lonBuffer
        maxLat += latBuffer
        maxLon += lonBuffer

        return doubleArrayOf(minLon, minLat, maxLon, maxLat)
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
