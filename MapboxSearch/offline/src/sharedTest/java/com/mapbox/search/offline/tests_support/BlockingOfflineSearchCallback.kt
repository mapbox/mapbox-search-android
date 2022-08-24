package com.mapbox.search.offline.tests_support

import com.mapbox.search.common.BaseBlockingCallback
import com.mapbox.search.offline.OfflineResponseInfo
import com.mapbox.search.offline.OfflineSearchCallback
import com.mapbox.search.offline.OfflineSearchResult

internal class BlockingOfflineSearchCallback :
    BaseBlockingCallback<BlockingOfflineSearchCallback.SearchEngineResult>(),
    OfflineSearchCallback {

    override fun onResults(results: List<OfflineSearchResult>, responseInfo: OfflineResponseInfo) {
        publishResult(SearchEngineResult.Results(results))
    }

    override fun onError(e: Exception) {
        publishResult(SearchEngineResult.Error(e))
    }

    sealed class SearchEngineResult {
        fun requireResults() = (this as Results).results

        data class Results(val results: List<OfflineSearchResult>) : SearchEngineResult()
        data class Error(val e: Exception) : SearchEngineResult()
    }
}
