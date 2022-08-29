package com.mapbox.search.offline

import com.mapbox.search.base.core.CoreOfflineIndexError
import com.mapbox.search.common.tests.ToStringVerifier
import com.mapbox.test.dsl.TestCase
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.TestFactory

internal class OfflineIndexErrorEventTest {

    @TestFactory
    fun `Test equals(), hashCode() and toString() methods`() = TestCase {
        Given("${OfflineIndexErrorEvent::class.java.simpleName} class") {
            When("equals(), hashCode(), toString() called") {
                Then("equals() and hashCode() functions should use every declared property") {
                    EqualsVerifier.forClass(OfflineIndexErrorEvent::class.java).verify()
                }

                Then("toString() function should use every declared property") {
                    ToStringVerifier(
                        clazz = OfflineIndexErrorEvent::class,
                        includeAllProperties = false
                    ).verify()
                }
            }
        }
    }

    @TestFactory
    fun `Check CoreOfflineIndexError mapping to platform type`() = TestCase {
        Given("Core OfflineIndexError instance") {
            val core = CoreOfflineIndexError(
                "test region id",
                "test dataset",
                "test version",
                "test tile",
                "test message"
            )

            When("Map core type to platform") {
                val platform = OfflineIndexErrorEvent(
                    regionId = "test region id",
                    dataset = "test dataset",
                    version = "test version",
                    tile = "test tile",
                    message = "test message"
                )

                Then("Mapped platform event should be correct", platform, core.mapToPlatformType())
            }
        }
    }
}
