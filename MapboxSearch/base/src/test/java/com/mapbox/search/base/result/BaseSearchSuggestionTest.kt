package com.mapbox.search.base.result

import com.mapbox.geojson.Point
import com.mapbox.search.base.record.BaseIndexableRecord
import com.mapbox.search.base.tests_support.TestParcelable
import com.mapbox.search.base.tests_support.createBaseSearchAddress
import com.mapbox.search.base.tests_support.createTestBaseRawSearchResult
import com.mapbox.search.base.tests_support.createTestBaseRequestOptions
import com.mapbox.search.base.tests_support.withPrefabTestBaseRawSearchResult
import com.mapbox.search.common.tests.createTestCoreRequestOptions
import com.mapbox.search.common.tests.withPrefabTestBoundingBox
import com.mapbox.search.common.tests.withPrefabTestPoint
import com.mapbox.test.dsl.TestCase
import io.mockk.every
import io.mockk.spyk
import nl.jqno.equalsverifier.EqualsVerifier
import nl.jqno.equalsverifier.Warning
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.TestFactory

@Suppress("LargeClass")
internal class BaseSearchSuggestionTest {

    @TestFactory
    fun `Check BaseSearchSuggestion property implementation`() = TestCase {
        Given("Two distinct core search results and corresponding Search suggestions") {

            val suggestion1 = BaseServerSearchSuggestion(BASE_RAW_SEARCH_RESULT_1, REQUEST_OPTIONS)
            val suggestion2 = BaseServerSearchSuggestion(BASE_RAW_SEARCH_RESULT_2, REQUEST_OPTIONS)

            When("Getting search suggestion properties") {
                Then("Suggestion id should be derived from Search result") {
                    assertEquals(BASE_RAW_SEARCH_RESULT_1.id, suggestion1.id)
                    assertEquals(BASE_RAW_SEARCH_RESULT_2.id, suggestion2.id)
                }

                Then("Suggestion name should be derived from Search result") {
                    assertEquals(BASE_RAW_SEARCH_RESULT_1.names.first(), suggestion1.name)
                    assertEquals(BASE_RAW_SEARCH_RESULT_2.names.first(), suggestion2.name)
                }

                Then("Suggestion descriptionText should be derived from Search result") {
                    assertEquals(BASE_RAW_SEARCH_RESULT_1.descriptionAddress, suggestion1.descriptionText)
                    assertEquals(BASE_RAW_SEARCH_RESULT_2.descriptionAddress, suggestion2.descriptionText)
                }

                Then("Suggestion address should be derived from Search result") {
                    assertEquals(BASE_RAW_SEARCH_RESULT_1.addresses?.firstOrNull(), suggestion1.address)
                    assertEquals(BASE_RAW_SEARCH_RESULT_2.addresses?.firstOrNull(), suggestion2.address)
                }

                Then("Suggestion full address should be derived from Search result") {
                    assertEquals(BASE_RAW_SEARCH_RESULT_1.fullAddress, suggestion1.fullAddress)
                    assertEquals(BASE_RAW_SEARCH_RESULT_2.fullAddress, suggestion2.fullAddress)
                }

                Then("Suggestion distanceMeters should be derived from Search result") {
                    assertEquals(BASE_RAW_SEARCH_RESULT_1.distanceMeters, suggestion1.distanceMeters)
                    assertEquals(BASE_RAW_SEARCH_RESULT_2.distanceMeters, suggestion2.distanceMeters)
                }

                Then("Suggestion categories should be derived from Search result") {
                    assertEquals(BASE_RAW_SEARCH_RESULT_1.categories, suggestion1.categories)
                    assertEquals(BASE_RAW_SEARCH_RESULT_2.categories, suggestion2.categories)
                }

                Then("Suggestion makiIcon should be derived from Search result") {
                    assertEquals(BASE_RAW_SEARCH_RESULT_1.icon, suggestion1.makiIcon)
                    assertEquals(BASE_RAW_SEARCH_RESULT_2.icon, suggestion2.makiIcon)
                }

                Then("Suggestion etaMinutes should be derived from Search result") {
                    assertEquals(BASE_RAW_SEARCH_RESULT_1.etaMinutes, suggestion1.etaMinutes)
                    assertEquals(BASE_RAW_SEARCH_RESULT_2.etaMinutes, suggestion2.etaMinutes)
                }

                Then("Suggestion isBatchResolveSupported value should be derived from Search result") {
                    assertEquals(BASE_RAW_SEARCH_RESULT_1.action?.multiRetrievable, suggestion1.isBatchResolveSupported)
                    assertEquals(BASE_RAW_SEARCH_RESULT_2.action?.multiRetrievable, suggestion2.isBatchResolveSupported)
                }
            }
        }
    }

