package com.mapbox.search.common

/**
 * Interface definition for a commonly used in the Search SDK callback
 * to be invoked when asynchronous operation completes.
 */
public interface CompletionCallback<T> {

    /**
     * Invoked when an operation completes successfully.
     */
    public fun onComplete(result: T)

    /**
     * Invoked when an error happened.
     *
     * @param e Exception, occurred during operation.
     */
    public fun onError(e: Exception)
}
