package com.mapbox.search.base.utils.extension

import com.mapbox.search.base.core.CoreRoutablePoint
import com.mapbox.search.common.RoutablePoint

fun CoreRoutablePoint.mapToPlatform() = RoutablePoint(
    point = point,
    name = name,
)

fun RoutablePoint.mapToCore() = CoreRoutablePoint(
    point,
    name,
)