    @TestFactory
    fun `Check ServerSearchSuggestion-specific implementation`() = TestCase {
        Given("Core search result without action") {
            val searchResult = BASE_RAW_SEARCH_RESULT_1.copy(action = null)

            WhenThrows(
                "Trying to instantiate ServerSearchSuggestion from backend and with action == null",
                IllegalStateException::class
            ) {
                BaseServerSearchSuggestion(searchResult, REQUEST_OPTIONS)
            }
        }

        Given("Core search result without types") {
            val searchResult = spyk(BASE_RAW_SEARCH_RESULT_1)
            every { searchResult.types } returns emptyList()

            WhenThrows(
                "Trying to instantiate ServerSearchSuggestion with illegal core search result",
                IllegalStateException::class
            ) {
                BaseServerSearchSuggestion(searchResult, REQUEST_OPTIONS)
            }
        }

        Given("All the possible BaseRawResultType types") {
            BaseRawResultType.values().forEach { rawResultType ->
                when (rawResultType) {
                    BaseRawResultType.ADDRESS,
                    BaseRawResultType.POI,
                    BaseRawResultType.COUNTRY,
                    BaseRawResultType.REGION,
                    BaseRawResultType.PLACE,
                    BaseRawResultType.DISTRICT,
                    BaseRawResultType.LOCALITY,
                    BaseRawResultType.NEIGHBORHOOD,
                    BaseRawResultType.STREET,
                    BaseRawResultType.POSTCODE,
                    BaseRawResultType.BLOCK -> {
                        When("Trying ro instantiate ServerSearchSuggestion with raw type = $rawResultType") {
                            val expectedResultType = checkNotNull(rawResultType.tryMapToSearchResultType()) {
                                "$rawResultType must have corresponding ${BaseSearchResultType::class.java.simpleName}"
                            }

                            val searchResult = BASE_RAW_SEARCH_RESULT_1.copy(
                                types = listOf(rawResultType)
                            )

                            val suggestion = BaseServerSearchSuggestion(searchResult, REQUEST_OPTIONS)
                            val expectedType = BaseSearchSuggestionType.SearchResultSuggestion(expectedResultType)

                            Then("Suggestion type should be $expectedType", expectedType, suggestion.type)
                        }
                    }
                    BaseRawResultType.CATEGORY -> {
                        When("Trying ro instantiate ServerSearchSuggestion with raw type = $rawResultType and with canonical name specified in external IDs") {
                            val searchResult = BASE_RAW_SEARCH_RESULT_1.copy(
                                types = listOf(BaseRawResultType.CATEGORY),
                                externalIDs = mapOf("federated" to "category.cafe")
                            )

                            val suggestion = BaseServerSearchSuggestion(searchResult, REQUEST_OPTIONS)

                            val expectedType = BaseSearchSuggestionType.Category("cafe")

                            Then("Suggestion type should be $expectedType", expectedType, suggestion.type)
                        }

                        WhenThrows(
                            "Trying ro instantiate ServerSearchSuggestion with raw type = $rawResultType and without canonical name",
                            IllegalStateException::class
                        ) {
                            val searchResult = BASE_RAW_SEARCH_RESULT_1.copy(
                                types = listOf(BaseRawResultType.CATEGORY),
                                externalIDs = emptyMap()
                            )
                            BaseServerSearchSuggestion(searchResult, REQUEST_OPTIONS)
                        }
                    }
                    BaseRawResultType.BRAND -> {
                        When("Trying ro instantiate ServerSearchSuggestion with raw type = $rawResultType") {
                            val searchResult = BASE_RAW_SEARCH_RESULT_1.copy(
                                types = listOf(BaseRawResultType.BRAND),
                            )

                            val suggestion = BaseServerSearchSuggestion(searchResult, REQUEST_OPTIONS)

                            val expectedType = BaseSearchSuggestionType.Brand("", "")

                            Then("Suggestion type should be $expectedType", expectedType, suggestion.type)
                        }
                    }
                    BaseRawResultType.QUERY -> {
                        When("Trying ro instantiate ServerSearchSuggestion with raw type = $rawResultType") {
                            val searchResult = BASE_RAW_SEARCH_RESULT_1.copy(
                                types = listOf(BaseRawResultType.QUERY)
                            )

                            val suggestion = BaseServerSearchSuggestion(searchResult, REQUEST_OPTIONS)

                            val expectedType = BaseSearchSuggestionType.Query

                            Then("Suggestion type should be $expectedType", expectedType, suggestion.type)
                        }
                    }
                    BaseRawResultType.USER_RECORD,
                    BaseRawResultType.UNKNOWN -> {
                        WhenThrows(
                            "Trying ro instantiate ServerSearchSuggestion with raw type = $rawResultType",
                            IllegalStateException::class
                        ) {
                            val searchResult = BASE_RAW_SEARCH_RESULT_1.copy(
                                types = listOf(rawResultType)
                            )

                            BaseServerSearchSuggestion(searchResult, REQUEST_OPTIONS)
                        }
                    }
                }
            }

            val multipleTypes = listOf(BaseRawResultType.PLACE, BaseRawResultType.REGION)
            When("Trying ro instantiate ServerSearchSuggestion with multiple types: $multipleTypes") {
                val searchResult = BASE_RAW_SEARCH_RESULT_3.copy(
                    types = multipleTypes,
                )
                val suggestion = BaseServerSearchSuggestion(searchResult, REQUEST_OPTIONS)
                val expectedType = BaseSearchSuggestionType.SearchResultSuggestion(
                    BaseSearchResultType.PLACE, BaseSearchResultType.REGION
                )

                Then("Suggestion type should be $expectedType", expectedType, suggestion.type)
            }
        }
    }

