package com.mapbox.search.autofill

import android.app.Application
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.search.base.BaseResponseInfo
import com.mapbox.search.base.BaseSearchCallback
import com.mapbox.search.base.BaseSearchMultipleSelectionCallback
import com.mapbox.search.base.BaseSearchSelectionCallback
import com.mapbox.search.base.BaseSearchSuggestionsCallback
import com.mapbox.search.base.SearchRequestContextProvider
import com.mapbox.search.base.assertDebug
import com.mapbox.search.base.core.CoreApiType
import com.mapbox.search.base.core.CoreEngineOptions
import com.mapbox.search.base.core.CoreReverseGeoOptions
import com.mapbox.search.base.core.CoreSearchEngine
import com.mapbox.search.base.core.CoreSearchEngineInterface
import com.mapbox.search.base.core.CoreSearchOptions
import com.mapbox.search.base.engine.BaseSearchEngine
import com.mapbox.search.base.engine.OneStepRequestCallbackWrapper
import com.mapbox.search.base.engine.TwoStepsBatchRequestCallbackWrapper
import com.mapbox.search.base.engine.TwoStepsRequestCallbackWrapper
import com.mapbox.search.base.failDebug
import com.mapbox.search.base.location.LocationEngineAdapter
import com.mapbox.search.base.location.WrapperLocationProvider
import com.mapbox.search.base.logger.logd
import com.mapbox.search.base.record.IndexableRecordResolver
import com.mapbox.search.base.record.SearchHistoryService
import com.mapbox.search.base.result.BaseGeocodingCompatSearchSuggestion
import com.mapbox.search.base.result.BaseIndexableRecordSearchSuggestion
import com.mapbox.search.base.result.BaseSearchResult
import com.mapbox.search.base.result.BaseSearchSuggestion
import com.mapbox.search.base.result.BaseServerSearchSuggestion
import com.mapbox.search.base.result.SearchResultFactory
import com.mapbox.search.base.result.mapToCore
import com.mapbox.search.base.task.AsyncOperationTaskImpl
import com.mapbox.search.base.utils.AndroidKeyboardLocaleProvider
import com.mapbox.search.base.utils.UserAgentProvider
import com.mapbox.search.base.utils.orientation.AndroidScreenOrientationProvider
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.common.concurrent.SearchSdkMainThreadWorker
import com.mapbox.search.internal.bindgen.ApiType
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

