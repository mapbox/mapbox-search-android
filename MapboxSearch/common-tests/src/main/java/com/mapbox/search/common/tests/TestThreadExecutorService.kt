package com.mapbox.search.common.tests

import java.util.concurrent.AbstractExecutorService
import java.util.concurrent.TimeUnit

class TestThreadExecutorService : AbstractExecutorService() {

    override fun isTerminated() = false

    override fun execute(command: Runnable) {
        command.run()
    }

    override fun shutdown() {
        // do nothing
    }

    override fun shutdownNow() = mutableListOf<Runnable>()

    override fun isShutdown() = false

    override fun awaitTermination(timeout: Long, unit: TimeUnit) = false
}
