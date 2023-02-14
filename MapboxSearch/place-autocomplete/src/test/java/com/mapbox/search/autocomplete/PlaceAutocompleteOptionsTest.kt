package com.mapbox.search.autocomplete

import com.mapbox.search.autocomplete.test.utils.TypeObjectCreator
import com.mapbox.search.base.defaultLocaleLanguage
import com.mapbox.search.common.CommonSdkTypeObjectCreators
import com.mapbox.search.common.tests.ReflectionObjectsFactory
import com.mapbox.search.common.tests.ToStringVerifier
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class PlaceAutocompleteOptionsTest {

    @Test
    fun `equals() and hashCode() functions are correct`() {
        EqualsVerifier.forClass(PlaceAutocompleteOptions::class.java)
            .verify()
    }

    @Test
    fun `toString() function is correct`() {
        ToStringVerifier(
            clazz = PlaceAutocompleteOptions::class,
            objectsFactory = ReflectionObjectsFactory(
                extraCreators = listOf(TypeObjectCreator.PLACE_TYPE_CREATOR)
            ),
            includeAllProperties = false
        ).verify()
    }

    @Test
    fun `check default options`() {
        val options = PlaceAutocompleteOptions()
        val filledOptions = PlaceAutocompleteOptions(
            limit = 10,
            countries = null,
            language = defaultLocaleLanguage(),
            types = null,
        )
        assertEquals(filledOptions, options)
    }
}
