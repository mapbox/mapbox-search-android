package com.mapbox.search.offline

import com.mapbox.search.base.result.BaseRawResultType
import com.mapbox.test.dsl.TestCase
import org.junit.jupiter.api.TestFactory

internal class NewOfflineSearchResultTypeTest {

    @TestFactory
    fun `Check createFromRawResultType() function`() = TestCase {
        Given("All the possible BaseRawResultType types") {
            BaseRawResultType.values().forEach { rawResultType ->
                When("BaseRawResultType is $rawResultType") {
                    val newType = when (rawResultType) {
                        BaseRawResultType.PLACE -> NewOfflineSearchResultType.PLACE
                        BaseRawResultType.STREET -> NewOfflineSearchResultType.STREET
                        BaseRawResultType.ADDRESS -> NewOfflineSearchResultType.ADDRESS
                        BaseRawResultType.POI -> NewOfflineSearchResultType.POI
                        BaseRawResultType.COUNTRY,
                        BaseRawResultType.REGION,
                        BaseRawResultType.DISTRICT,
                        BaseRawResultType.LOCALITY,
                        BaseRawResultType.NEIGHBORHOOD,
                        BaseRawResultType.POSTCODE,
                        BaseRawResultType.BLOCK,
                        BaseRawResultType.CATEGORY,
                        BaseRawResultType.BRAND,
                        BaseRawResultType.QUERY,
                        BaseRawResultType.USER_RECORD,
                        BaseRawResultType.UNKNOWN -> null
                    }

                    val expectedType =
                        NewOfflineSearchResultType.createFromRawResultType(rawResultType)
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
