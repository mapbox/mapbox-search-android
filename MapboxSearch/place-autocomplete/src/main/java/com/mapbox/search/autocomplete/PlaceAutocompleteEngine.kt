package com.mapbox.search.autocomplete

import android.app.Application
import com.mapbox.bindgen.Expected
import com.mapbox.bindgen.ExpectedFactory
import com.mapbox.common.location.LocationProvider
import com.mapbox.search.base.BaseResponseInfo
import com.mapbox.search.base.BaseSearchCallback
import com.mapbox.search.base.BaseSearchMultipleSelectionCallback
import com.mapbox.search.base.BaseSearchSdkInitializer
import com.mapbox.search.base.BaseSearchSelectionCallback
import com.mapbox.search.base.BaseSearchSuggestionsCallback
import com.mapbox.search.base.SearchRequestContextProvider
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
import com.mapbox.search.base.result.BaseSearchSuggestionType
import com.mapbox.search.base.result.BaseServerSearchSuggestion
import com.mapbox.search.base.result.SearchResultFactory
import com.mapbox.search.base.result.mapToCore
import com.mapbox.search.base.task.AsyncOperationTaskImpl
import com.mapbox.search.base.utils.extension.suspendFlatMap
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.common.concurrent.SearchSdkMainThreadWorker
import com.mapbox.search.internal.bindgen.ApiType
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

