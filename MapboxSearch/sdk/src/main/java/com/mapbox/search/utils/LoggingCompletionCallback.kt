package com.mapbox.search.utils

import com.mapbox.search.CompletionCallback
import com.mapbox.search.common.logger.logd

internal class LoggingCompletionCallback(private val operationName: String) : CompletionCallback<Unit> {
    override fun onComplete(result: Unit) {
        logd("$operationName completed")
    }

    override fun onError(e: Exception) {
        logd("$operationName error: ${e.message}")
    }
}
