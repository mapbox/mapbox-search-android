package com.mapbox.search.base.engine

import androidx.collection.SparseArrayCompat
import com.mapbox.search.base.BaseRequestOptions
import com.mapbox.search.base.BaseResponseInfo
import com.mapbox.search.base.BaseSearchSelectionCallback
import com.mapbox.search.base.BaseSearchSuggestionsCallback
import com.mapbox.search.base.assertDebug
import com.mapbox.search.base.core.CoreApiType
import com.mapbox.search.base.core.CoreSearchCallback
import com.mapbox.search.base.core.CoreSearchEngineInterface
import com.mapbox.search.base.core.CoreSearchResponse
import com.mapbox.search.base.core.CoreSearchResponseErrorType
import com.mapbox.search.base.failDebug
import com.mapbox.search.base.record.SearchHistoryService
import com.mapbox.search.base.result.BaseSearchSuggestion
import com.mapbox.search.base.result.BaseSearchSuggestionType
import com.mapbox.search.base.result.SearchRequestContext
import com.mapbox.search.base.result.SearchResultFactory
import com.mapbox.search.base.result.mapToBase
import com.mapbox.search.base.task.AsyncOperationTaskImpl
import com.mapbox.search.base.throwDebug
import com.mapbox.search.base.utils.InternalIgnorableException
import com.mapbox.search.base.utils.extension.toPlatformHttpException
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.common.SearchCancellationException
import java.io.IOException
import java.util.concurrent.Executor

