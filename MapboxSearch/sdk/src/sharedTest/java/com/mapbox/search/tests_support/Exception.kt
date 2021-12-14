package com.mapbox.search.tests_support

internal fun Throwable?.equalsTo(other: Throwable?): Boolean {
    if (this == null || other == null) return this == other

    if (this === other) return true
    if (this.javaClass != other.javaClass) return false

    if (this.message != other.message) return false
    if (this.cause != other.cause) return false

    return true
}
