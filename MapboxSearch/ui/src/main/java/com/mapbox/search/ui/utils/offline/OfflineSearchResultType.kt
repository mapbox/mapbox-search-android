package com.mapbox.search.ui.utils.offline

import com.mapbox.search.offline.OfflineSearchResultType
import com.mapbox.search.result.SearchResultType

@JvmSynthetic
internal fun OfflineSearchResultType.mapToSdkSearchResultType(): SearchResultType {
    return when (this) {
        OfflineSearchResultType.PLACE -> SearchResultType.PLACE
        OfflineSearchResultType.STREET -> SearchResultType.STREET
        OfflineSearchResultType.ADDRESS -> SearchResultType.ADDRESS
        OfflineSearchResultType.USER_RECORD -> SearchResultType.PLACE
    }
}
