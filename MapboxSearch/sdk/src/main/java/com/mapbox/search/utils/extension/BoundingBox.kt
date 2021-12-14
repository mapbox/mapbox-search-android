package com.mapbox.search.utils.extension

import com.mapbox.geojson.BoundingBox
import com.mapbox.search.core.CoreBoundingBox

internal fun BoundingBox.mapToCore(): CoreBoundingBox = CoreBoundingBox(southwest(), northeast())
internal fun CoreBoundingBox.mapToPlatform(): BoundingBox = BoundingBox.fromPoints(min, max)
