package com.mapbox.search.ui.utils

import com.mapbox.search.CompletionCallback
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.record.IndexableDataProvider
import com.mapbox.search.record.IndexableRecord

internal class CompoundIndexableDataProvider<T1 : IndexableRecord, T2 : IndexableRecord>(
    private val provider1: IndexableDataProvider<T1>,
    private val provider2: IndexableDataProvider<T2>
) {

    fun getAll(callback: CompletionCallback<Pair<List<T1>, List<T2>>>): AsyncOperationTask {
        var resultSent = false
        var res1: List<T1>? = null
        var res2: List<T2>? = null

        val task1 = provider1.getAll(object : CompletionCallback<List<T1>> {
            override fun onComplete(result: List<T1>) {
                res1 = result
                val otherResult = res2
                if (!resultSent && otherResult != null) {
                    callback.onComplete(result to otherResult)
                    resultSent = true
                }
            }

            override fun onError(e: Exception) {
                if (!resultSent) {
                    callback.onError(e)
                    resultSent = true
                }
            }
        })

        val task2 = provider2.getAll(object : CompletionCallback<List<T2>> {
            override fun onComplete(result: List<T2>) {
                res2 = result
                val otherResult = res1
                if (!resultSent && otherResult != null) {
                    callback.onComplete(otherResult to result)
                    resultSent = true
                }
            }

            override fun onError(e: Exception) {
                if (!resultSent) {
                    callback.onError(e)
                    resultSent = true
                }
            }
        })

        return CompoundAsyncOperationTask(task1, task2)
    }

    private class CompoundAsyncOperationTask(vararg val tasks: AsyncOperationTask) : AsyncOperationTask {

        override val isDone: Boolean
            get() = tasks.all { it.isDone }

        override val isCancelled: Boolean
            get() = tasks.any { it.isCancelled }

        override fun cancel() {
            tasks.forEach { it.cancel() }
        }
    }
}
