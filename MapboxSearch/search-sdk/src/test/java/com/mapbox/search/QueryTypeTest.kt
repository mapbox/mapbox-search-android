package com.mapbox.search

import com.mapbox.search.base.core.CoreQueryType
import com.mapbox.search.tests_support.checkEnumValues
import com.mapbox.test.dsl.TestCase
import org.junit.jupiter.api.TestFactory

internal class QueryTypeTest {

    @TestFactory
    fun `Check QueryType public api fields`() = TestCase {
        Given("QueryType values") {
            checkEnumValues(QUERY_TYPE_VALUES, QueryType::class.java)
        }
    }

    @TestFactory
    fun `Check mapping QueryType to core`() = TestCase {
        Given("QueryTypeExtension") {
            mapOf(
                QueryType.COUNTRY to CoreQueryType.COUNTRY,
                QueryType.REGION to CoreQueryType.REGION,
                QueryType.POSTCODE to CoreQueryType.POSTCODE,
                QueryType.DISTRICT to CoreQueryType.DISTRICT,
                QueryType.PLACE to CoreQueryType.PLACE,
                QueryType.LOCALITY to CoreQueryType.LOCALITY,
                QueryType.NEIGHBORHOOD to CoreQueryType.NEIGHBORHOOD,
                QueryType.STREET to CoreQueryType.STREET,
                QueryType.ADDRESS to CoreQueryType.ADDRESS,
                QueryType.POI to CoreQueryType.POI,
                QueryType.CATEGORY to CoreQueryType.CATEGORY,
            ).forEach { (inputValue, expectedValue) ->
                When("Convert Platform QueryType <$inputValue> to core") {
                    val actualValue = inputValue.mapToCore()
                    Then("Core QueryType should be <$expectedValue>", actualValue, expectedValue)
                }
            }
        }
    }

    private companion object {
        val QUERY_TYPE_VALUES = listOf(
            "COUNTRY", "REGION", "POSTCODE", "DISTRICT", "PLACE", "LOCALITY",
            "NEIGHBORHOOD", "STREET", "ADDRESS", "POI", "CATEGORY"
        )
    }
}
