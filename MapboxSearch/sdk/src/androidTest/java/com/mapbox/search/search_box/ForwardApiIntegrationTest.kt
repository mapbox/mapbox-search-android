package com.mapbox.search.search_box

import com.mapbox.common.MapboxOptions
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.search.ApiType
import com.mapbox.search.BaseTest
import com.mapbox.search.EtaType
import com.mapbox.search.ForwardSearchOptions
import com.mapbox.search.MapboxSearchSdk
import com.mapbox.search.QueryType
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchEngineSettings
import com.mapbox.search.SearchNavigationOptions
import com.mapbox.search.common.IsoCountryCode
import com.mapbox.search.common.IsoLanguageCode
import com.mapbox.search.common.NavigationProfile
import com.mapbox.search.common.RoutablePoint
import com.mapbox.search.common.SearchRequestException
import com.mapbox.search.common.tests.FixedPointLocationEngine
import com.mapbox.search.result.NewSearchResultType
import com.mapbox.search.tests_support.BlockingSearchCallback
import com.mapbox.search.utils.assertEqualsIgnoreCase
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Locale

internal class ForwardApiIntegrationTest : BaseTest() {

    private lateinit var mockServer: MockWebServer
    private lateinit var searchEngine: SearchEngine

    @Before
    override fun setUp() {
        super.setUp()
        MapboxOptions.accessToken = TEST_ACCESS_TOKEN

        mockServer = MockWebServer()

        MapboxSearchSdk.initialize(
            application = targetApplication,
        )

        val searchEngineSettings = SearchEngineSettings(
            locationProvider = FixedPointLocationEngine(TEST_USER_LOCATION),
            baseUrl = mockServer.url("").toString()
        )

        searchEngine = SearchEngine.createSearchEngine(
            apiType = ApiType.SEARCH_BOX,
            settings = searchEngineSettings,
        )
    }

    @Test
    fun testRequestParametersAreCorrect() {
        mockServer.enqueue(MockResponse().setResponseCode(500))

        val options = ForwardSearchOptions.Builder()
            .language(IsoLanguageCode.FRENCH)
            .limit(5)
            .proximity(Point.fromLngLat(1.23, 2.34))
            .boundingBox(
                BoundingBox.fromPoints(
                    Point.fromLngLat(10.0, 15.0),
                    Point.fromLngLat(30.0, 50.0),
                ),
            )
            .countries(IsoCountryCode.FRANCE, IsoCountryCode.SPAIN)
            .types(QueryType.PLACE, QueryType.STREET)
            .navigationOptions(
                SearchNavigationOptions(
                    NavigationProfile.CYCLING,
                    EtaType.NAVIGATION
                )
            )
            .origin(Point.fromLngLat(3.45, 4.56))
            .build()

        val callback = BlockingSearchCallback()
        searchEngine.forward(TEST_QUERY, options, callback)

        val request = mockServer.takeRequest()
        assertEqualsIgnoreCase("get", request.method!!)

        val url = request.requestUrl!!
        assertEqualsIgnoreCase("//search/searchbox/v1/forward", url.encodedPath)
        assertEquals(TEST_ACCESS_TOKEN, url.queryParameter("access_token"))
        assertEquals(TEST_QUERY, url.queryParameter("q"))
        assertEquals(options.language?.code, url.queryParameter("language"))
        assertEquals(options.limit?.toString(), url.queryParameter("limit"))
        assertEquals(formatPoints(options.proximity), url.queryParameter("proximity"))
        assertEquals(
            formatPoints(options.boundingBox?.southwest(), options.boundingBox?.northeast()),
            url.queryParameter("bbox")
        )
        assertEquals(
            options.countries?.joinToString(separator = ",") { it.code },
            url.queryParameter("country"),
        )
        assertEquals(
            options.types?.joinToString(separator = ",") { it.name.lowercase(Locale.getDefault()) },
            url.queryParameter("types")
        )
        assertEquals(
            options.navigationOptions?.navigationProfile?.rawName!!,
            url.queryParameter("navigation_profile"),
        )
        assertEquals(
            options.navigationOptions?.etaType?.rawName!!,
            url.queryParameter("eta_type"),
        )
        assertEquals(formatPoints(options.origin), url.queryParameter("origin"))
    }

    @Test
    fun testSuccessfulResponse() {
        mockServer.enqueue(
            createSuccessfulResponse(
                "search_box_responses/forward_api/response_successful.json",
            ),
        )

        val callback = BlockingSearchCallback()
        searchEngine.forward(TEST_QUERY, TEST_OPTIONS, callback)

        val requestResult = callback.getResultBlocking()
        assertTrue(requestResult.isResults)

        val results = requestResult.requireResults()
        assertEquals(3, results.size)

        val first = results.first()
        assertEquals("Starbucks", first.name)
        assertEquals(listOf(NewSearchResultType.POI), first.newTypes)
        assertEquals(
            "1429 P St NW, Washington, District of Columbia 20005, United States",
            first.fullAddress,
        )
        assertEquals(Point.fromLngLat(-77.0330038, 38.9098802), first.coordinate)
        assertEquals(
            listOf(
                RoutablePoint(
                    Point.fromLngLat(-77.03309, 38.909659), "POI"
                )
            ),
            first.routablePoints,
        )
    }

    @Test
    fun testErrorResponse() {
        mockServer.enqueue(MockResponse().setResponseCode(404))

        val callback = BlockingSearchCallback()
        searchEngine.forward(TEST_QUERY, TEST_OPTIONS, callback)

        val res = callback.getResultBlocking()
        assertTrue(
            res is BlockingSearchCallback.SearchEngineResult.Error &&
                    res.e is SearchRequestException && res.e.code == 404
        )
    }

    private companion object {
        const val TEST_QUERY = "test query"
        const val TEST_ACCESS_TOKEN = "pk.test"
        val TEST_USER_LOCATION: Point = Point.fromLngLat(10.1, 11.1234567)
        val TEST_OPTIONS = ForwardSearchOptions.Builder().build()
    }
}
