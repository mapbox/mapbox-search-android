package com.mapbox.search

import com.mapbox.geojson.BoundingBox

/**
 * Provides map viewport, if you have map in your application.
 */
public fun interface ViewportProvider {

    /**
     * This method will be called in the following cases:
     * 1) Search engine requests the map's viewport visible to a user.
     * Provided viewport will be used as a bounding box for upcoming search request, if
     * [SearchOptions.boundingBox] or [CategorySearchOptions.boundingBox] aren't specified.
     * 2) [com.mapbox.search.analytics.AnalyticsService] requests the map's viewport visible to a user.
     * Provided viewport will be used for better and more accurate analytics data.
     *
     * @return Bounding box for current map viewport or null.
     */
    public fun getViewport(): BoundingBox?
}
