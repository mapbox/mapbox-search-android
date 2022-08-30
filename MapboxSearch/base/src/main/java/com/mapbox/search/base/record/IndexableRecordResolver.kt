package com.mapbox.search.base.record

import com.mapbox.search.base.task.AsyncOperationTaskImpl
import com.mapbox.search.common.AsyncOperationTask
import java.util.concurrent.Executor

interface IndexableRecordResolver {
    fun resolve(
        dataProviderName: String,
        userRecordId: String,
        executor: Executor,
        callback: (Result<BaseIndexableRecord>) -> Unit
    ): AsyncOperationTask

    companion object {
        val EMPTY = object : IndexableRecordResolver {
            override fun resolve(
                dataProviderName: String,
                userRecordId: String,
                executor: Executor,
                callback: (Result<BaseIndexableRecord>) -> Unit
            ): AsyncOperationTask {
                executor.execute {
                    callback(Result.failure(Exception("Not found")))
                }
                return AsyncOperationTaskImpl.COMPLETED
            }
        }
    }
}
