package com.mapbox.search.common.concurrent

import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

/**
 * Static [MainThreadWorker] instance that works as a central point to access [MainThreadWorker].
 */
public object SearchSdkMainThreadWorker : MainThreadWorker {

    /**
     * [MainThreadWorker] delegate.
     * Can be overridden for testing to provide synchronous [MainThreadWorker].
     */
    public var delegate: MainThreadWorker = AndroidMainThreadWorkerImpl()

    override val isMainThread: Boolean
        get() = delegate.isMainThread

    override val mainExecutor: Executor
        get() = delegate.mainExecutor

    override fun post(runnable: Runnable) {
        delegate.post(runnable)
    }

    override fun postDelayed(delay: Long, unit: TimeUnit, runnable: Runnable) {
        delegate.postDelayed(delay, unit, runnable)
    }

    override fun cancel(runnable: Runnable) {
        delegate.cancel(runnable)
    }

    /**
     * Resets [MainThreadWorker] implementation to Search SDK default.
     */
    public fun resetDelegate() {
        delegate = AndroidMainThreadWorkerImpl()
    }
}
