package com.mapbox.search.base.factory

import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.base.core.CoreDisplayText
import com.mapbox.search.common.LocalizedText
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@OptIn(MapboxExperimental::class)
class LocalizedTextTest {

    @Test
    fun `CoreDisplayText mapToPlatform() test`() {
        val coreDisplayText = CoreDisplayText(
            language = "en",
            text = "Test text"
        )

        val expectedLocalizedText = LocalizedText(
            language = coreDisplayText.language,
            text = coreDisplayText.text
        )

        assertEquals(expectedLocalizedText, coreDisplayText.mapToPlatform())
    }
}
