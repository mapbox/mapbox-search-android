package com.mapbox.search.tests_support

import com.mapbox.geojson.Point
import com.mapbox.search.OfflineReverseGeoOptions
import com.mapbox.search.OfflineSearchEngine
import com.mapbox.search.OfflineSearchOptions

internal fun OfflineSearchEngine.searchBlocking(
    query: String,
    options: OfflineSearchOptions = OfflineSearchOptions(),
): BlockingSearchCallback.SearchEngineResult {
    val callback = BlockingSearchCallback()
    search(query, options, callback)
    return callback.getResultBlocking()
}

internal fun OfflineSearchEngine.reverseGeocodingBlocking(
    options: OfflineReverseGeoOptions,
): BlockingSearchCallback.SearchEngineResult {
    val callback = BlockingSearchCallback()
    reverseGeocoding(options, callback)
    return callback.getResultBlocking()
}

internal fun OfflineSearchEngine.reverseGeocodingBlocking(point: Point): BlockingSearchCallback.SearchEngineResult =
    reverseGeocodingBlocking(OfflineReverseGeoOptions(point))
