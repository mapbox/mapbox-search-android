package com.mapbox.search.category

import android.Manifest
import com.mapbox.bindgen.Expected
import com.mapbox.common.location.LocationProvider
import com.mapbox.common.location.LocationServiceFactory
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.search.base.BaseSearchSdkInitializer
import com.mapbox.search.base.core.getUserActivityReporter
import com.mapbox.search.base.location.defaultLocationProvider
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.common.CompletionCallback
import com.mapbox.search.common.concurrent.SearchSdkMainThreadWorker
import java.util.concurrent.Executor

/**
 * Main entrypoint to the Mapbox Category SDK.
 */
public interface Category {

    /**
     * Search for places nearby the specified geographic point.
     *
     * @param query Search query.
     * @param proximity Geographic point to search nearby.
     * @param options Search options.
     * @return Result of the search request, one of error or value.
     */
    public suspend fun search(
        query: CategoryQuery,
        proximity: Point,
        options: CategoryOptions = CategoryOptions(),
    ): Expected<Exception, List<CategoryResult>>

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
        query: CategoryQuery,
        proximity: Point,
        options: CategoryOptions,
        executor: Executor,
        callback: CompletionCallback<List<CategoryResult>>
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
        query: CategoryQuery,
        proximity: Point,
        options: CategoryOptions,
        callback: CompletionCallback<List<CategoryResult>>
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
     * user location from the [LocationProvider] that was provided in the [Category.create].
     * @param options Search options.
     * @return Result of the search request, one of error or value.
     */
    public suspend fun search(
        query: CategoryQuery,
        region: BoundingBox,
        proximity: Point? = null,
        options: CategoryOptions = CategoryOptions(),
    ): Expected<Exception, List<CategoryResult>>

    /**
     * Search for places inside the specified bounding box.
     *
     * @param query Search query.
     * @param region Limit results to only those contained within the supplied bounding box.
     * @param proximity Optional geographic point to search nearby.
     * Bias the response to favor results that are closer to this location. If not specified the SDK will try to get
     * user location from the [LocationProvider] that was provided in the [Category.create].
     * @param options Search options.
     * @param executor Executor used for events dispatching. By default events are dispatched on the main thread.
     * @param callback Callback to handle search response.
     * @return an object representing pending completion of the task.
     */
    public fun search(
        query: CategoryQuery,
        region: BoundingBox,
        proximity: Point? = null,
        options: CategoryOptions,
        executor: Executor,
        callback: CompletionCallback<List<CategoryResult>>
    ): AsyncOperationTask

    /**
     * Search for places inside the specified bounding box.
     *
     * @param query Search query.
     * @param region Limit results to only those contained within the supplied bounding box.
     * @param proximity Optional geographic point to search nearby.
     * Bias the response to favor results that are closer to this location. If not specified the SDK will try to get
     * user location from the [LocationProvider] that was provided in the [Category.create].
     * @param options Search options.
     * @param callback Callback to handle search response. Callback functions are called on the main thread.
     * @return an object representing pending completion of the task.
     */
    public fun search(
        query: CategoryQuery,
        region: BoundingBox,
        proximity: Point? = null,
        options: CategoryOptions,
        callback: CompletionCallback<List<CategoryResult>>
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
        query: CategoryQuery,
        route: List<Point>,
        deviation: RouteDeviationOptions = RouteDeviationOptions.DEFAULT_DEVIATION,
        options: CategoryOptions = CategoryOptions(),
    ): Expected<Exception, List<CategoryResult>>

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
        query: CategoryQuery,
        route: List<Point>,
        deviation: RouteDeviationOptions,
        options: CategoryOptions,
        executor: Executor,
        callback: CompletionCallback<List<CategoryResult>>
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
        query: CategoryQuery,
        route: List<Point>,
        deviation: RouteDeviationOptions,
        options: CategoryOptions,
        callback: CompletionCallback<List<CategoryResult>>
    ): AsyncOperationTask {
        return search(query, route, deviation, options, SearchSdkMainThreadWorker.mainExecutor, callback)
    }

    /**
     * Companion object.
     */
    public companion object {

        /**
         * Creates a new instance of the [Category].
         *
         * @param locationProvider The mechanism responsible for providing location approximations to the SDK.
         * By default [LocationProvider] is provided by [LocationServiceFactory].
         * Note that this class requires [Manifest.permission.ACCESS_COARSE_LOCATION] or
         * [Manifest.permission.ACCESS_FINE_LOCATION] to work properly.
         *
         * @return a new instance instance of [Category].
         */
        @JvmStatic
        @JvmOverloads
        public fun create(
            locationProvider: LocationProvider? = defaultLocationProvider(),
        ): Category {
            val engine = CategorySearchEngine.create(
                BaseSearchSdkInitializer.app,
                locationProvider
            )

            return CategoryImpl(
                engine = engine,
                activityReporter = getUserActivityReporter(),
            )
        }
    }
}
