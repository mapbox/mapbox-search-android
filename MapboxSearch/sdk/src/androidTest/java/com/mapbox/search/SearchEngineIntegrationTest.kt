package com.mapbox.search

import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.search.common.FixedPointLocationEngine
import com.mapbox.search.metadata.OpenHours
import com.mapbox.search.metadata.ParkingData
import com.mapbox.search.record.FavoritesDataProvider
import com.mapbox.search.record.HistoryDataProvider
import com.mapbox.search.record.HistoryRecord
import com.mapbox.search.result.IndexableRecordSearchResult
import com.mapbox.search.result.IndexableRecordSearchSuggestion
import com.mapbox.search.result.OriginalResultType
import com.mapbox.search.result.ResultAccuracy
import com.mapbox.search.result.RoutablePoint
import com.mapbox.search.result.SearchAddress
import com.mapbox.search.result.SearchRequestContext
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchResultSuggestAction
import com.mapbox.search.result.SearchResultType
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.result.SearchSuggestionType
import com.mapbox.search.result.ServerSearchResultImpl
import com.mapbox.search.result.ServerSearchSuggestion
import com.mapbox.search.tests_support.BlockingCompletionCallback
import com.mapbox.search.tests_support.BlockingSearchSelectionCallback
import com.mapbox.search.tests_support.BlockingSearchSelectionCallback.SearchEngineResult
import com.mapbox.search.tests_support.EmptySearchSuggestionsCallback
import com.mapbox.search.tests_support.compareSearchResultWithServerSearchResult
import com.mapbox.search.tests_support.createHistoryRecord
import com.mapbox.search.tests_support.createTestHistoryRecord
import com.mapbox.search.tests_support.createTestOriginalSearchResult
import com.mapbox.search.tests_support.equalsTo
import com.mapbox.search.tests_support.record.clearBlocking
import com.mapbox.search.tests_support.record.getSizeBlocking
import com.mapbox.search.tests_support.record.upsertAllBlocking
import com.mapbox.search.tests_support.record.upsertBlocking
import com.mapbox.search.tests_support.searchBlocking
import com.mapbox.search.utils.KeyboardLocaleProvider
import com.mapbox.search.utils.TestAnalyticsService
import com.mapbox.search.utils.TimeProvider
import com.mapbox.search.utils.assertEqualsIgnoreCase
import com.mapbox.search.utils.concurrent.SearchSdkMainThreadWorker
import com.mapbox.search.utils.enqueueMultiple
import com.mapbox.search.utils.orientation.ScreenOrientation
import com.mapbox.search.utils.orientation.ScreenOrientationProvider
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

/**
 * Contains only forward-geocoding related functionality tests.
 * See [CategorySearchIntegrationTest], [ReverseGeocodingSearchIntegrationTest] for more tests.
 */
@Suppress("LargeClass")
internal class SearchEngineIntegrationTest : BaseTest() {

    private lateinit var mockServer: MockWebServer
    private lateinit var searchEngine: SearchEngine
    private lateinit var historyDataProvider: HistoryDataProvider
    private lateinit var favoritesDataProvider: FavoritesDataProvider
    private lateinit var searchEngineSettings: SearchEngineSettings
    private val timeProvider: TimeProvider = TimeProvider { TEST_LOCAL_TIME_MILLIS }
    private val keyboardLocaleProvider: KeyboardLocaleProvider = KeyboardLocaleProvider { TEST_KEYBOARD_LOCALE }
    private val orientationProvider: ScreenOrientationProvider = ScreenOrientationProvider { TEST_ORIENTATION }
    private val analyticsService: TestAnalyticsService = TestAnalyticsService()
    private val callbacksExecutor: Executor = SearchSdkMainThreadWorker.mainExecutor

    @Before
    override fun setUp() {
        super.setUp()

        mockServer = MockWebServer()

        searchEngineSettings = SearchEngineSettings(
            singleBoxSearchBaseUrl = mockServer.url("").toString(),
            geocodingEndpointBaseUrl = mockServer.url("").toString()
        )

        MapboxSearchSdk.initializeInternal(
            application = targetApplication,
            accessToken = TEST_ACCESS_TOKEN,
            locationEngine = FixedPointLocationEngine(TEST_USER_LOCATION),
            searchEngineSettings = searchEngineSettings,
            allowReinitialization = true,
            timeProvider = timeProvider,
            keyboardLocaleProvider = keyboardLocaleProvider,
            orientationProvider = orientationProvider,
        )

        searchEngine = MapboxSearchSdk.createSearchEngine(
            apiType = ApiType.SBS,
            coreEngine = MapboxSearchSdk.getSharedCoreEngineByApiType(ApiType.SBS),
            analyticsService = analyticsService
        )

        historyDataProvider = MapboxSearchSdk.serviceProvider.historyDataProvider()
        historyDataProvider.clearBlocking(callbacksExecutor)

        favoritesDataProvider = MapboxSearchSdk.serviceProvider.favoritesDataProvider()
        favoritesDataProvider.clearBlocking(callbacksExecutor)
    }

