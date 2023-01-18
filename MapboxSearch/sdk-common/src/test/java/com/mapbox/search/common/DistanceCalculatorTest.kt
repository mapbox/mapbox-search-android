package com.mapbox.search.common

import com.mapbox.geojson.Point
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class DistanceCalculatorTest {

    @Test
    fun `Check DistanceCalculator`() {
        val coreCalculator = mockCoreDistanceCalculator()
        val distanceCalculator = DistanceCalculatorImpl(coreCalculator)

        val distance = distanceCalculator.distance(POINT_FROM, POINT_TO)
        assertEquals(DISTANCE, distance)
        verify {
            coreCalculator.distance(
                POINT_FROM,
                POINT_TO
            )
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
