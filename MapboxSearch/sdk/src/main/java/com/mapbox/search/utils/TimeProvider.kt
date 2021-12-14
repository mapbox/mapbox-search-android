package com.mapbox.search.utils

internal fun interface TimeProvider {
    fun currentTimeMillis(): Long
}

internal class LocalTimeProvider : TimeProvider {
    override fun currentTimeMillis(): Long = System.currentTimeMillis()
}
