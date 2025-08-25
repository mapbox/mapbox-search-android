package com.mapbox.search.base.result

import com.mapbox.search.base.logger.reinitializeLogImpl
import com.mapbox.search.base.logger.resetLogImpl
import com.mapbox.search.base.tests_support.createTestBaseRawSearchResult
import com.mapbox.search.base.tests_support.createTestBaseRequestOptions
import com.mapbox.search.base.tests_support.withPrefabTestBaseRawSearchResult
import com.mapbox.search.common.tests.BuildConfig
import com.mapbox.search.common.tests.TestConstants.ASSERTIONS_KT_CLASS_NAME
import com.mapbox.search.common.tests.createCoreSearchAddress
import com.mapbox.search.common.tests.createCoreSearchAddressCountry
import com.mapbox.search.common.tests.createCoreSearchAddressRegion
import com.mapbox.search.common.tests.createTestCoreRequestOptions
import com.mapbox.search.common.tests.withPrefabTestBoundingBox
import com.mapbox.search.common.tests.withPrefabTestPoint
import com.mapbox.test.dsl.TestCase
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import nl.jqno.equalsverifier.EqualsVerifier
import nl.jqno.equalsverifier.Warning
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestFactory

internal class BaseSearchResultTest {

    @TestFactory
    fun `Check ServerSearchResultImpl-specific implementation`() = TestCase {
        Given("Search result with empty SearchResultType list") {
            val searchResultTypes: List<BaseSearchResultType> = emptyList()

            if (BuildConfig.DEBUG) {
                WhenThrows("Can't create ServerSearchResultImpl", IllegalStateException::class) {
                    BaseServerSearchResultImpl(
                        searchResultTypes,
                        BASE_RAW_SEARCH_RESULT,
                        REQUEST_OPTIONS
                    )
                }
            } else {
                When("Creating ServerSearchResultImpl") {
                    BaseServerSearchResultImpl(
                        searchResultTypes,
                        BASE_RAW_SEARCH_RESULT,
                        REQUEST_OPTIONS
                    )

                    Then("ServerSearchResultImpl Successfully is created") { }
                }
            }
        }
    }

    @TestFactory
    fun `Check ServerSearchResult equals-hashCode functions`() = TestCase {
        Given("ServerSearchResult class") {
            When("equals() and hashCode() called") {
                Then("equals() and hashCode() functions should use every declared property") {
                    EqualsVerifier.forClass(BaseServerSearchResultImpl::class.java)
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

    @TestFactory
    fun `Check IndexableRecordSearchResult equals-hashCode functions`() = TestCase {
        Given("IndexableRecordSearchResult class") {
            When("equals() and hashCode() called") {
                Then("equals() and hashCode() functions should use every declared property") {
                    EqualsVerifier.forClass(BaseIndexableRecordSearchResultImpl::class.java)
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
            core = createTestCoreRequestOptions(query = "test-query"),
        )

        val BASE_RAW_SEARCH_RESULT = createTestBaseRawSearchResult(
            id = "Search result 1",
            names = listOf("Search result 1.1", "Search result 1.2"),
            descriptionAddress = "Search result 1 description",
            addresses = listOf(
                createCoreSearchAddress(
                    country = createCoreSearchAddressCountry("Belarus"),
                    region = createCoreSearchAddressRegion("Minsk"),
                    street = "Francyska Skaryny",
                    houseNumber = "1"
                )
            ),
            fullAddress = "Test full address",
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
