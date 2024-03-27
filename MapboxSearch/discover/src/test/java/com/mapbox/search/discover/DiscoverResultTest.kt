package com.mapbox.search.discover

import com.mapbox.geojson.Point
import com.mapbox.search.base.core.countryIso1
import com.mapbox.search.base.core.countryIso2
import com.mapbox.search.base.result.BaseSearchResultType
import com.mapbox.search.base.result.BaseServerSearchResultImpl
import com.mapbox.search.base.result.mapToBase
import com.mapbox.search.base.utils.extension.mapToPlatform
import com.mapbox.search.common.tests.CommonSdkTypeObjectCreators
import com.mapbox.search.common.tests.ReflectionObjectsFactory
import com.mapbox.search.common.tests.ToStringVerifier
import com.mapbox.search.common.tests.createCoreSearchAddress
import com.mapbox.search.common.tests.createCoreSearchAddressCountry
import com.mapbox.search.common.tests.createCoreSearchAddressRegion
import com.mapbox.search.common.tests.createTestCoreRoutablePoint
import com.mapbox.search.common.tests.createTestCoreSearchResult
import com.mapbox.search.common.tests.createTestResultMetadata
import com.mapbox.search.common.tests.withPrefabTestPoint
import io.mockk.mockk
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class DiscoverResultTest {

    @Test
    fun `equals() and hashCode() functions are correct`() {
        EqualsVerifier.forClass(DiscoverResult::class.java)
            .withPrefabTestPoint()
            .verify()
    }

    @Test
    fun `toString() function is correct`() {
        ToStringVerifier(
            clazz = DiscoverResult::class,
            objectsFactory = ReflectionObjectsFactory(
                extraCreators = CommonSdkTypeObjectCreators.ALL_CREATORS
            ),
            includeAllProperties = false
        ).verify()
    }

    @Test
    fun `Check DiscoverApiResult`() {
        val coreMetadata = createTestResultMetadata(
            data = hashMapOf("iso_3166_1" to "fra", "iso_3166_2" to "fr")
        )

        val coreAddress = createCoreSearchAddress(
            houseNumber = "test-house-number",
            street = "test-street",
            neighborhood = "test-neighborhood",
            locality = "test-locality",
            postcode = "test-postcode",
            place = "test-place",
            district = "test-district",
            region = createCoreSearchAddressRegion("test-region"),
            country = createCoreSearchAddressCountry("test-country"),
        )

        val coreResult = createTestCoreSearchResult(
            names = listOf("Cafe de Paris"),
            addresses = listOf(coreAddress),
            center = Point.fromLngLat(123.0, 456.0),
            routablePoints = listOf(createTestCoreRoutablePoint()),
            categories = listOf("restaurant", "cafe"),
            categoryIds = listOf("restaurant-id", "cafe-id"),
            brand = listOf("test-brand"),
            brandId = "test-brand-id",
            icon = "cafe",
            metadata = coreMetadata
        )

        val baseResult = BaseServerSearchResultImpl(
            listOf(BaseSearchResultType.POI),
            coreResult.mapToBase(),
            mockk(),
        )

        val discoverResult = DiscoverResult.createFromSearchResult(baseResult)

        assertEquals(coreResult.names.first(), discoverResult.name)
        assertEquals(coreResult.center, discoverResult.coordinate)
        assertEquals(coreResult.routablePoints?.map { it.mapToPlatform() }, discoverResult.routablePoints)
        assertEquals(coreResult.categories, discoverResult.categories)
        assertEquals(coreResult.icon, discoverResult.makiIcon)

        assertEquals(coreAddress.houseNumber, discoverResult.address.houseNumber)
        assertEquals(coreAddress.street, discoverResult.address.street)
        assertEquals(coreAddress.neighborhood, discoverResult.address.neighborhood)
        assertEquals(coreAddress.locality, discoverResult.address.locality)
        assertEquals(coreAddress.postcode, discoverResult.address.postcode)
        assertEquals(coreAddress.place, discoverResult.address.place)
        assertEquals(coreAddress.district, discoverResult.address.district)
        assertEquals(coreAddress.region?.name, discoverResult.address.region)
        assertEquals(coreAddress.country?.name, discoverResult.address.country)
        assertEquals(coreMetadata.countryIso1, discoverResult.address.countryIso1)
        assertEquals(coreMetadata.countryIso2, discoverResult.address.countryIso2)
    }
}
