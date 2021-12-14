package com.mapbox.search.tests_support

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

internal abstract class BaseBlockingCallback<T : Any> {

    private lateinit var result: T
    private var countDownLatch = CountDownLatch(1)

    protected fun publishResult(result: T) {
        this.result = result
        countDownLatch.countDown()
    }

    fun getResultBlocking(timeout: Long = Long.MAX_VALUE, timeUnit: TimeUnit = TimeUnit.MILLISECONDS): T {
        countDownLatch.await(timeout, timeUnit)
        return result
    }

    fun reset() {
        countDownLatch = CountDownLatch(1)
    }
}