internal class AutofillSearchEngine(
    private val coreEngine: CoreSearchEngineInterface,
    private val historyService: SearchHistoryService,
    private val requestContextProvider: SearchRequestContextProvider,
    private val searchResultFactory: SearchResultFactory,
    private val engineExecutorService: ExecutorService = DEFAULT_EXECUTOR
) : BaseSearchEngine() {

    fun search(
        query: String,
        options: CoreSearchOptions,
        executor: Executor,
        callback: BaseSearchSuggestionsCallback,
    ): AsyncOperationTask {
        return makeRequest(callback) { task ->
            val requestContext = requestContextProvider.provide(ApiType.AUTOFILL)
            val requestId = coreEngine.search(
                query, emptyList(), options,
                TwoStepsRequestCallbackWrapper(
                    apiType = ApiType.AUTOFILL,
                    coreEngine = coreEngine,
                    historyService = historyService,
                    searchResultFactory = searchResultFactory,
                    callbackExecutor = executor,
                    workerExecutor = engineExecutorService,
                    searchRequestTask = task,
                    searchRequestContext = requestContext,
                    suggestion = null,
                    addResultToHistory = false,
                )
            )
            task.addOnCancelledCallback {
                coreEngine.cancel(requestId)
            }
        }
    }

    suspend fun search(query: String, options: CoreSearchOptions): SearchSuggestionsResponse {
        return suspendCancellableCoroutine { continuation ->
            val task = search(query, options, SearchSdkMainThreadWorker.mainExecutor, object : BaseSearchSuggestionsCallback {
                override fun onSuggestions(suggestions: List<BaseSearchSuggestion>, responseInfo: BaseResponseInfo) {
                    continuation.resumeWith(
                        Result.success(SearchSuggestionsResponse.Suggestions(suggestions, responseInfo))
                    )
                }

                override fun onError(e: Exception) {
                    continuation.resumeWith(Result.success(SearchSuggestionsResponse.Error(e)))
                }
            })

            continuation.invokeOnCancellation {
                task.cancel()
            }
        }
    }

    fun select(
        suggestion: BaseSearchSuggestion,
        executor: Executor,
        callback: BaseSearchSelectionCallback,
    ): AsyncOperationTask {
        return when (suggestion) {
            is BaseServerSearchSuggestion -> {
                makeRequest(callback as BaseSearchSuggestionsCallback) { task ->
                    val requestContext = suggestion.requestOptions.requestContext
                    val requestId = coreEngine.retrieve(
                        suggestion.requestOptions.core,
                        suggestion.rawSearchResult.mapToCore(),
                        TwoStepsRequestCallbackWrapper(
                            apiType = ApiType.AUTOFILL,
                            coreEngine = coreEngine,
                            historyService = historyService,
                            searchResultFactory = searchResultFactory,
                            callbackExecutor = executor,
                            workerExecutor = engineExecutorService,
                            searchRequestTask = task,
                            searchRequestContext = requestContext,
                            suggestion = suggestion,
                            addResultToHistory = false,
                        )
                    )
                    task.addOnCancelledCallback {
                        coreEngine.cancel(requestId)
                    }
                }
            }
            is BaseGeocodingCompatSearchSuggestion,
            is BaseIndexableRecordSearchSuggestion -> {
                val errorMsg = "Unsupported in Autofill suggestion type: $suggestion"
                failDebug { errorMsg }
                executor.execute {
                    callback.onError(Exception(errorMsg))
                }
                AsyncOperationTaskImpl.COMPLETED
            }
        }
    }

    suspend fun select(suggestion: BaseSearchSuggestion): SearchSelectionResponse {
        return suspendCancellableCoroutine { continuation ->
            val task = select(suggestion, SearchSdkMainThreadWorker.mainExecutor, object : BaseSearchSelectionCallback {
                override fun onSuggestions(suggestions: List<BaseSearchSuggestion>, responseInfo: BaseResponseInfo) {
                    continuation.resumeWith(
                        Result.success(SearchSelectionResponse.Suggestions(suggestions, responseInfo))
                    )
                }

                override fun onResult(suggestion: BaseSearchSuggestion, result: BaseSearchResult, responseInfo: BaseResponseInfo) {
                    continuation.resumeWith(
                        Result.success(SearchSelectionResponse.Result(suggestion, result, responseInfo))
                    )
                }

                override fun onCategoryResult(
                    suggestion: BaseSearchSuggestion,
                    results: List<BaseSearchResult>,
                    responseInfo: BaseResponseInfo
                ) {
                    continuation.resumeWith(
                        Result.success(SearchSelectionResponse.CategoryResult(suggestion, results, responseInfo))
                    )
                }

                override fun onError(e: Exception) {
                    continuation.resumeWith(
                        Result.success(SearchSelectionResponse.Error(e))
                    )
                }
            })

            continuation.invokeOnCancellation {
                task.cancel()
            }
        }
    }

    fun select(
        suggestions: List<BaseSearchSuggestion>,
        executor: Executor,
        callback: BaseSearchMultipleSelectionCallback,
    ): AsyncOperationTask {
        require(suggestions.isNotEmpty()) {
            "No suggestions were provided! Please, provide at least 1 suggestion."
        }

        if (suggestions.distinctBy { it.requestOptions }.size != 1) {
            executor.execute {
                callback.onError(
                    IllegalArgumentException("All provided suggestions must originate from the same search result!")
                )
            }
            return AsyncOperationTaskImpl.COMPLETED
        }

        logd("batch select($suggestions) called")

        val searchResponseInfo = BaseResponseInfo(suggestions.first().requestOptions, null, isReproducible = false)

        val filtered: List<BaseServerSearchSuggestion> = suggestions
            .mapNotNull { it as? BaseServerSearchSuggestion }
            .filter { it.isBatchResolveSupported }

        if (filtered.isEmpty()) {
            executor.execute { callback.onResult(filtered, emptyList(), searchResponseInfo) }
            return AsyncOperationTaskImpl.COMPLETED
        }

        logd("Batch retrieve. ${suggestions.size} requested, ${filtered.size} took for processing")

        val coreSearchResults = filtered.map { it.rawSearchResult.mapToCore() }

        val resultingFunction: (List<BaseSearchResult>) -> List<BaseSearchResult> = { remoteResults ->
            assertDebug(remoteResults.size == filtered.size) {
                "Not all items have been resolved. " +
                        "To resolve: ${filtered.map { it.id to it.type }}, " +
                        "actual: ${remoteResults.map { it.id to it.types }}"
            }
            remoteResults
        }

        return makeRequest(callback) { task ->
            val requestOptions = filtered.first().requestOptions
            val requestContext = requestOptions.requestContext
            val requestId = coreEngine.retrieveBucket(
                requestOptions.core,
                coreSearchResults,
                TwoStepsBatchRequestCallbackWrapper(
                    suggestions = filtered,
                    searchResultFactory = searchResultFactory,
                    callbackExecutor = executor,
                    workerExecutor = engineExecutorService,
                    searchRequestTask = task,
                    resultingFunction = resultingFunction,
                    searchRequestContext = requestContext,
                )
            )
            task.addOnCancelledCallback {
                coreEngine.cancel(requestId)
            }
        }
    }

    suspend fun select(suggestions: List<BaseSearchSuggestion>): SearchMultipleSelectionResponse {
        return suspendCancellableCoroutine { continuation ->
            val task = select(suggestions, SearchSdkMainThreadWorker.mainExecutor, object : BaseSearchMultipleSelectionCallback {

                override fun onResult(
                    suggestions: List<BaseSearchSuggestion>,
                    results: List<BaseSearchResult>,
                    responseInfo: BaseResponseInfo
                ) {
                    continuation.resumeWith(
                        Result.success(SearchMultipleSelectionResponse.Results(suggestions, results, responseInfo))
                    )
                }

                override fun onError(e: Exception) {
                    continuation.resumeWith(
                        Result.success(SearchMultipleSelectionResponse.Error(e))
                    )
                }
            })

            continuation.invokeOnCancellation {
                task.cancel()
            }
        }
    }

    fun search(
        options: CoreReverseGeoOptions,
        executor: Executor,
        callback: BaseSearchCallback,
    ): AsyncOperationTask {
        return makeRequest(callback) { task ->
            val requestContext = requestContextProvider.provide(CoreApiType.AUTOFILL)
            val requestId = coreEngine.reverseGeocoding(
                options,
                OneStepRequestCallbackWrapper(
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

    suspend fun search(options: CoreReverseGeoOptions): SearchResultsResponse {
        return suspendCancellableCoroutine { continuation ->
            val task = search(options, SearchSdkMainThreadWorker.mainExecutor, object : BaseSearchCallback {
                override fun onResults(results: List<BaseSearchResult>, responseInfo: BaseResponseInfo) {
                    continuation.resumeWith(
                        Result.success(SearchResultsResponse.Results(results, responseInfo))
                    )
                }

                override fun onError(e: Exception) {
                    continuation.resumeWith(
                        Result.success(SearchResultsResponse.Error(e))
                    )
                }
            })

            continuation.invokeOnCancellation {
                task.cancel()
            }
        }
    }

    companion object {

        private val DEFAULT_EXECUTOR: ExecutorService = Executors.newSingleThreadExecutor { runnable ->
            Thread(runnable, "AddressAutofill executor")
        }

        fun create(
            accessToken: String,
            app: Application,
            locationEngine: LocationEngine,
        ): AutofillSearchEngine {
            val coreEngine = CoreSearchEngine(
                CoreEngineOptions(
                    accessToken,
                    null,
                    CoreApiType.AUTOFILL,
                    UserAgentProvider.userAgent,
                    null
                ),
                WrapperLocationProvider(
                    LocationEngineAdapter(app, locationEngine),
                    null
                ),
            )

            val requestContextProvider = SearchRequestContextProvider(
                AndroidKeyboardLocaleProvider(app),
                AndroidScreenOrientationProvider(app)
            )

            val searchResultFactory = SearchResultFactory(IndexableRecordResolver.EMPTY)

            return AutofillSearchEngine(
                coreEngine = coreEngine,
                historyService = SearchHistoryService.STUB,
                requestContextProvider = requestContextProvider,
                searchResultFactory = searchResultFactory,
            )
        }
    }
}
