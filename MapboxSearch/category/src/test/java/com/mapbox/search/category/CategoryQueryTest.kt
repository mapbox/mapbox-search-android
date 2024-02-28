package com.mapbox.search.category

import com.mapbox.search.common.tests.ToStringVerifier
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class CategoryQueryTest {

    @Test
    fun `equals() and hashCode() functions are correct`() {
        EqualsVerifier.forClass(CategoryQuery.Category::class.java)
            .verify()
    }

    @Test
    fun `toString() function is correct`() {
        ToStringVerifier(
            clazz = CategoryQuery.Category::class,
            includeAllProperties = false
        ).verify()
    }

    @Test
    fun `Check Category instantiation`() {
        val testCanonicalName = "test-canonical-name"
        val categoryQuery = CategoryQuery.Category.create(testCanonicalName)
        assertEquals(testCanonicalName, categoryQuery.canonicalName)
    }
}
