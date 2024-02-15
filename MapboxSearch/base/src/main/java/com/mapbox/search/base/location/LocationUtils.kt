package com.mapbox.search.base.location

import com.mapbox.common.location.LocationProvider
import com.mapbox.common.location.LocationServiceFactory
import com.mapbox.geojson.BoundingBox
import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.max
import kotlin.math.min

fun defaultLocationProvider(): LocationProvider? =
    LocationServiceFactory.getOrCreate()
        .getDeviceLocationProvider(null)
        .value

fun calculateMapZoom(bbox: BoundingBox): Float {
    val eps = 1.0E-5
    val lngZoom = 360.0 / max(abs(bbox.west() - bbox.east()), eps)
    val latZoom = 180.0 / max(abs(bbox.north() - bbox.south()), eps)
    return log2(min(lngZoom, latZoom)).toFloat()
}
