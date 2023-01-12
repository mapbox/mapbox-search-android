package com.mapbox.search.discover

import com.mapbox.search.common.tests.ToStringVerifier
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.Test

internal class DiscoverApiAddressTest {

    @Test
    fun `equals() and hashCode() functions are correct`() {
        EqualsVerifier.forClass(DiscoverApiAddress::class.java)
            .verify()
    }

    @Test
    fun `toString() function is correct`() {
        ToStringVerifier(
            clazz = DiscoverApiAddress::class,
            includeAllProperties = false
        ).verify()
    }
}
