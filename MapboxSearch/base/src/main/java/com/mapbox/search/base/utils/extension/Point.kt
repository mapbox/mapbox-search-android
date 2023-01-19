package com.mapbox.search.base.utils.extension

import com.mapbox.geojson.Point
import com.mapbox.search.common.DistanceCalculator

/**
 * Calculates distance to specified destination point.
 *
 * @return distance to [destination] in meters.
 */
@JvmSynthetic
fun Point.distanceTo(destination: Point): Double =
    DistanceCalculator.instance(latitude = latitude())
        .distance(this, destination)
