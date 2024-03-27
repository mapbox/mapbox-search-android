package com.mapbox.search.adapter

import com.mapbox.search.SearchResultCallback
import com.mapbox.search.base.BaseResponseInfo
import com.mapbox.search.base.BaseSearchCallback
import com.mapbox.search.base.result.BaseSearchResult
import com.mapbox.search.common.SearchRequestException
import com.mapbox.search.mapToPlatform
import com.mapbox.search.result.SearchResult

internal class SearchResultCallbackAdapter(private val callback: SearchResultCallback) : BaseSearchCallback {

    override fun onResults(results: List<BaseSearchResult>, responseInfo: BaseResponseInfo) =
        if (results.isNotEmpty()) {
            callback.onResult(SearchResult(results.first()), responseInfo.mapToPlatform())
        } else {
            // this should not happen, the server should have returned a 404
            callback.onError(SearchRequestException("Not found", 404))
        }

    override fun onError(e: Exception) {
        callback.onError(e)
    }
}
