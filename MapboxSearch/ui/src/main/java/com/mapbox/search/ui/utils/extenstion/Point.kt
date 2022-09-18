package com.mapbox.search.ui.utils.extenstion

import com.mapbox.geojson.Point
import com.mapbox.search.ServiceProvider

/**
 * Calculates distance to specified destination point.
 *
 * @return distance to [destination] in meters.
 */
internal fun Point.distanceTo(destination: Point): Double = ServiceProvider.INSTANCE
    .distanceCalculator(latitude = latitude())
    .distance(this, destination)
