package com.mapbox.search.base

import com.mapbox.search.common.CompletionCallback

class StubCompletionCallback<T> : CompletionCallback<T> {
    override fun onComplete(result: T) {}

    override fun onError(e: Exception) {}
}
