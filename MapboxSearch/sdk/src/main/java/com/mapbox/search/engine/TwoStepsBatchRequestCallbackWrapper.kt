package com.mapbox.search.engine

import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchMultipleSelectionCallback
import com.mapbox.search.SearchRequestException
import com.mapbox.search.SearchRequestTaskImpl
import com.mapbox.search.common.assertDebug
import com.mapbox.search.common.reportRelease
import com.mapbox.search.core.CoreSearchCallback
import com.mapbox.search.core.CoreSearchResponse
import com.mapbox.search.core.http.HttpErrorsCache
import com.mapbox.search.mapToPlatform
import com.mapbox.search.markExecutedAndRunOnCallback
import com.mapbox.search.result.SearchRequestContext
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchResultFactory
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.result.mapToPlatform
import java.util.concurrent.Executor

internal class TwoStepsBatchRequestCallbackWrapper(
    private val suggestions: List<SearchSuggestion>,
    private val httpErrorsCache: HttpErrorsCache,
    private val searchResultFactory: SearchResultFactory,
    private val callbackExecutor: Executor,
    private val workerExecutor: Executor,
    private val searchRequestTask: SearchRequestTaskImpl<SearchMultipleSelectionCallback>,
    private val resultingFunction: (List<SearchResult>) -> List<SearchResult>,
    private val searchRequestContext: SearchRequestContext
) : CoreSearchCallback {

    override fun run(response: CoreSearchResponse) {
        workerExecutor.execute {
            if (searchRequestTask.isCompleted) {
                return@execute
            }

            // Update context with Response UUID, which can be obtained only after successful request
            val newContext = searchRequestContext.copy(responseUuid = response.responseUUID)

            try {
                if (!response.isSuccessful) {
                    val error = httpErrorsCache.getAndRemove(response.requestID)
                        ?: SearchRequestException(message = response.message, code = response.httpCode)

                    reportRelease(error)

                    searchRequestTask.markExecutedAndRunOnCallback(callbackExecutor) {
                        onError(error)
                    }
                    return@execute
                }

                val requestOptions = response.request.mapToPlatform(searchRequestContext = newContext)
                val results = response.results.mapNotNull {
                    val searchResult = it.mapToPlatform()
                    searchResultFactory.createSearchResult(searchResult, requestOptions)
                }

                // We do not put [response] into ResponseInfo for batch retrieve,
                // because RequestOptions and CoreSearchResponse are inconsistent.
                val responseInfo = ResponseInfo(requestOptions, null, isReproducible = false)
                assertDebug(results.size == response.results.size) {
                    "Can't parse some data. " +
                            "Original: ${response.results.map { it.id to it.types }}, " +
                            "parsed: ${results.map { it.id to it.types }}, " +
                            "requestOptions: $requestOptions"
                }
                searchRequestTask.markExecutedAndRunOnCallback(callbackExecutor) {
                    onResult(suggestions, resultingFunction(results), responseInfo)
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
}
