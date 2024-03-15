package com.mapbox.search.autocomplete

import android.app.Application
import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.search.base.SearchRequestContextProvider
import com.mapbox.search.base.core.CoreEngineOptions
import com.mapbox.search.base.core.CoreSearchEngine
import com.mapbox.search.base.core.getUserActivityReporter
import com.mapbox.search.base.location.LocationEngineAdapter
import com.mapbox.search.base.location.WrapperLocationProvider
import com.mapbox.search.base.location.defaultLocationEngine
import com.mapbox.search.base.utils.UserAgentProvider
import com.mapbox.search.common.IsoCountryCode
import com.mapbox.search.common.IsoLanguageCode
import com.mapbox.search.common.NavigationProfile
import com.mapbox.search.common.RoutablePoint
import com.mapbox.search.common.SearchRequestException
import com.mapbox.search.common.metadata.ImageInfo
import com.mapbox.search.common.metadata.OpenHours
import com.mapbox.search.common.metadata.OpenPeriod
import com.mapbox.search.common.metadata.WeekDay
import com.mapbox.search.common.metadata.WeekTimestamp
import com.mapbox.search.internal.bindgen.ApiType
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okhttp3.mockwebserver.SocketPolicy
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.Locale

@RunWith(AndroidJUnit4::class)
internal class PlaceAutocompleteIntegrationTest {

    private lateinit var mockServer: MockWebServer
    private lateinit var placeAutocomplete: PlaceAutocomplete

    @Before
    fun setUp() {
        mockServer = MockWebServer()

        val engine = createEngine(
            app = APP,
            token = TEST_ACCESS_TOKEN,
            url = mockServer.url("").toString(),
            locationEngine = defaultLocationEngine()
        )

        placeAutocomplete = PlaceAutocompleteImpl(
            accessToken = TEST_ACCESS_TOKEN,
            searchEngine = engine,
            activityReporter = getUserActivityReporter(TEST_ACCESS_TOKEN),
        )
    }

    @Test
    fun testRequestParametersForForwardGeocodingSearch() {
        mockServer.enqueue(MockResponse().setResponseCode(500))

        runBlocking {
            placeAutocomplete.suggestions(TEST_QUERY, TEST_REGION, TEST_LOCATION, TEST_OPTIONS)
        }

        val request = mockServer.takeRequest()
        assertEqualsIgnoreCase("get", request.method)

        val url = requireNotNull(request.requestUrl)
        assertEqualsIgnoreCase("//search/v1/suggest/$TEST_QUERY", url.encodedPath)
        assertEquals(TEST_ACCESS_TOKEN, url.queryParameter("access_token"))
        assertEquals(TEST_OPTIONS.language.code, url.queryParameter("language"))
        assertEquals(
            TEST_OPTIONS.countries?.joinToString(",") { it.code },
            url.queryParameter("country")
        )
        assertEquals(TEST_OPTIONS.limit.toString(), url.queryParameter("limit"))
        assertEquals(
            TEST_OPTIONS.types?.joinToString(",") { it.coreType.toString().lowercase() },
            url.queryParameter("types")
        )
        assertEquals(formatPoints(TEST_LOCATION), url.queryParameter("proximity"))
        assertEquals(formatPoints(TEST_LOCATION), url.queryParameter("origin"))
        assertEquals(
            formatPoints(TEST_REGION.southwest(), TEST_REGION.northeast()),
            url.queryParameter("bbox")
        )
        assertEquals(
            TEST_OPTIONS.navigationProfile?.rawName,
            url.queryParameter("navigation_profile")
        )
        assertEquals(
            "navigation",
            url.queryParameter("eta_type")
        )
    }

    @Test
    fun testRequestParametersForReverseGeocodingSearch() {
        mockServer.enqueue(MockResponse().setResponseCode(500))

        runBlocking {
            placeAutocomplete.suggestions(TEST_LOCATION, TEST_OPTIONS)
        }

        val request = mockServer.takeRequest()
        assertEqualsIgnoreCase("get", request.method)

        val url = requireNotNull(request.requestUrl)
        assertEqualsIgnoreCase("//search/v1/reverse/${formatPoints(TEST_LOCATION)}", url.encodedPath)
        assertEquals(TEST_ACCESS_TOKEN, url.queryParameter("access_token"))
        assertEquals(TEST_OPTIONS.language.code, url.queryParameter("language"))
        assertEquals(TEST_OPTIONS.limit.toString(), url.queryParameter("limit"))
        assertEquals(
            TEST_OPTIONS.types?.joinToString(",") { it.coreType.toString().lowercase() },
            url.queryParameter("types")
        )
    }

