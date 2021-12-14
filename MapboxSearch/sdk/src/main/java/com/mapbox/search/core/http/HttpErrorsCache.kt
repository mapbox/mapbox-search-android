package com.mapbox.search.core.http

import androidx.annotation.AnyThread
import java.util.concurrent.ConcurrentHashMap

@AnyThread
internal interface HttpErrorsCache {
    fun put(requestId: Int, e: Exception)
    fun getAndRemove(requestId: Int): Exception?
}

internal class HttpErrorsCacheImpl : HttpErrorsCache {

    private val map = ConcurrentHashMap<Int, Exception>()

    override fun put(requestId: Int, e: Exception) {
        map[requestId] = e
    }

    override fun getAndRemove(requestId: Int): Exception? {
        return map.remove(requestId)
    }
}
