package com.mapbox.search

import com.mapbox.search.common.SearchRequestException
import com.mapbox.search.result.SearchResult

/**
 * Search result callback for category search and reverse geocoding.
 */
public interface SearchCallback {

    /**
     * Called when results are ready.
     * @param results List of [SearchResult].
     * @param responseInfo Search response and request information.
     */
    public fun onResults(results: List<SearchResult>, responseInfo: ResponseInfo)

    /**
     * Called in case if error occurred during request.
     * @param e Exception, occurred during request.
     */
    public fun onError(e: Exception)
}

/**
 * Search callback for when only one response is expected
 */
public interface SearchResultCallback : SearchCallback {

    /**
     * Called when the result is ready.
     * @param result [SearchResult] that was retrieved
     * @param responseInfo Search response and request information.
     */
    public fun onResult(result: SearchResult, responseInfo: ResponseInfo)

    override fun onResults(results: List<SearchResult>, responseInfo: ResponseInfo): Unit =
        if (results.isNotEmpty()) {
            onResult(results.first(), responseInfo)
        } else {
            // this should not happen, the server should have returned a 404
            onError(SearchRequestException("Not found", 404))
        }
}
