package com.mapbox.search.common.extension

import com.mapbox.test.dsl.TestCase
import org.junit.jupiter.api.TestFactory

internal class DoubleExtensionsTest {

    @TestFactory
    fun `check safeCompareTo`() = TestCase {
        Given("Double.safeCompareTo() Extension") {
            listOf(
                Triple(null, null, true),
                Triple(null, 1.0, false),
                Triple(1.0, null, false),
                Triple(Double.NaN, Double.NaN, true),
                Triple(Double.NaN, 1.0, false),
                Triple(1.0, Double.NaN, false),
                Triple(.0, .0, true),
                Triple(1.0, 1.0, true),
                Triple(-1.0, -1.0, true),
                Triple(Double.MAX_VALUE, Double.MAX_VALUE, true),
                Triple(Double.MIN_VALUE, Double.MIN_VALUE, true),
            ).forEach { (a, b, result) ->
                When("$a.safeCompareTo($b) called") {
                    Then("Result should be $result", result, a.safeCompareTo(b))
                }
            }
        }
    }
}
