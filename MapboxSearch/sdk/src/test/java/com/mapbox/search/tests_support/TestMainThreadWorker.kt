package com.mapbox.search.tests_support

import com.mapbox.search.common.concurrent.MainThreadWorker
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

internal class TestMainThreadWorker : MainThreadWorker {

    override val isMainThread: Boolean
        get() = true

    override var mainExecutor: Executor = Executor { runnable ->
        runnable.run()
    }

    override fun post(runnable: Runnable) {
        runnable.run()
    }

    override fun postDelayed(delay: Long, unit: TimeUnit, runnable: Runnable) {
        post(runnable)
    }

    override fun cancel(runnable: Runnable) {
        // Do nothing
    }
}
