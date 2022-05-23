package com.mapbox.search.tests_support

import com.mapbox.geojson.Point
import com.mapbox.search.OfflineReverseGeoOptions
import com.mapbox.search.OfflineSearchEngine
import com.mapbox.search.OfflineSearchOptions
import com.mapbox.search.result.SearchSuggestion

internal fun OfflineSearchEngine.searchBlocking(
    query: String,
    options: OfflineSearchOptions = OfflineSearchOptions(),
): BlockingSearchSelectionCallback.SearchEngineResult {
    val callback = BlockingSearchSelectionCallback()
    search(query, options, callback)
    return callback.getResultBlocking()
}

internal fun OfflineSearchEngine.selectBlocking(
    suggestion: SearchSuggestion,
): BlockingSearchSelectionCallback.SearchEngineResult {
    val callback = BlockingSearchSelectionCallback()
    select(suggestion, callback)
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
