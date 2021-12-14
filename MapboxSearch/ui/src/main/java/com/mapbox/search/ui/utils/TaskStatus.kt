package com.mapbox.search.ui.utils

/**
 * TODO: Remove this, when SearchRequestTask#isCancelled, SearchRequestTask#isExecuted will be available
 */
internal class TaskStatus private constructor() {

    var isExecuted: Boolean = false
        private set

    var isCancelled: Boolean = false
        private set

    fun markCancelled() {
        if (isExecuted) {
            return
        }

        isCancelled = true
    }

    fun markExecuted() {
        if (isCancelled) {
            return
        }

        isExecuted = true
    }

    fun reset() {
        isExecuted = false
        isCancelled = false
    }

    companion object {
        fun idle() = TaskStatus()
    }
}
