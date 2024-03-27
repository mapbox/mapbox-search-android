package com.mapbox.search.discover

import com.mapbox.search.common.tests.ToStringVerifier
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class DiscoverQueryTest {

    @Test
    fun `equals() and hashCode() functions are correct`() {
        EqualsVerifier.forClass(DiscoverQuery.Category::class.java)
            .verify()
    }

    @Test
    fun `toString() function is correct`() {
        ToStringVerifier(
            clazz = DiscoverQuery.Category::class,
            includeAllProperties = false
        ).verify()
    }

    @Test
    fun `Check Category instantiation`() {
        val testCanonicalName = "test-canonical-name"
        val categoryQuery = DiscoverQuery.Category.create(testCanonicalName)
        assertEquals(testCanonicalName, categoryQuery.canonicalName)
    }
}
