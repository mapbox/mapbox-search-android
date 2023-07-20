package com.mapbox.search.discover

import android.app.Application
import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.search.base.SearchRequestContextProvider
import com.mapbox.search.base.core.CoreEngineOptions
import com.mapbox.search.base.core.CoreSearchEngine
import com.mapbox.search.base.core.getUserActivityReporter
import com.mapbox.search.base.location.LocationEngineAdapter
import com.mapbox.search.base.location.WrapperLocationProvider
import com.mapbox.search.base.location.defaultLocationEngine
import com.mapbox.search.base.record.IndexableRecordResolver
import com.mapbox.search.base.result.SearchResultFactory
import com.mapbox.search.base.utils.UserAgentProvider
import com.mapbox.search.common.IsoLanguageCode
import com.mapbox.search.common.RoutablePoint
import com.mapbox.search.common.SearchRequestException
import com.mapbox.search.common.concurrent.SearchSdkMainThreadWorker
import com.mapbox.search.internal.bindgen.ApiType
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
internal class DiscoverIntegrationTest {

    private lateinit var mockServer: MockWebServer
    private lateinit var discover: Discover

    @Before
    fun setUp() {
        mockServer = MockWebServer()

        val engine = createDiscoverApiSearchEngine(
            app = APP,
            token = TEST_ACCESS_TOKEN,
            url = mockServer.url("").toString()
        )

        discover = DiscoverImpl(
            engine = engine,
            activityReporter = getUserActivityReporter(TEST_ACCESS_TOKEN)
        )
    }

    @Test
    fun testRequestParametersForNearbySearch() {
        mockServer.enqueue(MockResponse().setResponseCode(500))

        val query = DiscoverQuery.Category.COFFEE_SHOP_CAFE
        val proximity = Point.fromLngLat(2.2966029609152523, 48.85993304489138)
        val options = DiscoverOptions(
            limit = 7,
            language = IsoLanguageCode.FRENCH
        )

        runBlocking {
            discover.search(query, proximity, options)
        }

        val request = mockServer.takeRequest()
        assertEqualsIgnoreCase("get", request.method)

        val url = requireNotNull(request.requestUrl)
        assertEqualsIgnoreCase(
            "//search/searchbox/v1/category/${query.canonicalName}",
            url.encodedPath
        )
        assertEquals(TEST_ACCESS_TOKEN, url.queryParameter("access_token"))
        assertEquals(options.language.code, url.queryParameter("language"))
        assertEquals(options.limit.toString(), url.queryParameter("limit"))
        assertEquals(formatPoints(proximity), url.queryParameter("proximity"))
    }

    @Test
    fun testRequestParametersForSearchInRegion() {
        mockServer.enqueue(MockResponse().setResponseCode(500))

        val query = DiscoverQuery.Category.ATM
        val proximity = Point.fromLngLat(14.421576441071238, 50.087300021978024)
        val region = BoundingBox.fromPoints(
            Point.fromLngLat(14.414441059376246, 50.08443383305258),
            Point.fromLngLat(14.432670012143467, 50.0930762433351)
        )
        val options = DiscoverOptions(
            limit = 5,
            language = IsoLanguageCode.CZECH
        )

        runBlocking {
            discover.search(query, region, proximity, options)
        }

        val request = mockServer.takeRequest()
        assertEqualsIgnoreCase("get", request.method)

        val url = requireNotNull(request.requestUrl)
        assertEqualsIgnoreCase(
            "//search/searchbox/v1/category/${query.canonicalName}",
            url.encodedPath
        )
        assertEquals(TEST_ACCESS_TOKEN, url.queryParameter("access_token"))
        assertEquals(options.language.code, url.queryParameter("language"))
        assertEquals(options.limit.toString(), url.queryParameter("limit"))
        assertEquals(formatPoints(proximity), url.queryParameter("proximity"))
        assertEquals(
            formatPoints(region.southwest(), region.northeast()),
            url.queryParameter("bbox")
        )
    }

