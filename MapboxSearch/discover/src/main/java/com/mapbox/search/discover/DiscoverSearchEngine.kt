package com.mapbox.search.discover

import android.app.Application
import com.mapbox.bindgen.Expected
import com.mapbox.bindgen.ExpectedFactory.createError
import com.mapbox.bindgen.ExpectedFactory.createValue
import com.mapbox.common.location.LocationProvider
import com.mapbox.search.base.BaseResponseInfo
import com.mapbox.search.base.BaseSearchCallback
import com.mapbox.search.base.BaseSearchSdkInitializer
import com.mapbox.search.base.SearchRequestContextProvider
import com.mapbox.search.base.core.CoreEngineOptions
import com.mapbox.search.base.core.CoreSearchEngine
import com.mapbox.search.base.core.CoreSearchEngineInterface
import com.mapbox.search.base.core.CoreSearchOptions
import com.mapbox.search.base.engine.BaseSearchEngine
import com.mapbox.search.base.engine.OneStepRequestCallbackWrapper
import com.mapbox.search.base.location.LocationEngineAdapter
import com.mapbox.search.base.location.WrapperLocationProvider
import com.mapbox.search.base.record.IndexableRecordResolver
import com.mapbox.search.base.result.BaseSearchResult
import com.mapbox.search.base.result.SearchResultFactory
import com.mapbox.search.base.utils.AndroidKeyboardLocaleProvider
import com.mapbox.search.base.utils.orientation.AndroidScreenOrientationProvider
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.common.concurrent.SearchSdkMainThreadWorker
import com.mapbox.search.internal.bindgen.ApiType
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

internal class DiscoverSearchEngine(
    private val coreEngine: CoreSearchEngineInterface,
    private val requestContextProvider: SearchRequestContextProvider,
    private val searchResultFactory: SearchResultFactory,
    private val callbackExecutor: Executor = SearchSdkMainThreadWorker.mainExecutor,
    private val engineExecutorService: ExecutorService = DEFAULT_EXECUTOR
) : BaseSearchEngine() {

    fun search(
        categoryName: String,
        options: CoreSearchOptions,
        executor: Executor,
        callback: BaseSearchCallback,
    ): AsyncOperationTask {
        return makeRequest(callback) { task ->
            val requestContext = requestContextProvider.provide(API_TYPE)
            val requestId = coreEngine.search(
                "", listOf(categoryName), options, OneStepRequestCallbackWrapper(
                    searchResultFactory = searchResultFactory,
                    callbackExecutor = executor,
                    workerExecutor = engineExecutorService,
                    searchRequestTask = task,
                    searchRequestContext = requestContext,
                    isOffline = false,
                )
            )
            task.addOnCancelledCallback {
                coreEngine.cancel(requestId)
            }
        }
    }

    suspend fun search(
        categoryName: String,
        options: CoreSearchOptions
    ): Expected<Exception, Pair<List<BaseSearchResult>, BaseResponseInfo>> {
        return suspendCancellableCoroutine { continuation ->
            val task = search(
                categoryName = categoryName,
                options = options,
                executor = callbackExecutor,
                callback = object : BaseSearchCallback {
                    override fun onResults(
                        results: List<BaseSearchResult>,
                        responseInfo: BaseResponseInfo
                    ) {
                        continuation.resumeWith(Result.success(createValue(results to responseInfo)))
                    }

                    override fun onError(e: Exception) {
                        continuation.resumeWith(Result.success(createError(e)))
                    }
                }
            )

            continuation.invokeOnCancellation {
                task.cancel()
            }
        }
    }

    companion object {

        private val API_TYPE = ApiType.SBS

        private val DEFAULT_EXECUTOR = Executors.newSingleThreadExecutor { runnable ->
            Thread(runnable, "DiscoverApi executor")
        }

        fun create(
            app: Application,
            locationProvider: LocationProvider?,
        ): DiscoverSearchEngine {
            val coreEngine = CoreSearchEngine(
                CoreEngineOptions(
                    baseUrl = null,
                    apiType = API_TYPE,
                    sdkInformation = BaseSearchSdkInitializer.sdkInformation,
                    eventsUrl = null,
                ),
                WrapperLocationProvider(
                    LocationEngineAdapter(app, locationProvider), null
                ),
            )

            val requestContextProvider = SearchRequestContextProvider(
                AndroidKeyboardLocaleProvider(app), AndroidScreenOrientationProvider(app)
            )

            val searchResultFactory = SearchResultFactory(IndexableRecordResolver.EMPTY)

            return DiscoverSearchEngine(
                coreEngine = coreEngine,
                requestContextProvider = requestContextProvider,
                searchResultFactory = searchResultFactory,
            )
        }
    }
}
