package com.mapbox.search.common.tests

import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

class TestThreadExecutorService : ExecutorService{

    override fun <T : Any?> submit(task: Callable<T>): Future<T> {
        return completed(task.call())
    }

    override fun <T : Any?> submit(task: Runnable, result: T): Future<T> {
        task.run()
        return completed(result)
    }

    override fun submit(task: Runnable): Future<*> {
        task.run()
        return completed(Unit)
    }

    override fun <T : Any> invokeAll(tasks: MutableCollection<out Callable<T>>): List<Future<T>> {
        return tasks.map { completed(it.call()) }
    }

    override fun <T : Any> invokeAll(
        tasks: MutableCollection<out Callable<T>>,
        timeout: Long,
        unit: TimeUnit?
    ): List<Future<T>> {
        return invokeAll(tasks)
    }

    override fun <T : Any?> invokeAny(tasks: MutableCollection<out Callable<T>>): T {
        return tasks.first().call()
    }

    override fun <T : Any?> invokeAny(
        tasks: MutableCollection<out Callable<T>>,
        timeout: Long,
        unit: TimeUnit?
    ): T {
        return invokeAny(tasks)
    }

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

    private companion object {
        fun <T> completed(value: T): Future<T> {
            return object : Future<T> {
                override fun cancel(mayInterruptIfRunning: Boolean) = false

                override fun isCancelled() = false

                override fun isDone() = true

                override fun get(): T = value

                override fun get(timeout: Long, unit: TimeUnit?): T = value
            }
        }
    }
}
