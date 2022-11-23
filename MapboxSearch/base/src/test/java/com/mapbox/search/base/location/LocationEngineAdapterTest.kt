package com.mapbox.search.base.location

import android.app.Application
import android.location.Location
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.geojson.Point
import com.mapbox.search.base.logger.reinitializeLogImpl
import com.mapbox.search.base.logger.resetLogImpl
import com.mapbox.search.base.utils.TimeProvider
import com.mapbox.test.dsl.TestCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestFactory

internal class LocationEngineAdapterTest {

    private lateinit var app: Application
    private lateinit var engine: LocationEngine
    private lateinit var timeProvider: TimeProvider

    private lateinit var adapter: LocationEngineAdapter

    private lateinit var locationPermissionChecker: (Application) -> Boolean

    @BeforeEach
    fun setUp() {
        app = mockk()
        engine = mockk(relaxed = true)
        timeProvider = mockk()
        locationPermissionChecker = mockk()
        every { locationPermissionChecker.invoke(any()) } returns true

        every { timeProvider.currentTimeMillis() } returns TIME_MILLIS
    }

    @TestFactory
    fun `Requests latest location at initialization if location permission granted`() = TestCase {
        Given("LocationEngineAdapter with mocked dependencies") {
            val callbackSlot = slot<LocationEngineCallback<LocationEngineResult>>()
            every { engine.getLastLocation(capture(callbackSlot)) } answers {
                callbackSlot.captured.onSuccess(LocationEngineResult.create(createMockedLocation(LOCATION)))
            }

            adapter = LocationEngineAdapter(app, engine, timeProvider, locationPermissionChecker)

            When("LocationEngineAdapter instantiated") {
                Verify("Location permissions checked") {
                    locationPermissionChecker(app)
                }

                VerifyOnce("Last known location requested") {
                    engine.getLastLocation(callbackSlot.captured)
                }

                Then("Adapter returns correct location", LOCATION, adapter.location)
            }
        }
    }

    @TestFactory
    fun `Requests new location if there's no last known location`() = TestCase {
        Given("LocationEngineAdapter with mocked dependencies") {
            val lastLocationCallbackSlot = slot< LocationEngineCallback<LocationEngineResult>>()
            every { engine.getLastLocation(capture(lastLocationCallbackSlot)) } answers {
                lastLocationCallbackSlot.captured.onSuccess(LocationEngineResult.create(emptyList()))
            }

            val locationUpdatesCallbackSlot = slot< LocationEngineCallback<LocationEngineResult>>()
            every { engine.requestLocationUpdates(any(), capture(locationUpdatesCallbackSlot), any()) } answers {
                locationUpdatesCallbackSlot.captured.onSuccess(LocationEngineResult.create(createMockedLocation(LOCATION)))
            }

            adapter = LocationEngineAdapter(app, engine, timeProvider, locationPermissionChecker)

            When("LocationEngineAdapter instantiated") {
                Verify("Location permissions checked") {
                    locationPermissionChecker(app)
                }

                VerifyOnce("Last known location requested") {
                    engine.getLastLocation(lastLocationCallbackSlot.captured)
                }

                VerifyOnce("Location updates requested") {
                    engine.requestLocationUpdates(any(), locationUpdatesCallbackSlot.captured, any())
                }

                VerifyOnce("Unsubscribes from location updates when location is received") {
                    engine.removeLocationUpdates(locationUpdatesCallbackSlot.captured)
                }

                Then("Adapter returns correct location", LOCATION, adapter.location)
            }
        }
    }

    @TestFactory
    fun `Requests new location if initial location request failed`() = TestCase {
        Given("LocationEngineAdapter with mocked dependencies") {
            val lastLocationCallbackSlot = slot< LocationEngineCallback<LocationEngineResult>>()
            every { engine.getLastLocation(capture(lastLocationCallbackSlot)) } answers {
                lastLocationCallbackSlot.captured.onFailure(Exception())
            }

            val locationUpdatesCallbackSlot = slot< LocationEngineCallback<LocationEngineResult>>()
            every { engine.requestLocationUpdates(any(), capture(locationUpdatesCallbackSlot), any()) } answers {
                locationUpdatesCallbackSlot.captured.onSuccess(LocationEngineResult.create(createMockedLocation(LOCATION)))
            }

            adapter = LocationEngineAdapter(app, engine, timeProvider, locationPermissionChecker)

            When("LocationEngineAdapter instantiated") {
                Verify("Location permissions checked") {
                    locationPermissionChecker(app)
                }

                VerifyOnce("Last known location requested") {
                    engine.getLastLocation(lastLocationCallbackSlot.captured)
                }

                VerifyOnce("Location updates requested") {
                    engine.requestLocationUpdates(any(), locationUpdatesCallbackSlot.captured, any())
                }

                VerifyOnce("Unsubscribes from location updates when location is received") {
                    engine.removeLocationUpdates(locationUpdatesCallbackSlot.captured)
                }

                Then("Adapter returns correct location", LOCATION, adapter.location)
            }
        }
    }

    private companion object {

        const val TIME_MILLIS = 123L

        val LOCATION: Point = Point.fromLngLat(10.0, 20.0)

        private fun createMockedLocation(point: Point): Location = mockk<Location>().apply {
            every { longitude } returns point.longitude()
            every { latitude } returns point.latitude()
        }

        @Suppress("JVM_STATIC_IN_PRIVATE_COMPANION")
        @BeforeAll
        @JvmStatic
        fun setUpAll() {
            resetLogImpl()
        }

        @Suppress("JVM_STATIC_IN_PRIVATE_COMPANION")
        @AfterAll
        @JvmStatic
        fun tearDownAll() {
            reinitializeLogImpl()
        }
    }
}
