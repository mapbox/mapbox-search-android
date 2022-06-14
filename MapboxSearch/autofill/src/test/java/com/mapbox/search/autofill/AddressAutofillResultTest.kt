package com.mapbox.search.autofill

import com.mapbox.search.common.CommonSdkTypeObjectCreators
import com.mapbox.search.common.tests.ReflectionObjectsFactory
import com.mapbox.search.common.tests.ToStringVerifier
import com.mapbox.search.common.withPrefabTestBoundingBox
import com.mapbox.search.common.withPrefabTestPoint
import com.mapbox.test.dsl.TestCase
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.TestFactory

internal class AddressAutofillResultTest {

    @TestFactory
    fun `Check AddressAutofillResult's equals() and hashCode()`() = TestCase {
        Given("${AddressAutofillResult::class.java.simpleName} class") {
            When("Call equals() and hashCode()") {
                Then("equals() and hashCode() should be implemented correctly") {
                    EqualsVerifier.forClass(AddressAutofillResult::class.java)
                        .withPrefabTestPoint()
                        .withPrefabTestBoundingBox()
                        .verify()
                }
            }
        }
    }

    @TestFactory
    fun `Check AddressAutofillResult's toString()`() = TestCase {
        Given("${AddressAutofillResult::class.java.simpleName} class") {
            When("Call toString()") {
                Then("toString() function should be implemented correctly") {
                    ToStringVerifier(
                        clazz = AddressAutofillResult::class,
                        objectsFactory = ReflectionObjectsFactory(
                            extraCreators = CommonSdkTypeObjectCreators.ALL_CREATORS
                        ),
                    ).verify()
                }
            }
        }
    }
}
