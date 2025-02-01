package com.mapbox.search.common.ev

import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.common.tests.ToStringVerifier
import com.mapbox.search.common.tests.withPrefabTestBoundingBox
import com.mapbox.search.common.tests.withPrefabTestPoint
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.Test

@OptIn(MapboxExperimental::class)
internal class EvAdditionalGeoLocationTest {

    @Test
    fun `test equals(), hashCode(), toString`() {
        EqualsVerifier
            .forClass(EvAdditionalGeoLocation::class.java)
            .withPrefabTestPoint()
            .withPrefabTestBoundingBox()
            .verify()

        ToStringVerifier(
            clazz = EvAdditionalGeoLocation::class,
        ).verify()
    }
}
