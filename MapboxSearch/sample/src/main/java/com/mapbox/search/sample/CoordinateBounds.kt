package com.mapbox.search.sample

import com.mapbox.geojson.BoundingBox
import com.mapbox.maps.CoordinateBounds

fun CoordinateBounds.toBoundingBox(): BoundingBox = BoundingBox.fromPoints(this.southwest, this.northeast)
