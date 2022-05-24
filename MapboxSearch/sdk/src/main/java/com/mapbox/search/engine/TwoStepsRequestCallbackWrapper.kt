package com.mapbox.search.engine

import androidx.collection.SparseArrayCompat
import com.mapbox.search.ApiType
import com.mapbox.search.AsyncOperationTask
import com.mapbox.search.CompletionCallback
import com.mapbox.search.RequestOptions
import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchRequestTaskImpl
import com.mapbox.search.SearchSelectionCallback
import com.mapbox.search.SearchSuggestionsCallback
import com.mapbox.search.SelectOptions
import com.mapbox.search.common.assertDebug
import com.mapbox.search.common.reportRelease
import com.mapbox.search.common.throwDebug
import com.mapbox.search.core.CoreSearchCallback
import com.mapbox.search.core.CoreSearchEngineInterface
import com.mapbox.search.core.CoreSearchResponse
import com.mapbox.search.internal.bindgen.SearchResponseError
import com.mapbox.search.mapToPlatform
import com.mapbox.search.markExecutedAndRunOnCallback
import com.mapbox.search.plusAssign
import com.mapbox.search.record.HistoryService
import com.mapbox.search.result.SearchRequestContext
import com.mapbox.search.result.SearchResultFactory
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.result.SearchSuggestionType
import com.mapbox.search.result.mapToPlatform
import com.mapbox.search.utils.extension.toPlatformHttpException
import java.util.concurrent.Executor

internal class TwoStepsRequestCallbackWrapper(
    private val apiType: ApiType = ApiType.SBS,
    private val coreEngine: CoreSearchEngineInterface,
    private val historyService: HistoryService,
    private val searchResultFactory: SearchResultFactory,
    private val callbackExecutor: Executor,
    private val workerExecutor: Executor,
    private val searchRequestTask: SearchRequestTaskImpl<SearchSuggestionsCallback>,
    private val searchRequestContext: SearchRequestContext,
    private val suggestion: SearchSuggestion?,
    private val selectOptions: SelectOptions?,
    private val isOfflineSearch: Boolean,
) : CoreSearchCallback {

    // TODO check if onSelected called when needed
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
                        reportRelease(IllegalStateException("CoreSearchResponse.isError == true but error is null"))
                        return@execute
                    }

                    when (coreError.typeInfo) {
                        SearchResponseError.Type.HTTP_ERROR -> {
                            val error = coreError.toPlatformHttpException()

                            reportRelease(error)
                            searchRequestTask.markExecutedAndRunOnCallback(callbackExecutor) {
                                onError(error)
                            }
                        }
                        SearchResponseError.Type.INTERNAL_ERROR -> {
                            val error = Exception(
                                "Unable to perform search request: ${coreError.internalError.message}"
                            )

                            reportRelease(error)
                            searchRequestTask.markExecutedAndRunOnCallback(callbackExecutor) {
                                onError(error)
                            }
                        }
                        SearchResponseError.Type.REQUEST_CANCELLED -> {
                            searchRequestTask.cancel()
                        }
                        null -> {
                            val error = IllegalStateException("CoreSearchResponse.error.typeInfo is null")
                            reportRelease(error)
                            searchRequestTask.markExecutedAndRunOnCallback(callbackExecutor) {
                                onError(error)
                            }
                        }
                    }
                    return@execute
                }

                val responseResult = requireNotNull(response.results.value)

                val requestOptions = response.request.mapToPlatform(searchRequestContext = newContext)
                val responseInfo = createResponseInfo(response, requestOptions)

                if (suggestion?.type is SearchSuggestionType.Category) {
                    val results = responseResult.mapNotNull {
                        val searchResult = it.mapToPlatform()
                        searchResultFactory.createSearchResult(searchResult, requestOptions)
                    }
                    assertDebug(results.size == responseResult.size) {
                        "Can't parse some data. " +
                                "Original: ${responseResult.map { it.id to it.types }}, " +
                                "parsed: ${results.map { it.id to it.types }}, " +
                                "requestOptions: $requestOptions"
                    }
                    searchRequestTask.markExecutedAndRunOnCallback(callbackExecutor) {
                        (this as SearchSelectionCallback).onCategoryResult(suggestion, results, responseInfo)
                    }
                } else if (suggestion != null &&
                    responseResult.size == 1 &&
                    searchResultFactory.isResolvedSearchResult(responseResult.first().mapToPlatform())
                ) {
                    val searchResult = searchResultFactory.createSearchResult(responseResult.first().mapToPlatform(), requestOptions)
                    if (searchResult != null) {
                        coreEngine.onSelected(response.request, responseResult.first())

                        fun publishResult() {
                            searchRequestTask.markExecutedAndRunOnCallback(callbackExecutor) {
                                (this as SearchSelectionCallback).onResult(suggestion, searchResult, responseInfo)
                            }
                        }

                        val asyncTask = if (selectOptions?.addResultToHistory == true) {
                            historyService.addToHistoryIfNeeded(
                                searchResult = searchResult,
                                executor = workerExecutor,
                                callback = object : CompletionCallback<Boolean> {
                                    override fun onComplete(result: Boolean) {
                                        publishResult()
                                    }

                                    override fun onError(e: Exception) {
                                        searchRequestTask.markExecutedAndRunOnCallback(callbackExecutor) {
                                            onError(e)
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
                    val results = SparseArrayCompat<Result<SearchSuggestion>>()
                    responseResult.forEachIndexed { index, searchResult ->
                        val original = searchResult.mapToPlatform()
                        val task = searchResultFactory.createSearchSuggestionAsync(original, requestOptions, apiType, workerExecutor, isOfflineSearch) {
                            if (it.isFailure) {
                                val e = it.exceptionOrNull()
                                throwDebug(e) {
                                    "Can't create suggestions ${response.results}: ${e?.message}"
                                }
                            }

                            results.append(index, it)

                            if (results.size() == responseResult.size) {
                                try {
                                    val suggestions = mutableListOf<SearchSuggestion>()
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
                                        reportRelease(e)
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
                tasks.forEach { it.cancel() }

                if (!searchRequestTask.isCancelled && !searchRequestTask.callbackActionExecuted) {
                    reportRelease(e)
                    searchRequestTask.markExecutedAndRunOnCallback(callbackExecutor) {
                        onError(e)
                    }
                } else {
                    throw e
                }
            }
        }
    }

    private fun createResponseInfo(coreSearchResponse: CoreSearchResponse, request: RequestOptions): ResponseInfo {
        val response = coreSearchResponse.mapToPlatform()
        return when {
            // If CoreSearchResponse is received for 1st step of forward geocoding or
            // for query (recursive) suggestion retrieval on 2nd step forward geocoding,
            // RequestOptions will contain all parameters, with which CoreSearchResponse
            // can be reproduced.
            suggestion == null || suggestion.type is SearchSuggestionType.Query -> ResponseInfo(request, response, isReproducible = true)

            // When CoreSearchResponse is received for category suggestion,
            // RequestOptions and CoreSearchResponse will be inconsistent, but we still
            // want to be able to report this CoreSearchResponse, so feedback could be triaged
            // easier.
            suggestion.type is SearchSuggestionType.Category -> ResponseInfo(request, response, isReproducible = false)

            // In other cases we retrieve SearchResult. For such use cases
            // we're not interested in CoreSearchResponse, because SearchResult contains
            // all required information.
            else -> ResponseInfo(request, null, isReproducible = false)
        }
    }
}
