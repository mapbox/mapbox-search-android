package com.mapbox.search.offline

import com.mapbox.search.base.BaseResponseInfo
import com.mapbox.search.base.BaseSearchCallback
import com.mapbox.search.base.result.BaseSearchResult

internal class OfflineSearchCallbackAdapter(private val callback: OfflineSearchCallback) : BaseSearchCallback {
    override fun onResults(results: List<BaseSearchResult>, responseInfo: BaseResponseInfo) {
        callback.onResults(
            results.map { OfflineSearchResult(it.rawSearchResult) },
            OfflineResponseInfo(responseInfo.requestOptions.core.mapToOfflineSdkType())
        )
    }

    override fun onError(e: Exception) {
        callback.onError(e)
    }
}
