package com.mapbox.search

import com.mapbox.geojson.Point
import com.mapbox.test.dsl.TestCase
import org.junit.Assert
import org.junit.jupiter.api.TestFactory
import java.util.concurrent.TimeUnit

internal class RouteOptionsTest {

    @TestFactory
    fun `Check RouteOptions creation`() = TestCase {
        Given("RouteOptions constructor") {
            WhenThrows("Trying to create RouteOptions with route, that has no points", IllegalArgumentException::class) {
                RouteOptions(emptyList(), TEST_TIME_DEVIATION)
            }

            WhenThrows("Trying to create RouteOptions with route, that has 1 point", IllegalArgumentException::class) {
                val route = listOf(Point.fromLngLat(1.0, 1.0))
                RouteOptions(route, TEST_TIME_DEVIATION)
            }

            When("Trying to create RouteOptions with route, that has 2 or more points") {
                val route = listOf(
                    Point.fromLngLat(1.0, 1.0),
                    Point.fromLngLat(2.0, 2.0)
                )
                RouteOptions(route, TEST_TIME_DEVIATION)

                Then("Successfully creates") {}
            }
        }
    }

    @TestFactory
    fun `Check time deviation calculation`() = TestCase {
        Given("Test input deviation options") {
            TIME_DEVIATION_TEST_PARAMS.forEach { (value, unit, expectedMinutes) ->
                When("Converting $value $unit to minutest") {
                    val deviation = RouteOptions.Deviation.Time(value, unit)
                    val route = listOf(
                        Point.fromLngLat(1.0, 1.0),
                        Point.fromLngLat(2.0, 2.0)
                    )
                    val routeOptions = RouteOptions(route, deviation)

                    Then("Deviation in minutes  should be $expectedMinutes") {
                        Assert.assertEquals(expectedMinutes, deviation.preciseDeviationInMinutes(), DOUBLE_COMPARISON_DELTA)
                        Assert.assertEquals(expectedMinutes, routeOptions.timeDeviationMinutes, DOUBLE_COMPARISON_DELTA)
                    }
                }
            }
        }
    }

    private companion object {

        const val DOUBLE_COMPARISON_DELTA = .000001

        val TEST_TIME_DEVIATION = RouteOptions.Deviation.Time(
            value = 1,
            unit = TimeUnit.MINUTES,
            sarType = RouteOptions.Deviation.SarType.ISOCHROME
        )

        val TIME_DEVIATION_TEST_PARAMS = listOf<Triple<Long, TimeUnit, Double>>(

            Triple(1, TimeUnit.MILLISECONDS, 1.0 / TimeUnit.MINUTES.toMillis(1)),
            Triple(0, TimeUnit.MILLISECONDS, .0),
            Triple(10, TimeUnit.MILLISECONDS, 10.0 / TimeUnit.MINUTES.toMillis(1)),

            Triple(1, TimeUnit.MICROSECONDS, 1.0 / TimeUnit.MINUTES.toMicros(1)),
            Triple(0, TimeUnit.MICROSECONDS, .0),
            Triple(10, TimeUnit.MICROSECONDS, 10.0 / TimeUnit.MINUTES.toMicros(1)),

            Triple(1, TimeUnit.NANOSECONDS, 1.0 / TimeUnit.MINUTES.toNanos(1)),
            Triple(0, TimeUnit.NANOSECONDS, .0),
            Triple(10, TimeUnit.NANOSECONDS, 10.0 / TimeUnit.MINUTES.toNanos(1)),

            Triple(1, TimeUnit.SECONDS, 1 / 60.0),
            Triple(0, TimeUnit.SECONDS, .0),
            Triple(30, TimeUnit.SECONDS, .5),

            Triple(1, TimeUnit.MINUTES, 1.0),
            Triple(0, TimeUnit.MINUTES, .0),
            Triple(10, TimeUnit.MINUTES, 10.0),

            Triple(1, TimeUnit.HOURS, 60.0),
            Triple(0, TimeUnit.HOURS, .0),
            Triple(10, TimeUnit.HOURS, 600.0),

            Triple(1, TimeUnit.DAYS, 1440.0),
            Triple(0, TimeUnit.DAYS, .0),
            Triple(10, TimeUnit.DAYS, 14400.0),

        )
    }
}
