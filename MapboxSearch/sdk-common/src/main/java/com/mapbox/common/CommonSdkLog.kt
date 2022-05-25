package com.mapbox.common

import androidx.annotation.VisibleForTesting

/**
 * This class in a "com.mapbox.common" package
 * because we need to access package-private com.mapbox.common.Log class from the Common SDK.
 */
internal object CommonSdkLog {

    private const val SDK_IDENTIFIER = "search-sdk-android"

    private var logger: LogImpl? = CommonSdkLogImpl()

    /**
     * Resets implementation that uses Common SDK class which can't be loaded in unit tests.
     */
    @VisibleForTesting
    fun resetLogImpl() {
        logger = null
    }

    @VisibleForTesting
    fun reinitializeLogImpl() {
        logger = CommonSdkLogImpl()
    }

    fun logd(tag: String?, message: String) {
        logger?.logd(tag, message)
    }

    fun logi(tag: String?, message: String) {
        logger?.logi(tag, message)
    }

    fun logw(tag: String?, message: String) {
        logger?.logw(tag, message)
    }

    fun loge(tag: String?, message: String) {
        logger?.loge(tag, message)
    }

    private fun formatCategory(tag: String?): String {
        return when (tag) {
            null -> SDK_IDENTIFIER
            else -> "$SDK_IDENTIFIER\\$tag"
        }
    }

    private interface LogImpl {
        fun logd(tag: String?, message: String)

        fun logi(tag: String?, message: String)

        fun logw(tag: String?, message: String)

        fun loge(tag: String?, message: String)
    }

    private class CommonSdkLogImpl : LogImpl {
        override fun logd(tag: String?, message: String) {
            Log.debug(message, formatCategory(tag))
        }

        override fun logi(tag: String?, message: String) {
            Log.info(message, formatCategory(tag))
        }

        override fun logw(tag: String?, message: String) {
            Log.warning(message, formatCategory(tag))
        }

        override fun loge(tag: String?, message: String) {
            Log.error(message, formatCategory(tag))
        }
    }
}
