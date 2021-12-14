package com.mapbox.search.ui.utils.extenstion

import com.mapbox.geojson.Point
import com.mapbox.search.MapboxSearchSdk

/**
 * Calculates distance to specified destination point.
 *
 * @return distance to [destination] in meters.
 */
internal fun Point.distanceTo(destination: Point): Double = MapboxSearchSdk.serviceProvider
    .distanceCalculator(latitude = latitude())
    .distance(this, destination)
