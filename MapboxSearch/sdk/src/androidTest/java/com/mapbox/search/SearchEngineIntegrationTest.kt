package com.mapbox.search

import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.search.base.core.CoreApiType
import com.mapbox.search.base.core.CoreRoutablePoint
import com.mapbox.search.base.core.createCoreResultMetadata
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
import com.mapbox.search.common.metadata.OpenPeriod
import com.mapbox.search.common.metadata.WeekDay
import com.mapbox.search.common.metadata.WeekTimestamp
import com.mapbox.search.common.tests.FixedPointLocationEngine
import com.mapbox.search.common.tests.equalsTo
import com.mapbox.search.record.FavoritesDataProvider
import com.mapbox.search.record.HistoryDataProvider
import com.mapbox.search.record.IndexableRecord
import com.mapbox.search.result.SearchAddress
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchResultType
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.result.SearchSuggestionType
import com.mapbox.search.result.isIndexableRecordSuggestion
import com.mapbox.search.result.record
import com.mapbox.search.tests_support.BlockingSearchSelectionCallback
import com.mapbox.search.tests_support.BlockingSearchSelectionCallback.SearchEngineResult
import com.mapbox.search.tests_support.EmptySearchSuggestionsCallback
import com.mapbox.search.tests_support.createHistoryRecord
import com.mapbox.search.tests_support.createSearchEngineWithBuiltInDataProvidersBlocking
import com.mapbox.search.tests_support.createTestHistoryRecord
import com.mapbox.search.tests_support.record.clearBlocking
import com.mapbox.search.tests_support.record.getAllBlocking
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

        mockServer = MockWebServer()

        MapboxSearchSdk.initialize(
            application = targetApplication,
            timeProvider = timeProvider,
            keyboardLocaleProvider = keyboardLocaleProvider,
            orientationProvider = orientationProvider,
        )

        searchEngineSettings = SearchEngineSettings(
            accessToken = TEST_ACCESS_TOKEN,
            locationEngine = FixedPointLocationEngine(TEST_USER_LOCATION),
            baseUrl = mockServer.url("").toString()
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
        assertEqualsIgnoreCase("get", request.method!!)

        val url = request.requestUrl!!
        assertEqualsIgnoreCase("//search/searchbox/v1/suggest", url.encodedPath)
        assertEquals(TEST_QUERY, url.queryParameter("q"))
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
        assertFalse(url.queryParameter("route").isNullOrEmpty())
        assertEquals("polyline6", url.queryParameter("route_geometry"))

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
        assertEqualsIgnoreCase("//search/searchbox/v1/suggest", req.requestUrl!!.encodedPath)
        assertEquals("actual query", req.requestUrl?.queryParameter("q"))
    }

    @Test
    fun testSuccessfulResponse() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/forward/suggestions-successful.json"))

        val options = SearchOptions(origin = TEST_ORIGIN_LOCATION, navigationOptions = TEST_NAV_OPTIONS)
        val response = searchEngine.searchBlocking(TEST_QUERY, options)
        assertTrue(response.isSuggestions)

        val suggestions = response.requireSuggestions()
        assertEquals(3, suggestions.size)

        val suggestion = suggestions[0]
        assertEquals("suggestion-id-1", suggestion.id)
        assertEquals("Washington", suggestion.name)
        assertEquals(
            "Washington, District of Columbia 20036, United States of America",
            suggestion.descriptionText
        )
        assertEquals(
            SearchAddress(
                country = "United States of America",
                region = "District of Columbia",
                postcode = "20036",
                place = "Washington",
                neighborhood = "Dupont Circle",
                street = "Connecticut Ave Nw"
            ),
            suggestion.address
        )
        assertEquals(
            "1211 Connecticut Ave NW, Washington, District of Columbia 20036, United States of America",
            suggestion.fullAddress
        )
        assertEquals(900.0, suggestion.distanceMeters)
        assertEquals(listOf("gym", "services"), suggestion.categories)
        assertEquals("marker", suggestion.makiIcon)
        assertEquals(null, suggestion.etaMinutes)
        assertEquals(null, suggestion.metadata)
        assertEquals(mapOf("id-1" to "id-1-key"), suggestion.externalIDs)
        assertEquals(0, suggestion.serverIndex)
        assertEquals(SearchSuggestionType.SearchResultSuggestion(listOf(SearchResultType.POI)), suggestion.type)

        assertEquals(SearchSuggestionType.SearchResultSuggestion(SearchResultType.PLACE), suggestions[1].type)
        assertEquals(SearchSuggestionType.SearchResultSuggestion(SearchResultType.STREET), suggestions[2].type)
    }

    @Test
    fun testOptionsLimit() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/forward/suggestions-successful.json"))

        val response = searchEngine.searchBlocking(TEST_QUERY, SearchOptions(limit = 1))
        assertEquals(1, response.requireSuggestions().size)
    }

    @Test
    fun testSuccessfulEmptyResponse() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/forward/suggestions-successful-empty.json"))

        val response = searchEngine.searchBlocking(TEST_QUERY, SearchOptions())

        assertTrue(response.requireSuggestionsAndInfo().first.isEmpty())
        assertNotNull(response.requireSuggestionsAndInfo().second.coreSearchResponse)
    }

    @Test
    fun testIndexableRecordsResponseOnly() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/forward/suggestions-successful-empty.json"))

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
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/forward/suggestions-successful.json"))

        val records = (1..10).map {
            createTestHistoryRecord(id = "id$it", name = "$TEST_QUERY $it")
        }
        historyDataProvider.upsertAllBlocking(records, callbacksExecutor)

        val response = searchEngine.searchBlocking(TEST_QUERY, SearchOptions())
        val suggestions = response.requireSuggestions()

        // records.size + 3 = records.size + number of suggestions from server
        assertEquals(records.size + 3, suggestions.size)

        records.indices.forEach { i ->
            assertTrue(suggestions[i].type is SearchSuggestionType.IndexableRecordItem)
        }
        (records.size until suggestions.size).forEach { i ->
            assertFalse(suggestions[i].type is SearchSuggestionType.IndexableRecordItem)
        }
    }

    @Test
    fun testMixedIndexableRecordsResponseWithLimit() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/forward/suggestions-successful.json"))

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
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/forward/suggestions-successful.json"))

        val records = (1..3).map {
            createTestHistoryRecord(id = "id$it", name = "$TEST_QUERY $it")
        }

        historyDataProvider.upsertAllBlocking(records, callbacksExecutor)

        val response = searchEngine.searchBlocking(
            TEST_QUERY,
            SearchOptions(ignoreIndexableRecords = true)
        )

        val suggestions = response.requireSuggestions()
        assertTrue(suggestions.isNotEmpty())
        assertFalse(suggestions.any { it.type is SearchSuggestionType.IndexableRecordItem })
        assertNotNull(response.requireSuggestionsAndInfo().second.coreSearchResponse)
    }

    @Test
    fun testIndexableRecordsZeroThreshold() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/forward/suggestions-successful.json"))

        val recordCoordinate = Point.fromLngLat(2.295135021209717, 48.859291076660156)
        val record = createTestHistoryRecord(id = "id1", name = TEST_QUERY, coordinate = recordCoordinate)
        historyDataProvider.upsertBlocking(record, callbacksExecutor)

        val response = searchEngine.searchBlocking(
            TEST_QUERY, SearchOptions(
                proximity = recordCoordinate,
                origin = recordCoordinate,
                indexableRecordsDistanceThresholdMeters = 0.0
            )
        )

        assertEquals(record, response.requireSuggestions().first().record)
    }

    @Test
    fun testIndexableRecordsInsideThreshold() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/forward/suggestions-successful.json"))

        val recordCoordinate = Point.fromLngLat(2.2945173400760424, 48.85832005563483)
        val record = createTestHistoryRecord(id = "id1", name = TEST_QUERY, coordinate = recordCoordinate)
        historyDataProvider.upsertBlocking(record, callbacksExecutor)

        // recordCoordinate + approximately 50 meters
        val userLocation = Point.fromLngLat(2.29497347098094, 48.8580726347223)
        val response = searchEngine.searchBlocking(
            TEST_QUERY, SearchOptions(
                origin = userLocation,
                proximity = userLocation,
                indexableRecordsDistanceThresholdMeters = 500.0
            )
        )

        assertEquals(record, response.requireSuggestions().first().record)
    }

    @Test
    fun testIndexableRecordsOutsideThreshold() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/forward/suggestions-successful.json"))

        val recordCoordinate = Point.fromLngLat(2.2945173400760424, 48.85832005563483)
        val record = createTestHistoryRecord(id = "id1", name = TEST_QUERY, coordinate = recordCoordinate)
        historyDataProvider.upsertBlocking(record, callbacksExecutor)

        // recordCoordinate + approximately 50 meters
        val userLocation = Point.fromLngLat(2.29497347098094, 48.8580726347223)
        val response = searchEngine.searchBlocking(
            TEST_QUERY,
            SearchOptions(
                proximity = userLocation,
                origin = userLocation,
                indexableRecordsDistanceThresholdMeters = 15.0
            )
        )

        assertFalse(response.requireSuggestions().any { it.isIndexableRecordSuggestion })
    }

    @Test
    fun testIndexableRecordsMatching() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/forward/suggestions-test-records-matching.json"))

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
            CoreRoutablePoint(Point.fromLngLat(-122.41936, 37.77707), "default")
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
            "San Francisco, California 94102, United States of America",
            suggestion.descriptionText
        )
        assertEquals(
            "San Francisco, California 94102, United States of America",
            searchResult.descriptionText
        )
        assertNotEquals(
            "San Francisco, California 94102, United States of America",
            record.descriptionText
        )

        assertEquals(listOf("cafe"), suggestion.categories)
        assertEquals(listOf("cafe"), searchResult.categories)
        assertNotEquals(listOf("cafe"), record.categories)

        assertEquals("restaurant", suggestion.makiIcon)
        assertEquals("restaurant", searchResult.makiIcon)
        assertNotEquals("restaurant", record.makiIcon)

        val metadata = SearchResultMetadata(
            createCoreResultMetadata(phone = "+123 456 789", data = hashMapOf())
        )
        assertEquals(metadata, suggestion.metadata)
        assertEquals(metadata, searchResult.metadata)
        assertNotEquals(metadata, record.metadata)
    }

    @Test
    fun testSuccessfulSuggestionSelection() {
        assertTrue(historyDataProvider.getAllBlocking().isEmpty())

        mockServer.enqueue(createSuccessfulResponse("sbs_responses/forward/suggestions-successful.json"))

        val suggestionsResponse = searchEngine.searchBlocking(TEST_QUERY)
        val suggestions = suggestionsResponse.requireSuggestions()

        mockServer.enqueue(createSuccessfulResponse("sbs_responses/forward/retrieve-suggest.json"))

        val selectionResponse = searchEngine.selectBlocking(suggestions.first())
        val selectionResult = selectionResponse.requireResult()
        assertNull(selectionResult.responseInfo.coreSearchResponse)

        val searchResult = selectionResult.result

        assertEquals("test-id", searchResult.id)
        assertEquals("Washington", searchResult.name)
        assertEquals(
            "Washington, District of Columbia 20036, United States of America",
            searchResult.descriptionText
        )
        assertEquals(
            "1211 Connecticut Ave NW, Washington, District of Columbia 20036, United States of America",
            searchResult.fullAddress
        )
        // TODO fixme Search Native should parse accuracy
        // assertEquals(ResultAccuracy.APPROXIMATE, searchResult.accuracy)
        assertEquals(
            SearchAddress(
                country = "United States of America",
                neighborhood = "Dupont Circle",
                place = "Washington",
                postcode = "20036",
                region = "District of Columbia",
                street = "Connecticut Ave Nw"
            ),
            searchResult.address
        )
        assertEquals(
            listOf(
                RoutablePoint(
                    Point.fromLngLat(-77.0412880184587, 38.90608285774093), "default"
                )
            ), searchResult.routablePoints
        )
        assertEquals(Point.fromLngLat(-77.041093, 38.906197), searchResult.coordinate)
        assertEquals("marker", searchResult.makiIcon)
        assertEquals(listOf("gym", "services"), searchResult.categories)
        assertEquals(null, searchResult.etaMinutes)
        assertEquals(null, searchResult.indexableRecord)
        assertEquals(900.0, searchResult.distanceMeters)
        assertEquals(hashMapOf("id-1" to "id-1-value"), searchResult.externalIDs)
        assertEquals(listOf(SearchResultType.POI), searchResult.types)
        assertEquals(0, searchResult.serverIndex)

        with(searchResult.metadata!!) {
            assertEquals("+123 456 789", phone)
            assertEquals("https://www.test.com", website)
            assertEquals(35, reviewCount)
            assertEquals(4.0, averageRating)
            assertEquals(
                OpenHours.Scheduled(
                    periods = listOf(
                        OpenPeriod(
                            open = WeekTimestamp(WeekDay.SATURDAY, 7, 0),
                            closed = WeekTimestamp(WeekDay.SATURDAY, 20, 0)
                        )
                    )
                ), openHours
            )
            assertEquals(
                listOf(ImageInfo("https://test.com/img-primary.jpg", 300, 350)),
                primaryPhotos
            )
            assertEquals(
                listOf(ImageInfo("https://test.com/img-other.jpg", 150, 350)),
                otherPhotos
            )
        }

        assertEquals(1, historyDataProvider.getSizeBlocking(callbacksExecutor))

        val historyData = historyDataProvider.getAllBlocking()
        assertEquals(1, historyData.size)

        assertEquals(
            createHistoryRecord(searchResult, TEST_LOCAL_TIME_MILLIS),
            historyData.first()
        )
    }

    @Test
    fun testSuccessfulSuggestionSelectionWithTurnedOffAddToHistoryLogic() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/forward/suggestions-successful.json"))
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/forward/retrieve-suggest.json"))

        assertEquals(0, historyDataProvider.getSizeBlocking())

        val suggestionsResponse = searchEngine.searchBlocking(TEST_QUERY)
        val suggestions = suggestionsResponse.requireSuggestions()

        val selectionResponse = searchEngine.selectBlocking(
            suggestions.first(), SelectOptions(addResultToHistory = false)
        )
        assertTrue(selectionResponse.isResult)

        assertEquals(0, historyDataProvider.getSizeBlocking())
    }

    // TODO FIXME recursive
