package com.mapbox.search.base.location

import android.content.Context
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.geojson.BoundingBox
import com.mapbox.search.base.BaseSearchSdkInitializer
import com.mapbox.search.base.BaseSearchSdkInitializerImpl
import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.max
import kotlin.math.min

fun defaultLocationEngine(context: Context = BaseSearchSdkInitializerImpl.app) =
    LocationEngineProvider.getBestLocationEngine(context)

fun calculateMapZoom(bbox: BoundingBox): Float {
    val eps = 1.0E-5
    val lngZoom = 360.0 / max(abs(bbox.west() - bbox.east()), eps)
    val latZoom = 180.0 / max(abs(bbox.north() - bbox.south()), eps)
    return log2(min(lngZoom, latZoom)).toFloat()
}
