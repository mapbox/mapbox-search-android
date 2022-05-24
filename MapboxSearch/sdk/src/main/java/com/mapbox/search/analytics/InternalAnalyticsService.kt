package com.mapbox.search.analytics

internal interface InternalAnalyticsService : AnalyticsService {
    fun setAccessToken(accessToken: String)
}
