package com.mapbox.search.offline

import com.mapbox.search.base.core.CoreResultType
import com.mapbox.test.dsl.TestCase
import org.junit.jupiter.api.TestFactory

internal class NewOfflineSearchResultTypeTest {

    @TestFactory
    fun `Check createFromRawResultType() function`() = TestCase {
        Given("All the possible CoreResultType types") {
            CoreResultType.values().forEach { coreResultType ->
                When("CoreResultType is $coreResultType") {
                    val newType = when (coreResultType) {
                        CoreResultType.PLACE -> NewOfflineSearchResultType.PLACE
                        CoreResultType.STREET -> NewOfflineSearchResultType.STREET
                        CoreResultType.ADDRESS -> NewOfflineSearchResultType.ADDRESS
                        CoreResultType.POI -> NewOfflineSearchResultType.POI
                        CoreResultType.COUNTRY,
                        CoreResultType.REGION,
                        CoreResultType.DISTRICT,
                        CoreResultType.LOCALITY,
                        CoreResultType.NEIGHBORHOOD,
                        CoreResultType.POSTCODE,
                        CoreResultType.BLOCK,
                        CoreResultType.CATEGORY,
                        CoreResultType.BRAND,
                        CoreResultType.QUERY,
                        CoreResultType.USER_RECORD,
                        CoreResultType.UNKNOWN -> null
                    }

                    val expectedType =
                        NewOfflineSearchResultType.createFromRawResultType(coreResultType)
                    Then("New type should be $expectedType", newType, expectedType)
                }
            }
        }
    }

    @TestFactory
    fun `Check FALLBACK_TYPE value`() = TestCase {
        Given("NewOfflineSearchResultType.FALLBACK_TYPE") {
            val fallbackType = NewOfflineSearchResultType.FALLBACK_TYPE
            When("Fallback type called") {
                Then("It should be ADDRESS", NewOfflineSearchResultType.ADDRESS, fallbackType)
            }
        }
    }

    @TestFactory
    fun `Check toOldResultType() function`() = TestCase {
        Given("NewOfflineSearchResultType values") {
            listOf(
                NewOfflineSearchResultType.PLACE,
                NewOfflineSearchResultType.STREET,
                NewOfflineSearchResultType.ADDRESS,
                NewOfflineSearchResultType.POI,
            ).forEach {
                When("NewOfflineSearchResultType is $it") {
                    @Suppress("DEPRECATION")
                    val oldType = when (it) {
                        NewOfflineSearchResultType.PLACE -> OfflineSearchResultType.PLACE
                        NewOfflineSearchResultType.STREET -> OfflineSearchResultType.STREET
                        NewOfflineSearchResultType.ADDRESS -> OfflineSearchResultType.ADDRESS
                        NewOfflineSearchResultType.POI -> OfflineSearchResultType.DEFAULT
                        else -> error("Unprocessed NewOfflineSearchResultType value $it")
                    }

                    Then(
                        "toOldResultType() should return $oldType",
                        oldType,
                        NewOfflineSearchResultType.toOldResultType(it),
                    )
                }
            }
        }
    }
}
