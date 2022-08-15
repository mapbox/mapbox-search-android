package com.mapbox.search.result

import com.mapbox.search.ApiType
import com.mapbox.search.AsyncOperationTask
import com.mapbox.search.CompletedAsyncOperationTask
import com.mapbox.search.CompletionCallback
import com.mapbox.search.RequestOptions
import com.mapbox.search.common.failDebug
import com.mapbox.search.record.DataProviderResolver
import com.mapbox.search.record.IndexableRecord
import java.util.concurrent.Executor

internal class SearchResultFactory(private val dataProviderResolver: DataProviderResolver) {

    fun isUserRecord(searchResult: OriginalSearchResult): Boolean {
        return searchResult.type == OriginalResultType.USER_RECORD
    }

    fun isResolvedSearchResult(searchResult: OriginalSearchResult): Boolean {
        fun debugInfo(): String {
            return prepareSearchResultInfo(searchResult)
        }

        return when {
            searchResult.types.isValidMultiType() && searchResult.types.all { it.isSearchResultType } -> {
                searchResult.action == null && searchResult.center != null
            }
            searchResult.type in NOT_SEARCH_RESULT_TYPES -> false
            else -> {
                failDebug { "Can't check is search result resolved: ${debugInfo()}" }
                false
            }
        }
    }

    fun createSearchResult(searchResult: OriginalSearchResult, requestOptions: RequestOptions): SearchResult? {
        fun debugInfo(): String {
            return prepareSearchResultInfo(searchResult, requestOptions)
        }

        if (!(searchResult.action == null && searchResult.center != null)) {
            failDebug { "Can't create a search result: missing 'action' for non-null 'center'. ${debugInfo()}" }
            return null
        }

        return when {
            searchResult.types.isValidMultiType() && searchResult.types.all { it.isSearchResultType } -> {
                val searchResultTypes = searchResult.types.map { it.tryMapToSearchResultType()!! }
                ServerSearchResultImpl(searchResultTypes, searchResult, requestOptions)
            }
            searchResult.type in NOT_SEARCH_RESULT_TYPES -> {
                failDebug { "Can't create SearchResult of ${searchResult.type} result type. ${debugInfo()}" }
                null
            }
            else -> {
                failDebug { "Illegal original types: ${searchResult.types}. ${debugInfo()}" }
                null
            }
        }
    }

