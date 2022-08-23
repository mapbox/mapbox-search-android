package com.mapbox.search.base.utils

fun interface TimeProvider {
    fun currentTimeMillis(): Long
}

class LocalTimeProvider : TimeProvider {
    override fun currentTimeMillis(): Long = System.currentTimeMillis()
}
