package com.mapbox.search.base.factory

import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.base.core.CoreBusinessDetails
import com.mapbox.search.common.BusinessDetails
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@OptIn(MapboxExperimental::class)
class BusinessDetailsTest {

    @Test
    fun `mapToPlatform test`() {
        val core = CoreBusinessDetails(
            name = "test-name",
            website = "https://test.com",
            logo = createCoreImageInfo(
                url = "https://test.com/img.png",
                width = 500,
                height = 300,
            )
        )

        val expected = BusinessDetails(
            name = core.name,
            website = core.website,
            logo = core.logo?.mapToPlatform()
        )

        assertEquals(expected, core.mapToPlatform())
    }
}
