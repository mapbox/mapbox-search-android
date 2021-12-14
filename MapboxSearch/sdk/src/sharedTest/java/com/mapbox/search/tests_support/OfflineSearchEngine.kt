package com.mapbox.search.tests_support

import com.mapbox.search.OfflineSearchEngine
import com.mapbox.search.OfflineSearchOptions

internal fun OfflineSearchEngine.searchBlocking(
    query: String,
    options: OfflineSearchOptions = OfflineSearchOptions(),
): BlockingSearchSelectionCallback.SearchEngineResult {
    val callback = BlockingSearchSelectionCallback()
    search(query, options, callback)
    return callback.getResultBlocking()
}
