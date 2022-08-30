package com.mapbox.search.offline

import com.mapbox.search.common.tests.ToStringVerifier
import com.mapbox.test.dsl.TestCase
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.TestFactory

internal class OfflineResponseInfoTest {

    @TestFactory
    fun `Test equals(), hashCode(), and toString() methods`() = TestCase {
        Given("${OfflineResponseInfo::class.java.simpleName} class") {
            When("equals(), hashCode(), toString() called") {
                Then("equals() and hashCode() functions should use every declared property") {
                    EqualsVerifier.forClass(OfflineResponseInfo::class.java).verify()
                }

                Then("toString() function should use every declared property") {
                    ToStringVerifier(
                        clazz = OfflineResponseInfo::class,
                        includeAllProperties = false
                    ).verify()
                }
            }
        }
    }
}
