package com.mapbox.search

import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.base.core.CoreAttributeSet
import com.mapbox.search.common.tests.ToStringVerifier
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

@OptIn(MapboxExperimental::class)
internal class RetrieveOptionsTest {

    @Test
    fun `Test generated equals(), hashCode() and toString() methods`() {
        EqualsVerifier.forClass(RetrieveOptions::class.java)
            .verify()

        ToStringVerifier(RetrieveOptions::class).verify()
    }

    @Test
    fun `Test default values`() {
        val options = RetrieveOptions()

        assertNull(options.attributeSets)
        assertNull(options.unsafeParameters)
    }

    @Test
    fun `Test mapToCore() function`() {
        val options = RetrieveOptions(
            attributeSets = listOf(AttributeSet.BASIC, AttributeSet.VISIT),
            unsafeParameters = TEST_UNSAFE_PARAMETERS,
        )

        val coreOptions = options.mapToCore()

        assertEquals(
            listOf(CoreAttributeSet.BASIC, CoreAttributeSet.VISIT),
            coreOptions.attributeSets
        )
        assertEquals(HashMap(TEST_UNSAFE_PARAMETERS), coreOptions.addonAPI)
    }

    @Test
    fun `Test mapToCore() function for empty options`() {
        val coreOptions = RetrieveOptions().mapToCore()

        assertNull(coreOptions.attributeSets)
        assertNull(coreOptions.addonAPI)
    }

    @Test
    fun `Test mapToCore() function for options with all the attribute sets`() {
        val coreOptions = RetrieveOptions(attributeSets = AttributeSet.values().toList()).mapToCore()

        assertEquals(CoreAttributeSet.values().toList(), coreOptions.attributeSets)
        assertNull(coreOptions.addonAPI)
    }

    @Test
    fun `Test mapToCore() function for options with empty collections`() {
        val coreOptions = RetrieveOptions(
            attributeSets = emptyList(),
            unsafeParameters = emptyMap(),
        ).mapToCore()

        assertEquals(emptyList<CoreAttributeSet>(), coreOptions.attributeSets)
        assertEquals(HashMap<String, String>(), coreOptions.addonAPI)
    }

    private companion object {

        val TEST_UNSAFE_PARAMETERS = mapOf(
            "unsafe_key_1" to "unsafe_value_1",
            "unsafe_key_2" to "unsafe_value_2",
        )
    }
}
