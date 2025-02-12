@file:Suppress("DEPRECATION")

package com.mapbox.search.offline

import com.mapbox.search.offline.OfflineSearchResultType.Companion.DEFAULT

/**
 * Defines type of offline search result.
 *
 * This enum has been replaced by [NewOfflineSearchResultType].
 * It no longer represents the currently supported types in the Offline Search Engine
 * because it cannot be extended without violating SemVer rules for public API changes.
 *
 * If a type of an [OfflineSearchResult] is not present in [OfflineSearchResultType],
 * it will default to [DEFAULT].
 */
@Deprecated(
    message = "Replaced by NewOfflineSearchResultType. This enum no longer represents the current supported types.",
    replaceWith = ReplaceWith("NewOfflineSearchResultType"),
)
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
     * Companion object for [OfflineSearchResultType].
     */
    ADDRESS;

    /**
     * Companion object.
     */
    public companion object {

        /**
         * The default type used when the type of an [OfflineSearchResult] is not present in [OfflineSearchResultType].
         */
        public val DEFAULT: OfflineSearchResultType = ADDRESS
    }
}
