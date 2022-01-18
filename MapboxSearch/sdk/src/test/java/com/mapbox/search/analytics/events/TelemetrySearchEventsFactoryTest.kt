package com.mapbox.search.analytics.events

import android.graphics.Bitmap
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.search.ApiType
import com.mapbox.search.BuildConfig
import com.mapbox.search.Country
import com.mapbox.search.EtaType
import com.mapbox.search.Language
import com.mapbox.search.QueryType
import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchNavigationOptions
import com.mapbox.search.SearchNavigationProfile
import com.mapbox.search.SearchOptions
import com.mapbox.search.ViewportProvider
import com.mapbox.search.analytics.AnalyticsEventJsonParser
import com.mapbox.search.analytics.FeedbackEvent
import com.mapbox.search.analytics.MissingResultFeedbackEvent
import com.mapbox.search.analytics.TelemetrySearchEventsFactory
import com.mapbox.search.core.CoreSearchEngineInterface
import com.mapbox.search.location.calculateMapZoom
import com.mapbox.search.record.FavoriteRecord
import com.mapbox.search.record.HistoryRecord
import com.mapbox.search.result.GeocodingCompatSearchSuggestion
import com.mapbox.search.result.IndexableRecordSearchResult
import com.mapbox.search.result.IndexableRecordSearchResultImpl
import com.mapbox.search.result.IndexableRecordSearchSuggestion
import com.mapbox.search.result.OriginalResultType
import com.mapbox.search.result.SearchAddress
import com.mapbox.search.result.SearchRequestContext
import com.mapbox.search.result.SearchResultSuggestAction
import com.mapbox.search.result.SearchResultType
import com.mapbox.search.result.ServerSearchResultImpl
import com.mapbox.search.result.ServerSearchSuggestion
import com.mapbox.search.result.mapToCore
import com.mapbox.search.tests_support.StubIndexableRecord
import com.mapbox.search.tests_support.assertEqualsJsonify
import com.mapbox.search.tests_support.createTestCoreSearchResponse
import com.mapbox.search.tests_support.createTestOriginalSearchResult
import com.mapbox.search.tests_support.createTestRequestOptions
import com.mapbox.search.utils.FormattedTimeProvider
import com.mapbox.search.utils.UUIDProvider
import com.mapbox.search.utils.orientation.ScreenOrientation
import com.mapbox.test.dsl.TestCase
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestFactory
import java.util.Locale

@Suppress("LargeClass")
internal class TelemetrySearchEventsFactoryTest {

    private val viewportProvider = ViewportProvider { TEST_VIEWPORT }
    private val formattedDateProvider = FormattedTimeProvider { TEST_EVENT_CREATION_DATE }
    private val uuidProvider = UUIDProvider { TEST_UUID }
    private val bitmapEncoder: (Bitmap) -> String = { TEST_ENCODED_BITMAP }
    private lateinit var eventJsonParser: AnalyticsEventJsonParser
    private lateinit var coreEngine: CoreSearchEngineInterface
    private lateinit var mockBitmap: Bitmap
    private lateinit var jsonSerializer: (Any) -> String

    private lateinit var eventsFactory: TelemetrySearchEventsFactory

    @BeforeEach
    fun setUp() {
        coreEngine = mockk()
        eventJsonParser = mockk()
        mockBitmap = mockk()
        jsonSerializer = mockk()

        every { coreEngine.makeFeedbackEvent(any(), any()) } returns TEST_CORE_RAW_EVENT
        every { jsonSerializer.invoke(ofType<SearchResultsInfo>()) } returns TEST_SEARCH_RESULTS_INFO_JSON

        eventsFactory = createEventsFactory()
    }

    private fun createEventsFactory(): TelemetrySearchEventsFactory {
        return TelemetrySearchEventsFactory(
            TEST_USER_AGENT, viewportProvider, uuidProvider, { coreEngine },
            eventJsonParser, formattedDateProvider, jsonSerializer, bitmapEncoder
        )
    }

