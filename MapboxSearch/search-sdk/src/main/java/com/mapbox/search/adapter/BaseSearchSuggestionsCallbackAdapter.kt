package com.mapbox.search.adapter

import com.mapbox.search.SearchSuggestionsCallback
import com.mapbox.search.base.BaseResponseInfo
import com.mapbox.search.base.BaseSearchSuggestionsCallback
import com.mapbox.search.base.result.BaseSearchSuggestion
import com.mapbox.search.mapToPlatform
import com.mapbox.search.result.mapToPlatform

internal open class BaseSearchSuggestionsCallbackAdapter(
    private val callback: SearchSuggestionsCallback
) : BaseSearchSuggestionsCallback {
    override fun onSuggestions(suggestions: List<BaseSearchSuggestion>, responseInfo: BaseResponseInfo) {
        callback.onSuggestions(
            suggestions.map { it.mapToPlatform() },
            responseInfo.mapToPlatform()
        )
    }

    override fun onError(e: Exception) {
        callback.onError(e)
    }
}
