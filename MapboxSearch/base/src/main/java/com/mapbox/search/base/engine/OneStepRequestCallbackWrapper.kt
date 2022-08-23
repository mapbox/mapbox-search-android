package com.mapbox.search.base.engine

import androidx.collection.SparseArrayCompat
import com.mapbox.search.base.BaseRequestOptions
import com.mapbox.search.base.BaseResponseInfo
import com.mapbox.search.base.BaseSearchCallback
import com.mapbox.search.base.core.CoreSearchCallback
import com.mapbox.search.base.core.CoreSearchResponse
import com.mapbox.search.base.core.CoreSearchResponseErrorType
import com.mapbox.search.base.failDebug
import com.mapbox.search.base.result.BaseSearchResult
import com.mapbox.search.base.result.SearchRequestContext
import com.mapbox.search.base.result.SearchResultFactory
import com.mapbox.search.base.result.mapToBase
import com.mapbox.search.base.task.ExtendedAsyncOperationTask
import com.mapbox.search.base.throwDebug
import com.mapbox.search.base.utils.extension.toPlatformHttpException
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.common.SearchCancellationException
import java.io.IOException
import java.util.concurrent.Executor

class OneStepRequestCallbackWrapper(
    private val searchResultFactory: SearchResultFactory,
    private val callbackExecutor: Executor,
    private val workerExecutor: Executor,
    private val searchRequestTask: ExtendedAsyncOperationTask<out BaseSearchCallback>,
    private val searchRequestContext: SearchRequestContext,
    private val isOffline: Boolean
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

                val request = BaseRequestOptions(
                    core = response.request,
                    requestContext = newContext,
                )
                val responseInfo = BaseResponseInfo(request, response.mapToBase(), isReproducible = !isOffline)

                val results = SparseArrayCompat<Result<BaseSearchResult>>()

                fun notifyCallbackIfNeeded() {
                    if (results.size() == responseResult.size) {
                        val searchResults = mutableListOf<BaseSearchResult>()
                        responseResult.indices.forEach { resultIndex ->
                            with(results[resultIndex]) {
                                if (this != null && isSuccess) {
                                    searchResults.add(getOrThrow())
                                } else {
                                    val e = this?.exceptionOrNull()
                                    throwDebug(e) {
                                        "Can't parse data from backend: ${responseResult[resultIndex]}: ${e?.message}"
                                    }
                                }
                            }
                        }
                        searchRequestTask.markExecutedAndRunOnCallback(callbackExecutor) {
                            onResults(searchResults, responseInfo)
                        }
                    }
                }

                responseResult.forEachIndexed { index, coreSearchResult ->
                    val rawSearchResult = coreSearchResult.mapToBase()
                    when {
                        searchResultFactory.isResolvedSearchResult(rawSearchResult) -> {
                            val searchResult = searchResultFactory.createSearchResult(rawSearchResult, request)
                            val res = when {
                                searchResult != null -> Result.success(searchResult)
                                else -> Result.failure(Exception("Can't resolve search result: $rawSearchResult"))
                            }
                            results.append(index, res)
                        }
                        searchResultFactory.isUserRecord(rawSearchResult) -> {
                            val task = searchResultFactory.resolveIndexableRecordSearchResultAsync(
                                rawSearchResult,
                                workerExecutor,
                                request
                            ) { result ->
                                with(result) {
                                    results.append(index, this)
                                    notifyCallbackIfNeeded()
                                }
                            }
                            searchRequestTask += task
                            tasks.add(task)
                        }
                        else -> {
                            results.append(
                                index,
                                Result.failure(Exception("Can't resolve search result $rawSearchResult"))
                            )
                        }
                    }

                    notifyCallbackIfNeeded()
                }

                if (responseResult.isEmpty()) {
                    searchRequestTask.markExecutedAndRunOnCallback(callbackExecutor) {
                        onResults(emptyList(), responseInfo)
                    }
                }
            } catch (e: Exception) {
                tasks.forEach { it.cancel() }

                if (!searchRequestTask.callbackActionExecuted && !searchRequestTask.isCancelled) {
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
