package com.mapbox.search.offline

import com.mapbox.annotation.MapboxExperimental
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.search.base.core.createCoreSearchOptions
import com.mapbox.search.base.utils.extension.mapToCore
import com.mapbox.search.common.ev.EvConnectorType
import com.mapbox.search.common.tests.ToStringVerifier
import com.mapbox.search.common.tests.catchThrowable
import com.mapbox.search.common.tests.withPrefabTestPoint
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

@OptIn(MapboxExperimental::class)
internal class OfflineCategorySearchOptionsTest {

    @Test
    fun `equals() and hashCode() functions are correct`() {
        EqualsVerifier.forClass(OfflineCategorySearchOptions::class.java)
            .withPrefabTestPoint()
            .verify()

        ToStringVerifier(
            clazz = OfflineCategorySearchOptions::class,
            includeAllProperties = false
        ).verify()
    }

    @Test
    fun `check empty OfflineCategorySearchOptions mapToCore() function`() {
        val actualOptions = OfflineCategorySearchOptions().mapToCore()
        val expectedOptions = createCoreSearchOptions()
        assertEquals(expectedOptions, actualOptions)
    }

    @Test
    fun `check filled OfflineCategorySearchOptions mapToCore() function`() {
        val actualOptions = OfflineCategorySearchOptions(
            proximity = TEST_POINT,
            limit = 100,
            origin = TEST_ORIGIN_POINT,
            boundingBox = TEST_BOUNDING_BOX,
            evSearchOptions = TEST_EV_OPTIONS,
            ensureResultsPerCategory = true,
        ).mapToCore()

        val expectedOptions = createCoreSearchOptions(
            proximity = TEST_POINT,
            limit = 100,
            origin = TEST_ORIGIN_POINT,
            bbox = TEST_BOUNDING_BOX.mapToCore(),
            evSearchOptions = TEST_EV_OPTIONS.mapToCore(),
            ensureResultsPerCategory = true,
        )

        assertEquals(expectedOptions, actualOptions)
    }

    @Test
    fun `check throws exception when limit is negative`() {
        val e = catchThrowable<IllegalStateException> {
            OfflineCategorySearchOptions(
                limit = -1
            )
        }
        assertNotNull(e)
        assertEquals(
            "Provided limit should be greater than 0 (was found: -1).",
            e?.message
        )
    }

    private companion object {
        val TEST_POINT: Point = Point.fromLngLat(10.0, 10.0)
        val TEST_ORIGIN_POINT: Point = Point.fromLngLat(20.0, 20.0)
        val TEST_BOUNDING_BOX: BoundingBox = BoundingBox.fromLngLats(0.0, 0.0, 90.0, 45.0)
        val TEST_EV_OPTIONS = OfflineEvSearchOptions(
            connectorTypes = listOf(EvConnectorType.TESLA_S, EvConnectorType.TESLA_R),
            operators = listOf("test-operator"),
            minChargingPower = 1000f,
            maxChargingPower = 10000f,
        )
    }
}
