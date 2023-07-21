package com.mapbox.search

import com.mapbox.geojson.Point
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
import com.mapbox.search.common.metadata.OpenHours
import com.mapbox.search.common.metadata.OpenPeriod
import com.mapbox.search.common.metadata.WeekDay
import com.mapbox.search.common.metadata.WeekTimestamp
import com.mapbox.search.common.tests.FixedPointLocationEngine
import com.mapbox.search.record.FavoritesDataProvider
import com.mapbox.search.record.HistoryDataProvider
import com.mapbox.search.result.SearchAddress
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchResultType
import com.mapbox.search.tests_support.BlockingSearchCallback
import com.mapbox.search.tests_support.EmptySearchCallback
import com.mapbox.search.tests_support.compareSearchResultWithServerSearchResult
import com.mapbox.search.tests_support.createHistoryRecord
import com.mapbox.search.tests_support.createSearchEngineWithBuiltInDataProvidersBlocking
import com.mapbox.search.tests_support.record.clearBlocking
import com.mapbox.search.tests_support.record.getSizeBlocking
import com.mapbox.search.tests_support.record.upsertBlocking
import com.mapbox.search.tests_support.reverseBlocking
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

        val options = ReverseGeoOptions(
            center = TEST_POINT,
            countries = listOf(IsoCountryCode.UNITED_STATES, IsoCountryCode.BELARUS),
            languages = listOf(IsoLanguageCode.ENGLISH),
            limit = 5,
            types = listOf(QueryType.ADDRESS, QueryType.POI)
        )

        searchEngine.reverseBlocking(options)

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

        val response = searchEngine.reverseBlocking(TEST_POINT)

        val (results, responseInfo) = response.requireResultPair()
        assertEquals(3, results.size)

        val searchResult = results.first()
        assertEquals("test-id", searchResult.id)
        assertEquals("Eiffel Tower", searchResult.name)
        assertEquals("75007 Paris, France", searchResult.descriptionText)
        assertEquals(
            SearchAddress(
                // TODO FIXME incorrect parsing
                houseNumber = "Hamp",
                street = "av. Anatole France",
                neighborhood = "Gros-Caillou",
                locality = "7th arrondissement of Paris",
                postcode = "75007",
                place = "Paris",
                district = null,
                region = null,
                country = "France",
            ),
            searchResult.address
        )
        assertEquals("5 Avenue Anatole France, 75007 Paris, France", searchResult.fullAddress)
        assertEquals(Point.fromLngLat(2.294481, 48.85837), searchResult.coordinate)
        assertEquals(
            listOf(
                RoutablePoint(
                    Point.fromLngLat(2.2944810097939197, 48.85836931442563),
                    "default"
                )
            ),
            searchResult.routablePoints
        )
        assertEquals(
            listOf(
                "historic site",
                "tourist attraction",
                "monument"
            ), searchResult.categories
        )
        assertEquals("marker", searchResult.makiIcon)
        // TODO FIXME Search Native should parse accuracy
        assertEquals(null, searchResult.accuracy)
        assertEquals(listOf(SearchResultType.POI), searchResult.types)
        assertEquals(null, searchResult.etaMinutes)
        assertEquals(
            SearchResultMetadata(
                // TODO FIXME parse "photos"
                metadata = hashMapOf("photos" to "[{\"width\":50,\"height\":50,\"url\":\"https://test.com/img1.jpg\"},{\"width\":150,\"height\":150,\"url\":\"https://test.com/img2.jpg\"}]"),
                reviewCount = 141783,
                phone = "+33 123 45 67 89",
                website = "https://www.toureiffel.paris/",
                averageRating = 5.0,
                description = "Famous symbol of France",
                primaryPhotos = null,
                otherPhotos = null,
                openHours = OpenHours.Scheduled(
                    periods = listOf(
                        OpenPeriod(
                            open = WeekTimestamp(WeekDay.MONDAY, 9, 0),
                            closed = WeekTimestamp(WeekDay.MONDAY, 23, 45)
                        )
                    )
                ),
                parking = null,
                cpsJson = null,
            ),
            searchResult.metadata
        )
        assertEquals(mapOf("tripadvisor" to "23789983"), searchResult.externalIDs)
        assertEquals(0, searchResult.serverIndex)
        assertEquals(null, searchResult.indexableRecord)

        assertNotNull(responseInfo.coreSearchResponse)
    }

    @Test
    fun testSuccessfulEmptyResponse() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/reverse_geocoding/successful_empty_response.json"))

        val response = searchEngine.reverseBlocking(TEST_POINT)
        val (results, responseInfo) = response.requireResultPair()
        assertTrue(results.isEmpty())
        assertNotNull(responseInfo.coreSearchResponse)
    }

    @Test
    fun testReverseGeocodingDoesNotReturnIndexableRecords() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/reverse_geocoding/successful_response.json"))

        val response1 = searchEngine.reverseBlocking(TEST_POINT)
        val (results1, responseInfo1) = response1.requireResultPair()

        assertEquals(3, results1.size)
        assertFalse(results1.any { it.indexableRecord != null })
        assertNotNull(responseInfo1.coreSearchResponse)

        val searchResult = results1.first()

        historyDataProvider.upsertBlocking(
            createHistoryRecord(searchResult, timeProvider.currentTimeMillis()),
            callbacksExecutor,
        )

        assertEquals(1, historyDataProvider.getSizeBlocking(callbacksExecutor))

        mockServer.enqueue(createSuccessfulResponse("sbs_responses/reverse_geocoding/successful_response.json"))
        val response2 = searchEngine.reverseBlocking(TEST_POINT)
        val (results2, responseInfo2) = response2.requireResultPair()

        assertFalse(results2.any { it.indexableRecord != null })
        assertEquals(results1.size, results2.size)
        results1.indices.forEach { index ->
            assertTrue(
                compareSearchResultWithServerSearchResult(results1[index], results2[index])
            )
        }
        assertNotNull(responseInfo2.coreSearchResponse)
    }

    @Test
    fun testSuccessfulIncorrectResponse() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/reverse_geocoding/successful_incorrect_response.json"))

        try {
            searchEngine.reverseBlocking(TEST_POINT)
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

        val response = searchEngine.reverseBlocking(TEST_POINT)
        val error = response.requireError()
        assertTrue(error is SearchRequestException && error.code == 404)
    }

    @Test
    fun testNetworkError() {
        mockServer.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))

        val response = searchEngine.reverseBlocking(TEST_POINT)
        val error = response.requireError()
        assertTrue(error is IOException)
    }

    @Test
    fun testBrokenResponseContent() {
        mockServer.enqueue(MockResponse().setResponseCode(200).setBody("I'm broken"))

        val response = searchEngine.reverseBlocking(TEST_POINT)
        assertTrue(response.isError)
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

        val response = searchEngine.reverseBlocking(TEST_POINT)
        val error = response.requireError()

        assertEquals(SearchRequestException("Wrong arguments", 422), error)
    }

    @Test
    fun testErrorBackendResponseExtendedFormat() {
        val errorResponse = MockResponse()
            .setResponseCode(400)
            .setBody(readFileFromAssets("sbs_responses/suggestions-error-response-extended-format.json"))

        mockServer.enqueue(errorResponse)

        val response = searchEngine.reverseBlocking(TEST_POINT)
        val error = response.requireError()

        assertEquals(
            SearchRequestException(
                readFileFromAssets("sbs_responses/suggestions-error-response-extended-format.json"),
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

        val TEST_USER_LOCATION: Point = Point.fromLngLat(10.1, 11.1234567)
        val TEST_POINT: Point = Point.fromLngLat(2.2946, 48.85836)

        const val TEST_LOCAL_TIME_MILLIS = 12345L
        val TEST_KEYBOARD_LOCALE: Locale = Locale.ENGLISH
        val TEST_ORIENTATION = ScreenOrientation.PORTRAIT
    }
}
