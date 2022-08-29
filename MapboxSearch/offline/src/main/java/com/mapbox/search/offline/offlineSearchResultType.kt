package com.mapbox.search.offline

import com.mapbox.search.base.result.BaseRawResultType

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
internal fun BaseRawResultType.tryMapToOfflineSdkType(): OfflineSearchResultType? {
    return when (this) {
        BaseRawResultType.PLACE -> OfflineSearchResultType.PLACE
        BaseRawResultType.STREET -> OfflineSearchResultType.STREET
        BaseRawResultType.ADDRESS -> OfflineSearchResultType.ADDRESS
        else -> null
    }
}
