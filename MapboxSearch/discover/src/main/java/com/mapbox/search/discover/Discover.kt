package com.mapbox.search.discover

import android.Manifest
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.bindgen.Expected
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.search.base.BaseSearchSdkInitializer
import com.mapbox.search.base.core.getUserActivityReporter
import com.mapbox.search.base.location.defaultLocationEngine
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.common.CompletionCallback
import com.mapbox.search.common.concurrent.SearchSdkMainThreadWorker
import java.util.concurrent.Executor

/**
 * Main entrypoint to the Mapbox Discover SDK.
 */
public interface Discover {

    /**
     * Search for places nearby the specified geographic point.
     *
     * @param query Search query.
     * @param proximity Geographic point to search nearby.
     * @param options Search options.
     * @return Result of the search request, one of error or value.
     */
    public suspend fun search(
        query: DiscoverQuery,
        proximity: Point,
        options: DiscoverOptions = DiscoverOptions(),
    ): Expected<Exception, List<DiscoverResult>>

    /**
     * Search for places nearby the specified geographic point.
     *
     * @param query Search query.
     * @param proximity Geographic point to search nearby.
     * @param options Search options.
     * @param executor Executor used for events dispatching. By default events are dispatched on the main thread.
     * @param callback Callback to handle search response.
     * @return an object representing pending completion of the task.
     */
    public fun search(
        query: DiscoverQuery,
        proximity: Point,
        options: DiscoverOptions,
        executor: Executor,
        callback: CompletionCallback<List<DiscoverResult>>
    ): AsyncOperationTask

    /**
     * Search for places nearby the specified geographic point.
     *
     * @param query Search query.
     * @param proximity Geographic point to search nearby.
     * @param options Search options.
     * @param callback Callback to handle search response. Callback functions are called on the main thread.
     * @return an object representing pending completion of the task.
     */
    public fun search(
        query: DiscoverQuery,
        proximity: Point,
        options: DiscoverOptions,
        callback: CompletionCallback<List<DiscoverResult>>
    ): AsyncOperationTask {
        return search(query, proximity, options, SearchSdkMainThreadWorker.mainExecutor, callback)
    }

    /**
     * Search for places inside the specified bounding box.
     *
     * @param query Search query.
     * @param region Limit results to only those contained within the supplied bounding box.
     * @param proximity Optional geographic point to search nearby.
     * Bias the response to favor results that are closer to this location. If not specified the SDK will try to get
     * user location from the [LocationEngine] that was provided in the [Discover.create].
     * @param options Search options.
     * @return Result of the search request, one of error or value.
     */
    public suspend fun search(
        query: DiscoverQuery,
        region: BoundingBox,
        proximity: Point? = null,
        options: DiscoverOptions = DiscoverOptions(),
    ): Expected<Exception, List<DiscoverResult>>

    /**
     * Search for places inside the specified bounding box.
     *
     * @param query Search query.
     * @param region Limit results to only those contained within the supplied bounding box.
     * @param proximity Optional geographic point to search nearby.
     * Bias the response to favor results that are closer to this location. If not specified the SDK will try to get
     * user location from the [LocationEngine] that was provided in the [Discover.create].
     * @param options Search options.
     * @param executor Executor used for events dispatching. By default events are dispatched on the main thread.
     * @param callback Callback to handle search response.
     * @return an object representing pending completion of the task.
     */
    public fun search(
        query: DiscoverQuery,
        region: BoundingBox,
        proximity: Point? = null,
        options: DiscoverOptions,
        executor: Executor,
        callback: CompletionCallback<List<DiscoverResult>>
    ): AsyncOperationTask

    /**
     * Search for places inside the specified bounding box.
     *
     * @param query Search query.
     * @param region Limit results to only those contained within the supplied bounding box.
     * @param proximity Optional geographic point to search nearby.
     * Bias the response to favor results that are closer to this location. If not specified the SDK will try to get
     * user location from the [LocationEngine] that was provided in the [Discover.create].
     * @param options Search options.
     * @param callback Callback to handle search response. Callback functions are called on the main thread.
     * @return an object representing pending completion of the task.
     */
    public fun search(
        query: DiscoverQuery,
        region: BoundingBox,
        proximity: Point? = null,
        options: DiscoverOptions,
        callback: CompletionCallback<List<DiscoverResult>>
    ): AsyncOperationTask {
        return search(query, region, proximity, options, SearchSdkMainThreadWorker.mainExecutor, callback)
    }

    /**
     * Search for places along the road.
     *
     * @param query Search query.
     * @param route Route to search across. At least 2 points must be provided.
     * @param deviation Option describing maximum detour from route. Default deviation is 10 minutes.
     * @param options Search options.
     * @return Result of the search request, one of error or value.
     */
    public suspend fun search(
        query: DiscoverQuery,
        route: List<Point>,
        deviation: RouteDeviationOptions = RouteDeviationOptions.DEFAULT_DEVIATION,
        options: DiscoverOptions = DiscoverOptions(),
    ): Expected<Exception, List<DiscoverResult>>

    /**
     * Search for places along the road.
     *
     * @param query Search query.
     * @param route Route to search across. At least 2 points must be provided.
     * @param deviation Option describing maximum detour from route. Default deviation is 10 minutes.
     * @param options Search options.
     * @param executor Executor used for events dispatching. By default events are dispatched on the main thread.
     * @param callback Callback to handle search response.
     * @return an object representing pending completion of the task.
     */
    public fun search(
        query: DiscoverQuery,
        route: List<Point>,
        deviation: RouteDeviationOptions,
        options: DiscoverOptions,
        executor: Executor,
        callback: CompletionCallback<List<DiscoverResult>>
    ): AsyncOperationTask

    /**
     * Search for places along the road.
     *
     * @param query Search query.
     * @param route Route to search across. At least 2 points must be provided.
     * @param deviation Option describing maximum detour from route. Default deviation is 10 minutes.
     * @param options Search options.
     * @param callback Callback to handle search response. Callback functions are called on the main thread.
     * @return an object representing pending completion of the task.
     */
    public fun search(
        query: DiscoverQuery,
        route: List<Point>,
        deviation: RouteDeviationOptions,
        options: DiscoverOptions,
        callback: CompletionCallback<List<DiscoverResult>>
    ): AsyncOperationTask {
        return search(query, route, deviation, options, SearchSdkMainThreadWorker.mainExecutor, callback)
    }

    /**
     * Companion object.
     */
    public companion object {

        /**
         * Creates a new instance of the [Discover].
         *
         * @param accessToken [Mapbox Access Token](https://docs.mapbox.com/help/glossary/access-token/).
         *
         * @param locationEngine The mechanism responsible for providing location approximations to the SDK.
         * By default [LocationEngine] is retrieved from [LocationEngineProvider.getBestLocationEngine].
         * Note that this class requires [Manifest.permission.ACCESS_COARSE_LOCATION] or
         * [Manifest.permission.ACCESS_FINE_LOCATION] to work properly.
         *
         * @return a new instance instance of [Discover].
         */
        @JvmStatic
        @JvmOverloads
        public fun create(
            accessToken: String,
            locationEngine: LocationEngine = defaultLocationEngine(),
        ): Discover {
            val engine = DiscoverSearchEngine.create(
                accessToken,
                BaseSearchSdkInitializer.app,
                locationEngine
            )

            return DiscoverImpl(
                engine = engine,
                activityReporter = getUserActivityReporter(accessToken),
            )
        }
    }
}
