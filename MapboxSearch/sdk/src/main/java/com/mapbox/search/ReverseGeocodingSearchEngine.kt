package com.mapbox.search

import com.mapbox.search.core.CoreSearchEngineInterface
import com.mapbox.search.core.http.HttpErrorsCache
import com.mapbox.search.engine.BaseSearchEngine
import com.mapbox.search.engine.OneStepRequestCallbackWrapper
import com.mapbox.search.result.SearchResultFactory
import com.mapbox.search.utils.concurrent.SearchSdkMainThreadWorker
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService

/**
 * Used for [reverse geocoding](https://docs.mapbox.com/android/search/overview/#reverse-geocoding) (looking up place by geographic coordinates to retrieve its name and address).
 *
 * To obtain [ReverseGeocodingSearchEngine] instance, please, use [MapboxSearchSdk.getReverseGeocodingSearchEngine].
 */
public interface ReverseGeocodingSearchEngine {

    /**
     * Experimental API, can be changed or removed in the next SDK releases.
     * The type of the API used by the Search Engine.
     */
    public val apiType: ApiType

    /**
     * Performs reverse geocoding.
     * @param options - reverse geocoding options.
     * @param executor Executor used for events dispatching. By default events are dispatched on the main thread.
     * @param callback - search result callback.
     * @return [SearchRequestTask] to be able to cancel request.
     */
    public fun search(
        options: ReverseGeoOptions,
        executor: Executor,
        callback: SearchCallback,
    ): SearchRequestTask

    /**
     * Performs reverse geocoding.
     * @param options - reverse geocoding options.
     * @param callback - search result callback, delivers results on the main thread.
     * @return [SearchRequestTask] to be able to cancel request.
     */
    public fun search(
        options: ReverseGeoOptions,
        callback: SearchCallback,
    ): SearchRequestTask = search(
        options = options,
        executor = SearchSdkMainThreadWorker.mainExecutor,
        callback = callback,
    )
}

internal class ReverseGeocodingSearchEngineImpl(
    override val apiType: ApiType,
    private val coreEngine: CoreSearchEngineInterface,
    private val httpErrorsCache: HttpErrorsCache,
    private val requestContextProvider: SearchRequestContextProvider,
    private val searchResultFactory: SearchResultFactory,
    private val engineExecutorService: ExecutorService,
) : BaseSearchEngine(), ReverseGeocodingSearchEngine {

    override fun search(
        options: ReverseGeoOptions,
        executor: Executor,
        callback: SearchCallback
    ): SearchRequestTask {
        return makeRequest(callback, engineExecutorService) { request ->
            val requestContext = requestContextProvider.provide(apiType)
            coreEngine.reverseGeocoding(
                options.mapToCore(),
                OneStepRequestCallbackWrapper(
                    httpErrorsCache = httpErrorsCache,
                    searchResultFactory = searchResultFactory,
                    callbackExecutor = executor,
                    workerExecutor = engineExecutorService,
                    searchRequestTask = request,
                    searchRequestContext = requestContext,
                    isOffline = false,
                )
            )
        }
    }
}
