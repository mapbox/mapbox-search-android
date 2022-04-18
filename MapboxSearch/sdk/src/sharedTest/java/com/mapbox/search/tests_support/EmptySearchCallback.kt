package com.mapbox.search.tests_support

import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchCallback
import com.mapbox.search.result.SearchResult

internal object EmptySearchCallback : SearchCallback {
    override fun onResults(results: List<SearchResult>, responseInfo: ResponseInfo) {
        // no op
    }

    override fun onError(e: Exception) {
        // no op
    }
}
