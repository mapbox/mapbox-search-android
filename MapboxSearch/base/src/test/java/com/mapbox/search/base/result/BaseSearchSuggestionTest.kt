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

                        // TODO(NN-899) FIXME search native should parse "poi_category_ids" and provide it to platforms
//                        WhenThrows(
//                            "Trying ro instantiate ServerSearchSuggestion with raw type = $rawResultType and without canonical name",
//                            IllegalStateException::class
//                        ) {
//                            val searchResult = BASE_RAW_SEARCH_RESULT_1.copy(
//                                types = listOf(BaseRawResultType.CATEGORY),
//                                externalIDs = emptyMap()
//                            )
//                            BaseServerSearchSuggestion(searchResult, REQUEST_OPTIONS)
//                        }
                    }
                    BaseRawResultType.BRAND -> {
                        When("Trying ro instantiate ServerSearchSuggestion with raw type = $rawResultType") {
                            val searchResult = BASE_RAW_SEARCH_RESULT_1.copy(
                                types = listOf(BaseRawResultType.BRAND),
                                brandId = "test-brand-id",
                                brand = listOf("Test brand", "other name")
                            )

                            val suggestion = BaseServerSearchSuggestion(searchResult, REQUEST_OPTIONS)

                            val expectedType = BaseSearchSuggestionType.Brand("Test brand", "test-brand-id")

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
    fun `Check Brand SearchSuggestion-specific implementation`() = TestCase {
        Given("Base result with brand type") {
            WhenThrows("Trying to instantiate brand without brand id", IllegalStateException::class) {
                val searchResult = BASE_RAW_SEARCH_RESULT_1.copy(
                    types = listOf(BaseRawResultType.BRAND),
                    brand = listOf("test-brand"),
                    brandId = null,
                )
                BaseServerSearchSuggestion(searchResult, REQUEST_OPTIONS)
            }

            When("Trying to instantiate brand without brand name") {
                val searchResult = BASE_RAW_SEARCH_RESULT_1.copy(
                    names = listOf("test-name"),
                    types = listOf(BaseRawResultType.BRAND),
                    brand = null,
                    brandId = "test-brand-id",
                )

                val suggestion = BaseServerSearchSuggestion(searchResult, REQUEST_OPTIONS)
                Then(
                    "Suggestion type should be correct",
                    BaseSearchSuggestionType.Brand("test-name", "test-brand-id"),
                    suggestion.type
                )
            }

            When("Trying to instantiate brand with empty brand name") {
                val searchResult = BASE_RAW_SEARCH_RESULT_1.copy(
                    names = listOf("test-name"),
                    types = listOf(BaseRawResultType.BRAND),
                    brand = listOf(""),
                    brandId = "test-brand-id",
                )
                val suggestion = BaseServerSearchSuggestion(searchResult, REQUEST_OPTIONS)
                Then(
                    "Suggestion type should be correct",
                    BaseSearchSuggestionType.Brand("test-name", "test-brand-id"),
                    suggestion.type
                )
            }

            When("Trying to instantiate brand with at least one non empty brand name") {
                val searchResult = BASE_RAW_SEARCH_RESULT_1.copy(
                    types = listOf(BaseRawResultType.BRAND),
                    brand = listOf("", "test-brand"),
                    brandId = "test-brand-id",
                )
                val suggestion = BaseServerSearchSuggestion(searchResult, REQUEST_OPTIONS)
                Then(
                    "Suggestion type should be correct",
                    BaseSearchSuggestionType.Brand("test-brand", "test-brand-id"),
                    suggestion.type
                )
            }

            When("Trying to instantiate brand with all non empty brand names") {
                val searchResult = BASE_RAW_SEARCH_RESULT_1.copy(
                    types = listOf(BaseRawResultType.BRAND),
                    brand = listOf("test-brand-1", "test-brand-2"),
                    brandId = "test-brand-id",
                )
                val suggestion = BaseServerSearchSuggestion(searchResult, REQUEST_OPTIONS)
                Then(
                    "Suggestion type should be correct",
                    BaseSearchSuggestionType.Brand("test-brand-1", "test-brand-id"),
                    suggestion.type
                )
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
            }
        }
    }

    @TestFactory
    fun `Check GeocodingCompatSearchSuggestion-specific implementation`() = TestCase {
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
            types = listOf(BaseRawResultType.POI),
            names = listOf("Search result 1.1", "Search result 1.2"),
            addresses = listOf(
                createBaseSearchAddress(
                    country = "Belarus",
                    region = "Minsk",
                    street = "Francyska Skaryny",
                    houseNumber = "1"
                )
            ),
            descriptionAddress = "Search result 1 description",
            fullAddress = "Full formatted address",
            distanceMeters = 123.0,
            icon = "cafe",
            action = BaseSuggestAction(
                endpoint = "test-endpoint-1",
                path = "test-path-1",
                query = "test-query-1",
                body = null
            ),
            etaMinutes = 5.0
        )

        val BASE_RAW_SEARCH_RESULT_2 = createTestBaseRawSearchResult(
            id = "Search result 2",
            types = listOf(BaseRawResultType.CATEGORY),
            names = listOf("Search result 2.1", "Search result 2.2"),
            addresses = listOf(
                createBaseSearchAddress(
                    country = "Belarus",
                    region = "Grodno",
                    street = "Janki Kupaly",
                    houseNumber = "15"
                )
            ),
            descriptionAddress = "Search result 2 description",
            fullAddress = "Full formatted address 2",
            distanceMeters = 456.0,
            categories = listOf("bar"),
            icon = "bar",
            externalIDs = mapOf("federated" to "category.cafe"),
            action = BaseSuggestAction(
                endpoint = "test-endpoint-2",
                path = "test-path-2",
                query = "test-query-2",
                body = null
            ),
            etaMinutes = 10.0
        )

        val BASE_RAW_SEARCH_RESULT_3 = createTestBaseRawSearchResult(
            id = "Search result 3",
            types = listOf(BaseRawResultType.REGION, BaseRawResultType.PLACE),
            names = listOf("Search result 3.1", "Search result 3.2"),
            addresses = listOf(
                createBaseSearchAddress(
                    country = "Belarus",
                    region = "Vitebsk",
                    street = "Ephrasinya Polackaya",
                    houseNumber = "24"
                )
            ),
            descriptionAddress = "Search result 3 description",
            fullAddress = "Full formatted address 3",
            distanceMeters = 789.0,
            categories = emptyList(),
            icon = null,
            action = BaseSuggestAction(
                endpoint = "test-endpoint-3",
                path = "test-path-3",
                query = "test-query-3",
                body = null
            ),
            etaMinutes = 15.0
        )
    }
}
