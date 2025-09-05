package com.mapbox.search.result

import com.mapbox.search.SearchResultMetadata
import com.mapbox.search.base.logger.reinitializeLogImpl
import com.mapbox.search.base.logger.resetLogImpl
import com.mapbox.search.common.tests.CustomTypeObjectCreatorImpl
import com.mapbox.search.common.tests.ReflectionObjectsFactory
import com.mapbox.search.common.tests.TestConstants
import com.mapbox.search.common.tests.ToStringVerifier
import com.mapbox.search.common.tests.withPrefabTestBoundingBox
import com.mapbox.search.common.tests.withPrefabTestPoint
import com.mapbox.search.mapToPlatform
import com.mapbox.search.tests_support.SdkCustomTypeObjectCreators
import com.mapbox.search.tests_support.createTestBaseSearchSuggestion
import com.mapbox.search.tests_support.createTestSearchSuggestion
import com.mapbox.search.tests_support.withPrefabTestBaseRawSearchResult
import com.mapbox.test.dsl.TestCase
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkStatic
import nl.jqno.equalsverifier.EqualsVerifier
import nl.jqno.equalsverifier.Warning
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestFactory

internal class SearchSuggestionTest {

    @TestFactory
    fun `Check SearchSuggestion implementation`() = TestCase {
        Given("SearchSuggestion class with mocked base suggestion") {
            val base = spyk(createTestBaseSearchSuggestion())
            val suggestion = SearchSuggestion(base)

            When("SearchSuggestion fields called") {
                Then("id should be ${base.id}", base.id, suggestion.id)
                Then("name should be ${base.name}", base.name, suggestion.name)
                Then("matchingName should be ${base.matchingName}", base.matchingName, suggestion.matchingName)
                Then("descriptionText should be ${base.descriptionText}", base.descriptionText, suggestion.descriptionText)
                Then("address should be ${base.address?.mapToPlatform()}", base.address?.mapToPlatform(), suggestion.address)
                Then("requestOptions should be ${base.requestOptions.mapToPlatform()}", base.requestOptions.mapToPlatform(), suggestion.requestOptions)
                Then("distanceMeters should be ${base.distanceMeters}", base.distanceMeters, suggestion.distanceMeters)
                Then("categories should be ${base.categories}", base.categories, suggestion.categories)
                Then("makiIcon should be ${base.makiIcon}", base.makiIcon, suggestion.makiIcon)
                Then("etaMinutes should be ${base.etaMinutes}", base.etaMinutes, suggestion.etaMinutes)
                Then("metadata should be ${base.metadata?.let { SearchResultMetadata(it) }}", base.metadata?.let { SearchResultMetadata(it) }, suggestion.metadata)
                Then("externalIDs should be ${base.externalIDs}", base.externalIDs, suggestion.externalIDs)
                Then("isBatchResolveSupported should be ${base.isBatchResolveSupported}", base.isBatchResolveSupported, suggestion.isBatchResolveSupported)
                Then("serverIndex should be ${base.serverIndex}", base.serverIndex, suggestion.serverIndex)
                Then("type should be ${base.getSearchSuggestionType()}", base.getSearchSuggestionType(), suggestion.type)
            }
        }
    }

    @TestFactory
    fun `Check SearchSuggestion equals-hashCode-toString functions`() = TestCase {
        Given("SearchSuggestion class") {
            When("equals() and hashCode() called") {
                Then("equals() and hashCode() functions should use every declared property") {
                    EqualsVerifier.forClass(SearchSuggestion::class.java)
                        .withPrefabTestPoint()
                        .withPrefabTestBoundingBox()
                        .withPrefabTestBaseRawSearchResult()
                        .suppress(Warning.ALL_FIELDS_SHOULD_BE_USED)
                        .verify()
                }
            }

            When("toString() called") {
                Then("toString() function should include every declared property") {
                    val customTypeObjectCreator = CustomTypeObjectCreatorImpl(
                        clazz = SearchSuggestion::class,
                        factory = { mode ->
                            listOf(
                                createTestSearchSuggestion("id-1"),
                                createTestSearchSuggestion("id-2"),
                            )[mode.ordinal]
                        }
                    )

                    ToStringVerifier(
                        clazz = SearchSuggestion::class,
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