class TwoStepsRequestCallbackWrapper(
    private val apiType: CoreApiType,
    private val coreEngine: CoreSearchEngineInterface,
    private val historyService: SearchHistoryService,
    private val searchResultFactory: SearchResultFactory,
    private val callbackExecutor: Executor,
    private val workerExecutor: Executor,
    private val searchRequestTask: AsyncOperationTaskImpl<BaseSearchSuggestionsCallback>,
    private val searchRequestContext: SearchRequestContext,
    private val suggestion: BaseSearchSuggestion?,
    private val addResultToHistory: Boolean,
) : CoreSearchCallback {

    override fun run(response: CoreSearchResponse) {
        workerExecutor.execute {
            if (searchRequestTask.isCompleted) {
                return@execute
            }

            // Update context with Response UUID, which can be obtained only after successful request
            val newContext = searchRequestContext.copy(responseUuid = response.responseUUID)

            val tasks = mutableListOf<AsyncOperationTask>()

            try {
                if (response.results.isError) {
                    val coreError = response.results.error
                    if (coreError == null) {
                        failDebug {
                            "CoreSearchResponse.isError == true but error is null"
                        }
                        return@execute
                    }

                    when (coreError.typeInfo) {
                        CoreSearchResponseErrorType.CONNECTION_ERROR -> {
                            val error = IOException(
                                "Unable to perform search request: ${coreError.connectionError.message}"
                            )

                            searchRequestTask.markExecutedAndRunOnCallback(callbackExecutor) {
                                onError(error)
                            }
                        }
                        CoreSearchResponseErrorType.HTTP_ERROR -> {
                            val error = coreError.toPlatformHttpException()

                            searchRequestTask.markExecutedAndRunOnCallback(callbackExecutor) {
                                onError(error)
                            }
                        }
                        CoreSearchResponseErrorType.INTERNAL_ERROR -> {
                            val error = Exception(
                                "Unable to perform search request: ${coreError.internalError.message}"
                            )

                            searchRequestTask.markExecutedAndRunOnCallback(callbackExecutor) {
                                onError(error)
                            }
                        }
                        CoreSearchResponseErrorType.REQUEST_CANCELLED -> {
                            searchRequestTask.markCancelledAndRunOnCallback(callbackExecutor) {
                                onError(SearchCancellationException(coreError.requestCancelled.reason))
                            }
                        }
                        null -> {
                            val error = IllegalStateException("CoreSearchResponse.error.typeInfo is null")
                            searchRequestTask.markExecutedAndRunOnCallback(callbackExecutor) {
                                onError(error)
                            }
                        }
                    }
                    return@execute
                }

                val responseResult = requireNotNull(response.results.value)

                val requestOptions = BaseRequestOptions(
                    core = response.request,
                    requestContext = newContext,
                )
                val responseInfo = createResponseInfo(response, requestOptions)

                if (suggestion?.type is BaseSearchSuggestionType.Category ||
                    suggestion?.type is BaseSearchSuggestionType.Brand
                ) {
                    val results = responseResult.mapNotNull {
                        val searchResult = it.mapToBase()
                        searchResultFactory.createSearchResult(searchResult, requestOptions)
                    }
                    assertDebug(results.size == responseResult.size) {
                        "Can't parse some data. " +
                                "Original: ${responseResult.map { it.id to it.types }}, " +
                                "parsed: ${results.map { it.id to it.types }}, " +
                                "requestOptions: $requestOptions"
                    }
                    searchRequestTask.markExecutedAndRunOnCallback(callbackExecutor) {
                        (this as BaseSearchSelectionCallback).onResults(suggestion, results, responseInfo)
                    }
                } else if (suggestion != null &&
                    responseResult.size == 1 &&
                    searchResultFactory.isResolvedSearchResult(responseResult.first().mapToBase())
                ) {
                    val searchResult = searchResultFactory.createSearchResult(
                        responseResult.first().mapToBase(),
                        requestOptions
                    )

                    if (searchResult != null) {
                        coreEngine.onSelected(response.request, responseResult.first())

                        fun publishResult() {
                            searchRequestTask.markExecutedAndRunOnCallback(callbackExecutor) {
                                (this as BaseSearchSelectionCallback).onResult(suggestion, searchResult, responseInfo)
                            }
                        }

                        val asyncTask = if (addResultToHistory) {
                            historyService.addToHistoryIfNeeded(
                                searchResult = searchResult,
                                executor = workerExecutor,
                                callback = { result ->
                                    result.onSuccess {
                                        publishResult()
                                    }.onFailure { e ->
                                        searchRequestTask.markExecutedAndRunOnCallback(callbackExecutor) {
                                            onError((e as? Exception) ?: Exception(e))
                                        }
                                    }
                                }
                            )
                        } else {
                            null
                        }

                        if (asyncTask == null) {
                            publishResult()
                        } else {
                            searchRequestTask += asyncTask
                            tasks.add(asyncTask)
                        }
                    } else {
                        searchRequestTask.markExecutedAndRunOnCallback(callbackExecutor) {
                            onError(Exception("Can't parse received search result: ${responseResult.first()}"))
                        }
                    }
                } else {
                    val results = SparseArrayCompat<Result<BaseSearchSuggestion>>()
                    responseResult.forEachIndexed { index, searchResult ->
                        val rawSearchResult = searchResult.mapToBase()
                        val task = searchResultFactory.createSearchSuggestionAsync(
                            rawSearchResult,
                            requestOptions,
                            apiType,
                            workerExecutor
                        ) {
                            if (it.isFailure) {
                                val e = it.exceptionOrNull()
                                if (e !is InternalIgnorableException) {
                                    throwDebug(e) {
                                        "Can't create suggestions ${response.results}: ${e?.message}"
                                    }
                                }
                            }

                            results.append(index, it)

                            if (results.size() == responseResult.size) {
                                try {
                                    val suggestions = mutableListOf<BaseSearchSuggestion>()
                                    responseResult.indices.forEach { resultIndex ->
                                        with(results[resultIndex]) {
                                            if (this != null && isSuccess) {
                                                suggestions.add(getOrThrow())
                                            }
                                        }
                                    }
                                    searchRequestTask.markExecutedAndRunOnCallback(callbackExecutor) {
                                        onSuggestions(suggestions, responseInfo)
                                    }
                                } catch (e: Exception) {
                                    if (!searchRequestTask.isCancelled && !searchRequestTask.callbackActionExecuted) {
                                        searchRequestTask.markExecutedAndRunOnCallback(callbackExecutor) {
                                            onError(e)
                                        }
                                    } else {
                                        throw e
                                    }
                                }
                            }
                        }
                        searchRequestTask += task
                        tasks.add(task)
                    }

                    if (responseResult.isEmpty()) {
                        searchRequestTask.markExecutedAndRunOnCallback(callbackExecutor) {
                            onSuggestions(emptyList(), responseInfo)
                        }
                    }
                }
            } catch (e: Exception) {
                if (e !is InternalIgnorableException) {
                    tasks.forEach { it.cancel() }

                    if (!searchRequestTask.isCancelled && !searchRequestTask.callbackActionExecuted) {
                        searchRequestTask.markExecutedAndRunOnCallback(callbackExecutor) {
                            onError(e)
                        }
                    } else {
                        throw e
                    }
                }
            }
        }
    }

    private fun createResponseInfo(coreSearchResponse: CoreSearchResponse, request: BaseRequestOptions): BaseResponseInfo {
        val response = coreSearchResponse.mapToBase()
        return when {
            // If CoreSearchResponse is received for 1st step of forward geocoding or
            // for query (recursive) suggestion retrieval on 2nd step forward geocoding,
            // RequestOptions will contain all parameters, with which CoreSearchResponse
            // can be reproduced.
            suggestion == null || suggestion.type is BaseSearchSuggestionType.Query -> BaseResponseInfo(request, response, isReproducible = true)

            // When CoreSearchResponse is received for category suggestion,
            // RequestOptions and CoreSearchResponse will be inconsistent, but we still
            // want to be able to report this CoreSearchResponse, so feedback could be triaged
            // easier.
            suggestion.type is BaseSearchSuggestionType.Category -> BaseResponseInfo(request, response, isReproducible = false)

            // In other cases we retrieve SearchResult. For such use cases
            // we're not interested in CoreSearchResponse, because SearchResult contains
            // all required information.
            else -> BaseResponseInfo(request, null, isReproducible = false)
        }
    }
}
