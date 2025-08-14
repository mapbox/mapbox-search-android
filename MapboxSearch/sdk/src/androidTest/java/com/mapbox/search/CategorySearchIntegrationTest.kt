package com.mapbox.search

import com.mapbox.common.MapboxOptions
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.search.base.core.CoreApiType
import com.mapbox.search.base.core.CoreResultType
import com.mapbox.search.base.result.SearchRequestContext
import com.mapbox.search.base.utils.KeyboardLocaleProvider
import com.mapbox.search.base.utils.TimeProvider
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
import com.mapbox.search.record.FavoritesDataProvider
import com.mapbox.search.record.HistoryDataProvider
import com.mapbox.search.record.IndexableRecord
import com.mapbox.search.result.ResultAccuracy
import com.mapbox.search.result.SearchAddress
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchResultType
import com.mapbox.search.tests_support.BlockingCompletionCallback
import com.mapbox.search.tests_support.BlockingSearchCallback
import com.mapbox.search.tests_support.EmptySearchCallback
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
import com.mapbox.search.utils.assertEqualsIgnoreCase
import com.mapbox.search.utils.enqueueMultiple
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

/**
 * Contains only category search related functionality tests.
 * See [ReverseGeocodingSearchIntegrationTest], [SearchEngineIntegrationTest] for more tests.
 */
@Suppress("LargeClass", "DEPRECATION")
internal class CategorySearchIntegrationTest : BaseTest() {

    private lateinit var mockServer: MockWebServer
    private lateinit var searchEngine: SearchEngine
    private lateinit var historyDataProvider: HistoryDataProvider
    private lateinit var favoritesDataProvider: FavoritesDataProvider
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

        val searchEngineSettings = SearchEngineSettings(
            locationProvider = FixedPointLocationEngine(TEST_USER_LOCATION),
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

        val options = CategorySearchOptions(
            proximity = Point.fromLngLat(10.5, 20.123),
            boundingBox = BoundingBox.fromPoints(Point.fromLngLat(10.0, 15.0), Point.fromLngLat(30.0, 50.0)),
            countries = listOf(IsoCountryCode.UNITED_STATES, IsoCountryCode.BELARUS),
            fuzzyMatch = true,
            languages = listOf(IsoLanguageCode.ENGLISH),
            limit = 5,
            origin = Point.fromLngLat(50.123, 70.123),
            navigationProfile = NavigationProfile.DRIVING,
            routeOptions = TEST_ROUTE_OPTIONS
        )

        val callback = BlockingSearchCallback()
        searchEngine.search(TEST_CATEGORY, options, callback)

        val request = mockServer.takeRequest()
        assertEqualsIgnoreCase("post", request.method!!)

        val url = request.requestUrl!!
        assertEqualsIgnoreCase("//search/v1/category/$TEST_CATEGORY", url.encodedPath)
        assertEquals(TEST_ACCESS_TOKEN, url.queryParameter("access_token"))
        assertEquals(formatPoints(options.proximity), url.queryParameter("proximity"))
        assertEquals(
            formatPoints(options.boundingBox?.southwest(), options.boundingBox?.northeast()),
            url.queryParameter("bbox")
        )
        assertEquals(options.countries?.joinToString(separator = ",") { it.code }, url.queryParameter("country"))
        assertEquals(IsoLanguageCode.ENGLISH.code, url.queryParameter("language"))
        assertEquals(options.limit.toString(), url.queryParameter("limit"))

        assertEquals(url.queryParameter("origin"), formatPoints(options.origin))
        assertEquals(options.navigationProfile?.rawName!!, url.queryParameter("navigation_profile"))

        assertEquals(TEST_ROUTE_OPTIONS.timeDeviationMinutes.formatToBackendConvention(), url.queryParameter("time_deviation"))
        // Route encoded as polyline6 format, it's tricky to decode it manually and test.

        assertFalse(request.headers["X-Request-ID"].isNullOrBlank())
    }

    @Test
    fun testRequestDebounce() {
        mockServer.enqueue(MockResponse().setResponseCode(500))

        val requestDebounceMillis = 200L
        val options = CategorySearchOptions(requestDebounce = requestDebounceMillis.toInt())

        val callback = BlockingSearchCallback()
        searchEngine.search("bar", options, callback)
        searchEngine.search("cafe", options, callback)

        Thread.sleep(requestDebounceMillis)

        val req = mockServer.takeRequest(options.requestDebounce!!.toLong() * 2, TimeUnit.MILLISECONDS)!!
        assertEquals(1, mockServer.requestCount)
        assertEqualsIgnoreCase("//search/v1/category/cafe", req.requestUrl!!.encodedPath)
    }

