package com.mapbox.search.base.factory

import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.base.core.CoreImageCategory
import com.mapbox.search.base.core.CoreImageInfo
import com.mapbox.search.common.metadata.ImageCategory
import com.mapbox.search.common.metadata.ImageInfo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

@OptIn(MapboxExperimental::class)
class ImageTest {

    @Test
    fun `CoreImageCategory mapToPlatform() test`() {
        assertEquals(ImageCategory.EV_CHARGER, CoreImageCategory.CHARGER.mapToPlatform())
        assertEquals(ImageCategory.ENTRANCE, CoreImageCategory.ENTRANCE.mapToPlatform())
        assertEquals(ImageCategory.LOCATION, CoreImageCategory.LOCATION.mapToPlatform())
        assertEquals(ImageCategory.EV_NETWORK, CoreImageCategory.NETWORK.mapToPlatform())
        assertEquals(ImageCategory.EV_OPERATOR, CoreImageCategory.OPERATOR.mapToPlatform())
        assertEquals(ImageCategory.OTHER, CoreImageCategory.OTHER.mapToPlatform())
        assertEquals(ImageCategory.EV_OWNER, CoreImageCategory.OWNER.mapToPlatform())
    }

    @Test
    fun `createCoreImageCategory() test`() {
        assertEquals(CoreImageCategory.CHARGER, createCoreImageCategory(ImageCategory.EV_CHARGER))
        assertEquals(CoreImageCategory.NETWORK, createCoreImageCategory(ImageCategory.EV_NETWORK))
        assertEquals(CoreImageCategory.OPERATOR, createCoreImageCategory(ImageCategory.EV_OPERATOR))
        assertEquals(CoreImageCategory.OWNER, createCoreImageCategory(ImageCategory.EV_OWNER))
        assertEquals(CoreImageCategory.ENTRANCE, createCoreImageCategory(ImageCategory.ENTRANCE))
        assertEquals(CoreImageCategory.LOCATION, createCoreImageCategory(ImageCategory.LOCATION))
        assertEquals(CoreImageCategory.OTHER, createCoreImageCategory(ImageCategory.OTHER))
        assertNull(createCoreImageCategory(null))
        assertNull(createCoreImageCategory("UNKNOWN_TYPE"))
    }

    @Test
    fun `CoreImageInfo mapToPlatform() test`() {
        val coreImageInfo = CoreImageInfo(
            url = "https://test.com/image.png",
            width = 800,
            height = 600,
            thumbnail = "https://test.com/thumb.png",
            category = CoreImageCategory.CHARGER,
            type = "image/png"
        )

        val expectedImageInfo = ImageInfo(
            url = coreImageInfo.url,
            width = coreImageInfo.width,
            height = coreImageInfo.height,
            thumbnailUrl = coreImageInfo.thumbnail,
            imageCategory = coreImageInfo.category?.mapToPlatform(),
            type = coreImageInfo.type
        )

        assertEquals(expectedImageInfo, coreImageInfo.mapToPlatform())
    }

    @Test
    fun `ImageInfo mapToCore() test`() {
        val imageInfo = ImageInfo(
            url = "https://test.com/image.png",
            width = 800,
            height = 600,
            thumbnailUrl = "https://test.com/thumb.png",
            imageCategory = ImageCategory.EV_CHARGER,
            type = "image/png"
        )

        val expectedCoreImageInfo = CoreImageInfo(
            url = imageInfo.url,
            width = imageInfo.width,
            height = imageInfo.height,
            thumbnail = imageInfo.thumbnailUrl,
            category = createCoreImageCategory(imageInfo.imageCategory),
            type = imageInfo.type
        )

        assertEquals(expectedCoreImageInfo, imageInfo.mapToCore())
    }
}
