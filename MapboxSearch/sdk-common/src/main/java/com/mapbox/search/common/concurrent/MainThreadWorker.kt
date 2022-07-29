package com.mapbox.search.common.concurrent

import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

/**
 * A task executor that works on the main thread.
 *
 * @see SearchSdkMainThreadWorker
 */
public interface MainThreadWorker {

    /**
     * True if the current thread is the main thread, false otherwise.
     */
    public val isMainThread: Boolean

    /**
     * Executor that will run enqueued tasks on the main thread.
     */
    public val mainExecutor: Executor

    /**
     * Posts passed runnable to the main thread.
     *
     * @param runnable The runnable to run on the main thread.
     */
    public fun post(runnable: Runnable)

    /**
     * Posts passed runnable to the main thread to be run after the specified amount of time elapses.
     *
     * @param delay The delay until the Runnable will be executed.
     * @param unit The unit of the delay. Default is milliseconds.
     * @param runnable The runnable to run on the main thread.
     */
    public fun postDelayed(delay: Long, unit: TimeUnit = TimeUnit.MILLISECONDS, runnable: Runnable)

    /**
     * Remove any pending posts of Runnable that are in the message queue.
     */
    public fun cancel(runnable: Runnable)
}
