package com.mapbox.search

import com.mapbox.geojson.Point
import com.mapbox.search.common.tests.ReflectionObjectsFactory
import com.mapbox.search.common.tests.ToStringVerifier
import com.mapbox.search.common.withPrefabTestPoint
import com.mapbox.search.tests_support.SdkCustomTypeObjectCreators
import com.mapbox.search.tests_support.createTestCoreReverseGeoOptions
import com.mapbox.test.dsl.TestCase
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.TestFactory

internal class OfflineReverseGeoOptionsTest {

    private val reflectionObjectFactory = ReflectionObjectsFactory(
        extraCreators = SdkCustomTypeObjectCreators.ALL_CREATORS
    )

    @TestFactory
    fun `Check OfflineReverseGeoOptions's equals() and hashCode()`() = TestCase {
        Given("${OfflineReverseGeoOptions::class.java.simpleName} class") {
            When("Call equals() and hashCode()") {
                Then("equals() and hashCode() should be implemented correctly") {
                    EqualsVerifier.forClass(OfflineReverseGeoOptions::class.java)
                        .withPrefabTestPoint()
                        .verify()
                }
            }
        }
    }

    @TestFactory
    fun `Check OfflineReverseGeoOptions's toString()`() = TestCase {
        Given("${OfflineReverseGeoOptions::class.java.simpleName} class") {
            When("Call toString()") {
                Then("toString() function should be implemented correctly") {
                    ToStringVerifier(
                        clazz = OfflineReverseGeoOptions::class,
                        objectsFactory = reflectionObjectFactory,
                        includeAllProperties = false
                    ).verify()
                }
            }
        }
    }

    @TestFactory
    fun `Check OfflineReverseGeoOptions mapToCore() function`() = TestCase {
        Given("OfflineReverseGeoOptions builder") {
            When("Build new OfflineReverseGeoOptions with all values set") {
                val actualOptions = OfflineReverseGeoOptions(center = TEST_POINT).mapToCore()
                val expectedOptions = createTestCoreReverseGeoOptions(point = TEST_POINT)
                Then("Options should be equal", expectedOptions, actualOptions)
            }
        }
    }

    private companion object {
        val TEST_POINT: Point = Point.fromLngLat(10.0, 10.0)
    }
}
