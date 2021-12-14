package com.mapbox.search.result

import com.mapbox.test.dsl.TestCase
import org.junit.jupiter.api.TestFactory

internal class OriginalResultTypeTest {

    @TestFactory
    fun `Check mapping to SearchResultType functions`() = TestCase {
        Given("All the possible OriginalResultType types") {
            OriginalResultType.values().forEach { originalType ->
                When("OriginalResultType is $originalType") {
                    when (originalType) {
                        OriginalResultType.ADDRESS -> {
                            Then("isSearchResultType should be true", true, originalType.isSearchResultType)
                            Then(
                                "tryMapToSearchResultType() should return ADDRESS",
                                SearchResultType.ADDRESS,
                                originalType.tryMapToSearchResultType()
                            )
                        }
                        OriginalResultType.POI -> {
                            Then("isSearchResultType should be true", true, originalType.isSearchResultType)
                            Then(
                                "tryMapToSearchResultType() should return POI",
                                SearchResultType.POI,
                                originalType.tryMapToSearchResultType()
                            )
                        }
                        OriginalResultType.COUNTRY -> {
                            Then("isSearchResultType should be true", true, originalType.isSearchResultType)
                            Then(
                                "tryMapToSearchResultType() should return COUNTRY",
                                SearchResultType.COUNTRY,
                                originalType.tryMapToSearchResultType()
                            )
                        }

                        OriginalResultType.REGION -> {
                            Then("isSearchResultType should be true", true, originalType.isSearchResultType)
                            Then(
                                "tryMapToSearchResultType() should return REGION",
                                SearchResultType.REGION,
                                originalType.tryMapToSearchResultType()
                            )
                        }
                        OriginalResultType.PLACE -> {
                            Then("isSearchResultType should be true", true, originalType.isSearchResultType)
                            Then(
                                "tryMapToSearchResultType() should return PLACE",
                                SearchResultType.PLACE,
                                originalType.tryMapToSearchResultType()
                            )
                        }
                        OriginalResultType.DISTRICT -> {
                            Then("isSearchResultType should be true", true, originalType.isSearchResultType)
                            Then(
                                "tryMapToSearchResultType() should return DISTRICT",
                                SearchResultType.DISTRICT,
                                originalType.tryMapToSearchResultType()
                            )
                        }
                        OriginalResultType.LOCALITY -> {
                            Then("isSearchResultType should be true", true, originalType.isSearchResultType)
                            Then(
                                "tryMapToSearchResultType() should return LOCALITY",
                                SearchResultType.LOCALITY,
                                originalType.tryMapToSearchResultType()
                            )
                        }
                        OriginalResultType.NEIGHBORHOOD -> {
                            Then("isSearchResultType should be true", true, originalType.isSearchResultType)
                            Then(
                                "tryMapToSearchResultType() should return NEIGHBORHOOD",
                                SearchResultType.NEIGHBORHOOD,
                                originalType.tryMapToSearchResultType()
                            )
                        }
                        OriginalResultType.STREET -> {
                            Then("isSearchResultType should be true", true, originalType.isSearchResultType)
                            Then(
                                "tryMapToSearchResultType() should return STREET",
                                SearchResultType.STREET,
                                originalType.tryMapToSearchResultType()
                            )
                        }
                        OriginalResultType.POSTCODE -> {
                            Then("isSearchResultType should be true", true, originalType.isSearchResultType)
                            Then(
                                "tryMapToSearchResultType() should return POSTCODE",
                                SearchResultType.POSTCODE,
                                originalType.tryMapToSearchResultType()
                            )
                        }
                        OriginalResultType.CATEGORY,
                        OriginalResultType.QUERY,
                        OriginalResultType.USER_RECORD,
                        OriginalResultType.UNKNOWN -> {
                            Then("isSearchResultType should be false", false, originalType.isSearchResultType)
                            Then(
                                "tryMapToSearchResultType() should return null",
                                null,
                                originalType.tryMapToSearchResultType()
                            )
                        }
                    }
                }
            }
        }
    }

    @TestFactory
    fun `Check mapping CoreResultType functions`() = TestCase {
        Given("All the possible OriginalResultType types") {
            OriginalResultType.values().forEach { originalType ->
                When("OriginalResultType is $originalType") {
                    val actualResultType = originalType.mapToCore().mapToPlatform()
                    Then(
                        "Mapped to core and mapped backed to platform type should be $originalType",
                        originalType,
                        actualResultType
                    )
                }
            }
        }
    }
}
