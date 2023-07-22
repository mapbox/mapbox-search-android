package com.mapbox.search

import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.search.base.utils.KeyboardLocaleProvider
import com.mapbox.search.base.utils.TimeProvider
import com.mapbox.search.base.utils.orientation.ScreenOrientation
import com.mapbox.search.base.utils.orientation.ScreenOrientationProvider
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.common.IsoCountryCode
import com.mapbox.search.common.IsoLanguageCode
import com.mapbox.search.common.NavigationProfile
import com.mapbox.search.common.SearchRequestException
import com.mapbox.search.common.concurrent.SearchSdkMainThreadWorker
import com.mapbox.search.common.tests.FixedPointLocationEngine
import com.mapbox.search.record.FavoritesDataProvider
import com.mapbox.search.record.HistoryDataProvider
import com.mapbox.search.result.SearchResult
import com.mapbox.search.tests_support.BlockingSearchCallback
import com.mapbox.search.tests_support.EmptySearchCallback
import com.mapbox.search.tests_support.categorySearchBlocking
import com.mapbox.search.tests_support.compareSearchResultWithServerSearchResult
import com.mapbox.search.tests_support.createHistoryRecord
import com.mapbox.search.tests_support.createSearchEngineWithBuiltInDataProvidersBlocking
import com.mapbox.search.tests_support.createTestHistoryRecord
import com.mapbox.search.tests_support.record.clearBlocking
import com.mapbox.search.tests_support.record.getBlocking
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
@Suppress("LargeClass")
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

        mockServer = MockWebServer()

        MapboxSearchSdk.initialize(
            application = targetApplication,
            timeProvider = timeProvider,
            keyboardLocaleProvider = keyboardLocaleProvider,
            orientationProvider = orientationProvider,
        )

        val searchEngineSettings = SearchEngineSettings(
            accessToken = TEST_ACCESS_TOKEN,
            locationEngine = FixedPointLocationEngine(TEST_USER_LOCATION),
            baseUrl = mockServer.url("").toString()
        )

        searchEngine = createSearchEngineWithBuiltInDataProvidersBlocking(
            apiType = ApiType.SearchBox,
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

        searchEngine.categorySearchBlocking(TEST_CATEGORY, options)

        val request = mockServer.takeRequest()
        assertEqualsIgnoreCase("get", request.method!!)

        val url = request.requestUrl!!
        assertEqualsIgnoreCase("//search/searchbox/v1/category/$TEST_CATEGORY", url.encodedPath)
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
        assertFalse(url.queryParameter("route").isNullOrEmpty())
        assertEquals("polyline6", url.queryParameter("route_geometry"))

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
        assertEqualsIgnoreCase("//search/searchbox/v1/category/cafe", req.requestUrl!!.encodedPath)
    }

    @Test
    fun testSuccessfulResponse() {
        mockServer.enqueue(createSuccessfulResponse("search_box_responses/category/successful_response.json"))

        val response = searchEngine.categorySearchBlocking(TEST_CATEGORY)

        val (results, responseInfo) = response.requireResultPair()
        assertEquals(3, results.size)
        assertNotNull(responseInfo.coreSearchResponse)
    }

    @Test
    fun testOptionsLimit() {
        mockServer.enqueue(createSuccessfulResponse("search_box_responses/category/successful_response.json"))

        val response = searchEngine.categorySearchBlocking(
            TEST_CATEGORY,
            CategorySearchOptions(limit = 1)
        )

        assertEquals(1, response.requireResults().size)
    }

    @Test
    fun testSuccessfulEmptyResponse() {
        mockServer.enqueue(createSuccessfulResponse("search_box_responses/category/successful_empty_response.json"))

        val response = searchEngine.categorySearchBlocking(TEST_CATEGORY)
        val (results, responseInfo) = response.requireResultPair()

        assertTrue(results.isEmpty())
        assertNotNull(responseInfo.coreSearchResponse)
    }

    @Test
    fun testIndexableRecordsResponseOnly() {
        mockServer.enqueue(createSuccessfulResponse("search_box_responses/category/successful_empty_response.json"))

        val records = (1..10).map {
            createTestHistoryRecord(
                id = "id$it",
                name = "$TEST_CATEGORY $it",
                categories = listOf(TEST_CATEGORY),
            )
        }
        historyDataProvider.upsertAllBlocking(records, callbacksExecutor)

        val response = searchEngine.categorySearchBlocking(TEST_CATEGORY, CategorySearchOptions())

        val results = response.requireResults()
        assertEquals(records.size, results.size)
        assertTrue(results.all { it.indexableRecord != null })
    }

    @Test
    fun testMixedIndexableRecordsResponse() {
        mockServer.enqueue(createSuccessfulResponse("search_box_responses/category/successful_response.json"))

        val response1 = searchEngine.categorySearchBlocking(TEST_CATEGORY)
        val firstRun = response1.requireResults()
        assertEquals(3, firstRun.size)
        assertFalse(firstRun.any { it.indexableRecord != null })
        assertEquals(0, historyDataProvider.getSizeBlocking(callbacksExecutor))

        historyDataProvider.upsertBlocking(
            createHistoryRecord(firstRun.first(), timeProvider.currentTimeMillis()),
            callbacksExecutor,
        )

        mockServer.enqueue(createSuccessfulResponse("search_box_responses/category/successful_response_new_ids.json"))

        val response2 = searchEngine.categorySearchBlocking(TEST_CATEGORY)

        val secondRun = response2.requireResults()
        assertEquals(3, secondRun.size)

        val firstResult = secondRun.first()

        /**
         * Despite the changed ID, core should match and merge server result with local indexable record result
         */
        assertTrue(secondRun[0].indexableRecord != null)
        assertNotEquals(firstRun[0].id, firstResult.base.rawSearchResult.id)
        assertNotEquals(
            firstResult.base.rawSearchResult.id,
            firstResult.base.rawSearchResult.userRecordId
        )

        val historyRecord = historyDataProvider.getBlocking(secondRun[0].id)
        assertEquals(historyRecord, secondRun[0].indexableRecord)

        assertTrue(compareSearchResultWithServerSearchResult(secondRun[1], firstRun[1]))
        assertTrue(compareSearchResultWithServerSearchResult(secondRun[2], firstRun[2]))
    }

    @Test
    fun testIgnoredIndexableRecordsResponse() {
        mockServer.enqueue(createSuccessfulResponse("search_box_responses/category/successful_response.json"))

        val response1 = searchEngine.categorySearchBlocking(TEST_CATEGORY)
        val (results1, responseInfo1) = response1.requireResultPair()

        assertEquals(3, results1.size)
        assertFalse(results1.any { it.indexableRecord != null })
        assertEquals(0, historyDataProvider.getSizeBlocking(callbacksExecutor))
        assertNotNull(responseInfo1.coreSearchResponse)

        historyDataProvider.upsertBlocking(
            createHistoryRecord(results1.first(), timeProvider.currentTimeMillis()),
            callbacksExecutor,
        )

        mockServer.enqueue(createSuccessfulResponse("search_box_responses/category/successful_response_new_ids.json"))

        val response2 = searchEngine.categorySearchBlocking(
            TEST_CATEGORY,
            CategorySearchOptions(ignoreIndexableRecords = true)
        )
        val (results2, responseInfo2) = response2.requireResultPair()

        assertEquals(3, results2.size)
        assertFalse(results2.any { it.indexableRecord != null })
        assertNotNull(responseInfo2.coreSearchResponse)
    }

    @Test
    fun testIndexableRecordsZeroThreshold() {
        mockServer.enqueue(createSuccessfulResponse("search_box_responses/category/successful_response.json"))

        val recordCoordinate = Point.fromLngLat(2.295135021209717, 48.859291076660156)
        val record = createTestHistoryRecord(
            id = "id1",
            name = TEST_CATEGORY,
            coordinate = recordCoordinate,
            categories = listOf(TEST_CATEGORY),
        )
        historyDataProvider.upsertBlocking(record, callbacksExecutor)

        val response = searchEngine.categorySearchBlocking(
            TEST_CATEGORY,
            CategorySearchOptions(
                proximity = recordCoordinate,
                origin = recordCoordinate,
                indexableRecordsDistanceThresholdMeters = 0.0
            ),
        )

        assertEquals(record, response.requireResults().first().indexableRecord)
    }

    @Test
    fun testIndexableRecordsInsideThreshold() {
        mockServer.enqueue(createSuccessfulResponse("search_box_responses/category/successful_response.json"))

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

        val response = searchEngine.categorySearchBlocking(
            TEST_CATEGORY,
            CategorySearchOptions(
                proximity = userLocation,
                origin = userLocation,
                indexableRecordsDistanceThresholdMeters = 500.0
            ),
        )

        assertEquals(record, response.requireResults().first().indexableRecord)
    }

    @Test
    fun testIndexableRecordsOutsideThreshold() {
        mockServer.enqueue(createSuccessfulResponse("search_box_responses/category/successful_response.json"))

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

        val response = searchEngine.categorySearchBlocking(
            TEST_CATEGORY,
            CategorySearchOptions(
                proximity = userLocation,
                origin = userLocation,
                indexableRecordsDistanceThresholdMeters = 15.0
            ),
        )

        assertFalse(response.requireResults().any { it.indexableRecord != null })
    }

    @Test
    fun testSuccessfulIncorrectResponse() {
        mockServer.enqueue(createSuccessfulResponse("search_box_responses/category/successful_incorrect_response.json"))

        try {
            searchEngine.categorySearchBlocking(TEST_CATEGORY, CategorySearchOptions())
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
        mockServer.enqueueMultiple(createSuccessfulResponse("search_box_responses/category/successful_incorrect_response.json"), 2)

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

        val response = searchEngine.categorySearchBlocking(TEST_CATEGORY)

        val e = response.requireError()
        assertTrue(e is SearchRequestException && e.code == 404)
    }

    @Test
    fun testNetworkError() {
        mockServer.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))
        val response = searchEngine.categorySearchBlocking(TEST_CATEGORY)
        assertTrue(response.requireError() is IOException)
    }

    @Test
    fun testBrokenResponseContent() {
        mockServer.enqueue(MockResponse().setResponseCode(200).setBody("I'm broken"))
        val response = searchEngine.categorySearchBlocking(TEST_CATEGORY)
        assertTrue(response.isError)
    }

    @Test
    fun testCheckAsyncOperationTaskCompletion() {
        mockServer.enqueue(createSuccessfulResponse("search_box_responses/category/successful_response.json"))

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
            .setBody(readFileFromAssets("search_box_responses/suggestions-error-response-simple-format.json"))

        mockServer.enqueue(errorResponse)

        val response = searchEngine.categorySearchBlocking(TEST_CATEGORY)

        val error = response.requireError()

        assertEquals(SearchRequestException("Wrong arguments", 422), error)
    }

    @Test
    fun testErrorBackendResponseExtendedFormat() {
        val errorResponse = MockResponse()
            .setResponseCode(400)
            .setBody(readFileFromAssets("search_box_responses/suggestions-error-response-extended-format.json"))

        mockServer.enqueue(errorResponse)

        val response = searchEngine.categorySearchBlocking(TEST_CATEGORY)
        val error = response.requireError()

        assertEquals(
            SearchRequestException(
                readFileFromAssets("search_box_responses/suggestions-error-response-extended-format.json"),
                400
            ),
            error
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