    fun createSearchSuggestionAsync(
        searchResult: OriginalSearchResult,
        requestOptions: RequestOptions,
        apiType: ApiType,
        callbackExecutor: Executor,
        isOffline: Boolean = false,
        callback: (Result<SearchSuggestion>) -> Unit
    ): AsyncOperationTask {
        fun debugInfo(): String {
            return prepareSearchResultInfo(searchResult, requestOptions, apiType)
        }

        when (apiType) {
            ApiType.GEOCODING -> {
                if (searchResult.action != null) {
                    failDebug { "Can't create search suggestion. ${debugInfo()}" }
                    callback(Result.failure(Exception("Can't create search suggestion from $searchResult")))
                    return CompletedAsyncOperationTask
                }
            }
            ApiType.SBS, ApiType.AUTOFILL -> {
                if (!isOffline && searchResult.action == null && searchResult.type != OriginalResultType.USER_RECORD) {
                    failDebug { "Can't create search suggestion from. ${debugInfo()}" }
                    callback(Result.failure(Exception("Can't create search suggestion from $searchResult")))
                    return CompletedAsyncOperationTask
                }
            }
        }

        return when (searchResult.type) {
            OriginalResultType.COUNTRY,
            OriginalResultType.REGION,
            OriginalResultType.PLACE,
            OriginalResultType.DISTRICT,
            OriginalResultType.LOCALITY,
            OriginalResultType.NEIGHBORHOOD,
            OriginalResultType.ADDRESS,
            OriginalResultType.POI,
            OriginalResultType.STREET,
            OriginalResultType.POSTCODE,
            OriginalResultType.BLOCK,
            OriginalResultType.QUERY -> {
                when (apiType) {
                    ApiType.GEOCODING -> {
                        if (searchResult.center != null && searchResult.type.isSearchResultType) {
                            val value = GeocodingCompatSearchSuggestion(searchResult, requestOptions)
                            callback(Result.success(value))
                            CompletedAsyncOperationTask
                        } else {
                            failDebug { "Can't create GeocodingCompatSearchSuggestion. ${debugInfo()}" }
                            callback(Result.failure(Exception("Can't create GeocodingCompatSearchSuggestion from $searchResult")))
                            CompletedAsyncOperationTask
                        }
                    }
                    ApiType.SBS, ApiType.AUTOFILL -> {
                        if (searchResult.types.isValidMultiType() && searchResult.center == null) {
                            val value = ServerSearchSuggestion(searchResult, requestOptions, isFromOffline = isOffline)
                            callback(Result.success(value))
                            CompletedAsyncOperationTask
                        } else {
                            failDebug { "Invalid search result with types or coordinate set. ${debugInfo()}" }
                            callback(Result.failure(Exception("Invalid search result with types or coordinate set: $searchResult")))
                            CompletedAsyncOperationTask
                        }
                    }
                }
            }
            OriginalResultType.CATEGORY -> {
                if (searchResult.categoryCanonicalName != null) {
                    val value = ServerSearchSuggestion(searchResult, requestOptions, isFromOffline = isOffline)
                    callback(Result.success(value))
                    CompletedAsyncOperationTask
                } else {
                    failDebug { "Invalid category search result without category canonical name. ${debugInfo()}" }
                    callback(Result.failure(Exception("Invalid category search result without category canonical name: $searchResult")))
                    CompletedAsyncOperationTask
                }
            }
            OriginalResultType.USER_RECORD -> {
                if (searchResult.layerId != null) {
                    resolveIndexableRecordAsync(searchResult, callbackExecutor) {
                        callback(it.map { record ->
                            IndexableRecordSearchSuggestion(record, searchResult, requestOptions)
                        })
                    }
                } else {
                    failDebug { "${OriginalResultType.USER_RECORD} search result without layer id." }
                    callback(Result.failure(Exception("USER_RECORD search result without layer id: $searchResult")))
                    CompletedAsyncOperationTask
                }
            }
            OriginalResultType.UNKNOWN -> {
                failDebug { "Invalid search result with ${OriginalResultType.UNKNOWN} result type. ${debugInfo()}" }
                callback(Result.failure(Exception("USER_RECORD search result without layer id: $searchResult")))
                CompletedAsyncOperationTask
            }
        }
    }

    fun resolveIndexableRecordSearchResultAsync(
        searchResult: OriginalSearchResult,
        callbackExecutor: Executor,
        requestOptions: RequestOptions,
        callback: (Result<IndexableRecordSearchResult>) -> Unit
    ): AsyncOperationTask {
        return resolveIndexableRecordAsync(searchResult, callbackExecutor) { result ->
            callback(result.map { record ->
                IndexableRecordSearchResultImpl(record, searchResult, requestOptions)
            })
        }
    }

    private fun resolveIndexableRecordAsync(
        searchResult: OriginalSearchResult,
        callbackExecutor: Executor,
        callback: (Result<IndexableRecord>) -> Unit
    ): AsyncOperationTask {
        val userRecordsLayer = searchResult.layerId?.let {
            dataProviderResolver.getRecordsLayer(it)
        }

        return if (userRecordsLayer == null) {
            callback(Result.failure(Exception("Can't find user records layer with id ${searchResult.layerId}. OriginalSearchResult: $searchResult")))
            CompletedAsyncOperationTask
        } else {
            val id = searchResult.userRecordId ?: searchResult.id
            userRecordsLayer.get(id, callbackExecutor, object : CompletionCallback<IndexableRecord?> {
                override fun onComplete(result: IndexableRecord?) {
                    if (result != null) {
                        callback(Result.success(result))
                    } else {
                        callback(Result.failure(Exception("Can't find record with id $id")))
                    }
                }

                override fun onError(e: Exception) {
                    callback(Result.failure(e))
                }
            })
        }
    }

    companion object {

        fun prepareSearchResultInfo(
            searchResult: OriginalSearchResult,
            requestOptions: RequestOptions? = null,
            apiType: ApiType? = null
        ): String {
            return "[SearchResult] ID: ${searchResult.id}, types: ${searchResult.types}, request options: $requestOptions, api: $apiType"
        }

        val NOT_SEARCH_RESULT_TYPES = arrayOf(
            OriginalResultType.USER_RECORD,
            OriginalResultType.CATEGORY,
            OriginalResultType.QUERY,
            OriginalResultType.UNKNOWN
        )
    }
}
