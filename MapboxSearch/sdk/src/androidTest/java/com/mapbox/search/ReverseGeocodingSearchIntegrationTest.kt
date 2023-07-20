package com.mapbox.search

import com.mapbox.geojson.Point
import com.mapbox.search.base.core.CoreApiType
import com.mapbox.search.base.result.BaseRawResultType
import com.mapbox.search.base.result.SearchRequestContext
import com.mapbox.search.base.utils.KeyboardLocaleProvider
import com.mapbox.search.base.utils.TimeProvider
import com.mapbox.search.base.utils.orientation.ScreenOrientation
import com.mapbox.search.base.utils.orientation.ScreenOrientationProvider
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.common.IsoCountryCode
import com.mapbox.search.common.IsoLanguageCode
import com.mapbox.search.common.RoutablePoint
import com.mapbox.search.common.SearchRequestException
import com.mapbox.search.common.concurrent.SearchSdkMainThreadWorker
import com.mapbox.search.common.metadata.ImageInfo
import com.mapbox.search.common.metadata.OpenHours
import com.mapbox.search.common.metadata.OpenPeriod
import com.mapbox.search.common.metadata.ParkingData
import com.mapbox.search.common.metadata.WeekDay
import com.mapbox.search.common.metadata.WeekTimestamp
import com.mapbox.search.common.tests.FixedPointLocationEngine
import com.mapbox.search.record.FavoritesDataProvider
import com.mapbox.search.record.HistoryDataProvider
import com.mapbox.search.result.ResultAccuracy
import com.mapbox.search.result.SearchAddress
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchResultType
import com.mapbox.search.tests_support.BlockingSearchCallback
import com.mapbox.search.tests_support.EmptySearchCallback
import com.mapbox.search.tests_support.compareSearchResultWithServerSearchResult
import com.mapbox.search.tests_support.createHistoryRecord
import com.mapbox.search.tests_support.createSearchEngineWithBuiltInDataProvidersBlocking
import com.mapbox.search.tests_support.createTestBaseRawSearchResult
import com.mapbox.search.tests_support.createTestServerSearchResult
import com.mapbox.search.tests_support.record.clearBlocking
import com.mapbox.search.tests_support.record.getSizeBlocking
import com.mapbox.search.tests_support.record.upsertBlocking
import com.mapbox.search.utils.assertEqualsIgnoreCase
import com.mapbox.search.utils.enqueueMultiple
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor

/**
 * Contains only reverse-geocoding related functionality tests.
 * See [CategorySearchIntegrationTest], [SearchEngineIntegrationTest] for more tests.
 */
internal class ReverseGeocodingSearchIntegrationTest : BaseTest() {

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

        val options = ReverseGeoOptions(
            center = TEST_POINT,
            countries = listOf(IsoCountryCode.UNITED_STATES, IsoCountryCode.BELARUS),
            languages = listOf(IsoLanguageCode.ENGLISH),
            limit = 5,
            types = listOf(QueryType.ADDRESS, QueryType.POI)
        )

        val callback = BlockingSearchCallback()
        searchEngine.search(options, callback)

        val request = mockServer.takeRequest()
        assertEqualsIgnoreCase("get", request.method!!)

