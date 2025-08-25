package com.mapbox.search.result

import com.mapbox.search.base.core.CoreResultType
import com.mapbox.search.tests_support.checkEnumValues
import com.mapbox.test.dsl.TestCase
import org.junit.jupiter.api.TestFactory

internal class SearchResultTypeTest {

    @TestFactory
    fun `Check SearchResultType public api fields`() = TestCase {
        Given("SearchResultType values") {
            checkEnumValues(SEARCH_RESULT_TYPE_VALUES, SearchResultType::class.java)
        }
    }

    @TestFactory
    fun `Check map to core mapping`() = TestCase {
        Given("All SearchResultType values") {
            SearchResultType.values().forEach {
                When("Value is $it") {
                    val expectedCoreType = when (it) {
                        SearchResultType.ADDRESS -> CoreResultType.ADDRESS
                        SearchResultType.POI -> CoreResultType.POI
                        SearchResultType.COUNTRY -> CoreResultType.COUNTRY
                        SearchResultType.REGION -> CoreResultType.REGION
                        SearchResultType.PLACE -> CoreResultType.PLACE
                        SearchResultType.DISTRICT -> CoreResultType.DISTRICT
                        SearchResultType.LOCALITY -> CoreResultType.LOCALITY
                        SearchResultType.NEIGHBORHOOD -> CoreResultType.NEIGHBORHOOD
                        SearchResultType.STREET -> CoreResultType.STREET
                        SearchResultType.POSTCODE -> CoreResultType.POSTCODE
                        SearchResultType.BLOCK -> CoreResultType.BLOCK
                    }
                    Then("Core value should be $expectedCoreType", expectedCoreType, it.mapToCore())
                }
            }
        }
    }

    private companion object {
        val SEARCH_RESULT_TYPE_VALUES = listOf(
            "COUNTRY", "REGION", "POSTCODE", "BLOCK", "DISTRICT", "PLACE",
            "LOCALITY", "NEIGHBORHOOD", "STREET", "ADDRESS", "POI",
        )
    }
}
