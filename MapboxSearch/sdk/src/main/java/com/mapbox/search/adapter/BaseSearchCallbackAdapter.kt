package com.mapbox.search.adapter

import com.mapbox.search.SearchCallback
import com.mapbox.search.base.BaseResponseInfo
import com.mapbox.search.base.BaseSearchCallback
import com.mapbox.search.base.result.BaseSearchResult
import com.mapbox.search.mapToPlatform
import com.mapbox.search.result.mapToPlatform

internal class BaseSearchCallbackAdapter(private val callback: SearchCallback) : BaseSearchCallback {
    override fun onResults(results: List<BaseSearchResult>, responseInfo: BaseResponseInfo) {
        callback.onResults(results.map { it.mapToPlatform() }, responseInfo.mapToPlatform())
    }

    override fun onError(e: Exception) {
        callback.onError(e)
    }
}
