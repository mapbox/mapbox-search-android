package com.mapbox.search.common

import android.util.Base64
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.slot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

internal class OsmIdUtilsTest {

    @Test
    fun `Convert Node POID ID to OSM ID`() {
        // urn:mbxpoi-osm:n123695063255
        val expected = "dXJuOm1ieHBvaS1vc206bjEyMzY5NTA2MzI1NQ=="
        assertEquals(expected, OsmIdUtils.fromPoiId(1236950632550))
    }

    @Test
    fun `Convert way POID ID to OSM ID`() {
        // urn:mbxpoi-osm:w123695063255
        val expected = "dXJuOm1ieHBvaS1vc206dzEyMzY5NTA2MzI1NQ=="
        assertEquals(expected, OsmIdUtils.fromPoiId(1236950632551))
    }

    @Test
    fun `Convert relation POID ID to OSM ID`() {
        // urn:mbxpoi-osm:r123695063255
        val expected = "dXJuOm1ieHBvaS1vc206cjEyMzY5NTA2MzI1NQ=="
        assertEquals(expected, OsmIdUtils.fromPoiId(1236950632554))
    }

    @Test
    fun `Throws IllegalArgumentException`() {
        assertThrows(IllegalArgumentException::class.java) {
            OsmIdUtils.fromPoiId(12369506325519)
        }
    }

    companion object {
        @JvmStatic
        @BeforeAll
        fun setUp() {
            mockkStatic(Base64::class)
            val arraySlot = slot<ByteArray>()

            every {
                Base64.encodeToString(capture(arraySlot), Base64.DEFAULT)
            } answers {
                java.util.Base64.getEncoder().encodeToString(arraySlot.captured)
            }

            val stringSlot = slot<String>()
            every {
                Base64.decode(capture(stringSlot), Base64.DEFAULT)
            } answers {
                java.util.Base64.getDecoder().decode(stringSlot.captured)
            }
        }
    }
}
