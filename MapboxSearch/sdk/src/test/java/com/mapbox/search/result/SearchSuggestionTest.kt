package com.mapbox.search.result

import com.mapbox.geojson.Point
import com.mapbox.search.common.tests.CustomTypeObjectCreatorImpl
import com.mapbox.search.common.tests.ReflectionObjectsFactory
import com.mapbox.search.common.tests.ToStringVerifier
import com.mapbox.search.record.FavoriteRecord
import com.mapbox.search.tests_support.SdkCustomTypeObjectCreators
import com.mapbox.search.tests_support.catchThrowable
import com.mapbox.search.tests_support.createTestOriginalSearchResult
import com.mapbox.search.tests_support.createTestRequestOptions
import com.mapbox.search.tests_support.withPrefabTestBoundingBox
import com.mapbox.search.tests_support.withPrefabTestOriginalSearchResult
import com.mapbox.search.tests_support.withPrefabTestPoint
import com.mapbox.test.dsl.TestCase
import io.mockk.every
import io.mockk.spyk
import nl.jqno.equalsverifier.EqualsVerifier
import nl.jqno.equalsverifier.Warning
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.jupiter.api.TestFactory

@Suppress("LargeClass")
@ExperimentalStdlibApi
internal class SearchSuggestionTest {

    @TestFactory
    fun `Check BaseSearchSuggestion property implementation`() = TestCase {
        Given("Two distinct core search results and corresponding Search suggestions") {

            val suggestion1 = ServerSearchSuggestion(ORIGINAL_SEARCH_RESULT_1, REQUEST_OPTIONS)
            val suggestion2 = ServerSearchSuggestion(ORIGINAL_SEARCH_RESULT_2, REQUEST_OPTIONS)

            When("Getting search suggestion properties") {
                Then("Suggestion id should be derived from Search result") {
                    assertEquals(ORIGINAL_SEARCH_RESULT_1.id, suggestion1.id)
                    assertEquals(ORIGINAL_SEARCH_RESULT_2.id, suggestion2.id)
                }

                Then("Suggestion name should be derived from Search result") {
                    assertEquals(ORIGINAL_SEARCH_RESULT_1.names.first(), suggestion1.name)
                    assertEquals(ORIGINAL_SEARCH_RESULT_2.names.first(), suggestion2.name)
                }

                Then("Suggestion descriptionText should be derived from Search result") {
                    assertEquals(ORIGINAL_SEARCH_RESULT_1.descriptionAddress, suggestion1.descriptionText)
                    assertEquals(ORIGINAL_SEARCH_RESULT_2.descriptionAddress, suggestion2.descriptionText)
                }

                Then("Suggestion address should be derived from Search result") {
                    assertEquals(ORIGINAL_SEARCH_RESULT_1.addresses?.firstOrNull(), suggestion1.address)
                    assertEquals(ORIGINAL_SEARCH_RESULT_2.addresses?.firstOrNull(), suggestion2.address)
                }

                Then("Suggestion distanceMeters should be derived from Search result") {
                    assertEquals(ORIGINAL_SEARCH_RESULT_1.distanceMeters, suggestion1.distanceMeters)
                    assertEquals(ORIGINAL_SEARCH_RESULT_2.distanceMeters, suggestion2.distanceMeters)
                }

                Then("Suggestion categories should be derived from Search result") {
                    assertEquals(ORIGINAL_SEARCH_RESULT_1.categories ?: emptyList<String>(), suggestion1.categories)
                    assertEquals(ORIGINAL_SEARCH_RESULT_2.categories ?: emptyList<String>(), suggestion2.categories)
                }

                Then("Suggestion makiIcon should be derived from Search result") {
                    assertEquals(ORIGINAL_SEARCH_RESULT_1.icon, suggestion1.makiIcon)
                    assertEquals(ORIGINAL_SEARCH_RESULT_2.icon, suggestion2.makiIcon)
                }

                Then("Suggestion etaMinutes should be derived from Search result") {
                    assertEquals(ORIGINAL_SEARCH_RESULT_1.etaMinutes, suggestion1.etaMinutes)
                    assertEquals(ORIGINAL_SEARCH_RESULT_2.etaMinutes, suggestion2.etaMinutes)
                }

                Then("Suggestion isBatchResolveSupported value should be derived from Search result") {
                    assertEquals(ORIGINAL_SEARCH_RESULT_1.action?.multiRetrievable, suggestion1.isBatchResolveSupported)
                    assertEquals(ORIGINAL_SEARCH_RESULT_2.action?.multiRetrievable, suggestion2.isBatchResolveSupported)
                }
            }
        }
    }

