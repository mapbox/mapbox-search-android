package com.mapbox.search.autofill

import android.app.Application
import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mapbox.common.MapboxOptions
import com.mapbox.common.location.LocationProvider
import com.mapbox.geojson.Point
import com.mapbox.search.base.BaseSearchSdkInitializer
import com.mapbox.search.base.SearchRequestContextProvider
import com.mapbox.search.base.core.CoreApiType
import com.mapbox.search.base.core.CoreEngineOptions
import com.mapbox.search.base.core.CoreSearchEngine
import com.mapbox.search.base.core.getUserActivityReporter
import com.mapbox.search.base.location.LocationEngineAdapter
import com.mapbox.search.base.location.WrapperLocationProvider
import com.mapbox.search.base.location.defaultLocationProvider
import com.mapbox.search.common.IsoCountryCode
import com.mapbox.search.common.IsoLanguageCode
import com.mapbox.search.common.SearchRequestException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okhttp3.mockwebserver.SocketPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.Locale

@RunWith(AndroidJUnit4::class)
internal class AddressAutofillIntegrationTest {

    private lateinit var mockServer: MockWebServer
    private lateinit var addressAutofill: AddressAutofill

    @Before
    fun setUp() {
        MapboxOptions.accessToken = TEST_ACCESS_TOKEN

        mockServer = MockWebServer()

        val engine = createAutofillSearchEngine(
            app = APP,
            url = mockServer.url("").toString(),
            locationProvider = defaultLocationProvider()
        )

        addressAutofill = AddressAutofillImpl(
            autofillEngine = engine,
            activityReporter = getUserActivityReporter()
        )
    }

    @Test
    fun testRequestParametersForForwardGeocodingSearch() {
        mockServer.enqueue(MockResponse().setResponseCode(500))

        val options = AddressAutofillOptions(
            countries = listOf(IsoCountryCode.UNITED_STATES, IsoCountryCode.CANADA),
            language = IsoLanguageCode.FRENCH
        )

        val query = requireNotNull(Query.create("Washington"))

        runBlocking {
            addressAutofill.suggestions(query, options)
        }

        val request = mockServer.takeRequest()
        assertEqualsIgnoreCase("get", request.method)

        val url = requireNotNull(request.requestUrl)
        assertEqualsIgnoreCase("//autofill/v1/suggest/${query.query}", url.encodedPath)
        assertEquals(TEST_ACCESS_TOKEN, url.queryParameter("access_token"))
        assertEquals(options.language?.code, url.queryParameter("language"))
        assertEquals(options.countries?.joinToString(",") { it.code }, url.queryParameter("country"))
        assertEquals("address", url.queryParameter("types"))
        assertEquals("true", url.queryParameter("streets"))
    }

    @Test
    fun testRequestParametersForReverseGeocodingSearch() {
        mockServer.enqueue(MockResponse().setResponseCode(500))

        val options = AddressAutofillOptions(
            language = IsoLanguageCode.FRENCH
        )

        val point = Point.fromLngLat(-77.03398187174899, 38.8999032596197)

        runBlocking {
            addressAutofill.reverse(point, options)
        }

        val request = mockServer.takeRequest()
        assertEqualsIgnoreCase("get", request.method)

        val url = requireNotNull(request.requestUrl)
        assertTrue(url.encodedPath.startsWith("//autofill/v1/retrieve/"))
        assertEquals(TEST_ACCESS_TOKEN, url.queryParameter("access_token"))
        assertEquals(options.language?.code, url.queryParameter("language"))
    }

