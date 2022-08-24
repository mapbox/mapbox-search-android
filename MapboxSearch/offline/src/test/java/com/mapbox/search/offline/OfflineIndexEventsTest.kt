package com.mapbox.search.offline

import com.mapbox.geojson.Point
import com.mapbox.search.common.tests.ReflectionObjectsFactory
import com.mapbox.search.common.tests.ToStringVerifier
import com.mapbox.search.common.tests.isEnum
import com.mapbox.test.dsl.TestCase
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.TestFactory

internal class OfflineIndexEventsTest {

    private val reflectionObjectFactory = ReflectionObjectsFactory()

    @TestFactory
    fun `Test generated equals(), hashCode() and toString() methods`() = TestCase {
        listOf(
            OfflineIndexChangeEvent::class,
            OfflineIndexErrorEvent::class,
            OfflineIndexChangeEvent.EventType::class,
        )
            // Check only `data classes` and `classes`, don't check `objects`
            .filter { it.objectInstance == null }
            .forEach { clazz ->
                Given("${clazz.java.simpleName} class") {
                    When("${clazz.java.simpleName} class") {
                        Then("equals() and hashCode() functions should use every declared property") {
                            EqualsVerifier.forClass(clazz.java)
                                .withPrefabValues(
                                    Point::class.java,
                                    Point.fromLngLat(2.0, 5.0),
                                    Point.fromLngLat(27.55140833333333, 53.911334999999994)
                                )
                                .verify()
                        }

                        if (!isEnum(clazz)) {
                            Then("toString() function should use every declared property") {
                                ToStringVerifier(
                                    clazz = clazz,
                                    objectsFactory = reflectionObjectFactory,
                                    includeAllProperties = false
                                ).verify()
                            }
                        }
                    }
                }
            }
    }
}
