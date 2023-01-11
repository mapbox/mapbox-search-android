package com.mapbox.search.discover

import android.Manifest
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.bindgen.Expected
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.search.base.BaseSearchSdkInitializer
import com.mapbox.search.base.location.defaultLocationEngine

/**
 * Main entrypoint to the Mapbox Discover API.
 */
public interface DiscoverApi {

    /**
     * Search for places nearby the specified geographic point.
     *
     * @param query Search query.
     * @param proximity Geographic point to search nearby.
     * @param options Search options.
     * @return Result of the search request, one of error or value.
     */
    public suspend fun search(
        query: DiscoverApiQuery,
        proximity: Point,
        options: DiscoverApiOptions = DiscoverApiOptions(),
    ): Expected<Exception, List<DiscoverApiResult>>

    /**
     * Search for places inside the specified bounding box.
     *
     * @param query Search query.
     * @param region Limit results to only those contained within the supplied bounding box.
     * @param proximity Optional geographic point to search nearby.
     * Bias the response to favor results that are closer to this location. If not specified the SDK will try to get
     * user location from the [LocationEngine] that was provided in the [DiscoverApi.create].
     * @param options Search options.
     * @return Result of the search request, one of error or value.
     */
    public suspend fun search(
        query: DiscoverApiQuery,
        region: BoundingBox,
        proximity: Point? = null,
        options: DiscoverApiOptions = DiscoverApiOptions(),
    ): Expected<Exception, List<DiscoverApiResult>>

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
        query: DiscoverApiQuery,
        route: List<Point>,
        deviation: RouteDeviationOptions = RouteDeviationOptions.DEFAULT_DEVIATION,
        options: DiscoverApiOptions = DiscoverApiOptions(),
    ): Expected<Exception, List<DiscoverApiResult>>

    /**
     * @suppress
     */
    public companion object {

        /**
         * Creates a new instance of the [DiscoverApi].
         *
         * @param accessToken [Mapbox Access Token](https://docs.mapbox.com/help/glossary/access-token/).
         *
         * @param locationEngine The mechanism responsible for providing location approximations to the SDK.
         * By default [LocationEngine] is retrieved from [LocationEngineProvider.getBestLocationEngine].
         * Note that this class requires [Manifest.permission.ACCESS_COARSE_LOCATION] or
         * [Manifest.permission.ACCESS_FINE_LOCATION] to work properly.
         *
         * @return a new instance instance of [DiscoverApi].
         */
        @JvmStatic
        @JvmOverloads
        public fun create(
            accessToken: String,
            locationEngine: LocationEngine = defaultLocationEngine(),
        ): DiscoverApi {
            val engine = DiscoverApiSearchEngine.create(
                accessToken,
                BaseSearchSdkInitializer.app,
                locationEngine
            )
            return DiscoverApiImpl(engine)
        }
    }
}
