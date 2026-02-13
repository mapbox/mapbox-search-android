package com.mapbox.search.base.perf

import android.util.Log
import androidx.annotation.RestrictTo
import kotlin.time.Duration

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
private const val TAG = "PERFORMANCE"

internal class LogcatPerformanceLogging : PerformanceObserver {
    override fun syncSectionStarted(name: String) {
        Log.d(TAG, "$name section started")
    }

    override fun syncSectionCompleted(name: String, duration: Duration?) {
        Log.d(TAG, "$name section completed in $duration")
    }

    override fun asyncSectionStarted(name: String, id: Int) {
        Log.d(TAG, "$name async section started (id: $id)")
    }

    override fun asyncSectionFinished(name: String, id: Int, duration: Duration?) {
        Log.d(TAG, "$name async section finished (id: $id) in $duration")
    }
}
