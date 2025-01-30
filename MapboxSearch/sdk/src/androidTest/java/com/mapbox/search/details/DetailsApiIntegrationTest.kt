package com.mapbox.search.details

import com.mapbox.annotation.MapboxExperimental
import com.mapbox.common.MapboxOptions
import com.mapbox.geojson.Point
import com.mapbox.search.AttributeSet
import com.mapbox.search.BaseTest
import com.mapbox.search.MapboxSearchSdk
import com.mapbox.search.common.IsoCountryCode
import com.mapbox.search.common.IsoLanguageCode
import com.mapbox.search.common.SearchRequestException
import com.mapbox.search.common.metadata.ImageInfo
import com.mapbox.search.common.metadata.OpenHours
import com.mapbox.search.tests_support.BlockingSearchResultCallback
import com.mapbox.search.utils.assertEqualsIgnoreCase
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(MapboxExperimental::class)
internal class DetailsApiIntegrationTest : BaseTest() {

    private lateinit var mockServer: MockWebServer
    private lateinit var detailsApiSettings: DetailsApiSettings
    private lateinit var detailsApi: DetailsApi

    @Before
    override fun setUp() {
        super.setUp()
        MapboxOptions.accessToken = TEST_ACCESS_TOKEN

        mockServer = MockWebServer()

        MapboxSearchSdk.initialize(
            application = targetApplication,
        )

        detailsApiSettings = DetailsApiSettings(
            baseUrl = mockServer.url("").toString(),
        )

        detailsApi = DetailsApi.create(detailsApiSettings)
    }

    @Test
    fun testRequestParametersAreCorrect() {
        mockServer.enqueue(MockResponse().setResponseCode(500))

        val options = RetrieveDetailsOptions(
            attributeSets = listOf(AttributeSet.BASIC, AttributeSet.VISIT, AttributeSet.PHOTOS, AttributeSet.VENUE),
            language = IsoLanguageCode.FRENCH,
            worldview = IsoCountryCode.FRANCE,
        )

        val callback = BlockingSearchResultCallback()
        detailsApi.retrieveDetails(TEST_MAPBOX_ID, options, callback)

        val request = mockServer.takeRequest()
        assertEqualsIgnoreCase("get", request.method!!)

        val url = request.requestUrl!!
        assertEqualsIgnoreCase("//search/details/v1/retrieve/$TEST_MAPBOX_ID", url.encodedPath)
        assertEquals(TEST_ACCESS_TOKEN, url.queryParameter("access_token"))
        assertEquals(options.language.code, url.queryParameter("language"))
        assertEquals(options.worldview!!.code, url.queryParameter("worldview"))
        assertEquals(
            options.attributeSets!!.joinToString(separator = ",") { it.name.lowercase() },
            url.queryParameter("attribute_sets")
        )
    }

    @Test
    fun testSuccessfulResponse() {
        mockServer.enqueue(createSuccessfulResponse("details_api/response_successful.json"))

        val callback = BlockingSearchResultCallback()
        detailsApi.retrieveDetails(TEST_MAPBOX_ID, RetrieveDetailsOptions(), callback)

        val requestResult = callback.getResultBlocking()
        assertTrue(requestResult.isSuccess)

        val searchResult = requestResult.getSuccess().result

        assertEquals("Planet Word", searchResult.name)
        assertEquals(
            "dXJuOm1ieHBvaTo0ZTg2ZWFkNS1jOWMwLTQ3OWEtOTA5Mi1kMDVlNDQ3NDdlODk",
            searchResult.mapboxId,
        )
        assertEquals(Point.fromLngLat(-77.029129, 38.902309), searchResult.coordinate)
        assertTrue(searchResult.metadata?.openHours is OpenHours.Scheduled)
        assertEquals("+12029313139", searchResult.metadata?.phone)

        assertEquals(
            listOf(
                ImageInfo(
                    url = "https://media-cdn.tripadvisor.com/media/photo-o/2a/08/ad/fe/caption.jpg",
                    width = 768,
                    height = 1024,
                ),
                ImageInfo(
                    url = "https://media-cdn.tripadvisor.com/media/photo-o/28/b6/2d/cc/caption.jpg",
                    width = 2006,
                    height = 975,
                ),
                ImageInfo(
                    url = "https://media-cdn.tripadvisor.com/media/photo-o/2a/45/ec/0a/caption.jpg",
                    width = 5184,
                    height = 3888,
                ),
                ImageInfo(
                    url = "https://media-cdn.tripadvisor.com/media/photo-o/28/b6/2d/ca/caption.jpg",
                    width = 2006,
                    height = 975,
                ),
            ),
            searchResult.metadata?.otherPhotos
        )
        assertEquals(
            listOf(
                ImageInfo(
                    url = "https://media-cdn.tripadvisor.com/media/photo-o/2a/08/ad/cd/caption.jpg",
                    width = 1024,
                    height = 768,
                ),
            ),
            searchResult.metadata?.primaryPhotos
        )
    }

    @Test
    fun testErrorResponse() {
        mockServer.enqueue(MockResponse().setResponseCode(404))

        val callback = BlockingSearchResultCallback()
        detailsApi.retrieveDetails(TEST_MAPBOX_ID, RetrieveDetailsOptions(), callback)

        val requestResult = callback.getResultBlocking()
        assertTrue(requestResult.isError)

        val e = requestResult.getError().e
        assertTrue(e is SearchRequestException && e.code == 404)
    }

    private companion object {
        const val TEST_ACCESS_TOKEN = "pk.test"
        const val TEST_MAPBOX_ID = "test-id"
    }
}
