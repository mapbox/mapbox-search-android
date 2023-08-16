package com.mapbox.search.base.core

import com.mapbox.search.base.utils.UserAgentProvider
import com.mapbox.search.internal.bindgen.UserActivityReporter
import com.mapbox.search.internal.bindgen.UserActivityReporterOptions

fun getUserActivityReporter(
    eventsUrl: String? = null,
    userAgentProvider: UserAgentProvider = UserAgentProvider
): UserActivityReporter {
    val options = UserActivityReporterOptions(
        userAgentProvider.sdkInformation(), eventsUrl
    )
    
    return UserActivityReporter.getOrCreate(options)
}
