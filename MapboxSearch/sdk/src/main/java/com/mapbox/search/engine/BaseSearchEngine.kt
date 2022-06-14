package com.mapbox.search.engine

import com.mapbox.search.SearchRequestTask
import com.mapbox.search.SearchRequestTaskImpl
import com.mapbox.search.plusAssign

internal abstract class BaseSearchEngine {

    protected fun <T> makeRequest(
        callback: T,
        searchCall: (SearchRequestTaskImpl<T>) -> Unit
    ): SearchRequestTask {
        val task = SearchRequestTaskImpl<T>().apply {
            callbackDelegate = callback
        }
        searchCall(task)
        return task
    }
}