    @Test
    fun testRequestParametersForSearchAlongTheRoute() {
        mockServer.enqueue(MockResponse().setResponseCode(500))

        val query = DiscoverQuery.Category.GAS_STATION

        val route = listOf(
            Point.fromLngLat(13.460232269954309, 52.529542975333825),
            Point.fromLngLat(13.383377596541125, 52.51672104042968),
            Point.fromLngLat(13.288769055122525, 52.510451128330985)
        )

        val deviation = RouteDeviationOptions.Time(15, TimeUnit.MINUTES)

        val options = DiscoverOptions(
            limit = 10,
            language = IsoLanguageCode.GERMAN
        )

        runBlocking {
            discover.search(query, route, deviation, options)
        }

        val request = mockServer.takeRequest()
        assertEqualsIgnoreCase("get", request.method)

        val url = requireNotNull(request.requestUrl)
        assertEqualsIgnoreCase(
            "//search/searchbox/v1/category/${query.canonicalName}",
            url.encodedPath
        )
        assertEquals(TEST_ACCESS_TOKEN, url.queryParameter("access_token"))
        assertEquals(options.language.code, url.queryParameter("language"))
        assertEquals(options.limit.toString(), url.queryParameter("limit"))
        assertEquals(
            deviation.timeDeviationMinutes.formatToBackendConvention(),
            url.queryParameter("time_deviation")
        )
        assertEquals(
            "isochrone",
            url.queryParameter("sar_type")
        )

        // Route encoded as polyline6 format, it's tricky to decode it manually and test.
        assertFalse(url.queryParameter("route").isNullOrEmpty())
        assertEquals("polyline6", url.queryParameter("route_geometry"))
    }

    @Test
    fun testSuccessfulResponse() {
        mockServer.enqueue(createSuccessfulResponse("successful_response.json"))

        val response = runBlocking {
            discover.search(
                DiscoverQuery.Category.COFFEE_SHOP_CAFE,
                Point.fromLngLat(2.2966029609152523, 48.85993304489138)
            )
        }

        assertTrue(response.isValue)

        val results = requireNotNull(response.value)
        assertEquals(2, results.size)

        val first = results.first()
        assertEquals("fruit stand", first.name)

        assertEquals(Point.fromLngLat(2.2930926, 48.858407), first.coordinate)
        assertEquals(
            listOf("cafe", "food", "food and drink"),
            first.categories
        )
        assertEquals(
            listOf(
                RoutablePoint(
                    Point.fromLngLat(2.292922543158576, 48.85865467984879),
                    "default"
                )
            ),
            first.routablePoints
        )
        assertEquals("restaurant", first.makiIcon)

        assertEquals(
            DiscoverAddress(
                houseNumber = null,
                street = "Paris",
                neighborhood = "Gros-Caillou",
                locality = "7th arrondissement of Paris",
                postcode = "75007",
                place = "Paris",
                district = null,
                region = null,
                country = "France",
                formattedAddress = "75007 Paris, France",
                // TODO FIXME address
//                countryIso1 = "fra",
//                countryIso2 = "fr"
                countryIso1 = null,
                countryIso2 = null
            ),
            first.address
        )

        val second = results[1]
        assertEquals("Le Champ de Mars", second.name)

        assertEquals(Point.fromLngLat(2.2999417, 48.8579342), second.coordinate)
        assertEquals(
            listOf("cafe"),
            second.categories
        )
        assertEquals(
            listOf(
                RoutablePoint(
                    Point.fromLngLat(2.300095, 48.857935),
                    "default"
                )
            ),
            second.routablePoints
        )
        assertEquals("restaurant", second.makiIcon)

        assertEquals(
            DiscoverAddress(
                houseNumber = null,
                street = "Paris",
                neighborhood = "Gros-Caillou",
                locality = "7th arrondissement of Paris",
                postcode = "75007",
                place = "Paris",
                district = null,
                region = null,
                country = "France",
                formattedAddress = "75007 Paris, France",
                // TODO FIXME address
//                countryIso1 = "fra",
//                countryIso2 = "fr"
                countryIso1 = null,
                countryIso2 = null
            ),
            second.address
        )
    }

    @Test
    fun testSuccessfulEmptyResponse() {
        mockServer.enqueue(createSuccessfulResponse("successful_empty_response.json"))

        val response = runBlocking {
            discover.search(
                DiscoverQuery.Category.COFFEE_SHOP_CAFE,
                Point.fromLngLat(2.2966029609152523, 48.85993304489138)
            )
        }

        assertTrue(response.isValue)
        assertEquals(0, requireNotNull(response.value).size)
    }

    @Test
    fun testErrorResponse() {
        mockServer.enqueue(createResponse(400, "error-response.json"))

        val response = runBlocking {
            discover.search(
                DiscoverQuery.Category.COFFEE_SHOP_CAFE,
                Point.fromLngLat(2.2966029609152523, 48.85993304489138)
            )
        }

        val error = SearchRequestException(
            readFileFromAssets("error-response.json"),
            400
        )

        assertTrue(response.isError)
        assertEquals(error, response.error)
    }

