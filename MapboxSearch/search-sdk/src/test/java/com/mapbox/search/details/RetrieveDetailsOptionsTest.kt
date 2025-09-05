package com.mapbox.search.details

import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.AttributeSet
import com.mapbox.search.base.core.CoreAttributeSet
import com.mapbox.search.common.IsoCountryCode
import com.mapbox.search.common.IsoLanguageCode
import com.mapbox.search.common.tests.ToStringVerifier
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@OptIn(MapboxExperimental::class)
internal class RetrieveDetailsOptionsTest {

    @Test
    fun `Test generated equals(), hashCode() and toString() methods`() {
        EqualsVerifier.forClass(RetrieveDetailsOptions::class.java)
            .verify()

        ToStringVerifier(RetrieveDetailsOptions::class).verify()
    }

    @Test
    fun `Test mapToCore() function`() {
        val options = RetrieveDetailsOptions(
            attributeSets = listOf(AttributeSet.BASIC, AttributeSet.VISIT),
            language = IsoLanguageCode.FRENCH,
            worldview = IsoCountryCode.FRANCE,
        )

        val coreOptions = options.mapToCore()

        assertEquals(
            listOf(CoreAttributeSet.BASIC, CoreAttributeSet.VISIT),
            coreOptions.attributeSets
        )
        assertEquals(IsoLanguageCode.FRENCH.code, coreOptions.language)
        assertEquals(IsoCountryCode.FRANCE.code, coreOptions.worldview)
    }
}
