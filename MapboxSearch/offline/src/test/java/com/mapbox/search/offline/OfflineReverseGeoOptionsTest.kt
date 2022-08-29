package com.mapbox.search.offline

import com.mapbox.geojson.Point
import com.mapbox.search.common.CommonSdkTypeObjectCreators
import com.mapbox.search.common.createTestCoreReverseGeoOptions
import com.mapbox.search.common.tests.ReflectionObjectsFactory
import com.mapbox.search.common.tests.ToStringVerifier
import com.mapbox.search.common.withPrefabTestPoint
import com.mapbox.test.dsl.TestCase
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.TestFactory

internal class OfflineReverseGeoOptionsTest {

    private val reflectionObjectFactory = ReflectionObjectsFactory(
        extraCreators = CommonSdkTypeObjectCreators.ALL_CREATORS
    )

    @TestFactory
    fun `Check OfflineReverseGeoOptions equals(), hashCode(), and toString() methods`() = TestCase {
        Given("${OfflineReverseGeoOptions::class.java.simpleName} class") {
            When("equals(), hashCode(), toString() called") {
                Then("equals() and hashCode() should be implemented correctly") {
                    EqualsVerifier.forClass(OfflineReverseGeoOptions::class.java)
                        .withPrefabTestPoint()
                        .verify()
                }

                Then("toString() function should use every declared property") {
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
