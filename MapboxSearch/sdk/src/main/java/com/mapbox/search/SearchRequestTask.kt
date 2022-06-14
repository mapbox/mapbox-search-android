package com.mapbox.search

import java.util.concurrent.Executor
import java.util.concurrent.Future

/**
 * Represents any request from any search engine. Should be cancelled if you leave the screen or application and result of search request do not needed anymore to avoid memory leaks.
 * @see SearchEngine
 */
public interface SearchRequestTask {

    /**
     * Denotes whether this task completed.
     */
    public val isDone: Boolean

    /**
     * Denotes whether this task was cancelled.
     */
    public val isCancelled: Boolean

    /**
     * Cancels request.
     */
    public fun cancel()
}

internal class SearchRequestTaskImpl<T>(delegate: T? = null) : SearchRequestTask {

    private val innerTasks: MutableList<SearchRequestTask> = mutableListOf()

    var callbackDelegate: T? = null
        @Synchronized
        set(value) {
            field = if (isCancelled || isDone) {
                null
            } else {
                value
            }
        }
        @Synchronized get

    var callbackActionExecuted: Boolean = false
        @Synchronized private set
        @Synchronized get

    override var isDone: Boolean = false
        @Synchronized private set
        @Synchronized get

    override var isCancelled: Boolean = false
        @Synchronized private set
        @Synchronized get

    val isCompleted: Boolean
        @Synchronized get() = isCancelled || isDone

    init {
        callbackDelegate = delegate
    }

    @Synchronized
    fun addInnerTask(task: SearchRequestTask) {
        if (isDone) {
            return
        }

        if (isCancelled) {
            task.cancel()
            return
        }

        innerTasks += task
    }

    @Synchronized
    override fun cancel() {
        if (isCompleted) {
            return
        }

        isCancelled = true
        innerTasks.forEach {
            it.cancel()
        }

        callbackDelegate = null
    }

    @Synchronized
    fun markCancelledAndRunOnCallback(action: T.() -> Unit) {
        if (isCompleted) {
            return
        }

        val delegate = callbackDelegate
        callbackDelegate = null

        cancel()

        delegate ?: return
        callbackActionExecuted = true
        action(delegate)
    }

    @Synchronized
    fun markExecutedAndRunOnCallback(action: T.() -> Unit) {
        if (isCancelled) {
            return
        }

        isDone = true

        val delegate = callbackDelegate ?: return
        callbackDelegate = null
        callbackActionExecuted = true
        action(delegate)
    }

    companion object {

        fun completed(): SearchRequestTaskImpl<*> {
            return SearchRequestTaskImpl<Any>().apply {
                markExecutedAndRunOnCallback {
                    // Nothing to do
                }
            }
        }
    }
}

internal fun <T> SearchRequestTaskImpl<T>.markExecutedAndRunOnCallback(executor: Executor, action: T.() -> Unit) {
    executor.execute {
        markExecutedAndRunOnCallback(action)
    }
}

internal fun <T> SearchRequestTaskImpl<T>.markCancelledAndRunOnCallback(executor: Executor, action: T.() -> Unit) {
    executor.execute {
        markCancelledAndRunOnCallback(action)
    }
}

internal class FutureSearchRequestTask(
    private val future: Future<*>
) : SearchRequestTask {

    override val isCancelled: Boolean
        @Synchronized get() = future.isCancelled

    override val isDone: Boolean
        @Synchronized get() = future.isDone

    @Synchronized
    override fun cancel() {
        if (isCancelled || isDone) {
            return
        }

        future.cancel(true)
    }
}

/**
 * Note: [AsyncOperationTask] operations are thread-safe, so no synchronization is needed.
 */
internal class SearchRequestTaskAsyncAdapter(
    private val asyncTask: AsyncOperationTask
) : SearchRequestTask {

    override val isCancelled: Boolean
        get() = asyncTask.isCancelled

    override val isDone: Boolean
        get() = asyncTask.isDone

    override fun cancel() {
        asyncTask.cancel()
    }
}

internal operator fun <T> SearchRequestTaskImpl<T>.plusAssign(task: Future<*>) {
    addInnerTask(FutureSearchRequestTask(task))
}

internal operator fun <T> SearchRequestTaskImpl<T>.plusAssign(task: AsyncOperationTask) {
    addInnerTask(SearchRequestTaskAsyncAdapter(task))
}
