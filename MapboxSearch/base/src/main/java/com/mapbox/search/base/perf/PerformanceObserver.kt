package com.mapbox.search.base.perf

import androidx.annotation.RestrictTo
import kotlin.time.Duration

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
internal interface PerformanceObserver {
    /**
     * Notifies about new synchronous section start.
     * @see [syncSectionCompleted]
     */
    fun syncSectionStarted(name: String)

    /**
     * Notifies about synchronous section end.
     * @param name always matches last started synchronous section
     * @see [syncSectionStarted]
     */
    fun syncSectionCompleted(name: String, duration: Duration?)

    /**
     * Notifies about new asynchronous section start.
     * @param name the name of the async section
     * @param id unique numeric identifier for the async section
     * @see [asyncSectionFinished]
     */
    fun asyncSectionStarted(name: String, id: Int)

    /**
     * Notifies about asynchronous section end.
     * @param name the name of the async section
     * @param id unique numeric identifier for the async section
     * @param duration the duration of the async section, null if not available
     * @see [asyncSectionStarted]
     */
    fun asyncSectionFinished(name: String, id: Int, duration: Duration?)
}
