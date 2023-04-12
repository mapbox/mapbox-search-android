package com.mapbox.search.offline

import com.mapbox.geojson.Point
import com.mapbox.search.common.tests.CommonSdkTypeObjectCreators
import com.mapbox.search.common.tests.ReflectionObjectsFactory
import com.mapbox.search.common.tests.ToStringVerifier
import com.mapbox.search.common.tests.createTestCoreSearchOptions
import com.mapbox.search.common.tests.withPrefabTestPoint
import com.mapbox.test.dsl.TestCase
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.TestFactory

internal class OfflineSearchOptionsTest {

    private val reflectionObjectFactory = ReflectionObjectsFactory(
        extraCreators = CommonSdkTypeObjectCreators.ALL_CREATORS
    )

    @TestFactory
    fun `Check OfflineSearchOptions equals(), hashCode(), and toString()`() = TestCase {
        Given("${OfflineSearchOptions::class.java.simpleName} class") {
            When("equals(), hashCode(), toString() called") {
                Then("equals() and hashCode() should be implemented correctly") {
                    EqualsVerifier.forClass(OfflineSearchOptions::class.java)
                        .withPrefabTestPoint()
                        .verify()

                    Then("toString() function should be implemented correctly") {
                        ToStringVerifier(
                            clazz = OfflineSearchOptions::class,
                            objectsFactory = reflectionObjectFactory,
                            includeAllProperties = false
                        ).verify()
                    }
                }
            }
        }
    }

    @TestFactory
    fun `Check OfflineSearchOptions default builder`() = TestCase {
        Given("OfflineSearchOptions builder") {
            When("Build new OfflineSearchOptions with default values") {
                val actualOptions = OfflineSearchOptions.Builder().build()

                val expectedOptions = OfflineSearchOptions(
                    proximity = null,
                    limit = null,
                    origin = null,
                )

                Then("Options should be equal", expectedOptions, actualOptions)
            }
        }
    }

    @TestFactory
    fun `Check OfflineSearchOptions builder with all values set`() = TestCase {
        Given("OfflineSearchOptions builder") {
            When("Build new OfflineSearchOptions with all values set") {

                val actualOptions = OfflineSearchOptions.Builder()
                    .proximity(TEST_POINT)
                    .limit(100)
                    .origin(TEST_ORIGIN_POINT)
                    .build()

                val expectedOptions = OfflineSearchOptions(
                    proximity = TEST_POINT,
                    limit = 100,
                    origin = TEST_ORIGIN_POINT,
                )

                Then("Options should be equal", expectedOptions, actualOptions)
            }
        }
    }

    @TestFactory
    fun `Check empty OfflineSearchOptions mapToCore() function`() = TestCase {
        Given("OfflineSearchOptions builder") {
            When("Build new OfflineSearchOptions with all values set") {
                val actualOptions = OfflineSearchOptions().mapToCore()
                val expectedOptions = createTestCoreSearchOptions()
                Then("Options should be as expected", expectedOptions, actualOptions)
            }
        }
    }

    @TestFactory
    fun `Check filled OfflineSearchOptions mapToCore() function`() = TestCase {
        Given("OfflineSearchOptions builder") {
            When("Build new OfflineSearchOptions with all values set") {
                val actualOptions = OfflineSearchOptions(
                    proximity = TEST_POINT,
                    limit = 100,
                    origin = TEST_ORIGIN_POINT,
                ).mapToCore()

                val expectedOptions = createTestCoreSearchOptions(
                    proximity = TEST_POINT,
                    limit = 100,
                    origin = TEST_ORIGIN_POINT,
                )

                Then("Options should be equal", expectedOptions, actualOptions)
            }
        }
    }

    @TestFactory
    fun `Check filled OfflineSearchOptions toBuilder() function`() = TestCase {
        Given("OfflineSearchOptions") {
            When("Use toBuilder() function and then build new OfflineSearchOptions") {
                val options = OfflineSearchOptions(
                    proximity = TEST_POINT,
                    limit = 100,
                    origin = TEST_ORIGIN_POINT,
                )

                Then("Options should be equal", options, options.toBuilder().build())
            }
        }
    }

    @TestFactory
    fun `Check limit field for OfflineSearchOptions builder and constructor`() = TestCase {
        mapOf(
            1 to 1,
            10 to 10,
            Int.MAX_VALUE to Int.MAX_VALUE,
        ).forEach { (inputValue, expectedValue) ->

            Given("OfflineSearchOptions with limit = $inputValue and creation via builder") {
                val searchOptions = OfflineSearchOptions.Builder()
                    .limit(inputValue).build()

                When("Get limit") {
                    val actualValue = searchOptions.limit
                    Then("It should be <$expectedValue>", expectedValue, actualValue)
                }
            }
        }

        listOf(0, Int.MIN_VALUE, -1).forEach { inputValue ->
            Given("OfflineSearchOptions with limit = $inputValue and creation via builder") {
                WhenThrows("Set limit to builder", IllegalStateException::class) {
                    OfflineSearchOptions.Builder().limit(inputValue).build()
                }
            }

            Given("OfflineSearchOptions with limit = $inputValue and creation via constructor") {
                WhenThrows("Pass limit to constructor", IllegalStateException::class) {
                    OfflineSearchOptions(limit = inputValue)
                }
            }
        }
    }

    private companion object {
        val TEST_POINT: Point = Point.fromLngLat(10.0, 10.0)
        val TEST_ORIGIN_POINT: Point = Point.fromLngLat(20.0, 20.0)
    }
}
