package com.mapbox.search.ui.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Simple helper class for debouncing main thread requests. All actions are executed
 * using [Dispatchers.Main].
 */
public class Debouncer(private val delayMillis: Long) {
    private var job: Job? = null

    /**
     * Takes an action that should be "debounced"
     * @param action to be debounced
     */
    public fun debounce(action: () -> Unit) {
        job?.cancel()
        job = CoroutineScope(Dispatchers.Main).launch {
            delay(delayMillis)
            action()
        }
    }

    /**
     * Cancels the currently running action.
     */
    public fun cancel() {
        job?.cancel()
    }
}
