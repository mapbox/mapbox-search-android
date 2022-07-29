package com.mapbox.search.location

import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.search.ViewportProvider
import com.mapbox.search.base.core.CoreLocationProvider
import com.mapbox.search.base.utils.extension.mapToPlatform
import com.mapbox.test.dsl.TestCase
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.TestFactory

internal class WrapperLocationProviderTest {

    @TestFactory
    fun `Check with empty location and viewport providers`() = TestCase {
        Given("WrapperLocationProvider with empty location provider") {
            val wrapperLocationProvider = WrapperLocationProvider(null, null)

            When("Get location") {
                val actualValue = wrapperLocationProvider.location
                Then("It should be <null>") {
                    Assertions.assertEquals(null, actualValue)
                }
            }

            When("Get viewport") {
                val actualValue = wrapperLocationProvider.viewport
                Then("It should be <null>") {
                    Assertions.assertEquals(null, actualValue)
                }
            }
        }
    }

    @TestFactory
    fun `Check location and viewport providers with empty data`() = TestCase {
        Given("WrapperLocationProvider with location with empty location") {
            val locationProvider = mockk<CoreLocationProvider>(relaxed = true)
            every { locationProvider.location } answers { null }

            val viewportProvider = mockk<ViewportProvider>()
            every { viewportProvider.getViewport() } answers { null }

            val wrapperLocationProvider = WrapperLocationProvider(locationProvider, viewportProvider)

            When("Get location") {
                val actualValue = wrapperLocationProvider.location
                Then("It should be <null>") {
                    Assertions.assertEquals(null, actualValue)
                }
            }

            When("Get viewport") {
                val actualValue = wrapperLocationProvider.viewport
                Then("It should be <null>") {
                    Assertions.assertEquals(null, actualValue)
                }
            }
        }
    }

    @TestFactory
    fun `Check not empty location and viewport providers`() = TestCase {
        Given("WrapperLocationProvider with empty location provider") {
            val locationProvider = mockk<CoreLocationProvider>(relaxed = true)
            every { locationProvider.location } answers { TEST_LOCATION }

            val viewportProvider = mockk<ViewportProvider>()
            every { viewportProvider.getViewport() } answers { TEST_VIEWPORT }

            val wrapperLocationProvider = WrapperLocationProvider(locationProvider, viewportProvider)

            When("Get location") {
                val actualValue = wrapperLocationProvider.location
                Then("It should be <$TEST_LOCATION>") {
                    Assertions.assertEquals(actualValue, TEST_LOCATION)
                }
            }

            When("Get viewport") {
                val actualValue = wrapperLocationProvider.viewport
                Then("It should be <$TEST_VIEWPORT>") {
                    Assertions.assertEquals(TEST_VIEWPORT, actualValue?.mapToPlatform())
                }
            }
        }
    }

    private companion object {
        val TEST_LOCATION: Point = Point.fromLngLat(27.0, 53.0)
        val TEST_VIEWPORT: BoundingBox = BoundingBox.fromPoints(
            Point.fromLngLat(27.0, 53.0),
            Point.fromLngLat(100.0, 100.0)
        )
    }
}
