package com.mapbox.search

import com.mapbox.search.base.core.CoreAttributeSet
import com.mapbox.search.base.core.CoreRetrieveOptions
import com.mapbox.test.dsl.TestCase
import org.junit.jupiter.api.TestFactory

internal class SelectOptionsTest {
    @TestFactory
    fun `Check empty SelectOptions mapToCore()`() = TestCase {
        Given("empty SelectOptions") {
            val selectOptions = SelectOptions()

            When(".mapToCore()") {
                val actual = selectOptions.mapToCore()
                val expected = CoreRetrieveOptions(null, null)

                Then("Options should be as expected", expected, actual)
            }
        }
    }

    @TestFactory
    fun `Check default SelectOptions values`() = TestCase {
        Given("empty SelectOptions") {
            val selectOptions = SelectOptions()

            When("SelectOptions created with default values") {
                Then("addResultToHistory should be true", true, selectOptions.addResultToHistory)
                Then("attributeSets should be null", null, selectOptions.attributeSets)
                Then("unsafeParameters should be null", null, selectOptions.unsafeParameters)
            }
        }
    }

    @TestFactory
    fun `Check SelectOptions mapToCore()`() = TestCase {
        Given("SelectOptions with AttributeSets") {
            val selectOptions = SelectOptions(attributeSets = AttributeSet.values().toList())

            When(".mapToCore()") {
                val actual = selectOptions.mapToCore()
                val expected = CoreRetrieveOptions(CoreAttributeSet.values().toList(), null)

                Then("Options should be as expected", expected, actual)
            }
        }
    }

    @TestFactory
    fun `Check SelectOptions with unsafeParameters mapToCore()`() = TestCase {
        Given("SelectOptions with unsafeParameters") {
            val selectOptions = SelectOptions(unsafeParameters = TEST_UNSAFE_PARAMETERS)

            When(".mapToCore()") {
                val actual = selectOptions.mapToCore()
                val expected = CoreRetrieveOptions(null, HashMap(TEST_UNSAFE_PARAMETERS))

                Then("Options should be as expected", expected, actual)
            }
        }

        Given("SelectOptions with empty unsafeParameters") {
            val selectOptions = SelectOptions(unsafeParameters = emptyMap())

            When(".mapToCore()") {
                val actual = selectOptions.mapToCore()
                val expected = CoreRetrieveOptions(null, HashMap<String, String>())

                Then("Options should be as expected", expected, actual)
            }
        }

        Given("SelectOptions with all the properties set") {
            val selectOptions = SelectOptions(
                addResultToHistory = false,
                attributeSets = AttributeSet.values().toList(),
                unsafeParameters = TEST_UNSAFE_PARAMETERS,
            )

            When(".mapToCore()") {
                val actual = selectOptions.mapToCore()
                val expected = CoreRetrieveOptions(
                    CoreAttributeSet.values().toList(),
                    HashMap(TEST_UNSAFE_PARAMETERS),
                )

                Then("Options should be as expected", expected, actual)
            }
        }
    }

    private companion object {

        val TEST_UNSAFE_PARAMETERS = mapOf(
            "unsafe-key-1" to "unsafe-value-1",
            "unsafe-key-2" to "unsafe-value-2",
        )
    }
}