    @Test
    fun testSuccessfulResponse() {
        mockServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val path = requireNotNull(request.path)
                val responsePath = when {
                    path.contains("/suggest") -> "suggestions_successful_response.json"
                    path.contains("/retrieve/actionId1") -> "retrieve_successful_1.json"
                    path.contains("/retrieve/actionId2") -> "retrieve_successful_2.json"
                    path.contains("/retrieve/actionId3") -> "retrieve_successful_3.json"
                    else -> error("Unknown URL path: $path")
                }
                return createSuccessfulResponse(responsePath)
            }
        }

        val response = runBlocking {
            addressAutofill.suggestions(TEST_QUERY, AddressAutofillOptions())
        }

        assertTrue(response.isValue)

        val results = requireNotNull(response.value)
        assertEquals(3, results.size)

        assertEquals(
            "740 15th St NW, Washington, District of Columbia 20005, United States",
            results[0].formattedAddress
        )
        assertEquals(
            "740 15th Street, Donora, Pennsylvania 15033, United States",
            results[1].formattedAddress
        )
        assertEquals(
            "740 15th Street, Bessemer, Pennsylvania 16112, United States",
            results[2].formattedAddress
        )

        val firstSuggestion = results.first()
        assertEquals("740 15th St NW", firstSuggestion.name)

        val selectionResponse = runBlocking {
            addressAutofill.select(firstSuggestion)
        }
        assertTrue(selectionResponse.isValue)

        val resultAddress = requireNotNull(selectionResponse.value).address
        assertEquals("740", resultAddress.houseNumber)
        assertEquals("15th St NW", resultAddress.street)
        assertEquals(null, resultAddress.neighborhood)
        assertEquals(null, resultAddress.locality)
        assertEquals("20005", resultAddress.postcode)
        assertEquals("Washington", resultAddress.place)
        assertEquals(null, resultAddress.district)
        assertEquals("District of Columbia", resultAddress.region)
        assertEquals("United States", resultAddress.country)
        assertEquals("us", resultAddress.countryIso1)
        assertEquals("US-DC", resultAddress.countryIso2)

        val autofillResult = runBlocking {
            addressAutofill.select(firstSuggestion)
        }
        assertTrue(autofillResult.isValue)
        val autofillResultValue: AddressAutofillResult = requireNotNull(autofillResult.value)
        assertEquals(Point.fromLngLat(-77.03375, 38.89936), autofillResultValue.coordinate)
    }

    @Test
    fun testResponseWithFewSuccessfulAndOneFailedRetrieve() {
        listOf(
            "suggestions_successful_response.json",
            "retrieve_successful_1.json",
            "retrieve_successful_2.json",
        ).forEach { path ->
            mockServer.enqueue(createSuccessfulResponse(path))
        }
        mockServer.enqueue(MockResponse().setResponseCode(500))

        val response = runBlocking {
            addressAutofill.suggestions(TEST_QUERY, AddressAutofillOptions())
        }

        assertTrue(response.isValue)

        val results = requireNotNull(response.value)
        assertEquals(3, results.size)

        val autofillResult = runBlocking {
            results.map { suggestion ->
                async {
                    addressAutofill.select(suggestion)
                }
            }.awaitAll()
        }.filter { result -> result.isValue }
        assertEquals(2, autofillResult.size)
    }

    @Test
    fun testResponseWithSuccessfulSuggestionsAndAllFailedRetrieve() {
        mockServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val path = requireNotNull(request.path)
                return when {
                    path.contains("/suggest") -> createSuccessfulResponse("suggestions_successful_response.json")
                    path.contains("/retrieve/actionId1") -> MockResponse().setResponseCode(501)
                    path.contains("/retrieve/actionId2") -> MockResponse().setResponseCode(502)
                    path.contains("/retrieve/actionId3") -> MockResponse().setResponseCode(503)
                    else -> error("Unknown URL path: $path")
                }
            }
        }

        val response = runBlocking {
            addressAutofill.suggestions(TEST_QUERY, AddressAutofillOptions())
        }
        assertTrue(response.isValue)
        assertEquals(3, response.value!!.size)

        response.value!!.map { suggestion ->
            val selectionResult = runBlocking {
                addressAutofill.select(suggestion)
            }
            assertTrue(selectionResult.isError)
        }
    }

    @Test
    fun testEmptySuggestionsResponse() {
        mockServer.enqueue(createSuccessfulResponse("suggestions_successful_empty_response.json"))

        val response = runBlocking {
            addressAutofill.suggestions(TEST_QUERY, AddressAutofillOptions())
        }

        assertTrue(response.isValue)
        assertEquals(true, response.value?.isEmpty())
    }

    @Test
    fun testErrorSuggestionsResponse() {
        val errorResponse = MockResponse()
            .setResponseCode(400)
            .setBody(readFileFromAssets("error-response.json"))

        mockServer.enqueue(errorResponse)

        val response = runBlocking {
            addressAutofill.suggestions(TEST_QUERY, AddressAutofillOptions())
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
            addressAutofill.suggestions(TEST_QUERY, AddressAutofillOptions())
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

        val TEST_QUERY: Query = requireNotNull(Query.create("123 Washington"))

        fun assertEqualsIgnoreCase(expected: String?, actual: String?) {
            assertEquals(
                expected?.lowercase(Locale.getDefault()),
                actual?.lowercase(Locale.getDefault())
            )
        }

        fun createAutofillSearchEngine(
            app: Application,
            url: String,
            locationProvider: LocationProvider?
        ): AutofillSearchEngine {
            val coreEngine = CoreSearchEngine(
                CoreEngineOptions(
                    baseUrl = url,
                    apiType = CoreApiType.AUTOFILL,
                    sdkInformation = BaseSearchSdkInitializer.sdkInformation,
                    eventsUrl = null,
                ),
                WrapperLocationProvider(
                    LocationEngineAdapter(app, locationProvider), null
                ),
            )

            return AutofillSearchEngine(
                coreEngine = coreEngine,
                requestContextProvider = SearchRequestContextProvider(app),
            )
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
