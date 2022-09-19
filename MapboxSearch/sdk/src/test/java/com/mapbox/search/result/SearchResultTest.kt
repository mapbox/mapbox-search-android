package com.mapbox.search.result

import com.mapbox.geojson.Point
import com.mapbox.search.base.logger.reinitializeLogImpl
import com.mapbox.search.base.logger.resetLogImpl
import com.mapbox.search.common.TestConstants.ASSERTIONS_KT_CLASS_NAME
import com.mapbox.search.common.tests.CustomTypeObjectCreatorImpl
import com.mapbox.search.common.tests.ReflectionObjectsFactory
import com.mapbox.search.common.tests.ToStringVerifier
import com.mapbox.search.common.withPrefabTestBoundingBox
import com.mapbox.search.common.withPrefabTestPoint
import com.mapbox.search.tests_support.SdkCustomTypeObjectCreators
import com.mapbox.search.tests_support.createTestSearchResult
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
    fun `Check SearchResult equals-hashCode-toString functions`() = TestCase {
        Given("SearchResult class") {
            When("equals() and hashCode() called") {
                Then("equals() and hashCode() functions should use every declared property") {
                    EqualsVerifier.forClass(SearchResult::class.java)
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
                        clazz = SearchResult::class,
                        factory = { mode ->
                            listOf(
                                createTestSearchResult(
                                    id = "test-result-1",
                                    center = Point.fromLngLat(10.0, 20.0),
                                ),
                                createTestSearchResult(
                                    id = "test-result-2",
                                    center = Point.fromLngLat(30.0, 50.0),
                                ),
                            )[mode.ordinal]
                        }
                    )

                    ToStringVerifier(
                        clazz = SearchResult::class,
                        ignoredProperties = listOf("base"),
                        objectsFactory = ReflectionObjectsFactory(
                            extraCreators = listOf(customTypeObjectCreator) + SdkCustomTypeObjectCreators.ALL_CREATORS,
                        )
                    ).verify()
                }
            }
        }
    }

    private companion object {

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
