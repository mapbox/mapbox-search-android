package com.mapbox.search.engine

import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchMultipleSelectionCallback
import com.mapbox.search.SearchRequestTaskImpl
import com.mapbox.search.common.assertDebug
import com.mapbox.search.common.reportRelease
import com.mapbox.search.core.CoreSearchCallback
import com.mapbox.search.core.CoreSearchResponse
import com.mapbox.search.internal.bindgen.SearchResponseError
import com.mapbox.search.mapToPlatform
import com.mapbox.search.markExecutedAndRunOnCallback
import com.mapbox.search.result.SearchRequestContext
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchResultFactory
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.result.mapToPlatform
import com.mapbox.search.utils.extension.toPlatformHttpException
import java.util.concurrent.Executor

internal class TwoStepsBatchRequestCallbackWrapper(
    private val suggestions: List<SearchSuggestion>,
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
                val results = responseResult.mapNotNull {
                    val searchResult = it.mapToPlatform()
                    searchResultFactory.createSearchResult(searchResult, requestOptions)
                }

                // We do not put [response] into ResponseInfo for batch retrieve,
                // because RequestOptions and CoreSearchResponse are inconsistent.
                val responseInfo = ResponseInfo(requestOptions, null, isReproducible = false)
                assertDebug(results.size == responseResult.size) {
                    "Can't parse some data. " +
                            "Original: ${responseResult.map { it.id to it.types }}, " +
                            "parsed: ${results.map { it.id to it.types }}, " +
                            "requestOptions: $requestOptions"
                }
                searchRequestTask.markExecutedAndRunOnCallback(callbackExecutor) {
                    onResult(suggestions, resultingFunction(results), responseInfo)
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
}