    @TestFactory
    fun `Check SearchResults converts to SearchFeedbackEvent`() = TestCase {
        Given("TelemetrySearchEventsFactory with mocked dependencies") {
            every { eventJsonParser.parse(TEST_CORE_RAW_EVENT) } answers {
                SearchFeedbackEvent().apply {
                    event = SearchFeedbackEvent.EVENT_NAME
                    endpoint = TEST_ENDPOINT
                    requestParamsJson = TEST_REQUEST_PARAMS_JSON
                }
            }

            eventsFactory = createEventsFactory()

            When("Converting search results with default feedback id") {
                listOf(TEST_SERVER_SEARCH_RESULT, TEST_LOCAL_SEARCH_RESULT).forEach { searchResult ->
                    val isCached = searchResult is IndexableRecordSearchResult
                    val feedbackEvent = eventsFactory.createSearchFeedbackEvent(
                        searchResult.originalSearchResult,
                        searchResult.requestOptions,
                        createTestCoreSearchResponse(
                            results = listOf(TEST_SEARCH_RESULT.mapToCore())
                        ),
                        TEST_USER_LOCATION,
                        isReproducible = true,
                        event = FeedbackEvent(
                            reason = "Missing routable point",
                            text = "Fix, please!",
                            screenshot = mockBitmap,
                            sessionId = TEST_SESSION_ID,
                        ),
                        isCached = isCached
                    )

                    Then("Feedback event for ${searchResult.javaClass.simpleName} contains all values") {
                        assertEqualsJsonify(
                            expectedValue = SearchFeedbackEvent().apply {
                                event = SearchFeedbackEvent.EVENT_NAME
                                cached = searchResult is IndexableRecordSearchResult
                                created = TEST_EVENT_CREATION_DATE
                                latitude = TEST_USER_LOCATION.latitude()
                                longitude = TEST_USER_LOCATION.longitude()
                                resultIndex = TEST_SEARCH_RESULT.serverIndex
                                orientation = TEST_REQUEST_OPTIONS.requestContext.screenOrientation?.rawValue
                                userAgent = TEST_USER_AGENT
                                queryString = searchResult.requestOptions.query
                                language = searchResult.requestOptions.options.languages?.map { it.code }
                                boundingBox = searchResult.requestOptions.options.boundingBox?.coordinates()
                                proximity = searchResult.requestOptions.options.proximity?.coordinates()
                                country = searchResult.requestOptions.options.countries?.map { it.code }
                                endpoint = TEST_ENDPOINT
                                fuzzyMatch = searchResult.requestOptions.options.fuzzyMatch
                                limit = searchResult.requestOptions.options.limit
                                types = searchResult.requestOptions.options.types?.map { it.name }
                                feedbackReason = "Missing routable point"
                                feedbackText = "Fix, please!"
                                selectedItemName = searchResult.originalSearchResult.names.first()
                                keyboardLocale = TEST_LOCALE.language
                                mapZoom = calculateMapZoom(TEST_VIEWPORT)
                                mapCenterLatitude = TEST_VIEWPORT.centerLatitude()
                                mapCenterLongitude = TEST_VIEWPORT.centerLongitude()
                                resultId = when (isCached) {
                                    true -> null
                                    false -> searchResult.originalSearchResult.id
                                }
                                sessionIdentifier = when (isCached) {
                                    true -> NOT_AVAILABLE_SESSION_ID
                                    false -> TEST_SESSION_ID
                                }
                                responseUuid = TEST_RESPONSE_UUID
                                feedbackId = TEST_UUID
                                if (BuildConfig.DEBUG) {
                                    isTest = true
                                }
                                screenshot = TEST_ENCODED_BITMAP
                                resultCoordinates = searchResult.coordinate?.coordinates()
                                requestParamsJson = TEST_REQUEST_PARAMS_JSON
                                appMetadata = AppMetadata(
                                    name = null,
                                    version = null,
                                    userId = null,
                                    sessionId = TEST_SESSION_ID
                                )
                                searchResultsJson = TEST_SEARCH_RESULTS_INFO_JSON
                                schema = SEARCH_FEEDBACK_SCHEMA_VERSION
                            },
                            actualValue = feedbackEvent
                        )
                    }
                }
            }

            When("Converting search results with overridden feedback id") {
                val overriddenFeedbackId = "overridden-feedback-id"

                val feedbackEvent = eventsFactory.createSearchFeedbackEvent(
                    TEST_SERVER_SEARCH_RESULT.originalSearchResult,
                    TEST_SERVER_SEARCH_RESULT.requestOptions,
                    createTestCoreSearchResponse(
                        results = listOf(TEST_SEARCH_RESULT.mapToCore())
                    ),
                    TEST_USER_LOCATION,
                    isReproducible = true,
                    event = FeedbackEvent(
                        reason = "Missing routable point",
                        text = "Fix, please!",
                        screenshot = mockBitmap,
                        sessionId = TEST_SESSION_ID,
                        feedbackId = overriddenFeedbackId,
                    ),
                    isCached = false
                )

                Then("Feedback id should be $overriddenFeedbackId", overriddenFeedbackId, feedbackEvent.feedbackId)
            }
        }
    }

