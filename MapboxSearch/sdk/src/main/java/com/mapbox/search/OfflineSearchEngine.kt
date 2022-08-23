package com.mapbox.search

import com.mapbox.common.TilesetDescriptor
import com.mapbox.geojson.Point
import com.mapbox.search.analytics.AnalyticsService
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.common.concurrent.SearchSdkMainThreadWorker
import java.util.concurrent.Executor

/**
 * The [OfflineSearchEngine] interface provides forward and reverse geocoding search that works offline.
 * An instance of the [OfflineSearchEngine] can be obtained with [MapboxSearchSdk.createOfflineSearchEngine].
 *
 * Offline forward geocoding search works in a two-step manner, see [SearchEngine] for more details.
 *
 * The API of this class is temporary and subject to change.
 * Tiles loading functionality is available to selected customers only. Contact our team, to get early preview.
 */
public interface OfflineSearchEngine {

    /**
     * Interface definition for a callback to be invoked when the [OfflineSearchEngine] is ready for use.
     * With the current implementation the callback is invoked when previously downloaded offline data has been prepared
     * and available for search.
     *
     * If the callback returns an error, you can continue to use [OfflineSearchEngine] functions,
     * but previously downloaded offline data won't be available for search.
     */
    public interface EngineReadyCallback {

        /**
         * Called when the engine is ready for use.
         */
        public fun onEngineReady()

        // TODO this function is never called
        /**
         * Called if an error occurred.
         * @param e Exception, occurred during the request.
         */
        public fun onError(e: Exception)
    }

    /**
     * Interface for a listener to be invoked when index data is changed in the [OfflineSearchEngine].
     * This interface notifies users when tiles changes made with [com.mapbox.common.TileStore]
     * have been processed and visible in the [OfflineSearchEngine].
     */
    public interface OnIndexChangeListener {

        /**
         * Called when tiles changes made with [com.mapbox.common.TileStore]
         * have been processed and visible in the [OfflineSearchEngine].
         *
         * @param event Information about changes in the offline search index.
         */
        public fun onIndexChange(event: OfflineIndexChangeEvent)

        /**
         * Called when tiles changes made with [com.mapbox.common.TileStore] couldn't be processed in the [OfflineSearchEngine].
         *
         * @param event Information about error.
         */
        public fun onError(event: OfflineIndexErrorEvent)
    }

    /**
     * Experimental API, can be changed or removed in the next SDK releases.
     * Settings used for [OfflineSearchEngine] initialization.
     */
    public val settings: OfflineSearchEngineSettings

    /**
     * [AnalyticsService] instance associated with this [OfflineSearchEngine].
     */
    public val analyticsService: AnalyticsService

    /**
     * Selects preferable tileset for offline search. If dataset or version is set, [OfflineSearchEngine] will try to
     * match appropriate tileset and use it. If several tilesets are available, the latest registered will be used.
     *
     * By default, if multiple tilesets are registered at once
     * (e.g. one offline region is loaded for several tilesets in single call),
     * the biggest one (tilesets are compared as pair of strings {dataset, version}) is considered as latest.
     *
     * @param dataset Preferable dataset.
     * @param version Preferable version.
     */
    public fun selectTileset(dataset: String?, version: String?)

    /**
     * Creates TilesetDescriptor for offline search index data using the specified dataset and version.
     * Downloaded data will include addresses and places.
     *
     * @param dataset Tiles dataset.
     * @param version Tiles version, chosen automatically if empty.
     */
    public fun createTilesetDescriptor(dataset: String, version: String): TilesetDescriptor

    /**
     * Creates TilesetDescriptor for offline search index data using default dataset and version.
     * Downloaded data will include addresses and places.
     */
    public fun createTilesetDescriptor(): TilesetDescriptor = createTilesetDescriptor(
        dataset = OfflineSearchEngineSettings.DEFAULT_DATASET,
        version = OfflineSearchEngineSettings.DEFAULT_VERSION,
    )

    /**
     * Creates TilesetDescriptor for offline search using the specified dataset and version.
     * Downloaded data will include only places.
     *
     * @param dataset Tiles dataset.
     * @param version Tiles version, chosen automatically if empty.
     */
    public fun createPlacesTilesetDescriptor(dataset: String, version: String): TilesetDescriptor

    /**
     * Creates TilesetDescriptor for offline search using the specified dataset and version.
     * Downloaded data will include only places.
     */
    public fun createPlacesTilesetDescriptor(): TilesetDescriptor = createPlacesTilesetDescriptor(
        dataset = OfflineSearchEngineSettings.DEFAULT_DATASET,
        version = OfflineSearchEngineSettings.DEFAULT_VERSION,
    )

    /**
     * Performs forward geocoding search request.
     * Each new search request cancels the previous one if it is still in progress.
     * In this case [SearchCallback.onError] will be called with [com.mapbox.search.common.SearchCancellationException].
     *
     * @param query Search query.
     * @param options Search options.
     * @param executor Executor used for events dispatching. By default events are dispatched on the main thread.
     * @param callback The callback to handle search result.
     * @return [AsyncOperationTask] object which allows to cancel the request.
     *
     * @see [SearchEngine.search].
     */
    public fun search(
        query: String,
        options: OfflineSearchOptions,
        executor: Executor,
        callback: SearchCallback,
    ): AsyncOperationTask

