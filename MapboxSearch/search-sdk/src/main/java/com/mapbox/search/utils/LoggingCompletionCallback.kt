package com.mapbox.search.utils

import com.mapbox.search.base.logger.logd
import com.mapbox.search.common.CompletionCallback

internal class LoggingCompletionCallback(private val operationName: String) : CompletionCallback<Unit> {
    override fun onComplete(result: Unit) {
        logd("$operationName completed")
    }

    override fun onError(e: Exception) {
        logd("$operationName error: ${e.message}")
    }
}
