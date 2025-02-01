package com.mapbox.search.offline

import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.base.core.CoreDisplayText
import com.mapbox.search.base.core.CoreEvMetadata
import com.mapbox.search.base.core.CoreFacility
import com.mapbox.search.base.core.CoreParkingType
import com.mapbox.search.base.core.CoreResultMetadata
import com.mapbox.search.base.core.createCoreOpenHours
import com.mapbox.search.base.factory.createCoreImageInfo
import com.mapbox.search.base.factory.mapToPlatform
import com.mapbox.search.base.mapToPlatform
import com.mapbox.search.common.tests.ToStringVerifier
import com.mapbox.search.common.tests.withPrefabTestBoundingBox
import com.mapbox.search.common.tests.withPrefabTestPoint
import com.mapbox.search.internal.bindgen.OpenMode
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

@OptIn(MapboxExperimental::class)
internal class OfflineSearchResultMetadataTest {

    @Test
    fun `test equals(), hashCode(), toString()`() {
        EqualsVerifier
            .forClass(OfflineSearchResultMetadata::class.java)
            .withPrefabTestPoint()
            .withPrefabTestBoundingBox()
            .withOnlyTheseFields("coreMetadata")
            .verify()

        ToStringVerifier(
            clazz = OfflineSearchResultMetadata::class,
            includeAllProperties = false,
            ignoredProperties = listOf("coreMetadata"),
        ).verify()
    }

    @Test
    fun `fields access test`() {
        val mockData = mockk<HashMap<String, String>>(relaxed = true)
        val testWebsite = "https://test.com"
        val primaryPhotos = listOf(createCoreImageInfo("https://test.com/primary.png", 300, 100))
        val otherPhotos = listOf(createCoreImageInfo("https://test.com/other.png", 300, 100))
        val openHours = createCoreOpenHours(OpenMode.ALWAYS_OPEN, emptyList())
        val parkingAvailable = true
        val parkingType = CoreParkingType.PARKING_GARAGE
        val directions = listOf(CoreDisplayText("en", "Turn right"))
        val streetParking = true
        val evMetadata = mockk<CoreEvMetadata>(relaxed = true)
        val facilities = listOf(CoreFacility.CAFE)
        val timezone = "Europe/Paris"
        val lastUpdated = "2025-01-01"

        val coreMetadata = mockk<CoreResultMetadata>(relaxed = true).apply {
            every { this@apply.data } returns mockData
            every { this@apply.website } returns testWebsite
            every { this@apply.primaryPhoto } returns primaryPhotos
            every { this@apply.otherPhoto } returns otherPhotos
            every { this@apply.openHours } returns openHours
            every { this@apply.parkingAvailable } returns parkingAvailable
            every { this@apply.parkingType } returns parkingType
            every { this@apply.directions } returns directions
            every { this@apply.streetParking } returns streetParking
            every { this@apply.evMetadata } returns evMetadata
            every { this@apply.facilities } returns facilities
            every { this@apply.timezone } returns timezone
            every { this@apply.lastUpdated } returns lastUpdated
        }

        val metadata = OfflineSearchResultMetadata(coreMetadata)
        verify(exactly = 1) {
            coreMetadata.data
            coreMetadata.website
            coreMetadata.primaryPhoto
            coreMetadata.otherPhoto
            coreMetadata.openHours
            coreMetadata.parkingAvailable
            coreMetadata.parkingType
            coreMetadata.directions
            coreMetadata.streetParking
            coreMetadata.evMetadata
            coreMetadata.facilities
            coreMetadata.timezone
            coreMetadata.lastUpdated
        }
        assertSame(mockData, metadata.extraData)
        assertSame(testWebsite, metadata.website)
        assertEquals(primaryPhotos.map { it.mapToPlatform() }, metadata.primaryPhotos)
        assertEquals(otherPhotos.map { it.mapToPlatform() }, metadata.otherPhotos)
        assertEquals(openHours.mapToPlatform(), metadata.openHours)
        assertEquals(parkingAvailable, metadata.parkingAvailable)
        assertEquals(parkingType.mapToPlatform(), metadata.parkingType)
        assertEquals(directions.map { it.mapToPlatform() }, metadata.directions)
        assertEquals(streetParking, metadata.streetParking)
        assertEquals(evMetadata.mapToPlatform(), metadata.evMetadata)
        assertEquals(facilities.map { it.mapToPlatform() }, metadata.facilities)
        assertEquals(timezone, metadata.timezone)
        assertEquals(lastUpdated, metadata.lastUpdated)
    }
}
