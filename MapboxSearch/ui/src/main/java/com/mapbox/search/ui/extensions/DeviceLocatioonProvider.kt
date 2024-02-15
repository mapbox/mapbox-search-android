package com.mapbox.search.ui.extensions

import com.mapbox.common.location.LocationProvider
import com.mapbox.geojson.Point
import com.mapbox.search.base.utils.extension.toPoint
import com.mapbox.search.common.DistanceCalculator

/**
 * TODO
 *
 */
public fun LocationProvider.userDistanceTo(destination: Point, callback: (Double?) -> Unit) {
    getLastLocation { location ->
        if (location == null) {
            callback(null)
        } else {
            val distance = DistanceCalculator.instance(latitude = location.latitude)
                .distance(location.toPoint(), destination)
            callback(distance)
        }
    }
}
