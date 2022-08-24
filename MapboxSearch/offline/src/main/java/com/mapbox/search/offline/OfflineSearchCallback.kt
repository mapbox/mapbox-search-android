package com.mapbox.search.offline

/**
 * Search result callback for category search and reverse geocoding.
 */
public interface OfflineSearchCallback {

    /**
     * Called when results are ready.
     * @param results List of [OfflineSearchResult].
     * @param responseInfo Search response and request information.
     */
    public fun onResults(results: List<OfflineSearchResult>, responseInfo: OfflineResponseInfo)

    /**
     * Called in case if error occurred during request.
     * @param e Exception, occurred during request.
     */
    public fun onError(e: Exception)
}
