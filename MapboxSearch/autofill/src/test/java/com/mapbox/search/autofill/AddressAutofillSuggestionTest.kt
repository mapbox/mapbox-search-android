package com.mapbox.search.autofill

import com.mapbox.geojson.Point
import com.mapbox.search.base.result.BaseSearchAddress
import com.mapbox.search.common.CommonSdkTypeObjectCreators
import com.mapbox.search.common.tests.ReflectionObjectsFactory
import com.mapbox.search.common.tests.ToStringVerifier
import com.mapbox.search.common.withPrefabTestBoundingBox
import com.mapbox.search.common.withPrefabTestPoint
import com.mapbox.test.dsl.TestCase
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.TestFactory

internal class AddressAutofillSuggestionTest {

    @TestFactory
    fun `Check AddressAutofillSuggestion result() function`() = TestCase {
        Given("${AddressAutofillSuggestion::class.java.simpleName} instance") {
            val name = "Test name"
            val formattedAddress = "Test formatted address"
            val coordinate = Point.fromLngLat(10.0, 11.0)
            val address = AddressComponents.fromCoreSdkAddress(BaseSearchAddress(country = "test"))!!

            val suggestion = AddressAutofillSuggestion(name, formattedAddress, coordinate, address)

            When("result() function called") {
                val result = AddressAutofillResult(suggestion, address)

                Then("Returned value should be $result",
                    suggestion.result(),
                    result
                )
            }
        }
    }

    @TestFactory
    fun `Check AddressAutofillSuggestion's equals() and hashCode()`() = TestCase {
        Given("${AddressAutofillSuggestion::class.java.simpleName} class") {
            When("Call equals() and hashCode()") {
                Then("equals() and hashCode() should be implemented correctly") {
                    EqualsVerifier.forClass(AddressAutofillSuggestion::class.java)
                        .withPrefabTestPoint()
                        .withPrefabTestBoundingBox()
                        .verify()
                }
            }
        }
    }

    @TestFactory
    fun `Check AddressAutofillSuggestion's toString()`() = TestCase {
        Given("${AddressAutofillSuggestion::class.java.simpleName} class") {
            When("Call toString()") {
                Then("toString() function should be implemented correctly") {
                    ToStringVerifier(
                        clazz = AddressAutofillSuggestion::class,
                        objectsFactory = ReflectionObjectsFactory(
                            extraCreators = CommonSdkTypeObjectCreators.ALL_CREATORS
                        ),
                        ignoredProperties = listOf("address"),
                    ).verify()
                }
            }
        }
    }
}
