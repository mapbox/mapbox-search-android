package com.mapbox.search.analytics

internal interface InternalAnalyticsService : AnalyticsService {

    fun postJsonEvent(event: String)

    fun setAccessToken(accessToken: String)
}
