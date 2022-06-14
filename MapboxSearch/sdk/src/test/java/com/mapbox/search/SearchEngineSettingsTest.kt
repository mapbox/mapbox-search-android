package com.mapbox.search

import com.mapbox.search.common.tests.ReflectionObjectsFactory
import com.mapbox.search.common.tests.ToStringVerifier
import com.mapbox.search.tests_support.SdkCustomTypeObjectCreators
import com.mapbox.test.dsl.TestCase
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.TestFactory

internal class SearchEngineSettingsTest {

    private val reflectionObjectFactory = ReflectionObjectsFactory(
        extraCreators = SdkCustomTypeObjectCreators.ALL_CREATORS
    )

    @TestFactory
    fun `Test generated equals(), hashCode() and toString() methods`() = TestCase {
        Given("SearchEngineSettings class") {
            When("equals(), hashCode() and toString() called") {
                Then("equals() and hashCode() functions should use every declared property") {
                    EqualsVerifier.forClass(SearchEngineSettings::class.java).verify()
                }

                Then("toString() function should use every declared property") {
                    ToStringVerifier(
                        clazz = SearchEngineSettings::class,
                        objectsFactory = reflectionObjectFactory,
                        includeAllProperties = false
                    ).verify()
                }
            }
        }
    }
}
