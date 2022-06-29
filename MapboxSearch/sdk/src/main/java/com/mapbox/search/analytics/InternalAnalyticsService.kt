package com.mapbox.search.analytics

import com.mapbox.search.common.BuildConfig
import com.mapbox.search.common.logger.loge

internal interface InternalAnalyticsService : AnalyticsService {
    fun reportError(throwable: Throwable)
    fun setAccessToken(accessToken: String)
}

@JvmSynthetic
internal fun InternalAnalyticsService.reportRelease(e: Throwable, message: String = "Error occurred") {
    loge("$message. Error: ${e.message}")
    if (!BuildConfig.DEBUG) {
        reportError(e)
    }
}
