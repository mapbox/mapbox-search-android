package com.mapbox.search

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
