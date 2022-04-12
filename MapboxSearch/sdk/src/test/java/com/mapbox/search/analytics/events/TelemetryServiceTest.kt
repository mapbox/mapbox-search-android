package com.mapbox.search.analytics.events

import android.content.Context
import android.location.Location
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.android.telemetry.MapboxCrashReporter
import com.mapbox.android.telemetry.MapboxTelemetry
import com.mapbox.geojson.Point
import com.mapbox.search.BuildConfig
import com.mapbox.search.ResponseInfo
import com.mapbox.search.analytics.AnalyticsEventJsonParser
import com.mapbox.search.analytics.FeedbackEvent
import com.mapbox.search.analytics.MissingResultFeedbackEvent
import com.mapbox.search.analytics.TelemetrySearchEventsFactory
import com.mapbox.search.analytics.TelemetryService
import com.mapbox.search.common.FixedPointLocationEngine
import com.mapbox.search.result.mapToPlatform
import com.mapbox.search.tests_support.catchThrowable
import com.mapbox.search.tests_support.createTestCoreSearchResponseSuccess
import com.mapbox.search.tests_support.createTestFavoriteRecord
import com.mapbox.search.tests_support.createTestRequestOptions
import com.mapbox.search.tests_support.createTestSearchResult
import com.mapbox.search.tests_support.createTestSuggestion
import com.mapbox.test.dsl.TestCase
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestFactory
import java.lang.IllegalStateException

@Suppress("LargeClass")
internal class TelemetryServiceTest {

    private lateinit var context: Context
    private lateinit var locationEngine: LocationEngine
    private lateinit var mapBoxTelemetry: MapboxTelemetry
    private lateinit var eventsJsonParser: AnalyticsEventJsonParser
    private lateinit var eventsFactory: TelemetrySearchEventsFactory
    private lateinit var crashReporter: MapboxCrashReporter
    private lateinit var telemetryService: TelemetryService

    @BeforeEach
    fun setUp() {
        val mockedLocation = mockk<Location>(relaxed = true)
        every { mockedLocation.latitude } returns TEST_LOCATION.latitude()
        every { mockedLocation.longitude } returns TEST_LOCATION.longitude()

        context = mockk(relaxed = true)
        locationEngine = FixedPointLocationEngine(mockedLocation)
        mapBoxTelemetry = mockk(relaxed = true)
        eventsJsonParser = mockk()
        eventsFactory = mockk(relaxed = true)
        crashReporter = mockk()

        mockkStatic(PermissionsManager::class)
        every { PermissionsManager.areLocationPermissionsGranted(any()) } returns true }

    @AfterEach
    fun tearDown() {
        unmockkStatic(PermissionsManager::class)
    }

    @TestFactory
    fun `Send feedback for SearchSuggestion with valid data`() = TestCase {
        Given("TelemetryAnalyticsService with mocked dependencies") {
            val searchSuggestion = createTestSuggestion()
            val mockedFeedbackEvent = mockk<SearchFeedbackEvent>()
            every {
                eventsFactory.createSearchFeedbackEvent(
                    originalSearchResult = searchSuggestion.originalSearchResult,
                    requestOptions = searchSuggestion.requestOptions,
                    searchResponse = TEST_RESPONSE_INFO.coreSearchResponse,
                    currentLocation = TEST_LOCATION,
                    isReproducible = true,
                    event = TEST_FEEDBACK_EVENT,
                    isCached = any(),
                )
            } returns mockedFeedbackEvent
            telemetryService = TelemetryService(
                context, mapBoxTelemetry, locationEngine, eventsJsonParser, eventsFactory, crashReporter
            )

            When("Sending feedback for search suggestion with valid data") {
                every { mockedFeedbackEvent.isValid } returns true
                telemetryService.sendFeedback(searchSuggestion, TEST_RESPONSE_INFO, TEST_FEEDBACK_EVENT)

                VerifyOnce("Telemetry called once") { mapBoxTelemetry.push(mockedFeedbackEvent) }
            }
        }
    }

