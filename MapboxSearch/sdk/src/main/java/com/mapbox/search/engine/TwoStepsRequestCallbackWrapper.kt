package com.mapbox.search.engine

import androidx.collection.SparseArrayCompat
import com.mapbox.search.ApiType
import com.mapbox.search.AsyncOperationTask
import com.mapbox.search.CompletionCallback
import com.mapbox.search.RequestOptions
import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchRequestException
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
import com.mapbox.search.core.http.HttpErrorsCache
import com.mapbox.search.mapToPlatform
import com.mapbox.search.markExecutedAndRunOnCallback
import com.mapbox.search.plusAssign
import com.mapbox.search.record.HistoryService
import com.mapbox.search.result.SearchRequestContext
import com.mapbox.search.result.SearchResultFactory
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.result.SearchSuggestionType
import com.mapbox.search.result.mapToPlatform
import java.util.concurrent.Executor

internal class TwoStepsRequestCallbackWrapper(
    private val apiType: ApiType = ApiType.SBS,
    private val coreEngine: CoreSearchEngineInterface,
    private val httpErrorsCache: HttpErrorsCache,
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
                if (!response.isSuccessful) {
                    val error = httpErrorsCache.getAndRemove(response.requestID) ?: when {
                        isOfflineSearch -> Exception("Unknown error. Response: $response")
                        else -> SearchRequestException(message = response.message, code = response.httpCode)
                    }

                    reportRelease(error)

                    searchRequestTask.markExecutedAndRunOnCallback(callbackExecutor) {
                        onError(error)
                    }
                    return@execute
                }

                val requestOptions = response.request.mapToPlatform(searchRequestContext = newContext)
                val responseInfo = createResponseInfo(response, requestOptions)

                if (suggestion?.type is SearchSuggestionType.Category) {
                    val results = response.results.mapNotNull {
                        val searchResult = it.mapToPlatform()
                        searchResultFactory.createSearchResult(searchResult, requestOptions)
                    }
                    assertDebug(results.size == response.results.size) {
                        "Can't parse some data. " +
                                "Original: ${response.results.map { it.id to it.types }}, " +
                                "parsed: ${results.map { it.id to it.types }}, " +
                                "requestOptions: $requestOptions"
                    }
                    searchRequestTask.markExecutedAndRunOnCallback(callbackExecutor) {
                        (this as SearchSelectionCallback).onCategoryResult(suggestion, results, responseInfo)
                    }
                } else if (suggestion != null &&
                    response.results.size == 1 &&
                    searchResultFactory.isResolvedSearchResult(response.results.first().mapToPlatform())
                ) {
                    val searchResult = searchResultFactory.createSearchResult(response.results.first().mapToPlatform(), requestOptions)
                    if (searchResult != null) {
                        coreEngine.onSelected(response.request, response.results.first())

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
                            onError(Exception("Can't parse received search result: ${response.results.first()}"))
                        }
                    }
                } else {
                    val results = SparseArrayCompat<Result<SearchSuggestion>>()
                    response.results.forEachIndexed { index, searchResult ->
                        val original = searchResult.mapToPlatform()
                        val task = searchResultFactory.createSearchSuggestionAsync(original, requestOptions, apiType, workerExecutor, isOfflineSearch) {
                            if (it.isFailure) {
                                throwDebug(it.exceptionOrNull()) {
                                    "Can't create suggestions ${response.results}"
                                }
                            }

                            results.append(index, it)

                            if (results.size() == response.results.size) {
                                try {
                                    val suggestions = mutableListOf<SearchSuggestion>()
                                    response.results.indices.forEach { resultIndex ->
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
                                    if (!searchRequestTask.isCanceled && !searchRequestTask.callbackActionExecuted) {
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

                    if (response.results.isEmpty()) {
                        searchRequestTask.markExecutedAndRunOnCallback(callbackExecutor) {
                            onSuggestions(emptyList(), responseInfo)
                        }
                    }
                }
            } catch (e: Exception) {
                tasks.forEach { it.cancel() }

                if (!searchRequestTask.isCanceled && !searchRequestTask.callbackActionExecuted) {
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

    private fun createResponseInfo(response: CoreSearchResponse, request: RequestOptions): ResponseInfo {
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
