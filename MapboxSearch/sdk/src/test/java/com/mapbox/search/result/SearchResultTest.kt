package com.mapbox.search.result

import com.mapbox.geojson.Point
import com.mapbox.search.BuildConfig
import com.mapbox.search.base.logger.reinitializeLogImpl
import com.mapbox.search.base.logger.resetLogImpl
import com.mapbox.search.base.result.BaseRawResultType
import com.mapbox.search.base.result.BaseRawSearchResult
import com.mapbox.search.base.result.BaseSuggestAction
import com.mapbox.search.common.TestConstants.ASSERTIONS_KT_CLASS_NAME
import com.mapbox.search.common.tests.CustomTypeObjectCreatorImpl
import com.mapbox.search.common.tests.ReflectionObjectsFactory
import com.mapbox.search.common.tests.ToStringVerifier
import com.mapbox.search.common.withPrefabTestBoundingBox
import com.mapbox.search.common.withPrefabTestPoint
import com.mapbox.search.tests_support.SdkCustomTypeObjectCreators
import com.mapbox.search.tests_support.createTestBaseRawSearchResult
import com.mapbox.search.tests_support.createTestRequestOptions
import com.mapbox.search.tests_support.withPrefabTestBaseRawSearchResult
import com.mapbox.test.dsl.TestCase
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import nl.jqno.equalsverifier.EqualsVerifier
import nl.jqno.equalsverifier.Warning
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestFactory

internal class SearchResultTest {

    @TestFactory
    fun `Check ServerSearchResultImpl-specific implementation`() = TestCase {
        Given("Search result with empty SearchResultType list") {
            val searchResultTypes: List<SearchResultType> = emptyList()

            if (BuildConfig.DEBUG) {
                WhenThrows("Can't create ServerSearchResultImpl", IllegalStateException::class) {
                    ServerSearchResultImpl(
                        searchResultTypes,
                        BASE_RAW_SEARCH_RESULT,
                        REQUEST_OPTIONS)
                }
            } else {
                When("Creating ServerSearchResultImpl") {
                    ServerSearchResultImpl(
                        searchResultTypes,
                        BASE_RAW_SEARCH_RESULT,
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
                        .withPrefabTestBaseRawSearchResult()
                        // TODO(#777) EqualsVerifier check fails on overridden from superclass properties
                        .suppress(Warning.ALL_FIELDS_SHOULD_BE_USED)
                        .verify()
                }
            }

            When("toString() called") {
                Then("toString() function should include every declared property") {
                    val customTypeObjectCreator = CustomTypeObjectCreatorImpl(
                        clazz = BaseRawSearchResult::class,
                        factory = { mode ->
                            listOf(
                                createTestBaseRawSearchResult(
                                    id = "test-result-1",
                                    center = Point.fromLngLat(10.0, 20.0),
                                ),
                                createTestBaseRawSearchResult(
                                    id = "test-result-2",
                                    center = Point.fromLngLat(30.0, 50.0),
                                ),
                            )[mode.ordinal]
                        }
                    )

                    ToStringVerifier(
                        clazz = ServerSearchResultImpl::class,
                        ignoredProperties = listOf("rawSearchResult"),
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
                        .withPrefabTestBaseRawSearchResult()
                        // TODO(#777) EqualsVerifier check fails on overridden from superclass properties
                        .suppress(Warning.ALL_FIELDS_SHOULD_BE_USED)
                        .verify()
                }
            }

            When("toString() called") {
                Then("toString() function should include every declared property") {
                    val customTypeObjectCreator = CustomTypeObjectCreatorImpl(
                        clazz = BaseRawSearchResult::class,
                        factory = { mode ->
                            listOf(
                                createTestBaseRawSearchResult(
                                    id = "test-result-1",
                                    center = Point.fromLngLat(10.0, 20.0),
                                    types = listOf(BaseRawResultType.USER_RECORD),
                                    layerId = "test-layer-id-1"
                                ),
                                createTestBaseRawSearchResult(
                                    id = "test-result-2",
                                    center = Point.fromLngLat(30.0, 50.0),
                                    types = listOf(BaseRawResultType.USER_RECORD),
                                    layerId = "test-layer-id-2"
                                ),
                            )[mode.ordinal]
                        }
                    )

                    ToStringVerifier(
                        clazz = IndexableRecordSearchResultImpl::class,
                        ignoredProperties = listOf("rawSearchResult"),
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

        val BASE_RAW_SEARCH_RESULT = createTestBaseRawSearchResult(
            id = "Search result 1",
            names = listOf("Search result 1.1", "Search result 1.2"),
            descriptionAddress = "Search result 1 description",
            addresses = listOf(SearchAddress(country = "Belarus", region = "Minsk", street = "Francyska Skaryny", houseNumber = "1")),
            distanceMeters = 123.0,
            icon = "cafe",
            etaMinutes = 5.0,
            types = listOf(BaseRawResultType.POI),
            action = BaseSuggestAction(endpoint = "test-endpoint-1", path = "test-path-1", query = "test-query-1", body = null, multiRetrievable = true)
        )

        @Suppress("JVM_STATIC_IN_PRIVATE_COMPANION")
        @BeforeAll
        @JvmStatic
        fun setUpAll() {
            resetLogImpl()
            mockkStatic(ASSERTIONS_KT_CLASS_NAME)
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
