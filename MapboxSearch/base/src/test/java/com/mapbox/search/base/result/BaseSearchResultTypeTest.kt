package com.mapbox.search.base.result

import com.mapbox.search.base.core.CoreResultType
import com.mapbox.test.dsl.TestCase
import org.junit.jupiter.api.TestFactory

internal class BaseSearchResultTypeTest {

    @TestFactory
    fun `Check mapping BaseSearchResultType to CoreResultType`() = TestCase {
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
                        BaseSearchResultType.UNKNOWN -> CoreResultType.UNKNOWN
                    }
                    Then("Core value should be $expectedCoreType", expectedCoreType, it.mapToCore())
                }
            }
        }
    }

    @TestFactory
    fun `Check mapping CoreResultType to BaseSearchResultType`() = TestCase {
        Given("All the possible CoreResultType types") {
            CoreResultType.values().forEach { rawResultType ->
                When("CoreResultType is $rawResultType") {
                    when (rawResultType) {
                        CoreResultType.ADDRESS -> {
                            Then("isSearchResultType should be true", true, rawResultType.isSearchResultType)
                            Then(
                                "tryMapToSearchResultType() should return ADDRESS",
                                BaseSearchResultType.ADDRESS,
                                rawResultType.tryMapToSearchResultType()
                            )
                        }
                        CoreResultType.POI -> {
                            Then("isSearchResultType should be true", true, rawResultType.isSearchResultType)
                            Then(
                                "tryMapToSearchResultType() should return POI",
                                BaseSearchResultType.POI,
                                rawResultType.tryMapToSearchResultType()
                            )
                        }
                        CoreResultType.COUNTRY -> {
                            Then("isSearchResultType should be true", true, rawResultType.isSearchResultType)
                            Then(
                                "tryMapToSearchResultType() should return COUNTRY",
                                BaseSearchResultType.COUNTRY,
                                rawResultType.tryMapToSearchResultType()
                            )
                        }
                        CoreResultType.REGION -> {
                            Then("isSearchResultType should be true", true, rawResultType.isSearchResultType)
                            Then(
                                "tryMapToSearchResultType() should return REGION",
                                BaseSearchResultType.REGION,
                                rawResultType.tryMapToSearchResultType()
                            )
                        }
                        CoreResultType.PLACE -> {
                            Then("isSearchResultType should be true", true, rawResultType.isSearchResultType)
                            Then(
                                "tryMapToSearchResultType() should return PLACE",
                                BaseSearchResultType.PLACE,
                                rawResultType.tryMapToSearchResultType()
                            )
                        }
                        CoreResultType.DISTRICT -> {
                            Then("isSearchResultType should be true", true, rawResultType.isSearchResultType)
                            Then(
                                "tryMapToSearchResultType() should return DISTRICT",
                                BaseSearchResultType.DISTRICT,
                                rawResultType.tryMapToSearchResultType()
                            )
                        }
                        CoreResultType.LOCALITY -> {
                            Then("isSearchResultType should be true", true, rawResultType.isSearchResultType)
                            Then(
                                "tryMapToSearchResultType() should return LOCALITY",
                                BaseSearchResultType.LOCALITY,
                                rawResultType.tryMapToSearchResultType()
                            )
                        }
                        CoreResultType.NEIGHBORHOOD -> {
                            Then("isSearchResultType should be true", true, rawResultType.isSearchResultType)
                            Then(
                                "tryMapToSearchResultType() should return NEIGHBORHOOD",
                                BaseSearchResultType.NEIGHBORHOOD,
                                rawResultType.tryMapToSearchResultType()
                            )
                        }
                        CoreResultType.STREET -> {
                            Then("isSearchResultType should be true", true, rawResultType.isSearchResultType)
                            Then(
                                "tryMapToSearchResultType() should return STREET",
                                BaseSearchResultType.STREET,
                                rawResultType.tryMapToSearchResultType()
                            )
                        }
                        CoreResultType.POSTCODE -> {
                            Then("isSearchResultType should be true", true, rawResultType.isSearchResultType)
                            Then(
                                "tryMapToSearchResultType() should return POSTCODE",
                                BaseSearchResultType.POSTCODE,
                                rawResultType.tryMapToSearchResultType()
                            )
                        }
                        CoreResultType.BLOCK -> {
                            Then("isSearchResultType should be true", true, rawResultType.isSearchResultType)
                            Then(
                                "tryMapToSearchResultType() should return BLOCK",
                                BaseSearchResultType.BLOCK,
                                rawResultType.tryMapToSearchResultType()
                            )
                        }
                        CoreResultType.UNKNOWN -> {
                            Then("isSearchResultType should be true", true, rawResultType.isSearchResultType)
                            Then(
                                "tryMapToSearchResultType() should return UNKNOWN",
                                BaseSearchResultType.UNKNOWN,
                                rawResultType.tryMapToSearchResultType()
                            )
                        }
                        CoreResultType.CATEGORY,
                        CoreResultType.BRAND,
                        CoreResultType.QUERY,
                        CoreResultType.USER_RECORD -> {
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
}