    @TestFactory
    fun `Send feedback for SearchSuggestion with invalid data`() = TestCase {
        Given("TelemetryAnalyticsService with mocked dependencies") {
            val searchSuggestion = createTestSuggestion()
            val mockedFeedbackEvent = mockk<SearchFeedbackEvent>()
            every {
                eventsFactory.createSearchFeedbackEvent(
                    originalSearchResult = searchSuggestion.originalSearchResult,
                    requestOptions = searchSuggestion.requestOptions,
                    searchResponse = TEST_RESPONSE_INFO.coreSearchResponse,
                    currentLocation = TEST_LOCATION,
                    isReproducible = true,
                    event = TEST_FEEDBACK_EVENT,
                    isCached = any(),
                )
            } returns mockedFeedbackEvent
            telemetryService = TelemetryService(
                context, mapBoxTelemetry, locationEngine, eventsJsonParser, eventsFactory, crashReporter
            )

            When("Sending feedback for search suggestion with invalid data") {
                every { mockedFeedbackEvent.isValid } returns false
                val caughtException = catchThrowable<IllegalStateException> {
                    telemetryService.sendFeedback(searchSuggestion, TEST_RESPONSE_INFO, TEST_FEEDBACK_EVENT)
                }

                Verify("Telemetry isn't called") { mapBoxTelemetry wasNot Called }

                if (BuildConfig.DEBUG) {
                    Then("IllegalStateException was thrown") {
                        Assertions.assertTrue(caughtException is IllegalStateException)
                    }
                }
            }
        }
    }

    @TestFactory
    fun `Send feedback for SearchResult with valid data`() = TestCase {
        Given("TelemetryAnalyticsService with mocked dependencies") {
            val searchResult = createTestSearchResult()
            val mockedFeedbackEvent = mockk<SearchFeedbackEvent>()
            every {
                eventsFactory.createSearchFeedbackEvent(
                    originalSearchResult = searchResult.originalSearchResult,
                    requestOptions = searchResult.requestOptions,
                    searchResponse = TEST_RESPONSE_INFO.coreSearchResponse,
                    currentLocation = TEST_LOCATION,
                    isReproducible = true,
                    event = TEST_FEEDBACK_EVENT,
                    isCached = any(),
                )
            } returns mockedFeedbackEvent

            telemetryService = TelemetryService(
                context, mapBoxTelemetry, locationEngine, eventsJsonParser, eventsFactory, crashReporter
            )

            When("Sending feedback for search result with valid data") {
                every { mockedFeedbackEvent.isValid } returns true
                telemetryService.sendFeedback(searchResult, TEST_RESPONSE_INFO, TEST_FEEDBACK_EVENT)

                VerifyOnce("Telemetry called once") { mapBoxTelemetry.push(mockedFeedbackEvent) }
            }
        }
    }

    @TestFactory
    fun `Send feedback for SearchResult with invalid data`() = TestCase {
        Given("TelemetryAnalyticsService with mocked dependencies") {
            val searchResult = createTestSearchResult()
            val mockedFeedbackEvent = mockk<SearchFeedbackEvent>()
            every {
                eventsFactory.createSearchFeedbackEvent(
                    originalSearchResult = searchResult.originalSearchResult,
                    requestOptions = searchResult.requestOptions,
                    searchResponse = TEST_RESPONSE_INFO.coreSearchResponse,
                    currentLocation = TEST_LOCATION,
                    isReproducible = true,
                    event = TEST_FEEDBACK_EVENT,
                    isCached = any(),
                )
            } returns mockedFeedbackEvent
            telemetryService = TelemetryService(
                context, mapBoxTelemetry, locationEngine, eventsJsonParser, eventsFactory, crashReporter
            )

            When("Sending feedback for search result with invalid data") {
                every { mockedFeedbackEvent.isValid } returns false
                val caughtException = catchThrowable<Throwable> {
                    telemetryService.sendFeedback(searchResult, TEST_RESPONSE_INFO, TEST_FEEDBACK_EVENT)
                }

                Verify("Telemetry isn't called") { mapBoxTelemetry wasNot Called }

                if (BuildConfig.DEBUG) {
                    Then("IllegalStateException was thrown") {
                        Assertions.assertTrue(caughtException is IllegalStateException)
                    }
                }
            }
        }
    }

    @TestFactory
    fun `Send feedback for IndexableRecord with valid data`() = TestCase {
        Given("TelemetryAnalyticsService with mocked dependencies") {
            val favoriteRecord = createTestFavoriteRecord()
            val mockedFeedbackEvent = mockk<SearchFeedbackEvent>()
            every {
                eventsFactory.createSearchFeedbackEvent(favoriteRecord, TEST_FEEDBACK_EVENT, TEST_LOCATION)
            } returns mockedFeedbackEvent
            telemetryService = TelemetryService(
                context, mapBoxTelemetry, locationEngine, eventsJsonParser, eventsFactory, crashReporter
            )

            When("Sending feedback for indexable record with valid data") {
                every { mockedFeedbackEvent.isValid } returns true
                telemetryService.sendFeedback(favoriteRecord, TEST_FEEDBACK_EVENT)

                VerifyOnce("Telemetry called once") { mapBoxTelemetry.push(mockedFeedbackEvent) }
            }
        }
    }

