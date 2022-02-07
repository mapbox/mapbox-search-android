package com.mapbox.search.common

import com.mapbox.search.common.logger.loge
import com.mapbox.search.common.logger.logw

inline fun assertDebug(value: Boolean, message: () -> Any) {
    if (BuildConfig.DEBUG) {
        check(value, message)
    }

    if (!value) {
        logw(message().toString())
        @Suppress("DEPRECATION")
        reportError(IllegalStateException(message().toString()))
    }
}

inline fun failDebug(cause: Throwable? = null, message: () -> Any) {
    val exception = IllegalStateException(message().toString(), cause)
    if (BuildConfig.DEBUG) {
        throw exception
    } else {
        @Suppress("DEPRECATION")
        reportError(exception)
    }
    logw(message().toString())
}

inline fun throwDebug(e: Throwable? = null, message: () -> Any = { "Error!" }) {
    val exception = IllegalStateException(message().toString(), e)
    if (BuildConfig.DEBUG) {
        throw exception
    }
    if (e != null) {
        logw(e, message().toString())
    } else {
        logw(message().toString())
    }
}

fun reportRelease(e: Throwable, message: String) {
    reportRelease(e) {
        message
    }
}

inline fun reportRelease(e: Throwable, message: () -> Any = { "Error!" }) {
    loge(e, message().toString())
    if (!BuildConfig.DEBUG) {
        @Suppress("DEPRECATION")
        reportError(e)
    }
}

@Deprecated(
    """
        Do not use this method inside SDK code. 
        Better use a more suitable "reportRelease(e)" method. 
    """,
    replaceWith = ReplaceWith("reportRelease(e)"))
fun reportError(e: Throwable) {
    val reporter = CommonErrorsReporter.reporter
    if (reporter != null) {
        reporter(e)
    } else {
        logw("Errors reported is not initialized")
    }
}
