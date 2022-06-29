package com.mapbox.search.result

import com.mapbox.geojson.Point
import com.mapbox.search.tests_support.createTestOriginalSearchResult
import com.mapbox.search.tests_support.createTestRequestOptions
import com.mapbox.test.dsl.TestCase
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
    }
}
