package com.mapbox.search.ui.utils.offline

import com.mapbox.search.base.failDebug
import com.mapbox.search.offline.NewOfflineSearchResultType
import com.mapbox.search.result.SearchResultType

internal fun createSearchResultTypeFromOfflineType(@NewOfflineSearchResultType.Type type: String): SearchResultType {
    return when (type) {
        NewOfflineSearchResultType.PLACE -> SearchResultType.PLACE
        NewOfflineSearchResultType.STREET -> SearchResultType.STREET
        NewOfflineSearchResultType.ADDRESS -> SearchResultType.ADDRESS
        NewOfflineSearchResultType.POI -> SearchResultType.POI
        else -> {
            failDebug {
                "Unprocessed offline search result type: $type"
            }
            SearchResultType.ADDRESS
        }
    }
}
