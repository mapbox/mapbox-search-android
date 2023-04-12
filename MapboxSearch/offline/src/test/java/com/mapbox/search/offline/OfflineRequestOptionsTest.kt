package com.mapbox.search.offline

import com.mapbox.search.common.tests.ToStringVerifier
import com.mapbox.search.common.tests.createTestCoreRequestOptions
import com.mapbox.test.dsl.TestCase
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.TestFactory

internal class OfflineRequestOptionsTest {

    @TestFactory
    fun `Test equals(), hashCode() and toString() methods`() = TestCase {
        Given("${OfflineRequestOptions::class.java.simpleName} class") {
            When("equals(), hashCode(), toString() called") {
                Then("equals() and hashCode() functions should use every declared property") {
                    EqualsVerifier.forClass(OfflineRequestOptions::class.java).verify()
                }

                Then("toString() function should use every declared property") {
                    ToStringVerifier(
                        clazz = OfflineRequestOptions::class,
                        includeAllProperties = false
                    ).verify()
                }
            }
        }
    }

    @TestFactory
    fun `Check CoreRequestOptions mapping to platform type`() = TestCase {
        Given("Core OfflineIndexError instance") {
            val core = createTestCoreRequestOptions(
                query = "test-query",
                proximityRewritten = true,
                originRewritten = true
            )

            When("Map core type to platform") {
                val platform = OfflineRequestOptions(
                    query = "test-query",
                    proximityRewritten = true,
                    originRewritten = true
                )

                Then("Mapped platform options should be correct", platform, core.mapToOfflineSdkType())
            }
        }
    }
}
