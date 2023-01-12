package com.mapbox.search.discover

import com.mapbox.search.common.tests.ToStringVerifier
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class DiscoverApiQueryTest {

    @Test
    fun `equals() and hashCode() functions are correct`() {
        EqualsVerifier.forClass(DiscoverApiQuery.Category::class.java)
            .verify()
    }

    @Test
    fun `toString() function is correct`() {
        ToStringVerifier(
            clazz = DiscoverApiQuery.Category::class,
            includeAllProperties = false
        ).verify()
    }

    @Test
    fun `Check Category instantiation`() {
        val testCanonicalName = "test-canonical-name"
        val categoryQuery = DiscoverApiQuery.Category.create(testCanonicalName)
        assertEquals(testCanonicalName, categoryQuery.canonicalName)
    }
}
