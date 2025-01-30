@file:OptIn(MapboxExperimental::class)

package com.mapbox.search.offline

import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.base.core.CoreConnectorType
import com.mapbox.search.base.core.createCoreEvSearchOptions
import com.mapbox.search.common.ev.EvConnectorType
import com.mapbox.search.common.tests.CommonSdkTypeObjectCreators
import com.mapbox.search.common.tests.ReflectionObjectsFactory
import com.mapbox.search.common.tests.ToStringVerifier
import com.mapbox.test.dsl.TestCase
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.TestFactory

internal class OfflineEvSearchOptionsTest {

    private val reflectionObjectFactory = ReflectionObjectsFactory(
        extraCreators = CommonSdkTypeObjectCreators.ALL_CREATORS
    )

    @TestFactory
    fun `Check OfflineEvSearchOptions equals(), hashCode(), and toString()`() = TestCase {
        Given("${OfflineEvSearchOptions::class.java.simpleName} class") {
            When("equals(), hashCode(), toString() called") {
                Then("equals() and hashCode() should be implemented correctly") {
                    EqualsVerifier.forClass(OfflineEvSearchOptions::class.java)
                        .verify()

                    Then("toString() function should be implemented correctly") {
                        ToStringVerifier(
                            clazz = OfflineEvSearchOptions::class,
                            objectsFactory = reflectionObjectFactory,
                            includeAllProperties = false
                        ).verify()
                    }
                }
            }
        }
    }

    @TestFactory
    fun `Check OfflineEvSearchOptions default constructor`() = TestCase {
        Given("OfflineEvSearchOptions built with default constructor") {
            When("Build new OfflineEvSearchOptions with default values") {
                val options = OfflineEvSearchOptions()

                val expectedOptions = OfflineEvSearchOptions(
                    connectorTypes = null,
                    operators = null,
                    minChargingPower = null,
                    maxChargingPower = null,
                )

                Then("Options should be equal", expectedOptions, options)
            }
        }
    }

    @TestFactory
    fun `Check OfflineEvSearchOptions mapToCore() function`() = TestCase {
        Given("OfflineEvSearchOptions with all values initialized") {
            val evSearchOptions = OfflineEvSearchOptions(
                connectorTypes = listOf(EvConnectorType.TESLA_S, EvConnectorType.TESLA_R),
                operators = listOf("test-operator"),
                minChargingPower = 1000f,
                maxChargingPower = 10000f,
            )

            When("mapToCore() called") {
                val core = evSearchOptions.mapToCore()

                val expected = createCoreEvSearchOptions(
                    connectorTypes = listOf(CoreConnectorType.TESLA_S, CoreConnectorType.TESLA_R),
                    operators = listOf("test-operator"),
                    minChargingPower = 1000f,
                    maxChargingPower = 10000f,
                )

                Then("Options should be as expected", expected, core)
            }
        }
    }
}
