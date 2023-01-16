package com.mapbox.search.autofill

import com.mapbox.search.common.IsoLanguageCode
import com.mapbox.search.common.tests.ToStringVerifier
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Locale

internal class AddressAutofillOptionsTest {

    @Test
    fun `equals() and hashCode() functions are correct`() {
        EqualsVerifier.forClass(AddressAutofillOptions::class.java)
            .verify()
    }

    @Test
    fun `toString() function is correct`() {
        ToStringVerifier(
            clazz = AddressAutofillOptions::class,
            includeAllProperties = false
        ).verify()
    }

    @Test
    fun `Check default constructor parameters`() {
        val options = AddressAutofillOptions(countries = null)

        assertEquals(null, options.countries)
        assertEquals(IsoLanguageCode(Locale.getDefault().language), options.language)
    }
}
