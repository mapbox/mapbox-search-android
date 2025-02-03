package com.mapbox.search.common

import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.common.tests.ToStringVerifier
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.Test

@OptIn(MapboxExperimental::class)
internal class BusinessDetailsTest {

    @Test
    fun `test equals(), hashCode(), toString()`() {
        EqualsVerifier
            .forClass(BusinessDetails::class.java)
            .verify()

        ToStringVerifier(
            clazz = BusinessDetails::class,
            includeAllProperties = false
        ).verify()
    }
}