    @Test
    fun testSuccessfulEmptyResponse() {
        mockServer.enqueue(createSuccessfulResponse("suggestions_successful_empty_response.json"))

        val response = runBlocking {
            placeAutocomplete.suggestions(TEST_QUERY)
        }

        assertTrue(response.isValue)
        assertTrue(requireNotNull(response.value).isEmpty())
    }

    @Test
    fun testSuccessfulResponse() {
        mockServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val path = requireNotNull(request.path)
                val responsePath = when {
                    path.contains("/suggest") -> "suggestions_successful_response.json"
                    path.contains("/retrieve") -> {
                        val body = String(request.body.readByteArray())
                        when (val id = JSONObject(body).getString("id")) {
                            "suggestion-id-1" -> "retrieve_successful_1.json"
                            "suggestion-id-2" -> "retrieve_successful_2.json"
                            "suggestion-id-3" -> "retrieve_successful_3.json"
                            else -> error("Unknown suggestion id: $id")
                        }
                    }
                    else -> error("Unknown URL path: $path")
                }
                return createSuccessfulResponse(responsePath)
            }
        }

        val response = runBlocking {
            placeAutocomplete.suggestions(TEST_QUERY)
        }

        assertTrue(response.isValue)

        val suggestions = requireNotNull(response.value)
        assertEquals(3, suggestions.size)

        val suggestion = suggestions.first()
        assertEquals("Starbucks", suggestion.name)
        assertEquals(
            "901 15th St NW, Washington, District of Columbia 20005, United States of America",
            suggestion.formattedAddress
        )
        assertEquals("restaurant", suggestion.makiIcon)
        assertEquals(PlaceAutocompleteType.Poi, suggestion.type)
        assertEquals(listOf("food", "food and drink", "coffee shop"), suggestion.categories)

        val selectResponse = runBlocking {
            placeAutocomplete.select(suggestion)
        }
        assertTrue(response.isValue)

        val result = selectResponse.value!!
        assertEquals("Starbucks", result.name)
        assertEquals(Point.fromLngLat(-77.033568, 38.90143), result.coordinate)
        assertEquals(null, result.routablePoints)
        assertEquals("restaurant", result.makiIcon)
        assertEquals(PlaceAutocompleteType.Poi, result.type)
        assertEquals(listOf("food", "food and drink", "coffee shop"), result.categories)

        assertEquals(
            PlaceAutocompleteAddress(
                houseNumber = "901",
                street = "15th St Nw",
                neighborhood = "Downtown",
                locality = null,
                postcode = "20005",
                place = "Washington",
                district = null,
                region = "District of Columbia",
                country = "United States of America",
                formattedAddress = "901 15th St NW, Washington, District of Columbia 20005, United States of America",
                countryIso1 = "US",
                countryIso2 = "US-DC",
            ),
            result.address
        )
        assertEquals("+1 123-456-789", result.phone)
        assertEquals("http://www.test.com/", result.website)
        assertEquals(35, result.reviewCount)
        assertEquals(4.0, result.averageRating)
        assertEquals(
            OpenHours.Scheduled(
                periods = listOf(
                    OpenPeriod(
                        open = WeekTimestamp(WeekDay.SATURDAY, 7, 0),
                        closed = WeekTimestamp(WeekDay.SATURDAY, 20, 0)
                    )
                )
            ), result.openHours
        )
        assertEquals(listOf(ImageInfo("https://test.com/img-primary.jpg", 300, 350)), result.primaryPhotos)
        assertEquals(listOf(ImageInfo("https://test.com/img-other.jpg", 150, 350)), result.otherPhotos)

        assertEquals(
            "Virginia, United States",
            suggestions[1].formattedAddress
        )

        assertEquals(
            "Arlington, Virginia, United States",
            suggestions[2].formattedAddress
        )
    }

    @Test
    fun testResponseWithFewSuccessfulAndOneFailedRetrieve() {
        mockServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val path = requireNotNull(request.path)
                return when {
                    path.contains("/suggest") -> createSuccessfulResponse("suggestions_successful_response.json")
                    path.contains("/retrieve") -> {
                        val body = String(request.body.readByteArray())
                        when (val id = JSONObject(body).getString("id")) {
                            "suggestion-id-1" -> createSuccessfulResponse("retrieve_successful_1.json")
                            "suggestion-id-2" -> createSuccessfulResponse("retrieve_successful_2.json")
                            "suggestion-id-3" -> MockResponse().setResponseCode(500)
                            else -> error("Unknown suggestion id: $id")
                        }
                    }
                    else -> error("Unknown URL path: $path")
                }
            }
        }

        val response = runBlocking {
            placeAutocomplete.suggestions(TEST_QUERY)
        }

        assertTrue(response.isValue)

        val suggestions = requireNotNull(response.value)
        assertEquals(2, suggestions.size)

        assertEquals(
            "901 15th St NW, Washington, District of Columbia 20005, United States of America",
            suggestions[0].formattedAddress
        )

        assertEquals(
            "Virginia, United States",
            suggestions[1].formattedAddress
        )
    }

    @Test
    fun testResponseWithSuccessfulSuggestionsAndAllFailedRetrieve() {
        mockServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val path = requireNotNull(request.path)
                return when {
                    path.contains("/suggest") -> createSuccessfulResponse("suggestions_successful_response.json")
                    path.contains("/retrieve") -> {
                        val body = String(request.body.readByteArray())
                        when (val id = JSONObject(body).getString("id")) {
                            "suggestion-id-1" -> MockResponse().setResponseCode(501)
                            "suggestion-id-2" -> MockResponse().setResponseCode(502)
                            "suggestion-id-3" -> MockResponse().setResponseCode(503)
                            else -> error("Unknown suggestion id: $id")
                        }
                    }
                    else -> error("Unknown URL path: $path")
                }
            }
        }

        val response = runBlocking {
            placeAutocomplete.suggestions(TEST_QUERY)
        }
        assertTrue(response.isValue)
        assertEquals(3, response.value!!.size)

        response.value!!.map { suggestion ->
            val selectionResult = runBlocking {
                placeAutocomplete.select(suggestion)
            }
            assertTrue(selectionResult.isError)
        }
    }

    @Test
    fun testSuccessfulResponseWhenAllSuggestionsHaveCoordinates() {
        mockServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val path = requireNotNull(request.path)
                return when {
                    path.contains("/suggest") -> createSuccessfulResponse("suggestions_successful_with_coordinates_response.json")
                    path.contains("/retrieve") -> {
                        val body = String(request.body.readByteArray())
                        when (val id = JSONObject(body).getString("id")) {
                            "suggestion-id-1" -> createSuccessfulResponse("retrieve_successful_1.json")
                            else -> error("Unknown suggestion id: $id")
                        }
                    }
                    else -> error("Unknown URL path: $path")
                }
            }
        }

        val suggestionsResponse = runBlocking {
            placeAutocomplete.suggestions(TEST_QUERY)
        }

        assertTrue(suggestionsResponse.isValue)
        val suggestions = suggestionsResponse.value!!
        assertEquals(3, suggestions.size)

        assertEquals("Starbucks", suggestions[0].name)
        assertEquals("1401 New York Ave NW, Washington, District of Columbia 20005, United States of America", suggestions[0].formattedAddress)
        assertEquals(
            listOf(
                RoutablePoint(Point.fromLngLat(-77.032161, 38.900017), "POI")
            ),
            suggestions[0].routablePoints
        )

        assertEquals("Starbucks", suggestions[1].name)
        assertEquals(
            "901 15th St NW, Washington, District of Columbia 20005, United States of America",
            suggestions[1].formattedAddress
        )
        assertEquals(
            listOf(
                RoutablePoint(Point.fromLngLat(-77.033568, 38.90143), "POI")
            ),
            suggestions[1].routablePoints
        )

        assertEquals("Starbucks", suggestions[2].name)
        assertEquals(
            "1401 New York Ave NW, Washington, District of Columbia 20005, United States of America",
            suggestions[2].formattedAddress
        )
        assertEquals(null, suggestions[2].routablePoints)

        val selectionResponse = runBlocking {
            placeAutocomplete.select(suggestions.first())
        }

        assertTrue(selectionResponse.isValue)
        val result = selectionResponse.value!!
        assertNotNull(result.phone)
    }

    @Test
    fun testSuccessfulResponseWhenSomeSuggestionsHaveCoordinates() {
        mockServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val path = requireNotNull(request.path)
                return when {
                    path.contains("/suggest") -> createSuccessfulResponse("suggestions_successful_with_mixed_coordinates_response.json")
                    path.contains("/retrieve") -> {
                        val body = String(request.body.readByteArray())
                        when (val id = JSONObject(body).getString("id")) {
                            "suggestion-id-1" -> createSuccessfulResponse("retrieve_successful_1.json")
                            else -> error("Unknown suggestion id: $id")
                        }
                    }
                    else -> error("Unknown URL path: $path")
                }
            }
        }

        val suggestionsResponse = runBlocking {
            placeAutocomplete.suggestions(TEST_QUERY)
        }

        assertTrue(suggestionsResponse.isValue)
        val suggestions = suggestionsResponse.value!!
        assertEquals(3, suggestions.size)

        assertEquals("Starbucks", suggestions[0].name)
        assertEquals("1401 New York Ave NW, Washington, District of Columbia 20005, United States of America", suggestions[0].formattedAddress)
        assertEquals(null, suggestions[0].routablePoints)

        val selectionResponse = runBlocking {
            placeAutocomplete.select(suggestions.first())
        }

        assertTrue(selectionResponse.isValue)
        val result = selectionResponse.value!!
        assertNotNull(result.phone)
    }

    @Test
    fun testSuccessfulResponseWithUnsupportedSuggestionTypes() {
        mockServer.enqueue(createSuccessfulResponse("suggestions_successful_with_unsupported_suggestions.json"))

        val suggestionsResponse = runBlocking {
            placeAutocomplete.suggestions(TEST_QUERY)
        }

        assertTrue(suggestionsResponse.isValue)
        val suggestions = suggestionsResponse.value!!
        assertEquals(1, suggestions.size)

        assertEquals("Starbucks", suggestions[0].name)
        assertEquals("1401 New York Ave NW, Washington, District of Columbia 20005, United States of America", suggestions[0].formattedAddress)
        assertEquals(
            listOf(
                RoutablePoint(Point.fromLngLat(-77.032161, 38.900017), "POI")
            ),
            suggestions[0].routablePoints
        )
    }

    @Test
    fun testErrorSuggestionsResponse() {
        val errorResponse = MockResponse()
            .setResponseCode(400)
            .setBody(readFileFromAssets("error-response.json"))

        mockServer.enqueue(errorResponse)

        val response = runBlocking {
            placeAutocomplete.suggestions(TEST_QUERY)
        }

        val error = SearchRequestException(
            readFileFromAssets("error-response.json"),
            400
        )

        assertTrue(response.isError)
        assertEquals(error, response.error)
    }

    @Test
    fun testNetworkError() {
        mockServer.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))

        val response = runBlocking {
            placeAutocomplete.suggestions(TEST_QUERY)
        }

        assertTrue(response.isError)
        assertTrue(response.error is IOException)
    }

    private companion object {

        val CONTEXT: Context
            get() = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext

        val APP: Application
            get() = CONTEXT as Application

        const val TEST_ACCESS_TOKEN = "pk.test"

        val TEST_OPTIONS = PlaceAutocompleteOptions(
            limit = 5,
            countries = listOf(IsoCountryCode.UNITED_STATES, IsoCountryCode.CANADA),
            language = IsoLanguageCode.FRENCH,
            types = listOf(
                PlaceAutocompleteType.Poi,
                PlaceAutocompleteType.AdministrativeUnit.Street,
                PlaceAutocompleteType.AdministrativeUnit.Address
            ),
            navigationProfile = NavigationProfile.CYCLING
        )

        val TEST_LOCATION: Point = Point.fromLngLat(14.421576441071238, 50.087300021978024)

        val TEST_REGION: BoundingBox = BoundingBox.fromPoints(
            Point.fromLngLat(14.414441059376246, 50.08443383305258),
            Point.fromLngLat(14.432670012143467, 50.0930762433351)
        )

        const val TEST_QUERY = "Washington"

        fun assertEqualsIgnoreCase(expected: String?, actual: String?) {
            assertEquals(
                expected?.lowercase(Locale.getDefault()),
                actual?.lowercase(Locale.getDefault())
            )
        }

        fun createEngine(
            app: Application,
            token: String,
            url: String,
            locationEngine: LocationEngine
        ): PlaceAutocompleteEngine {
            val coreEngine = CoreSearchEngine(
                CoreEngineOptions(
                    token,
                    url,
                    ApiType.SBS,
                    UserAgentProvider.userAgent,
                    null
                ),
                WrapperLocationProvider(
                    LocationEngineAdapter(app, locationEngine), null
                ),
            )

            return PlaceAutocompleteEngine(
                coreEngine = coreEngine,
                requestContextProvider = SearchRequestContextProvider(app),
            )
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
