package com.mapbox.search.base.task

import com.mapbox.common.Cancelable
import com.mapbox.search.common.AsyncOperationTask
import java.util.concurrent.Executor
import java.util.concurrent.Future

interface ExtendedAsyncOperationTask<T> : AsyncOperationTask {

    val isCompleted: Boolean
        get() = isCancelled || isDone

    val callbackDelegate: T?

    val callbackActionExecuted: Boolean

    fun addInnerTask(cancelable: CancelableWrapper)

    fun onComplete()

    fun markCancelledAndRunOnCallback(action: T.() -> Unit)

    fun markExecutedAndRunOnCallback(action: T.() -> Unit)

    operator fun plusAssign(cancelableWrapper: CancelableWrapper) {
        addInnerTask(cancelableWrapper)
    }

    operator fun plusAssign(task: AsyncOperationTask) {
        addInnerTask(CancelableWrapper.fromTask(task))
    }

    operator fun plusAssign(cancelable: Cancelable) {
        addInnerTask(CancelableWrapper.fromMapboxCommonCancellable(cancelable))
    }

    operator fun plusAssign(future: Future<*>) {
        addInnerTask(CancelableWrapper.fromFuture(future))
    }

    fun runIfNotCancelled(block: ExtendedAsyncOperationTask<*>.() -> Unit) {
        if (!isCancelled) {
            block()
        }
    }

    fun markExecutedAndRunOnCallback(executor: Executor, action: T.() -> Unit) {
        executor.execute {
            markExecutedAndRunOnCallback(action)
        }
    }

    fun markCancelledAndRunOnCallback(executor: Executor, action: T.() -> Unit) {
        executor.execute {
            markCancelledAndRunOnCallback(action)
        }
    }
}
