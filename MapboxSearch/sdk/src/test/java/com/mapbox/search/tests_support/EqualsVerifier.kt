package com.mapbox.search.tests_support

import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.search.result.OriginalSearchResult
import nl.jqno.equalsverifier.api.SingleTypeEqualsVerifierApi

internal fun <T> SingleTypeEqualsVerifierApi<T>.withPrefabTestPoint(
    red: Point = Point.fromLngLat(1.0, 2.0),
    blue: Point = Point.fromLngLat(3.0, 4.0)
): SingleTypeEqualsVerifierApi<T> {
    return withPrefabValues(Point::class.java, red, blue)
}

internal fun <T> SingleTypeEqualsVerifierApi<T>.withPrefabTestBoundingBox(
    red: BoundingBox = BoundingBox.fromPoints(Point.fromLngLat(1.0, 2.0), Point.fromLngLat(3.0, 4.0)),
    blue: BoundingBox = BoundingBox.fromPoints(Point.fromLngLat(5.0, 6.0), Point.fromLngLat(7.0, 8.0))
): SingleTypeEqualsVerifierApi<T> {
    return withPrefabValues(BoundingBox::class.java, red, blue)
}

internal fun <T> SingleTypeEqualsVerifierApi<T>.withPrefabTestOriginalSearchResult(
    red: OriginalSearchResult = createTestOriginalSearchResult(id = "test-result-1"),
    blue: OriginalSearchResult = createTestOriginalSearchResult(id = "test-result-2")
): SingleTypeEqualsVerifierApi<T> {
    return withPrefabValues(OriginalSearchResult::class.java, red, blue)
}
