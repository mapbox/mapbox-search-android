package com.mapbox.search.analytics

internal fun interface ErrorsReporter {
    fun reportError(throwable: Throwable)
}
