package com.mapbox.search.tests_support

import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import java.util.Locale

internal object TestObjects {

    val TEST_LOCALE: Locale = Locale.ENGLISH

    val TEST_BOUNDING_BOX: BoundingBox = BoundingBox.fromPoints(
        Point.fromLngLat(10.0, 20.0),
        Point.fromLngLat(20.0, 30.0),
    )

    val TEST_POINT: Point = Point.fromLngLat(10.0, 10.0)
}
