package com.mapbox.search.offline.tests_support

import com.mapbox.geojson.Point
import com.mapbox.search.offline.OfflineReverseGeoOptions
import com.mapbox.search.offline.OfflineSearchEngine
import com.mapbox.search.offline.OfflineSearchOptions

internal fun OfflineSearchEngine.searchBlocking(
    query: String,
    options: OfflineSearchOptions = OfflineSearchOptions(),
): BlockingOfflineSearchCallback.SearchEngineResult {
    val callback = BlockingOfflineSearchCallback()
    search(query, options, callback)
    return callback.getResultBlocking()
}

internal fun OfflineSearchEngine.reverseGeocodingBlocking(
    options: OfflineReverseGeoOptions,
): BlockingOfflineSearchCallback.SearchEngineResult {
    val callback = BlockingOfflineSearchCallback()
    reverseGeocoding(options, callback)
    return callback.getResultBlocking()
}

internal fun OfflineSearchEngine.reverseGeocodingBlocking(point: Point): BlockingOfflineSearchCallback.SearchEngineResult =
    reverseGeocodingBlocking(OfflineReverseGeoOptions(point))