    @TestFactory
    fun `Check ServerSearchSuggestion-specific implementation`() = TestCase {
        Given("Core search result without action") {
            val searchResult = ORIGINAL_SEARCH_RESULT_1.copy(action = null)

            WhenThrows(
                "Trying to instantiate ServerSearchSuggestion from backend and with action == null",
                IllegalStateException::class
            ) {
                ServerSearchSuggestion(searchResult, REQUEST_OPTIONS, isFromOffline = false)
            }

            When("Trying to instantiate ServerSearchSuggestion from offline engine and with action == null") {
                val t = catchThrowable<Throwable> {
                    ServerSearchSuggestion(searchResult, REQUEST_OPTIONS, isFromOffline = true)
                }

                Then("ServerSearchSuggestion should be created", null, t)
            }
        }

        Given("Core search result without types") {
            val searchResult = spyk(ORIGINAL_SEARCH_RESULT_1)
            every { searchResult.types } returns emptyList()

            WhenThrows(
                "Trying to instantiate ServerSearchSuggestion with illegal core search result",
                IllegalStateException::class
            ) {
                ServerSearchSuggestion(searchResult, REQUEST_OPTIONS)
            }
        }

        Given("All the possible OriginalResultType types") {
            OriginalResultType.values().forEach { originalType ->
                when (originalType) {
                    OriginalResultType.ADDRESS,
                    OriginalResultType.POI,
                    OriginalResultType.COUNTRY,
                    OriginalResultType.REGION,
                    OriginalResultType.PLACE,
                    OriginalResultType.DISTRICT,
                    OriginalResultType.LOCALITY,
                    OriginalResultType.NEIGHBORHOOD,
                    OriginalResultType.STREET,
                    OriginalResultType.POSTCODE -> {
                        When("Trying ro instantiate ServerSearchSuggestion with original type = $originalType") {
                            val expectedResultType = checkNotNull(originalType.tryMapToSearchResultType()) {
                                "$originalType must have corresponding ${SearchResultType::class.java.simpleName}"
                            }

                            val searchResult = ORIGINAL_SEARCH_RESULT_1.copy(
                                types = listOf(originalType)
                            )

                            val suggestion = ServerSearchSuggestion(searchResult, REQUEST_OPTIONS)
                            val expectedType = SearchSuggestionType.SearchResultSuggestion(expectedResultType)

                            Then("Suggestion type should be $expectedType", expectedType, suggestion.type)
                        }
                    }
                    OriginalResultType.CATEGORY -> {
                        When("Trying ro instantiate ServerSearchSuggestion with original type = $originalType and with canonical name specified in external IDs") {
                            val searchResult = ORIGINAL_SEARCH_RESULT_1.copy(
                                types = listOf(OriginalResultType.CATEGORY),
                                externalIDs = mapOf("federated" to "category.cafe")
                            )

                            val suggestion = ServerSearchSuggestion(searchResult, REQUEST_OPTIONS)

                            val expectedType = SearchSuggestionType.Category("cafe")

                            Then("Suggestion type should be $expectedType", expectedType, suggestion.type)
                        }

                        WhenThrows(
                            "Trying ro instantiate ServerSearchSuggestion with original type = $originalType and without canonical name",
                            IllegalStateException::class
                        ) {
                            val searchResult = ORIGINAL_SEARCH_RESULT_1.copy(
                                types = listOf(OriginalResultType.CATEGORY),
                                externalIDs = emptyMap()
                            )
                            ServerSearchSuggestion(searchResult, REQUEST_OPTIONS)
                        }
                    }
                    OriginalResultType.QUERY -> {
                        When("Trying ro instantiate ServerSearchSuggestion with original type = $originalType") {
                            val searchResult = ORIGINAL_SEARCH_RESULT_1.copy(
                                types = listOf(OriginalResultType.QUERY)
                            )

                            val suggestion = ServerSearchSuggestion(searchResult, REQUEST_OPTIONS)

                            val expectedType = SearchSuggestionType.Query

                            Then("Suggestion type should be $expectedType", expectedType, suggestion.type)
                        }
                    }
                    OriginalResultType.USER_RECORD,
                    OriginalResultType.UNKNOWN -> {
                        WhenThrows(
                            "Trying ro instantiate ServerSearchSuggestion with original type = $originalType",
                            IllegalStateException::class
                        ) {
                            val searchResult = ORIGINAL_SEARCH_RESULT_1.copy(
                                types = listOf(originalType)
                            )

                            ServerSearchSuggestion(searchResult, REQUEST_OPTIONS)
                        }
                    }
                }
            }

            val multipleTypes = listOf(OriginalResultType.PLACE, OriginalResultType.REGION)
            When("Trying ro instantiate ServerSearchSuggestion with multiple types: $multipleTypes") {
                val searchResult = ORIGINAL_SEARCH_RESULT_3.copy(
                    types = multipleTypes,
                )
                val suggestion = ServerSearchSuggestion(searchResult, REQUEST_OPTIONS)
                val expectedType = SearchSuggestionType.SearchResultSuggestion(
                    SearchResultType.PLACE, SearchResultType.REGION
                )

                Then("Suggestion type should be $expectedType", expectedType, suggestion.type)
            }
        }
    }

