package com.mapbox.search.base.core

import com.mapbox.search.base.utils.UserAgentProvider
import com.mapbox.search.internal.bindgen.UserActivityReporter
import com.mapbox.search.internal.bindgen.UserActivityReporterOptions

fun getUserActivityReporter(
    eventsUrl: String? = null
): UserActivityReporter {
    val options = UserActivityReporterOptions(
        UserAgentProvider.sdkInformation(),
        eventsUrl
    )
    return UserActivityReporter.getOrCreate(options)
}
