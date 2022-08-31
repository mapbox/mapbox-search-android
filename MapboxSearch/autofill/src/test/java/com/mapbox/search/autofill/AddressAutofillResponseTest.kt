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
import nl.jqno.equalsverifier.api.SingleTypeEqualsVerifierApi
import org.junit.jupiter.api.TestFactory

internal class AddressAutofillResponseTest {

    @TestFactory
    fun `Check AddressAutofillResponse Suggestion's equals() and hashCode()`() = TestCase {
        Given("${AddressAutofillResponse.Suggestions::class.java.simpleName} class") {
            When("Call equals() and hashCode()") {
                Then("equals() and hashCode() should be implemented correctly") {
                    EqualsVerifier.forClass(AddressAutofillResponse.Suggestions::class.java)
                        .withPrefabTestPoint()
                        .withPrefabTestBoundingBox()
                        .withPrefabTestAddressAutofillResponse()
                        .verify()
                }
            }
        }
    }

    @TestFactory
    fun `Check AddressAutofillResponse Error's equals() and hashCode()`() = TestCase {
        Given("${AddressAutofillResponse.Error::class.java.simpleName} class") {
            When("Call equals() and hashCode()") {
                Then("equals() and hashCode() should be implemented correctly") {
                    EqualsVerifier.forClass(AddressAutofillResponse.Error::class.java)
                        .verify()
                }
            }
        }
    }

    @TestFactory
    fun `Check AddressAutofillResponse's toString()`() = TestCase {
        Given("${AddressAutofillResponse::class.java.simpleName} class") {
            When("Call toString()") {
                Then("toString() function should be implemented correctly") {
                    ToStringVerifier(
                        clazz = AddressAutofillResponse::class,
                        objectsFactory = ReflectionObjectsFactory(
                            extraCreators = CommonSdkTypeObjectCreators.ALL_CREATORS
                        ),
                    ).verify()
                }
            }
        }
    }

    private companion object {
        fun <T> SingleTypeEqualsVerifierApi<T>.withPrefabTestAddressAutofillResponse(): SingleTypeEqualsVerifierApi<T> {
            val red = AddressAutofillResponse.Suggestions(suggestions = emptyList())
            val blue = AddressAutofillResponse.Suggestions(
                suggestions = listOf(
                    AddressAutofillSuggestion(
                        formattedAddress = "test address",
                        coordinate = Point.fromLngLat(10.0, 20.0),
                        address = AddressComponents.fromCoreSdkAddress(BaseSearchAddress(country = "test"))!!
                    )
                )
            )
            return withPrefabValues(AddressAutofillResponse::class.java, red, blue)
        }
    }
}
