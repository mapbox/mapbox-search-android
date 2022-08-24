package com.mapbox.search.base.utils

import com.mapbox.search.common.BuildConfig

object UserAgentProvider {
    val userAgent = if (BuildConfig.DEBUG) {
        "search-sdk-android-internal/${BuildConfig.VERSION_NAME}"
    } else {
        "search-sdk-android/${BuildConfig.VERSION_NAME}"
    }
}
