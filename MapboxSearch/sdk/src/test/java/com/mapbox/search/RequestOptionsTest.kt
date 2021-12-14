package com.mapbox.search

import com.mapbox.geojson.Point
import com.mapbox.search.core.CoreRequestOptions
import com.mapbox.search.result.SearchRequestContext
import com.mapbox.test.dsl.TestCase
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.jupiter.api.AfterAll
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
        Given("Mocked SearchOptions mapToCore()") {
            mockkStatic("com.mapbox.search.SearchOptionsKt")
            val testOption = TEST_ENTRIES.keys.first()
            When("Convert Platform RequestOptions to core") {
                testOption.mapToCore()
                Verify("SearchOptions mapToCore() was invoked") {
                    testOption.options.mapToCore()
                }
            }
        }
    }

    @TestFactory
    fun `Check mapping CoreRequestOptions to platform`() = TestCase {
        Given("RequestOptions mapToPlatform() extension") {
            TEST_ENTRIES_REV.forEach { (inputValue, expectedValue) ->
                When("Convert Core RequestOptions <$inputValue> to platform") {
                    val actualValue = inputValue.mapToPlatform(TEST_SEARCH_REQUEST_CONTEXT)
                    Then("RequestOptions should be <$expectedValue>", expectedValue, actualValue)
                }
            }
        }
        Given("Mocked SearchOptions mapToPlatform()") {
            mockkStatic("com.mapbox.search.SearchOptionsKt")
            val testCoreOption = TEST_ENTRIES.values.first()
            When("Convert Core RequestOptions to platform") {
                testCoreOption.mapToPlatform(TEST_SEARCH_REQUEST_CONTEXT)
                Verify("SearchOptions mapToCore() was invoked") {
                    testCoreOption.options.mapToPlatform()
                }
            }
        }
    }

    companion object {

        private val TEST_POINT: Point = Point.fromLngLat(10.0, 10.0)
        private val TEST_ORIGIN_POINT: Point = Point.fromLngLat(20.0, 20.0)
        private val TEST_SEARCH_REQUEST_CONTEXT: SearchRequestContext = SearchRequestContext(ApiType.SBS)

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

        private val TEST_ENTRIES_REV = TEST_ENTRIES.entries.associateBy({ it.value }, { it.key })

        @AfterAll
        @JvmStatic
        fun tearDown() {
            unmockkStatic("com.mapbox.search.SearchOptionsKt")
        }
    }
}
