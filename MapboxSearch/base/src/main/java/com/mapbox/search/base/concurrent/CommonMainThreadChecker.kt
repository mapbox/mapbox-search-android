package com.mapbox.search.base.concurrent

import com.mapbox.search.common.concurrent.SearchSdkMainThreadWorker

val isOnMainLooper: Boolean
    get() = SearchSdkMainThreadWorker.isMainThread

fun checkMainThread() {
    check(isOnMainLooper) {
        "Expected main thread but was: ${Thread.currentThread().name}"
    }
}
