package com.mapbox.search.result

import com.mapbox.search.tests_support.toStringFull
import com.mapbox.test.dsl.TestCase
import org.junit.jupiter.api.TestFactory

internal class SearchAddressTest {

    @TestFactory
    fun `Check formattedAddress with empty house number`() = TestCase {
        Given("Address with empty house number") {
            val address = FULL_SEARCH_ADDRESS.copy(houseNumber = null)

            listOf(
                SearchAddress.FormatStyle.Short,
                SearchAddress.FormatStyle.Medium,
                SearchAddress.FormatStyle.Long,
                SearchAddress.FormatStyle.Full
            ).forEach {
                When("Format Style is $it") {
                    val expectedFormattedAddress = when (it) {
                        is SearchAddress.FormatStyle.Short -> {
                            address.street
                        }
                        is SearchAddress.FormatStyle.Medium -> {
                            listOf(address.street, address.place).joinToString()
                        }
                        is SearchAddress.FormatStyle.Long -> {
                            listOf(
                                address.street,
                                address.neighborhood,
                                address.locality,
                                address.place,
                                address.district,
                                address.region,
                                address.country
                            ).joinToString()
                        }
                        is SearchAddress.FormatStyle.Full -> {
                            listOf(
                                address.street,
                                address.neighborhood,
                                address.locality,
                                address.place,
                                address.district,
                                address.region,
                                address.country,
                                address.postcode
                            ).joinToString()
                        }
                        else -> error("Undeclared format style: $it")
                    }

                    Then(
                        "Formatted address should be: $expectedFormattedAddress",
                        expectedFormattedAddress,
                        address.formattedAddress(it)
                    )
                }
            }
        }
    }

    @TestFactory
    fun `Check formattedAddress with empty SearchAddress`() = TestCase {
        Given("Empty and null SearchAddresses") {
            listOf(
                EMPTY_SEARCH_ADDRESS,
                NULL_SEARCH_ADDRESS
            ).forEach { address ->
                When("Call formattedAddress for ${address.toStringFull()}") {
                    val actualValue = address.formattedAddress()
                    Then("It should be <null>", null, actualValue)
                }
            }
        }
    }

    @TestFactory
    fun `Check formattedAddress with default style`() = TestCase {
        Given("Full SearchAddresses") {
            val address = FULL_SEARCH_ADDRESS
            When("Call default formattedAddress for $address") {
                val actualValue = address.formattedAddress()
                val expectedValue = "$HOUSE_NUMBER $STREET, $PLACE"
                Then("It should be <$expectedValue>", expectedValue, actualValue)
            }
        }
    }

    @TestFactory
    fun `Check formattedAddress for country with regions`() = TestCase {
        Given("Full SearchAddresses") {
            listOf("united states of america", "united states", "usa").forEach { country ->
                val address = FULL_SEARCH_ADDRESS.copy(country = country)
                When("Call default formattedAddress for country = $country") {
                    val actualValue = address.formattedAddress()
                    val expectedValue = "$HOUSE_NUMBER $STREET, $PLACE, $REGION"
                    Then("It should be <$expectedValue>", expectedValue, actualValue)
                }
            }
        }
    }

    @TestFactory
    fun `Check formattedAddress with different format styles`() = TestCase {
        Given("Full SearchAddresses") {
            val address = FULL_SEARCH_ADDRESS
            mapOf(
                SearchAddress.FormatStyle.Short to "$HOUSE_NUMBER $STREET",
                SearchAddress.FormatStyle.Medium to "$HOUSE_NUMBER $STREET, $PLACE",
                SearchAddress.FormatStyle.Long to "$HOUSE_NUMBER $STREET, $NEIGHBORHOOD, $LOCALITY, $PLACE, $DISTRICT, $REGION, $COUNTRY",
                SearchAddress.FormatStyle.Full to "$HOUSE_NUMBER $STREET, $NEIGHBORHOOD, $LOCALITY, $PLACE, $DISTRICT, $REGION, $COUNTRY, $POSTCODE",
            ).forEach { (style, expectedValue) ->
                When("Call $style formattedAddress for $address") {
                    val actualValue = address.formattedAddress(style)
                    Then("It should be <$expectedValue>", expectedValue, actualValue)
                }
            }
        }
    }

    @TestFactory
    fun `Check toBuilder() function with full SearchAddress`() = TestCase {
        Given("Full SearchAddress") {
            When("Use toBuilder() function and then build new SearchAddress") {
                Then(
                    "Options should be equal",
                    FULL_SEARCH_ADDRESS,
                    FULL_SEARCH_ADDRESS.toBuilder().build()
                )
            }
        }
    }

    private companion object {

        const val HOUSE_NUMBER = "house_number"
        const val STREET = "street"
        const val NEIGHBORHOOD = "neighborhood"
        const val LOCALITY = "locality"
        const val POSTCODE = "postcode"
        const val PLACE = "place"
        const val DISTRICT = "district"
        const val REGION = "region"
        const val COUNTRY = "country"

        val EMPTY_SEARCH_ADDRESS = SearchAddress("", "", "", "", "", "", "", "", "")
        val NULL_SEARCH_ADDRESS = SearchAddress(null, null, null, null, null, null, null, null, null)
        val FULL_SEARCH_ADDRESS = SearchAddress(
            HOUSE_NUMBER,
            STREET,
            NEIGHBORHOOD,
            LOCALITY,
            POSTCODE,
            PLACE,
            DISTRICT,
            REGION,
            COUNTRY
        )
    }
}
