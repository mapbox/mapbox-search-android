package com.mapbox.search.base

import com.mapbox.search.base.logger.loge
import com.mapbox.search.base.logger.logw

inline fun assertDebug(value: Boolean, message: () -> Any) {
    if (BuildConfig.DEBUG) {
        check(value, message)
    }

    if (!value) {
        logw(message().toString())
    }
}

inline fun failDebug(cause: Throwable? = null, message: () -> Any) {
    val exception = IllegalStateException(message().toString(), cause)
    if (BuildConfig.DEBUG) {
        throw exception
    }
    logw(message().toString())
}

inline fun throwDebug(e: Throwable? = null, message: () -> Any = { "Error!" }) {
    val exception = IllegalStateException(message().toString(), e)
    if (BuildConfig.DEBUG) {
        throw exception
    }
    loge(message().toString())
}
