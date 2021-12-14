package com.mapbox.search.result

import com.mapbox.search.tests_support.createCoreSearchAddress
import com.mapbox.search.tests_support.createSearchAddress
import com.mapbox.search.tests_support.toStringFull
import com.mapbox.test.dsl.TestCase
import org.junit.jupiter.api.TestFactory

internal class SearchAddressExtensionTest {

    @TestFactory
    fun `Check mapping SearchAddress to core`() = TestCase {
        Given("SearchAddressExtension") {
            generatePlatformTestData().forEach { (inputValue, expectedValue) ->
                When("Convert ${inputValue.toStringFull()} to core") {
                    val actualValue = inputValue.mapToCore()
                    Then("HouseNumber should be <${expectedValue.houseNumber}> ", expectedValue.houseNumber, actualValue.houseNumber)
                    Then("Street should be <${expectedValue.street}> ", expectedValue.street, actualValue.street)
                    Then("Neighborhood should be <${expectedValue.neighborhood}> ", expectedValue.neighborhood, actualValue.neighborhood)
                    Then("Locality should be <${expectedValue.locality}> ", expectedValue.locality, actualValue.locality)
                    Then("Postcode should be <${expectedValue.postcode}> ", expectedValue.postcode, actualValue.postcode)
                    Then("Place should be <${expectedValue.place}> ", expectedValue.place, actualValue.place)
                    Then("District should be <${expectedValue.district}> ", expectedValue.district, actualValue.district)
                    Then("Region should be <${expectedValue.region}> ", expectedValue.region, actualValue.region)
                    Then("Country should be <${expectedValue.country}> ", expectedValue.country, actualValue.country)
                }
            }
        }
    }

    @TestFactory
    fun `Check mapping SearchAddress to platform`() = TestCase {
        Given("SearchAddressExtension") {
            generateCoreTestData().forEach { (inputValue, expectedValue) ->
                When("Convert ${expectedValue.toStringFull()} to platform") {
                    val actualValue = inputValue.mapToPlatform()
                    Then("HouseNumber should be <${expectedValue.houseNumber}> ", expectedValue.houseNumber, actualValue.houseNumber)
                    Then("Street should be <${expectedValue.street}> ", expectedValue.street, actualValue.street)
                    Then("Neighborhood should be <${expectedValue.neighborhood}> ", expectedValue.neighborhood, actualValue.neighborhood)
                    Then("Locality should be <${expectedValue.locality}> ", expectedValue.locality, actualValue.locality)
                    Then("Postcode should be <${expectedValue.postcode}> ", expectedValue.postcode, actualValue.postcode)
                    Then("Place should be <${expectedValue.place}> ", expectedValue.place, actualValue.place)
                    Then("District should be <${expectedValue.district}> ", expectedValue.district, actualValue.district)
                    Then("Region should be <${expectedValue.region}> ", expectedValue.region, actualValue.region)
                    Then("Country should be <${expectedValue.country}> ", expectedValue.country, actualValue.country)
                }
            }

            When("Convert CoreSearchAddress with empty fields to platform") {
                val core = createCoreSearchAddress(defaultValue = "")
                val platform = createSearchAddress(defaultValue = null)

                Then("HouseNumber should be $platform", platform, core.mapToPlatform())
            }
        }
    }

    private companion object {

        val searchAddress1 = createSearchAddress(houseNumber = "houseNumber")
        val searchAddress2 = createSearchAddress(street = "street")
        val searchAddress3 = createSearchAddress(neighborhood = "neighborhood")
        val searchAddress4 = createSearchAddress(locality = "locality")
        val searchAddress5 = createSearchAddress(postcode = "postcode")
        val searchAddress6 = createSearchAddress(place = "place")
        val searchAddress7 = createSearchAddress(district = "district")
        val searchAddress8 = createSearchAddress(region = "region")
        val searchAddress9 = createSearchAddress(country = "country")

        val coreSearchAddress1 = createCoreSearchAddress(houseNumber = "houseNumber")
        val coreSearchAddress2 = createCoreSearchAddress(street = "street")
        val coreSearchAddress3 = createCoreSearchAddress(neighborhood = "neighborhood")
        val coreSearchAddress4 = createCoreSearchAddress(locality = "locality")
        val coreSearchAddress5 = createCoreSearchAddress(postcode = "postcode")
        val coreSearchAddress6 = createCoreSearchAddress(place = "place")
        val coreSearchAddress7 = createCoreSearchAddress(district = "district")
        val coreSearchAddress8 = createCoreSearchAddress(region = "region")
        val coreSearchAddress9 = createCoreSearchAddress(country = "country")

        fun generatePlatformTestData() = mapOf(
            searchAddress1 to coreSearchAddress1,
            searchAddress2 to coreSearchAddress2,
            searchAddress3 to coreSearchAddress3,
            searchAddress4 to coreSearchAddress4,
            searchAddress5 to coreSearchAddress5,
            searchAddress6 to coreSearchAddress6,
            searchAddress7 to coreSearchAddress7,
            searchAddress8 to coreSearchAddress8,
            searchAddress9 to coreSearchAddress9,
        )

       fun generateCoreTestData() = mapOf(
            coreSearchAddress1 to searchAddress1,
            coreSearchAddress2 to searchAddress2,
            coreSearchAddress3 to searchAddress3,
            coreSearchAddress4 to searchAddress4,
            coreSearchAddress5 to searchAddress5,
            coreSearchAddress6 to searchAddress6,
            coreSearchAddress7 to searchAddress7,
            coreSearchAddress8 to searchAddress8,
            coreSearchAddress9 to searchAddress9,
        )
    }
}
