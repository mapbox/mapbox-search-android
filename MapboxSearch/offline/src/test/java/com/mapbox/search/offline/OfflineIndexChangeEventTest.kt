package com.mapbox.search.offline

import com.mapbox.search.base.core.CoreOfflineIndexChangeEvent
import com.mapbox.search.base.core.CoreOfflineIndexChangeEventType
import com.mapbox.search.common.tests.ToStringVerifier
import com.mapbox.test.dsl.TestCase
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.TestFactory

internal class OfflineIndexChangeEventTest {

    @TestFactory
    fun `Check generated equals(), hashCode() and toString() methods`() = TestCase {
        Given("${OfflineIndexChangeEvent::class.java.simpleName} class") {
            When("equals(), hashCode(), toString() called") {
                Then("equals() and hashCode() functions should use every declared property") {
                    EqualsVerifier.forClass(OfflineIndexChangeEvent::class.java).verify()
                }

                Then("toString() function should use every declared property") {
                    ToStringVerifier(
                        clazz = OfflineIndexChangeEvent::class,
                        includeAllProperties = false
                    ).verify()
                }
            }
        }
    }

    @TestFactory
    fun `Check CoreOfflineIndexChangeEvent mapping to platform type`() = TestCase {
        Given("Core OfflineIndexChangeEvent instance") {
            val core = CoreOfflineIndexChangeEvent(
                CoreOfflineIndexChangeEventType.ADDED,
                "test region id",
                "test dataset",
                "test version"
            )

            When("Map core type to platform") {
                val platform = OfflineIndexChangeEvent(
                    type = OfflineIndexChangeEvent.EventType.ADD,
                    regionId = "test region id",
                    dataset = "test dataset",
                    version = "test version"
                )

                Then("Mapped platform event should be correct", platform, core.mapToPlatformType())
            }
        }
    }

    @TestFactory
    fun `Check CoreOfflineIndexChangeEventType mapping to platform type`() = TestCase {
        Given("CoreOfflineIndexChangeEventType values") {
            CoreOfflineIndexChangeEventType.values().forEach { coreType ->
                When("Map core type $coreType to platform") {
                    val platformType = when (coreType) {
                        CoreOfflineIndexChangeEventType.ADDED -> OfflineIndexChangeEvent.EventType.ADD
                        CoreOfflineIndexChangeEventType.UPDATED -> OfflineIndexChangeEvent.EventType.UPDATE
                        CoreOfflineIndexChangeEventType.REMOVED -> OfflineIndexChangeEvent.EventType.REMOVE
                    }

                    Then("Mapped platform type should be $platformType", platformType, coreType.mapToPlatformType())
                }
            }
        }
    }
}
