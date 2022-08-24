package com.mapbox.search.base.result

import com.mapbox.geojson.Point
import com.mapbox.search.base.logger.reinitializeLogImpl
import com.mapbox.search.base.logger.resetLogImpl
import com.mapbox.search.base.record.IndexableRecordResolver
import com.mapbox.search.base.tests_support.createBaseSearchAddress
import com.mapbox.search.base.tests_support.createTestBaseRawSearchResult
import com.mapbox.search.base.tests_support.createTestBaseRequestOptions
import com.mapbox.search.common.TestConstants
import com.mapbox.search.common.createTestCoreRequestOptions
import com.mapbox.test.dsl.TestCase
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestFactory

internal class SearchResultFactoryTest {

    @TestFactory
    fun `Check SearchResult creation method`() = TestCase {
        Given("SearchResultFactory instance") {

            val resolver = mockk<IndexableRecordResolver>()
            val factory = SearchResultFactory(resolver)

            SEARCH_RESULT_TYPES_MAP.forEach { (types, expectedCreated) ->
                When("Creating SearchResult from base raw search result with types: $types") {
                    val actualCreated = try {
                        factory.createSearchResult(
                            BASE_RAW_SEARCH_RESULT.copy(types = types),
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

        val SEARCH_RESULT_TYPES_MAP: Map<List<BaseRawResultType>, Boolean> =
            BaseRawResultType.values().associate { listOf(it) to true } + mapOf(
                listOf(BaseRawResultType.COUNTRY, BaseRawResultType.REGION) to true,
                listOf(BaseRawResultType.REGION, BaseRawResultType.PLACE) to true,
                listOf(BaseRawResultType.COUNTRY, BaseRawResultType.REGION) to true,
                listOf(BaseRawResultType.COUNTRY, BaseRawResultType.POI) to false,
                listOf(BaseRawResultType.ADDRESS, BaseRawResultType.POI) to false,
                listOf(BaseRawResultType.REGION, BaseRawResultType.ADDRESS, BaseRawResultType.POI) to false,
                emptyList<BaseRawResultType>() to false,
                listOf(BaseRawResultType.PLACE, BaseRawResultType.CATEGORY) to false,
                listOf(BaseRawResultType.UNKNOWN) to false,
                listOf(BaseRawResultType.CATEGORY) to false,
                listOf(BaseRawResultType.QUERY) to false,
                listOf(BaseRawResultType.USER_RECORD) to false,
            )

        val REQUEST_OPTIONS = createTestBaseRequestOptions(
            core = createTestCoreRequestOptions(query = "test-query"),
        )

        val BASE_RAW_SEARCH_RESULT = createTestBaseRawSearchResult(
            id = "Search result 1",
            names = listOf("Search result 1.1", "Search result 1.2"),
            descriptionAddress = "Search result 1 description",
            addresses = listOf(
                createBaseSearchAddress(
                    country = "Belarus",
                    region = "Minsk",
                    street = "Francyska Skaryny",
                    houseNumber = "1"
                )
            ),
            distanceMeters = 123.0,
            icon = "cafe",
            etaMinutes = 5.0,
            center = Point.fromLngLat(1.0, 1.0),
            types = listOf(BaseRawResultType.POI)
        )

        @Suppress("JVM_STATIC_IN_PRIVATE_COMPANION")
        @BeforeAll
        @JvmStatic
        fun setUpAll() {
            resetLogImpl()
            mockkStatic(TestConstants.ASSERTIONS_KT_CLASS_NAME)
        }

        @Suppress("JVM_STATIC_IN_PRIVATE_COMPANION")
        @AfterAll
        @JvmStatic
        fun tearDownAll() {
            reinitializeLogImpl()
            unmockkStatic(TestConstants.ASSERTIONS_KT_CLASS_NAME)
        }
    }
}