    @TestFactory
    fun `Check IndexableRecordSearchSuggestion-specific implementation`() = TestCase {
        Given("Core search result with illegal type") {
            val searchResult = ORIGINAL_SEARCH_RESULT_1.copy(
                types = listOf(OriginalResultType.POI)
            )

            WhenThrows(
                "Trying to instantiate IndexableRecordSearchSuggestion with illegal core search result",
                IllegalStateException::class
            ) {
                IndexableRecordSearchSuggestion(TEST_RECORD, searchResult, REQUEST_OPTIONS)
            }
        }

        Given("Core search result without 'layerId'") {
            val searchResult = ORIGINAL_SEARCH_RESULT_1.copy(
                types = listOf(OriginalResultType.USER_RECORD),
                layerId = null
            )

            WhenThrows(
                "Trying to instantiate IndexableRecordSearchSuggestion with illegal core search result",
                IllegalStateException::class
            ) {
                IndexableRecordSearchSuggestion(TEST_RECORD, searchResult, REQUEST_OPTIONS)
            }
        }

        Given("Correct Core search result representing `USER_RECORD`") {
            val searchResult = ORIGINAL_SEARCH_RESULT_1.copy(
                action = null,
                types = listOf(OriginalResultType.USER_RECORD),
                layerId = "test-layer-id",
                id = "generic-id",
                userRecordId = "test-user-record-id"
            )

            val suggestion = IndexableRecordSearchSuggestion(TEST_RECORD, searchResult, REQUEST_OPTIONS)

            When("Getting IndexableRecordItem properties") {
                Then(
                    "Suggestion id value must be based on search result's `userRecordId`",
                    searchResult.userRecordId,
                    suggestion.id
                )

                Then(
                    "Suggestion type value should be derived from core search result",
                    SearchSuggestionType.IndexableRecordItem(searchResult.layerId!!, TEST_RECORD.type),
                    suggestion.type
                )

                Then(
                    "'isBatchResolveSupported' is always true for IndexableRecordItem",
                    true,
                    suggestion.isBatchResolveSupported
                )
            }
        }
    }

