package com.mapbox.search.offline

import com.mapbox.search.base.failDebug
import com.mapbox.search.base.result.BaseSearchResultType

/**
 * Defines type of offline search result.
 */
public enum class OfflineSearchResultType {

    /**
     * Typically these are cities, villages, municipalities, etc.
     * They’re usually features used in postal addressing, and are suitable for display in ambient
     * end-user applications where current-location context is needed (for example, in weather displays).
     */
    PLACE,

    /**
     * Features that are smaller than places and that correspond to streets in cities, villages, etc.
     */
    STREET,

    /**
     * Individual residential or business addresses.
     */
    ADDRESS,
}

@JvmSynthetic
internal fun BaseSearchResultType.mapToOfflineSdkType(): OfflineSearchResultType {
    return when (this) {
        BaseSearchResultType.PLACE -> OfflineSearchResultType.PLACE
        BaseSearchResultType.STREET -> OfflineSearchResultType.STREET
        BaseSearchResultType.ADDRESS -> OfflineSearchResultType.ADDRESS
        else -> {
            failDebug {
                "Unprocessed in offline result type: $this"
            }
            OfflineSearchResultType.PLACE
        }
    }
}
