package com.mapbox.search.tests_support

internal inline fun <reified T : Throwable> catchThrowable(block: () -> Unit): T? {
    return try {
        block()
        null
    } catch (e: Throwable) {
        if (e is T) e else throw e
    }
}
