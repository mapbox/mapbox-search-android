package com.mapbox.search.utils.concurrent

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

internal class AndroidMainThreadWorkerImpl(
    private val mainHandler: Handler = Handler(Looper.getMainLooper())
) : MainThreadWorker {

    override val isMainThread: Boolean
        get() = Looper.myLooper() == Looper.getMainLooper()

    override val mainExecutor: Executor = Executor { runnable ->
        post(runnable)
    }

    override fun post(runnable: Runnable) {
        if (isMainThread) {
            runnable.run()
        } else {
            mainHandler.post(runnable)
        }
    }

    override fun postDelayed(delay: Long, unit: TimeUnit, runnable: Runnable) {
        mainHandler.postDelayed(runnable, unit.toMillis(delay))
    }

    override fun cancel(runnable: Runnable) {
        mainHandler.removeCallbacks(runnable)
    }
}
