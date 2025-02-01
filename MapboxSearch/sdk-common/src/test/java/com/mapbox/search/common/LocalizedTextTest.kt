package com.mapbox.search.common

import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.common.tests.ToStringVerifier
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.Test

@OptIn(MapboxExperimental::class)
internal class LocalizedTextTest {

    @Test
    fun `test equals(), hashCode(), toString()`() {
        EqualsVerifier
            .forClass(LocalizedText::class.java)
            .verify()

        ToStringVerifier(
            clazz = LocalizedText::class,
            includeAllProperties = false
        ).verify()
    }
}
