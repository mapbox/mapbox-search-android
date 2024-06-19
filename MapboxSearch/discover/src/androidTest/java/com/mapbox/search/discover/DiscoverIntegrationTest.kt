package com.mapbox.search.discover

import android.app.Application
import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mapbox.common.MapboxOptions
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.search.base.SearchRequestContextProvider
import com.mapbox.search.base.core.CoreEngineOptions
import com.mapbox.search.base.core.CoreSearchEngine
import com.mapbox.search.base.core.getUserActivityReporter
import com.mapbox.search.base.location.LocationEngineAdapter
import com.mapbox.search.base.location.WrapperLocationProvider
import com.mapbox.search.base.location.defaultLocationProvider
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
        MapboxOptions.accessToken = TEST_ACCESS_TOKEN

        mockServer = MockWebServer()

        val engine = createDiscoverApiSearchEngine(
            app = APP,
            url = mockServer.url("").toString()
        )

        discover = DiscoverImpl(
            engine = engine,
            activityReporter = getUserActivityReporter()
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
        assertEqualsIgnoreCase("//search/v1/category/${query.canonicalName}", url.encodedPath)
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
        assertEqualsIgnoreCase("//search/v1/category/${query.canonicalName}", url.encodedPath)
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
        assertEqualsIgnoreCase("post", request.method)

        val url = requireNotNull(request.requestUrl)
        assertEqualsIgnoreCase("//search/v1/category/${query.canonicalName}", url.encodedPath)
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
        assertTrue(request.body.size > 0)
        // Route encoded as polyline6 format, it's tricky to decode it manually and test.
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
        assertEquals("Starbucks", first.name)

        assertEquals(Point.fromLngLat(2.2966029609152523, 48.85993304489138), first.coordinate)
        assertEquals(
            listOf("restaurant", "food", "food and drink", "coffee shop", "coffee", "cafe"),
            first.categories
        )
        assertEquals(
            listOf(RoutablePoint(Point.fromLngLat(2.2966029609152523, 48.85993304489138), "Address")),
            first.routablePoints
        )
        assertEquals("restaurant", first.makiIcon)

        assertEquals(
            DiscoverAddress(
                houseNumber = "26",
                street = "26 Av. De l'Opéra",
                neighborhood = "Paris",
                locality = null,
                postcode = "75001",
                place = "Paris",
                district = null,
                region = "Île-de-France",
                country = "France",
                formattedAddress = "26 Av. de l'Opéra, 75001 Paris, France",
                countryIso1 = "fra",
                countryIso2 = "fr"
            ),
            first.address
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
        val errorResponse = MockResponse()
            .setResponseCode(400)
            .setBody(readFileFromAssets("error-response.json"))

        mockServer.enqueue(errorResponse)

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
    fun testSuccessfulResponseToCallbacks() {
        mockServer.enqueue(createSuccessfulResponse("successful_response.json"))

        val response = discover.searchBlocking(
            DiscoverQuery.Category.COFFEE_SHOP_CAFE,
            Point.fromLngLat(2.2966029609152523, 48.85993304489138)
        )

        assertTrue(response.isResult)

        val results = response.requireResult()
        assertEquals(2, results.size)

        val first = results.first()
        assertEquals("Starbucks", first.name)

        assertEquals(Point.fromLngLat(2.2966029609152523, 48.85993304489138), first.coordinate)
        assertEquals(
            listOf("restaurant", "food", "food and drink", "coffee shop", "coffee", "cafe"),
            first.categories
        )
        assertEquals(
            listOf(RoutablePoint(Point.fromLngLat(2.2966029609152523, 48.85993304489138), "Address")),
            first.routablePoints
        )
        assertEquals("restaurant", first.makiIcon)

        assertEquals(
            DiscoverAddress(
                houseNumber = "26",
                street = "26 Av. De l'Opéra",
                neighborhood = "Paris",
                locality = null,
                postcode = "75001",
                place = "Paris",
                district = null,
                region = "Île-de-France",
                country = "France",
                formattedAddress = "26 Av. de l'Opéra, 75001 Paris, France",
                countryIso1 = "fra",
                countryIso2 = "fr"
            ),
            first.address
        )
    }

    @Test
    fun testErrorResponseToCallbacks() {
        val errorResponse = MockResponse()
            .setResponseCode(400)
            .setBody(readFileFromAssets("error-response.json"))

        mockServer.enqueue(errorResponse)

        val response = discover.searchBlocking(
            DiscoverQuery.Category.COFFEE_SHOP_CAFE,
            Point.fromLngLat(2.2966029609152523, 48.85993304489138)
        )

        val error = SearchRequestException(
            readFileFromAssets("error-response.json"),
            400
        )

        assertTrue(response.isError)
        assertEquals(error, response.requireError())
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
            url: String
        ): DiscoverSearchEngine {
            val coreEngine = CoreSearchEngine(
                CoreEngineOptions(
                    baseUrl = url,
                    apiType = ApiType.SBS,
                    sdkInformation = UserAgentProvider.sdkInformation(),
                    eventsUrl = null,
                ),
                WrapperLocationProvider(
                    LocationEngineAdapter(app, defaultLocationProvider()), null
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
            return MockResponse()
                .setResponseCode(200)
                .setBody(readFileFromAssets(bodyContentPath))
        }
    }
}
