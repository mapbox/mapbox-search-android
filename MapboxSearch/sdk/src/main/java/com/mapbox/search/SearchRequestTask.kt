package com.mapbox.search

import java.util.concurrent.Executor
import java.util.concurrent.Future

/**
 * Represents any request from any search engine. Should be cancelled if you leave the screen or application and result of search request do not needed anymore to avoid memory leaks.
 * @see SearchEngine
 * @see CategorySearchEngine
 * @see ReverseGeocodingSearchEngine
 */
// TODO(#224): replace with AsyncOperationTask and expose isCancelled/isExecuted properties
public interface SearchRequestTask {

    /*
    Now we can't implement isCancelled flag because there's no reliable way to determine if request has been cancelled by the native core.
    With the similar reason we don't make public isExecuted flag because cancelled request won't be executed
    and isExecuted will always be false, which can be confusing for customers.

    val isCancelled: Boolean
    val isExecuted: Boolean
     */

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
            field = if (isCanceled || isExecuted) {
                null
            } else {
                value
            }
        }
        @Synchronized get

    var callbackActionExecuted: Boolean = false
        @Synchronized private set
        @Synchronized get

    var isCanceled: Boolean = false
        @Synchronized private set
        @Synchronized get

    var isExecuted: Boolean = false
        @Synchronized private set
        @Synchronized get

    val isCompleted: Boolean
        @Synchronized get() = isCanceled || isExecuted

    init {
        callbackDelegate = delegate
    }

    @Synchronized
    fun addInnerTask(task: SearchRequestTask) {
        if (isExecuted) {
            return
        }

        if (isCanceled) {
            task.cancel()
            return
        }

        innerTasks += task
    }

    @Synchronized
    override fun cancel() {
        if (isExecuted) {
            return
        }

        isCanceled = true
        innerTasks.forEach {
            it.cancel()
        }

        callbackDelegate = null
    }

    @Synchronized
    fun markExecutedAndRunOnCallback(action: T.() -> Unit) {
        if (isCanceled) {
            return
        }

        isExecuted = true

        val delegate = callbackDelegate
        callbackDelegate = null

        if (delegate != null) {
            callbackActionExecuted = true
            action(delegate)
        }
    }

    // We don't actually cancel any request because native core doesn't support it yet,
    // but we need to mark previously made request as cancelled
    // because every new submitted to the same SearchEngineInterface request cancels previous one
    @Synchronized
    fun markCancelled() {
        cancel()
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

internal class FutureSearchRequestTask(
    private val future: Future<*>
) : SearchRequestTask {

    val isCanceled: Boolean
        @Synchronized get() = future.isCancelled

    private val isExecuted: Boolean
        get() = future.isDone

    @Synchronized
    override fun cancel() {
        if (isCanceled || isExecuted) {
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

    val isCanceled: Boolean
        get() = asyncTask.isCancelled

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
