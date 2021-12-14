package com.mapbox.search.utils.concurrent

import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

internal class CompletedFuture<T>(private val result: T) : Future<T> {

    override fun cancel(mayInterruptIfRunning: Boolean) = false

    override fun isCancelled() = false

    override fun isDone() = true

    override fun get() = result

    override fun get(timeout: Long, unit: TimeUnit?) = get()
}
