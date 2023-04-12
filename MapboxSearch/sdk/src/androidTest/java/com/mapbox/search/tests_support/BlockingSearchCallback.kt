package com.mapbox.search.tests_support

import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchCallback
import com.mapbox.search.result.SearchResult
import java.util.concurrent.CountDownLatch

internal class BlockingSearchCallback : SearchCallback {

    private lateinit var result: SearchEngineResult
    private var countDownLatch = CountDownLatch(1)

    override fun onResults(results: List<SearchResult>, responseInfo: ResponseInfo) {
        result = SearchEngineResult.Results(results, responseInfo)
        countDownLatch.countDown()
    }

    override fun onError(e: Exception) {
        result = SearchEngineResult.Error(e)
        countDownLatch.countDown()
    }

    fun getResultBlocking(): SearchEngineResult {
        countDownLatch.await()
        return result
    }

    fun reset() {
        countDownLatch = CountDownLatch(1)
    }

    sealed class SearchEngineResult {

        fun requireResults() = (this as Results).results

        data class Results(val results: List<SearchResult>, val responseInfo: ResponseInfo) : SearchEngineResult()
        data class Error(val e: Exception) : SearchEngineResult()
    }
}
