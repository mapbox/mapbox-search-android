package com.mapbox.search.analytics.events

import android.content.Context
import com.mapbox.bindgen.Value
import com.mapbox.common.Event
import com.mapbox.common.EventsServiceInterface
import com.mapbox.common.location.Location
import com.mapbox.common.location.LocationProvider
import com.mapbox.geojson.Point
import com.mapbox.search.BuildConfig
import com.mapbox.search.ResponseInfo
import com.mapbox.search.analytics.AnalyticsEventJsonParser
import com.mapbox.search.analytics.AnalyticsServiceImpl
import com.mapbox.search.analytics.FeedbackEvent
import com.mapbox.search.analytics.MissingResultFeedbackEvent
import com.mapbox.search.analytics.SearchFeedbackEventsFactory
import com.mapbox.search.base.logger.reinitializeLogImpl
import com.mapbox.search.base.logger.resetLogImpl
import com.mapbox.search.base.result.mapToBase
import com.mapbox.search.common.CompletionCallback
import com.mapbox.search.common.tests.FixedPointLocationEngine
import com.mapbox.search.common.tests.TestExecutor
import com.mapbox.search.common.tests.catchThrowable
import com.mapbox.search.common.tests.createTestCoreSearchResponseSuccess
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.tests_support.createTestBaseSearchSuggestion
import com.mapbox.search.tests_support.createTestRequestOptions
import com.mapbox.test.dsl.TestCase
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestFactory
import java.util.concurrent.Executor
import kotlin.IllegalStateException

@Suppress("LargeClass")
internal class AnalyticsServiceImplTest {

    private lateinit var context: Context
    private lateinit var locationProvider: LocationProvider
    private lateinit var eventsService: EventsServiceInterface
    private lateinit var eventsJsonParser: AnalyticsEventJsonParser
    private lateinit var feedbackEventsFactory: SearchFeedbackEventsFactory
    private lateinit var analyticsServiceImpl: AnalyticsServiceImpl
    private lateinit var callbackExecutor: Executor

    private lateinit var validMockedFeedbackEvent: SearchFeedbackEvent
    private lateinit var invalidMockedFeedbackEvent: SearchFeedbackEvent

    @BeforeEach
    fun setUp() {
        val mockedLocation = mockk<Location>(relaxed = true)
        every { mockedLocation.latitude } returns TEST_LOCATION.latitude()
        every { mockedLocation.longitude } returns TEST_LOCATION.longitude()

        context = mockk(relaxed = true)
        locationProvider = FixedPointLocationEngine(mockedLocation)
        eventsService = mockk(relaxed = true)
        eventsJsonParser = mockk()
        feedbackEventsFactory = mockk(relaxed = true)
        callbackExecutor = spyk(TestExecutor())

        validMockedFeedbackEvent = mockk()
        every { validMockedFeedbackEvent.isValid } returns true

        invalidMockedFeedbackEvent = mockk()
        every { invalidMockedFeedbackEvent.isValid } returns false

        analyticsServiceImpl = AnalyticsServiceImpl(
            eventsService, eventsJsonParser, feedbackEventsFactory, locationProvider
        )

        every {
            eventsJsonParser.serialize(validMockedFeedbackEvent)
        } returns TEST_SERIALIZED_FEEDBACK_EVENT_RAW
    }

    @TestFactory
    fun `Send feedback for SearchSuggestion with valid data`() = TestCase {
        Given("AnalyticsService with mocked dependencies") {
            val searchSuggestion = SearchSuggestion(createTestBaseSearchSuggestion())

            val callbackSlot = slot<CompletionCallback<SearchFeedbackEvent>>()

            val mockConsumerCallback = mockk<CompletionCallback<Unit>>(relaxed = true)

            every {
                feedbackEventsFactory.createSearchFeedbackEvent(
                    baseRawSearchResult = searchSuggestion.base.rawSearchResult,
                    requestOptions = searchSuggestion.requestOptions,
                    searchResponse = TEST_RESPONSE_INFO.coreSearchResponse,
                    currentLocation = TEST_LOCATION,
                    isReproducible = true,
                    event = TEST_FEEDBACK_EVENT,
                    isCached = any(),
                    callback = capture(callbackSlot),
                )
            } answers {
                callbackSlot.captured.onComplete(validMockedFeedbackEvent)
            }

            When("Sending feedback for search suggestion with valid data") {
                analyticsServiceImpl.sendFeedback(
                    searchSuggestion,
                    TEST_RESPONSE_INFO,
                    TEST_FEEDBACK_EVENT,
                    mockConsumerCallback
                )

                VerifyOnce("Event serialized") {
                    eventsJsonParser.serialize(validMockedFeedbackEvent)
                }

                VerifyOnce("Event sent") {
                    eventsService.sendEvent(TEST_SERIALIZED_FEEDBACK_EVENT, any())
                }

                Verify("Consumer callback was called") {
                    mockConsumerCallback.onComplete(Unit)
                }
            }
        }
    }

