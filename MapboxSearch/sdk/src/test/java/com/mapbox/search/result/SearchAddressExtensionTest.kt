package com.mapbox.search.result

import com.mapbox.search.tests_support.createBaseSearchAddress
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
                    Then("Region should be <${expectedValue.region}> ", expectedValue.region, actualValue.region?.name)
                    Then("Country should be <${expectedValue.country}> ", expectedValue.country, actualValue.country?.name)
                }
            }
        }
    }

    @TestFactory
    fun `Check mapping SearchAddress to base`() = TestCase {
        Given("SearchAddressExtension") {
            generatePlatformTestData().forEach { (inputValue, expectedValue) ->
                When("Convert ${inputValue.toStringFull()} to core") {
                    val actualValue = inputValue.mapToBase()
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
            generateBaseTestData().forEach { (inputValue, expectedValue) ->
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

            When("Convert BaseSearchAddress with empty fields to platform") {
                val core = createBaseSearchAddress(defaultValue = "")
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

        val baseSearchAddress1 = createBaseSearchAddress(houseNumber = "houseNumber")
        val baseSearchAddress2 = createBaseSearchAddress(street = "street")
        val baseSearchAddress3 = createBaseSearchAddress(neighborhood = "neighborhood")
        val baseSearchAddress4 = createBaseSearchAddress(locality = "locality")
        val baseSearchAddress5 = createBaseSearchAddress(postcode = "postcode")
        val baseSearchAddress6 = createBaseSearchAddress(place = "place")
        val baseSearchAddress7 = createBaseSearchAddress(district = "district")
        val baseSearchAddress8 = createBaseSearchAddress(region = "region")
        val baseSearchAddress9 = createBaseSearchAddress(country = "country")

        fun generatePlatformTestData() = mapOf(
            searchAddress1 to baseSearchAddress1,
            searchAddress2 to baseSearchAddress2,
            searchAddress3 to baseSearchAddress3,
            searchAddress4 to baseSearchAddress4,
            searchAddress5 to baseSearchAddress5,
            searchAddress6 to baseSearchAddress6,
            searchAddress7 to baseSearchAddress7,
            searchAddress8 to baseSearchAddress8,
            searchAddress9 to baseSearchAddress9,
        )

       fun generateBaseTestData() = mapOf(
            baseSearchAddress1 to searchAddress1,
            baseSearchAddress2 to searchAddress2,
            baseSearchAddress3 to searchAddress3,
            baseSearchAddress4 to searchAddress4,
            baseSearchAddress5 to searchAddress5,
            baseSearchAddress6 to searchAddress6,
            baseSearchAddress7 to searchAddress7,
            baseSearchAddress8 to searchAddress8,
            baseSearchAddress9 to searchAddress9,
        )
    }
}
