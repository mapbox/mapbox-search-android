package com.mapbox.search.location

import com.mapbox.geojson.Point
import com.mapbox.search.ViewportProvider
import com.mapbox.search.core.CoreBoundingBox
import com.mapbox.search.core.CoreLocationProvider
import com.mapbox.search.utils.extension.mapToCore

internal class WrapperLocationProvider(
    private val locationProvider: CoreLocationProvider?,
    private val viewportProvider: ViewportProvider?
) : CoreLocationProvider {

    override fun getLocation(): Point? {
        return locationProvider?.location
    }

    override fun getViewport(): CoreBoundingBox? {
        return viewportProvider?.getViewport()?.mapToCore()
    }
}
