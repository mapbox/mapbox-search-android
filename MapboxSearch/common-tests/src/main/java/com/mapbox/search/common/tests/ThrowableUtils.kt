package com.mapbox.search.common.tests

inline fun <reified T : Throwable> catchThrowable(block: () -> Unit): T? {
    return try {
        block()
        null
    } catch (e: Throwable) {
        if (e is T) e else throw e
    }
}

fun Throwable?.equalsTo(other: Throwable?): Boolean {
    if (this == null || other == null) return this == other

    if (this === other) return true
    if (this.javaClass != other.javaClass) return false

    if (this.message != other.message) return false
    if (this.cause != other.cause) return false

    return true
}
