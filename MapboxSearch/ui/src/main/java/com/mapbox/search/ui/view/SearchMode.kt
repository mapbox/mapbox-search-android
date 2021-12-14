package com.mapbox.search.ui.view

import com.mapbox.common.ReachabilityInterface

/**
 * Enum that represents search mode.
 */
public enum class SearchMode {

    /**
     * Online more.
     */
    ONLINE,

    /**
     * Offline mode.
     */
    OFFLINE,

    /**
     * Determined automatically based on the device's network reachability.
     */
    AUTO;

    internal fun isOnlineSearch(reachabilityInterface: ReachabilityInterface): Boolean {
        return when (this) {
            ONLINE -> true
            OFFLINE -> false
            AUTO -> reachabilityInterface.isReachable
        }
    }
}
