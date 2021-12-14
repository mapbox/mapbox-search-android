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
 * Category search engine, used for search by category.
 *
 * To obtain [CategorySearchEngine] instance, please, use [MapboxSearchSdk.getCategorySearchEngine].
 */
public interface CategorySearchEngine {

    /**
     * Experimental API, can be changed or removed in the next SDK releases.
     * The type of the API used by the Search Engine.
     */
    public val apiType: ApiType

    /**
     * Performs a search request for places based on a category.
     *
     * @param categoryName name of category to search.
     * @param options category search options.
     * @param executor Executor used for events dispatching. By default events are dispatched on the main thread.
     * @param callback search callback to retrieve results.
     * @return [SearchRequestTask] object that represents request.
     */
    public fun search(
        categoryName: String,
        options: CategorySearchOptions,
        executor: Executor,
        callback: SearchCallback,
    ): SearchRequestTask

    /**
     * Performs a search request for places based on a category.
     *
     * @param categoryName name of category to search.
     * @param options category search options.
     * @param callback search callback to retrieve results. Events are dispatched on the main thread.
     * @return [SearchRequestTask] object that represents request.
     */
    public fun search(
        categoryName: String,
        options: CategorySearchOptions,
        callback: SearchCallback,
    ): SearchRequestTask = search(
        categoryName = categoryName,
        options = options,
        executor = SearchSdkMainThreadWorker.mainExecutor,
        callback = callback,
    )
}

internal class CategorySearchEngineImpl(
    override val apiType: ApiType,
    private val coreEngine: CoreSearchEngineInterface,
    private val httpErrorsCache: HttpErrorsCache,
    private val requestContextProvider: SearchRequestContextProvider,
    private val searchResultFactory: SearchResultFactory,
    private val engineExecutorService: ExecutorService,
) : BaseSearchEngine(), CategorySearchEngine {

    override fun search(
        categoryName: String,
        options: CategorySearchOptions,
        executor: Executor,
        callback: SearchCallback,
    ): SearchRequestTask {
        return makeRequest(callback, engineExecutorService) { request ->
            val requestContext = requestContextProvider.provide(apiType)
            coreEngine.search(
                "",
                listOf(categoryName),
                options.mapToCoreCategory(),
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
