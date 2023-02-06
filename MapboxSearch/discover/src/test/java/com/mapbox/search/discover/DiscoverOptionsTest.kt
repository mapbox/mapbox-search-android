package com.mapbox.search.discover

import com.mapbox.search.base.defaultLocaleLanguage
import com.mapbox.search.common.tests.ToStringVerifier
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class DiscoverOptionsTest {

    @Test
    fun `equals() and hashCode() functions are correct`() {
        EqualsVerifier.forClass(DiscoverOptions::class.java)
            .verify()
    }

    @Test
    fun `toString() function is correct`() {
        ToStringVerifier(
            clazz = DiscoverOptions::class,
            includeAllProperties = false
        ).verify()
    }

    @Test
    fun `Check default parameters`() {
        val options = DiscoverOptions()
        assertEquals(10, options.limit)
        assertEquals(defaultLocaleLanguage(), options.language)
    }
}