    @Test
    fun testCallbacksAndCoroutinesResponsesTheSame() {
        mockServer.enqueue(createSuccessfulResponse("successful_response.json"))
        val callbackResponse = discover.searchBlocking(
            DiscoverQuery.Category.COFFEE_SHOP_CAFE,
            Point.fromLngLat(2.2966029609152523, 48.85993304489138)
        )
        assertTrue(callbackResponse.isResult)

        mockServer.enqueue(createSuccessfulResponse("successful_response.json"))
        val coroutineResponse = runBlocking {
            discover.search(
                DiscoverQuery.Category.COFFEE_SHOP_CAFE,
                Point.fromLngLat(2.2966029609152523, 48.85993304489138)
            )
        }
        assertTrue(coroutineResponse.isValue)

        assertEquals(2, coroutineResponse.value?.size)
        assertEquals(callbackResponse.requireResult(), coroutineResponse.value)
    }

    @Test
    fun testErrorCallbacksAndCoroutinesResponsesTheSame() {
        mockServer.enqueue(createResponse(400, "error-response.json"))

        val callbackResponse = discover.searchBlocking(
            DiscoverQuery.Category.COFFEE_SHOP_CAFE,
            Point.fromLngLat(2.2966029609152523, 48.85993304489138)
        )
        assertTrue(callbackResponse.isError)

        mockServer.enqueue(createResponse(400, "error-response.json"))
        val coroutineResponse = runBlocking {
            discover.search(
                DiscoverQuery.Category.COFFEE_SHOP_CAFE,
                Point.fromLngLat(2.2966029609152523, 48.85993304489138)
            )
        }
        assertTrue(coroutineResponse.isError)

        assertEquals(callbackResponse.requireError(), coroutineResponse.error)
    }

    private companion object {

        val CONTEXT: Context
            get() = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext

        val APP: Application
            get() = CONTEXT as Application

        const val TEST_ACCESS_TOKEN = "pk.test"

        fun assertEqualsIgnoreCase(expected: String?, actual: String?) {
            assertEquals(
                expected?.lowercase(Locale.getDefault()),
                actual?.lowercase(Locale.getDefault())
            )
        }

        fun createDiscoverApiSearchEngine(
            app: Application,
            token: String,
            url: String
        ): DiscoverSearchEngine {
            val coreEngine = CoreSearchEngine(
                CoreEngineOptions(
                    token,
                    url,
                    ApiType.SEARCH_BOX,
                    UserAgentProvider.userAgent,
                    null
                ),
                WrapperLocationProvider(
                    LocationEngineAdapter(app, defaultLocationEngine()), null
                ),
            )

            return DiscoverSearchEngine(
                coreEngine = coreEngine,
                requestContextProvider = SearchRequestContextProvider(app),
                searchResultFactory = SearchResultFactory(IndexableRecordResolver.EMPTY),
            )
        }

        fun Discover.searchBlocking(
            query: DiscoverQuery,
            proximity: Point,
            options: DiscoverOptions = DiscoverOptions(),
            executor: Executor = SearchSdkMainThreadWorker.mainExecutor
        ): BlockingCompletionCallback.CompletionCallbackResult<List<DiscoverResult>> {
            val callback = BlockingCompletionCallback<List<DiscoverResult>>()
            search(query, proximity, options, executor, callback)
            return callback.getResultBlocking()
        }

        fun Double.format(digits: Int) = "%.${digits}f".format(Locale.ENGLISH, this)

        fun Double.formatToBackendConvention() = format(6)

        fun formatPoints(vararg points: Point?): String {
            return points
                .flatMap { listOfNotNull(it?.longitude(), it?.latitude()) }
                .joinToString(separator = ",") { it.formatToBackendConvention() }
        }

        fun readBytesFromAssets(fileName: String): ByteArray {
            return CONTEXT.resources.assets.open(fileName).use {
                it.readBytes()
            }
        }

        fun readFileFromAssets(fileName: String): String = String(readBytesFromAssets(fileName))

        fun createSuccessfulResponse(bodyContentPath: String): MockResponse {
            return createResponse(200, bodyContentPath)
        }

        fun createResponse(code: Int, bodyPath: String): MockResponse {
            return MockResponse()
                .setResponseCode(code)
                .setBody(readFileFromAssets(bodyPath))
        }
    }
}
