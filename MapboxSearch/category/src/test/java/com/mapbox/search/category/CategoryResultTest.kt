package com.mapbox.search.category

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

internal class CategoryResultTest {

    @Test
    fun `equals() and hashCode() functions are correct`() {
        EqualsVerifier.forClass(CategoryResult::class.java)
            .withPrefabTestPoint()
            .verify()
    }

    @Test
    fun `toString() function is correct`() {
        ToStringVerifier(
            clazz = CategoryResult::class,
            objectsFactory = ReflectionObjectsFactory(
                extraCreators = CommonSdkTypeObjectCreators.ALL_CREATORS
            ),
            includeAllProperties = false
        ).verify()
    }

    @Test
    fun `Check CategoryApiResult`() {
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

        val categoryResult = CategoryResult.createFromSearchResult(baseResult)

        assertEquals(coreResult.names.first(), categoryResult.name)
        assertEquals(coreResult.center, categoryResult.coordinate)
        assertEquals(coreResult.routablePoints?.map { it.mapToPlatform() }, categoryResult.routablePoints)
        assertEquals(coreResult.categories, categoryResult.categories)
        assertEquals(coreResult.icon, categoryResult.makiIcon)

        assertEquals(coreAddress.houseNumber, categoryResult.address.houseNumber)
        assertEquals(coreAddress.street, categoryResult.address.street)
        assertEquals(coreAddress.neighborhood, categoryResult.address.neighborhood)
        assertEquals(coreAddress.locality, categoryResult.address.locality)
        assertEquals(coreAddress.postcode, categoryResult.address.postcode)
        assertEquals(coreAddress.place, categoryResult.address.place)
        assertEquals(coreAddress.district, categoryResult.address.district)
        assertEquals(coreAddress.region?.name, categoryResult.address.region)
        assertEquals(coreAddress.country?.name, categoryResult.address.country)
        assertEquals(coreMetadata.countryIso1, categoryResult.address.countryIso1)
        assertEquals(coreMetadata.countryIso2, categoryResult.address.countryIso2)
    }
}