        val url = request.requestUrl!!
        assertEqualsIgnoreCase("//search/searchbox/v1/reverse", url.encodedPath)
        assertEquals(
            TEST_POINT.longitude().formatToBackendConvention(),
            url.queryParameter("longitude")
        )
        assertEquals(
            TEST_POINT.latitude().formatToBackendConvention(),
            url.queryParameter("latitude")
        )
        assertEquals(IsoLanguageCode.ENGLISH.code, url.queryParameter("language"))
        assertEquals(options.limit.toString(), url.queryParameter("limit"))
        assertEquals(
            options.types?.joinToString(separator = ",") { it.name.lowercase(Locale.getDefault()) },
            url.queryParameter("types")
        )
        assertFalse(request.headers["X-Request-ID"].isNullOrEmpty())
    }

    @Test
    fun testSuccessfulResponse() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/reverse_geocoding/successful_response.json"))

        val callback = BlockingSearchCallback()
        searchEngine.search(ReverseGeoOptions(center = TEST_POINT), callback)

        val res = callback.getResultBlocking()
        assertTrue(res is BlockingSearchCallback.SearchEngineResult.Results)
        res as BlockingSearchCallback.SearchEngineResult.Results
        assertEquals(3, res.results.size)

        val searchResult = res.results.first()

        val rawSearchResult = createTestBaseRawSearchResult(
            id = "p4bWdnYBo8NaDG6XjlSq",
            types = listOf(BaseRawResultType.POI),
            names = listOf("Eiffel Tower"),
            languages = listOf("def"), // should it be "en"?
            categories = listOf("historic site", "tourist attraction", "monument", "viewpoint"),
            addresses = listOf(
                SearchAddress(
                    country = "France",
                    houseNumber = "5",
                    neighborhood = "Gros-Caillou",
                    place = "Paris",
                    postcode = "75007",
                    street = "Avenue Anatole France"
                )
            ),
            fullAddress = "5 Avenue Anatole France, 75007 Paris, France",
            descriptionAddress = "Eiffel Tower, 5 Avenue Anatole France, 75007 Paris, France",
            matchingName = "Eiffel Tower",
            center = Point.fromLngLat(2.294464, 48.858353),
            accuracy = ResultAccuracy.Point,
            routablePoints = listOf(RoutablePoint(point = Point.fromLngLat(2.294464, 48.858353), name = "Address")),
            icon = "marker",
            distanceMeters = 10.009866290988025,
            metadata = SearchResultMetadata(
                metadata = hashMapOf(),
                reviewCount = 140247,
                phone = "+33 (0)8 92 70 12 39",
                website = "https://www.toureiffel.paris/",
                averageRating = 4.5,
                description = "Completed in 1889, this colossal landmark, although initially hated by many Parisians, is now a famous symbol of French civic pride.",
                primaryPhotos = listOf(
                    ImageInfo(
                        url = "http://media-cdn.tripadvisor.com/media/photo-t/1b/15/a3/a1/c-emeric-livinec-sete.jpg",
                        width = 50,
                        height = 50
                    ),
                    ImageInfo(
                        url = "http://media-cdn.tripadvisor.com/media/photo-l/1b/15/a3/a1/c-emeric-livinec-sete.jpg",
                        width = 150,
                        height = 150
                    ),
                    ImageInfo(
                        url = "http://media-cdn.tripadvisor.com/media/photo-o/1b/15/a3/a1/c-emeric-livinec-sete.jpg",
                        width = 3000,
                        height = 2000
                    )
                ),
                otherPhotos = null,
                openHours = OpenHours.Scheduled(
                    periods = listOf(
                        OpenPeriod(
                            open = WeekTimestamp(day = WeekDay.MONDAY, hour = 9, minute = 30),
                            closed = WeekTimestamp(day = WeekDay.TUESDAY, hour = 17, minute = 0)
                        )
                    )
                ),
                parking = ParkingData(
                    totalCapacity = 5,
                    reservedForDisabilities = 3
                ),
                cpsJson = "{\"raw\":{}}"
            ),
            externalIDs = mapOf(
                "tripadvisor" to "188151",
                "foursquare" to "51a2445e5019c80b56934c75",
            )
        )

        val expectedResult = createTestServerSearchResult(
            listOf(SearchResultType.POI),
            rawSearchResult,
            RequestOptions(
                query = formatPoints(TEST_POINT),
                endpoint = "reverse",
                options = SearchOptions(
                    languages = listOf(IsoLanguageCode(Locale.getDefault().language)),
                    proximity = TEST_POINT,
                    origin = TEST_POINT
                ),
                proximityRewritten = false,
                originRewritten = false,
                sessionID = "",
                requestContext = SearchRequestContext(
                    apiType = CoreApiType.SEARCH_BOX,
                    keyboardLocale = TEST_KEYBOARD_LOCALE,
                    screenOrientation = TEST_ORIENTATION,
                    responseUuid = "6b5d7e47-f901-48e9-ab14-9b8319fa07ed"
                )
            )
        )
        assertTrue(compareSearchResultWithServerSearchResult(expectedResult, searchResult))
        assertNotNull(res.responseInfo.coreSearchResponse)
    }

    @Test
    fun testSuccessfulEmptyResponse() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/reverse_geocoding/successful_empty_response.json"))

        val callback = BlockingSearchCallback()
        searchEngine.search(ReverseGeoOptions(center = TEST_POINT), callback)

        val res = callback.getResultBlocking() as BlockingSearchCallback.SearchEngineResult.Results
        assertTrue(res.results.isEmpty())
        assertNotNull(res.responseInfo.coreSearchResponse)
    }

    @Test
    fun testReverseGeocodingDoesNotReturnIndexableRecords() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/reverse_geocoding/successful_response.json"))

        val callback = BlockingSearchCallback()
        searchEngine.search(ReverseGeoOptions(center = TEST_POINT), callback)

        val firstRun = callback.getResultBlocking() as BlockingSearchCallback.SearchEngineResult.Results
        assertEquals(3, firstRun.results.size)
        assertFalse(firstRun.results.any { it.indexableRecord != null })
        assertNotNull(firstRun.responseInfo.coreSearchResponse)

        val searchResult = firstRun.results.first()

        historyDataProvider.upsertBlocking(
            createHistoryRecord(searchResult, timeProvider.currentTimeMillis()),
            callbacksExecutor,
        )

        assertEquals(1, historyDataProvider.getSizeBlocking(callbacksExecutor))

        callback.reset()
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/reverse_geocoding/successful_response.json"))
        searchEngine.search(ReverseGeoOptions(center = TEST_POINT), callback)

        val secondRun = callback.getResultBlocking() as BlockingSearchCallback.SearchEngineResult.Results
        assertFalse(secondRun.results.any { it.indexableRecord != null })
        assertEquals(firstRun.results.size, secondRun.results.size)
        firstRun.results.indices.forEach { index ->
            assertTrue(
                compareSearchResultWithServerSearchResult(firstRun.results[index], secondRun.results[index])
            )
        }
        assertNotNull(secondRun.responseInfo.coreSearchResponse)
    }

    @Test
    fun testSuccessfulIncorrectResponse() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/reverse_geocoding/successful_incorrect_response.json"))

        try {
            val callback = BlockingSearchCallback()
            searchEngine.search(ReverseGeoOptions(center = TEST_POINT), callback)
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

        val callback = BlockingSearchCallback()
        searchEngine.search(ReverseGeoOptions(center = TEST_POINT), callback)

        val res = callback.getResultBlocking()
        assertTrue(res is BlockingSearchCallback.SearchEngineResult.Error && res.e is SearchRequestException && res.e.code == 404)
    }

    @Test
    fun testNetworkError() {
        mockServer.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))

        val callback = BlockingSearchCallback()
        searchEngine.search(ReverseGeoOptions(center = TEST_POINT), callback)

        val res = callback.getResultBlocking()
        assertTrue(res is BlockingSearchCallback.SearchEngineResult.Error && res.e is IOException)
    }

    @Test
    fun testBrokenResponseContent() {
        mockServer.enqueue(MockResponse().setResponseCode(200).setBody("I'm broken"))

        val callback = BlockingSearchCallback()
        searchEngine.search(ReverseGeoOptions(center = TEST_POINT), callback)

        val res = callback.getResultBlocking()
        assertTrue(res is BlockingSearchCallback.SearchEngineResult.Error)
    }

    @Test
    fun testCheckAsyncOperationTaskCompletion() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/reverse_geocoding/successful_response.json"))

        val countDownLatch = CountDownLatch(1)
        var task: AsyncOperationTask? = null

        task = searchEngine.search(ReverseGeoOptions(center = TEST_POINT), object : SearchCallback {
            override fun onResults(results: List<SearchResult>, responseInfo: ResponseInfo) {
                assertTrue(task?.isDone == true)
                countDownLatch.countDown()
            }

            override fun onError(e: Exception) {
                fail("Error happened: $e")
            }
        })

        countDownLatch.await()
    }

    @Test
    fun testConsecutiveRequests() {
        mockServer.enqueueMultiple(createSuccessfulResponse("sbs_responses/reverse_geocoding/successful_response.json"), 2)

        val task1 = searchEngine.search(ReverseGeoOptions(center = TEST_POINT), EmptySearchCallback)

        val callback = BlockingSearchCallback()
        val task2 = searchEngine.search(ReverseGeoOptions(center = TEST_POINT), callback)
        callback.getResultBlocking()

        // Unlike other searches, reverse geocoding doesn't cancel previous requests, so task1 shouldn't be cancelled by this time
        assertFalse(task1.isCancelled)
        assertTrue(task2.isDone)
    }

    @Test
    fun testErrorBackendResponseSimpleFormat() {
        val errorResponse = MockResponse()
            .setResponseCode(422)
            .setBody(readFileFromAssets("sbs_responses/suggestions-error-response-simple-format.json"))

        mockServer.enqueue(errorResponse)

        val callback = BlockingSearchCallback()
        searchEngine.search(ReverseGeoOptions(center = TEST_POINT), callback)

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
        searchEngine.search(ReverseGeoOptions(center = TEST_POINT), callback)

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

        val TEST_USER_LOCATION: Point = Point.fromLngLat(10.1, 11.1234567)
        val TEST_POINT: Point = Point.fromLngLat(2.2946, 48.85836)

        const val TEST_LOCAL_TIME_MILLIS = 12345L
        val TEST_KEYBOARD_LOCALE: Locale = Locale.ENGLISH
        val TEST_ORIENTATION = ScreenOrientation.PORTRAIT
    }
}
