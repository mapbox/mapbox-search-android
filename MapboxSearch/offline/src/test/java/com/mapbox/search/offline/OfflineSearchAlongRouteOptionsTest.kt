package com.mapbox.search.offline

import com.mapbox.annotation.MapboxExperimental
import com.mapbox.geojson.Point
import com.mapbox.search.common.tests.ToStringVerifier
import com.mapbox.search.common.tests.withPrefabTestPoint
import com.mapbox.test.dsl.TestCase
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.TestFactory

@OptIn(MapboxExperimental::class)
internal class OfflineSearchAlongRouteOptionsTest {

    @TestFactory
    fun `Check OfflineSearchAlongRouteOptions equals(), hashCode(), and toString()`() = TestCase {
        Given("${OfflineSearchAlongRouteOptions::class.java.simpleName} class") {
            When("equals(), hashCode(), toString() called") {
                Then("equals() and hashCode() should be implemented correctly") {
                    EqualsVerifier.forClass(OfflineSearchAlongRouteOptions::class.java)
                        .withPrefabTestPoint()
                        .verify()

                    Then("toString() function should be implemented correctly") {
                        ToStringVerifier(
                            clazz = OfflineSearchAlongRouteOptions::class,
                            includeAllProperties = false
                        ).verify()
                    }
                }
            }
        }
    }

    @TestFactory
    fun `Throws exception if number of route points is less than 2`() = TestCase {
        Given("${OfflineSearchAlongRouteOptions::class.java.simpleName} class") {
            When("OfflineSearchAlongRouteOptions instantiated with route of 1 point") {
                Then("Illegal argument exception should be thrown") {
                    try {
                        OfflineSearchAlongRouteOptions(
                            route = listOf(Point.fromLngLat(10.0, 20.0)),
                        )
                        Assertions.fail()
                    } catch (e: IllegalArgumentException) {
                        // ok
                    }
                }
            }
        }
    }
}
