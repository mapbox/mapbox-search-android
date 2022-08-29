package com.mapbox.search.offline

import com.mapbox.search.base.result.BaseRawResultType
import com.mapbox.test.dsl.TestCase
import org.junit.jupiter.api.TestFactory

internal class OfflineSearchResultTypeTest {

    @TestFactory
    fun `Check BaseRawResultType tryMapToOfflineSdkType() function`() = TestCase {
        Given("All the possible BaseRawResultType types") {
            BaseRawResultType.values().forEach { rawResultType ->
                When("BaseRawResultType is $rawResultType") {
                    when (rawResultType) {
                        BaseRawResultType.PLACE -> {
                            Then(
                                "Mapped value should be ${OfflineSearchResultType.PLACE}",
                                OfflineSearchResultType.PLACE,
                                rawResultType.tryMapToOfflineSdkType()
                            )
                        }
                        BaseRawResultType.STREET -> {
                            Then(
                                "Mapped value should be ${OfflineSearchResultType.STREET}",
                                OfflineSearchResultType.STREET,
                                rawResultType.tryMapToOfflineSdkType()
                            )
                        }
                        BaseRawResultType.ADDRESS -> {
                            Then(
                                "Mapped value should be ${OfflineSearchResultType.ADDRESS}",
                                OfflineSearchResultType.ADDRESS,
                                rawResultType.tryMapToOfflineSdkType()
                            )
                        }
                        BaseRawResultType.POI,
                        BaseRawResultType.COUNTRY,
                        BaseRawResultType.REGION,
                        BaseRawResultType.DISTRICT,
                        BaseRawResultType.LOCALITY,
                        BaseRawResultType.NEIGHBORHOOD,
                        BaseRawResultType.POSTCODE,
                        BaseRawResultType.BLOCK,
                        BaseRawResultType.CATEGORY,
                        BaseRawResultType.QUERY,
                        BaseRawResultType.USER_RECORD,
                        BaseRawResultType.UNKNOWN -> {
                            Then(
                                "Mapped value should be null",
                                null,
                                rawResultType.tryMapToOfflineSdkType()
                            )
                        }
                    }
                }
            }
        }
    }
}