    @TestFactory
    fun `Check IndexableRecordSearchSuggestion-specific implementation`() = TestCase {
        Given("Core search result with illegal type") {
            val searchResult = BASE_RAW_SEARCH_RESULT_1.copy(
                types = listOf(BaseRawResultType.POI)
            )

            WhenThrows(
                "Trying to instantiate IndexableRecordSearchSuggestion with illegal core search result",
                IllegalStateException::class
            ) {
                BaseIndexableRecordSearchSuggestion(TEST_RECORD, searchResult, REQUEST_OPTIONS)
            }
        }

        Given("Core search result without 'layerId'") {
            val searchResult = BASE_RAW_SEARCH_RESULT_1.copy(
                types = listOf(BaseRawResultType.USER_RECORD),
                layerId = null
            )

            WhenThrows(
                "Trying to instantiate IndexableRecordSearchSuggestion with illegal core search result",
                IllegalStateException::class
            ) {
                BaseIndexableRecordSearchSuggestion(TEST_RECORD, searchResult, REQUEST_OPTIONS)
            }
        }

        Given("Correct Core search result representing `USER_RECORD`") {
            val searchResult = BASE_RAW_SEARCH_RESULT_1.copy(
                action = null,
                types = listOf(BaseRawResultType.USER_RECORD),
                layerId = "test-layer-id",
                id = "generic-id",
                userRecordId = "test-user-record-id"
            )

            val suggestion = BaseIndexableRecordSearchSuggestion(TEST_RECORD, searchResult, REQUEST_OPTIONS)

            When("Getting IndexableRecordItem properties") {
                Then(
                    "Suggestion id value must be based on search result's `userRecordId`",
                    searchResult.userRecordId,
                    suggestion.id
                )

                Then(
                    "Suggestion type value should be derived from core search result",
                    BaseSearchSuggestionType.IndexableRecordItem(TEST_RECORD, searchResult.layerId!!),
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
            val searchResult = BASE_RAW_SEARCH_RESULT_1.copy(
                action = null,
                center = Point.fromLngLat(10.123, 50.456),
                types = listOf(BaseRawResultType.POI)
            )

            val suggestion = BaseGeocodingCompatSearchSuggestion(searchResult, REQUEST_OPTIONS)

            When("Getting GeocodingCompatSearchSuggestion properties") {
                Then("'isBatchResolveSupported' is always true for GeocodingCompatSearchSuggestion") {
                    assertTrue(suggestion.isBatchResolveSupported)
                }
            }
        }

        Given("All the possible BaseRawResultType types") {
            BaseRawResultType.values().forEach { rawResultType ->
                when (rawResultType) {
                    BaseRawResultType.ADDRESS,
                    BaseRawResultType.POI,
                    BaseRawResultType.COUNTRY,
                    BaseRawResultType.REGION,
                    BaseRawResultType.PLACE,
                    BaseRawResultType.DISTRICT,
                    BaseRawResultType.LOCALITY,
                    BaseRawResultType.NEIGHBORHOOD,
                    BaseRawResultType.STREET,
                    BaseRawResultType.POSTCODE,
                    BaseRawResultType.BLOCK -> {
                        When("Trying ro instantiate GeocodingCompatSearchSuggestion with raw type = $rawResultType") {
                            val expectedResultType = checkNotNull(rawResultType.tryMapToSearchResultType()) {
                                "$rawResultType must have corresponding ${BaseSearchResultType::class.java.simpleName}"
                            }

                            val searchResult = BASE_RAW_SEARCH_RESULT_1.copy(
                                action = null,
                                center = Point.fromLngLat(10.123, 50.456),
                                types = listOf(rawResultType)
                            )

                            val suggestion = BaseGeocodingCompatSearchSuggestion(searchResult, REQUEST_OPTIONS)
                            val expectedType = BaseSearchSuggestionType.SearchResultSuggestion(expectedResultType)

                            Then("Suggestion type should be $expectedType", expectedType, suggestion.type)
                        }
                    }
                    BaseRawResultType.CATEGORY,
                    BaseRawResultType.BRAND,
                    BaseRawResultType.QUERY,
                    BaseRawResultType.USER_RECORD,
                    BaseRawResultType.UNKNOWN -> {
                        WhenThrows(
                            "Trying ro instantiate GeocodingCompatSearchSuggestion with raw type = $rawResultType",
                            IllegalStateException::class
                        ) {
                            val searchResult = BASE_RAW_SEARCH_RESULT_1.copy(
                                action = null,
                                center = Point.fromLngLat(10.123, 50.456),
                                types = listOf(rawResultType)
                            )

                            BaseGeocodingCompatSearchSuggestion(searchResult, REQUEST_OPTIONS)
                        }
                    }
                }
            }
        }
    }

    @TestFactory
    fun `Check ServerSearchSuggestion equals-hashCode functions`() = TestCase {
        Given("ServerSearchSuggestion class") {
            When("equals() and hashCode() called") {
                Then("equals() and hashCode() functions should use every declared property") {
                    EqualsVerifier.forClass(BaseServerSearchSuggestion::class.java)
                        .withPrefabTestPoint()
                        .withPrefabTestBoundingBox()
                        .withPrefabTestBaseRawSearchResult()
                        .withIgnoredFields("type")
                        .withNonnullFields("rawSearchResult", "requestOptions")
                        // TODO(#777) EqualsVerifier check fails on overridden from superclass properties
                        .suppress(Warning.ALL_FIELDS_SHOULD_BE_USED)
                        .verify()
                }
            }
        }
    }

    @TestFactory
    fun `Check IndexableRecordSearchSuggestion equals-hashCode functions`() = TestCase {
        Given("IndexableRecordSearchSuggestion class") {
            When("equals() and hashCode() called") {
                Then("equals() and hashCode() functions should use every declared property") {
                    EqualsVerifier.forClass(BaseIndexableRecordSearchSuggestion::class.java)
                        .withPrefabTestPoint()
                        .withPrefabTestBoundingBox()
                        .withPrefabTestBaseRawSearchResult()
                        // TODO(#777) EqualsVerifier check fails on overridden from superclass properties
                        .suppress(Warning.ALL_FIELDS_SHOULD_BE_USED)
                        .verify()
                }
            }
        }
    }

    private companion object {

        val REQUEST_OPTIONS = createTestBaseRequestOptions(
            core = createTestCoreRequestOptions(query = "Test query"),
        )

        val TEST_RECORD = BaseIndexableRecord(
            id = "test id",
            name = "test name",
            coordinate = Point.fromLngLat(.0, .1),
            descriptionText = null,
            address = null,
            type = BaseSearchResultType.POI,
            routablePoints = null,
            metadata = null,
            makiIcon = null,
            categories = emptyList(),
            indexTokens = emptyList(),
            sdkResolvedRecord = TestParcelable("test record")
        )

        val BASE_RAW_SEARCH_RESULT_1 = createTestBaseRawSearchResult(
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
            fullAddress = "Full formatted address",
            distanceMeters = 123.0,
            icon = "cafe",
            etaMinutes = 5.0,
            types = listOf(BaseRawResultType.POI),
            action = BaseSuggestAction(
                endpoint = "test-endpoint-1",
                path = "test-path-1",
                query = "test-query-1",
                body = null,
                multiRetrievable = true
            )
        )

        val BASE_RAW_SEARCH_RESULT_2 = createTestBaseRawSearchResult(
            id = "Search result 2",
            names = listOf("Search result 2.1", "Search result 2.2"),
            descriptionAddress = "Search result 2 description",
            addresses = listOf(
                createBaseSearchAddress(
                    country = "Belarus",
                    region = "Grodno",
                    street = "Janki Kupaly",
                    houseNumber = "15"
                )
            ),
            fullAddress = "Full formatted address 2",
            distanceMeters = 456.0,
            icon = "bar",
            etaMinutes = 10.0,
            types = listOf(BaseRawResultType.CATEGORY),
            externalIDs = mapOf("federated" to "category.cafe"),
            categories = listOf("bar"),
            action = BaseSuggestAction(
                endpoint = "test-endpoint-2",
                path = "test-path-2",
                query = "test-query-2",
                body = null,
                multiRetrievable = false
            )
        )

        val BASE_RAW_SEARCH_RESULT_3 = createTestBaseRawSearchResult(
            id = "Search result 3",
            names = listOf("Search result 3.1", "Search result 3.2"),
            descriptionAddress = "Search result 3 description",
            addresses = listOf(
                createBaseSearchAddress(
                    country = "Belarus",
                    region = "Vitebsk",
                    street = "Ephrasinya Polackaya",
                    houseNumber = "24"
                )
            ),
            fullAddress = "Full formatted address 3",
            distanceMeters = 789.0,
            icon = null,
            etaMinutes = 15.0,
            types = listOf(BaseRawResultType.REGION, BaseRawResultType.PLACE),
            categories = emptyList(),
            action = BaseSuggestAction(
                endpoint = "test-endpoint-3",
                path = "test-path-3",
                query = "test-query-3",
                body = null,
                multiRetrievable = false
            )
        )
    }
}