    /**
     * Performs forward geocoding search request.
     * Each new search request cancels the previous one if it is still in progress.
     * In this case [SearchCallback.onError] will be called with [com.mapbox.search.common.SearchCancellationException].
     *
     * @param query Search query.
     * @param options Search options.
     * @param callback The callback to handle search result on the main thread.
     * @return [AsyncOperationTask] object which allows to cancel the request.
     *
     * @see [SearchEngine.search].
     */
    public fun search(
        query: String,
        options: OfflineSearchOptions,
        callback: SearchCallback,
    ): AsyncOperationTask = search(
        query = query,
        options = options,
        executor = SearchSdkMainThreadWorker.mainExecutor,
        callback = callback,
    )

    /**
     * Performs reverse geocoding search request.
     * Each new search request cancels the previous one if it is still in progress.
     * In this case [SearchCallback.onError] will be called with [com.mapbox.search.common.SearchCancellationException].
     *
     * @param options Reverse geocoding options.
     * @param executor Executor used for events dispatching. By default events are dispatched on the main thread.
     * @param callback Search result callback.
     * @return [AsyncOperationTask] object which allows to cancel the request.
     */
    public fun reverseGeocoding(
        options: OfflineReverseGeoOptions,
        executor: Executor,
        callback: SearchCallback,
    ): AsyncOperationTask

    /**
     * Performs reverse geocoding search request.
     * Each new search request cancels the previous one if it is still in progress.
     * In this case [SearchCallback.onError] will be called with [com.mapbox.search.common.SearchCancellationException].
     *
     * @param options Reverse geocoding options.
     * @param callback Search result callback, delivers results on the main thread.
     * @return [AsyncOperationTask] object which allows to cancel the request.
     */
    public fun reverseGeocoding(
        options: OfflineReverseGeoOptions,
        callback: SearchCallback,
    ): AsyncOperationTask = reverseGeocoding(
        options = options,
        executor = SearchSdkMainThreadWorker.mainExecutor,
        callback = callback,
    )

    /**
     * Searches for addresses nearby (around [proximity] point), matched with specified [street] name.
     * Each new search request cancels the previous one if it is still in progress.
     * In this case [SearchCallback.onError] will be called with [com.mapbox.search.common.SearchCancellationException].
     *
     * @param street Street name to match.
     * @param proximity Coordinate to search in its vicinity.
     * @param radiusMeters Radius (in meters) around [proximity].
     * @param executor Executor used for events dispatching. By default events are dispatched on the main thread.
     * @param callback Search result callback.
     * @return [AsyncOperationTask] object which allows to cancel the request.
     */
    public fun searchAddressesNearby(
        street: String,
        proximity: Point,
        radiusMeters: Double,
        executor: Executor,
        callback: SearchCallback,
    ): AsyncOperationTask

    /**
     * Searches for addresses nearby (around [proximity] point), matched with specified [street] name.
     * Each new search request cancels the previous one if it is still in progress.
     * In this case [SearchCallback.onError] will be called with [com.mapbox.search.common.SearchCancellationException].
     *
     * @param street Street name to match.
     * @param proximity Coordinate to search in its vicinity.
     * @param radiusMeters Radius (in meters) around [proximity].
     * @param callback Search result callback, delivers results on the main thread.
     * @return [AsyncOperationTask] object which allows to cancel the request.
     */
    public fun searchAddressesNearby(
        street: String,
        proximity: Point,
        radiusMeters: Double,
        callback: SearchCallback,
    ): AsyncOperationTask = searchAddressesNearby(
        street = street,
        proximity = proximity,
        radiusMeters = radiusMeters,
        executor = SearchSdkMainThreadWorker.mainExecutor,
        callback = callback,
    )

    /**
     * Adds a callback to be notified when engine is ready.
     * If the engine is already ready, callback will be notified immediately.
     *
     * @param executor Executor used for events dispatching. By default events are dispatched on the main thread.
     * @param callback The callback to notify when engine is ready.
     */
    public fun addEngineReadyCallback(executor: Executor, callback: EngineReadyCallback)

    /**
     * Adds a callback to be notified when engine is ready.
     * If the engine is already ready, callback will be notified immediately.
     *
     * @param callback The callback to notify when engine is ready. Events are dispatched on the main thread.
     */
    public fun addEngineReadyCallback(callback: EngineReadyCallback): Unit = addEngineReadyCallback(
        executor = SearchSdkMainThreadWorker.mainExecutor,
        callback = callback,
    )

    /**
     * Removes a previously added callback.
     *
     * @param callback The callback to remove.
     */
    public fun removeEngineReadyCallback(callback: EngineReadyCallback)

    /**
     * Adds a listener to be notified of index change events.
     *
     * @param executor Executor used for events dispatching. By default events are dispatched on the main thread.
     * @param listener The listener to notify when an event happens.
     */
    public fun addOnIndexChangeListener(executor: Executor, listener: OnIndexChangeListener)

    /**
     * Adds a listener to be notified of index change events.
     *
     * @param listener The listener to notify when an event happens. Events are dispatched on the main thread.
     */
    public fun addOnIndexChangeListener(listener: OnIndexChangeListener): Unit = addOnIndexChangeListener(
        executor = SearchSdkMainThreadWorker.mainExecutor,
        listener = listener,
    )

    /**
     * Removes a previously added listener.
     *
     * @param listener The listener to remove.
     */
    public fun removeOnIndexChangeListener(listener: OnIndexChangeListener)
}
