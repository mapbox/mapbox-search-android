package com.mapbox.search.engine

import com.mapbox.search.SearchRequestTask
import com.mapbox.search.SearchRequestTaskImpl
import com.mapbox.search.plusAssign
import java.lang.ref.WeakReference
import java.util.concurrent.ExecutorService

internal abstract class BaseSearchEngine(
    /**
     * TODO Temporary solution for https://github.com/mapbox/mapbox-search-sdk/issues/830
     */
    private val autoCancelPreviousRequest: Boolean = false
) {

    private var previousRequestTask: WeakReference<SearchRequestTask>? = null

    protected fun <T> makeRequest(
        callback: T,
        engineExecutor: ExecutorService,
        searchCall: (SearchRequestTaskImpl<T>) -> Unit
    ): SearchRequestTask {
        val task = SearchRequestTaskImpl<T>().apply {
            callbackDelegate = callback
        }

        task += engineExecutor.submit {
            searchCall(task)
            if (autoCancelPreviousRequest) {
                previousRequestTask?.get()?.cancel()
                previousRequestTask = WeakReference(task)
            }
        }

        return task
    }
}
