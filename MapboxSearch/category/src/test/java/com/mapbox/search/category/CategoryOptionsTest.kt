package com.mapbox.search.category

import com.mapbox.search.base.defaultLocaleLanguage
import com.mapbox.search.common.tests.ToStringVerifier
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class CategoryOptionsTest {

    @Test
    fun `equals() and hashCode() functions are correct`() {
        EqualsVerifier.forClass(CategoryOptions::class.java)
            .verify()
    }

    @Test
    fun `toString() function is correct`() {
        ToStringVerifier(
            clazz = CategoryOptions::class,
            includeAllProperties = false
        ).verify()
    }

    @Test
    fun `Check default parameters`() {
        val options = CategoryOptions()
        assertEquals(10, options.limit)
        assertEquals(defaultLocaleLanguage(), options.language)
    }
}
