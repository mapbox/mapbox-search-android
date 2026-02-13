package com.mapbox.search.base.perf

import androidx.annotation.MainThread
import com.mapbox.annotation.MapboxExperimental

/**
 * API to control performance related information Navigation SDK provides.
 */
object SearchPerformance {

    private val loggingSwitch = PerformanceObserverSwitch(LogcatPerformanceLogging())
    private val tracingSwitch = PerformanceObserverSwitch(getTraceSectionsPerformanceObserver())

    /***
     * Controls if Nav SDK logs performance related information to logcat on with info level.
     * @param isEnabled defines if logging is enabled
     */
    @MapboxExperimental
    @MainThread
    fun performanceInfoLoggingEnabled(isEnabled: Boolean) {
        loggingSwitch.setEnabled(isEnabled)
    }

    /***
     * Controls if Nav SDK enables internal trace sections.
     * Known limitations:
     * * Internal trace sections don't work on devices with Android version below 29(Q).
     * * Disabling/enabling of tracing during recording may result in sections without begin or end.
     * It's recommended to enable/disable tracing before system starts recording the trace and
     * after recording is finished.
     * @param isEnabled defines if tracing should be enabled
     */
    @MapboxExperimental
    @MainThread
    fun performanceTracingEnabled(isEnabled: Boolean) {
        tracingSwitch.setEnabled(isEnabled)
    }
}

private class PerformanceObserverSwitch(
    private val observer: PerformanceObserver?,
) {

    private var wasEnabled = false

    fun setEnabled(isEnabled: Boolean) {
        if (observer == null) return
        when {
            isEnabled && !wasEnabled -> {
                PerformanceTracker.addObserver(observer)
            }
            !isEnabled && wasEnabled -> {
                PerformanceTracker.removeObserver(observer)
            }
        }
        wasEnabled = isEnabled
    }
}
