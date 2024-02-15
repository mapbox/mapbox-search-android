package com.mapbox.search

import com.mapbox.common.MapboxOptions
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.search.base.core.CoreApiType
import com.mapbox.search.base.core.CoreRoutablePoint
import com.mapbox.search.base.core.createCoreResultMetadata
import com.mapbox.search.base.result.BaseRawResultType
import com.mapbox.search.base.result.BaseServerSearchSuggestion
import com.mapbox.search.base.result.BaseSuggestAction
import com.mapbox.search.base.result.SearchRequestContext
import com.mapbox.search.base.utils.KeyboardLocaleProvider
import com.mapbox.search.base.utils.TimeProvider
import com.mapbox.search.base.utils.extension.mapToCore
import com.mapbox.search.base.utils.orientation.ScreenOrientation
import com.mapbox.search.base.utils.orientation.ScreenOrientationProvider
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.common.IsoCountryCode
import com.mapbox.search.common.IsoLanguageCode
import com.mapbox.search.common.NavigationProfile
import com.mapbox.search.common.RoutablePoint
import com.mapbox.search.common.SearchRequestException
import com.mapbox.search.common.concurrent.SearchSdkMainThreadWorker
import com.mapbox.search.common.metadata.ImageInfo
import com.mapbox.search.common.metadata.OpenHours
import com.mapbox.search.common.metadata.ParkingData
import com.mapbox.search.common.tests.FixedPointLocationEngine
import com.mapbox.search.common.tests.equalsTo
import com.mapbox.search.record.FavoritesDataProvider
import com.mapbox.search.record.HistoryDataProvider
import com.mapbox.search.record.HistoryRecord
import com.mapbox.search.record.IndexableRecord
import com.mapbox.search.result.ResultAccuracy
import com.mapbox.search.result.SearchAddress
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchResultType
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.result.SearchSuggestionType
import com.mapbox.search.result.isIndexableRecordSuggestion
import com.mapbox.search.result.mapToPlatform
import com.mapbox.search.result.record
import com.mapbox.search.tests_support.BlockingCompletionCallback
import com.mapbox.search.tests_support.BlockingSearchSelectionCallback
import com.mapbox.search.tests_support.BlockingSearchSelectionCallback.SearchEngineResult
import com.mapbox.search.tests_support.EmptySearchSuggestionsCallback
import com.mapbox.search.tests_support.compareSearchResultWithServerSearchResult
import com.mapbox.search.tests_support.createHistoryRecord
import com.mapbox.search.tests_support.createSearchEngineWithBuiltInDataProvidersBlocking
import com.mapbox.search.tests_support.createTestBaseRawSearchResult
import com.mapbox.search.tests_support.createTestHistoryRecord
import com.mapbox.search.tests_support.createTestServerSearchResult
import com.mapbox.search.tests_support.record.clearBlocking
import com.mapbox.search.tests_support.record.getSizeBlocking
import com.mapbox.search.tests_support.record.upsertAllBlocking
import com.mapbox.search.tests_support.record.upsertBlocking
import com.mapbox.search.tests_support.searchBlocking
import com.mapbox.search.tests_support.selectBlocking
import com.mapbox.search.utils.assertEqualsIgnoreCase
import com.mapbox.search.utils.enqueueMultiple
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
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
    private val callbacksExecutor: Executor = SearchSdkMainThreadWorker.mainExecutor

    @Before
    override fun setUp() {
        super.setUp()
        MapboxOptions.accessToken = TEST_ACCESS_TOKEN

        mockServer = MockWebServer()

        MapboxSearchSdk.initialize(
            application = targetApplication,
            timeProvider = timeProvider,
            keyboardLocaleProvider = keyboardLocaleProvider,
            orientationProvider = orientationProvider,
        )

        searchEngineSettings = SearchEngineSettings(
            locationProvider = FixedPointLocationEngine(TEST_USER_LOCATION),
            geocodingEndpointBaseUrl = mockServer.url("").toString(),
            singleBoxSearchBaseUrl = mockServer.url("").toString()
        )

        searchEngine = createSearchEngineWithBuiltInDataProvidersBlocking(
            apiType = ApiType.SBS,
            settings = searchEngineSettings,
        )

        historyDataProvider = ServiceProvider.INSTANCE.historyDataProvider()
        historyDataProvider.clearBlocking(callbacksExecutor)

        favoritesDataProvider = ServiceProvider.INSTANCE.favoritesDataProvider()
        favoritesDataProvider.clearBlocking(callbacksExecutor)
    }

    @Test
    fun testRequestParametersAreCorrect() {
        mockServer.enqueue(MockResponse().setResponseCode(500))

        val options = SearchOptions(
            proximity = Point.fromLngLat(10.5, 20.123),
            boundingBox = BoundingBox.fromPoints(Point.fromLngLat(10.0, 15.0), Point.fromLngLat(30.0, 50.0)),
            countries = listOf(IsoCountryCode.UNITED_STATES, IsoCountryCode.BELARUS),
            fuzzyMatch = true,
            languages = listOf(IsoLanguageCode.ENGLISH),
            limit = 5,
            types = listOf(QueryType.COUNTRY, QueryType.LOCALITY, QueryType.ADDRESS),
            origin = Point.fromLngLat(50.123, 70.123),
            navigationOptions = SearchNavigationOptions(
                navigationProfile = NavigationProfile.DRIVING,
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
        assertEquals(IsoLanguageCode.ENGLISH.code, url.queryParameter("language"))
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
        assertEquals(6, suggestions.size)
        assertFalse(suggestions.any { it.type is SearchSuggestionType.IndexableRecordItem })

        val first = suggestions[0]

        val baseRawSearchResult = createTestBaseRawSearchResult(
            id = "Y2OAgKLU9Mz8PAA=.Y2OAgOTEotzUPAA=.RYzBDQIxDASpJW8q4MmfGpDPtu4scjaynccJUQb9EpFI7GtntNrPaeRV0JqmH-VSrlzBW5RzcV7FtKubaDy6IIl0weyq07MC8qwWiUaTqiFUyWOQsqzbYr6Z0TBA5Bzxh7u2fWGfe9h_P-8v",
            types = listOf(BaseRawResultType.REGION),
            names = listOf("Minsk"),
            languages = listOf("en"),
            addresses = listOf(SearchAddress(country = "Belarus")),
            fullAddress = "Belarus",
            descriptionAddress = "Belarus",
            matchingName = "Minsk",
            distanceMeters = 5000000.0,
            icon = "marker",
            categories = listOf("cafe"),
            action = BaseSuggestAction(
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

        val expectedResult = BaseServerSearchSuggestion(
            baseRawSearchResult,
            TEST_REQUEST_OPTIONS.copy(
                options = options,
                requestContext = TEST_REQUEST_OPTIONS.requestContext.copy(
                    responseUuid = "bf62f6f4-92db-11eb-a8b3-0242ac130003"
                )
            ).mapToBase()
        ).mapToPlatform()
        assertTrue(compareSearchResultWithServerSearchResult(expectedResult, first))

        assertEquals(SearchSuggestionType.SearchResultSuggestion(SearchResultType.PLACE, SearchResultType.REGION), suggestions[1].type)
        assertEquals(SearchSuggestionType.SearchResultSuggestion(SearchResultType.POI), suggestions[2].type)
        assertEquals(SearchSuggestionType.Category("cafe"), suggestions[3].type)
        assertEquals(SearchSuggestionType.Category("florist"), suggestions[4].type)
        assertEquals(SearchSuggestionType.Brand("Starbucks", "starbucks"), suggestions[5].type)

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
        suggestions.forEach { suggestion ->
            assertTrue((suggestion.type as? SearchSuggestionType.IndexableRecordItem)?.isHistoryRecord == true)

            val historyRecord = records.find { record -> record.id == suggestion.id }!!
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
        val historyRecord = records.first()
        assertEquals(historyRecord, searchResult.indexableRecord)

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
        assertEquals(records.size + 6, suggestions.size)

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
        assertEquals(record, suggestions.first().record)
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
        assertEquals(record, suggestions.first().record)
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
        assertFalse(suggestions.any { it.isIndexableRecordSuggestion })
    }

    @Test
    fun testIndexableRecordsMatching() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/suggestions-successful-for-indexable-records-matching.json"))

        val recordCoordinate = Point.fromLngLat(-122.41936, 37.77707)
        val record = createTestHistoryRecord(
            id = "id1",
            name = "Starbucks",
            coordinate = recordCoordinate,
            address = SearchAddress(
                country = "United States of America",
                region = "California",
                place = "San Francisco",
                neighborhood = "Downtown",
                postcode = "94102",
                street = "Van Ness",
                houseNumber = "150"
            ),
            searchResultType = SearchResultType.ADDRESS,
        )
        historyDataProvider.upsertBlocking(record, callbacksExecutor)

        val suggestionsResponse = searchEngine.searchBlocking(
            query = "Starbucks",
            options = SearchOptions(
                proximity = recordCoordinate,
                origin = recordCoordinate,
            )
        )

        val suggestions = suggestionsResponse.requireSuggestions()
        assertEquals(1, suggestions.size)

        val suggestion = suggestions.first()
        assertTrue(suggestion.type is SearchSuggestionType.IndexableRecordItem)

        val matchedRecord = (suggestion.type as SearchSuggestionType.IndexableRecordItem).record
        assertSame(record, matchedRecord)

        val selectionResult = searchEngine.selectBlocking(suggestion)
        val searchResult = selectionResult.requireResult().result
        assertSame(record, searchResult.indexableRecord)

        assertEquals(suggestion.id, record.id)
        assertEquals(searchResult.id, record.id)

        /**
         * Suggestion name, address and coordinate are always the same as in [IndexableRecord]
         * because record is matched by these fields.
         */
        assertEquals(suggestion.name, record.name)
        assertEquals(suggestion.address, record.address)
        assertEquals(suggestion.base.coordinate, record.coordinate)

        assertEquals(searchResult.name, record.name)
        assertEquals(searchResult.address, record.address)
        assertEquals(searchResult.base.coordinate, record.coordinate)

        /**
         * All the other fields might be different and suggestion should return values from backend
         * as they are likely to be up-to-date.
         *
         * If user needs values from indexable record, that data is also available to user from
         * [IndexableRecord] instance.
         */
        val routablePoints = listOf(
            CoreRoutablePoint(Point.fromLngLat(-122.419548, 37.777044), "POI")
        )
        assertEquals(
            routablePoints,
            suggestion.base.routablePoints
        )
        assertEquals(
            routablePoints,
            searchResult.routablePoints?.map { it.mapToCore() }
        )
        assertNotEquals(
            routablePoints,
            record.routablePoints
        )

        assertEquals(
            "150 Van Ness, San Francisco, California 94102, United States of America",
            suggestion.descriptionText
        )
        assertEquals(
            "150 Van Ness, San Francisco, California 94102, United States of America",
            searchResult.descriptionText
        )
        assertNotEquals(
            "150 Van Ness, San Francisco, California 94102, United States of America",
            record.descriptionText
        )

        assertEquals(listOf("cafe"), suggestion.categories)
        assertEquals(listOf("cafe"), searchResult.categories)
        assertNotEquals(listOf("cafe"), record.categories)

        assertEquals("restaurant", suggestion.makiIcon)
        assertEquals("restaurant", searchResult.makiIcon)
        assertNotEquals("restaurant", record.makiIcon)

        val metadata = SearchResultMetadata(
            createCoreResultMetadata(data = hashMapOf("iso_3166_1" to "US"))
        )
        assertEquals(metadata, suggestion.metadata)
        assertEquals(metadata, searchResult.metadata)
        assertNotEquals(metadata, record.metadata)
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

        val baseRawSearchResult = createTestBaseRawSearchResult(
            id = "place.11543680732831130",
            types = listOf(BaseRawResultType.PLACE, BaseRawResultType.REGION),
            names = listOf("Minsk"),
            descriptionAddress = "Minsk Region, Belarus, Planet Earth",
            languages = listOf("en"),
            addresses = listOf(SearchAddress(country = "Belarus", region = "Minsk Region")),
            fullAddress = "Minsk Region, Belarus, Planet Earth",
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

        val expectedResult = createTestServerSearchResult(
            listOf(SearchResultType.PLACE, SearchResultType.REGION),
            baseRawSearchResult,
            TEST_REQUEST_OPTIONS.run {
                copy(
                    options = options,
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

        val callbackResult = blockingCompletionCallback.getResultBlocking()
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

        val recursionSearchResult = createTestBaseRawSearchResult(
            id = "Y2WAgMLS1KJKAA==.42CAgMy8ktSivMQcAA==.42SAgKDU5NKi4sz8PAA=",
            types = listOf(BaseRawResultType.QUERY),
            names = listOf("Did you mean recursion?"),
            languages = listOf("en"),
            addresses = listOf(SearchAddress()),
            descriptionAddress = "Make a new search",
            fullAddress = null,
            matchingName = "Did you mean recursion?",
            icon = "marker",
            action = BaseSuggestAction(
                endpoint = "suggest",
                path = "Recursion",
                query = "&proximity=-122.084088,37.422065&language=en&limit=10",
                body = null,
                multiRetrievable = false
            ),
        )

        val expectedResult = BaseServerSearchSuggestion(
            recursionSearchResult,
            TEST_REQUEST_OPTIONS.copy(
                options = options,
            ).mapToBase()
        ).mapToPlatform()

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

        val baseRawCategorySuggestion = createTestBaseRawSearchResult(
            id = "42CAgOTEktT0_KJKAA==.42eAgMSCTN2C_Ezd4tSisszkVAA=.Y2GAgOTEtFQA",
            types = listOf(BaseRawResultType.CATEGORY),
            names = listOf("Cafe"),
            languages = listOf("en"),
            addresses = listOf(SearchAddress()),
            categories = listOf("Cafe"),
            descriptionAddress = "Category",
            fullAddress = null,
            matchingName = "Cafe",
            icon = "restaurant",
            externalIDs = mapOf("federated" to "category.cafe"),
            action = BaseSuggestAction(
                endpoint = "retrieve",
                path = "",
                query = null,
                body = "{\"id\":\"category-test-id\"}".toByteArray(),
                multiRetrievable = false
            ),
        )

        val expectedSearchSuggestion = BaseServerSearchSuggestion(
            baseRawCategorySuggestion,
            TEST_REQUEST_OPTIONS.run {
                copy(
                    options = options.copy(
                        types = listOf(QueryType.CATEGORY)
                    ),
                    requestContext = requestContext.copy(
                        responseUuid = "be35d556-9e14-4303-be15-57497c331348"
                    ),
                )
            }.mapToBase()
        ).mapToPlatform()
        assertTrue(compareSearchResultWithServerSearchResult(expectedSearchSuggestion, suggestions.first()))

        assertEquals(SearchSuggestionType.Category("cafe"), suggestions[0].type)
        assertEquals(SearchSuggestionType.Category("internet_cafe"), suggestions[1].type)

        assertNotNull(res.responseInfo.coreSearchResponse)
        assertTrue(res.responseInfo.isReproducible)

        mockServer.enqueue(createSuccessfulResponse("sbs_responses/retrieve-successful-category-cafe.json"))

        callback.reset()
        searchEngine.select(suggestions.first(), callback)

        val selectionResult = callback.getResultBlocking() as SearchEngineResult.Results
        val categoryResults = selectionResult.results

        val baseRawSearchResult = createTestBaseRawSearchResult(
            id = "ItFzVnwBAsSWFvXxpsTw",
            types = listOf(BaseRawResultType.POI),
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
                street = "10th St",
            )),
            fullAddress = "12 10th St, San Francisco, California 94103, United States of America",
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

        val expectedSearchResult = createTestServerSearchResult(
            listOf(SearchResultType.POI),
            baseRawSearchResult,
            TEST_REQUEST_OPTIONS.run {
                copy(
                    options = options.copy(
                        types = listOf(QueryType.CATEGORY)
                    ),
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
    fun testBrandSuggestionSelection() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/suggestions-successful-only-brand.json"))

        val options = SearchOptions(origin = TEST_ORIGIN_LOCATION)
        val suggestionsResult = searchEngine.searchBlocking(TEST_QUERY, options)
        val suggestions = suggestionsResult.requireSuggestions()
        assertEquals(1, suggestions.size)

        val baseRawCategorySuggestion = createTestBaseRawSearchResult(
            id = "test-brand-internal-id",
            types = listOf(BaseRawResultType.BRAND),
            names = listOf("Starbucks"),
            languages = listOf("en"),
            addresses = listOf(SearchAddress()),
            categories = null,
            descriptionAddress = "Brand",
            fullAddress = "Brand",
            matchingName = "Starbucks",
            icon = null,
            externalIDs = mapOf("federated" to "brand.test-external-id"),
            action = BaseSuggestAction(
                endpoint = "retrieve",
                path = "",
                query = null,
                body = "{\"id\":\"test-brand-action-id\"}".toByteArray(),
                multiRetrievable = false
            ),
        )

        val expectedSearchSuggestion = BaseServerSearchSuggestion(
            baseRawCategorySuggestion,
            TEST_REQUEST_OPTIONS.run {
                copy(
                    options = options,
                    requestContext = requestContext.copy(
                        responseUuid = "ca7c0ef1-ec25-40c4-962c-ca6e279a2146"
                    ),
                )
            }.mapToBase()
        ).mapToPlatform()

        val suggestion = suggestions.first()
        assertTrue(compareSearchResultWithServerSearchResult(expectedSearchSuggestion, suggestion))
        assertEquals(SearchSuggestionType.Brand("Starbucks", "test-external-id"), suggestion.type)

        mockServer.enqueue(createSuccessfulResponse("sbs_responses/retrieve-successful-category-cafe.json"))

        val selectionResult = searchEngine.selectBlocking(suggestion)
        val results = selectionResult.requireResults()
        assertEquals(results.size, 3)
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
    }

    @Test
    fun testNetworkError() {
        mockServer.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))

        val callback = BlockingSearchSelectionCallback()
        searchEngine.search(TEST_QUERY, SearchOptions(), callback)

        val res = callback.getResultBlocking()
        assertTrue(res is SearchEngineResult.Error && res.e is IOException)
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
    }

    @Test
    fun testCheckAsyncOperationTaskCompletion() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/suggestions-successful-for-minsk.json"))

        var countDownLatch = CountDownLatch(1)
        var task: AsyncOperationTask? = null

        var searchSuggestions: List<SearchSuggestion> = emptyList()
        task = searchEngine.search(TEST_QUERY, SearchOptions(), object : SearchSuggestionsCallback {
            override fun onSuggestions(suggestions: List<SearchSuggestion>, responseInfo: ResponseInfo) {
                searchSuggestions = suggestions
                assertTrue(task?.isDone == true)
                countDownLatch.countDown()
            }

            override fun onError(e: Exception) {
                fail("Error happened: $e")
            }
        })
        countDownLatch.await()

        mockServer.enqueue(createSuccessfulResponse("sbs_responses/retrieve-response-successful.json"))

        countDownLatch = CountDownLatch(1)
        var selectionTask: AsyncOperationTask? = null
        selectionTask = searchEngine.select(searchSuggestions.first(), object : SearchSelectionCallback {
            override fun onSuggestions(suggestions: List<SearchSuggestion>, responseInfo: ResponseInfo) {
                assertTrue(selectionTask?.isDone == true)
                countDownLatch.countDown()
            }

            override fun onError(e: Exception) {
                fail("Error happened: $e")
            }

            override fun onResult(suggestion: SearchSuggestion, result: SearchResult, responseInfo: ResponseInfo) {
                assertTrue(selectionTask?.isDone == true)
                countDownLatch.countDown()
            }

            override fun onResults(
                suggestion: SearchSuggestion,
                results: List<SearchResult>,
                responseInfo: ResponseInfo
            ) {
                assertTrue(selectionTask?.isDone == true)
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
    fun testSBSCzechAddressFormatting() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/suggestions-successful-czech.json"))

        val callback = BlockingSearchSelectionCallback()
        searchEngine.search(TEST_QUERY, SearchOptions(), callback)

        val suggestion = (callback.getResultBlocking() as SearchEngineResult.Suggestions).suggestions.first()

        assertEquals("Legerova 15", suggestion.name)
        assertEquals("Legerova 15, 12000 Praha, Praha, Česko", suggestion.fullAddress)
        assertEquals("12000 Praha, Praha, Česko", suggestion.descriptionText)
    }

    @Test
    fun testSBSPoiAddressFormatting() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/suggestions-address-formatting-test.json"))

        val suggestionsResponse = searchEngine.searchBlocking(TEST_QUERY, SearchOptions())

        val suggestions = suggestionsResponse.requireSuggestions()
        val suggestion = suggestions.first()

        assertEquals("667 Madison Ave, New York City, New York 10065, United States of America", suggestion.fullAddress)
    }

    @Test
    fun testSbsBackendDataCorrections() {
        /**
         * For some results backend sends incorrectly formatted fields. Search SDK tries to patch such data:
         * (SSDK-276) street: madison ave -> Madison Ave
         * (SSDK-277) name: 667 Madison -> 667 Madison Ave
         */
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/suggestions-data-formatting-corrections-test.json"))

        val suggestionsResponse = searchEngine.searchBlocking(TEST_QUERY, SearchOptions())

        val suggestions = suggestionsResponse.requireSuggestions()

        val suggestion1 = suggestions.first()
        assertEquals("667 Madison Ave", suggestion1.name)
        assertEquals("Madison Ave", suggestion1.address?.street)

        val suggestion2 = suggestions[1]
        assertEquals("E 59th St", suggestion2.address?.street)
    }

    @Test
    fun testGeocodingCzechAddressFormatting() {
        searchEngine = SearchEngine.createSearchEngine(ApiType.GEOCODING, searchEngineSettings)

        mockServer.enqueue(createSuccessfulResponse("geocoding_responses/suggestions-successful-czech.json"))

        val callback = BlockingSearchSelectionCallback()
        searchEngine.search(TEST_QUERY, SearchOptions(), callback)

        val suggestion = (callback.getResultBlocking() as SearchEngineResult.Suggestions).suggestions.first()

        assertEquals("Legerova 15", suggestion.name)
        assertEquals("Legerova 15, 12000 Praha, Praha, Česko", suggestion.fullAddress)
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
        searchEngine = SearchEngine.createSearchEngine(ApiType.GEOCODING, searchEngineSettings)

        mockServer.enqueue(createSuccessfulResponse("geocoding_responses/suggestions.json"))

        val callback = BlockingSearchSelectionCallback()
        searchEngine.search(TEST_QUERY, SearchOptions(), callback)

        val suggestions = callback.getResultBlocking().requireSuggestions()
        assertTrue(suggestions.isNotEmpty())
        assertEquals("fr", suggestions.first().metadata?.countryIso1)
    }

    @After
    override fun tearDown() {
        mockServer.shutdown()
        super.tearDown()
    }

    private companion object {

        const val TEST_QUERY = "Minsk"
        const val TEST_ACCESS_TOKEN = "pk.test"
        val TEST_USER_LOCATION: Point = Point.fromLngLat(10.1, 11.1234567)
        val TEST_ORIGIN_LOCATION: Point = Point.fromLngLat(10.1, 11.12345)
        val TEST_NAV_OPTIONS: SearchNavigationOptions = SearchNavigationOptions(
            navigationProfile = NavigationProfile.DRIVING,
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
                apiType = CoreApiType.SBS,
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
