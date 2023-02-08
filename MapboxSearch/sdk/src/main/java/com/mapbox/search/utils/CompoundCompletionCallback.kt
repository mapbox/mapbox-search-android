package com.mapbox.search.utils

import com.mapbox.search.base.task.AsyncOperationTaskImpl
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.common.CompletionCallback
import java.util.concurrent.Executor

internal class CompoundCompletionCallback<T>(
    private val numberOfTasks: Int,
    private val callbackExecutor: Executor,
    private val resultingCallback: CompletionCallback<T>,
) : CompletionCallback<T> {

    private val completions = mutableListOf<T>()
    private val tasks = mutableListOf<AsyncOperationTask>()

    private val compoundTask = AsyncOperationTaskImpl<Any>()

    fun getCompoundTask(): AsyncOperationTask = compoundTask

    fun addInnerTask(task: AsyncOperationTask) {
        compoundTask += task
        tasks += task
    }

    override fun onComplete(result: T) {
        if (compoundTask.isCompleted) {
            return
        }
        completions += result
        if (completions.size == numberOfTasks) {
            callbackExecutor.execute {
                compoundTask.onComplete()
                resultingCallback.onComplete(result)
            }
        }
    }

    override fun onError(e: Exception) {
        if (compoundTask.isCompleted) {
            return
        }
        tasks.forEach { it.cancel() }

        callbackExecutor.execute {
            compoundTask.onComplete()
            resultingCallback.onError(e)
        }
    }
}