    @TestFactory
    fun `Check SearchSuggestion converts to SearchFeedbackEvent`() = TestCase {
        Given("TelemetrySearchEventsFactory with mocked dependencies") {
            every { eventJsonParser.parse(TEST_CORE_RAW_EVENT) } answers {
                SearchFeedbackEvent().apply {
                    event = SearchFeedbackEvent.EVENT_NAME
                    endpoint = TEST_ENDPOINT
                    requestParamsJson = TEST_REQUEST_PARAMS_JSON
                }
            }

            eventsFactory = createEventsFactory()

            When("Converting search suggestions with default feedback id") {
                listOf(
                    TEST_SERVER_SEARCH_SUGGESTION,
                    TEST_LOCAL_SEARCH_SUGGESTION,
                    TEST_GEOCODING_COMPAT_SEARCH_SUGGESTION
                ).forEach { searchSuggestion ->
                    val isCached = searchSuggestion is IndexableRecordSearchSuggestion
                    val feedbackEvent = eventsFactory.createSearchFeedbackEvent(
                        searchSuggestion.originalSearchResult,
                        searchSuggestion.requestOptions,
                        null,
                        TEST_USER_LOCATION,
                        isReproducible = true,
                        event = FeedbackEvent(
                            reason = "Missing routable point",
                            text = "Fix, please!",
                            screenshot = mockBitmap,
                        ),
                        isCached = isCached
                    )

                    Then("Feedback event for ${searchSuggestion.javaClass.simpleName} contains all values") {
                        assertEqualsJsonify(
                            expectedValue = SearchFeedbackEvent().apply {
                                event = SearchFeedbackEvent.EVENT_NAME
                                cached = searchSuggestion is IndexableRecordSearchSuggestion
                                created = TEST_EVENT_CREATION_DATE
                                latitude = TEST_USER_LOCATION.latitude()
                                longitude = TEST_USER_LOCATION.longitude()
                                resultIndex = searchSuggestion.serverIndex
                                orientation = TEST_REQUEST_OPTIONS.requestContext.screenOrientation?.rawValue
                                userAgent = TEST_USER_AGENT
                                queryString = searchSuggestion.requestOptions.query
                                language = searchSuggestion.requestOptions.options.languages?.map { it.code }
                                boundingBox = searchSuggestion.requestOptions.options.boundingBox?.coordinates()
                                proximity = searchSuggestion.requestOptions.options.proximity?.coordinates()
                                country = searchSuggestion.requestOptions.options.countries?.map { it.code }
                                endpoint = TEST_ENDPOINT
                                fuzzyMatch = searchSuggestion.requestOptions.options.fuzzyMatch
                                limit = searchSuggestion.requestOptions.options.limit
                                types = searchSuggestion.requestOptions.options.types?.map { it.name }
                                feedbackReason = "Missing routable point"
                                feedbackText = "Fix, please!"
                                selectedItemName = searchSuggestion.originalSearchResult.names.first()
                                mapZoom = calculateMapZoom(TEST_VIEWPORT)
                                mapCenterLatitude = TEST_VIEWPORT.centerLatitude()
                                mapCenterLongitude = TEST_VIEWPORT.centerLongitude()
                                keyboardLocale = TEST_LOCALE.language
                                resultId = when (isCached) {
                                    true -> null
                                    false -> searchSuggestion.originalSearchResult.id
                                }
                                sessionIdentifier = when (isCached) {
                                    true -> NOT_AVAILABLE_SESSION_ID
                                    false -> TEST_SESSION_ID
                                }
                                responseUuid = TEST_RESPONSE_UUID
                                feedbackId = TEST_UUID
                                if (BuildConfig.DEBUG) {
                                    isTest = true
                                }
                                screenshot = TEST_ENCODED_BITMAP
                                resultCoordinates = searchSuggestion.originalSearchResult.center?.coordinates()
                                requestParamsJson = TEST_REQUEST_PARAMS_JSON
                                appMetadata = null
                                searchResultsJson = null
                                schema = SEARCH_FEEDBACK_SCHEMA_VERSION
                            },
                            actualValue = feedbackEvent
                        )
                    }
                }
            }

            When("Converting search suggestion with overridden feedback id") {
                val overriddenFeedbackId = "overridden-feedback-id"

                val feedbackEvent = eventsFactory.createSearchFeedbackEvent(
                    TEST_SERVER_SEARCH_SUGGESTION.originalSearchResult,
                    TEST_SERVER_SEARCH_SUGGESTION.requestOptions,
                    null,
                    TEST_USER_LOCATION,
                    isReproducible = true,
                    event = FeedbackEvent(
                        reason = "Missing routable point",
                        text = "Fix, please!",
                        screenshot = mockBitmap,
                        feedbackId = overriddenFeedbackId,
                    ),
                    isCached = false
                )

                Then("Feedback id should be", overriddenFeedbackId, feedbackEvent.feedbackId)
            }
        }
    }