    @TestFactory
    fun `Check GeocodingCompatSearchSuggestion-specific implementation`() = TestCase {
        Given("Correct Core search result representing Geocoding search suggestion") {
            val searchResult = ORIGINAL_SEARCH_RESULT_1.copy(
                action = null,
                center = Point.fromLngLat(10.123, 50.456),
                types = listOf(OriginalResultType.POI)
            )

            val suggestion = GeocodingCompatSearchSuggestion(searchResult, REQUEST_OPTIONS)

            When("Getting GeocodingCompatSearchSuggestion properties") {
                Then("'isBatchResolveSupported' is always true for GeocodingCompatSearchSuggestion") {
                    assertTrue(suggestion.isBatchResolveSupported)
                }
            }
        }

        Given("All the possible OriginalResultType types") {
            OriginalResultType.values().forEach { originalType ->
                when (originalType) {
                    OriginalResultType.ADDRESS,
                    OriginalResultType.POI,
                    OriginalResultType.COUNTRY,
                    OriginalResultType.REGION,
                    OriginalResultType.PLACE,
                    OriginalResultType.DISTRICT,
                    OriginalResultType.LOCALITY,
                    OriginalResultType.NEIGHBORHOOD,
                    OriginalResultType.STREET,
                    OriginalResultType.POSTCODE -> {
                        When("Trying ro instantiate GeocodingCompatSearchSuggestion with original type = $originalType") {
                            val expectedResultType = checkNotNull(originalType.tryMapToSearchResultType()) {
                                "$originalType must have corresponding ${SearchResultType::class.java.simpleName}"
                            }

                            val searchResult = ORIGINAL_SEARCH_RESULT_1.copy(
                                action = null,
                                center = Point.fromLngLat(10.123, 50.456),
                                types = listOf(originalType)
                            )

                            val suggestion = GeocodingCompatSearchSuggestion(searchResult, REQUEST_OPTIONS)
                            val expectedType = SearchSuggestionType.SearchResultSuggestion(expectedResultType)

                            Then("Suggestion type should be $expectedType", expectedType, suggestion.type)
                        }
                    }
                    OriginalResultType.CATEGORY,
                    OriginalResultType.QUERY,
                    OriginalResultType.USER_RECORD,
                    OriginalResultType.UNKNOWN -> {
                        WhenThrows(
                            "Trying ro instantiate GeocodingCompatSearchSuggestion with original type = $originalType",
                            IllegalStateException::class
                        ) {
                            val searchResult = ORIGINAL_SEARCH_RESULT_1.copy(
                                action = null,
                                center = Point.fromLngLat(10.123, 50.456),
                                types = listOf(originalType)
                            )

                            GeocodingCompatSearchSuggestion(searchResult, REQUEST_OPTIONS)
                        }
                    }
                }
            }
        }
    }

    @TestFactory
    fun `Check ServerSearchSuggestion equals-hashCode-toString functions`() = TestCase {
        Given("ServerSearchSuggestion class") {
            When("equals() and hashCode() called") {
                Then("equals() and hashCode() functions should use every declared property") {
                    EqualsVerifier.forClass(ServerSearchSuggestion::class.java)
                        .withPrefabTestPoint()
                        .withPrefabTestBoundingBox()
                        .withPrefabTestOriginalSearchResult()
                        .withIgnoredFields("type")
                        .withNonnullFields("originalSearchResult", "requestOptions", "isFromOffline")
                        // TODO(#777) EqualsVerifier check fails on overridden from superclass properties
                        .suppress(Warning.ALL_FIELDS_SHOULD_BE_USED)
                        .verify()
                }
            }

            When("toString() called") {
                Then("toString() function should include every declared property") {
                    ToStringVerifier(
                        clazz = ServerSearchSuggestion::class,
                        ignoredProperties = listOf("isFromOffline", "originalSearchResult"),
                        objectsFactory = ReflectionObjectsFactory(
                            extraCreators = SdkCustomTypeObjectCreators.ALL_CREATORS,
                        )
                    ).verify()
                }
            }
        }
    }

