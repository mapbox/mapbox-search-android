package com.mapbox.search.common.ev

import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.common.tests.ToStringVerifier
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.Test

@OptIn(MapboxExperimental::class)
internal class EvsePublishTokenTypeTest {

    @Test
    fun `test equals(), hashCode(), toString`() {
        EqualsVerifier
            .forClass(EvsePublishTokenType::class.java)
            .verify()

        ToStringVerifier(
            clazz = EvsePublishTokenType::class,
        ).verify()
    }
}