    @TestFactory
    fun `Send feedback for IndexableRecord with invalid data`() = TestCase {
        Given("TelemetryAnalyticsService with mocked dependencies") {
            val favoriteRecord = createTestFavoriteRecord()
            val mockedFeedbackEvent = mockk<SearchFeedbackEvent>()
            every {
                eventsFactory.createSearchFeedbackEvent(favoriteRecord, TEST_FEEDBACK_EVENT, TEST_LOCATION)
            } returns mockedFeedbackEvent
            telemetryService = TelemetryService(
                context, mapBoxTelemetry, locationEngine, eventsJsonParser, eventsFactory, crashReporter
            )

            When("Sending feedback for indexable record with invalid data") {
                every { mockedFeedbackEvent.isValid } returns false
                val caughtException = catchThrowable<Throwable> {
                    telemetryService.sendFeedback(favoriteRecord, TEST_FEEDBACK_EVENT)
                }

                Verify("Telemetry isn't called") { mapBoxTelemetry wasNot Called }

                if (BuildConfig.DEBUG) {
                    Then("IllegalStateException was thrown") {
                        Assertions.assertTrue(caughtException is IllegalStateException)
                    }
                }
            }
        }
    }

    @TestFactory
    fun `Send missing result feedback for ResponseInfo with valid data`() = TestCase {
        Given("TelemetryAnalyticsService with mocked dependencies") {
            val mockedFeedbackEvent = mockk<SearchFeedbackEvent>()
            every {
                eventsFactory.createSearchFeedbackEvent(TEST_MISSING_RESULT_FEEDBACK_EVENT, TEST_LOCATION)
            } returns mockedFeedbackEvent
            telemetryService = TelemetryService(
                context, mapBoxTelemetry, locationEngine, eventsJsonParser, eventsFactory, crashReporter
            )

            When("Sending feedback for response info with valid data") {
                every { mockedFeedbackEvent.isValid } returns true
                telemetryService.sendMissingResultFeedback(TEST_MISSING_RESULT_FEEDBACK_EVENT)

                VerifyOnce("Telemetry called once") { mapBoxTelemetry.push(mockedFeedbackEvent) }
            }
        }
    }

    @TestFactory
    fun `Send missing result feedback for ResponseInfo with invalid data`() = TestCase {
        Given("TelemetryAnalyticsService with mocked dependencies") {
            val mockedFeedbackEvent = mockk<SearchFeedbackEvent>()
            every {
                eventsFactory.createSearchFeedbackEvent(TEST_MISSING_RESULT_FEEDBACK_EVENT, TEST_LOCATION)
            } returns mockedFeedbackEvent
            telemetryService = TelemetryService(
                context, mapBoxTelemetry, locationEngine, eventsJsonParser, eventsFactory, crashReporter
            )

            When("Sending feedback for indexable record with invalid data") {
                every { mockedFeedbackEvent.isValid } returns false
                val caughtException = catchThrowable<Throwable> {
                    telemetryService.sendMissingResultFeedback(TEST_MISSING_RESULT_FEEDBACK_EVENT)
                }

                Verify("Telemetry isn't called") { mapBoxTelemetry wasNot Called }

                if (BuildConfig.DEBUG) {
                    Then("IllegalStateException was thrown") {
                        Assertions.assertTrue(caughtException is IllegalStateException)
                    }
                }
            }
        }
    }

    @TestFactory
    fun `Create event raw feedback event`() = TestCase {
        Given("TelemetryAnalyticsService with mocked dependencies") {
            val searchResult = createTestSearchResult()
            val mockedFeedbackEvent = mockk<SearchFeedbackEvent>()
            every {
                eventsFactory.createSearchFeedbackEvent(
                    originalSearchResult = any(),
                    requestOptions = any(),
                    searchResponse = any(),
                    currentLocation = null,
                    isReproducible = true,
                    event = null,
                    isCached = any(),
                    asTemplate = true,
                )
            } returns mockedFeedbackEvent
            every { eventsJsonParser.serialize(mockedFeedbackEvent) } returns TEST_RAW_EVENT
            telemetryService = TelemetryService(
                context, mapBoxTelemetry, locationEngine, eventsJsonParser, eventsFactory, crashReporter
            )

            When("Creating raw event for SearchResult") {
                val rawEvent = telemetryService.createRawFeedbackEvent(searchResult, TEST_RESPONSE_INFO)

                Then("Raw event was successfully created", rawEvent, TEST_RAW_EVENT)
            }

            When("Creating raw event for SearchSuggestion") {
                val searchSuggestion = createTestSuggestion()
                val rawEvent = telemetryService.createRawFeedbackEvent(searchSuggestion, TEST_RESPONSE_INFO)

                Then("Raw event was successfully created", rawEvent, TEST_RAW_EVENT)
            }
        }
    }

