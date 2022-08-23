package com.mapbox.search.base.engine

import com.mapbox.search.base.BaseRequestOptions
import com.mapbox.search.base.BaseResponseInfo
import com.mapbox.search.base.BaseSearchMultipleSelectionCallback
import com.mapbox.search.base.assertDebug
import com.mapbox.search.base.core.CoreSearchCallback
import com.mapbox.search.base.core.CoreSearchResponse
import com.mapbox.search.base.core.CoreSearchResponseErrorType
import com.mapbox.search.base.failDebug
import com.mapbox.search.base.result.BaseSearchResult
import com.mapbox.search.base.result.BaseSearchSuggestion
import com.mapbox.search.base.result.SearchRequestContext
import com.mapbox.search.base.result.SearchResultFactory
import com.mapbox.search.base.result.mapToBase
import com.mapbox.search.base.task.AsyncOperationTaskImpl
import com.mapbox.search.base.utils.extension.toPlatformHttpException
import com.mapbox.search.common.SearchCancellationException
import java.io.IOException
import java.util.concurrent.Executor

class TwoStepsBatchRequestCallbackWrapper(
    private val suggestions: List<BaseSearchSuggestion>,
    private val searchResultFactory: SearchResultFactory,
    private val callbackExecutor: Executor,
    private val workerExecutor: Executor,
    private val searchRequestTask: AsyncOperationTaskImpl<BaseSearchMultipleSelectionCallback>,
    private val resultingFunction: (List<BaseSearchResult>) -> List<BaseSearchResult>,
    private val searchRequestContext: SearchRequestContext,
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
                val results = responseResult.mapNotNull {
                    val searchResult = it.mapToBase()
                    searchResultFactory.createSearchResult(searchResult, requestOptions)
                }

                // We do not put [response] into ResponseInfo for batch retrieve,
                // because RequestOptions and CoreSearchResponse are inconsistent.
                val responseInfo = BaseResponseInfo(requestOptions, null, isReproducible = false)
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