    @TestFactory
    fun `Send feedback for SearchSuggestion with invalid data`() = TestCase {
        Given("AnalyticsService with mocked dependencies") {
            val searchSuggestion = SearchSuggestion(createTestBaseSearchSuggestion())
            val mockedFeedbackEvent = mockk<SearchFeedbackEvent>()

            val callbackSlot = slot<CompletionCallback<SearchFeedbackEvent>>()
            val mockConsumerCallback = mockk<CompletionCallback<Unit>>(relaxed = true)

            every {
                feedbackEventsFactory.createSearchFeedbackEvent(
                    baseRawSearchResult = searchSuggestion.base.rawSearchResult,
                    requestOptions = searchSuggestion.requestOptions,
                    searchResponse = TEST_RESPONSE_INFO.coreSearchResponse,
                    currentLocation = TEST_LOCATION,
                    isReproducible = true,
                    event = TEST_FEEDBACK_EVENT,
                    isCached = any(),
                    callback = capture(callbackSlot),
                )
            } answers {
                callbackSlot.captured.onComplete(mockedFeedbackEvent)
            }

            When("Sending feedback for search suggestion with invalid data") {
                every { mockedFeedbackEvent.isValid } returns false
                val caughtException = catchThrowable<IllegalStateException> {
                    analyticsServiceImpl.sendFeedback(
                        searchSuggestion,
                        TEST_RESPONSE_INFO,
                        TEST_FEEDBACK_EVENT,
                        mockConsumerCallback
                    )
                    throw IllegalStateException("Some illegal state") // This error should be thrown by sendFeedback.
                }

                VerifyNo("sendEvent() wasn't called") {
                    eventsService.sendEvent(any(), any())
                }
                Verify("Consumer callback was called") {
                    mockConsumerCallback.onError(any())
                }

                if (BuildConfig.DEBUG) {
                    Then("IllegalStateException was thrown") {
                        Assertions.assertTrue(caughtException is IllegalStateException)
                    }
                }
            }
        }
    }
//
//    @TestFactory
//    fun `Send feedback for SearchResult with valid data`() = TestCase {
//        Given("AnalyticsService with mocked dependencies") {
//            val searchResult = createTestSearchResult()
//
//            val callbackSlot = slot<CompletionCallback<SearchFeedbackEvent>>()
//            every {
//                feedbackEventsFactory.createSearchFeedbackEvent(
//                    baseRawSearchResult = searchResult.base.rawSearchResult,
//                    requestOptions = searchResult.requestOptions,
//                    searchResponse = TEST_RESPONSE_INFO.coreSearchResponse,
//                    currentLocation = TEST_LOCATION,
//                    isReproducible = true,
//                    event = TEST_FEEDBACK_EVENT,
//                    isCached = any(),
//                    callback = capture(callbackSlot),
//                )
//            } answers {
//                callbackSlot.captured.onComplete(validMockedFeedbackEvent)
//            }
//
//            When("Sending feedback for search result with valid data") {
//                analyticsServiceImpl.sendFeedback(searchResult, TEST_RESPONSE_INFO, TEST_FEEDBACK_EVENT)
//
//                VerifyOnce("Event serialized") {
//                    eventsJsonParser.serialize(validMockedFeedbackEvent)
//                }
//
//                VerifyOnce("Event sent") {
//                    eventsService.sendEvent(TEST_SERIALIZED_FEEDBACK_EVENT, any())
//                }
//            }
//        }
//    }
//
//    @TestFactory
//    fun `Send feedback for SearchResult with invalid data`() = TestCase {
//        Given("AnalyticsService with mocked dependencies") {
//            val searchResult = createTestSearchResult()
//
//            val callbackSlot = slot<CompletionCallback<SearchFeedbackEvent>>()
//
//            every {
//                feedbackEventsFactory.createSearchFeedbackEvent(
//                    baseRawSearchResult = searchResult.base.rawSearchResult,
//                    requestOptions = searchResult.requestOptions,
//                    searchResponse = TEST_RESPONSE_INFO.coreSearchResponse,
//                    currentLocation = TEST_LOCATION,
//                    isReproducible = true,
//                    event = TEST_FEEDBACK_EVENT,
//                    isCached = any(),
//                    callback = capture(callbackSlot),
//                )
//            } answers {
//                callbackSlot.captured.onComplete(invalidMockedFeedbackEvent)
//            }
//
//            When("Sending feedback for search result with invalid data") {
//                val caughtException = catchThrowable<Throwable> {
//                    analyticsServiceImpl.sendFeedback(searchResult, TEST_RESPONSE_INFO, TEST_FEEDBACK_EVENT)
//                }
//
//                Verify("Events service wasn't called") { eventsService wasNot Called }
//
//                if (BuildConfig.DEBUG) {
//                    Then("IllegalStateException was thrown") {
//                        Assertions.assertTrue(caughtException is IllegalStateException)
//                    }
//                }
//            }
//        }
//    }
//
//    @TestFactory
//    fun `Send feedback for IndexableRecord with valid data`() = TestCase {
//        Given("AnalyticsService with mocked dependencies") {
//            val favoriteRecord = createTestFavoriteRecord()
//            every {
//                feedbackEventsFactory.createSearchFeedbackEvent(favoriteRecord, TEST_FEEDBACK_EVENT, TEST_LOCATION)
//            } returns validMockedFeedbackEvent
//
//            When("Sending feedback for indexable record with valid data") {
//                analyticsServiceImpl.sendFeedback(favoriteRecord, TEST_FEEDBACK_EVENT)
//
//                VerifyOnce("Event serialized") {
//                    eventsJsonParser.serialize(validMockedFeedbackEvent)
//                }
//
//                VerifyOnce("Event sent") {
//                    eventsService.sendEvent(TEST_SERIALIZED_FEEDBACK_EVENT, any())
//                }
//            }
//        }
//    }
//
//    @TestFactory
//    fun `Send feedback for IndexableRecord with invalid data`() = TestCase {
//        Given("AnalyticsService with mocked dependencies") {
//            val favoriteRecord = createTestFavoriteRecord()
//            every {
//                feedbackEventsFactory.createSearchFeedbackEvent(favoriteRecord, TEST_FEEDBACK_EVENT, TEST_LOCATION)
//            } returns invalidMockedFeedbackEvent
//
//            When("Sending feedback for indexable record with invalid data") {
//                val caughtException = catchThrowable<Throwable> {
//                    analyticsServiceImpl.sendFeedback(favoriteRecord, TEST_FEEDBACK_EVENT)
//                    throw IllegalStateException("Some illegal state")
//                }
//
//                Verify("Events service wasn't called") { eventsService wasNot Called }
//
//                if (BuildConfig.DEBUG) {
//                    Then("IllegalStateException was thrown") {
//                        Assertions.assertTrue(caughtException is IllegalStateException)
//                    }
//                }
//            }
//        }
//    }
//
//    @TestFactory
//    fun `Send missing result feedback for ResponseInfo with valid data`() = TestCase {
//        Given("AnalyticsService with mocked dependencies") {
//            val callbackSlot = slot<CompletionCallback<SearchFeedbackEvent>>()
//            every {
//                feedbackEventsFactory.createSearchFeedbackEvent(TEST_MISSING_RESULT_FEEDBACK_EVENT, TEST_LOCATION, capture(callbackSlot))
//            } answers {
//                callbackSlot.captured.onComplete(validMockedFeedbackEvent)
//            }
//
//            When("Sending feedback for response info with valid data") {
//                analyticsServiceImpl.sendMissingResultFeedback(TEST_MISSING_RESULT_FEEDBACK_EVENT)
//
//                VerifyOnce("Event serialized") {
//                    eventsJsonParser.serialize(validMockedFeedbackEvent)
//                }
//
//                VerifyOnce("Event sent") {
//                    eventsService.sendEvent(TEST_SERIALIZED_FEEDBACK_EVENT, any())
//                }
//            }
//        }
//    }
//
//    @TestFactory
//    fun `Send missing result feedback for ResponseInfo with invalid data`() = TestCase {
//        Given("AnalyticsService with mocked dependencies") {
//            val callbackSlot = slot<CompletionCallback<SearchFeedbackEvent>>()
//            every {
//                feedbackEventsFactory.createSearchFeedbackEvent(TEST_MISSING_RESULT_FEEDBACK_EVENT, TEST_LOCATION, capture(callbackSlot))
//            } answers {
//                callbackSlot.captured.onComplete(invalidMockedFeedbackEvent)
//            }
//
//            When("Sending feedback for indexable record with invalid data") {
//                val caughtException = catchThrowable<Throwable> {
//                    analyticsServiceImpl.sendMissingResultFeedback(TEST_MISSING_RESULT_FEEDBACK_EVENT)
//                }
//
//                Verify("Events service wasn't called") { eventsService wasNot Called }
//
//                if (BuildConfig.DEBUG) {
//                    Then("IllegalStateException was thrown") {
//                        Assertions.assertTrue(caughtException is IllegalStateException)
//                    }
//                }
//            }
//        }
//    }
//
//    @TestFactory
//    fun `Create event raw feedback event`() = TestCase {
//        Given("AnalyticsService with mocked dependencies") {
//            val searchResult = createTestSearchResult()
//            val mockedFeedbackEvent = mockk<SearchFeedbackEvent>()
//
//            val callbackSlot = slot<CompletionCallback<SearchFeedbackEvent>>()
//
//            every {
//                feedbackEventsFactory.createSearchFeedbackEvent(
//                    baseRawSearchResult = any(),
//                    requestOptions = any(),
//                    searchResponse = any(),
//                    currentLocation = null,
//                    isReproducible = true,
//                    event = null,
//                    isCached = any(),
//                    asTemplate = true,
//                    callback = capture(callbackSlot)
//                )
//            } answers {
//                callbackSlot.captured.onComplete(mockedFeedbackEvent)
//            }
//
//            every { eventsJsonParser.serialize(mockedFeedbackEvent) } returns TEST_RAW_EVENT
//
//            When("Creating raw event for SearchResult") {
//                val callback = BlockingCompletionCallback<String>()
//
//                @Suppress("DEPRECATION")
//                analyticsServiceImpl.createRawFeedbackEvent(searchResult, TEST_RESPONSE_INFO, callbackExecutor, callback)
//
//                Then(
//                    "Raw event was successfully created",
//                    callback.getResultBlocking().requireResult(),
//                    TEST_RAW_EVENT
//                )
//
//                Verify("Callback called inside executor", exactly = 2) {
//                    callbackExecutor.execute(any())
//                }
//            }
//
//            When("Creating raw event for SearchSuggestion") {
//                val callback = BlockingCompletionCallback<String>()
//
//                val searchSuggestion = SearchSuggestion(createTestBaseSearchSuggestion())
//
//                @Suppress("DEPRECATION")
//                analyticsServiceImpl.createRawFeedbackEvent(
//                    searchSuggestion,
//                    TEST_RESPONSE_INFO,
//                    callbackExecutor,
//                    callback
//                )
//
//                Then(
//                    "Raw event was successfully created",
//                    callback.getResultBlocking().requireResult(),
//                    TEST_RAW_EVENT
//                )
//
//                Verify("Callback called inside executor", exactly = 2) {
//                    callbackExecutor.execute(any())
//                }
//            }
//        }
//    }

    private companion object {

        const val TEST_RAW_EVENT = "{\"event\":\"search.feedback\"}"

        const val TEST_SERIALIZED_FEEDBACK_EVENT_RAW = "{\"event\":\"search.feedback\"}"
        val TEST_SERIALIZED_FEEDBACK_EVENT = Event(requireNotNull(Value.fromJson(TEST_SERIALIZED_FEEDBACK_EVENT_RAW).value), null)

        val TEST_LOCATION: Point = Point.fromLngLat(10.0, 20.0)
        val TEST_FEEDBACK_EVENT = FeedbackEvent("Missing routable point", "Fix, please!")
        val TEST_RESPONSE_INFO = ResponseInfo(
            requestOptions = createTestRequestOptions(),
            coreSearchResponse = createTestCoreSearchResponseSuccess().mapToBase(),
            isReproducible = true,
        )
        val TEST_MISSING_RESULT_FEEDBACK_EVENT = MissingResultFeedbackEvent(
            TEST_RESPONSE_INFO,
            "Fix, please!"
        )

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
