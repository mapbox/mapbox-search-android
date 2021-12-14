package com.mapbox.search

import com.mapbox.geojson.Point
import com.mapbox.search.core.CoreDistanceCalculatorInterface
import com.mapbox.test.dsl.TestCase
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.TestFactory

internal class DistanceCalculatorTest {

    @TestFactory
    fun `Check DistanceCalculator`() = TestCase {
        Given("SearchResult with mocked nativeSearchResult") {
            val coreCalculator = mockCoreDistanceCalculator()
            val distanceCalculator = DistanceCalculatorImpl(coreCalculator)

            When("Call distance()") {
                val distance = distanceCalculator.distance(POINT_FROM, POINT_TO)
                Then("Mocked value returned", DISTANCE, distance)
                Verify("Corresponding function from core called") {
                    coreCalculator.distance(
                        POINT_FROM,
                        POINT_TO
                    )
                }
            }
        }
    }

    private companion object {

        val POINT_FROM: Point = Point.fromLngLat(1.0, 2.0)
        val POINT_TO: Point = Point.fromLngLat(3.0, 4.0)

        const val DISTANCE = 123.0

        private fun mockCoreDistanceCalculator(): CoreDistanceCalculatorInterface {
            return mockk<CoreDistanceCalculatorInterface>().apply {
                every { distance(POINT_FROM, POINT_TO) } returns DISTANCE
            }
        }
    }
}