    @TestFactory
    fun `Check IndexableRecord converts to SearchFeedbackEvent`() = TestCase {
        Given("TelemetrySearchEventsFactory with mocked dependencies") {
            every { eventJsonParser.parse(TEST_CORE_RAW_EVENT) } answers {
                SearchFeedbackEvent().apply {
                    event = SearchFeedbackEvent.EVENT_NAME
                    endpoint = TEST_ENDPOINT
                }
            }

            eventsFactory = createEventsFactory()

            When("Converting FavoriteRecord with default feedback id") {
                val feedbackEvent = eventsFactory.createSearchFeedbackEvent(
                    TEST_FAVORITE_RECORD,
                    FeedbackEvent(
                        reason = "Missing routable point",
                        text = "Fix, please!",
                        screenshot = mockBitmap,
                    ),
                    TEST_USER_LOCATION
                )
                Then("Feedback event contains all values") {
                    assertEqualsJsonify(
                        expectedValue = SearchFeedbackEvent().apply {
                            event = SearchFeedbackEvent.EVENT_NAME
                            cached = true
                            created = TEST_EVENT_CREATION_DATE
                            latitude = TEST_USER_LOCATION.latitude()
                            longitude = TEST_USER_LOCATION.longitude()
                            resultIndex = -1
                            userAgent = TEST_USER_AGENT
                            queryString = ""
                            feedbackReason = "Missing routable point"
                            feedbackText = "Fix, please!"
                            selectedItemName = TEST_FAVORITE_RECORD.address?.formattedAddress(SearchAddress.FormatStyle.Full)
                            mapZoom = calculateMapZoom(TEST_VIEWPORT)
                            mapCenterLatitude = TEST_VIEWPORT.centerLatitude()
                            mapCenterLongitude = TEST_VIEWPORT.centerLongitude()
                            sessionIdentifier = NOT_AVAILABLE_SESSION_ID
                            feedbackId = TEST_UUID
                            if (BuildConfig.DEBUG) {
                                isTest = true
                            }
                            screenshot = TEST_ENCODED_BITMAP
                            resultCoordinates = TEST_FAVORITE_RECORD.coordinate.coordinates()
                            requestParamsJson = null
                            appMetadata = null
                            searchResultsJson = null
                            schema = SEARCH_FEEDBACK_SCHEMA_VERSION
                        },
                        actualValue = feedbackEvent
                    )
                }
            }

            When("Converting FavoriteRecord with overridden feedback id") {
                val overriddenFeedbackId = "overridden-feedback-id"

                val feedbackEvent = eventsFactory.createSearchFeedbackEvent(
                    TEST_FAVORITE_RECORD,
                    FeedbackEvent(
                        reason = "Missing routable point",
                        text = "Fix, please!",
                        screenshot = mockBitmap,
                        feedbackId = overriddenFeedbackId,
                    ),
                    TEST_USER_LOCATION
                )
                Then("Feedback id should be $overriddenFeedbackId", overriddenFeedbackId, feedbackEvent.feedbackId)
            }

            When("Converting HistoryRecord with default feedback id") {
                val feedbackEvent = eventsFactory.createSearchFeedbackEvent(
                    TEST_HISTORY_RECORD,
                    FeedbackEvent(
                        reason = "Missing routable point",
                        text = "Fix, please!",
                        screenshot = mockBitmap,
                    ),
                    TEST_USER_LOCATION
                )
                Then("Feedback event contains all values") {
                    assertEqualsJsonify(
                        expectedValue = SearchFeedbackEvent().apply {
                            event = SearchFeedbackEvent.EVENT_NAME
                            cached = true
                            created = TEST_EVENT_CREATION_DATE
                            latitude = TEST_USER_LOCATION.latitude()
                            longitude = TEST_USER_LOCATION.longitude()
                            resultIndex = -1
                            userAgent = TEST_USER_AGENT
                            queryString = ""
                            feedbackReason = "Missing routable point"
                            feedbackText = "Fix, please!"
                            selectedItemName = NO_ADDRESS_PLACEHOLDER
                            mapZoom = calculateMapZoom(TEST_VIEWPORT)
                            mapCenterLatitude = TEST_VIEWPORT.centerLatitude()
                            mapCenterLongitude = TEST_VIEWPORT.centerLongitude()
                            sessionIdentifier = NOT_AVAILABLE_SESSION_ID
                            feedbackId = TEST_UUID
                            if (BuildConfig.DEBUG) {
                                isTest = true
                            }
                            screenshot = TEST_ENCODED_BITMAP
                            resultCoordinates = TEST_HISTORY_RECORD.coordinate?.coordinates()
                            requestParamsJson = null
                            appMetadata = null
                            schema = SEARCH_FEEDBACK_SCHEMA_VERSION
                        },
                        actualValue = feedbackEvent
                    )
                }
            }

            When("Converting HistoryRecord with overridden feedback id") {
                val overriddenFeedbackId = "overridden-feedback-id"

                val feedbackEvent = eventsFactory.createSearchFeedbackEvent(
                    TEST_HISTORY_RECORD,
                    FeedbackEvent(
                        reason = "Missing routable point",
                        text = "Fix, please!",
                        screenshot = mockBitmap,
                        feedbackId = overriddenFeedbackId,
                    ),
                    TEST_USER_LOCATION
                )
                Then("Feedback id should be $overriddenFeedbackId", overriddenFeedbackId, feedbackEvent.feedbackId)
            }
        }
    }

