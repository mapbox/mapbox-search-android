package com.mapbox.search.common

interface SearchCommonAsyncOperationTask {
    val isDone: Boolean
    val isCancelled: Boolean
    fun cancel()
}

internal class SearchCommonAsyncOperationTaskImpl : SearchCommonAsyncOperationTask {

    var onCancelCallback: (() -> Unit)? = null
        @Synchronized
        set(value) {
            field = if (isDone || isCancelled) {
                null
            } else {
                value
            }
        }
        @Synchronized get

    override var isDone: Boolean = false
        @Synchronized private set
        @Synchronized get

    override var isCancelled: Boolean = false
        @Synchronized private set
        @Synchronized get

    @Synchronized
    fun onComplete() {
        if (isDone || isCancelled) {
            return
        }

        isDone = true
        onCancelCallback = null
    }

    @Synchronized
    override fun cancel() {
        if (isDone || isCancelled) {
            return
        }

        onCancelCallback?.invoke()
        onCancelCallback = null
        isCancelled = true
    }
}