    @TestFactory
    fun `Send feedback for valid raw feedback event`() = TestCase {
        Given("TelemetryAnalyticsService with mocked dependencies") {
            val searchResult = createTestSearchResult()
            val mockedFeedbackEvent = mockk<SearchFeedbackEvent>()
            every {
                eventsFactory.createSearchFeedbackEvent(
                    originalSearchResult = any(),
                    requestOptions = any(),
                    searchResponse = any(),
                    currentLocation = null,
                    isReproducible = true,
                    event = null,
                    isCached = any(),
                    asTemplate = true,
                )
            } returns mockedFeedbackEvent
            every { eventsJsonParser.serialize(mockedFeedbackEvent) } returns TEST_RAW_EVENT
            telemetryService = TelemetryService(
                context, mapBoxTelemetry, locationEngine, eventsJsonParser, eventsFactory, crashReporter
            )

            When("Sending feedback for valid raw feedback event") {
                every { eventsJsonParser.parse(TEST_RAW_EVENT) } returns mockedFeedbackEvent
                every { mockedFeedbackEvent.isValid } returns true
                val rawEvent = telemetryService.createRawFeedbackEvent(searchResult, TEST_RESPONSE_INFO)
                telemetryService.sendRawFeedbackEvent(rawEvent, TEST_FEEDBACK_EVENT)

                VerifyOnce("Telemetry called once") { mapBoxTelemetry.push(mockedFeedbackEvent) }
            }
        }
    }

    @TestFactory
    fun `Send feedback for invalid raw feedback event`() = TestCase {
        Given("TelemetryAnalyticsService with mocked dependencies") {
            val searchResult = createTestSearchResult()
            val mockedFeedbackEvent = mockk<SearchFeedbackEvent>()
            every {
                eventsFactory.createSearchFeedbackEvent(
                    originalSearchResult = any(),
                    requestOptions = any(),
                    searchResponse = any(),
                    currentLocation = null,
                    isReproducible = true,
                    event = null,
                    isCached = any(),
                    asTemplate = true,
                )
            } returns mockedFeedbackEvent
            every { eventsJsonParser.serialize(mockedFeedbackEvent) } returns TEST_RAW_EVENT
            telemetryService = TelemetryService(
                context, mapBoxTelemetry, locationEngine, eventsJsonParser, eventsFactory, crashReporter
            )

            When("Sending feedback for invalid raw feedback event") {
                every { eventsJsonParser.parse(TEST_RAW_EVENT) } throws IllegalArgumentException()
                every { mockedFeedbackEvent.isValid } returns false
                val rawEvent = telemetryService.createRawFeedbackEvent(searchResult, TEST_RESPONSE_INFO)
                telemetryService.sendRawFeedbackEvent(rawEvent, TEST_FEEDBACK_EVENT)

                VerifyNo("Event factory isn't called") {
                    eventsFactory.updateCachedSearchFeedbackEvent(any(), any(), TEST_LOCATION)
                }

                VerifyNo("Telemetry isn't RequestOptionscalled") {
                    mapBoxTelemetry.push(any())
                }
            }
        }
    }

    private companion object {

        const val TEST_RAW_EVENT = "{\"event\":\"search.feedback\"}"
        val TEST_LOCATION: Point = Point.fromLngLat(10.0, 20.0)
        val TEST_FEEDBACK_EVENT = FeedbackEvent("Missing routable point", "Fix, please!")
        val TEST_RESPONSE_INFO = ResponseInfo(
            requestOptions = createTestRequestOptions(),
            coreSearchResponse = createTestCoreSearchResponseSuccess().mapToPlatform(),
            isReproducible = true,
        )
        val TEST_MISSING_RESULT_FEEDBACK_EVENT = MissingResultFeedbackEvent(
            TEST_RESPONSE_INFO,
            "Fix, please!"
        )
    }
}