internal class PlaceAutocompleteEngine(
    private val coreEngine: CoreSearchEngineInterface,
    private val requestContextProvider: SearchRequestContextProvider,
    private val historyService: SearchHistoryService = SearchHistoryService.STUB,
    private val searchResultFactory: SearchResultFactory = SearchResultFactory(
        IndexableRecordResolver.EMPTY
    ),
    private val engineExecutorService: ExecutorService = DEFAULT_EXECUTOR,
    private val apiType: ApiType = ApiType.SBS
) : BaseSearchEngine() {

    fun search(
        query: String,
        options: CoreSearchOptions,
        executor: Executor,
        callback: BaseSearchSuggestionsCallback,
    ): AsyncOperationTask {
        return makeRequest(callback) { task ->
            val requestContext = requestContextProvider.provide(apiType)
            val requestId = coreEngine.search(
                query, emptyList(), options,
                TwoStepsRequestCallbackWrapper(
                    apiType = apiType,
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

    suspend fun search(query: String, options: CoreSearchOptions): Expected<Exception, Pair<List<BaseSearchSuggestion>, BaseResponseInfo>> {
        return suspendCancellableCoroutine { continuation ->
            val task = search(query, options, SearchSdkMainThreadWorker.mainExecutor, object : BaseSearchSuggestionsCallback {
                override fun onSuggestions(suggestions: List<BaseSearchSuggestion>, responseInfo: BaseResponseInfo) {
                    continuation.resumeWith(
                        Result.success(ExpectedFactory.createValue(suggestions to responseInfo))
                    )
                }

                override fun onError(e: Exception) {
                    continuation.resumeWith(Result.success(ExpectedFactory.createError(e)))
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
                            apiType = apiType,
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

    suspend fun select(suggestion: BaseSearchSuggestion): Expected<Exception, SearchSelectionResponse> {
        return suspendCancellableCoroutine { continuation ->
            val task = select(suggestion, SearchSdkMainThreadWorker.mainExecutor, object : BaseSearchSelectionCallback {
                override fun onSuggestions(suggestions: List<BaseSearchSuggestion>, responseInfo: BaseResponseInfo) {
                    continuation.resumeWith(
                        Result.success(ExpectedFactory.createValue(SearchSelectionResponse.Suggestions(suggestions, responseInfo)))
                    )
                }

                override fun onResult(suggestion: BaseSearchSuggestion, result: BaseSearchResult, responseInfo: BaseResponseInfo) {
                    continuation.resumeWith(
                        Result.success(ExpectedFactory.createValue(SearchSelectionResponse.Result(suggestion, result, responseInfo)))
                    )
                }

                override fun onResults(
                    suggestion: BaseSearchSuggestion,
                    results: List<BaseSearchResult>,
                    responseInfo: BaseResponseInfo
                ) {
                    continuation.resumeWith(
                        Result.success(ExpectedFactory.createValue(SearchSelectionResponse.Results(suggestion, results, responseInfo)))
                    )
                }

                override fun onError(e: Exception) {
                    continuation.resumeWith(Result.success(ExpectedFactory.createError(e)))
                }
            })

            continuation.invokeOnCancellation {
                task.cancel()
            }
        }
    }

    suspend fun resolveAll(
        suggestions: List<BaseSearchSuggestion>,
        allowCategorySuggestions: Boolean,
    ): Expected<Exception, List<BaseSearchResult>> {
        return when {
            suggestions.isEmpty() -> {
                ExpectedFactory.createValue(emptyList())
            }
            else -> {
                coroutineScope {
                    val deferredSuggestions: List<Deferred<Expected<Exception, SearchSelectionResponse>>> = suggestions
                        .filter {
                            when (it.type) {
                                // Filtering in order to avoid infinite recursion
                                // because of some specific suggestions like "Did you mean recursion?"
                                is BaseSearchSuggestionType.Query -> false
                                is BaseSearchSuggestionType.Category -> allowCategorySuggestions
                                else -> true
                            }
                        }
                        .map { suggestion ->
                            async { select(suggestion) }
                        }

                    val responses: List<Expected<Exception, List<BaseSearchResult>>> = deferredSuggestions
                        .map { deferred ->
                            deferred.await().suspendFlatMap { response ->
                                when (response) {
                                    is SearchSelectionResponse.Suggestions -> {
                                        resolveAll(response.suggestions, allowCategorySuggestions)
                                    }
                                    is SearchSelectionResponse.Result -> {
                                        ExpectedFactory.createValue(listOf(response.result))
                                    }
                                    is SearchSelectionResponse.Results -> {
                                        ExpectedFactory.createValue(response.results)
                                    }
                                }
                            }
                        }

                    // If at least one response completed successfully, return it.
                    if (responses.isNotEmpty() && responses.all { it.isError }) {
                        responses.first()
                    } else {
                        responses.asSequence()
                            .mapNotNull { it.value }
                            .flatten()
                            .toList()
                            .let {
                                ExpectedFactory.createValue(it)
                            }
                    }
                }
            }
        }
    }

    sealed class SearchSelectionResponse {

        data class Suggestions(
            val suggestions: List<BaseSearchSuggestion>,
            val responseInfo: BaseResponseInfo,
        ) : SearchSelectionResponse()

        data class Result(
            val suggestion: BaseSearchSuggestion,
            val result: BaseSearchResult,
            val responseInfo: BaseResponseInfo,
        ) : SearchSelectionResponse()

        data class Results(
            val suggestion: BaseSearchSuggestion,
            val results: List<BaseSearchResult>,
            val responseInfo: BaseResponseInfo,
        ) : SearchSelectionResponse()
    }

    companion object {

        private val DEFAULT_EXECUTOR: ExecutorService =
            Executors.newSingleThreadExecutor { runnable ->
                Thread(runnable, "AddressAutofill executor")
            }

        fun create(
            app: Application,
            locationProvider: LocationProvider,
        ): BaseSearchEngine {
            val coreEngine = CoreSearchEngine(
                CoreEngineOptions(
                    baseUrl = null,
                    apiType = CoreApiType.SBS,
                    sdkInformation = BaseSearchSdkInitializer.sdkInformation,
                    eventsUrl = null,
                ),
                WrapperLocationProvider(
                    LocationEngineAdapter(app, locationProvider),
                    null
                ),
            )

            return PlaceAutocompleteEngine(
                coreEngine = coreEngine,
                requestContextProvider = SearchRequestContextProvider(app),
            )
        }
    }
}
