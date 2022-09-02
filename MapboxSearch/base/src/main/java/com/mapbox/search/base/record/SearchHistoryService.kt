package com.mapbox.search.base.record

import com.mapbox.search.base.result.BaseSearchResult
import com.mapbox.search.base.task.AsyncOperationTaskImpl
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.common.concurrent.SearchSdkMainThreadWorker
import java.util.concurrent.Executor

interface SearchHistoryService {
    fun addToHistoryIfNeeded(
        searchResult: BaseSearchResult,
        executor: Executor = SearchSdkMainThreadWorker.mainExecutor,
        callback: (Result<Boolean>) -> Unit
    ): AsyncOperationTask

    companion object {
        val STUB = object : SearchHistoryService {
            override fun addToHistoryIfNeeded(
                searchResult: BaseSearchResult,
                executor: Executor,
                callback: (Result<Boolean>) -> Unit
            ): AsyncOperationTask {
                executor.execute {
                    callback(Result.success(false))
                }
                return AsyncOperationTaskImpl.COMPLETED
            }
        }
    }
}
