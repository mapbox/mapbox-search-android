package com.mapbox.search.base.engine

import com.mapbox.search.base.task.AsyncOperationTaskImpl
import com.mapbox.search.base.task.ExtendedAsyncOperationTask

abstract class BaseSearchEngine {

    protected fun <T> makeRequest(
        callback: T,
        searchCall: (AsyncOperationTaskImpl<T>) -> Unit
    ): ExtendedAsyncOperationTask<T> {
        val task = AsyncOperationTaskImpl<T>().apply {
            callbackDelegate = callback
        }
        searchCall(task)
        return task
    }
}
