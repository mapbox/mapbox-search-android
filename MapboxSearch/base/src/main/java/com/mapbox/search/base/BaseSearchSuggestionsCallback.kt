package com.mapbox.search.base

import com.mapbox.search.base.result.BaseSearchResult
import com.mapbox.search.base.result.BaseSearchSuggestion

interface BaseSearchSuggestionsCallback {
    fun onSuggestions(suggestions: List<BaseSearchSuggestion>, responseInfo: BaseResponseInfo)
    fun onError(e: Exception)
}

interface BaseSearchSelectionCallback : BaseSearchSuggestionsCallback {

    fun onResult(suggestion: BaseSearchSuggestion, result: BaseSearchResult, responseInfo: BaseResponseInfo)

    fun onCategoryResult(
        suggestion: BaseSearchSuggestion,
        results: List<BaseSearchResult>,
        responseInfo: BaseResponseInfo
    )
}

interface BaseSearchMultipleSelectionCallback {

    fun onResult(
        suggestions: List<BaseSearchSuggestion>,
        results: List<BaseSearchResult>,
        responseInfo: BaseResponseInfo
    )

    fun onError(e: Exception)
}