    @TestFactory
    fun `Check IndexableRecordSearchSuggestion equals-hashCode-toString functions`() = TestCase {
        Given("IndexableRecordSearchSuggestion class") {
            When("equals() and hashCode() called") {
                Then("equals() and hashCode() functions should use every declared property") {
                    EqualsVerifier.forClass(IndexableRecordSearchSuggestion::class.java)
                        .withPrefabTestPoint()
                        .withPrefabTestBoundingBox()
                        .withPrefabTestOriginalSearchResult()
                        // TODO(#777) EqualsVerifier check fails on overridden from superclass properties
                        .suppress(Warning.ALL_FIELDS_SHOULD_BE_USED)
                        .verify()
                }
            }

            When("toString() called") {
                Then("toString() function should include every declared property") {
                    val customTypeObjectCreator = CustomTypeObjectCreatorImpl(
                        clazz = OriginalSearchResult::class,
                        factory = { mode ->
                            listOf(
                                createTestOriginalSearchResult(
                                    id = "test-result-1",
                                    types = listOf(OriginalResultType.USER_RECORD),
                                    layerId = "test-layer-id-1"
                                ),
                                createTestOriginalSearchResult(
                                    id = "test-result-2",
                                    types = listOf(OriginalResultType.USER_RECORD),
                                    layerId = "test-layer-id-2"
                                ),
                            )[mode.ordinal]
                        }
                    )

                    ToStringVerifier(
                        clazz = IndexableRecordSearchSuggestion::class,
                        ignoredProperties = listOf("originalSearchResult"),
                        objectsFactory = ReflectionObjectsFactory(
                            extraCreators = listOf(customTypeObjectCreator) + SdkCustomTypeObjectCreators.ALL_CREATORS,
                        ),
                    ).verify()
                }
            }
        }
    }

    private companion object {

        val REQUEST_OPTIONS = createTestRequestOptions("Test query")

        val TEST_RECORD = FavoriteRecord(
            id = "Test id",
            name = "Test Local Favorite Name",
            coordinate = Point.fromLngLat(2.294423282146454, 48.85825817805569),
            descriptionText = "Test description text",
            address = SearchAddress(
                houseNumber = "22",
                street = "Baker street"
            ),
            type = SearchResultType.POI,
            makiIcon = null,
            categories = emptyList(),
            routablePoints = null,
            metadata = null
        )

        val ORIGINAL_SEARCH_RESULT_1 = createTestOriginalSearchResult(
            id = "Search result 1",
            names = listOf("Search result 1.1", "Search result 1.2"),
            descriptionAddress = "Search result 1 description",
            addresses = listOf(SearchAddress(country = "Belarus", region = "Minsk", street = "Francyska Skaryny", houseNumber = "1")),
            distanceMeters = 123.0,
            icon = "cafe",
            etaMinutes = 5.0,
            types = listOf(OriginalResultType.POI),
            action = SearchResultSuggestAction(endpoint = "test-endpoint-1", path = "test-path-1", query = "test-query-1", body = null, multiRetrievable = true)
        )

        val ORIGINAL_SEARCH_RESULT_2 = createTestOriginalSearchResult(
            id = "Search result 2",
            names = listOf("Search result 2.1", "Search result 2.2"),
            descriptionAddress = "Search result 2 description",
            addresses = listOf(SearchAddress(country = "Belarus", region = "Grodno", street = "Janki Kupaly", houseNumber = "15")),
            distanceMeters = 456.0,
            icon = "bar",
            etaMinutes = 10.0,
            types = listOf(OriginalResultType.CATEGORY),
            externalIDs = mapOf("federated" to "category.cafe"),
            categories = listOf("bar"),
            action = SearchResultSuggestAction(endpoint = "test-endpoint-2", path = "test-path-2", query = "test-query-2", body = null, multiRetrievable = false)
        )

        val ORIGINAL_SEARCH_RESULT_3 = createTestOriginalSearchResult(
            id = "Search result 3",
            names = listOf("Search result 3.1", "Search result 3.2"),
            descriptionAddress = "Search result 3 description",
            addresses = listOf(SearchAddress(country = "Belarus", region = "Vitebsk", street = "Ephrasinya Polackaya", houseNumber = "24")),
            distanceMeters = 789.0,
            icon = null,
            etaMinutes = 15.0,
            types = listOf(OriginalResultType.REGION, OriginalResultType.PLACE),
            categories = emptyList(),
            action = SearchResultSuggestAction(endpoint = "test-endpoint-3", path = "test-path-3", query = "test-query-3", body = null, multiRetrievable = false)
        )
    }
}
