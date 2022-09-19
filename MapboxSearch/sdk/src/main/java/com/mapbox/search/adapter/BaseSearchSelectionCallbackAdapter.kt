package com.mapbox.search.adapter

import com.mapbox.search.SearchSelectionCallback
import com.mapbox.search.base.BaseResponseInfo
import com.mapbox.search.base.BaseSearchSelectionCallback
import com.mapbox.search.base.result.BaseSearchResult
import com.mapbox.search.base.result.BaseSearchSuggestion
import com.mapbox.search.mapToPlatform
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.mapToPlatform

internal class BaseSearchSelectionCallbackAdapter(
    private val callback: SearchSelectionCallback
) : BaseSearchSuggestionsCallbackAdapter(callback), BaseSearchSelectionCallback {

    override fun onResult(
        suggestion: BaseSearchSuggestion,
        result: BaseSearchResult,
        responseInfo: BaseResponseInfo
    ) {
        callback.onResult(
            suggestion.mapToPlatform(),
            SearchResult(result),
            responseInfo.mapToPlatform()
        )
    }

    override fun onCategoryResult(
        suggestion: BaseSearchSuggestion,
        results: List<BaseSearchResult>,
        responseInfo: BaseResponseInfo
    ) {
        callback.onCategoryResult(
            suggestion.mapToPlatform(),
            results.map { SearchResult(it) },
            responseInfo.mapToPlatform()
        )
    }
}
