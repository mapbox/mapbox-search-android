package com.mapbox.search.offline

import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.search.base.BaseRequestOptions
import com.mapbox.search.base.BaseResponseInfo
import com.mapbox.search.base.result.BaseRawResultType
import com.mapbox.search.base.result.BaseRawSearchResult
import com.mapbox.search.base.result.BaseSearchResultType
import com.mapbox.search.base.result.BaseServerSearchResultImpl
import com.mapbox.search.base.result.SearchRequestContext
import com.mapbox.search.common.tests.createTestCoreRequestOptions
import com.mapbox.search.internal.bindgen.ApiType
import com.mapbox.test.dsl.TestCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestFactory
import java.util.UUID
import kotlin.random.Random

internal class OfflineSearchResultCallbackAdapterTest {
    @TestFactory
    fun `Check empty results`() = TestCase {
        Given("Feature from a click event") {
            val mockedCallback = mockk<OfflineSearchResultCallback>()
            val searchResultSlot = slot<OfflineSearchResult>()
            val responseInfoSlot = slot<OfflineResponseInfo>()

            every {
                mockedCallback.onResult(capture(searchResultSlot), capture(responseInfoSlot))
            } returns Unit

            val callback = OfflineSearchResultCallbackAdapter(TEST_RETRIEVE_FEATURE, mockedCallback)

            When("No results are found") {

                callback.onResults(emptyList(), TEST_RESPONSE_INFO)

                Then("The feature is returned as the OfflineSearchResult") {
                    assertEquals(TEST_FEATURE_AS_RESULT.id, searchResultSlot.captured.id)
                    assertEquals(TEST_FEATURE_AS_RESULT.mapboxId, searchResultSlot.captured.mapboxId)
                    assertEquals(TEST_FEATURE_AS_RESULT.name, searchResultSlot.captured.name)
                    assertEquals(TEST_FEATURE_AS_RESULT.coordinate, searchResultSlot.captured.coordinate)
                }
            }
        }
    }

    @TestFactory
    fun `Check best match is found`() = TestCase {
        Given("Feature from a click event") {
            val mockedCallback = mockk<OfflineSearchResultCallback>()
            val searchResultSlot = slot<OfflineSearchResult>()
            val responseInfoSlot = slot<OfflineResponseInfo>()

            every {
                mockedCallback.onResult(capture(searchResultSlot), capture(responseInfoSlot))
            } returns Unit

            val callback = OfflineSearchResultCallbackAdapter(TEST_RETRIEVE_FEATURE, mockedCallback)

            When("Best match is found") {

                callback.onResults(listOf(TEST_OFFLINE_GOOD_SEARCH_RESULT), TEST_RESPONSE_INFO)

                Then("The best result is found") {
                    assertEquals(TEST_OFFLINE_GOOD_SEARCH_RESULT.id, searchResultSlot.captured.id)
                    assertEquals(TEST_OFFLINE_GOOD_SEARCH_RESULT.mapboxId, searchResultSlot.captured.mapboxId)
                    assertEquals(TEST_OFFLINE_GOOD_SEARCH_RESULT.name, searchResultSlot.captured.name)
                    assertEquals(TEST_OFFLINE_GOOD_SEARCH_RESULT.coordinate, searchResultSlot.captured.coordinate)
                }
            }
        }
    }

    @TestFactory
    fun `Check no match is found because of distance`() = TestCase {
        Given("Feature from a click event") {
            val mockedCallback = mockk<OfflineSearchResultCallback>()
            val searchResultSlot = slot<OfflineSearchResult>()
            val responseInfoSlot = slot<OfflineResponseInfo>()

            every {
                mockedCallback.onResult(capture(searchResultSlot), capture(responseInfoSlot))
            } returns Unit

            val callback = OfflineSearchResultCallbackAdapter(TEST_RETRIEVE_FEATURE, mockedCallback)

            When("Best match is NOT found") {

                callback.onResults(listOf(TEST_OFFLINE_BAD_SEARCH_RESULT_NAME), TEST_RESPONSE_INFO)

                Then("The best result is found") {
                    assertEquals(TEST_FEATURE_AS_RESULT.id, searchResultSlot.captured.id)
                    assertEquals(TEST_FEATURE_AS_RESULT.mapboxId, searchResultSlot.captured.mapboxId)
                    assertEquals(TEST_FEATURE_AS_RESULT.name, searchResultSlot.captured.name)
                    assertEquals(TEST_FEATURE_AS_RESULT.coordinate, searchResultSlot.captured.coordinate)
                }
            }
        }
    }

