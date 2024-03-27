package com.mapbox.search.tests_support

import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchResultCallback
import com.mapbox.search.common.tests.BaseBlockingCallback
import com.mapbox.search.result.SearchResult

internal class BlockingSearchResultCallback : SearchResultCallback, BaseBlockingCallback<Any>() {

    override fun onResult(result: SearchResult, responseInfo: ResponseInfo) {
        publishResult(result)
    }

    override fun onError(e: Exception) {
        publishResult(e)
    }
}
