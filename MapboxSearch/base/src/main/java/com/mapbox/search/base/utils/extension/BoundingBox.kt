package com.mapbox.search.base.utils.extension

import com.mapbox.geojson.BoundingBox
import com.mapbox.search.base.core.CoreBoundingBox

fun BoundingBox.mapToCore(): CoreBoundingBox = CoreBoundingBox(southwest(), northeast())
fun CoreBoundingBox.mapToPlatform(): BoundingBox = BoundingBox.fromPoints(min, max)
