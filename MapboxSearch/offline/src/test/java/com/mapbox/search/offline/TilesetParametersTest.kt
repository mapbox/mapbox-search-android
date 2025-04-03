package com.mapbox.search.offline

import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.common.IsoCountryCode
import com.mapbox.search.common.IsoLanguageCode
import com.mapbox.search.common.tests.ToStringVerifier
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

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
}