    @TestFactory
    fun `Check ResponseInfo converts to SearchFeedbackEvent`() = TestCase {
        Given("TelemetrySearchEventsFactory with mocked dependencies") {
            every { eventJsonParser.parse(TEST_CORE_RAW_EVENT) } answers {
                SearchFeedbackEvent().apply {
                    event = SearchFeedbackEvent.EVENT_NAME
                    endpoint = TEST_ENDPOINT
                    requestParamsJson = TEST_REQUEST_PARAMS_JSON
                }
            }

            eventsFactory = createEventsFactory()

            When("Converting ResponseInfo with default feedback id") {
                val feedbackEvent = eventsFactory.createSearchFeedbackEvent(
                    MissingResultFeedbackEvent(
                        ResponseInfo(
                            requestOptions = TEST_REQUEST_OPTIONS,
                            coreSearchResponse = createTestCoreSearchResponse(
                                results = listOf(TEST_SEARCH_RESULT.mapToCore())
                            ),
                            isReproducible = true,
                        ),
                        "Please, add Paris to search results!",
                        sessionId = TEST_SESSION_ID,
                        screenshot = mockBitmap
                    ),
                    TEST_USER_LOCATION
                )

                Then("Feedback event contains all values") {
                    assertEqualsJsonify(
                        expectedValue = SearchFeedbackEvent().apply {
                            event = SearchFeedbackEvent.EVENT_NAME
                            created = TEST_EVENT_CREATION_DATE
                            latitude = TEST_USER_LOCATION.latitude()
                            longitude = TEST_USER_LOCATION.longitude()
                            resultIndex = -1
                            orientation = TEST_REQUEST_OPTIONS.requestContext.screenOrientation?.rawValue
                            userAgent = TEST_USER_AGENT
                            queryString = TEST_REQUEST_OPTIONS.query
                            language = TEST_SEARCH_OPTIONS.languages?.map { it.code }
                            boundingBox = TEST_SEARCH_OPTIONS.boundingBox?.coordinates()
                            proximity = TEST_SEARCH_OPTIONS.proximity?.coordinates()
                            country = TEST_SEARCH_OPTIONS.countries?.map { it.code }
                            endpoint = TEST_ENDPOINT
                            fuzzyMatch = true
                            limit = 6
                            types = TEST_SEARCH_OPTIONS.types?.map { it.name }
                            feedbackReason = "cannot_find"
                            feedbackText = "Please, add Paris to search results!"
                            selectedItemName = ""
                            keyboardLocale = TEST_LOCALE.language
                            mapZoom = calculateMapZoom(TEST_VIEWPORT)
                            mapCenterLatitude = TEST_VIEWPORT.centerLatitude()
                            mapCenterLongitude = TEST_VIEWPORT.centerLongitude()
                            sessionIdentifier = TEST_SESSION_ID
                            responseUuid = TEST_RESPONSE_UUID
                            feedbackId = TEST_UUID
                            if (BuildConfig.DEBUG) {
                                isTest = true
                            }
                            screenshot = TEST_ENCODED_BITMAP
                            requestParamsJson = TEST_REQUEST_PARAMS_JSON
                            appMetadata = AppMetadata(
                                name = null,
                                version = null,
                                userId = null,
                                sessionId = TEST_SESSION_ID
                            )
                            searchResultsJson = TEST_SEARCH_RESULTS_INFO_JSON
                            schema = SEARCH_FEEDBACK_SCHEMA_VERSION
                        },
                        actualValue = feedbackEvent
                    )
                }
            }

            When("Converting ResponseInfo with overridden feedback id") {
                val overriddenFeedbackId = "overridden-feedback-id"

                val feedbackEvent = eventsFactory.createSearchFeedbackEvent(
                    MissingResultFeedbackEvent(
                        ResponseInfo(
                            requestOptions = TEST_REQUEST_OPTIONS,
                            coreSearchResponse = createTestCoreSearchResponse(
                                results = listOf(TEST_SEARCH_RESULT.mapToCore())
                            ),
                            isReproducible = true,
                        ),
                        "Please, add Paris to search results!",
                        sessionId = TEST_SESSION_ID,
                        screenshot = mockBitmap,
                        feedbackId = overriddenFeedbackId,
                    ),
                    TEST_USER_LOCATION
                )

                Then("Feedback id should be $overriddenFeedbackId", overriddenFeedbackId, feedbackEvent.feedbackId)
            }
        }
    }

