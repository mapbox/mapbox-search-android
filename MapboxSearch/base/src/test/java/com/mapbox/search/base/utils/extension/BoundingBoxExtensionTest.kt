package com.mapbox.search.base.utils.extension

import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.test.dsl.TestCase
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.TestFactory

internal class BoundingBoxExtensionTest {

    @TestFactory
    fun `Check mapping BoundingBox to core`() = TestCase {
        Given("BoundingBox extension") {
            When("Convert <BoundingBox(Point($southwestLatitude, $southwestLongitude), Point($northeastLatitude, $northeastLongitude))> to platform") {
                val actualValue = BoundingBox.fromPoints(
                    Point.fromLngLat(southwestLongitude, southwestLatitude),
                    Point.fromLngLat(northeastLongitude, northeastLatitude)
                ).mapToCore()
                Then("BoundingBox should be <Point($southwestLongitude, $southwestLatitude, $northeastLongitude, $northeastLatitude)>") {
                    Assertions.assertEquals(southwestLatitude, actualValue.min.latitude())
                    Assertions.assertEquals(southwestLongitude, actualValue.min.longitude())
                    Assertions.assertEquals(northeastLatitude, actualValue.max.latitude())
                    Assertions.assertEquals(northeastLongitude, actualValue.max.longitude())
                }
            }
        }
    }

    private companion object {
        private const val southwestLatitude = 53.0
        private const val southwestLongitude = 27.0

        private const val northeastLatitude = 54.0
        private const val northeastLongitude = 28.0
    }
}
