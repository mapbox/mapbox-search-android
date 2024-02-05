package com.mapbox.search.base.core

import com.mapbox.search.base.BaseSearchSdkInitializer
import com.mapbox.search.internal.bindgen.UserActivityReporter
import com.mapbox.search.internal.bindgen.UserActivityReporterOptions

fun getUserActivityReporter(
    eventsUrl: String? = null
): UserActivityReporter {
    val options = UserActivityReporterOptions(
        BaseSearchSdkInitializer.sdkInformation,
        eventsUrl
    )
    return UserActivityReporter.getOrCreate(options)
}
