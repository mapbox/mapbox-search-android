package com.mapbox.search.result

import com.mapbox.geojson.Point
import com.mapbox.search.BuildConfig
import com.mapbox.search.TestConstants.ASSERTIONS_KT_CLASS_NAME
import com.mapbox.search.common.tests.CustomTypeObjectCreatorImpl
import com.mapbox.search.common.tests.ReflectionObjectsFactory
import com.mapbox.search.common.tests.ToStringVerifier
import com.mapbox.search.tests_support.SdkCustomTypeObjectCreators
import com.mapbox.search.tests_support.createTestOriginalSearchResult
import com.mapbox.search.tests_support.createTestRequestOptions
import com.mapbox.search.tests_support.withPrefabTestBoundingBox
import com.mapbox.search.tests_support.withPrefabTestOriginalSearchResult
import com.mapbox.search.tests_support.withPrefabTestPoint
import com.mapbox.test.dsl.TestCase
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import nl.jqno.equalsverifier.EqualsVerifier
import nl.jqno.equalsverifier.Warning
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestFactory

@ExperimentalStdlibApi
internal class SearchResultTest {

    @TestFactory
    fun `Check ServerSearchResultImpl-specific implementation`() = TestCase {
        Given("Search result with empty SearchResultType list") {
            val searchResultTypes: List<SearchResultType> = emptyList()

            if (BuildConfig.DEBUG) {
                WhenThrows("Can't create ServerSearchResultImpl", IllegalStateException::class) {
                    ServerSearchResultImpl(
                        searchResultTypes,
                        ORIGINAL_SEARCH_RESULT,
                        REQUEST_OPTIONS)
                }
            } else {
                When("Creating ServerSearchResultImpl") {
                    ServerSearchResultImpl(
                        searchResultTypes,
                        ORIGINAL_SEARCH_RESULT,
                        REQUEST_OPTIONS)

                    Then("ServerSearchResultImpl Successfully is created") { }
                }
            }
        }
    }

    @TestFactory
    fun `Check ServerSearchResult equals-hashCode-toString functions`() = TestCase {
        Given("ServerSearchResult class") {
            When("equals() and hashCode() called") {
                Then("equals() and hashCode() functions should use every declared property") {
                    EqualsVerifier.forClass(ServerSearchResultImpl::class.java)
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
                                    center = Point.fromLngLat(10.0, 20.0),
                                ),
                                createTestOriginalSearchResult(
                                    id = "test-result-2",
                                    center = Point.fromLngLat(30.0, 50.0),
                                ),
                            )[mode.ordinal]
                        }
                    )

                    ToStringVerifier(
                        clazz = ServerSearchResultImpl::class,
                        ignoredProperties = listOf("originalSearchResult"),
                        objectsFactory = ReflectionObjectsFactory(
                            extraCreators = listOf(customTypeObjectCreator) + SdkCustomTypeObjectCreators.ALL_CREATORS,
                        )
                    ).verify()
                }
            }
        }
    }

    @TestFactory
    fun `Check IndexableRecordSearchResult equals-hashCode-toString functions`() = TestCase {
        Given("IndexableRecordSearchResult class") {
            When("equals() and hashCode() called") {
                Then("equals() and hashCode() functions should use every declared property") {
                    EqualsVerifier.forClass(IndexableRecordSearchResultImpl::class.java)
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
                                    center = Point.fromLngLat(10.0, 20.0),
                                    types = listOf(OriginalResultType.USER_RECORD),
                                    layerId = "test-layer-id-1"
                                ),
                                createTestOriginalSearchResult(
                                    id = "test-result-2",
                                    center = Point.fromLngLat(30.0, 50.0),
                                    types = listOf(OriginalResultType.USER_RECORD),
                                    layerId = "test-layer-id-2"
                                ),
                            )[mode.ordinal]
                        }
                    )

                    ToStringVerifier(
                        clazz = IndexableRecordSearchResultImpl::class,
                        ignoredProperties = listOf("originalSearchResult"),
                        objectsFactory = ReflectionObjectsFactory(
                            extraCreators = listOf(customTypeObjectCreator) + SdkCustomTypeObjectCreators.ALL_CREATORS,
                        )
                    ).verify()
                }
            }
        }
    }

    private companion object {
        val REQUEST_OPTIONS = createTestRequestOptions(query = "test-query")

        val ORIGINAL_SEARCH_RESULT = createTestOriginalSearchResult(
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

        @Suppress("JVM_STATIC_IN_PRIVATE_COMPANION")
        @BeforeAll
        @JvmStatic
        fun setUpAll() {
            mockkStatic(ASSERTIONS_KT_CLASS_NAME)
        }

        @Suppress("JVM_STATIC_IN_PRIVATE_COMPANION")
        @AfterAll
        @JvmStatic
        fun tearDownAll() {
            unmockkStatic(ASSERTIONS_KT_CLASS_NAME)
        }
    }
}
