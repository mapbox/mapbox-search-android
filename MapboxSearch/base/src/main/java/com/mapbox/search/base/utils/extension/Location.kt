package com.mapbox.search.base.utils.extension

import com.mapbox.common.location.Location
import com.mapbox.geojson.Point

fun Location.toPoint(): Point = Point.fromLngLat(longitude, latitude)
