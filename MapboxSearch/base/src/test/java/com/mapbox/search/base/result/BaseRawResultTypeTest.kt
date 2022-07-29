package com.mapbox.search.base.result

import com.mapbox.test.dsl.TestCase
import org.junit.jupiter.api.TestFactory

internal class BaseRawResultTypeTest {

    @TestFactory
    fun `Check mapping to SearchResultType functions`() = TestCase {
        Given("All the possible BaseRawResultType types") {
            BaseRawResultType.values().forEach { rawResultType ->
                When("BaseRawResultType is $rawResultType") {
                    when (rawResultType) {
                        BaseRawResultType.ADDRESS -> {
                            Then("isSearchResultType should be true", true, rawResultType.isSearchResultType)
                            Then(
                                "tryMapToSearchResultType() should return ADDRESS",
                                BaseSearchResultType.ADDRESS,
                                rawResultType.tryMapToSearchResultType()
                            )
                        }
                        BaseRawResultType.POI -> {
                            Then("isSearchResultType should be true", true, rawResultType.isSearchResultType)
                            Then(
                                "tryMapToSearchResultType() should return POI",
                                BaseSearchResultType.POI,
                                rawResultType.tryMapToSearchResultType()
                            )
                        }
                        BaseRawResultType.COUNTRY -> {
                            Then("isSearchResultType should be true", true, rawResultType.isSearchResultType)
                            Then(
                                "tryMapToSearchResultType() should return COUNTRY",
                                BaseSearchResultType.COUNTRY,
                                rawResultType.tryMapToSearchResultType()
                            )
                        }
                        BaseRawResultType.REGION -> {
                            Then("isSearchResultType should be true", true, rawResultType.isSearchResultType)
                            Then(
                                "tryMapToSearchResultType() should return REGION",
                                BaseSearchResultType.REGION,
                                rawResultType.tryMapToSearchResultType()
                            )
                        }
                        BaseRawResultType.PLACE -> {
                            Then("isSearchResultType should be true", true, rawResultType.isSearchResultType)
                            Then(
                                "tryMapToSearchResultType() should return PLACE",
                                BaseSearchResultType.PLACE,
                                rawResultType.tryMapToSearchResultType()
                            )
                        }
                        BaseRawResultType.DISTRICT -> {
                            Then("isSearchResultType should be true", true, rawResultType.isSearchResultType)
                            Then(
                                "tryMapToSearchResultType() should return DISTRICT",
                                BaseSearchResultType.DISTRICT,
                                rawResultType.tryMapToSearchResultType()
                            )
                        }
                        BaseRawResultType.LOCALITY -> {
                            Then("isSearchResultType should be true", true, rawResultType.isSearchResultType)
                            Then(
                                "tryMapToSearchResultType() should return LOCALITY",
                                BaseSearchResultType.LOCALITY,
                                rawResultType.tryMapToSearchResultType()
                            )
                        }
                        BaseRawResultType.NEIGHBORHOOD -> {
                            Then("isSearchResultType should be true", true, rawResultType.isSearchResultType)
                            Then(
                                "tryMapToSearchResultType() should return NEIGHBORHOOD",
                                BaseSearchResultType.NEIGHBORHOOD,
                                rawResultType.tryMapToSearchResultType()
                            )
                        }
                        BaseRawResultType.STREET -> {
                            Then("isSearchResultType should be true", true, rawResultType.isSearchResultType)
                            Then(
                                "tryMapToSearchResultType() should return STREET",
                                BaseSearchResultType.STREET,
                                rawResultType.tryMapToSearchResultType()
                            )
                        }
                        BaseRawResultType.POSTCODE -> {
                            Then("isSearchResultType should be true", true, rawResultType.isSearchResultType)
                            Then(
                                "tryMapToSearchResultType() should return POSTCODE",
                                BaseSearchResultType.POSTCODE,
                                rawResultType.tryMapToSearchResultType()
                            )
                        }
                        BaseRawResultType.BLOCK -> {
                            Then("isSearchResultType should be true", true, rawResultType.isSearchResultType)
                            Then(
                                "tryMapToSearchResultType() should return POSTCODE",
                                BaseSearchResultType.BLOCK,
                                rawResultType.tryMapToSearchResultType()
                            )
                        }
                        BaseRawResultType.CATEGORY,
                        BaseRawResultType.QUERY,
                        BaseRawResultType.USER_RECORD,
                        BaseRawResultType.UNKNOWN -> {
                            Then("isSearchResultType should be false", false, rawResultType.isSearchResultType)
                            Then(
                                "tryMapToSearchResultType() should return null",
                                null,
                                rawResultType.tryMapToSearchResultType()
                            )
                        }
                    }
                }
            }
        }
    }

    @TestFactory
    fun `Check mapping CoreResultType functions`() = TestCase {
        Given("All the possible BaseRawResultType types") {
            BaseRawResultType.values().forEach { rawResultType ->
                When("BaseRawResultType is $rawResultType") {
                    val actualResultType = rawResultType.mapToCore().mapToBase()
                    Then(
                        "Mapped to core and mapped backed to platform type should be $rawResultType",
                        rawResultType,
                        actualResultType
                    )
                }
            }
        }
    }
}
