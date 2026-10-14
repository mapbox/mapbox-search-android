package com.mapbox.search.offline

import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.base.core.CoreOfflineSearchLanguage
import com.mapbox.search.base.core.CoreOfflineSearchTileset
import com.mapbox.search.base.core.CoreOfflineSearchTilesetSelection
import com.mapbox.search.common.IsoCountryCode
import com.mapbox.search.common.IsoLanguageCode
import com.mapbox.search.common.tests.ToStringVerifier
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@OptIn(MapboxExperimental::class)
internal class TilesetParametersTest {

    @Test
    fun `Check TilesetParameters equals(), hashCode(), and toString()`() {
        EqualsVerifier.forClass(TilesetParameters::class.java)
            .withIgnoredFields("generatedDatasetName\$delegate")
            .verify()

        ToStringVerifier(
            clazz = TilesetParameters::class,
            ignoredProperties = listOf("generatedDatasetName")
        ).verify()
    }

    @Test
    fun `Check DEFAULT_DATASET and DEFAULT_VERSION fields`() {
        assertEquals("mbx-gen2", TilesetParameters.DEFAULT_DATASET)
        assertEquals("", TilesetParameters.DEFAULT_VERSION)
    }

    @Test
    fun `Check TilesetParameter default Builder`() {
        val tilesetParameters = TilesetParameters.Builder().build()

        assertEquals(
            DatasetNameBuilder.buildDatasetName(
                dataset = TilesetParameters.DEFAULT_DATASET,
                language = null,
                worldview = null,
            ),
            tilesetParameters.generatedDatasetName
        )

        assertEquals(
            TilesetParameters.DEFAULT_VERSION,
            tilesetParameters.version
        )
    }

    @Suppress("DEPRECATION")
    @Test
    fun `Check Builder's dataset and version parameters`() {
        val tilesetParameters = TilesetParameters.Builder("test-dataset", "test-version")
            .build()

        assertEquals(
            DatasetNameBuilder.buildDatasetName(
                dataset = "test-dataset",
                language = null,
                worldview = null,
            ),
            tilesetParameters.generatedDatasetName
        )

        assertEquals(
            "test-version",
            tilesetParameters.version
        )
    }

    @Test
    fun `Check Builder's language()`() {
        val tilesetParameters = TilesetParameters.Builder()
            .language(IsoLanguageCode.ITALIAN)
            .build()

        assertEquals(
            DatasetNameBuilder.buildDatasetName(
                dataset = TilesetParameters.DEFAULT_DATASET,
                language = IsoLanguageCode.ITALIAN.code,
                worldview = null,
            ),
            tilesetParameters.generatedDatasetName
        )
    }

    @Test
    fun `Check Builder's worldview()`() {
        val tilesetParameters = TilesetParameters.Builder()
            .worldview(IsoLanguageCode.ENGLISH, IsoCountryCode.UNITED_KINGDOM)
            .build()

        assertEquals(
            DatasetNameBuilder.buildDatasetName(
                dataset = TilesetParameters.DEFAULT_DATASET,
                language = IsoLanguageCode.ENGLISH.code,
                worldview = IsoCountryCode.UNITED_KINGDOM.code,
            ),
            tilesetParameters.generatedDatasetName
        )
    }

    @Test
    fun `Check Builder's worldview() call replaces previously set language`() {
        val tilesetParameters = TilesetParameters.Builder()
            .language(IsoLanguageCode.CZECH)
            .worldview(IsoLanguageCode.SPANISH, IsoCountryCode.SPAIN)
            .build()

        assertEquals(
            DatasetNameBuilder.buildDatasetName(
                dataset = TilesetParameters.DEFAULT_DATASET,
                language = IsoLanguageCode.SPANISH.code,
                worldview = IsoCountryCode.SPAIN.code,
            ),
            tilesetParameters.generatedDatasetName
        )
    }

    @Test
    fun `Check Builder's overviewOnly()`() {
        assertEquals(false, TilesetParameters.Builder().build().overviewOnly)
        assertEquals(true, TilesetParameters.Builder().overviewOnly(true).build().overviewOnly)
    }

    @Suppress("DEPRECATION")
    @Test
    fun `Check mapping to core passes parameters`() {
        val tilesetParameters = TilesetParameters.Builder("test-dataset", "test-version")
            .worldview(IsoLanguageCode.FRENCH, IsoCountryCode.UNITED_STATES)
            .overviewOnly(true)
            .build()

        assertEquals(
            CoreOfflineSearchTileset(
                "test-dataset",
                listOf(CoreOfflineSearchLanguage("fr", "us")),
                true,
            ),
            tilesetParameters.mapToCore()
        )
    }

    @Test
    fun `Check mapping to core lowercases language and worldview`() {
        val tilesetParameters = TilesetParameters.Builder()
            .worldview(IsoLanguageCode("FR"), IsoCountryCode("US"))
            .build()

        assertEquals(
            listOf(CoreOfflineSearchLanguage("fr", "us")),
            tilesetParameters.mapToCore().languages,
        )
        assertEquals(
            CoreOfflineSearchLanguage("fr", "us"),
            tilesetParameters.getCoreOfflineSearchTilesetSelection().language,
        )
    }

    @Test
    fun `Check mapping to core validates language`() {
        val tilesetParameters = TilesetParameters.Builder()
            .language(IsoLanguageCode("english"))
            .build()

        assertThrows<IllegalArgumentException> {
            tilesetParameters.mapToCore()
        }
        assertThrows<IllegalArgumentException> {
            tilesetParameters.getCoreOfflineSearchTilesetSelection()
        }
    }

    @Test
    fun `Check mapping to core with default parameters`() {
        assertEquals(
            CoreOfflineSearchTileset(TilesetParameters.DEFAULT_DATASET, emptyList(), false),
            TilesetParameters.Builder().build().mapToCore()
        )
    }

    @Suppress("DEPRECATION")
    @Test
    fun `Check mapping to core selection`() {
        assertEquals(
            CoreOfflineSearchTilesetSelection("test-dataset", CoreOfflineSearchLanguage("fr", null)),
            TilesetParameters.Builder("test-dataset", "test-version")
                .language(IsoLanguageCode.FRENCH)
                .overviewOnly(true)
                .build()
                .getCoreOfflineSearchTilesetSelection()
        )
        assertEquals(
            CoreOfflineSearchTilesetSelection(TilesetParameters.DEFAULT_DATASET, null),
            TilesetParameters.Builder().build().getCoreOfflineSearchTilesetSelection()
        )
    }
}
