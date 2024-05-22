package com.mapbox.search.base.result

import com.mapbox.search.base.BaseRequestOptions
import com.mapbox.search.base.core.CoreApiType
import com.mapbox.search.base.failDebug
import com.mapbox.search.base.record.BaseIndexableRecord
import com.mapbox.search.base.record.IndexableRecordResolver
import com.mapbox.search.base.task.AsyncOperationTaskImpl
import com.mapbox.search.base.utils.InternalIgnorableException
import com.mapbox.search.common.AsyncOperationTask
import java.util.concurrent.Executor

class SearchResultFactory(private val recordResolver: IndexableRecordResolver) {

    fun isUserRecord(searchResult: BaseRawSearchResult): Boolean {
        return searchResult.type == BaseRawResultType.USER_RECORD
    }

    fun isResolvedSearchResult(searchResult: BaseRawSearchResult): Boolean {
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

    fun createSearchResult(searchResult: BaseRawSearchResult, requestOptions: BaseRequestOptions): BaseSearchResult? {
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
                BaseServerSearchResultImpl(searchResultTypes, searchResult, requestOptions)
            }
            searchResult.type in NOT_SEARCH_RESULT_TYPES -> {
                failDebug { "Can't create SearchResult of ${searchResult.type} result type. ${debugInfo()}" }
                null
            }
            else -> {
                failDebug { "Illegal raw types: ${searchResult.types}. ${debugInfo()}" }
                null
            }
        }
    }

    fun createSearchSuggestionAsync(
        searchResult: BaseRawSearchResult,
        requestOptions: BaseRequestOptions,
        apiType: CoreApiType,
        callbackExecutor: Executor,
        callback: (Result<BaseSearchSuggestion>) -> Unit
    ): AsyncOperationTask {
        fun debugInfo(): String {
            return prepareSearchResultInfo(searchResult, requestOptions, apiType)
        }

        when (apiType) {
            CoreApiType.GEOCODING -> {
                if (searchResult.action != null) {
                    failDebug { "Can't create search suggestion. ${debugInfo()}" }
                    callback(Result.failure(Exception("Can't create search suggestion from $searchResult")))
                    return AsyncOperationTaskImpl.COMPLETED
                }
            }
            CoreApiType.SBS, CoreApiType.AUTOFILL -> {
                if (searchResult.action == null && searchResult.type != BaseRawResultType.USER_RECORD) {
                    failDebug { "Can't create search suggestion from. ${debugInfo()}" }
                    callback(Result.failure(Exception("Can't create search suggestion from $searchResult")))
                    return AsyncOperationTaskImpl.COMPLETED
                }
            }
            CoreApiType.SEARCH_BOX -> {
                if (searchResult.type == BaseRawResultType.BRAND) {
                    val result = if (searchResult.isValidBrandType) {
                        val value = BaseServerSearchSuggestion(searchResult, requestOptions)
                        Result.success(value)
                    } else {
                        Result.failure(InternalIgnorableException("Skipping invalid BRAND search result"))
                    }
                    callback(result)
                    return AsyncOperationTaskImpl.COMPLETED
                } else if (searchResult.action == null) {
                    if (searchResult.type == BaseRawResultType.QUERY) {
                        callback(Result.failure(InternalIgnorableException("Skipping query suggestion without action")))
                        return AsyncOperationTaskImpl.COMPLETED
                    } else if (searchResult.type != BaseRawResultType.USER_RECORD) {
                        failDebug { "Can't create search suggestion from. ${debugInfo()}" }
                        callback(Result.failure(Exception("Can't create search suggestion from $searchResult")))
                        return AsyncOperationTaskImpl.COMPLETED
                    }
                }
            }
        }

        return when (searchResult.type) {
            BaseRawResultType.COUNTRY,
            BaseRawResultType.REGION,
            BaseRawResultType.PLACE,
            BaseRawResultType.DISTRICT,
            BaseRawResultType.LOCALITY,
            BaseRawResultType.NEIGHBORHOOD,
            BaseRawResultType.ADDRESS,
            BaseRawResultType.POI,
            BaseRawResultType.STREET,
            BaseRawResultType.POSTCODE,
            BaseRawResultType.BLOCK,
            BaseRawResultType.QUERY -> {
                when (apiType) {
                    CoreApiType.GEOCODING -> {
                        if (searchResult.center != null && searchResult.type.isSearchResultType) {
                            val value = BaseGeocodingCompatSearchSuggestion(searchResult, requestOptions)
                            callback(Result.success(value))
                            AsyncOperationTaskImpl.COMPLETED
                        } else {
                            failDebug { "Can't create GeocodingCompatSearchSuggestion. ${debugInfo()}" }
                            callback(Result.failure(Exception("Can't create GeocodingCompatSearchSuggestion from $searchResult")))
                            AsyncOperationTaskImpl.COMPLETED
                        }
                    }
                    CoreApiType.SEARCH_BOX, CoreApiType.SBS, CoreApiType.AUTOFILL -> {
                        if (searchResult.types.isValidMultiType()) {
                            val value = BaseServerSearchSuggestion(searchResult, requestOptions)
                            callback(Result.success(value))
                            AsyncOperationTaskImpl.COMPLETED
                        } else {
                            failDebug { "Invalid search result types: ${debugInfo()}" }
                            callback(Result.failure(Exception("Invalid search result types: $searchResult")))
                            AsyncOperationTaskImpl.COMPLETED
                        }
                    }
                    else -> error("Unsupported API type: $apiType")
                }
            }
            BaseRawResultType.BRAND -> {
                val result = if (searchResult.isValidBrandType) {
                    val value = BaseServerSearchSuggestion(searchResult, requestOptions)
                    Result.success(value)
                } else {
                    val errorMsg = "Invalid brand search result: $searchResult"
                    failDebug { errorMsg }
                    Result.failure(Exception(errorMsg))
                }
                callback(result)
                AsyncOperationTaskImpl.COMPLETED
            }
            BaseRawResultType.CATEGORY -> {
                val result = if (searchResult.isValidCategoryType) {
                    val value = BaseServerSearchSuggestion(searchResult, requestOptions)
                    Result.success(value)
                } else {
                    failDebug { "Invalid category search result without category canonical name. ${debugInfo()}" }
                    Result.failure(Exception("Invalid category search result without category canonical name: $searchResult"))
                }
                callback(result)
                AsyncOperationTaskImpl.COMPLETED
            }
            BaseRawResultType.USER_RECORD -> {
                if (searchResult.layerId != null) {
                    resolveIndexableRecordAsync(searchResult, callbackExecutor) {
                        callback(it.map { record ->
                            BaseIndexableRecordSearchSuggestion(record, searchResult, requestOptions)
                        })
                    }
                } else {
                    failDebug { "${BaseRawResultType.USER_RECORD} search result without layer id." }
                    callback(Result.failure(Exception("USER_RECORD search result without layer id: $searchResult")))
                    AsyncOperationTaskImpl.COMPLETED
                }
            }
            BaseRawResultType.UNKNOWN -> {
                failDebug { "Invalid search result with ${BaseRawResultType.UNKNOWN} result type. ${debugInfo()}" }
                callback(Result.failure(Exception("Unknown search result type: $searchResult")))
                AsyncOperationTaskImpl.COMPLETED
            }
        }
    }

    fun resolveIndexableRecordSearchResultAsync(
        searchResult: BaseRawSearchResult,
        callbackExecutor: Executor,
        requestOptions: BaseRequestOptions,
        callback: (Result<BaseSearchResult>) -> Unit
    ): AsyncOperationTask {
        return resolveIndexableRecordAsync(searchResult, callbackExecutor) { result ->
            callback(result.map { record ->
                BaseIndexableRecordSearchResultImpl(record, searchResult, requestOptions)
            })
        }
    }

    private fun resolveIndexableRecordAsync(
        searchResult: BaseRawSearchResult,
        callbackExecutor: Executor,
        callback: (Result<BaseIndexableRecord>) -> Unit
    ): AsyncOperationTask {
        val layerId = searchResult.layerId
        if (layerId == null) {
            callback(Result.failure(Exception("Can't find user records layer with id ${searchResult.layerId}. RawSearchResult: $searchResult")))
            return AsyncOperationTaskImpl.COMPLETED
        }

        val recordId = searchResult.userRecordId ?: searchResult.id
        return recordResolver.resolve(layerId, recordId, callbackExecutor, callback)
    }

    companion object {

        fun prepareSearchResultInfo(
            searchResult: BaseRawSearchResult,
            requestOptions: BaseRequestOptions? = null,
            apiType: CoreApiType? = null
        ): String {
            return "[SearchResult] ID: ${searchResult.id}, types: ${searchResult.types}, request options: $requestOptions, api: $apiType"
        }

        val NOT_SEARCH_RESULT_TYPES = arrayOf(
            BaseRawResultType.USER_RECORD,
            BaseRawResultType.CATEGORY,
            BaseRawResultType.BRAND,
            BaseRawResultType.QUERY,
            BaseRawResultType.UNKNOWN
        )
    }
}
