package com.mapbox.search.base.location

import com.mapbox.geojson.Point
import com.mapbox.search.base.core.CoreBoundingBox
import com.mapbox.search.base.core.CoreLocationProvider

class WrapperLocationProvider(
    private val locationProvider: CoreLocationProvider?,
    private val viewportProvider: (() -> CoreBoundingBox?)?
) : CoreLocationProvider {

    override fun getLocation(): Point? {
        return locationProvider?.location
    }

    override fun getViewport(): CoreBoundingBox? {
        return viewportProvider?.invoke()
    }
}
