package com.mapbox.search.details

import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.ApiType
import com.mapbox.search.MapboxSearchSdk
import com.mapbox.search.SearchCallback
import com.mapbox.search.SearchEngineFactory
import com.mapbox.search.SearchResultCallback
import com.mapbox.search.base.core.getUserActivityReporter
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.common.concurrent.SearchSdkMainThreadWorker
import java.util.concurrent.Executor

/**
 * The Details API provides access to POI metadata, boundary details, addresses and places.
 * For more information, visit [Details API page](https://docs.mapbox.com/api/search/details/).
 *
 * Instance of the [DetailsApi] can be obtained with [DetailsApi.create].
 */
@MapboxExperimental
public interface DetailsApi {

    /**
     * Request basic metadata for a POI, which includes attributes such as POI name, address,
     * coordinates, primary photo and category classification.
     *
     * To retrieve additional attributes beyond the basic data for a POI,
     * specify [RetrieveDetailsOptions.attributeSets] in the provided [options].
     *
     * @param mapboxId A unique identifier for the geographic feature.
     * @param options Retrieve options.
     * @param callback Search result callback. Events are dispatched on the main thread.
     * @return [AsyncOperationTask] object representing pending completion of the request
     */
    public fun retrieveDetails(
        mapboxId: String,
        options: RetrieveDetailsOptions,
        callback: SearchResultCallback,
    ): AsyncOperationTask = retrieveDetails(
        mapboxId = mapboxId,
        options = options,
        executor = SearchSdkMainThreadWorker.mainExecutor,
        callback = callback,
    )

    /**
     * Request basic metadata for a POI, which includes attributes such as POI name, address,
     * coordinates, primary photo and category classification.
     *
     * To retrieve additional attributes beyond the basic data for a POI,
     * specify [RetrieveDetailsOptions.attributeSets] in the provided [options].
     *
     * @param mapboxId A unique identifier for the geographic feature.
     * @param options Retrieve options.
     * @param executor [Executor] used for events dispatching, default is the main thread.
     * @param callback Search result callback.
     * @return [AsyncOperationTask] object representing pending completion of the request
     */
    public fun retrieveDetails(
        mapboxId: String,
        options: RetrieveDetailsOptions,
        executor: Executor,
        callback: SearchResultCallback,
    ): AsyncOperationTask

    /**
     * Request basic metadata for POIs, which includes attributes such as POI name, address,
     * coordinates, primary photo and category classification.
     *
     * To retrieve additional attributes beyond the basic data for a POI,
     * specify [RetrieveDetailsOptions.attributeSets] in the provided [options].
     *
     * @param mapboxIds Unique identifiers for the geographic features.
     * @param options Retrieve options.
     * @param callback Search result callback. Events are dispatched on the main thread.
     * @return [AsyncOperationTask] object representing pending completion of the request
     */
    public fun retrieveDetails(
        mapboxIds: List<String>,
        options: RetrieveDetailsOptions,
        callback: SearchCallback,
    ): AsyncOperationTask = retrieveDetails(
        mapboxIds = mapboxIds,
        options = options,
        executor = SearchSdkMainThreadWorker.mainExecutor,
        callback = callback,
    )

    /**
     * Request basic metadata for POIs, which includes attributes such as POI name, address,
     * coordinates, primary photo and category classification.
     *
     * To retrieve additional attributes beyond the basic data for a POI,
     * specify [RetrieveDetailsOptions.attributeSets] in the provided [options].
     *
     * @param mapboxIds Unique identifiers for the geographic features.
     * @param options Retrieve options.
     * @param executor [Executor] used for events dispatching, default is the main thread.
     * @param callback Search result callback.
     * @return [AsyncOperationTask] object representing pending completion of the request
     */
    public fun retrieveDetails(
        mapboxIds: List<String>,
        options: RetrieveDetailsOptions,
        executor: Executor,
        callback: SearchCallback,
    ): AsyncOperationTask

    /**
     * Companion object.
     */
    public companion object {

        /**
         * Creates a new instance of the [DetailsApi].
         *
         * @param settings [DetailsApiSettings] settings.
         * @return a new instance instance of the [DetailsApi].
         */
        @JvmStatic
        @JvmOverloads
        public fun create(settings: DetailsApiSettings = DetailsApiSettings()): DetailsApi {
            val coreEngine = SearchEngineFactory().createCoreEngineByApiType(
                apiType = ApiType.SEARCH_BOX,
                baseUrl = settings.baseUrl,
                locationProvider = settings.locationProvider,
                viewportProvider = settings.viewportProvider,
            )

            return DetailsApiImpl(
                coreEngine,
                getUserActivityReporter(),
                MapboxSearchSdk.searchRequestContextProvider,
                MapboxSearchSdk.searchResultFactory,
            )
        }
    }
}
