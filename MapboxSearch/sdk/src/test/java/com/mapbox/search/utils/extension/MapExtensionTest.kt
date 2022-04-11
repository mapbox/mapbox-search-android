package com.mapbox.search.utils.extension

import com.mapbox.test.dsl.TestCase
import org.junit.jupiter.api.TestFactory

internal class MapExtensionTest {

    @TestFactory
    fun `Check MutableMap addValue()`() = TestCase {
        Given("Empty map") {
            When("Map.addValue() called with default value-collection creator") {
                val map = mutableMapOf<Int, MutableSet<Int>>()

                map.addValue(1, 1)
                map.addValue(1, 2)
                map.addValue(1, 3)

                Then(
                    "Returned value should contain all added elements",
                    mutableSetOf(1, 2, 3),
                    map.getValue(1)
                )
            }

            When("Map.addValue() called with custom value-collection creator") {
                val map = mutableMapOf<Int, MutableSet<Int>>()

                val valueCollection1 = mutableSetOf<Int>()
                val valueCollection2 = mutableSetOf<Int>()
                map.addValue(1, 1) {
                    valueCollection1
                }
                map.addValue(1, 2) {
                    valueCollection2
                }
                map.addValue(1, 3) {
                    valueCollection2
                }

                Then(
                    "Value-collection should contain all added elements",
                    mutableSetOf(1, 2, 3),
                    valueCollection1
                )

                Then(
                    "Another collection should remain be empty",
                    mutableSetOf<Int>(),
                    valueCollection2
                )
            }
        }
    }
}