    @TestFactory
    fun `Check cached SearchFeedbackEvent`() = TestCase {
        Given("TelemetrySearchEventsFactory with mocked dependencies") {
            val eventsFactory = createEventsFactory()
            val cachedSearchEvent = SearchFeedbackEvent().apply {
                event = SearchFeedbackEvent.EVENT_NAME
                selectedItemName = "cached-item"
                sessionIdentifier = "cached-session-id"
                resultIndex = -1
                queryString = "cached query"
                schema = "search.feedback-2.0"
            }

            When("Updating cached search feedback event: $cachedSearchEvent") {
                eventsFactory.updateCachedSearchFeedbackEvent(
                    cachedSearchEvent,
                    FeedbackEvent(
                        reason = "Missing routable point",
                        text = "Fix, please!",
                        screenshot = mockBitmap,
                        sessionId = TEST_SESSION_ID
                    ),
                    TEST_USER_LOCATION
                )

                Then("Feedback event was properly created from raw event") {
                    assertEqualsJsonify(
                        expectedValue = SearchFeedbackEvent().apply {
                            event = SearchFeedbackEvent.EVENT_NAME
                            cached = true
                            created = TEST_EVENT_CREATION_DATE
                            latitude = TEST_USER_LOCATION.latitude()
                            longitude = TEST_USER_LOCATION.longitude()
                            resultIndex = -1
                            userAgent = TEST_USER_AGENT
                            queryString = "cached query"
                            feedbackReason = "Missing routable point"
                            feedbackText = "Fix, please!"
                            selectedItemName = "cached-item"
                            mapZoom = calculateMapZoom(TEST_VIEWPORT)
                            mapCenterLatitude = TEST_VIEWPORT.centerLatitude()
                            mapCenterLongitude = TEST_VIEWPORT.centerLongitude()
                            sessionIdentifier = "cached-session-id"
                            feedbackId = TEST_UUID
                            if (BuildConfig.DEBUG) {
                                isTest = true
                            }
                            screenshot = TEST_ENCODED_BITMAP
                            appMetadata = AppMetadata(
                                name = null,
                                version = null,
                                userId = null,
                                sessionId = TEST_SESSION_ID
                            )
                            searchResultsJson = null
                            schema = "search.feedback-2.0"
                        },
                        actualValue = cachedSearchEvent
                    )
                }
            }
        }
    }

