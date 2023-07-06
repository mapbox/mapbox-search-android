package com.mapbox.search.base.utils

import com.mapbox.search.common.BuildConfig

object UserAgentProvider {

    const val sdkPackageName = "com.mapbox.search"
    const val sdkName = "search-sdk-android"
    const val sdkVersionName = BuildConfig.VERSION_NAME

    val userAgent = if (BuildConfig.DEBUG) {
        "$sdkName-internal/$sdkVersionName"
    } else {
        "$sdkName/$sdkVersionName"
    }
}
