package com.mapbox.search.common.concurrent

object CommonMainThreadChecker {
    lateinit var isOnMainLooper: (() -> Boolean)
}

val isOnMainLooper: Boolean
    get() = CommonMainThreadChecker.isOnMainLooper()

fun checkMainThread() {
    check(isOnMainLooper) {
        "Expected main thread but was: ${Thread.currentThread().name}"
    }
}
