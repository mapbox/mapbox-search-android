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
                val expected = CoreRetrieveOptions(null)

                Then("Options should be as expected", expected, actual)
            }
        }
    }

    @TestFactory
    fun `Check SelectOptions mapToCore()`() = TestCase {
        Given("SelectOptions with AttributeSets") {
            val selectOptions = SelectOptions(attributeSets = AttributeSet.values().toList())

            When(".mapToCore()") {
                val actual = selectOptions.mapToCore()
                val expected = CoreRetrieveOptions(CoreAttributeSet.values().toList())

                Then("Options should be as expected", expected, actual)
            }
        }
    }
}