    @TestFactory
    fun `Check no match is found because of name`() = TestCase {
        Given("Feature from a click event") {
            val mockedCallback = mockk<OfflineSearchResultCallback>()
            val searchResultSlot = slot<OfflineSearchResult>()
            val responseInfoSlot = slot<OfflineResponseInfo>()

            every {
                mockedCallback.onResult(capture(searchResultSlot), capture(responseInfoSlot))
            } returns Unit

            val callback = OfflineSearchResultCallbackAdapter(TEST_RETRIEVE_FEATURE, mockedCallback)

            When("Best match is NOT found") {

                callback.onResults(listOf(TEST_OFFLINE_BAD_SEARCH_RESULT_DISTANCE), TEST_RESPONSE_INFO)

                Then("The best result is found") {
                    assertEquals(TEST_FEATURE_AS_RESULT.id, searchResultSlot.captured.id)
                    assertEquals(TEST_FEATURE_AS_RESULT.mapboxId, searchResultSlot.captured.mapboxId)
                    assertEquals(TEST_FEATURE_AS_RESULT.name, searchResultSlot.captured.name)
                    assertEquals(TEST_FEATURE_AS_RESULT.coordinate, searchResultSlot.captured.coordinate)
                }
            }
        }
    }

    private companion object {
        val TEST_RETRIEVE_FEATURE = Feature.fromJson("""
                {
                    "type": "Feature",
                    "id": "132494314",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -77.2214842,
                            39.0468692
                        ]
                    },
                    "properties": {
                        "class": "park_like",
                        "iso_3166_2": "US-MD",
                        "name_script": "Latin",
                        "filterrank": 1,
                        "type": "Nature Reserve",
                        "sizerank": 14,
                        "name": "Adventure Conservation Park",
                        "iso_3166_1": "US",
                        "maki": "park"
                    }
                }
            """.trimIndent())!!
        val TEST_CORE_REQUEST_OPTIONS = createTestCoreRequestOptions(
            query = TEST_RETRIEVE_FEATURE.getStringProperty("name"),
            options = OfflineSearchOptions().mapToCore()
        )
        val TEST_RESPONSE_INFO = BaseResponseInfo(
            BaseRequestOptions(
                TEST_CORE_REQUEST_OPTIONS,
                SearchRequestContext(ApiType.SBS)
            ),
            null,
                false
        )
        fun createBaseRawSearchResult(
            id: String = UUID.randomUUID().toString(),
            mapboxId: String? = null,
            resultType: BaseRawResultType = BaseRawResultType.PLACE,
            name: String = "Random Place Name",
            distanceMeters: Double? = null,
            center: Point = Point.fromLngLat(Random.nextDouble(), Random.nextDouble())
        ) = BaseRawSearchResult(
            id = id,
            mapboxId = mapboxId,
            listOf(resultType),
            listOf(name),
            null,
            listOf(),
            listOf(),
            null,
            name,
            null,
            distanceMeters,
            center,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            0,
            null,
            null,
            null
        )
        val TEST_FEATURE_AS_RESULT = OfflineSearchResult(createBaseRawSearchResult(
            id = "",
            name = TEST_RETRIEVE_FEATURE.getStringProperty("name"),
            center = TEST_RETRIEVE_FEATURE.geometry() as Point)
        )
        val TEST_OFFLINE_GOOD_SEARCH_RESULT = BaseServerSearchResultImpl(
            listOf(BaseSearchResultType.PLACE),
            createBaseRawSearchResult(
                id = "good-search-result",
                name = TEST_RETRIEVE_FEATURE.getStringProperty("name"),
                center = TEST_RETRIEVE_FEATURE.geometry() as Point,
                distanceMeters = .5
            ),
            BaseRequestOptions(
                TEST_CORE_REQUEST_OPTIONS,
                SearchRequestContext(ApiType.SBS)
            )
        )
        val TEST_OFFLINE_BAD_SEARCH_RESULT_DISTANCE = BaseServerSearchResultImpl(
            listOf(BaseSearchResultType.PLACE),
            createBaseRawSearchResult(
                id = "bad-search-result-distance",
                name = TEST_RETRIEVE_FEATURE.getStringProperty("name"),
                center = TEST_RETRIEVE_FEATURE.geometry() as Point,
                distanceMeters = 500.0
            ),
            BaseRequestOptions(
                TEST_CORE_REQUEST_OPTIONS,
                SearchRequestContext(ApiType.SBS)
            )
        )
        val TEST_OFFLINE_BAD_SEARCH_RESULT_NAME = BaseServerSearchResultImpl(
            listOf(BaseSearchResultType.PLACE),
            createBaseRawSearchResult(
                id = "bad-search-result-name",
                name = "some random name that doesn't match",
                center = TEST_RETRIEVE_FEATURE.geometry() as Point,
                distanceMeters = 0.5
            ),
            BaseRequestOptions(
                TEST_CORE_REQUEST_OPTIONS,
                SearchRequestContext(ApiType.SBS)
            )
        )
    }
}