//    @Test
//    fun testRecursiveQuerySelection() {
//        mockServer.enqueue(createSuccessfulResponse("sbs_responses/forward/suggestions-with-recursive.json"))
//        mockServer.enqueue(createSuccessfulResponse("sbs_responses/forward/suggestions-with-recursive.json"))
//
//        val options = SearchOptions(origin = TEST_ORIGIN_LOCATION, navigationOptions = TEST_NAV_OPTIONS)
//        val response = searchEngine.searchBlocking(TEST_QUERY, options)
//
//        val suggestions = response.requireSuggestions()
//        assertEquals(2, suggestions.size)
//
//        val suggestion = suggestions.first()
//        assertEquals("Did you mean recursion?", suggestion.name)
//        assertEquals("Make a new search", suggestion.descriptionText)
//        assertEquals(SearchSuggestionType.Query, suggestion.type)
//
//        val selectionResponse = searchEngine.selectBlocking(suggestion)
//        assertTrue(selectionResponse.isSuggestions)
//    }

    @Test
    fun testCategorySuggestionSelection() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/forward/suggestions-category.json"))
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/forward/retrieve-category-cafe.json"))

        val response = searchEngine.searchBlocking(TEST_QUERY)
        val suggestions = response.requireSuggestions()

        val suggestion = suggestions[0]
        assertEquals("Cafe", suggestion.name)
        assertEquals(SearchSuggestionType.Category("cafe"), suggestion.type)
        assertEquals(listOf("Cafe"), suggestion.categories)

        val selectionResponse = searchEngine.selectBlocking(suggestion)
        assertTrue(selectionResponse.isResults)
        assertEquals(3, selectionResponse.requireResults().size)
    }

    @Test
    fun testBrandSuggestionSelection() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/forward/suggestions-brand.json"))
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/forward/retrieve-category-cafe.json"))

        val response = searchEngine.searchBlocking(TEST_QUERY)
        val suggestions = response.requireSuggestions()
        assertEquals(1, suggestions.size)

        val suggestion = suggestions.first()
        assertEquals("Starbucks", suggestion.name)
        assertEquals(SearchSuggestionType.Brand("", ""), suggestion.type)

        val selectionResponse = searchEngine.selectBlocking(suggestion)
        assertTrue(selectionResponse.isResults)
        assertEquals(3, selectionResponse.requireResults().size)
    }

    @Test
    fun testSuccessfulIncorrectResponse() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/forward/suggestions-successful-incorrect.json"))

        try {
            searchEngine.searchBlocking(TEST_QUERY)
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
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/forward/suggestions-successful.json"))
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
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/forward/suggestions-successful.json"))

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

        mockServer.enqueue(createSuccessfulResponse("sbs_responses/forward/retrieve-suggest.json"))

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
        mockServer.enqueueMultiple(createSuccessfulResponse("sbs_responses/forward/suggestions-successful.json"), 2)

        val task1 = searchEngine.search(TEST_QUERY, SearchOptions(requestDebounce = 1000), EmptySearchSuggestionsCallback)

        val callback = BlockingSearchSelectionCallback()
        val task2 = searchEngine.search(TEST_QUERY, SearchOptions(), callback)
        callback.getResultBlocking()

        assertTrue(task1.isCancelled)
        assertTrue(task2.isDone)
    }

    @Test
    fun testSBSCzechAddressFormatting() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/forward/suggestions-test-address-formatting.json"))

        val response = searchEngine.searchBlocking(TEST_QUERY)
        assertTrue(response.isSuggestions)

        val suggestions = response.requireSuggestions()
        val suggestion = suggestions.first()

        assertEquals("Legerova 15", suggestion.name)
        assertEquals("Legerova 15, 12000 Praha, Praha, Česko", suggestion.fullAddress)
        assertEquals("12000 Praha, Praha, Česko", suggestion.descriptionText)
    }

    @Test
    fun testSbsBackendDataCorrections() {
        /**
         * For some results backend sends incorrectly formatted fields. Search SDK tries to patch such data:
         * (SSDK-276) street: madison ave -> Madison Ave
         * (SSDK-277) name: 667 Madison -> 667 Madison Ave
         */
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/forward/suggestions-backend-patches.json"))

        val suggestionsResponse = searchEngine.searchBlocking(TEST_QUERY, SearchOptions())

        val suggestions = suggestionsResponse.requireSuggestions()
        val suggestion = suggestions.first()

        assertEquals("667 Madison Ave", suggestion.name)
        assertEquals("667 Madison Ave, New York City, New York 10065, United States of America", suggestion.fullAddress)
        assertEquals("Madison Ave", suggestion.address?.street)

        assertEquals("E 59th St", suggestions[1].address?.street)
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

        const val TEST_QUERY = "Washington"
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
                apiType = CoreApiType.SEARCH_BOX,
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
