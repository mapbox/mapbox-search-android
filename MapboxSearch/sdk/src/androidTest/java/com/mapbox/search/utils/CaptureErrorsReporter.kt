package com.mapbox.search.utils

import com.mapbox.search.analytics.ErrorsReporter

internal class CaptureErrorsReporter : ErrorsReporter {

    private var _capturedErrors: MutableList<Throwable> = mutableListOf()
    val capturedErrors: List<Throwable>
        get() = _capturedErrors

    fun reset() {
        _capturedErrors = mutableListOf()
    }

    override fun reportError(throwable: Throwable) {
        _capturedErrors.add(throwable)
    }
}
