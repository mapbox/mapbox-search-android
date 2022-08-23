package com.mapbox.search.base.result

import com.mapbox.search.base.core.CoreResultType
import com.mapbox.test.dsl.TestCase
import org.junit.jupiter.api.TestFactory

internal class BaseSearchResultTypeTest {

    @TestFactory
    fun `Check map to core mapping`() = TestCase {
        Given("All SearchResultType values") {
            BaseSearchResultType.values().forEach {
                When("Value is $it") {
                    val expectedCoreType = when (it) {
                        BaseSearchResultType.ADDRESS -> CoreResultType.ADDRESS
                        BaseSearchResultType.POI -> CoreResultType.POI
                        BaseSearchResultType.COUNTRY -> CoreResultType.COUNTRY
                        BaseSearchResultType.REGION -> CoreResultType.REGION
                        BaseSearchResultType.PLACE -> CoreResultType.PLACE
                        BaseSearchResultType.DISTRICT -> CoreResultType.DISTRICT
                        BaseSearchResultType.LOCALITY -> CoreResultType.LOCALITY
                        BaseSearchResultType.NEIGHBORHOOD -> CoreResultType.NEIGHBORHOOD
                        BaseSearchResultType.STREET -> CoreResultType.STREET
                        BaseSearchResultType.POSTCODE -> CoreResultType.POSTCODE
                        BaseSearchResultType.BLOCK -> CoreResultType.BLOCK
                    }
                    Then("Core value should be $expectedCoreType", expectedCoreType, it.mapToCore())
                }
            }
        }
    }
}
