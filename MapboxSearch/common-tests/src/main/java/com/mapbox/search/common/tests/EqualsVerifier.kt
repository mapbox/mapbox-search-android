package com.mapbox.search.common.tests

import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import nl.jqno.equalsverifier.api.SingleTypeEqualsVerifierApi

fun <T> SingleTypeEqualsVerifierApi<T>.withPrefabTestPoint(
    red: Point = Point.fromLngLat(1.0, 2.0),
    blue: Point = Point.fromLngLat(3.0, 4.0)
): SingleTypeEqualsVerifierApi<T> {
    return withPrefabValues(Point::class.java, red, blue)
}

fun <T> SingleTypeEqualsVerifierApi<T>.withPrefabTestBoundingBox(
    red: BoundingBox = BoundingBox.fromPoints(Point.fromLngLat(1.0, 2.0), Point.fromLngLat(3.0, 4.0)),
    blue: BoundingBox = BoundingBox.fromPoints(Point.fromLngLat(5.0, 6.0), Point.fromLngLat(7.0, 8.0))
): SingleTypeEqualsVerifierApi<T> {
    return withPrefabValues(BoundingBox::class.java, red, blue)
}
