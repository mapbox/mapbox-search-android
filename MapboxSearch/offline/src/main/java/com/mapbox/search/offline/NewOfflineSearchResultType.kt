package com.mapbox.search.offline

import androidx.annotation.StringDef
import com.mapbox.search.base.result.BaseRawResultType

/**
 * Defines type of the [OfflineSearchResult].
 * Replaces enum [OfflineSearchResult].
 */
public object NewOfflineSearchResultType {

    /**
     * Typically these are cities, villages, municipalities, etc.
     * They’re usually features used in postal addressing, and are suitable for display in ambient
     * end-user applications where current-location context is needed (for example, in weather displays).
     */
    public const val PLACE: String = "PLACE"

    /**
     * Features that are smaller than places and that correspond to streets in cities, villages, etc.
     */
    public const val STREET: String = "STREET"

    /**
     * Individual residential or business addresses.
     */
    public const val ADDRESS: String = "ADDRESS"

    /**
     * Points of interest.
     * These include EV charging stations, restaurants, stores, concert venues, parks, museums, etc.
     */
    public const val POI: String = "POI"

    /**
     * Retention policy for the [NewOfflineSearchResultType].
     */
    @Retention(AnnotationRetention.BINARY)
    @StringDef(
        PLACE,
        STREET,
        ADDRESS,
        POI,
    )
    public annotation class Type

    @Type
    @JvmSynthetic
    internal val FALLBACK_TYPE = ADDRESS

    @Type
    internal fun createFromRawResultType(type: BaseRawResultType): String? {
        return when (type) {
            BaseRawResultType.PLACE -> PLACE
            BaseRawResultType.STREET -> STREET
            BaseRawResultType.ADDRESS -> ADDRESS
            BaseRawResultType.POI -> POI
            else -> null
        }
    }

    @Suppress("DEPRECATION")
    @JvmSynthetic
    internal fun toOldResultType(@Type type: String): OfflineSearchResultType {
        return when (type) {
            PLACE -> OfflineSearchResultType.PLACE
            STREET -> OfflineSearchResultType.STREET
            ADDRESS -> OfflineSearchResultType.ADDRESS
            else -> OfflineSearchResultType.DEFAULT
        }
    }
}
