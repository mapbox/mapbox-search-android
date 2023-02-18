package com.mapbox.search.base.core

import com.mapbox.search.base.utils.UserAgentProvider
import com.mapbox.search.internal.bindgen.UserActivityReporter
import com.mapbox.search.internal.bindgen.UserActivityReporterOptions

fun getUserActivityReporter(
    accessToken: String,
    userAgent: String = UserAgentProvider.userAgent,
    eventsUrl: String? = null
): UserActivityReporter {
    val options = UserActivityReporterOptions(
        accessToken, userAgent, eventsUrl
    )
    return UserActivityReporter.getOrCreate(options)
}
