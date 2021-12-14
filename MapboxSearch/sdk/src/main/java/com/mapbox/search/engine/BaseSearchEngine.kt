package com.mapbox.search.engine

import com.mapbox.search.SearchRequestTask
import com.mapbox.search.SearchRequestTaskImpl
import com.mapbox.search.plusAssign
import java.util.concurrent.ExecutorService

internal abstract class BaseSearchEngine {

    protected fun <T> makeRequest(
        callback: T,
        engineExecutor: ExecutorService,
        searchCall: (SearchRequestTaskImpl<T>) -> Unit
    ): SearchRequestTask {
        val request = SearchRequestTaskImpl<T>().apply {
            callbackDelegate = callback
        }

        request += engineExecutor.submit { searchCall(request) }

        return request
    }
}
