package com.mapbox.search.autofill

import com.mapbox.search.common.CommonSdkTypeObjectCreators
import com.mapbox.search.common.tests.ReflectionObjectsFactory
import com.mapbox.search.common.tests.ToStringVerifier
import com.mapbox.search.common.withPrefabTestBoundingBox
import com.mapbox.search.common.withPrefabTestPoint
import com.mapbox.search.result.SearchAddress
import com.mapbox.test.dsl.TestCase
import io.mockk.every
import io.mockk.mockk
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.TestFactory

internal class AddressComponentsTest {

    @TestFactory
    fun `Check AddressComponents properties`() = TestCase {
        Given("${AddressComponents::class.java.simpleName} class") {
            When("All AddressComponents properties accessed") {
                val searchAddress = SearchAddress(
                    houseNumber = "5",
                    street = "Rue De Marseille",
                    neighborhood = "Porte-Saint-Martin",
                    locality = "10th arrondissement of Paris",
                    postcode = "75010",
                    place = "Paris",
                    district = "Paris district",
                    region = "Paris region",
                    country = "France"
                )

                val addressComponents = requireNotNull(AddressComponents.fromCoreSdkAddress(searchAddress))

                Then(
                    "houseNumber should be retrieved from SearchAddress",
                    searchAddress.houseNumber,
                    addressComponents.houseNumber
                )

                Then(
                    "street should be retrieved from SearchAddress",
                    searchAddress.street,
                    addressComponents.street
                )

                Then(
                    "neighborhood should be retrieved from SearchAddress",
                    searchAddress.neighborhood,
                    addressComponents.neighborhood
                )

                Then(
                    "locality should be retrieved from SearchAddress",
                    searchAddress.locality,
                    addressComponents.locality
                )

                Then(
                    "postcode should be retrieved from SearchAddress",
                    searchAddress.postcode,
                    addressComponents.postcode
                )

                Then(
                    "place should be retrieved from SearchAddress",
                    searchAddress.place,
                    addressComponents.place
                )

                Then(
                    "district should be retrieved from SearchAddress",
                    searchAddress.district,
                    addressComponents.district
                )

                Then(
                    "region should be retrieved from SearchAddress",
                    searchAddress.region,
                    addressComponents.region
                )

                Then(
                    "country should be retrieved from SearchAddress",
                    searchAddress.country,
                    addressComponents.country
                )
            }
        }
    }

    @TestFactory
    fun `Check AddressComponents fromCoreSdkAddress() function`() = TestCase {
        Given("${AddressComponents::class.java.simpleName} class") {
            When("fromCoreSdkAddress() called with non empty SearchAddress") {
                val searchAddress = SearchAddress(country = "France")

                Then(
                    "fromCoreSdkAddress() should return non null",
                    false,
                    AddressComponents.fromCoreSdkAddress(searchAddress) == null
                )
            }

            When("fromCoreSdkAddress() called with empty SearchAddress") {
                val emptySearchAddress = SearchAddress()

                Then(
                    "fromCoreSdkAddress() should return null",
                    null,
                    AddressComponents.fromCoreSdkAddress(emptySearchAddress)
                )
            }
        }
    }

    @TestFactory
    fun `Check AddressComponents formattedAddress() function`() = TestCase {
        Given("${AddressComponents::class.java.simpleName} class") {
            When("SearchAddress.formattedAddress() returns non null") {
                val formattedAddress = "Rue de Marseille, Paris, France"

                val searchAddress = mockk<SearchAddress>(relaxed = true)
                every { searchAddress.formattedAddress(any()) } returns formattedAddress
                every { searchAddress.country } returns "France"

                val addressComponents = requireNotNull(AddressComponents.fromCoreSdkAddress(searchAddress))

                Then(
                    "formattedAddress() should return $formattedAddress",
                    formattedAddress,
                    addressComponents.formattedAddress()
                )
            }

            When("SearchAddress.formattedAddress() returns null") {
                val searchAddress = mockk<SearchAddress>(relaxed = true)
                every { searchAddress.formattedAddress(any()) } returns null
                every { searchAddress.country } returns "France"

                val addressComponents = requireNotNull(AddressComponents.fromCoreSdkAddress(searchAddress))
                Then(
                    "formattedAddress() should return toString()",
                    addressComponents.toString(),
                    addressComponents.formattedAddress()
                )
            }
        }
    }

    @TestFactory
    fun `Check AddressComponents equals() and hashCode()`() = TestCase {
        Given("${AddressComponents::class.java.simpleName} class") {
            When("Call equals() and hashCode()") {
                Then("equals() and hashCode() should be implemented correctly") {
                    EqualsVerifier.forClass(AddressComponents::class.java)
                        .withPrefabTestPoint()
                        .withPrefabTestBoundingBox()
                        .verify()
                }
            }
        }
    }

    @TestFactory
    fun `Check AddressComponents toString()`() = TestCase {
        Given("${AddressComponents::class.java.simpleName} class") {
            When("Call toString()") {
                Then("toString() function should be implemented correctly") {
                    ToStringVerifier(
                        clazz = AddressComponents::class,
                        objectsFactory = ReflectionObjectsFactory(
                            extraCreators = CommonSdkTypeObjectCreators.ALL_CREATORS
                        ),
                        ignoredProperties = listOf("coreSdkAddress"),
                    ).verify()
                }
            }
        }
    }
}