    @Test
    fun testRequestParametersAreCorrect() {
        mockServer.enqueue(MockResponse().setResponseCode(500))

        val options = SearchOptions(
            proximity = Point.fromLngLat(10.5, 20.123),
            boundingBox = BoundingBox.fromPoints(Point.fromLngLat(10.0, 15.0), Point.fromLngLat(30.0, 50.0)),
            countries = listOf(Country.UNITED_STATES, Country.BELARUS),
            fuzzyMatch = true,
            languages = listOf(Language.ENGLISH),
            limit = 5,
            types = listOf(QueryType.COUNTRY, QueryType.LOCALITY, QueryType.ADDRESS),
            origin = Point.fromLngLat(50.123, 70.123),
            navigationOptions = SearchNavigationOptions(
                navigationProfile = SearchNavigationProfile.DRIVING,
                etaType = EtaType.NAVIGATION
            ),
            routeOptions = TEST_ROUTE_OPTIONS
        )

        val callback = BlockingSearchSelectionCallback()
        searchEngine.search(TEST_QUERY, options, callback)

        val request = mockServer.takeRequest()
        assertEqualsIgnoreCase("post", request.method!!)

        val url = request.requestUrl!!
        assertEqualsIgnoreCase("//search/v1/suggest/Minsk", url.encodedPath)
        assertEquals(TEST_ACCESS_TOKEN, url.queryParameter("access_token"))
        assertEquals(formatPoints(options.proximity), url.queryParameter("proximity"))
        assertEquals(
            formatPoints(options.boundingBox?.southwest(), options.boundingBox?.northeast()),
            url.queryParameter("bbox")
        )
        assertEquals(options.countries?.joinToString(separator = ",") { it.code }, url.queryParameter("country"))
        assertEquals(Language.ENGLISH.code, url.queryParameter("language"))
        assertEquals(options.limit.toString(), url.queryParameter("limit"))
        assertEquals(
            options.types?.joinToString(separator = ",") { it.name.lowercase(Locale.getDefault()) },
            url.queryParameter("types")
        )

        assertEquals(formatPoints(options.origin), url.queryParameter("origin"))
        assertEquals(options.navigationOptions?.navigationProfile?.rawName!!, url.queryParameter("navigation_profile"))
        assertEquals(options.navigationOptions?.etaType?.rawName!!, url.queryParameter("eta_type"))

        assertEquals(
            TEST_ROUTE_OPTIONS.timeDeviationMinutes.formatToBackendConvention(),
            url.queryParameter("time_deviation")
        )
        // Route encoded as polyline6 format, it's tricky to decode it manually and test.

        assertFalse(request.headers["X-Request-ID"].isNullOrEmpty())
    }

    @Test
    fun testRequestDebounce() {
        mockServer.enqueue(MockResponse().setResponseCode(500))

        val requestDebounceMillis = 200L
        val options = SearchOptions(requestDebounce = requestDebounceMillis.toInt())

        val callback = BlockingSearchSelectionCallback()
        searchEngine.search("skipped query", options, callback)
        searchEngine.search("actual query", options, callback)

        Thread.sleep(requestDebounceMillis)

        val req = mockServer.takeRequest(options.requestDebounce!!.toLong() * 2, TimeUnit.MILLISECONDS)!!
        assertEquals(1, mockServer.requestCount)
        assertEqualsIgnoreCase("//search/v1/suggest/actual%20query", req.requestUrl!!.encodedPath)
    }

    @Test
    fun testSuccessfulResponse() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/suggestions-successful-for-minsk.json"))

        val callback = BlockingSearchSelectionCallback()
        val options = SearchOptions(origin = TEST_ORIGIN_LOCATION, navigationOptions = TEST_NAV_OPTIONS)
        searchEngine.search(TEST_QUERY, options, callback)

        val res = callback.getResultBlocking()
        assertTrue(res is SearchEngineResult.Suggestions)
        val suggestions = (res as SearchEngineResult.Suggestions).suggestions
        assertEquals(5, suggestions.size)
        assertFalse(suggestions.any { it.type is SearchSuggestionType.IndexableRecordItem })

        val first = suggestions[0]

        val originalSearchResult = createTestOriginalSearchResult(
            id = "Y2OAgKLU9Mz8PAA=.Y2OAgOTEotzUPAA=.RYzBDQIxDASpJW8q4MmfGpDPtu4scjaynccJUQb9EpFI7GtntNrPaeRV0JqmH-VSrlzBW5RzcV7FtKubaDy6IIl0weyq07MC8qwWiUaTqiFUyWOQsqzbYr6Z0TBA5Bzxh7u2fWGfe9h_P-8v",
            types = listOf(OriginalResultType.REGION),
            names = listOf("Minsk"),
            languages = listOf("en"),
            addresses = listOf(SearchAddress(country = "Belarus")),
            descriptionAddress = "Belarus",
            matchingName = "Minsk",
            distanceMeters = 5000000.0,
            icon = "marker",
            categories = listOf("cafe"),
            action = SearchResultSuggestAction(
                endpoint = "retrieve",
                path = "",
                query = null,
                body = "{\"id\":\"test-id\"}".toByteArray(),
                multiRetrievable = false
            ),
            externalIDs = mapOf(
                "carmen" to "place.9038333669154200",
                "federated" to "carmen.place.9038333669154200",
            ),
            etaMinutes = 10.5,
            metadata = SearchResultMetadata(
                metadata = HashMap(
                    mutableMapOf(
                        "iso_3166_1" to "by",
                        "iso_3166_2" to "BY-MI"
                    )
                )
            )
        )

        val expectedResult = ServerSearchSuggestion(
            originalSearchResult,
            TEST_REQUEST_OPTIONS.copy(
                options = options.copy(proximity = TEST_USER_LOCATION),
                proximityRewritten = true,
                requestContext = TEST_REQUEST_OPTIONS.requestContext.copy(
                    responseUuid = "bf62f6f4-92db-11eb-a8b3-0242ac130003"
                )
            )
        )
        assertTrue(compareSearchResultWithServerSearchResult(expectedResult, first))

