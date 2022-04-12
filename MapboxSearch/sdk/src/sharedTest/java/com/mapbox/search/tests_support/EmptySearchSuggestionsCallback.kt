package com.mapbox.search.tests_support

import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchSuggestionsCallback
import com.mapbox.search.result.SearchSuggestion

internal object EmptySearchSuggestionsCallback : SearchSuggestionsCallback {
    override fun onSuggestions(suggestions: List<SearchSuggestion>, responseInfo: ResponseInfo) {
        // no op
    }

    override fun onError(e: Exception) {
        // no op
    }
}
