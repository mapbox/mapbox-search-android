package com.mapbox.search.common

/**
 * Represents the result (task handle) of an asynchronous operation.
 */
public interface AsyncOperationTask {

    /**
     * Denotes whether this task completed.
     */
    public val isDone: Boolean

    /**
     * Denotes whether this task was cancelled.
     */
    public val isCancelled: Boolean

    /**
     * Attempts to cancel execution of this task.
     */
    public fun cancel()

    /**
     * @suppress
     */
    public companion object {

        /**
         * Represents completed [AsyncOperationTask]
         */
        @JvmStatic
        @get:JvmName("getCompleted")
        public val COMPLETED : AsyncOperationTask = object : AsyncOperationTask {
            override val isDone: Boolean
                get() = true

            override val isCancelled: Boolean
                get() = false

            override fun cancel() {
                // do nothing
            }
        }
    }
}