    private companion object {

        const val TEST_USER_AGENT = "search-sdk-android-test"
        const val TEST_CORE_RAW_EVENT: String = "{\"event\":\"search.feedback\",\"endpoint\":\"test-endpoint\"}"
        const val TEST_EVENT_CREATION_DATE: String = "2020-09-11T00:29:09+0300"
        const val TEST_UUID: String = "test-generated-uuid"
        const val TEST_ENCODED_BITMAP = "VGhlIHBhdGggb2YgdGhlIHJpZ2h0ZW91cyBtYW4gaXMgYmVzZXQ"
        const val TEST_REQUEST_PARAMS_JSON: String = "{\"navigation_profile\":\"driving\",\"eta_type\":\"navigation\",\"route\":\"iikeFfygjVixNiwhAjvNkw\",\"route_geometry\":\"polyline6\",\"time_deviation\":1,\"sar_type\":\"isochrone\",\"origin\":[1.0,1.0]}"
        const val TEST_SEARCH_RESULTS_INFO_JSON: String = "{\"results\": [{\"name\": \"<Local item>\", \"address\": \"Starbucks, 1380 Pear Ave, Mountain View, California 94043, United States of America\", \"id\": \"some-starbucks-id\"}], \"multiStepSearch\": true}"
        const val TEST_SESSION_ID: String = "test-session-id"
        val TEST_USER_LOCATION: Point = Point.fromLngLat(27.55140833333333, 53.911334999999994)
        val TEST_USER_PROXIMITY: Point = Point.fromLngLat(-88.2960988764769, 36.292616502438)
        val TEST_ORIGIN_LOCATION: Point = Point.fromLngLat(-88.3, 36.3)
        val TEST_VIEWPORT: BoundingBox = BoundingBox.fromLngLats(0.0, 0.0, 90.0, 45.0)
        val TEST_LOCALE: Locale = Locale.GERMANY
        const val TEST_RESPONSE_UUID = "test-response-uuid"
        const val TEST_ENDPOINT = "test-endpoint"

        const val NOT_AVAILABLE_SESSION_ID = "<Not available>"
        const val SEARCH_FEEDBACK_SCHEMA_VERSION = "search.feedback-2.3"
        const val NO_ADDRESS_PLACEHOLDER = "<No address>"

        val TEST_SEARCH_OPTIONS = SearchOptions(
            proximity = TEST_USER_PROXIMITY,
            boundingBox = BoundingBox.fromPoints(
                Point.fromLngLat(-170.0, -80.0),
                Point.fromLngLat(160.0, 70.0)
            ),
            countries = listOf(Country.FRANCE, Country.GERMANY),
            fuzzyMatch = true,
            languages = listOf(Language.FRENCH, Language.GERMAN, Language.ENGLISH),
            limit = 6,
            types = listOf(QueryType.POI, QueryType.ADDRESS, QueryType.POSTCODE),
            origin = TEST_ORIGIN_LOCATION,
            navigationOptions = SearchNavigationOptions(
                navigationProfile = SearchNavigationProfile.DRIVING,
                etaType = EtaType.NAVIGATION
            )
        )

        val TEST_REQUEST_OPTIONS = createTestRequestOptions(
            "Paris Eiffel Tower",
            endpoint = "suggest",
            options = TEST_SEARCH_OPTIONS,
            requestContext = SearchRequestContext(
                apiType = ApiType.SBS,
                keyboardLocale = TEST_LOCALE,
                screenOrientation = ScreenOrientation.PORTRAIT,
                responseUuid = TEST_RESPONSE_UUID
            )
        )

        val TEST_SEARCH_RESULT = createTestOriginalSearchResult(
            id = "Y2aAgIL8TAA=.42eAgMSCTN2C_Ezd4tSisszkVAA=.U2aAgLT80qLiwtLEolQ9U8NEIxMT01RTA0PLZAuDJFMzS2OTZHNTAA==",
            types = listOf(OriginalResultType.POI),
            names = listOf("Tour Eiffel"),
            languages = listOf("fr"),
            addresses = listOf(SearchAddress()),
            descriptionAddress = "5 avenue Anatole France, 75007 Paris, France",
            distanceMeters = 7100000.0,
            center = Point.fromLngLat(2.294423282146454, 48.85825817805569),
            categories = listOf("site historique", "attraction touristique", "monument", "point de vue"),
            icon = "marker",
            action = SearchResultSuggestAction(
                "retrieve",
                "ApIRHSCw9FLmeKhRFdha2Sy8oVmp0-B_Ko22am7vqmh_mcvnj3rskkkBhjrnEczEtxkl7eidXKaRuvSTx8jtc0tYoyZ6QUXiJ4uW5wK7gm5waUK2B0ERxYjwhDVM6l2V_w9ydWevuMT1NEyqa3LVWGJYJwo3Gxq3NY_gq06crd1x-oWFyPM3nevna4qzgZMheenaRJqdCUAM8K0oFhbNKBoPrHNQm1PO1dmE6UneRKouDtmlgM2Hd0-PQPLd9SYbIel2CO_6-XtG2paaLZe0iN4BYug4DcS7MANbEe2AL3Zsy59t6JSVGnWO1QlZMb8Ir8IrPo7d15t1Y5GJYhmgdP1bTI6Z1B03PZZ9AFcVzO8SH3_SQvdxGRbS1aCfcRIxPfkDqtwiYj1j4OHjKZ1rVRFcoQK_W6_cMQ_Jy1qu-CF-eFatBRIRIboDkzPFAL7ja1HSbKDUpMKJN6fZhNsfYkByWzFq1v4YaZNuAC6OmGoNh5_WEO0qUPyEt6PQDVtko27ma97cBgN7AUQxdzFCkRd3ftDF9AFbRAYQvVuI8AZ2CFI3gHODW3UEZzP2hZsBCxUg1y3l763vPE17ioGimZ2LuVjn_7FeT8IbQRHtsJMOhE8reMN25AS9tWEcjOt3uX2ffDga7r79NSeDyN-zRfZuMDZ290lDCOx7XfBXj6Y8EJWVTqQN261uxq6EdREhgT0-HnVLe3Q1R2Ce3A6qca53j7mY0dNNiBK78qiNLl8lsvqYJbeLJZpAX2893SJv15Ufh0Xkv-TYhzURuJNmIie-LdvQCJBtylnOwrnIyo9NH3oZKskC21YE6NRk9KNEpxR4uEqWMrQ3gfM3DMtZFcIw2PYp_Mp0VF0tWpKBg4ej22FNVgMNrO5dn91w_mqulANRwDTrmjXGLUxFE0Fe4S5Z5gHKHMiB6AW23GIePCpprEN2Kwlj0S6bCskorc27mcC8w2sgEHVYUVVMSJM9GGVrkqxmCxcTdiVVrLz8LwdK3I6y2v5i7foONg_IjTbZj9AYnXY2LDqol97AYP0ne4BbgUPtx7PGDfRP9-rbYkSN9BpnnhBZvoQy7JznixmlcpTBhmHjTAJl85u1rGPy-0t5EjA8ToQb_tYaMx5n9CFWMbW10ZoqYHEt-GugTZa_osZ9KJHQLWw-k1jpmQBpkEY0mJszdPC7XGNM7w7Gwgk757zpEyfKT4jCunpUK6MTT6mniRDGBnXRb1AL2U7FNEJCVXL5_Q==",
                "",
                byteArrayOf(),
                true
            ),
            serverIndex = 99,
        )

        val TEST_SERVER_SEARCH_RESULT = ServerSearchResultImpl(
            types = listOf(SearchResultType.ADDRESS),
            originalSearchResult = TEST_SEARCH_RESULT,
            requestOptions = TEST_REQUEST_OPTIONS
        )

        val TEST_LOCAL_SEARCH_RESULT = IndexableRecordSearchResultImpl(
            record = StubIndexableRecord(),
            originalSearchResult = TEST_SEARCH_RESULT,
            requestOptions = TEST_REQUEST_OPTIONS
        )

        val TEST_SERVER_SEARCH_SUGGESTION = ServerSearchSuggestion(
            originalSearchResult = TEST_SEARCH_RESULT,
            requestOptions = TEST_REQUEST_OPTIONS
        )

        val TEST_FAVORITE_RECORD = FavoriteRecord(
            id = "test-favorite-record-id",
            name = "Test Local Favorite Name",
            coordinate = Point.fromLngLat(2.294423282146454, 48.85825817805569),
            descriptionText = "Test description text",
            address = SearchAddress(
                houseNumber = "22",
                street = "Baker street"
            ),
            type = SearchResultType.POI,
            makiIcon = null,
            categories = emptyList(),
            routablePoints = null,
            metadata = null
        )

        val TEST_LOCAL_SEARCH_SUGGESTION = IndexableRecordSearchSuggestion(
            record = TEST_FAVORITE_RECORD,
            originalSearchResult = TEST_SEARCH_RESULT.copy(
                types = listOf(OriginalResultType.USER_RECORD), layerId = "testLayerId"
            ),
            requestOptions = TEST_REQUEST_OPTIONS
        )

        val TEST_GEOCODING_COMPAT_SEARCH_SUGGESTION = GeocodingCompatSearchSuggestion(
            originalSearchResult = TEST_SEARCH_RESULT.copy(action = null),
            requestOptions = TEST_REQUEST_OPTIONS
        )

        val TEST_HISTORY_RECORD = HistoryRecord(
            id = "test-history-record-id",
            name = "Test Local History Name",
            coordinate = Point.fromLngLat(2.294423282146454, 48.85825817805569),
            descriptionText = "Test description text",
            address = SearchAddress(),
            type = SearchResultType.POI,
            routablePoints = null,
            metadata = null,
            makiIcon = null,
            categories = emptyList(),
            timestamp = 123_456_789L
        )

        private fun BoundingBox.coordinates(): List<Double> = listOf(west(), south(), east(), north())

        private fun BoundingBox.centerLatitude() = (north() + south()) / 2

        private fun BoundingBox.centerLongitude() = (east() + west()) / 2
    }
}
