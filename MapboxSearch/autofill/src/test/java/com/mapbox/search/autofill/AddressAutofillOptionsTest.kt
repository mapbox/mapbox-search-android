package com.mapbox.search.autofill

import com.mapbox.search.common.IsoLanguageCode
import com.mapbox.search.common.tests.ToStringVerifier
import com.mapbox.test.dsl.TestCase
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.TestFactory
import java.util.Locale

internal class AddressAutofillOptionsTest {

    @TestFactory
    fun `Check default constructor parameters`() = TestCase {
        Given("AddressAutofillOptions class") {
            When("AddressAutofillOptions object instantiated with default language") {
                val options = AddressAutofillOptions(countries = null)

                Then(
                    "AddressAutofillOptions language should be determined by the current locale",
                    expectedValue = IsoLanguageCode(Locale.getDefault().language),
                    actualValue = options.language
                )
            }
        }
    }

    @TestFactory
    fun `Check AddressAutofillOptions's equals() and hashCode()`() = TestCase {
        Given("${AddressAutofillOptions::class.java.simpleName} class") {
            When("Call AddressAutofillOptions equals() and hashCode()") {
                Then("equals() and hashCode() should be implemented correctly") {
                    EqualsVerifier.forClass(AddressAutofillOptions::class.java)
                        .verify()
                }
            }
        }
    }

    @TestFactory
    fun `Check AddressAutofillOptions's toString()`() = TestCase {
        Given("${AddressAutofillOptions::class.java.simpleName} class") {
            When("Call AddressAutofillOptions toString()") {
                Then("toString() function should be implemented correctly") {
                    ToStringVerifier(
                        clazz = AddressAutofillOptions::class,
                        includeAllProperties = false
                    ).verify()
                }
            }
        }
    }
}
