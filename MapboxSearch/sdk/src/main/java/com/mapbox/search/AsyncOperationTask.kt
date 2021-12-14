package com.mapbox.search

import com.mapbox.common.Cancelable
import com.mapbox.search.task.CancelableWrapper
import java.util.concurrent.Future

internal operator fun AsyncOperationTaskImpl.plusAssign(cancelableWrapper: CancelableWrapper) {
    addInnerTask(cancelableWrapper)
}

internal operator fun AsyncOperationTaskImpl.plusAssign(task: AsyncOperationTask) {
    addInnerTask(CancelableWrapper.fromTask(task))
}

internal operator fun AsyncOperationTaskImpl.plusAssign(cancelable: Cancelable) {
    addInnerTask(CancelableWrapper.fromMapboxCommonCancellable(cancelable))
}

internal operator fun AsyncOperationTaskImpl.plusAssign(future: Future<*>) {
    addInnerTask(CancelableWrapper.fromFuture(future))
}

internal fun AsyncOperationTaskImpl.runIfNotCancelled(block: AsyncOperationTaskImpl.() -> Unit) {
    if (!isCancelled) {
        block()
    }
}

/**
 * Represents the result (task handle) of an asynchronous operation.
 * @see [com.mapbox.search.record.LocalDataProvider]
 * @see [OfflineSearchEngine]
 */
public interface AsyncOperationTask {

    /**
     * Denotes whether this task completed.
     */
    public val isDone: Boolean

    /**
     * Denotes whether this task was cancelled.
     */
    public val isCancelled: Boolean

    /**
     * Attempts to cancel execution of this task.
     */
    public fun cancel()
}

internal class AsyncOperationTaskImpl : AsyncOperationTask {

    private val cancelableList: MutableList<CancelableWrapper> = mutableListOf()

    var onCancelCallback: (() -> Unit)? = null
        @Synchronized
        set(value) {
            field = if (isDone || isCancelled) {
                null
            } else {
                value
            }
        }
        @Synchronized get

    override var isDone: Boolean = false
        @Synchronized private set
        @Synchronized get

    override var isCancelled: Boolean = false
        @Synchronized private set
        @Synchronized get

    @Synchronized
    fun addInnerTask(cancelable: CancelableWrapper) {
        if (isDone) {
            return
        }

        if (isCancelled) {
            cancelable.cancel()
            return
        }

        cancelableList.add(cancelable)
    }

    @Synchronized
    fun onComplete() {
        if (isDone || isCancelled) {
            return
        }

        isDone = true
        onCancelCallback = null
    }

    @Synchronized
    override fun cancel() {
        if (isDone || isCancelled) {
            return
        }

        cancelableList.forEach { it.cancel() }
        cancelableList.clear()
        onCancelCallback?.invoke()
        onCancelCallback = null
        isCancelled = true
    }
}

internal object CompletedAsyncOperationTask : AsyncOperationTask {

    override val isDone: Boolean
        get() = true

    override val isCancelled: Boolean
        get() = false

    override fun cancel() {
        // do nothing
    }
}
