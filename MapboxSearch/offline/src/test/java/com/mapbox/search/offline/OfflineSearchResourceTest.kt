package com.mapbox.search.offline

import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.base.core.CoreOfflineSearchResource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@OptIn(MapboxExperimental::class)
internal class OfflineSearchResourceTest {

    @Test
    fun `Check Japanese dictionary maps to CoreOfflineSearchResource JAPANESE_DICTIONARY`() {
        assertEquals(
            listOf(CoreOfflineSearchResource.JAPANESE_DICTIONARY),
            OfflineSearchResource.mapToCoreOfflineSearchResource(
                listOf(OfflineSearchResource.JAPANESE_DICTIONARY)
            ),
        )
    }

    @Test
    fun `Check no resources map to no core resources`() {
        assertEquals(
            emptyList<CoreOfflineSearchResource>(),
            OfflineSearchResource.mapToCoreOfflineSearchResource(emptyList()),
        )
    }

    @Test
    fun `Check createTilesetDescriptor rejects unsupported resources`() {
        assertThrows<IllegalArgumentException> {
            OfflineSearchEngine.createTilesetDescriptor(
                tilesets = listOf(TilesetParameters.Builder().build()),
                additionalResources = listOf("unknown"),
            )
        }
    }
}
