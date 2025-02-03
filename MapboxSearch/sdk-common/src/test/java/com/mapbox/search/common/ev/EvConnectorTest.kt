package com.mapbox.search.common.ev

import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.common.tests.ToStringVerifier
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.Test

@OptIn(MapboxExperimental::class)
internal class EvConnectorTest {

    @Test
    fun `test equals(), hashCode(), toString()`() {
        EqualsVerifier
            .forClass(EvConnector::class.java)
            .verify()

        ToStringVerifier(
            clazz = EvConnector::class,
            includeAllProperties = false
        ).verify()
    }
}
