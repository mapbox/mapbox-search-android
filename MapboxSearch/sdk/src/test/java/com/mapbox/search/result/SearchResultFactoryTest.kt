package com.mapbox.search.result

import com.mapbox.geojson.Point
import com.mapbox.search.TestConstants.ASSERTIONS_KT_CLASS_NAME
import com.mapbox.search.common.logger.reinitializeLogImpl
import com.mapbox.search.common.logger.resetLogImpl
import com.mapbox.search.common.reportError
import com.mapbox.search.common.reportRelease
import com.mapbox.search.tests_support.createTestOriginalSearchResult
import com.mapbox.search.tests_support.createTestRequestOptions
import com.mapbox.test.dsl.TestCase
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestFactory

internal class SearchResultFactoryTest {

    @TestFactory
    fun `Check SearchResult creation method`() = TestCase {
        Given("SearchResultFactory instance") {
            val factory = SearchResultFactory { null }

            SEARCH_RESULT_TYPES_MAP.forEach { (types, expectedCreated) ->
                When("Creating SearchResult from original search result with types: $types") {
                    val actualCreated = try {
                        factory.createSearchResult(
                            ORIGINAL_SEARCH_RESULT.copy(types = types),
                            REQUEST_OPTIONS
                        )
                    } catch (e: IllegalArgumentException) {
                        null
                    } catch (e: IllegalStateException) {
                        null
                    } != null

                    Then("Search result is created should be $expectedCreated", expectedCreated, actualCreated)
                }
            }
        }
    }

    @TestFactory
    fun `Check SearchResult with illegal location`() = TestCase {
        Given("SearchResultFactory instance") {
            val factory = SearchResultFactory { null }

            When("Search result with NaN location is passed") {
                val illegalResult = ORIGINAL_SEARCH_RESULT.copy(center = Point.fromLngLat(Double.NaN, Double.NaN))

                factory.createSearchResult(
                    illegalResult,
                    REQUEST_OPTIONS
                )

                Verify("Error reported") {
                    reportRelease(any(), any<String>())
                }
            }
        }
    }

    private companion object {

        val SEARCH_RESULT_TYPES_MAP: Map<List<OriginalResultType>, Boolean> =
            OriginalResultType.values().map { listOf(it) to true }.toMap() + mapOf(
                listOf(OriginalResultType.COUNTRY, OriginalResultType.REGION) to true,
                listOf(OriginalResultType.REGION, OriginalResultType.PLACE) to true,
                listOf(OriginalResultType.COUNTRY, OriginalResultType.REGION) to true,
                listOf(OriginalResultType.COUNTRY, OriginalResultType.POI) to false,
                listOf(OriginalResultType.ADDRESS, OriginalResultType.POI) to false,
                listOf(OriginalResultType.REGION, OriginalResultType.ADDRESS, OriginalResultType.POI) to false,
                emptyList<OriginalResultType>() to false,
                listOf(OriginalResultType.PLACE, OriginalResultType.CATEGORY) to false,
                listOf(OriginalResultType.UNKNOWN) to false,
                listOf(OriginalResultType.CATEGORY) to false,
                listOf(OriginalResultType.QUERY) to false,
                listOf(OriginalResultType.USER_RECORD) to false,
            )

        val REQUEST_OPTIONS = createTestRequestOptions(query = "test-query")

        val ORIGINAL_SEARCH_RESULT = createTestOriginalSearchResult(
            id = "Search result 1",
            names = listOf("Search result 1.1", "Search result 1.2"),
            descriptionAddress = "Search result 1 description",
            addresses = listOf(SearchAddress(country = "Belarus", region = "Minsk", street = "Francyska Skaryny", houseNumber = "1")),
            distanceMeters = 123.0,
            icon = "cafe",
            etaMinutes = 5.0,
            center = Point.fromLngLat(1.0, 1.0),
            types = listOf(OriginalResultType.POI)
        )

        @Suppress("DEPRECATION", "JVM_STATIC_IN_PRIVATE_COMPANION")
        @BeforeAll
        @JvmStatic
        fun setUpAll() {
            resetLogImpl()
            mockkStatic(ASSERTIONS_KT_CLASS_NAME)
            every { reportError(any()) } returns Unit
        }

        @Suppress("JVM_STATIC_IN_PRIVATE_COMPANION")
        @AfterAll
        @JvmStatic
        fun tearDownAll() {
            reinitializeLogImpl()
            unmockkStatic(ASSERTIONS_KT_CLASS_NAME)
        }
    }
}
