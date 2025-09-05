package com.mapbox.search

import com.mapbox.search.base.utils.extension.mapToCore
import com.mapbox.search.common.IsoCountryCode
import com.mapbox.search.common.IsoLanguageCode
import com.mapbox.search.common.RestrictedMapboxSearchAPI
import com.mapbox.search.common.tests.ToStringVerifier
import com.mapbox.search.common.tests.withPrefabTestPoint
import com.mapbox.search.tests_support.TestObjects
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Locale

@OptIn(RestrictedMapboxSearchAPI::class)
internal class BrandSearchOptionsTest {

    private lateinit var defaultLocale: Locale

    @BeforeEach
    fun setUp() {
        defaultLocale = Locale.getDefault()
        Locale.setDefault(TestObjects.TEST_LOCALE)
    }

    @AfterEach
    fun tearDown() {
        Locale.setDefault(defaultLocale)
    }

    @Test
    fun `test equals and hashCode functions`() {
        EqualsVerifier
            .forClass(BrandSearchOptions::class.java)
            .withPrefabTestPoint()
            .verify()
    }

    @Test
    fun `test toString function`() {
        ToStringVerifier(
            clazz = BrandSearchOptions::class,
            includeAllProperties = false
        ).verify()
    }

    @Test
    fun `test default constructor`() {
        val options = BrandSearchOptions()
        assertNull(options.proximity)
        assertNull(options.boundingBox)
        assertNull(options.countries)
        assertNull(options.limit)
        assertNull(options.showClosedPOIs)
        assertNull(options.unsafeParameters)
        assertEquals(TestObjects.TEST_LOCALE.language, options.language?.code)
    }

    @Test
    fun `test map to core`() {
        val options = BrandSearchOptions(
            proximity = TestObjects.TEST_POINT,
            boundingBox = TestObjects.TEST_BOUNDING_BOX,
            countries = listOf(IsoCountryCode.FRANCE, IsoCountryCode.UNITED_KINGDOM),
            language = IsoLanguageCode.FRENCH,
            limit = 15,
            showClosedPOIs = true,
            unsafeParameters = mapOf("arg1" to "val1", "arg2" to "val2")
        )

        val testQuery = "test-brand-name"
        val core = options.mapToCoreBrandOptions(testQuery)
        assertEquals(testQuery, core.query)
        assertEquals(options.proximity, core.proximity)
        assertEquals(options.boundingBox?.mapToCore(), core.bbox)
        assertEquals(options.countries?.map { it.code }, core.countries)
        assertEquals(
            options.language?.code?.let { listOf(it) },
            core.language,
        )
        assertEquals(options.limit, core.limit)
        assertEquals(options.showClosedPOIs, core.showClosedPois)
        assertEquals(options.unsafeParameters, core.addonAPI)
    }
}
