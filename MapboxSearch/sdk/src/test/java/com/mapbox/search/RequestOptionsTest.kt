package com.mapbox.search

import com.mapbox.geojson.Point
import com.mapbox.search.base.core.CoreApiType
import com.mapbox.search.base.core.CoreRequestOptions
import com.mapbox.search.base.result.SearchRequestContext
import com.mapbox.test.dsl.TestCase
import org.junit.jupiter.api.TestFactory

internal class RequestOptionsTest {

    @TestFactory
    fun `Check mapping RequestOptions to core`() = TestCase {
        Given("RequestOptions mapToCore() extension") {
            TEST_ENTRIES.forEach { (inputValue, expectedValue) ->
                When("Convert Platform RequestOptions <$inputValue> to core") {
                    val actualValue = inputValue.mapToCore()
                    Then("Core RequestOptions should be <$expectedValue>", expectedValue, actualValue)
                }
            }
        }
    }

    companion object {

        private val TEST_POINT: Point = Point.fromLngLat(10.0, 10.0)
        private val TEST_ORIGIN_POINT: Point = Point.fromLngLat(20.0, 20.0)
        private val TEST_SEARCH_REQUEST_CONTEXT: SearchRequestContext = SearchRequestContext(CoreApiType.SBS)

        private val TEST_ENTRIES = mapOf(
            RequestOptions(
                query = "",
                options = SearchOptions(),
                proximityRewritten = false,
                originRewritten = false,
                endpoint = "suggest",
                sessionID = "",
                requestContext = TEST_SEARCH_REQUEST_CONTEXT,
            ) to CoreRequestOptions(
                "",
                "suggest",
                SearchOptions().mapToCore(),
                false,
                false,
                ""
            ),
            RequestOptions(
                query = "test query 2",
                options = SearchOptions(proximity = TEST_POINT, origin = TEST_ORIGIN_POINT),
                proximityRewritten = true,
                originRewritten = true,
                endpoint = "suggest",
                sessionID = "test-session-id-2",
                requestContext = TEST_SEARCH_REQUEST_CONTEXT,
            ) to CoreRequestOptions(
                "test query 2",
                "suggest",
                SearchOptions(proximity = TEST_POINT, origin = TEST_ORIGIN_POINT).mapToCore(),
                true,
                true,
                "test-session-id-2"
            )
        )
    }
}
