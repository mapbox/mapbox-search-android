package com.mapbox.search.base

import com.mapbox.search.base.result.BaseSearchResult

interface BaseSearchCallback {
    fun onResults(results: List<BaseSearchResult>, responseInfo: BaseResponseInfo)
    fun onError(e: Exception)
}