        assertEquals(SearchSuggestionType.SearchResultSuggestion(SearchResultType.PLACE, SearchResultType.REGION), suggestions[1].type)
        assertEquals(SearchSuggestionType.SearchResultSuggestion(SearchResultType.POI), suggestions[2].type)
        assertEquals(SearchSuggestionType.Category("cafe"), suggestions[3].type)
        assertEquals(SearchSuggestionType.Category("florist"), suggestions[4].type)

        assertNotNull(res.responseInfo.coreSearchResponse)
    }

    @Test
    fun testOptionsLimit() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/suggestions-successful-for-minsk.json"))

        val callback = BlockingSearchSelectionCallback()
        val options = SearchOptions(limit = 3)
        searchEngine.search(TEST_QUERY, options, callback)

        val res = callback.getResultBlocking()
        assertEquals(3, res.requireSuggestions().size)
    }

    @Test
    fun testSuccessfulEmptyResponse() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/suggestions-successful-empty.json"))

        val callback = BlockingSearchSelectionCallback()
        searchEngine.search(TEST_QUERY, SearchOptions(), callback)

        val res = callback.getResultBlocking() as SearchEngineResult.Suggestions
        assertTrue(res.suggestions.isEmpty())
        assertNotNull(res.responseInfo.coreSearchResponse)
    }

    @Test
    fun testIndexableRecordsResponseOnly() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/suggestions-successful-empty.json"))

        val records = (1..10).map {
            createTestHistoryRecord(
                id = "id$it",
                name = "$TEST_QUERY $it",
                descriptionText = "test-description-text for $it",
                address = SearchAddress(houseNumber = "test-address-house-number for $it"),
                makiIcon = "test-maki for $it",
                coordinate = Point.fromLngLat(10.0 + it, 30.0 + it),
                routablePoints = listOf(RoutablePoint(Point.fromLngLat(10.0 + it, 30.0 + it), "point $it")),
                categories = listOf("test-category for $it"),
                metadata = SearchResultMetadata(
                    metadata = hashMapOf(),
                    reviewCount = it,
                    phone = "+1 650-965-2048-$it",
                    website = "https://www.test-meta-$it.com",
                    averageRating = 4.0,
                    description = "Starbucks, Mountain View",
                    primaryPhotos = emptyList(),
                    otherPhotos = null,
                    openHours = OpenHours.AlwaysOpen,
                    parking = null,
                    cpsJson = "{\"raw\":{}}",
                )
            )
        }

        historyDataProvider.upsertAllBlocking(records, callbacksExecutor)

        val callback = BlockingSearchSelectionCallback()
        searchEngine.search(TEST_QUERY, SearchOptions(limit = records.size), callback)

        val suggestionsResult = callback.getResultBlocking() as SearchEngineResult.Suggestions

        val suggestions = suggestionsResult.suggestions

        assertEquals(records.size, suggestions.size)
        suggestions.forEachIndexed { index, suggestion ->
            assertTrue((suggestion.type as? SearchSuggestionType.IndexableRecordItem)?.isHistoryRecord == true)

            val historyRecord = records[index]
            assertEquals(historyRecord.id, suggestion.id)
            assertEquals(historyRecord.name, suggestion.name)
            assertEquals(historyRecord.descriptionText, suggestion.descriptionText)
            assertEquals(historyRecord.address, suggestion.address)
            assertEquals(historyRecord.makiIcon, suggestion.makiIcon)
        }

        assertNotNull(suggestionsResult.responseInfo.coreSearchResponse)

        callback.reset()
        searchEngine.select(suggestions.first(), callback)

        val selectionResult = callback.getResultBlocking()
        assertTrue(selectionResult is SearchEngineResult.Result)
        selectionResult as SearchEngineResult.Result

        val searchResult = selectionResult.result
        assertTrue(searchResult is IndexableRecordSearchResult)
        searchResult as IndexableRecordSearchResult

        val historyRecord = records.first()
        assertEquals(historyRecord, searchResult.record)

        assertEquals(historyRecord.id, searchResult.id)
        assertEquals(historyRecord.name, searchResult.name)
        assertEquals(historyRecord.descriptionText, searchResult.descriptionText)
        assertEquals(historyRecord.routablePoints, searchResult.routablePoints)
        assertEquals(historyRecord.categories, searchResult.categories)
        assertEquals(historyRecord.coordinate, searchResult.coordinate)
        assertEquals(historyRecord.address, searchResult.address)
        assertEquals(historyRecord.makiIcon, searchResult.makiIcon)
        assertEquals(historyRecord.metadata, searchResult.metadata)
        assertEquals(historyRecord.type, searchResult.types.first())
    }

    @Test
    fun testMixedIndexableRecordsResponse() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/suggestions-successful-for-minsk.json"))

        val records = (1..10).map {
            createTestHistoryRecord(id = "id$it", name = "$TEST_QUERY $it")
        }
        historyDataProvider.upsertAllBlocking(records, callbacksExecutor)

        val callback = BlockingSearchSelectionCallback()
        searchEngine.search(TEST_QUERY, SearchOptions(), callback)

        val suggestions = callback.getResultBlocking().requireSuggestions()

        // records.size + 5 = records.size + number of suggestions from server
        assertEquals(records.size + 5, suggestions.size)

        records.indices.forEach { i ->
            assertTrue(suggestions[i].type is SearchSuggestionType.IndexableRecordItem)
        }
        (records.size until suggestions.size).forEach { i ->
            assertFalse(suggestions[i].type is SearchSuggestionType.IndexableRecordItem)
        }
    }

    @Test
    fun testMixedIndexableRecordsResponseWithLimit() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/suggestions-successful-for-minsk.json"))

        val records = (1..3).map {
            createTestHistoryRecord(id = "id$it", name = "$TEST_QUERY $it")
        }
        historyDataProvider.upsertAllBlocking(records, callbacksExecutor)

        val callback = BlockingSearchSelectionCallback()
        val searchOptions = SearchOptions(limit = records.size + 1)
        searchEngine.search(TEST_QUERY, searchOptions, callback)

        val suggestions = callback.getResultBlocking().requireSuggestions()

        assertEquals(searchOptions.limit, suggestions.size)

        records.indices.forEach { i ->
            assertTrue(suggestions[i].type is SearchSuggestionType.IndexableRecordItem)
        }
        assertFalse(suggestions.last().type is SearchSuggestionType.IndexableRecordItem)
    }

    @Test
    fun testIgnoredIndexableRecordsResponse() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/suggestions-successful-for-minsk.json"))

        val records = (1..3).map {
            createTestHistoryRecord(id = "id$it", name = "$TEST_QUERY $it")
        }

        historyDataProvider.upsertAllBlocking(records, callbacksExecutor)

        val callback = BlockingSearchSelectionCallback()
        searchEngine.search(TEST_QUERY, SearchOptions(ignoreIndexableRecords = true), callback)

        val suggestionsResult = callback.getResultBlocking() as SearchEngineResult.Suggestions

        val suggestions = suggestionsResult.suggestions
        assertTrue(suggestions.isNotEmpty())
        assertFalse(suggestions.any { it.type is SearchSuggestionType.IndexableRecordItem })
        assertNotNull(suggestionsResult.responseInfo.coreSearchResponse)
    }

    @Test
    fun testIndexableRecordsZeroThreshold() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/suggestions-successful-for-minsk.json"))

        val recordCoordinate = Point.fromLngLat(2.295135021209717, 48.859291076660156)
        val record = createTestHistoryRecord(id = "id1", name = TEST_QUERY, coordinate = recordCoordinate)
        historyDataProvider.upsertBlocking(record, callbacksExecutor)

        val callback = BlockingSearchSelectionCallback()
        searchEngine.search(TEST_QUERY, SearchOptions(
            proximity = recordCoordinate,
            origin = recordCoordinate,
            indexableRecordsDistanceThresholdMeters = 0.0
        ), callback)

        val suggestions = (callback.getResultBlocking() as SearchEngineResult.Suggestions).suggestions
        assertEquals(record, (suggestions.first() as IndexableRecordSearchSuggestion).record)
    }

    @Test
    fun testIndexableRecordsInsideThreshold() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/suggestions-successful-for-minsk.json"))

        val recordCoordinate = Point.fromLngLat(2.2945173400760424, 48.85832005563483)
        val record = createTestHistoryRecord(id = "id1", name = TEST_QUERY, coordinate = recordCoordinate)
        historyDataProvider.upsertBlocking(record, callbacksExecutor)

        // recordCoordinate + approximately 50 meters
        val userLocation = Point.fromLngLat(2.29497347098094, 48.8580726347223)
        val callback = BlockingSearchSelectionCallback()
        searchEngine.search(TEST_QUERY, SearchOptions(
            origin = userLocation,
            proximity = userLocation,
            indexableRecordsDistanceThresholdMeters = 500.0
        ), callback)

        val suggestionsResult = callback.getResultBlocking() as SearchEngineResult.Suggestions

        val suggestions = suggestionsResult.suggestions
        assertEquals(record, (suggestions.first() as IndexableRecordSearchSuggestion).record)
    }

    @Test
    fun testIndexableRecordsOutsideThreshold() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/suggestions-successful-for-minsk.json"))

        val recordCoordinate = Point.fromLngLat(2.2945173400760424, 48.85832005563483)
        val record = createTestHistoryRecord(id = "id1", name = TEST_QUERY, coordinate = recordCoordinate)
        historyDataProvider.upsertBlocking(record, callbacksExecutor)

        // recordCoordinate + approximately 50 meters
        val userLocation = Point.fromLngLat(2.29497347098094, 48.8580726347223)
        val callback = BlockingSearchSelectionCallback()
        searchEngine.search(TEST_QUERY, SearchOptions(
            proximity = userLocation,
            origin = userLocation,
            indexableRecordsDistanceThresholdMeters = 15.0
        ), callback)

        val suggestionsResult = callback.getResultBlocking() as SearchEngineResult.Suggestions

        val suggestions = suggestionsResult.suggestions
        assertFalse(suggestions.any { it is IndexableRecordSearchSuggestion })
    }

    @Test
    fun testSuccessfulSuggestionSelection() {
        val blockingCompletionCallback = BlockingCompletionCallback<List<HistoryRecord>>()
        historyDataProvider.getAll(blockingCompletionCallback)

        var callbackResult = blockingCompletionCallback.getResultBlocking()
        assertTrue(callbackResult is BlockingCompletionCallback.CompletionCallbackResult.Result)

        callbackResult as BlockingCompletionCallback.CompletionCallbackResult.Result
        assertTrue(callbackResult.result.isEmpty())

        mockServer.enqueue(createSuccessfulResponse("sbs_responses/suggestions-successful-for-minsk.json"))

        var callback = BlockingSearchSelectionCallback()
        val options = SearchOptions(origin = TEST_ORIGIN_LOCATION, navigationOptions = TEST_NAV_OPTIONS)
        searchEngine.search(TEST_QUERY, options, callback)

        val res = callback.getResultBlocking()
        val suggestions = (res as SearchEngineResult.Suggestions).suggestions

        mockServer.enqueue(createSuccessfulResponse("sbs_responses/retrieve-response-successful.json"))

        callback = BlockingSearchSelectionCallback()
        searchEngine.select(suggestions.first(), callback)
        val selectionResult = callback.getResultBlocking() as SearchEngineResult.Result

        val searchResult = selectionResult.result

        val originalSearchResult = createTestOriginalSearchResult(
            id = "place.11543680732831130",
            types = listOf(OriginalResultType.PLACE, OriginalResultType.REGION),
            names = listOf("Minsk"),
            descriptionAddress = "Minsk Region, Belarus, Planet Earth",
            languages = listOf("en"),
            addresses = listOf(SearchAddress(country = "Belarus", region = "Minsk Region")),
            distanceMeters = 5000000.0,
            matchingName = "Minsk",
            center = Point.fromLngLat(27.234342, 53.940465),
            accuracy = ResultAccuracy.Rooftop,
            routablePoints = listOf(
                RoutablePoint(point = Point.fromLngLat(27.234300, 53.973651), name = "City Entrance")
            ),
            icon = "marker",
            metadata = SearchResultMetadata(
                metadata = HashMap(
                    mutableMapOf(
                        "iso_3166_1" to "by",
                        "iso_3166_2" to "BY-MI"
                    )
                ),
                reviewCount = 17,
                phone = "+1 650-965-2048",
                website = "https://www.starbucks.com/store-locator/store/7373/shoreline-pear-1380-pear-avenue-mountain-view-ca-940431360-us",
                averageRating = 4.0,
                description = "Starbucks, Mountain View",
                primaryPhotos = listOf(
                    ImageInfo(
                        url = "http://media-cdn.tripadvisor.com/media/photo-t/18/47/98/c6/starbucks-inside-and.jpg",
                        width = 50,
                        height = 50
                    ),
                    ImageInfo(
                        url = "http://media-cdn.tripadvisor.com/media/photo-l/18/47/98/c6/starbucks-inside-and.jpg",
                        width = 150,
                        height = 150
                    ),
                    ImageInfo(
                        url = "http://media-cdn.tripadvisor.com/media/photo-o/18/47/98/c6/starbucks-inside-and.jpg",
                        width = 1708,
                        height = 2046
                    )
                ),
                otherPhotos = null,
                openHours = OpenHours.AlwaysOpen,
                parking = ParkingData(
                    totalCapacity = 20,
                    reservedForDisabilities = 2
                ),
                cpsJson = "{\"raw\":{}}"
            ),
            serverIndex = 0,
            etaMinutes = 10.5,
            externalIDs = mapOf("carmen" to "place.11543680732831130")
        )

        val expectedResult = ServerSearchResultImpl(
            listOf(SearchResultType.PLACE, SearchResultType.REGION),
            originalSearchResult,
            TEST_REQUEST_OPTIONS.run {
                copy(
                    options = options.copy(proximity = TEST_USER_LOCATION),
                    proximityRewritten = true,
                    requestContext = requestContext.copy(
                        responseUuid = "0a197057-edf0-4447-be63-9badcf7c19be"
                    )
                )
            }
        )
        assertTrue(compareSearchResultWithServerSearchResult(expectedResult, searchResult))

        assertEquals(1, historyDataProvider.getSizeBlocking(callbacksExecutor))

        blockingCompletionCallback.reset()
        historyDataProvider.getAll(blockingCompletionCallback)

        callbackResult = blockingCompletionCallback.getResultBlocking()
        assertTrue(callbackResult is BlockingCompletionCallback.CompletionCallbackResult.Result)

        callbackResult as BlockingCompletionCallback.CompletionCallbackResult.Result
        assertTrue(callbackResult.result.isNotEmpty())

        assertEquals(
            createHistoryRecord(searchResult, TEST_LOCAL_TIME_MILLIS),
            callbackResult.result.first()
        )

        assertNull(selectionResult.responseInfo.coreSearchResponse)
    }

    @Test
    fun testSuccessfulSuggestionSelectionWithTurnedOffAddToHistoryLogic() {
        val blockingCompletionCallback = BlockingCompletionCallback<List<HistoryRecord>>()
        historyDataProvider.getAll(blockingCompletionCallback)

        var callbackResult = blockingCompletionCallback.getResultBlocking()
        assertTrue(callbackResult is BlockingCompletionCallback.CompletionCallbackResult.Result)

        callbackResult as BlockingCompletionCallback.CompletionCallbackResult.Result
        assertTrue(callbackResult.result.isEmpty())

        mockServer.enqueue(createSuccessfulResponse("sbs_responses/suggestions-successful-for-minsk.json"))

        var callback = BlockingSearchSelectionCallback()
        val options = SearchOptions(origin = TEST_ORIGIN_LOCATION, navigationOptions = TEST_NAV_OPTIONS)
        searchEngine.search(TEST_QUERY, options, callback)

        val res = callback.getResultBlocking()
        val suggestions = (res as SearchEngineResult.Suggestions).suggestions

        mockServer.enqueue(createSuccessfulResponse("sbs_responses/retrieve-response-successful.json"))

        callback = BlockingSearchSelectionCallback()
        searchEngine.select(suggestions.first(), SelectOptions(addResultToHistory = false), callback)
        val selectionResult = callback.getResultBlocking() as SearchEngineResult.Result

        assertEquals("place.11543680732831130", selectionResult.result.id)
        assertEquals(0, historyDataProvider.getSizeBlocking(callbacksExecutor))
        assertNull(selectionResult.responseInfo.coreSearchResponse)
    }

    @Test
    fun testRecursiveQuerySelection() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/suggestions-successful-with-recursion-query.json"))

        val callback = BlockingSearchSelectionCallback()
        val options = SearchOptions(origin = TEST_ORIGIN_LOCATION, navigationOptions = TEST_NAV_OPTIONS)
        searchEngine.search(TEST_QUERY, options, callback)

        val res = callback.getResultBlocking()
        val suggestions = (res as SearchEngineResult.Suggestions).suggestions

        assertEquals(2, suggestions.size)

        val recursionSearchResult = createTestOriginalSearchResult(
            id = "Y2WAgMLS1KJKAA==.42CAgMy8ktSivMQcAA==.42SAgKDU5NKi4sz8PAA=",
            types = listOf(OriginalResultType.QUERY),
            names = listOf("Did you mean recursion?"),
            languages = listOf("en"),
            addresses = listOf(SearchAddress()),
            descriptionAddress = "Make a new search",
            matchingName = "Did you mean recursion?",
            icon = "marker",
            action = SearchResultSuggestAction(
                endpoint = "suggest",
                path = "Recursion",
                query = "&proximity=-122.084088,37.422065&language=en&limit=10",
                body = null,
                multiRetrievable = false
            ),
        )

        val expectedResult = ServerSearchSuggestion(
            recursionSearchResult,
            TEST_REQUEST_OPTIONS.copy(
                options = options.copy(proximity = TEST_USER_LOCATION),
                proximityRewritten = true
            )
        )

        assertTrue(compareSearchResultWithServerSearchResult(expectedResult, suggestions.first()))

        mockServer.enqueue(createSuccessfulResponse("sbs_responses/suggestions-successful-with-recursion-query.json"))

        callback.reset()
        searchEngine.select(suggestions.first(), callback)

        val selectionResult = callback.getResultBlocking() as SearchEngineResult.Suggestions

        val newSuggestions = selectionResult.suggestions
        assertEquals(suggestions.size, newSuggestions.size)
        newSuggestions.indices.forEach { index ->
            assertTrue(compareSearchResultWithServerSearchResult(suggestions[index], newSuggestions[index]))
        }

        assertNotNull(selectionResult.responseInfo.coreSearchResponse)
    }

    @Test
    fun testCategorySuggestionSelection() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/suggestions-successful-only-categories.json"))

        val callback = BlockingSearchSelectionCallback()
        val options = SearchOptions(origin = TEST_ORIGIN_LOCATION, types = listOf(QueryType.CATEGORY))
        searchEngine.search(TEST_QUERY, options, callback)

        val res = callback.getResultBlocking()
        assertTrue(res is SearchEngineResult.Suggestions)
        val suggestions = (res as SearchEngineResult.Suggestions).suggestions

        val originalCategorySuggestion = createTestOriginalSearchResult(
            id = "42CAgOTEktT0_KJKAA==.42eAgMSCTN2C_Ezd4tSisszkVAA=.Y2GAgOTEtFQA",
            types = listOf(OriginalResultType.CATEGORY),
            names = listOf("Cafe"),
            languages = listOf("en"),
            addresses = listOf(SearchAddress()),
            categories = listOf("Cafe"),
            descriptionAddress = "Category",
            matchingName = "Cafe",
            icon = "restaurant",
            externalIDs = mapOf("federated" to "category.cafe"),
            action = SearchResultSuggestAction(
                endpoint = "retrieve",
                path = "",
                query = null,
                body = "{\"id\":\"category-test-id\"}".toByteArray(),
                multiRetrievable = false
            ),
        )

        val expectedSearchSuggestion = ServerSearchSuggestion(
            originalCategorySuggestion,
            TEST_REQUEST_OPTIONS.run {
                copy(
                    options = options.copy(
                        proximity = TEST_USER_LOCATION,
                        types = listOf(QueryType.CATEGORY)
                    ),
                    proximityRewritten = true,
                    requestContext = requestContext.copy(
                        responseUuid = "be35d556-9e14-4303-be15-57497c331348"
                    ),
                )
            }
        )
        assertTrue(compareSearchResultWithServerSearchResult(expectedSearchSuggestion, suggestions.first()))

        assertEquals(SearchSuggestionType.Category("cafe"), suggestions[0].type)
        assertEquals(SearchSuggestionType.Category("internet_cafe"), suggestions[1].type)

        assertNotNull(res.responseInfo.coreSearchResponse)
        assertTrue(res.responseInfo.isReproducible)

        mockServer.enqueue(createSuccessfulResponse("sbs_responses/retrieve-successful-category-cafe.json"))

        callback.reset()
        searchEngine.select(suggestions.first(), callback)

        val selectionResult = callback.getResultBlocking() as SearchEngineResult.CategoryResult
        val categoryResults = selectionResult.results

        val originalCategorySearchResult = createTestOriginalSearchResult(
            id = "ItFzVnwBAsSWFvXxpsTw",
            types = listOf(OriginalResultType.POI),
            names = listOf("SimplexiTea"),
            center = Point.fromLngLat(-122.41721, 37.775934),
            accuracy = ResultAccuracy.Point,
            languages = listOf("def"),
            addresses = listOf(SearchAddress(
                country = "United States of America",
                houseNumber = "12",
                neighborhood = "South of Market",
                place = "San Francisco",
                postcode = "94103",
                region = "California",
                street = "10th st",
            )),
            categories = listOf(
                "food",
                "food and drink",
                "coffee shop",
                "coffee",
                "cafe",
                "bubble tea",
            ),
            descriptionAddress = "12 10th St, San Francisco, California 94103, United States of America",
            matchingName = "SimplexiTea",
            icon = "restaurant",
            externalIDs = mapOf(
                "mbx_poi" to "category-result-id-1",
                "federated" to "category-result-id-1-federated",
            ),
            distanceMeters = 1.2674344310855685E7,
        )

        val expectedSearchResult = ServerSearchResultImpl(
            listOf(SearchResultType.POI),
            originalCategorySearchResult,
            TEST_REQUEST_OPTIONS.run {
                copy(
                    options = options.copy(
                        proximity = TEST_USER_LOCATION,
                        types = listOf(QueryType.CATEGORY)
                    ),
                    proximityRewritten = true,
                    requestContext = requestContext.copy(
                        responseUuid = "730bb97b-b30f-4e54-9772-8af2d4e0edf0"
                    ),
                )
            }
        )

        assertTrue(compareSearchResultWithServerSearchResult(expectedSearchResult, categoryResults.first()))

        assertEquals(categoryResults.size, 3)

        assertNotNull(selectionResult.responseInfo.coreSearchResponse)
        assertFalse(selectionResult.responseInfo.isReproducible)
    }

    @Test
    fun testSuccessfulIncorrectResponse() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/suggestions-successful-incorrect-for-minsk.json"))

        try {
            val callback = BlockingSearchSelectionCallback()
            searchEngine.search(TEST_QUERY, SearchOptions(), callback)
            if (BuildConfig.DEBUG) {
                fail()
            }
        } catch (t: Throwable) {
            if (!BuildConfig.DEBUG) {
                fail()
            }
        }
    }

    @Test
    fun testErrorResponse() {
        mockServer.enqueue(MockResponse().setResponseCode(404))

        val callback = BlockingSearchSelectionCallback()
        searchEngine.search(TEST_QUERY, SearchOptions(), callback)

        val res = callback.getResultBlocking()
        assertTrue(res is SearchEngineResult.Error && res.e is SearchRequestException && res.e.code == 404)
        res as SearchEngineResult.Error

        if (!BuildConfig.DEBUG) {
            assertEquals(analyticsService.capturedErrors, listOf(res.e))
        }
    }

    @Test
    fun testNetworkError() {
        mockServer.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))

        val callback = BlockingSearchSelectionCallback()
        searchEngine.search(TEST_QUERY, SearchOptions(), callback)

        val res = callback.getResultBlocking()
        assertTrue(res is SearchEngineResult.Error && res.e is IOException)
        res as SearchEngineResult.Error

        if (!BuildConfig.DEBUG) {
            assertEquals(analyticsService.capturedErrors, listOf(res.e))
        }
    }

    @Test
    fun testNetworkErrorForConsecutiveRequests() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/suggestions-successful-for-minsk.json"))
        mockServer.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))

        val successfulResponse = searchEngine.searchBlocking(TEST_QUERY, SearchOptions())
        assertTrue(successfulResponse.requireSuggestions().isNotEmpty())

        val errorResponse = searchEngine.searchBlocking(TEST_QUERY, SearchOptions())

        // TODO(https://github.com/mapbox/mapbox-search-sdk/issues/870)
        // Native Search SDK should return Http Error here. See testNetworkError() test.
        assertTrue(errorResponse.requireError().equalsTo(Exception("Unable to perform search request: Invalid json response")))
    }

    @Test
    fun testBrokenResponseContent() {
        mockServer.enqueue(MockResponse().setResponseCode(200).setBody("I'm broken"))

        val callback = BlockingSearchSelectionCallback()
        searchEngine.search(TEST_QUERY, SearchOptions(), callback)

        val res = callback.getResultBlocking()
        assertTrue(res is SearchEngineResult.Error)
        res as SearchEngineResult.Error

        if (!BuildConfig.DEBUG) {
            assertEquals(analyticsService.capturedErrors, listOf(res.e))
        }
    }

    @Test
    fun testCheckAsyncOperationTaskCompletion() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/suggestions-successful-for-minsk.json"))

        var countDownLatch = CountDownLatch(1)
        var task: SearchRequestTask? = null

        var searchSuggestions: List<SearchSuggestion> = emptyList()
        task = searchEngine.search(TEST_QUERY, SearchOptions(), object : SearchSuggestionsCallback {
            override fun onSuggestions(suggestions: List<SearchSuggestion>, responseInfo: ResponseInfo) {
                searchSuggestions = suggestions
                assertTrue((task as? SearchRequestTaskImpl<*>)?.isDone == true)
                countDownLatch.countDown()
            }

            override fun onError(e: Exception) {
                fail("Error happened: $e")
            }
        })
        countDownLatch.await()

        mockServer.enqueue(createSuccessfulResponse("sbs_responses/retrieve-response-successful.json"))

        countDownLatch = CountDownLatch(1)
        var selectionTask: SearchRequestTask? = null
        selectionTask = searchEngine.select(searchSuggestions.first(), object : SearchSelectionCallback {
            override fun onSuggestions(suggestions: List<SearchSuggestion>, responseInfo: ResponseInfo) {
                assertTrue((selectionTask as? SearchRequestTaskImpl<*>)?.isDone == true)
                countDownLatch.countDown()
            }

            override fun onError(e: Exception) {
                fail("Error happened: $e")
            }

            override fun onResult(suggestion: SearchSuggestion, result: SearchResult, responseInfo: ResponseInfo) {
                assertTrue((selectionTask as? SearchRequestTaskImpl<*>)?.isDone == true)
                countDownLatch.countDown()
            }

            override fun onCategoryResult(
                suggestion: SearchSuggestion,
                results: List<SearchResult>,
                responseInfo: ResponseInfo
            ) {
                assertTrue((selectionTask as? SearchRequestTaskImpl<*>)?.isDone == true)
                countDownLatch.countDown()
            }
        })

        countDownLatch.await()
    }

    @Test
    fun testConsecutiveRequests() {
        mockServer.enqueueMultiple(createSuccessfulResponse("sbs_responses/suggestions-successful-for-minsk.json"), 2)

        val task1 = searchEngine.search(TEST_QUERY, SearchOptions(requestDebounce = 1000), EmptySearchSuggestionsCallback)

        val callback = BlockingSearchSelectionCallback()
        val task2 = searchEngine.search(TEST_QUERY, SearchOptions(), callback)
        callback.getResultBlocking()

        assertTrue(task1.isCancelled)
        assertTrue(task2.isDone)
    }

    @Test
    fun testErrorBackendResponseSimpleFormat() {
        val errorResponse = MockResponse()
            .setResponseCode(422)
            .setBody(readFileFromAssets("sbs_responses/suggestions-error-response-simple-format.json"))

        mockServer.enqueue(errorResponse)

        val callback = BlockingSearchSelectionCallback()
        searchEngine.search(TEST_QUERY, SearchOptions(), callback)

        val res = callback.getResultBlocking()
        assertTrue(res is SearchEngineResult.Error)

        assertEquals(
            SearchRequestException("Wrong arguments", 422),
            (res as SearchEngineResult.Error).e
        )
    }

    @Test
    fun testErrorBackendResponseExtendedFormat() {
        val errorResponse = MockResponse()
            .setResponseCode(400)
            .setBody(readFileFromAssets("sbs_responses/suggestions-error-response-extended-format.json"))

        mockServer.enqueue(errorResponse)

        val callback = BlockingSearchSelectionCallback()
        searchEngine.search(TEST_QUERY, SearchOptions(), callback)

        val res = callback.getResultBlocking()
        assertTrue(res is SearchEngineResult.Error)

        assertEquals(
            SearchRequestException(
                readFileFromAssets("sbs_responses/suggestions-error-response-extended-format.json"),
                400
            ),
            (res as SearchEngineResult.Error).e
        )
    }

    @Test
    fun testMetadataForGeocodingAPI() {
        searchEngine = MapboxSearchSdk.createSearchEngine(ApiType.GEOCODING, searchEngineSettings, useSharedCoreEngine = true)

        mockServer.enqueue(createSuccessfulResponse("geocoding_responses/suggestions.json"))

        val callback = BlockingSearchSelectionCallback()
        searchEngine.search(TEST_QUERY, SearchOptions(), callback)

        val suggestions = callback.getResultBlocking().requireSuggestions()
        assertTrue(suggestions.isNotEmpty())
        assertEquals("fr", suggestions.first().metadata?.countryIso1)
    }

    @After
    override fun tearDown() {
        analyticsService.reset()
        mockServer.shutdown()
        super.tearDown()
    }

    private companion object {

        const val TEST_QUERY = "Minsk"
        const val TEST_ACCESS_TOKEN = "pk.test"
        val TEST_USER_LOCATION: Point = Point.fromLngLat(10.1, 11.1234567)
        val TEST_ORIGIN_LOCATION: Point = Point.fromLngLat(10.1, 11.12345)
        val TEST_NAV_OPTIONS: SearchNavigationOptions = SearchNavigationOptions(
            navigationProfile = SearchNavigationProfile.DRIVING,
            etaType = EtaType.NAVIGATION
        )

        const val TEST_LOCAL_TIME_MILLIS = 12345L
        const val TEST_UUID = "test-generated-uuid"
        val TEST_KEYBOARD_LOCALE: Locale = Locale.ENGLISH
        val TEST_ORIENTATION = ScreenOrientation.PORTRAIT

        val TEST_REQUEST_OPTIONS = RequestOptions(
            query = TEST_QUERY,
            endpoint = "suggest",
            options = SearchOptions.Builder().build(),
            proximityRewritten = false,
            originRewritten = false,
            sessionID = TEST_UUID,
            requestContext = SearchRequestContext(
                apiType = ApiType.SBS,
                keyboardLocale = TEST_KEYBOARD_LOCALE,
                screenOrientation = TEST_ORIENTATION,
                responseUuid = ""
            )
        )

        val TEST_ROUTE_OPTIONS = RouteOptions(
            route = listOf(Point.fromLngLat(1.0, 2.0), Point.fromLngLat(3.0, 4.0), Point.fromLngLat(5.0, 6.0)),
            deviation = RouteOptions.Deviation.Time(value = 30, unit = TimeUnit.SECONDS)
        )
    }
}
