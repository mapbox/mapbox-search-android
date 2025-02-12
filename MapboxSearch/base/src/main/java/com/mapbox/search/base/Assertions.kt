package com.mapbox.search.base

import androidx.annotation.VisibleForTesting
import com.mapbox.search.base.logger.loge
import com.mapbox.search.base.logger.logw

@VisibleForTesting
interface SdkAssertion {

    fun assertDebug(value: Boolean, message: () -> Any)
    fun failDebug(cause: Throwable?, message: () -> Any)
    fun throwDebug(e: Throwable? = null, message: () -> Any = { "Error!" })

    companion object {

        val IMPL: SdkAssertion = SdkAssertionImpl()
    }
}

internal class SdkAssertionImpl : SdkAssertion {
    override fun assertDebug(value: Boolean, message: () -> Any) {
        if (BuildConfig.DEBUG) {
            check(value, message)
        }

        if (!value) {
            logw(message().toString())
        }
    }

    override fun failDebug(cause: Throwable?, message: () -> Any) {
        val exception = IllegalStateException(message().toString(), cause)
        if (BuildConfig.DEBUG) {
            throw exception
        }
        logw(message().toString())
    }

    override fun throwDebug(e: Throwable?, message: () -> Any) {
        val exception = IllegalStateException(message().toString(), e)
        if (BuildConfig.DEBUG) {
            throw exception
        }
        loge(message().toString())
    }
}

fun assertDebug(value: Boolean, message: () -> Any) {
    SdkAssertion.IMPL.assertDebug(value, message)
}

fun failDebug(cause: Throwable? = null, message: () -> Any) {
    SdkAssertion.IMPL.failDebug(cause, message)
}

fun throwDebug(e: Throwable? = null, message: () -> Any = { "Error!" }) {
    SdkAssertion.IMPL.throwDebug(e, message)
}
