package com.mapbox.search.adapter

import com.mapbox.search.SearchMultipleSelectionCallback
import com.mapbox.search.base.BaseResponseInfo
import com.mapbox.search.base.BaseSearchMultipleSelectionCallback
import com.mapbox.search.base.result.BaseSearchResult
import com.mapbox.search.base.result.BaseSearchSuggestion
import com.mapbox.search.mapToPlatform
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.mapToPlatform

internal class BaseSearchMultipleSelectionCallbackAdapter(
    private val callback: SearchMultipleSelectionCallback
) : BaseSearchMultipleSelectionCallback {

    override fun onResult(
        suggestions: List<BaseSearchSuggestion>,
        results: List<BaseSearchResult>,
        responseInfo: BaseResponseInfo
    ) {
        callback.onResult(
            suggestions.map { it.mapToPlatform() },
            results.map { SearchResult(it) },
            responseInfo.mapToPlatform()
        )
    }

    override fun onError(e: Exception) {
        callback.onError(e)
    }
}
