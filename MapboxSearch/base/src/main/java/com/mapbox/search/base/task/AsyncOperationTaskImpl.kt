package com.mapbox.search.base.task

class AsyncOperationTaskImpl<T>(delegate: T? = null) : ExtendedAsyncOperationTask<T> {

    private val cancelableList: MutableList<CancelableWrapper> = mutableListOf()

    override var callbackDelegate: T? = null
        @Synchronized
        set(value) {
            field = if (isCompleted) {
                null
            } else {
                value
            }
        }
        @Synchronized get

    override var callbackActionExecuted: Boolean = false
        @Synchronized private set
        @Synchronized get

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

    init {
        callbackDelegate = delegate
    }

    @Synchronized
    override fun addInnerTask(cancelable: CancelableWrapper) {
        if (isDone) {
            return
        }

        if (isCancelled) {
            cancelable.cancel()
            return
        }

        cancelableList.add(cancelable)
    }

    @Synchronized
    override fun onComplete() {
        if (isCompleted) {
            return
        }

        cancelableList.clear()

        isDone = true
        onCancelCallback = null

        callbackDelegate = null
    }

    @Synchronized
    override fun cancel() {
        if (isCompleted) {
            return
        }

        cancelableList.forEach { it.cancel() }
        cancelableList.clear()
        onCancelCallback?.invoke()
        onCancelCallback = null
        isCancelled = true

        callbackDelegate = null
    }

    @Synchronized
    override fun markCancelledAndRunOnCallback(action: T.() -> Unit) {
        if (isCompleted) {
            return
        }

        val delegate = callbackDelegate
        callbackDelegate = null

        cancel()

        delegate ?: return
        callbackActionExecuted = true
        action(delegate)
    }

    @Synchronized
    override fun markExecutedAndRunOnCallback(action: T.() -> Unit) {
        if (isCompleted) {
            return
        }

        isDone = true

        onCancelCallback = null

        val delegate = callbackDelegate ?: return
        callbackDelegate = null
        callbackActionExecuted = true
        action(delegate)
    }

    companion object {

        val COMPLETED = completed<Any>()

        fun <T> completed(): AsyncOperationTaskImpl<T> {
            return AsyncOperationTaskImpl<T>().apply {
                onComplete()
            }
        }
    }
}