    @Test
    fun testSuccessfulResponse() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/category/successful_response.json"))

        val callback = BlockingSearchCallback()
        searchEngine.search(TEST_CATEGORY, CategorySearchOptions(proximity = TEST_USER_LOCATION, origin = TEST_USER_LOCATION), callback)

        val res = callback.getResultBlocking()
        assertTrue(res is BlockingSearchCallback.SearchEngineResult.Results)
        res as BlockingSearchCallback.SearchEngineResult.Results
        assertEquals(3, res.results.size)

        val searchResult = res.results.first()

        val baseRawSearchResult = createTestBaseRawSearchResult(
            id = "6IXWdnYBo8NaDG6XivFv",
            types = listOf(CoreResultType.POI),
            names = listOf("Starbucks"),
            languages = listOf("def"), // should it be "en"?
            categories = listOf("restaurant", "food", "food and drink", "coffee shop", "coffee", "cafe"),
            categoryIds = listOf("restaurant", "food", "food_and_drink", "coffee_shop", "coffee", "cafe"),
            brand = listOf("Starbucks"),
            brandId = "starbucks",
            addresses = listOf(
                SearchAddress(
                    country = "United States of America",
                    houseNumber = "750",
                    neighborhood = "Old Mountain View",
                    place = "Mountain View",
                    postcode = "94041",
                    region = "California",
                    street = "Castro St"
                )
            ),
            fullAddress = "750 Castro St, Mountain View, California 94041, United States of America",
            descriptionAddress = "Starbucks, 750 Castro St, Mountain View, California 94041, United States of America",
            matchingName = "Starbucks",
            center = Point.fromLngLat(-122.08295, 37.38755),
            accuracy = ResultAccuracy.Point,
            routablePoints = listOf(
                RoutablePoint(point = Point.fromLngLat(-122.08295, 37.38755), name = "Address")
            ),
            icon = "restaurant",
            distanceMeters = 1.2677248179192241E7,
            metadata = SearchResultMetadata(
                metadata = hashMapOf("raw_plugshare" to "[{\"id\":616134,\"network_id\":8,\"outlets\":[{\"connector\":6,\"id\":1746812,\"kilowatts\":72,\"power\":1}]},{\"id\":616139,\"network_id\":8,\"outlets\":[{\"connector\":6,\"id\":1746817,\"kilowatts\":72,\"power\":1}]}]"),
                reviewCount = 12,
                phone = "+1 650-564-9255",
                website = "https://www.starbucks.com/store-locator/store/11148",
                averageRating = 3.5,
                rating = 3.5f,
                description = null,
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
                    totalCapacity = 2,
                    reservedForDisabilities = 1
                ),
                cpsJson = "{\"raw\":{}}"
            ),
            externalIDs = mapOf(
                "tripadvisor" to "4113702",
                "foursquare" to "4740b317f964a520724c1fe3",
            ),
        )

        val expected = createTestServerSearchResult(
            listOf(SearchResultType.POI),
            baseRawSearchResult,
            RequestOptions(
                query = TEST_CATEGORY,
                endpoint = "category",
                options = SearchOptions(proximity = TEST_USER_LOCATION, origin = TEST_USER_LOCATION),
                proximityRewritten = false,
                originRewritten = false,
                sessionID = "any",
                requestContext = SearchRequestContext(
                    apiType = CoreApiType.SBS,
                    keyboardLocale = TEST_KEYBOARD_LOCALE,
                    screenOrientation = TEST_ORIENTATION,
                    responseUuid = "544304d0-2007-4354-a599-c522cb150bb0"
                )
            )
        )

        assertTrue(compareSearchResultWithServerSearchResult(expected, searchResult))
        assertNotNull(res.responseInfo.coreSearchResponse)
    }

    @Test
    fun testOptionsLimit() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/category/successful_response.json"))

        val callback = BlockingSearchCallback()
        searchEngine.search(TEST_CATEGORY, CategorySearchOptions(limit = 1), callback)

        assertEquals(1, callback.getResultBlocking().requireResults().size)
    }

    @Test
    fun testSuccessfulEmptyResponse() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/category/successful_empty_response.json"))

        val callback = BlockingSearchCallback()
        searchEngine.search(TEST_CATEGORY, CategorySearchOptions(), callback)

        val res = callback.getResultBlocking() as BlockingSearchCallback.SearchEngineResult.Results
        assertTrue(res.results.isEmpty())
        assertNotNull(res.responseInfo.coreSearchResponse)
    }

    @Test
    fun testIndexableRecordsResponseOnly() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/category/successful_empty_response.json"))

        val records = (1..10).map {
            createTestHistoryRecord(
                id = "id$it",
                name = "$TEST_CATEGORY $it",
                categories = listOf(TEST_CATEGORY),
            )
        }
        historyDataProvider.upsertAllBlocking(records, callbacksExecutor)

        val callback = BlockingSearchCallback()
        searchEngine.search(TEST_CATEGORY, CategorySearchOptions(), callback)

        val results = callback.getResultBlocking().requireResults()
        assertEquals(records.size, results.size)
        assertTrue(results.all { it.indexableRecord != null })
    }

    @Test
    fun testMixedIndexableRecordsResponse() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/category/successful_response.json"))

        val callback = BlockingSearchCallback()
        searchEngine.search(TEST_CATEGORY, CategorySearchOptions(), callback)

        val firstRun = callback.getResultBlocking() as BlockingSearchCallback.SearchEngineResult.Results
        assertEquals(3, firstRun.results.size)
        assertFalse(firstRun.results.any { it.indexableRecord != null })
        assertEquals(0, historyDataProvider.getSizeBlocking(callbacksExecutor))
        assertNotNull(firstRun.responseInfo.coreSearchResponse)

        historyDataProvider.upsertBlocking(
            createHistoryRecord(firstRun.results.first(), timeProvider.currentTimeMillis()),
            callbacksExecutor,
        )

        callback.reset()
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/category/successful_response_new_ids.json"))
        searchEngine.search(TEST_CATEGORY, CategorySearchOptions(), callback)

        val secondRun = callback.getResultBlocking() as BlockingSearchCallback.SearchEngineResult.Results
        assertEquals(3, secondRun.results.size)

        val firstResult = secondRun.results.first()

        /**
         * Despite the changed ID, core should match and merge server result with local indexable record result
         */
        assertTrue(secondRun.results[0].indexableRecord != null)
        assertNotEquals(firstRun.results[0].id, firstResult.base.rawSearchResult.id)
        assertNotEquals(
            firstResult.base.rawSearchResult.id,
            firstResult.base.rawSearchResult.userRecordId
        )

        val blockingCompletionCallback = BlockingCompletionCallback<IndexableRecord?>()
        historyDataProvider.get(secondRun.results[0].id, blockingCompletionCallback)
        val callbackResult = blockingCompletionCallback.getResultBlocking()

        assertTrue(callbackResult is BlockingCompletionCallback.CompletionCallbackResult.Result)
        callbackResult as BlockingCompletionCallback.CompletionCallbackResult.Result

        assertEquals(callbackResult.result, secondRun.results[0].indexableRecord)

        assertNotNull(secondRun.responseInfo.coreSearchResponse)

        assertTrue(compareSearchResultWithServerSearchResult(secondRun.results[1], firstRun.results[1]))
        assertTrue(compareSearchResultWithServerSearchResult(secondRun.results[2], firstRun.results[2]))
    }

    @Test
    fun testIgnoredIndexableRecordsResponse() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/category/successful_response.json"))

        val callback = BlockingSearchCallback()
        searchEngine.search(TEST_CATEGORY, CategorySearchOptions(), callback)

        val firstRun = callback.getResultBlocking() as BlockingSearchCallback.SearchEngineResult.Results
        assertEquals(3, firstRun.results.size)
        assertFalse(firstRun.results.any { it.indexableRecord != null })
        assertEquals(0, historyDataProvider.getSizeBlocking(callbacksExecutor))
        assertNotNull(firstRun.responseInfo.coreSearchResponse)

        historyDataProvider.upsertBlocking(
            createHistoryRecord(firstRun.results.first(), timeProvider.currentTimeMillis()),
            callbacksExecutor,
        )

        callback.reset()
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/category/successful_response_new_ids.json"))
        searchEngine.search(TEST_CATEGORY, CategorySearchOptions(ignoreIndexableRecords = true), callback)

        val secondRun = callback.getResultBlocking() as BlockingSearchCallback.SearchEngineResult.Results
        assertEquals(3, secondRun.results.size)
        assertFalse(secondRun.results.any { it.indexableRecord != null })
        assertNotNull(secondRun.responseInfo.coreSearchResponse)
    }

    @Test
    fun testIndexableRecordsZeroThreshold() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/category/successful_response.json"))

        val recordCoordinate = Point.fromLngLat(2.295135021209717, 48.859291076660156)
        val record = createTestHistoryRecord(
            id = "id1",
            name = TEST_CATEGORY,
            coordinate = recordCoordinate,
            categories = listOf(TEST_CATEGORY),
        )
        historyDataProvider.upsertBlocking(record, callbacksExecutor)

        val callback = BlockingSearchCallback()
        searchEngine.search(
            TEST_CATEGORY,
            CategorySearchOptions(
                proximity = recordCoordinate,
                origin = recordCoordinate,
                indexableRecordsDistanceThresholdMeters = 0.0
            ),
            callback
        )

        val results = (callback.getResultBlocking() as BlockingSearchCallback.SearchEngineResult.Results).results
        assertEquals(record, results.first().indexableRecord)
    }

    @Test
    fun testIndexableRecordsInsideThreshold() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/category/successful_response.json"))

        val recordCoordinate = Point.fromLngLat(2.295135021209717, 48.859291076660156)
        val record = createTestHistoryRecord(
            id = "id1",
            name = TEST_CATEGORY,
            coordinate = recordCoordinate,
            categories = listOf(TEST_CATEGORY),
        )
        historyDataProvider.upsertBlocking(record, callbacksExecutor)

        // recordCoordinate + approximately 50 meters
        val userLocation = Point.fromLngLat(2.29497347098094, 48.8580726347223)

        val callback = BlockingSearchCallback()
        searchEngine.search(
            TEST_CATEGORY,
            CategorySearchOptions(
                proximity = userLocation,
                origin = userLocation,
                indexableRecordsDistanceThresholdMeters = 500.0
            ),
            callback
        )

        val results = (callback.getResultBlocking() as BlockingSearchCallback.SearchEngineResult.Results).results
        assertEquals(record, results.first().indexableRecord)
    }

    @Test
    fun testIndexableRecordsOutsideThreshold() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/category/successful_response.json"))

        val recordCoordinate = Point.fromLngLat(2.295135021209717, 48.859291076660156)
        val record = createTestHistoryRecord(
            id = "id1",
            name = TEST_CATEGORY,
            coordinate = recordCoordinate,
            categories = listOf(TEST_CATEGORY),
        )
        historyDataProvider.upsertBlocking(record, callbacksExecutor)

        // recordCoordinate + approximately 50 meters
        val userLocation = Point.fromLngLat(2.29497347098094, 48.8580726347223)

        val callback = BlockingSearchCallback()
        searchEngine.search(
            TEST_CATEGORY,
            CategorySearchOptions(
                proximity = userLocation,
                origin = userLocation,
                indexableRecordsDistanceThresholdMeters = 15.0
            ),
            callback
        )

        val results = (callback.getResultBlocking() as BlockingSearchCallback.SearchEngineResult.Results).results
        assertFalse(results.any { it.indexableRecord != null })
    }

    @Test
    fun testSuccessfulIncorrectResponse() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/category/successful_incorrect_response.json"))

        try {
            val callback = BlockingSearchCallback()
            searchEngine.search(TEST_CATEGORY, CategorySearchOptions(), callback)
            if (BuildConfig.DEBUG) {
                Assert.fail()
            }
        } catch (t: Throwable) {
            if (!BuildConfig.DEBUG) {
                Assert.fail()
            }
        }
    }

    @Test
    fun testConsecutiveRequests() {
        mockServer.enqueueMultiple(createSuccessfulResponse("sbs_responses/category/successful_incorrect_response.json"), 2)

        val task1 = searchEngine.search(TEST_CATEGORY, CategorySearchOptions(requestDebounce = 1000), EmptySearchCallback)

        val callback = BlockingSearchCallback()
        val task2 = searchEngine.search(TEST_CATEGORY, CategorySearchOptions(), callback)
        callback.getResultBlocking()

        assertTrue(task1.isCancelled)
        assertTrue(task2.isDone)
    }

    @Test
    fun testErrorResponse() {
        mockServer.enqueue(MockResponse().setResponseCode(404))

        val callback = BlockingSearchCallback()
        searchEngine.search(TEST_CATEGORY, CategorySearchOptions(), callback)

        val res = callback.getResultBlocking()
        assertTrue(res is BlockingSearchCallback.SearchEngineResult.Error && res.e is SearchRequestException && res.e.code == 404)
    }

    @Test
    fun testNetworkError() {
        mockServer.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))

        val callback = BlockingSearchCallback()
        searchEngine.search(TEST_CATEGORY, CategorySearchOptions(), callback)

        val res = callback.getResultBlocking()
        assertTrue(res is BlockingSearchCallback.SearchEngineResult.Error && res.e is IOException)
    }

    @Test
    fun testBrokenResponseContent() {
        mockServer.enqueue(MockResponse().setResponseCode(200).setBody("I'm broken"))

        val callback = BlockingSearchCallback()
        searchEngine.search(TEST_CATEGORY, CategorySearchOptions(), callback)

        val res = callback.getResultBlocking()
        assertTrue(res is BlockingSearchCallback.SearchEngineResult.Error)
        res as BlockingSearchCallback.SearchEngineResult.Error
    }

    @Test
    fun testCheckAsyncOperationTaskCompletion() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/category/successful_response.json"))

        val countDownLatch = CountDownLatch(1)
        var task: AsyncOperationTask? = null

        task = searchEngine.search(TEST_CATEGORY, CategorySearchOptions(), object : SearchCallback {
            override fun onResults(results: List<SearchResult>, responseInfo: ResponseInfo) {
                assertTrue(task?.isDone == true)
                countDownLatch.countDown()
            }

            override fun onError(e: Exception) {
                Assert.fail("Error happened: $e")
            }
        })

        countDownLatch.await()
    }

    @Test
    fun testErrorBackendResponseSimpleFormat() {
        val errorResponse = MockResponse()
            .setResponseCode(422)
            .setBody(readFileFromAssets("sbs_responses/suggestions-error-response-simple-format.json"))

        mockServer.enqueue(errorResponse)

        val callback = BlockingSearchCallback()
        searchEngine.search(TEST_CATEGORY, CategorySearchOptions(), callback)

        val res = callback.getResultBlocking()
        assertTrue(res is BlockingSearchCallback.SearchEngineResult.Error)

        assertEquals(
            SearchRequestException("Wrong arguments", 422),
            (res as BlockingSearchCallback.SearchEngineResult.Error).e
        )
    }

    @Test
    fun testErrorBackendResponseExtendedFormat() {
        val errorResponse = MockResponse()
            .setResponseCode(400)
            .setBody(readFileFromAssets("sbs_responses/suggestions-error-response-extended-format.json"))

        mockServer.enqueue(errorResponse)

        val callback = BlockingSearchCallback()
        searchEngine.search(TEST_CATEGORY, CategorySearchOptions(), callback)

        val res = callback.getResultBlocking()
        assertTrue(res is BlockingSearchCallback.SearchEngineResult.Error)

        assertEquals(
            SearchRequestException(
                readFileFromAssets("sbs_responses/suggestions-error-response-extended-format.json"),
                400
            ),
            (res as BlockingSearchCallback.SearchEngineResult.Error).e
        )
    }

    @After
    override fun tearDown() {
        mockServer.shutdown()
        super.tearDown()
    }

    private companion object {

        const val TEST_CATEGORY = "cafe"
        const val TEST_ACCESS_TOKEN = "pk.test"
        val TEST_USER_LOCATION: Point = Point.fromLngLat(10.1, 11.1234567)

        const val TEST_LOCAL_TIME_MILLIS = 12345L
        val TEST_KEYBOARD_LOCALE: Locale = Locale.ENGLISH
        val TEST_ORIENTATION = ScreenOrientation.PORTRAIT

        val TEST_ROUTE_OPTIONS = RouteOptions(
            route = listOf(Point.fromLngLat(1.0, 2.0), Point.fromLngLat(3.0, 4.0), Point.fromLngLat(5.0, 6.0)),
            deviation = RouteOptions.Deviation.Time(value = 30, unit = TimeUnit.SECONDS)
        )
    }
}
