package com.mapbox.search.ui.utils.offline

import com.mapbox.search.base.failDebug
import com.mapbox.search.offline.NewOfflineSearchResultType
import com.mapbox.search.result.NewSearchResultType

@NewSearchResultType.Type
internal fun createNewSearchResultTypeFromOfflineType(
    @NewOfflineSearchResultType.Type type: String,
): String {
    return when (type) {
        NewOfflineSearchResultType.PLACE -> NewSearchResultType.PLACE
        NewOfflineSearchResultType.STREET -> NewSearchResultType.STREET
        NewOfflineSearchResultType.ADDRESS -> NewSearchResultType.ADDRESS
        NewOfflineSearchResultType.POI -> NewSearchResultType.POI
        else -> {
            failDebug {
                "Unprocessed offline search result type: $type"
            }
            NewSearchResultType.ADDRESS
        }
    }
}
