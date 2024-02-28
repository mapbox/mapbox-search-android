package com.mapbox.search.category

import com.mapbox.search.common.tests.ToStringVerifier
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.Test

internal class CategoryAddressTest {

    @Test
    fun `equals() and hashCode() functions are correct`() {
        EqualsVerifier.forClass(CategoryAddress::class.java)
            .verify()
    }

    @Test
    fun `toString() function is correct`() {
        ToStringVerifier(
            clazz = CategoryAddress::class,
            includeAllProperties = false
        ).verify()
    }
}
